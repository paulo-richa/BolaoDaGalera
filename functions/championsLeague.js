const { CL_TEAMS } = require("./teams_cl");
const { mapPhase } = require("./utils");
const { getChampionsLeagueData } = require("./fallback-api");
const { logger } = require("firebase-functions");

/**
 * Unlike libertadores.js, there's no r16Mapping/productionIds here - the
 * Champions League's knockout bracket isn't determined yet (league phase
 * only, see teams_cl.js), so there's no production history to preserve.
 * Once the knockout stage appears in football-data.org's response, it's
 * written the same way as the league phase below - revisit this file if
 * that turns out to need the same "known-incomplete free tier" workaround
 * libertadores.js has for Libertadores' quarterfinals onward.
 */
async function syncChampionsLeague(db, admin, axios) {
    logger.info("Iniciando sincronização da Champions League...");
    const matchesRef = db.collection("championships").doc("CHAMPIONS_LEAGUE").collection("matches");

    try {
        const apiData = await getChampionsLeagueData(axios);

        if (apiData && apiData.matches) {
            const batch = db.batch();
            const now = Date.now();

            for (const m of apiData.matches) {
                const mappedPhase = mapPhase(m.stage);

                const matchId = `CL-2026-M${m.id}`;

                let hName = m.homeTeam?.name || "A definir";
                let aName = m.awayTeam?.name || "A definir";
                let hTeam = CL_TEAMS[hName] || { name: hName, flag: "", code: m.homeTeam?.tla || "TBD", crest: null };
                let aTeam = CL_TEAMS[aName] || { name: aName, flag: "", code: m.awayTeam?.tla || "TBD", crest: null };

                const matchTime = Date.parse(m.utcDate);
                let targetStatus = m.status;

                // SCORE LOGIC: always prioritize regulation time (90 min)
                const s = m.score;
                let hScore = null, aScore = null;
                if (s) {
                    if (s.regularTime && s.regularTime.home !== null) {
                        hScore = s.regularTime.home;
                        aScore = s.regularTime.away;
                    } else {
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

                // SAFETY LOCK: never accept a regression from the API (same
                // reasoning as libertadores.js/brasileirao.js).
                const existingData = currentDoc.exists ? currentDoc.data() : null;
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
                    championshipId: "CHAMPIONS_LEAGUE",
                    phase: mappedPhase,
                    matchOrder: 0,
                    group: m.matchday ? `Rodada ${m.matchday}` : m.stage,
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

            logger.info(`Champions League sincronizada.`);
        }
    } catch (e) {
        logger.error("Erro na sincronização da Champions League:", e.message);
    }
}

module.exports = { syncChampionsLeague };
