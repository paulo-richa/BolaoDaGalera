const { LIB_TEAMS } = require("./teams_lib");
const { syncPhase, syncPhaseIfCheckpointDue } = require("./knockoutFallbackSync");

/**
 * football-data.org's free plan doesn't cover Libertadores' current season
 * (see functions/fallback-api.js), which left the quarterfinals permanently
 * incomplete. A configurable external source (see
 * functions/libertadoresResultsScraper.js and LIBERTADORES_RESULTS_SOURCE_URL)
 * is used here ONLY as a fallback for phases not covered by football-data.org,
 * via the generic engine in knockoutFallbackSync.js - see that file for how
 * it reads only bare facts (matchup, date, final score). Group stage and
 * Round of 16 keep coming from football-data.org (already synced/concluded,
 * untouched by this module).
 *
 * SEMIFINALS/FINAL are listed with empty `ties` (their matchups aren't known
 * until the previous phase concludes) - a phase with no ties configured is
 * skipped entirely, so nothing needs to change here once the source
 * publishes them, only the `ties` list (and possibly a NAME_ALIASES entry
 * for a team not seen before) - see the TODO comments below.
 */
const COMPETITION = {
    championshipId: "LIBERTADORES",
    idPrefix: "CLI-2026-",
    checkpointCollection: "libertadoresQuarterCheckpoints",
    sourceUrlEnvVar: "LIBERTADORES_RESULTS_SOURCE_URL",
    teamDictionary: LIB_TEAMS,
    // The external source's team names, normalized to the LIB_TEAMS keys used
    // elsewhere - add an entry here if a semifinal/final team's exact display
    // name on the source doesn't match a LIB_TEAMS key already.
    nameAliases: {
        "Fluminense": "Fluminense FC",
        "Platense": "CA Platense",
        "Palmeiras": "SE Palmeiras",
        "LDU de Quito": "LDU de Quito",
        "Estudiantes": "Estudiantes de La Plata",
        "Estudiantes de La Plata": "Estudiantes de La Plata",
        "Corinthians": "SC Corinthians Paulista",
        "Independiente del Valle": "CAR Independiente del Valle",
        "Flamengo": "CR Flamengo"
    }
};

/**
 * One entry per knockout phase this fallback can cover. `ties` is a list of
 * [teamAKey, teamBKey] pairs (teamDictionary keys) - the order only defines
 * this module's internal match-id numbering, it has no bearing on which
 * team is "home" in either leg. `singleMatch: true` (FINAL) means exactly
 * one match, no ida/volta - its id has no "-L{leg}" suffix, matching the
 * convention football-data.org's sync already used for CLI-2026-FINAL.
 */
const PHASE_CONFIGS = {
    QUARTERFINALS: {
        sourceLabel: "Quartas de Final",
        idSuffix: "QF",
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
        idSuffix: "SF",
        displayGroup: "Semifinal",
        singleMatch: false,
        // TODO: fill in once the quarterfinals decide who advances, e.g.:
        // [["Winner teamDictionary key", "Winner teamDictionary key"], [...]]
        ties: []
    },
    FINAL: {
        // TODO: confirm the exact phase label the source uses once it publishes
        // this round (likely "Final") before relying on this entry.
        sourceLabel: "Final",
        idSuffix: "FINAL",
        displayGroup: "Final",
        singleMatch: true,
        // TODO: fill in the single tie once the semifinals decide who advances.
        ties: []
    }
};

// Thin, phase-fixed wrappers so callers (index.js) don't need to change for
// the quarterfinals, which are already configured and deployed today.
async function syncLibertadoresQuarterfinals(db, admin, axios) {
    await syncPhase(db, admin, axios, COMPETITION, PHASE_CONFIGS, "QUARTERFINALS");
}

async function syncLibertadoresQuarterfinalsIfCheckpointDue(db, admin, axios) {
    await syncPhaseIfCheckpointDue(db, admin, axios, COMPETITION, PHASE_CONFIGS, "QUARTERFINALS");
}

module.exports = {
    syncLibertadoresQuarterfinals,
    syncLibertadoresQuarterfinalsIfCheckpointDue,
    COMPETITION,
    PHASE_CONFIGS
};
