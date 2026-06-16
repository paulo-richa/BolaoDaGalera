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

const API_KEY = "***REMOVED_SECRET***";
const isMatchHappening = false;

exports.syncScores = onSchedule({
    schedule: "* * * * *", // Roda a cada minuto para o gatekeeper verificar o estado
    timeZone: "America/Sao_Paulo",
    region: "southamerica-east1",
    memory: "256MiB"
}, async (event) => {
    try {
        const now = Date.now();
        const configRef = db.collection('config').doc('sync_status');
        const configDoc = await configRef.get();
        const config = configDoc.exists ? configDoc.data() : { nextAllowedRun: 0 };

        // 1. GATEKEEPER: Se ainda não deu o tempo da próxima execução agendada, encerra
        if (now < config.nextAllowedRun) {
            return;
        }

        logger.info("Verificando necessidade de sincronização...");

        // 2. Football-Data API
        const response = await axios.get("https://api.football-data.org/v4/competitions/WC/matches", {
            headers: { 'X-Auth-Token': API_KEY }
        });
        const matches = response.data.matches || [];

        // 3. Lógica de Intervalo Dinâmico:
        // Turbo (1min): 10 min antes do jogo, durante o jogo, e 10 min após acabar.
        // Normal (15min): Fora desses horários.
        const TEN_MIN_MS = 10 * 60 * 1000;
        const TYPICAL_MATCH_MS = 115 * 60 * 1000; // ~115 mins (90 + acréscimos + intervalo)

        const hasMatchOngoing = matches.some(m => {
            if (m.status === 'IN_PLAY' || m.status === 'LIVE' || m.status === 'PAUSED') return true;

            const matchStart = new Date(m.utcDate).getTime();
            // Janela: 10 min antes até 10 min após o tempo estimado de fim
            const isNearMatch = now >= (matchStart - TEN_MIN_MS) &&
                               now <= (matchStart + TYPICAL_MATCH_MS + TEN_MIN_MS);

            return isNearMatch && m.status !== 'FINISHED';
        });

        // 4. Define o intervalo para a PRÓXIMA execução e salva no Firestore
        const nextInterval = hasMatchOngoing ? (1 * 60 * 1000) : (15 * 60 * 1000);
        await configRef.set({
            nextAllowedRun: now + nextInterval,
            lastRun: now,
            status: hasMatchOngoing ? "TURBO (1min)" : "NORMAL (15min)"
        });

        logger.info(`Sincronização processada. Próxima em: ${nextInterval/60000}min (Modo: ${hasMatchOngoing ? "TURBO" : "NORMAL"})`);

        // 5. Atualiza os placares no Firestore
        let updatedCount = 0;
        for (const m of matches) {
            const wasUpdated = await updateMatchInFirestore(m.homeTeam?.tla, m.awayTeam?.tla, {
                homeScore: m.score?.fullTime?.home,
                awayScore: m.score?.fullTime?.away,
                status: m.status,
                utcDate: m.utcDate
            });
            if (wasUpdated) updatedCount++;
        }
        logger.info(`Fim do ciclo. ${updatedCount} jogos atualizados.`);
    } catch (error) {
        logger.error("Erro na sincronização:", error.message);
        if (error.response) {
            logger.error("Detalhes do erro API:", error.response.data);
        }
    }
});

async function updateMatchInFirestore(hCode, aCode, data) {
    if (!hCode || !aCode) return false;

    const matchesRef = db.collection('matches');
    let snapshot = await matchesRef.where('homeTeamCode', '==', hCode).where('awayTeamCode', '==', aCode).get();
    let isInverted = false;

    if (snapshot.empty) {
        snapshot = await matchesRef.where('homeTeamCode', '==', aCode).where('awayTeamCode', '==', hCode).get();
        isInverted = true;
    }

    if (snapshot.empty) {
        // Log opcional para jogos da API que não existem no seu banco
        // logger.debug(`Jogo ignorado (não existe no Firestore): ${hCode} x ${aCode}`);
        return false;
    }

    const matchDoc = snapshot.docs[0];
    const matchData = matchDoc.data();

    if (matchData.isManual) return false;

    let apiHomeScore = isInverted ? data.awayScore : data.homeScore;
    let apiAwayScore = isInverted ? data.homeScore : data.awayScore;

    // Se o jogo ainda não aconteceu e o placar é 0x0, não faz nada
    if (data.status === 'TIMED' && apiHomeScore === 0 && apiAwayScore === 0) return false;

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
                logger.info(`ATUALIZADO: ${hCode} x ${aCode} -> ${apiHomeScore}x${apiAwayScore}`);
                return true;
            }
        }
    }
    return false;
}
