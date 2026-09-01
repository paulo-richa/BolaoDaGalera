const { logger } = require("firebase-functions");

/**
 * Grava o registro persistente da notificação (histórico do sininho no app).
 * Sempre criado, mesmo se o usuário não tiver token de push registrado -
 * garante que o app mostra a notificação na próxima vez que abrir.
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
 * Envia push data-only (nunca "notification") pros tokens informados -
 * BolaoFirebaseMessagingService no Android sempre monta a notificação
 * manualmente a partir do payload "data", inclusive com app fechado.
 * Retorna os tokens que a FCM rejeitou como inválidos/expirados, pra quem
 * chamar decidir se quer limpar do perfil do usuário.
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
 * Grava a notificação persistente e, se o usuário tiver algum dispositivo
 * registrado, dispara o push - removendo do perfil qualquer token que a FCM
 * rejeitar como inválido/expirado.
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
