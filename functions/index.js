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
const { cleanupDeletedBoloes, cleanupExpiredInvitations } = require("./cleanup");
const { makeNotificationTriggers } = require("./notificationTriggers");
const { onDocumentWritten } = require("firebase-functions/v2/firestore");
const { onRequest } = require("firebase-functions/v2/https");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const { defineSecret } = require("firebase-functions/params");

// Chave da football-data.org, armazenada no Secret Manager (nunca no código).
// Definir com: firebase functions:secrets:set FOOTBALL_DATA_API_KEY
const footballDataApiKey = defineSecret("FOOTBALL_DATA_API_KEY");

// Token para proteger endpoints administrativos (recálculo de ranking,
// sync manual). Definir com: firebase functions:secrets:set ADMIN_TOKEN
// Chamar passando o header "x-admin-token: <valor>".
const adminToken = defineSecret("ADMIN_TOKEN");

function requireAdminToken(req, res) {
    if (req.get("x-admin-token") !== process.env.ADMIN_TOKEN) {
        res.status(403).json({ status: "error", message: "Token administrativo inválido ou ausente." });
        return false;
    }
    return true;
}

/**
 * Gatilho: Quando um jogo é atualizado no Firestore.
 * Se o jogo estiver finalizado, calcula os rankings.
 */
exports.onMatchUpdate = onDocumentWritten("championships/{championshipId}/matches/{matchId}", async (event) => {
    const afterData = event.data.after.data();
    if (!afterData) return;

    const beforeData = event.data.before.data();
    // O sync reescreve o documento (lastSync novo) mesmo sem o placar mudar de
    // verdade - sem essa checagem, recalculamos o ranking do zero a cada sync,
    // mesmo quando nada relevante mudou.
    const scoreChanged = !beforeData ||
        beforeData.status !== afterData.status ||
        beforeData.homeScore !== afterData.homeScore ||
        beforeData.awayScore !== afterData.awayScore;

    if (scoreChanged && afterData.status === "FINISHED" && afterData.homeScore !== null && afterData.awayScore !== null) {
        await updateMatchRankings(db, admin, event.params.championshipId, event.params.matchId, {
            homeScore: afterData.homeScore,
            awayScore: afterData.awayScore
        });
    }
});

/**
 * Notificações push: convite recebido, pedidos pendentes, resumos, etc.
 * (funções individuais registradas conforme cada tipo é implementado).
 */
const notificationTriggers = makeNotificationTriggers(db, admin);
exports.onInvitationCreated = notificationTriggers.onInvitationCreated;

/**
 * Endpoint para forçar a recalculação de todos os rankings (Útil após migrações).
 */
exports.recalculateAllRankings = onRequest({ timeoutSeconds: 540, memory: "512MiB", secrets: [adminToken] }, async (req, res) => {
    if (!requireAdminToken(req, res)) return;
    const boloes = await db.collection("boloes").get();
    for (const bDoc of boloes.docs) {
        await fullRecalculateRanking(db, admin, bDoc.id);
    }
    res.send("Rankings recalculados com sucesso.");
});

/**
 * Sincronização Manual da Libertadores via HTTP (Zero custos).
 * Chamada pelo GitHub Actions (agendamento externo, gratuito).
 *
 * Fonte única de verdade: football-data.org. Não calculamos/adivinhamos
 * quem avança de fase aqui — só gravamos exatamente o que a API manda.
 * Quando a API publicar o próximo confronto, o sync normal já traz os
 * times e a data certos.
 */
exports.syncLibertadoresHTTP = onRequest({ secrets: [footballDataApiKey, adminToken] }, async (req, res) => {
    if (!requireAdminToken(req, res)) return;
    try {
        logger.info("📡 Sincronizando Libertadores (HTTP)");
        await syncLibertadores(db, admin, axios);
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
exports.syncBrasileiraoHTTP = onRequest({ secrets: [footballDataApiKey, adminToken] }, async (req, res) => {
    if (!requireAdminToken(req, res)) return;
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
 * Verifica se existe jogo LIVE ou próximo do horário (30 min antes até 30
 * min depois do início). Usado tanto pelo endpoint HTTP de debug quanto
 * pela function agendada que roda a cada minuto.
 */
async function checkShouldSyncFrequently() {
    const now = Date.now();
    const THIRTY_MIN = 30 * 60 * 1000;

    // A API nunca manda literalmente "LIVE" — os status reais de jogo em
    // andamento são estes (mesma lista usada no app para exibir "AO VIVO").
    const liveSnap = await db.collectionGroup("matches")
        .where("status", "in", ["IN_PLAY", "PAUSED", "EXTRA_TIME", "PENALTIES", "LIVE"])
        .limit(1)
        .get();

    if (!liveSnap.empty) {
        return { shouldSyncFrequently: true, reason: "LIVE" };
    }

    const upcomingSnap = await db.collectionGroup("matches")
        .where("matchDateMillis", ">=", now - THIRTY_MIN)
        .where("matchDateMillis", "<=", now + THIRTY_MIN)
        .limit(1)
        .get();

    if (!upcomingSnap.empty) {
        return { shouldSyncFrequently: true, reason: "NEAR_KICKOFF" };
    }

    return { shouldSyncFrequently: false };
}

/**
 * Endpoint de debug/manual para inspecionar o resultado de
 * checkShouldSyncFrequently() sem precisar esperar o agendamento.
 */
exports.checkSyncFrequency = onRequest(async (req, res) => {
    try {
        return res.json(await checkShouldSyncFrequently());
    } catch (error) {
        logger.error("❌ Erro ao verificar frequência de sync:", error.message);
        return res.status(500).json({ shouldSyncFrequently: false, error: error.message });
    }
});

/**
 * Agendamento fixo (Cloud Scheduler nativo do Firebase, sem custo dentro
 * da faixa grátis): 4x/dia (02h, 09h, 14h, 18h, horário de Brasília),
 * garante sincronização mesmo sem jogo ao vivo.
 */
exports.scheduledFixedSync = onSchedule(
    { schedule: "0 2,9,14,18 * * *", timeZone: "America/Sao_Paulo", secrets: [footballDataApiKey], timeoutSeconds: 300, memory: "256MiB" },
    async () => {
        logger.info("📡 Sync agendado fixo (4x/dia)");
        await syncBrasileirao(db, admin, axios);
        await syncLibertadores(db, admin, axios);
    }
);

/**
 * Agendamento "a cada 3 minutos" (Cloud Scheduler nativo do Firebase): só
 * chama a sincronização de verdade quando há jogo LIVE ou próximo do
 * início, mantendo o custo desprezível fora das janelas de jogo.
 */
exports.scheduledLiveCheck = onSchedule(
    { schedule: "*/3 * * * *", timeZone: "America/Sao_Paulo", secrets: [footballDataApiKey], timeoutSeconds: 170, memory: "256MiB" },
    async () => {
        const { shouldSyncFrequently, reason } = await checkShouldSyncFrequently();
        if (!shouldSyncFrequently) return;

        logger.info(`📡 Sync agendado (jogo ao vivo/próximo - ${reason})`);
        await syncBrasileirao(db, admin, axios);
        await syncLibertadores(db, admin, axios);
    }
);

/**
 * Limpeza diária (Cloud Scheduler nativo do Firebase): remove bolões
 * marcados como deletados há mais de 7 dias (e seus palpites/convites) e
 * convites pendentes expirados há mais de 7 dias.
 */
exports.scheduledCleanup = onSchedule(
    { schedule: "0 4 * * *", timeZone: "America/Sao_Paulo" },
    async () => {
        await cleanupDeletedBoloes(db);
        await cleanupExpiredInvitations(db);
    }
);
