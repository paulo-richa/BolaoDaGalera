const { API_KEY } = require("./config");
const { BR_TEAMS } = require("./teams_br");
const { logger } = require("firebase-functions");

async function syncBrasileirao(db, admin, axios) {
    logger.info("Iniciando sincronização inteligente do Brasileirão...");
    const matchesRef = db.collection("championships").doc("BRASILEIRAO").collection("matches");

    try {
        const compRes = await axios.get("https://api.football-data.org/v4/competitions/BSA", {
            headers: { 'X-Auth-Token': API_KEY }
        }).catch(() => null);

        if (!compRes || !compRes.data || !compRes.data.currentSeason) {
            logger.error("Não foi possível obter os dados da competição BSA.");
            return;
        }

        const currentMatchday = compRes.data.currentSeason.currentMatchday;
        const roundsToSync = [currentMatchday, currentMatchday - 1, currentMatchday - 2].filter(r => r > 0);

        const pendingMatchesSnapshot = await matchesRef
            .where('status', 'in', ['IN_PLAY', 'TIMED', 'LIVE'])
            .limit(10)
            .get();

        const pendingRounds = pendingMatchesSnapshot.docs.map(doc => {
            const match = doc.id.match(/-R(\d+)-/);
            return match ? parseInt(match[1]) : null;
        }).filter(r => r !== null && !roundsToSync.includes(r));

        const allRoundsToSync = [...new Set([...roundsToSync, ...pendingRounds])];
        const now = Date.now();

        for (const rd of allRoundsToSync) {
            logger.info(`Sincronizando Rodada ${rd}...`);
            const resBSA = await axios.get(`https://api.football-data.org/v4/competitions/BSA/matches?matchday=${rd}`, {
                headers: { 'X-Auth-Token': API_KEY },
                timeout: 15000
            }).catch(() => null);

            if (resBSA && resBSA.data && resBSA.data.matches) {
                const batch = db.batch();
                let hasUpdates = false;

                for (const m of resBSA.data.matches) {
                    const matchId = `BSA-2026-R${m.matchday}-${m.id}`;

                    const doc = await matchesRef.doc(matchId).get();
                    const existing = doc.exists ? doc.data() : null;

                    // TRAVA DE SEGURANÇA: Se já foi editado manualmente, o robô não mexe mais
                    if (existing && existing.isManual) continue;

                    if (['POSTPONED', 'CANCELLED', 'SUSPENDED'].includes(m.status)) {
                        if (existing) {
                            batch.delete(matchesRef.doc(matchId));
                            hasUpdates = true;
                        }
                        continue;
                    }

                    const s = m.score;
                    const hScore = s?.regularTime?.home ?? s?.fullTime?.home;
                    const aScore = s?.regularTime?.away ?? s?.fullTime?.away;
                    const newHScore = hScore !== undefined ? hScore : null;
                    const newAScore = aScore !== undefined ? aScore : null;

                    const matchTime = Date.parse(m.utcDate);
                    let targetStatus = m.status;
                    if (m.status !== "FINISHED" && (now - matchTime > 4 * 3600000) && newHScore !== null && newAScore !== null) {
                        targetStatus = "FINISHED";
                    }

                    if (!existing || existing.status !== targetStatus || existing.homeScore !== newHScore || existing.awayScore !== newAScore) {
                        const hTeam = BR_TEAMS[m.homeTeam.name] || { name: m.homeTeam.name, flag: "", code: m.homeTeam.tla || "TBD", crest: null };
                        const aTeam = BR_TEAMS[m.awayTeam.name] || { name: m.awayTeam.name, flag: "", code: m.awayTeam.tla || "TBD", crest: null };

                        batch.set(matchesRef.doc(matchId), {
                            status: targetStatus,
                            homeTeam: hTeam.name, homeTeamCode: hTeam.code, homeTeamFlag: hTeam.flag, homeTeamCrest: hTeam.crest || m.homeTeam.crest,
                            awayTeam: aTeam.name, awayTeamCode: aTeam.code, awayTeamFlag: aTeam.flag, awayTeamCrest: aTeam.crest || m.awayTeam.crest,
                            homeScore: newHScore,
                            awayScore: newAScore,
                            championshipId: "BRASILEIRAO",
                            phase: "GROUP_STAGE",
                            group: `Rodada ${m.matchday}`,
                            matchDateMillis: matchTime,
                            lastSync: admin.firestore.FieldValue.serverTimestamp()
                        }, { merge: true });
                        hasUpdates = true;
                    }
                }

                if (hasUpdates) {
                    await batch.commit();
                    logger.info(`Rodada ${rd} atualizada.`);
                }
            }
        }
    } catch (e) {
        logger.error("Erro na sincronização do Brasileirão:", e.message);
    }
}

module.exports = { syncBrasileirao };
