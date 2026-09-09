const cheerio = require("cheerio");
const { logger } = require("firebase-functions");

/**
 * Minimal, conservative fallback source for knockout-phase competitions
 * football-data.org's free plan doesn't cover - see
 * functions/knockoutFallbackSync.js for how this is used and which
 * competitions/phases are actually configured today. We only ever read bare
 * facts (matchup, date, final score), at low frequency (a couple of times a
 * day), identifying ourselves honestly via User-Agent - never
 * live/minute-by-minute data.
 *
 * The base URL is a parameter, never hardcoded here: each competition
 * resolves its own from its own Secret Manager entry (see
 * knockoutFallbackSync.js and index.js), so a source can be swapped later
 * without a code change, and different competitions can point at different
 * pages of the same site. The page is expected to expose two listings under
 * this base - "" (upcoming) and "/resultados" (finished) - each grouping
 * match cards under a date heading, with a phase label, kickoff time, team
 * names and (once finished) final scores.
 */
const USER_AGENT = "BolaoDaGaleraBot/1.0 (+https://bolaodagalera-bb002.web.app)";
const SEASON_YEAR = 2026;

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
 * The source's date headers carry an explicit "DD/MM" only for matches more
 * than a day out - matches happening today/tomorrow are headed just "Hoje"/
 * "Amanhã", with no date at all. Resolves those two relative to the current
 * date in Brasília time (fixed -03:00 offset - Brazil has had no DST since
 * 2019, same assumption toMatchDateMillis above already makes). Returns null
 * for anything else, so the caller falls back to the normal comma-split
 * extraction unchanged.
 */
function resolveRelativeDayMonth(headerText) {
    const normalized = headerText.trim().toLowerCase();
    // "Ontem" (yesterday) shows up on the /resultados listing for a match that finished within
    // the last day - missing it here isn't just a missing "today" label, it silently produces
    // NaN via the comma-split fallback below, which then sorts unpredictably against real
    // timestamps in knockoutFallbackSync.js's groupIntoLegs (see the comment there).
    const daysAhead = { "ontem": -1, "hoje": 0, "amanhã": 1, "amanha": 1 }[normalized];
    if (daysAhead === undefined) return null;

    const brasiliaNow = new Date(Date.now() - 3 * 3_600_000);
    brasiliaNow.setUTCDate(brasiliaNow.getUTCDate() + daysAhead);
    const day = String(brasiliaNow.getUTCDate()).padStart(2, "0");
    const month = String(brasiliaNow.getUTCMonth() + 1).padStart(2, "0");
    return `${day}/${month}`;
}

/**
 * Extracts every card matching phaseLabel (e.g. "Quartas de Final",
 * "Semifinal", "Final") from one page (either the "próximos jogos" or
 * "resultados" listing) - each date header groups the matches
 * played/scheduled on that day.
 */
function parsePhaseCards(html, phaseLabel) {
    const $ = cheerio.load(html);
    const matches = [];

    $(".flex.flex-col.gap-8 > .min-w-0").each((_, dateBlock) => {
        const headerText = $(dateBlock).find("h3").first().text().trim();
        const dayMonth = resolveRelativeDayMonth(headerText) || headerText.split(",").pop().trim();
        if (!dayMonth) return;

        $(dateBlock)
            .find('a[href^="/aovivo/"]')
            .each((__, anchor) => {
                const $anchor = $(anchor);
                const anchorText = normalizeWhitespace($anchor.text());
                if (!anchorText.includes(phaseLabel)) return;

                const time = $anchor.find("time").first().text().trim();
                const finished = anchorText.includes("FIM DE JOGO");

                const teamAName = $anchor.find('[id^="jogo-card-team-a-"]').parent().find("span.truncate").first().text().trim();
                const teamBName = $anchor.find('[id^="jogo-card-team-b-"]').parent().find("span.truncate").first().text().trim();
                if (!teamAName || !teamBName) return;

                let teamAScore = null;
                let teamBScore = null;
                if (finished) {
                    // The score <span> is a sibling of the icon's *name-wrapper* div, not of the icon
                    // itself - one .parent() only reaches that wrapper (icon + name), missing the score
                    // entirely. Needs the wrapper's parent (the full row) to find it.
                    const scoreARaw = $anchor.find('[id^="jogo-card-team-a-"]').parent().parent().find("span.font-black").first().text().trim();
                    const scoreBRaw = $anchor.find('[id^="jogo-card-team-b-"]').parent().parent().find("span.font-black").first().text().trim();
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

/** Public entry point: fetches both listings under baseUrl and returns every match found for phaseLabel. */
async function scrapePhase(axios, baseUrl, phaseLabel) {
    const matches = [];
    for (const path of ["", "/resultados"]) {
        const url = `${baseUrl}${path}`;
        try {
            const html = await fetchHtml(axios, url);
            matches.push(...parsePhaseCards(html, phaseLabel));
        } catch (e) {
            const status = e.response?.status;
            const bodySnippet = typeof e.response?.data === "string" ? e.response.data.slice(0, 200) : "";
            logger.warn(`⚠️ Fonte de resultados indisponível em ${url} (status=${status}): ${e.message} ${bodySnippet}`);
        }
    }
    return matches;
}

module.exports = { scrapePhase, parsePhaseCards, toMatchDateMillis };
