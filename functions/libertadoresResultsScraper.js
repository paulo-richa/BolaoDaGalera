const cheerio = require("cheerio");
const { logger } = require("firebase-functions");

/**
 * Minimal, conservative fallback source for the Libertadores quarterfinals
 * only - see functions/libertadoresQuarterfinals.js for why this exists.
 * We only ever read bare facts (matchup, date, final score), at low
 * frequency (a couple of times a day), identifying ourselves honestly via
 * User-Agent - never live/minute-by-minute data.
 *
 * The base URL is intentionally not hardcoded: it's configured via
 * LIBERTADORES_RESULTS_SOURCE_URL (Secret Manager, see index.js), so the
 * source can be swapped later without a code change. The page is expected
 * to expose two listings under this base - "" (upcoming) and "/resultados"
 * (finished) - each grouping match cards under a date heading, with a
 * phase label, kickoff time, team names and (once finished) final scores.
 */
const USER_AGENT = "BolaoDaGaleraBot/1.0 (+https://bolaodagalera-bb002.web.app)";
const PHASE_LABEL = "Quartas de Final";
const SEASON_YEAR = 2026;

function baseUrl() {
    return (process.env.LIBERTADORES_RESULTS_SOURCE_URL || "").trim().replace(/\/+$/, "");
}

async function fetchHtml(axios, url) {
    const response = await axios.get(url, {
        headers: { "User-Agent": USER_AGENT },
        timeout: 15000
    });
    return response.data;
}

/** Collapses all whitespace (including the newlines cheerio's .text() preserves between sibling tags) to single spaces. */
function normalizeWhitespace(text) {
    return text.replace(/\s+/g, " ").trim();
}

/** Parses "HH:MM" (Brasília time, same convention as the rest of the sync code) + "DD/MM" into a UTC epoch. */
function toMatchDateMillis(dayMonth, time) {
    const [day, month] = dayMonth.split("/");
    const [hour, minute] = (time || "00:00").split(":");
    return Date.parse(`${SEASON_YEAR}-${month}-${day}T${hour}:${minute}:00-03:00`);
}

/**
 * Extracts every Quartas de Final card from one page (either the "próximos
 * jogos" or "resultados" listing) - each date header groups the matches
 * played/scheduled on that day.
 */
function parseQuarterfinalCards(html) {
    const $ = cheerio.load(html);
    const matches = [];

    $(".flex.flex-col.gap-8 > .min-w-0").each((_, dateBlock) => {
        const dayMonth = $(dateBlock).find("h3").first().text().trim().split(",").pop().trim();
        if (!dayMonth) return;

        $(dateBlock)
            .find('a[href^="/aovivo/"]')
            .each((__, anchor) => {
                const $anchor = $(anchor);
                const anchorText = normalizeWhitespace($anchor.text());
                if (!anchorText.includes(PHASE_LABEL)) return;

                const time = $anchor.find("time").first().text().trim();
                const finished = anchorText.includes("FIM DE JOGO");

                const teamAName = $anchor.find('[id^="jogo-card-team-a-"]').parent().find("span.truncate").first().text().trim();
                const teamBName = $anchor.find('[id^="jogo-card-team-b-"]').parent().find("span.truncate").first().text().trim();
                if (!teamAName || !teamBName) return;

                let teamAScore = null;
                let teamBScore = null;
                if (finished) {
                    const scoreARaw = $anchor.find('[id^="jogo-card-team-a-"]').parent().find("span.font-black").first().text().trim();
                    const scoreBRaw = $anchor.find('[id^="jogo-card-team-b-"]').parent().find("span.font-black").first().text().trim();
                    teamAScore = scoreARaw ? parseInt(scoreARaw, 10) : null;
                    teamBScore = scoreBRaw ? parseInt(scoreBRaw, 10) : null;
                }

                matches.push({
                    teamAName,
                    teamBName,
                    matchDateMillis: toMatchDateMillis(dayMonth, time),
                    finished: finished && teamAScore !== null && teamBScore !== null,
                    teamAScore,
                    teamBScore
                });
            });
    });

    return matches;
}

/** Public entry point: fetches both listings and returns every Quartas de Final match found. */
async function scrapeLibertadoresQuarterfinals(axios) {
    const base = baseUrl();
    if (!base) {
        logger.warn("⚠️ LIBERTADORES_RESULTS_SOURCE_URL não configurada - pulando fallback das quartas.");
        return [];
    }

    const matches = [];
    for (const path of ["", "/resultados"]) {
        const url = `${base}${path}`;
        try {
            const html = await fetchHtml(axios, url);
            matches.push(...parseQuarterfinalCards(html));
        } catch (e) {
            const status = e.response?.status;
            const bodySnippet = typeof e.response?.data === "string" ? e.response.data.slice(0, 200) : "";
            logger.warn(`⚠️ Fonte de resultados indisponível em ${url} (status=${status}): ${e.message} ${bodySnippet}`);
        }
    }
    return matches;
}

module.exports = { scrapeLibertadoresQuarterfinals, parseQuarterfinalCards, toMatchDateMillis };
