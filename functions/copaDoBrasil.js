const { BR_TEAMS } = require("./teams_br");
const { syncPhase, syncPhaseIfCheckpointDue } = require("./knockoutFallbackSync");

/**
 * Copa do Brasil isn't covered by football-data.org's free plan at all
 * (confirmed 403 on /v4/competitions/CDB/matches), so this competition has
 * no football-data.org sync module of its own - it goes straight to the
 * same external-source fallback used for Libertadores' quarterfinals+ (see
 * knockoutFallbackSync.js). Only starting from the semifinals: the earlier
 * rounds/teams aren't covered here by design (see the user's explicit scope
 * decision), so this only ever needs SEMIFINALS and FINAL configured.
 */
const COMPETITION = {
    championshipId: "COPA_DO_BRASIL",
    idPrefix: "CDB-2026-",
    checkpointCollection: "copaDoBrasilCheckpoints",
    sourceUrlEnvVar: "COPA_DO_BRASIL_RESULTS_SOURCE_URL",
    teamDictionary: BR_TEAMS,
    // teams_br.js already has plain-name aliases ("Palmeiras", "Vasco",
    // "Grêmio", "Atlético-MG") matching the external source's exact display
    // names, so no extra aliasing is needed for the semifinalists today.
    nameAliases: {}
};

/** See libertadoresQuarterfinals.js's PHASE_CONFIGS for the documented shape. */
const PHASE_CONFIGS = {
    SEMIFINALS: {
        // TODO: confirm the exact phase label the source uses for this
        // competition before relying on this entry.
        sourceLabel: "Semifinal",
        idSuffix: "SF",
        displayGroup: "Semifinal",
        singleMatch: false,
        // Fixed real-world draw (see news coverage confirming both ties).
        ties: [
            ["Palmeiras", "Vasco"],
            ["Atlético-MG", "Grêmio"]
        ]
    },
    FINAL: {
        // TODO: confirm the exact phase label the source uses for this
        // competition before relying on this entry.
        sourceLabel: "Final",
        idSuffix: "FINAL",
        displayGroup: "Final",
        singleMatch: true,
        // TODO: fill in the single tie once the semifinals decide who advances.
        ties: []
    }
};

// index.js only needs one daily call and one 15-min checkpoint poll for this
// competition - both phases are checked every time, but syncPhase/
// syncPhaseIfCheckpointDue silently no-op for FINAL until its ties are filled in.
async function syncCopaDoBrasil(db, admin, axios) {
    await syncPhase(db, admin, axios, COMPETITION, PHASE_CONFIGS, "SEMIFINALS");
    await syncPhase(db, admin, axios, COMPETITION, PHASE_CONFIGS, "FINAL");
}

async function syncCopaDoBrasilIfCheckpointDue(db, admin, axios) {
    await syncPhaseIfCheckpointDue(db, admin, axios, COMPETITION, PHASE_CONFIGS, "SEMIFINALS");
    await syncPhaseIfCheckpointDue(db, admin, axios, COMPETITION, PHASE_CONFIGS, "FINAL");
}

module.exports = {
    syncCopaDoBrasil,
    syncCopaDoBrasilIfCheckpointDue,
    COMPETITION,
    PHASE_CONFIGS
};
