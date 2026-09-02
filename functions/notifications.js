const { logger } = require("firebase-functions");

/**
 * Writes the persistent notification record (in-app bell/history).
 * Always created, even if the user has no registered push token -
 * guarantees the app shows the notification the next time it opens.
 */
async function createNotification(db, { userId, title, message, type, bolaoId = null, matchId = null }) {
    await db.collection("notifications").add({
        userId,
        title,
        message,
        type,
        bolaoId,
        matchId,
        createdAtMillis: Date.now(),
        read: false
    });
}

/**
 * Sends a data-only push (never "notification") to the given tokens -
 * BolaoFirebaseMessagingService on Android always builds the notification
 * manually from the "data" payload, even with the app closed.
 * Returns the tokens FCM rejected as invalid/expired, letting the caller
 * decide whether to clean them from the user's profile.
 */
async function sendPush(admin, tokens, { title, body, deepLink, type }) {
    if (!tokens || tokens.length === 0) return [];

    const response = await admin.messaging().sendEachForMulticast({
        tokens,
        data: {
            title,
            body,
            type,
            ...(deepLink ? { deepLink } : {})
        }
    });

    const invalidTokens = [];
    response.responses.forEach((r, idx) => {
        if (!r.success && r.error &&
            (r.error.code === "messaging/registration-token-not-registered" ||
                r.error.code === "messaging/invalid-argument")) {
            invalidTokens.push(tokens[idx]);
        }
    });
    return invalidTokens;
}

/**
 * Writes the persistent notification and, if the user has any registered
 * device, fires the push - removing from the profile any token FCM
 * rejects as invalid/expired.
 */
async function notifyUser(db, admin, userId, { title, message, type, bolaoId = null, matchId = null, deepLink = null }) {
    try {
        await createNotification(db, { userId, title, message, type, bolaoId, matchId });

        const userDoc = await db.collection("users").doc(userId).get();
        if (!userDoc.exists) return;

        const tokens = userDoc.data().fcmTokens || [];
        if (tokens.length === 0) return;

        const invalidTokens = await sendPush(admin, tokens, { title, body: message, deepLink, type });
        if (invalidTokens.length > 0) {
            await userDoc.ref.update({ fcmTokens: admin.firestore.FieldValue.arrayRemove(...invalidTokens) });
        }
    } catch (e) {
        logger.error(`Erro ao notificar usuário ${userId}:`, e.message);
    }
}

module.exports = { createNotification, sendPush, notifyUser };
