const { logger } = require("firebase-functions");
const { LIB_TEAMS } = require("./teams_lib");
const { scrapeLibertadoresPhase } = require("./libertadoresResultsScraper");
const { migratePredictionsIfMatchChanged } = require("./knockout");

/**
 * football-data.org's free plan doesn't cover Libertadores' current season
 * (see functions/fallback-api.js), which left the quarterfinals permanently
 * incomplete. A configurable external source (see
 * functions/libertadoresResultsScraper.js and LIBERTADORES_RESULTS_SOURCE_URL)
 * is used here ONLY as a fallback for phases not covered by football-data.org,
 * and only for the bare facts (matchup, date, final score) - never anything
 * else from the page. Group stage and Round of 16 keep coming from
 * football-data.org (already synced/concluded, untouched by this module).
 *
 * Structured to cover any knockout phase, not just the quarterfinals: each
 * entry in PHASE_CONFIGS describes one phase's source label, doc-id prefix
 * and known ties. SEMIFINALS/FINAL are listed with empty `ties` (their
 * matchups aren't known until the previous phase concludes) - a phase with
 * no ties configured is skipped entirely (see isPhaseConfigured), so nothing
 * needs to change here once the source publishes them, only the `ties` list
 * (and possibly a NAME_ALIASES entry for a team not seen before) - see the
 * TODO comments below.
 */

// The external source's team names, normalized to the LIB_TEAMS keys used
// elsewhere - add an entry here if a semifinal/final team's exact display
// name on the source doesn't match a LIB_TEAMS key already.
const NAME_ALIASES = {
    "Fluminense": "Fluminense FC",
    "Platense": "CA Platense",
    "Palmeiras": "SE Palmeiras",
    "LDU de Quito": "LDU de Quito",
    "Estudiantes": "Estudiantes de La Plata",
    "Estudiantes de La Plata": "Estudiantes de La Plata",
    "Corinthians": "SC Corinthians Paulista",
    "Independiente del Valle": "CAR Independiente del Valle",
    "Flamengo": "CR Flamengo"
};

/**
 * One entry per knockout phase this fallback can cover. `ties` is a list of
 * [teamAKey, teamBKey] pairs (LIB_TEAMS keys) - the order only defines this
 * module's internal match-id numbering, it has no bearing on which team is
 * "home" in either leg. `singleMatch: true` (FINAL) means exactly one match,
 * no ida/volta - its id has no "-L{leg}" suffix, matching the convention
 * football-data.org's sync already used for CLI-2026-FINAL.
 */
const PHASE_CONFIGS = {
    QUARTERFINALS: {
        sourceLabel: "Quartas de Final",
        idPrefix: "QF",
        displayGroup: "Quartas de Final",
        singleMatch: false,
        // Fixed real-world draw (see news coverage confirming all 4 ties on 2026-08-25).
        ties: [
            ["Estudiantes de La Plata", "SC Corinthians Paulista"],
            ["CAR Independiente del Valle", "CR Flamengo"],
            ["Fluminense FC", "CA Platense"],
            ["SE Palmeiras", "LDU de Quito"]
        ]
    },
    SEMIFINALS: {
        // TODO: confirm the exact phase label the source uses once it publishes
        // this round (likely "Semifinal") before relying on this entry.
        sourceLabel: "Semifinal",
        idPrefix: "SF",
        displayGroup: "Semifinal",
        singleMatch: false,
        // TODO: fill in once the quarterfinals decide who advances, e.g.:
        // [["Winner LIB_TEAMS key", "Winner LIB_TEAMS key"], [...]]
        ties: []
    },
    FINAL: {
        // TODO: confirm the exact phase label the source uses once it publishes
        // this round (likely "Final") before relying on this entry.
        sourceLabel: "Final",
        idPrefix: "FINAL",
        displayGroup: "Final",
        singleMatch: true,
        // TODO: fill in the single tie once the semifinals decide who advances.
        ties: []
    }
};

function normalizeTeamName(rawName) {
    return NAME_ALIASES[rawName] || rawName;
}

function tieIndexFor(config, teamAKey, teamBKey) {
    return config.ties.findIndex(
        (tie) => (tie[0] === teamAKey && tie[1] === teamBKey) || (tie[0] === teamBKey && tie[1] === teamAKey)
    );
}

/** Groups the scraped cards by tie and assigns leg 1/2 by date order (earlier = leg 1); a single-match phase keeps only the earliest card. */
function groupIntoLegs(config, rawMatches) {
    const byTie = config.ties.map(() => []);

    for (const raw of rawMatches) {
        const teamAKey = normalizeTeamName(raw.teamAName);
        const teamBKey = normalizeTeamName(raw.teamBName);
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

function teamFieldsFor(key) {
    const team = LIB_TEAMS[key] || { name: key, flag: "", code: "TBD", crest: null };
    return { name: team.name, code: team.code, flag: team.flag, crest: team.crest };
}

function tieCodesFor(config) {
    return config.ties.map((tie) => tie.map((key) => teamFieldsFor(key).code));
}

function tieIndexForCodes(tieCodes, homeCode, awayCode) {
    return tieCodes.findIndex(
        (codes) => (codes[0] === homeCode && codes[1] === awayCode) || (codes[0] === awayCode && codes[1] === homeCode)
    );
}

function matchIdFor(config, tieIndex, leg) {
    if (config.singleMatch) return `CLI-2026-${config.idPrefix}`;
    return `CLI-2026-${config.idPrefix}${tieIndex + 1}-L${leg}`;
}

/**
 * Firestore's own doc ID for each tie/leg, keyed by whatever a previous sync
 * already wrote there (e.g. football-data.org's partial sync for this phase,
 * which numbers ties by API response order, not the fixed order used here) -
 * without this, we'd create a second, empty document for a match a user
 * already predicted against under a different ID.
 */
async function findExistingDocsByTie(matchesRef, phaseKey, config) {
    const tieCodes = tieCodesFor(config);
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
// still level after extra time - see the module doc comment about NOT
// recording a penalty-shootout score, only normal time + extra time).
const CHECKPOINT_MINUTES = [150, 180];

/**
 * Prevents the same (match, checkpoint) pair from re-triggering a sync on
 * every 15-min poll tick once it's due - same create()-is-atomic idiom as
 * matchReminder.js's claimReminder.
 */
async function claimCheckpoint(db, matchId, checkpointMinutes) {
    try {
        await db.collection("libertadoresQuarterCheckpoints").doc(`${matchId}_${checkpointMinutes}`).create({ claimedAtMillis: Date.now() });
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
async function hasPhaseCheckpointDue(db, phaseKey) {
    const snap = await db.collection("championships").doc("LIBERTADORES").collection("matches")
        .where("phase", "==", phaseKey)
        .get();

    const now = Date.now();
    let anyDue = false;

    for (const doc of snap.docs) {
        const data = doc.data();
        if (data.status === "FINISHED" || !data.matchDateMillis) continue;

        for (const minutes of CHECKPOINT_MINUTES) {
            if (now < data.matchDateMillis + minutes * 60_000) continue;
            // eslint-disable-next-line no-await-in-loop
            if (await claimCheckpoint(db, doc.id, minutes)) anyDue = true;
        }
    }

    return anyDue;
}

/** Is this phase ready to sync? A phase with no ties configured yet (ties TBD) is silently skipped. */
function isPhaseConfigured(config) {
    return config.ties.length > 0;
}

async function syncLibertadoresPhase(db, admin, axios, phaseKey) {
    const config = PHASE_CONFIGS[phaseKey];
    if (!isPhaseConfigured(config)) return;

    let rawMatches;
    try {
        rawMatches = await scrapeLibertadoresPhase(axios, config.sourceLabel);
    } catch (e) {
        logger.error(`Erro ao buscar ${config.displayGroup} da Libertadores na fonte externa:`, e.message);
        return;
    }
    if (rawMatches.length === 0) {
        logger.warn(`⚠️ Fonte externa não retornou nenhum jogo de ${config.displayGroup}.`);
        return;
    }

    const matchesRef = db.collection("championships").doc("LIBERTADORES").collection("matches");
    const legs = groupIntoLegs(config, rawMatches);
    const existingDocsByTie = await findExistingDocsByTie(matchesRef, phaseKey, config);
    const migrationsNeeded = [];

    for (const legMatch of legs) {
        const existingForTie = existingDocsByTie[legMatch.tieIndex] || [];
        const matchedExisting = existingForTie[legMatch.leg - 1];
        const matchId = matchedExisting ? matchedExisting.id : matchIdFor(config, legMatch.tieIndex, legMatch.leg);
        const homeTeam = teamFieldsFor(legMatch.teamAKey);
        const awayTeam = teamFieldsFor(legMatch.teamBKey);

        const currentDoc = matchedExisting ? { exists: true, data: () => matchedExisting.data } : await matchesRef.doc(matchId).get();
        if (currentDoc.exists && currentDoc.data().isManual) continue;

        const existing = currentDoc.exists ? currentDoc.data() : null;

        // Same regression guard used by the football-data.org sync: never let an
        // incomplete/upcoming read overwrite an already-finished real result.
        const existingIsFinal = existing && existing.status === "FINISHED" &&
            existing.homeScore !== null && existing.awayScore !== null;
        if (existingIsFinal && !legMatch.finished) {
            continue;
        }

        const updates = {
            status: legMatch.finished ? "FINISHED" : "SCHEDULED",
            homeScore: legMatch.finished ? legMatch.teamAScore : null,
            awayScore: legMatch.finished ? legMatch.teamBScore : null,
            championshipId: "LIBERTADORES",
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

    logger.info(`${config.displayGroup} da Libertadores sincronizada(s) via fonte externa (${legs.length} jogo(s)).`);
}

async function syncLibertadoresPhaseIfCheckpointDue(db, admin, axios, phaseKey) {
    const config = PHASE_CONFIGS[phaseKey];
    if (!isPhaseConfigured(config)) return;
    if (!(await hasPhaseCheckpointDue(db, phaseKey))) return;

    logger.info(`⏰ Checkpoint de ${config.displayGroup} da Libertadores atingido, sincronizando...`);
    await syncLibertadoresPhase(db, admin, axios, phaseKey);
}

// Thin, phase-fixed wrappers so callers (index.js) don't need to change for
// the quarterfinals, which are already configured and deployed today.
async function syncLibertadoresQuarterfinals(db, admin, axios) {
    await syncLibertadoresPhase(db, admin, axios, "QUARTERFINALS");
}

async function syncLibertadoresQuarterfinalsIfCheckpointDue(db, admin, axios) {
    await syncLibertadoresPhaseIfCheckpointDue(db, admin, axios, "QUARTERFINALS");
}

module.exports = {
    syncLibertadoresQuarterfinals,
    syncLibertadoresQuarterfinalsIfCheckpointDue,
    syncLibertadoresPhase,
    syncLibertadoresPhaseIfCheckpointDue,
    groupIntoLegs,
    normalizeTeamName,
    PHASE_CONFIGS
};
