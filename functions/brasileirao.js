const { API_KEY } = require("./config");
const { logger } = require("firebase-functions");

async function syncBrasileirao(db, admin, axios) {
    logger.info("Iniciando sincronização do Brasileirão...");
    const matchesRef = db.collection("championships").doc("BRASILEIRAO").collection("matches");

    try {
        const resBSA = await axios.get("https://api.football-data.org/v4/competitions/BSA/matches", {
            headers: { 'X-Auth-Token': API_KEY },
            timeout: 15000
        }).catch(() => null);

        if (resBSA && resBSA.data && resBSA.data.matches) {
            const batch = db.batch();
            for (const m of resBSA.data.matches) {
                const matchId = `BSA-2026-R${m.matchday}-${m.id}`;
                const s = m.score;
                const hScore = s?.fullTime?.home ?? s?.regularTime?.home;
                const aScore = s?.fullTime?.away ?? s?.regularTime?.away;

                batch.set(matchesRef.doc(matchId), {
                    status: m.status,
                    homeScore: hScore !== undefined ? hScore : null,
                    awayScore: aScore !== undefined ? aScore : null,
                    championshipId: "BRASILEIRAO",
                    lastSync: admin.firestore.FieldValue.serverTimestamp()
                }, { merge: true });
            }
            await batch.commit();
            logger.info(`Brasileirão sincronizado: ${resBSA.data.matches.length} jogos.`);
        }
    } catch (e) {
        logger.error("Erro na sincronização do Brasileirão:", e.message);
    }
}

module.exports = { syncBrasileirao };
