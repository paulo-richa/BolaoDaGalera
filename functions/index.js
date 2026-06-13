const { onSchedule } = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");
const axios = require("axios");
const { logger } = require("firebase-functions");

admin.initializeApp();
const db = admin.firestore();

// Mesmos mapeamentos do script original
const NAME_TO_CODE = {
    "Mexico": "MEX", "México": "MEX", "South Africa": "RSA", "África do Sul": "RSA",
    "Korea Republic": "KOR", "South Korea": "KOR", "Coreia do Sul": "KOR",
    "Czech Republic": "CZE", "Czechia": "CZE", "República Tcheca": "CZE",
    "Canada": "CAN", "Canadá": "CAN", "Switzerland": "SUI", "Suíça": "SUI",
    "Qatar": "QAT", "Catar": "QAT", "Bosnia-Herzegovina": "BIH", "Bósnia": "BIH",
    "Brazil": "BRA", "Brasil": "BRA", "Morocco": "MAR", "Marrocos": "MAR",
    "Scotland": "SCO", "Escócia": "SCO", "Haiti": "HAI",
    "United States": "USA", "USA": "USA", "EUA": "USA", "Paraguay": "PAR", "Paraguai": "PAR"
    // ... adicione outros se necessário
};

const API_KEY = "c366f1ec224c43a28100c97ea5aab282";

exports.syncScores = onSchedule({
    schedule: "every 10 minutes",
    region: "southamerica-east1", // São Paulo para menor latência
    memory: "256MiB"
}, async (event) => {
    logger.info("Iniciando sincronização agendada...");

    try {
        // 1. Football-Data
        const response = await axios.get("https://api.football-data.org/v4/competitions/WC/matches", {
            headers: { 'X-Auth-Token': API_KEY }
        });
        const matches = response.data.matches || [];

        for (const m of matches) {
            await updateMatchInFirestore(m.homeTeam?.tla, m.awayTeam?.tla, {
                homeScore: m.score?.fullTime?.home,
                awayScore: m.score?.fullTime?.away,
                status: m.status,
                utcDate: m.utcDate
            });
        }
    } catch (error) {
        logger.error("Erro na sincronização:", error.message);
    }
});

async function updateMatchInFirestore(hCode, aCode, data) {
    if (!hCode || !aCode) return;

    const matchesRef = db.collection('matches');
    let snapshot = await matchesRef.where('homeTeamCode', '==', hCode).where('awayTeamCode', '==', aCode).get();
    let isInverted = false;

    if (snapshot.empty) {
        snapshot = await matchesRef.where('homeTeamCode', '==', aCode).where('awayTeamCode', '==', hCode).get();
        isInverted = true;
    }

    if (snapshot.empty) return;

    const matchDoc = snapshot.docs[0];
    const matchData = matchDoc.data();

    if (matchData.isManual) return;

    let apiHomeScore = isInverted ? data.awayScore : data.homeScore;
    let apiAwayScore = isInverted ? data.homeScore : data.awayScore;

    if (data.status === 'TIMED' && apiHomeScore === 0 && apiAwayScore === 0) return;

    if (apiHomeScore !== undefined && apiAwayScore !== undefined && apiHomeScore !== null && apiAwayScore !== null) {
        const currentTotal = (matchData.homeScore || 0) + (matchData.awayScore || 0);
        const newTotal = apiHomeScore + apiAwayScore;
        const isProgression = matchData.homeScore === null || matchData.homeScore === undefined || newTotal >= currentTotal;

        if (isProgression) {
            if (apiHomeScore !== matchData.homeScore || apiAwayScore !== matchData.awayScore) {
                await matchDoc.ref.update({
                    homeScore: apiHomeScore,
                    awayScore: apiAwayScore
                });
                logger.info(`Atualizado: ${hCode} x ${aCode} -> ${apiHomeScore}x${apiAwayScore}`);
            }
        }
    }
}
