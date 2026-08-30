const admin = require("firebase-admin");
const axios = require("axios");
const { logger } = require("firebase-functions");

// Inicialização
admin.initializeApp();
const db = admin.firestore();

// Módulos internos
const { syncBrasileirao } = require("./brasileirao");
const { syncLibertadores } = require("./libertadores");
const { updateMatchRankings, fullRecalculateRanking } = require("./rankings");
const { advanceTeams } = require("./knockout");
const { onDocumentWritten } = require("firebase-functions/v2/firestore");
const { onRequest } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");

// Chave da football-data.org, armazenada no Secret Manager (nunca no código).
// Definir com: firebase functions:secrets:set FOOTBALL_DATA_API_KEY
const footballDataApiKey = defineSecret("FOOTBALL_DATA_API_KEY");

/**
 * Gatilho: Quando um jogo é atualizado no Firestore.
 * Se o jogo estiver finalizado, calcula os rankings.
 */
exports.onMatchUpdate = onDocumentWritten("championships/{championshipId}/matches/{matchId}", async (event) => {
    const afterData = event.data.after.data();
    if (!afterData) return;

    if (afterData.status === "FINISHED" && afterData.homeScore !== null && afterData.awayScore !== null) {
        await updateMatchRankings(db, admin, event.params.championshipId, event.params.matchId, {
            homeScore: afterData.homeScore,
            awayScore: afterData.awayScore
        });

        // Se for jogo de mata-mata, avança os times na chave
        if (event.params.championshipId === "LIBERTADORES") {
            await advanceTeams(db, admin, event.params.championshipId);
        }
    }
});

/**
 * Endpoint para forçar a recalculação de todos os rankings (Útil após migrações).
 */
exports.recalculateAllRankings = onRequest({ timeoutSeconds: 540, memory: "512MiB" }, async (req, res) => {
    const boloes = await db.collection("boloes").get();
    for (const bDoc of boloes.docs) {
        await fullRecalculateRanking(db, admin, bDoc.id);
    }
    res.send("Rankings recalculados com sucesso.");
});

/**
 * Sincronização Manual da Libertadores via HTTP (Zero custos).
 * Chamada pelo GitHub Actions (agendamento externo, gratuito).
 */
exports.syncLibertadoresHTTP = onRequest({ secrets: [footballDataApiKey] }, async (req, res) => {
    try {
        logger.info("📡 Sincronizando Libertadores (HTTP)");
        await syncLibertadores(db, admin, axios);
        await advanceTeams(db, admin, "LIBERTADORES");
        logger.info("✅ Sincronização concluída");
        return res.json({ status: "success", message: "Libertadores sincronizada" });
    } catch (error) {
        logger.error("❌ Erro:", error.message);
        return res.status(500).json({ status: "error", message: error.message });
    }
});

/**
 * Sincronização Manual do Brasileirão via HTTP (Zero custos).
 * Chamada pelo GitHub Actions (agendamento externo, gratuito).
 */
exports.syncBrasileiraoHTTP = onRequest({ secrets: [footballDataApiKey] }, async (req, res) => {
    try {
        logger.info("📡 Sincronizando Brasileirão (HTTP)");
        await syncBrasileirao(db, admin, axios);
        logger.info("✅ Sincronização concluída");
        return res.json({ status: "success", message: "Brasileirão sincronizado" });
    } catch (error) {
        logger.error("❌ Erro:", error.message);
        return res.status(500).json({ status: "error", message: error.message });
    }
});

/**
 * Endpoint de decisão para o GitHub Actions.
 *
 * Retorna shouldSyncFrequently=true quando:
 * - Existe algum jogo com status LIVE, OU
 * - Existe algum jogo cujo horário programado (matchDateMillis) está
 *   dentro da janela de 30 minutos antes até 30 minutos depois do início.
 *
 * O workflow do GitHub Actions roda a cada 5 minutos e chama este endpoint;
 * se vier true, ele faz um loop interno chamando os sync HTTP a cada 60s
 * até o próximo disparo do cron, simulando sincronização "a cada 1 minuto"
 * sem custo nenhum (GitHub Actions é gratuito para esse volume de uso).
 */
exports.checkSyncFrequency = onRequest(async (req, res) => {
    try {
        const now = Date.now();
        const THIRTY_MIN = 30 * 60 * 1000;

        const liveSnap = await db.collectionGroup("matches")
            .where("status", "==", "LIVE")
            .limit(1)
            .get();

        if (!liveSnap.empty) {
            return res.json({ shouldSyncFrequently: true, reason: "LIVE" });
        }

        const upcomingSnap = await db.collectionGroup("matches")
            .where("matchDateMillis", ">=", now - THIRTY_MIN)
            .where("matchDateMillis", "<=", now + THIRTY_MIN)
            .limit(1)
            .get();

        if (!upcomingSnap.empty) {
            return res.json({ shouldSyncFrequently: true, reason: "NEAR_KICKOFF" });
        }

        return res.json({ shouldSyncFrequently: false });
    } catch (error) {
        logger.error("❌ Erro ao verificar frequência de sync:", error.message);
        return res.status(500).json({ shouldSyncFrequently: false, error: error.message });
    }
});
