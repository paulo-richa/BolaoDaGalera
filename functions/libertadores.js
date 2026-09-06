const { LIB_TEAMS } = require("./teams_lib");
const { mapPhase } = require("./utils");
const { getLibertadoresData } = require("./fallback-api");
const { logger } = require("firebase-functions");

async function syncLibertadores(db, admin, axios) {
    logger.info("Iniciando sincronização da Libertadores...");
    const matchesRef = db.collection("championships").doc("LIBERTADORES").collection("matches");

    try {
        // Fetch data with automatic fallback
        const apiData = await getLibertadoresData(axios);

        if (apiData && apiData.matches) {
            const resCLI = { data: apiData };
            const batch = db.batch();
            const now = Date.now();
            const knockoutPairs = {};

            // Strict Round of 16 mapping (leg 1 and leg 2 to the same key)
            const r16Mapping = {
                564456: 1, 564465: 1, // Estudiantes vs Catolica
                564462: 2, 564470: 2, // Rosario vs Corinthians
                564460: 3, 564468: 3, // Cruzeiro vs Flamengo
                564457: 4, 564464: 4, // Tolima vs Ind. del Valle
                564461: 5, 564469: 5, // Mirassol vs LDU
                564459: 6, 564466: 6, // Palmeiras vs Cerro
                564458: 7, 564467: 7, // Platense vs Coquimbo
                564455: 8, 564463: 8  // Fluminense vs Rivadavia
            };

            for (const m of resCLI.data.matches) {
                const knockoutStages = ["ROUND_OF_16", "PLAY_OFFS", "QUARTER_FINALS", "SEMI_FINALS", "FINAL"];
                if (m.stage !== "GROUP_STAGE" && !knockoutStages.includes(m.stage)) continue;

                const isKO = knockoutStages.includes(m.stage);
                const mappedPhase = mapPhase(m.stage);

                // football-data.org's free plan doesn't cover this competition's
                // current season, which leaves its knockout-phase response
                // permanently incomplete/inconsistent from Quarterfinals onward
                // (see fallback-api.js). libertadoresQuarterfinals.js owns these
                // phases exclusively now - writing them here too would just have
                // the two syncs fight over the same documents on every run.
                // SEMIFINALS/FINAL currently have no ties configured there yet
                // (unknown until the previous phase concludes), so until that's
                // filled in, these phases simply won't appear in Firestore at
                // all - preferred over writing football-data.org's known-wrong data.
                if (mappedPhase === "QUARTERFINALS" || mappedPhase === "SEMIFINALS" || mappedPhase === "FINAL") continue;

                let matchId = `CLI-2026-M${m.id}`;
                let knockoutOrder = 0;

                let hName = m.homeTeam?.name || "A definir";
                let aName = m.awayTeam?.name || "A definir";
                let hTeam = LIB_TEAMS[hName] || { name: hName, flag: "", code: m.homeTeam?.tla || "TBD", crest: null };
                let aTeam = LIB_TEAMS[aName] || { name: aName, flag: "", code: m.awayTeam?.tla || "TBD", crest: null };

                if (mappedPhase === "ROUND_OF_16") {
                    knockoutOrder = r16Mapping[m.id] || 0;
                    const isVolta = m.matchday === 2 || m.id > 564462;

                    // PRODUCTION ID MAP (based on users' real existing predictions)
                    const productionIds = {
                        "564456-L1": "CLI-2026-R16-1-L1", // Estudiantes leg 1
                        "564462-L1": "CLI-2026-R16-2-L1", // Corinthians leg 1
                        "564459-L1": "CLI-2026-R16-6-L1", // Palmeiras leg 1
                        "564470-L2": "CLI-2026-M564470-L2", // Corinthians leg 2
                        "564466-L2": "CLI-2026-M564466-L2", // Palmeiras leg 2
                        "564464-L2": "CLI-2026-M564464-L2", // Ind. del Valle leg 2
                        "564469-L2": "CLI-2026-M564469-L2", // LDU leg 2
                        "564465-L2": "CLI-2026-M564465-L2",
                        "564468-L2": "CLI-2026-M564468-L2",
                        "564467-L2": "CLI-2026-M564467-L2",
                        "564463-L2": "CLI-2026-M564463-L2"
                    };

                    const key = `${m.id}-${isVolta ? "L2" : "L1"}`;
                    matchId = productionIds[key] || `CLI-2026-R16-${knockoutOrder}-${isVolta ? "L2" : "L1"}`;
                }
                // SEMIFINALS/FINAL used to be handled here too, but are now
                // skipped above (see the QUARTERFINALS comment) - removed
                // rather than left as dead code.

                const matchTime = Date.parse(m.utcDate);
                let targetStatus = m.status;

                // SCORE LOGIC: always prioritize regulation time (90 min)
                const s = m.score;
                let hScore = null, aScore = null;
                if (s) {
                    // If regularTime exists, it's our official 90 minutes (regardless of duration)
                    if (s.regularTime && s.regularTime.home !== null) {
                        hScore = s.regularTime.home;
                        aScore = s.regularTime.away;
                    } else {
                        // Fall back to fullTime if it's the only value available
                        hScore = s.fullTime?.home ?? null;
                        aScore = s.fullTime?.away ?? null;
                    }
                }

                if (m.status !== "FINISHED" && (now - matchTime > 4 * 3600000) && hScore !== null) {
                    targetStatus = "FINISHED";
                }

                const currentDoc = await matchesRef.doc(matchId).get();
                if (currentDoc.exists && currentDoc.data().isManual) {
                    continue;
                }

                // SAFETY LOCK: never accept a regression from the API (e.g. a match
                // already FINISHED with a real score reverting to something else with a null score).
                const existingData = currentDoc.exists ? currentDoc.data() : null;
                // Uses m.status (raw API value), not targetStatus, for the same
                // reason as brasileirao.js: the heuristic promotion to FINISHED
                // after 4h stalled should not disguise still-incomplete data.
                const existingIsFinal = existingData && existingData.status === "FINISHED" &&
                    existingData.homeScore !== null && existingData.awayScore !== null;
                if (existingIsFinal && (m.status !== "FINISHED" || hScore === null || aScore === null)) {
                    logger.warn(
                        `⚠️ Ignorando retrocesso da API para ${matchId}: ` +
                        `era FINISHED ${existingData.homeScore}x${existingData.awayScore}, API mandou status=${m.status} ${hScore}x${aScore}`
                    );
                    continue;
                }

                const updates = {
                    status: targetStatus,
                    homeScore: hScore,
                    awayScore: aScore,
                    apiWinner: m.score?.winner || null,
                    championshipId: "LIBERTADORES",
                    phase: mappedPhase,
                    matchOrder: knockoutOrder,
                    group: m.group || (m.matchday ? `Rodada ${m.matchday}` : m.stage),
                    matchDateMillis: matchTime,
                    lastSync: admin.firestore.FieldValue.serverTimestamp(),
                    source: "api"
                };

                // ONLY UPDATE TEAMS IF THE API RETURNS REAL NAMES or if the field in the database is empty/TBD
                const existing = currentDoc.data();
                if (hTeam.code !== "TBD" || !existing || existing.homeTeamCode === "TBD") {
                    updates.homeTeam = hTeam.name;
                    updates.homeTeamCode = hTeam.code;
                    updates.homeTeamFlag = hTeam.flag;
                    updates.homeTeamCrest = hTeam.crest || m.homeTeam?.crest;
                }
                if (aTeam.code !== "TBD" || !existing || existing.awayTeamCode === "TBD") {
                    updates.awayTeam = aTeam.name;
                    updates.awayTeamCode = aTeam.code;
                    updates.awayTeamFlag = aTeam.flag;
                    updates.awayTeamCrest = aTeam.crest || m.awayTeam?.crest;
                }

                batch.set(matchesRef.doc(matchId), updates, { merge: true });
            }
            await batch.commit();

            logger.info(`Libertadores sincronizada.`);
        }
    } catch (e) {
        logger.error("Erro na sincronização da Libertadores:", e.message);
    }
}

module.exports = { syncLibertadores };
