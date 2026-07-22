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
