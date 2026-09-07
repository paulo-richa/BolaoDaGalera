const { logger } = require("firebase-functions");
const { scrapePhase } = require("./knockoutResultsScraper");
const { migratePredictionsIfMatchChanged } = require("./knockout");

/** Reads and sanitizes this competition's own base-URL secret (see index.js). */
function resolveBaseUrl(competition) {
    return (process.env[competition.sourceUrlEnvVar] || "").trim().replace(/\/+$/, "");
}

/**
 * Generic engine behind the external-source fallback used when
 * football-data.org's free plan doesn't cover a competition's current
 * knockout phases (see libertadoresQuarterfinals.js and copaDoBrasil.js for
 * the two competitions using it today). Reads ONLY bare facts (matchup,
 * date, final score) from a configurable external source, at low frequency.
 *
 * A "competition" using this engine provides:
 *   - championshipId: the Firestore championships/{id} this writes to.
 *   - idPrefix: match-id prefix, e.g. "CLI-2026-" or "CDB-2026-".
 *   - teamDictionary: map of canonical team key -> { name, code, flag, crest }.
 *   - nameAliases: map of the external source's exact team name -> a
 *     teamDictionary key (only needed where they don't already match).
 *   - checkpointCollection: Firestore collection name for this
 *     competition's own checkpoint markers (kept separate per competition
 *     so two competitions' checkpoints never collide).
 *
 * ...and a `phaseConfigs` map (phaseKey -> { sourceLabel, displayGroup,
 * singleMatch, ties }), same shape for every competition - see
 * libertadoresQuarterfinals.js's PHASE_CONFIGS for the documented example.
 * A phase with an empty `ties` list is skipped entirely (its matchups
 * aren't known yet), so adding a new phase later is just filling in `ties`.
 */

function tieIndexFor(config, teamAKey, teamBKey) {
    return config.ties.findIndex(
        (tie) => (tie[0] === teamAKey && tie[1] === teamBKey) || (tie[0] === teamBKey && tie[1] === teamAKey)
    );
}

/** Groups the scraped cards by tie and assigns leg 1/2 by date order (earlier = leg 1); a single-match phase keeps only the earliest card. */
function groupIntoLegs(competition, config, rawMatches) {
    const normalize = (rawName) => competition.nameAliases[rawName] || rawName;
    const byTie = config.ties.map(() => []);

    for (const raw of rawMatches) {
        const teamAKey = normalize(raw.teamAName);
        const teamBKey = normalize(raw.teamBName);
        const tieIndex = tieIndexFor(config, teamAKey, teamBKey);
        if (tieIndex === -1) {
            logger.warn(`⚠️ Fonte externa (${config.displayGroup}): confronto não reconhecido (${raw.teamAName} x ${raw.teamBName}), ignorando.`);
            continue;
        }
        byTie[tieIndex].push({ ...raw, teamAKey, teamBKey });
    }

    const legsPerTie = config.singleMatch ? 1 : 2;
    const legs = [];
    byTie.forEach((matches, tieIndex) => {
        matches
            .sort((a, b) => a.matchDateMillis - b.matchDateMillis)
            .slice(0, legsPerTie)
            .forEach((match, legIdx) => {
                legs.push({ ...match, tieIndex, leg: legIdx + 1 });
            });
    });
    return legs;
}

function teamFieldsFor(competition, key) {
    const team = competition.teamDictionary[key] || { name: key, flag: "", code: "TBD", crest: null };
    return { name: team.name, code: team.code, flag: team.flag, crest: team.crest };
}

function tieCodesFor(competition, config) {
    return config.ties.map((tie) => tie.map((key) => teamFieldsFor(competition, key).code));
}

function tieIndexForCodes(tieCodes, homeCode, awayCode) {
    return tieCodes.findIndex(
        (codes) => (codes[0] === homeCode && codes[1] === awayCode) || (codes[0] === awayCode && codes[1] === homeCode)
    );
}

function matchIdFor(competition, config, tieIndex, leg) {
    if (config.singleMatch) return `${competition.idPrefix}${config.idSuffix}`;
    return `${competition.idPrefix}${config.idSuffix}${tieIndex + 1}-L${leg}`;
}

function matchesRefFor(db, competition) {
    return db.collection("championships").doc(competition.championshipId).collection("matches");
}

/**
 * Firestore's own doc ID for each tie/leg, keyed by whatever a previous sync
 * already wrote there (e.g. football-data.org's partial sync for this phase,
 * which numbers ties by API response order, not the fixed order used here) -
 * without this, we'd create a second, empty document for a match a user
 * already predicted against under a different ID.
 */
async function findExistingDocsByTie(matchesRef, phaseKey, competition, config) {
    const tieCodes = tieCodesFor(competition, config);
    const snap = await matchesRef.where("phase", "==", phaseKey).get();
    const byTie = config.ties.map(() => []);

    snap.forEach((doc) => {
        const data = doc.data();
        const tieIndex = tieIndexForCodes(tieCodes, data.homeTeamCode, data.awayTeamCode);
        if (tieIndex === -1) return;
        byTie[tieIndex].push({ id: doc.id, data });
    });

    return byTie.map((docs) => docs.sort((a, b) => (a.data.matchDateMillis || 0) - (b.data.matchDateMillis || 0)));
}

// Minutes after kickoff to re-check a still-unfinished match: 2h30 covers
// normal time + stoppage for the vast majority of matches; the +30min
// follow-up exists for the rare tie that needed extra time (a two-legged
// aggregate tie only goes to penalties on the second leg, and only when
// still level after extra time - a penalty-shootout score is never
// recorded, only normal time + extra time).
const CHECKPOINT_MINUTES = [150, 180];

/**
 * Prevents the same (match, checkpoint) pair from re-triggering a sync on
 * every 15-min poll tick once it's due - same create()-is-atomic idiom as
 * matchReminder.js's claimReminder.
 */
async function claimCheckpoint(db, competition, matchId, checkpointMinutes) {
    try {
        await db.collection(competition.checkpointCollection).doc(`${matchId}_${checkpointMinutes}`).create({ claimedAtMillis: Date.now() });
        return true;
    } catch (e) {
        if (e.code === 6 || (e.message && e.message.includes("already exists"))) return false;
        throw e;
    }
}

/**
 * Cheap check (Firestore only, no external request) for whether any
 * not-yet-finished match in phaseKey has just crossed a checkpoint - the
 * external source is only actually fetched when this returns true, keeping
 * the frequent poll itself free of any request to that source.
 */
async function hasPhaseCheckpointDue(db, competition, phaseKey) {
    const snap = await matchesRefFor(db, competition).where("phase", "==", phaseKey).get();

    const now = Date.now();
    let anyDue = false;

    for (const doc of snap.docs) {
        const data = doc.data();
        if (data.status === "FINISHED" || !data.matchDateMillis) continue;

        for (const minutes of CHECKPOINT_MINUTES) {
            if (now < data.matchDateMillis + minutes * 60_000) continue;
            // eslint-disable-next-line no-await-in-loop
            if (await claimCheckpoint(db, competition, doc.id, minutes)) anyDue = true;
        }
    }

    return anyDue;
}

/** Is this phase ready to sync? A phase with no ties configured yet (ties TBD) is silently skipped. */
function isPhaseConfigured(config) {
    return config.ties.length > 0;
}

async function syncPhase(db, admin, axios, competition, phaseConfigs, phaseKey) {
    const config = phaseConfigs[phaseKey];
    if (!isPhaseConfigured(config)) return;

    const baseUrl = resolveBaseUrl(competition);
    if (!baseUrl) {
        logger.warn(`⚠️ ${competition.sourceUrlEnvVar} não configurada - pulando fallback de ${config.displayGroup} (${competition.championshipId}).`);
        return;
    }

    let rawMatches;
    try {
        rawMatches = await scrapePhase(axios, baseUrl, config.sourceLabel);
    } catch (e) {
        logger.error(`Erro ao buscar ${config.displayGroup} (${competition.championshipId}) na fonte externa:`, e.message);
        return;
    }
    if (rawMatches.length === 0) {
        logger.warn(`⚠️ Fonte externa não retornou nenhum jogo de ${config.displayGroup} (${competition.championshipId}).`);
        return;
    }

    const matchesRef = matchesRefFor(db, competition);
    const legs = groupIntoLegs(competition, config, rawMatches);
    const existingDocsByTie = await findExistingDocsByTie(matchesRef, phaseKey, competition, config);
    const migrationsNeeded = [];

    for (const legMatch of legs) {
        const existingForTie = existingDocsByTie[legMatch.tieIndex] || [];
        const matchedExisting = existingForTie[legMatch.leg - 1];
        const matchId = matchedExisting ? matchedExisting.id : matchIdFor(competition, config, legMatch.tieIndex, legMatch.leg);
        const homeTeam = teamFieldsFor(competition, legMatch.teamAKey);
        const awayTeam = teamFieldsFor(competition, legMatch.teamBKey);

        const currentDoc = matchedExisting ? { exists: true, data: () => matchedExisting.data } : await matchesRef.doc(matchId).get();
        if (currentDoc.exists && currentDoc.data().isManual) continue;

        const existing = currentDoc.exists ? currentDoc.data() : null;

        // Never let an incomplete/upcoming read overwrite an already-finished real result.
        const existingIsFinal = existing && existing.status === "FINISHED" &&
            existing.homeScore !== null && existing.awayScore !== null;
        if (existingIsFinal && !legMatch.finished) {
            continue;
        }

        const updates = {
            status: legMatch.finished ? "FINISHED" : "SCHEDULED",
            homeScore: legMatch.finished ? legMatch.teamAScore : null,
            awayScore: legMatch.finished ? legMatch.teamBScore : null,
            championshipId: competition.championshipId,
            phase: phaseKey,
            matchOrder: legMatch.tieIndex + 1,
            group: config.displayGroup,
            matchDateMillis: legMatch.matchDateMillis,
            homeTeam: homeTeam.name,
            homeTeamCode: homeTeam.code,
            homeTeamFlag: homeTeam.flag,
            homeTeamCrest: homeTeam.crest,
            awayTeam: awayTeam.name,
            awayTeamCode: awayTeam.code,
            awayTeamFlag: awayTeam.flag,
            awayTeamCrest: awayTeam.crest,
            lastSync: admin.firestore.FieldValue.serverTimestamp(),
            source: "external-fallback"
        };

        if (existing && existing.homeTeamCode && existing.awayTeamCode &&
            (existing.homeTeamCode !== updates.homeTeamCode || existing.awayTeamCode !== updates.awayTeamCode)) {
            migrationsNeeded.push({ matchId, oldMatch: existing, newMatch: updates });
        }

        await matchesRef.doc(matchId).set(updates, { merge: true });
    }

    for (const migration of migrationsNeeded) {
        await migratePredictionsIfMatchChanged(db, migration.matchId, migration.oldMatch, migration.newMatch);
    }

    logger.info(`${config.displayGroup} (${competition.championshipId}) sincronizada(s) via fonte externa (${legs.length} jogo(s)).`);
}

async function syncPhaseIfCheckpointDue(db, admin, axios, competition, phaseConfigs, phaseKey) {
    const config = phaseConfigs[phaseKey];
    if (!isPhaseConfigured(config)) return;
    if (!(await hasPhaseCheckpointDue(db, competition, phaseKey))) return;

    logger.info(`⏰ Checkpoint de ${config.displayGroup} (${competition.championshipId}) atingido, sincronizando...`);
    await syncPhase(db, admin, axios, competition, phaseConfigs, phaseKey);
}

module.exports = { syncPhase, syncPhaseIfCheckpointDue, groupIntoLegs, isPhaseConfigured };
