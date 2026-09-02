const admin = require("firebase-admin");
const axios = require("axios");
const { logger } = require("firebase-functions");

// Initialization
admin.initializeApp();
const db = admin.firestore();

// Internal modules
const { syncBrasileirao } = require("./brasileirao");
const { syncLibertadores } = require("./libertadores");
const { updateMatchRankings, fullRecalculateRanking } = require("./rankings");
const { cleanupDeletedBoloes, cleanupExpiredInvitations } = require("./cleanup");
const { makeNotificationTriggers } = require("./notificationTriggers");
const { sendDailyDigest } = require("./dailyDigest");
const { sendMatchReminders } = require("./matchReminder");
const { checkRoundCompletionAndNotify } = require("./roundSummary");
const { onDocumentWritten } = require("firebase-functions/v2/firestore");
const { onRequest } = require("firebase-functions/v2/https");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const { defineSecret } = require("firebase-functions/params");

// football-data.org key, stored in Secret Manager (never in code).
// Set with: firebase functions:secrets:set FOOTBALL_DATA_API_KEY
const footballDataApiKey = defineSecret("FOOTBALL_DATA_API_KEY");

// Token to protect administrative endpoints (ranking recalculation,
// manual sync). Set with: firebase functions:secrets:set ADMIN_TOKEN
// Call passing the header "x-admin-token: <value>".
const adminToken = defineSecret("ADMIN_TOKEN");

function requireAdminToken(req, res) {
    if (req.get("x-admin-token") !== process.env.ADMIN_TOKEN) {
        res.status(403).json({ status: "error", message: "Token administrativo inválido ou ausente." });
        return false;
    }
    return true;
}

/**
 * Trigger: fires when a match is updated in Firestore.
 * If the match is finished, calculates the rankings.
 */
exports.onMatchUpdate = onDocumentWritten(
    { document: "championships/{championshipId}/matches/{matchId}", timeoutSeconds: 300, memory: "256MiB" },
    async (event) => {
        const afterData = event.data.after.data();
        if (!afterData) return;

        const beforeData = event.data.before.data();
        // Sync rewrites the document (new lastSync) even when the score hasn't
        // actually changed - without this check, we'd recalculate the ranking
        // from scratch on every sync, even when nothing relevant changed.
        const scoreChanged = !beforeData ||
            beforeData.status !== afterData.status ||
            beforeData.homeScore !== afterData.homeScore ||
            beforeData.awayScore !== afterData.awayScore;

        if (scoreChanged && afterData.status === "FINISHED" && afterData.homeScore !== null && afterData.awayScore !== null) {
            await updateMatchRankings(db, admin, event.params.championshipId, event.params.matchId, {
                homeScore: afterData.homeScore,
                awayScore: afterData.awayScore
            });
            await checkRoundCompletionAndNotify(db, admin, event.params.championshipId, afterData);
        }
    }
);

/**
 * Push notifications: invitation received, pending requests, digests, etc.
 * (individual functions registered as each type is implemented).
 */
const notificationTriggers = makeNotificationTriggers(db, admin);
exports.onInvitationCreated = notificationTriggers.onInvitationCreated;
exports.onBolaoUpdated = notificationTriggers.onBolaoUpdated;

/**
 * Endpoint to force recalculation of all rankings (useful after migrations).
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
 * Manual Libertadores sync via HTTP (zero cost).
 * Called by GitHub Actions (external, free scheduling).
 *
 * Single source of truth: football-data.org. We do not calculate/guess
 * who advances to the next phase here — we only write exactly what the
 * API returns. Once the API publishes the next matchup, the regular sync
 * already brings the correct teams and date.
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
 * Manual Brasileirao sync via HTTP (zero cost).
 * Called by GitHub Actions (external, free scheduling).
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
 * Checks whether a match is LIVE or near kickoff (30 min before to 30
 * min after start). Used both by the debug HTTP endpoint and by the
 * scheduled function that runs every minute.
 */
async function checkShouldSyncFrequently() {
    const now = Date.now();
    const THIRTY_MIN = 30 * 60 * 1000;

    // The API never literally sends "LIVE" — these are the actual statuses
    // for a match in progress (same list the app uses to display "LIVE").
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
 * Debug/manual endpoint to inspect the result of
 * checkShouldSyncFrequently() without waiting for the scheduled run.
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
 * Fixed schedule (Firebase's native Cloud Scheduler, free within the free
 * tier): 4x/day (02h, 09h, 14h, 18h, Brasilia time), guarantees sync even
 * without a live match.
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
 * "Every 3 minutes" schedule (Firebase's native Cloud Scheduler): only
 * triggers the actual sync when there's a LIVE match or one about to
 * start, keeping the cost negligible outside match windows.
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
 * Daily cleanup (Firebase's native Cloud Scheduler): removes boloes
 * marked as deleted more than 7 days ago (along with their
 * predictions/invitations) and pending invitations expired more than 7
 * days ago.
 */
exports.scheduledCleanup = onSchedule(
    { schedule: "0 4 * * *", timeZone: "America/Sao_Paulo" },
    async () => {
        await cleanupDeletedBoloes(db);
        await cleanupExpiredInvitations(db);
    }
);

/**
 * Daily digest (Firebase's native Cloud Scheduler): at 09h Brasilia time,
 * notifies each user how many of today's matches they still haven't
 * predicted, summed across every bolao they participate in.
 */
exports.scheduledDailyDigest = onSchedule(
    { schedule: "0 9 * * *", timeZone: "America/Sao_Paulo", timeoutSeconds: 300, memory: "256MiB" },
    async () => {
        await sendDailyDigest(db, admin);
    }
);

/**
 * Per-match reminder (Firebase's native Cloud Scheduler): every 15 min,
 * notifies users who haven't predicted a match starting in under 1h.
 * matchReminders/{bolaoId}_{matchId}_{userId} guarantees the same
 * reminder is never sent twice, even when the same window is covered
 * across successive runs.
 */
exports.scheduledMatchReminder = onSchedule(
    { schedule: "*/15 * * * *", timeZone: "America/Sao_Paulo", timeoutSeconds: 300, memory: "256MiB" },
    async () => {
        await sendMatchReminders(db, admin);
    }
);
