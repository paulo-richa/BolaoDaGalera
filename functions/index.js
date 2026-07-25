const { onSchedule } = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");
const axios = require("axios");

// Inicialização
admin.initializeApp();
const db = admin.firestore();

// Módulos internos
const { syncBrasileirao } = require("./brasileirao");
const { syncLibertadores } = require("./libertadores");
const { cleanupDeletedBoloes, cleanupExpiredInvitations } = require("./cleanup");
const { updateMatchRankings, fullRecalculateRanking } = require("./rankings");
const { onDocumentWritten } = require("firebase-functions/v2/firestore");
const { onRequest } = require("firebase-functions/v2/https");

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
 * Sincronização Geral de Jogos e Resultados.
 * Roda a cada 1 minuto.
 */
exports.syncScores = onSchedule({
    schedule: "every 1 minutes",
    timeZone: "America/Sao_Paulo",
    region: "southamerica-east1",
    memory: "256MiB",
    timeoutSeconds: 120
}, async (event) => {
    // Roda sincronizações em paralelo para eficiência
    await Promise.all([
        syncBrasileirao(db, admin, axios),
        syncLibertadores(db, admin, axios)
    ]);
});

/**
 * Limpeza Diária de Bolões Deletados.
 * Roda às 03:00.
 */
exports.cleanupDeletedBoloes = onSchedule({
    schedule: "0 3 * * *",
    timeZone: "America/Sao_Paulo",
    region: "southamerica-east1",
    memory: "256MiB"
}, async (event) => {
    await cleanupDeletedBoloes(db);
});

/**
 * Limpeza Diária de Convites Expirados.
 * Roda às 03:30.
 */
exports.cleanupExpiredInvitations = onSchedule({
    schedule: "30 3 * * *",
    timeZone: "America/Sao_Paulo",
    region: "southamerica-east1",
    memory: "256MiB"
}, async (event) => {
    await cleanupExpiredInvitations(db);
});
