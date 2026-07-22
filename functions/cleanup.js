const { logger } = require("firebase-functions");

async function cleanupDeletedBoloes(db) {
    try {
        const sevenDaysAgo = Date.now() - (7 * 24 * 60 * 60 * 1000);
        const snapshot = await db.collection("boloes").where("deletedAtMillis", "<=", sevenDaysAgo).get();

        if (snapshot.empty) return;

        logger.info(`Limpando ${snapshot.size} bolões deletados...`);

        for (const doc of snapshot.docs) {
            const bolaoId = doc.id;

            // 1. Deletar Subcollection Predictions
            const predsSnap = await doc.ref.collection("predictions").get();
            const batch = db.batch();
            predsSnap.forEach(pDoc => batch.delete(pDoc.ref));
            await batch.commit();

            // 2. Deletar Convites vinculados
            const invitesSnap = await db.collection("invitations").where("bolaoId", "==", bolaoId).get();
            const inviteBatch = db.batch();
            invitesSnap.forEach(iDoc => inviteBatch.delete(iDoc.ref));
            await inviteBatch.commit();

            // 3. Deletar o documento do Bolão
            await doc.ref.delete();
            logger.info(`Bolão ${bolaoId} removido permanentemente.`);
        }
    } catch (e) {
        logger.error("Erro na limpeza de bolões:", e.message);
    }
}

async function cleanupExpiredInvitations(db) {
    try {
        const sevenDaysAgo = Date.now() - (7 * 24 * 60 * 60 * 1000);
        const snapshot = await db.collection("invitations")
            .where("status", "==", "PENDING")
            .where("createdAtMillis", "<=", sevenDaysAgo)
            .get();

        if (snapshot.empty) return;

        logger.info(`Limpando ${snapshot.size} convites expirados...`);

        const batch = db.batch();
        snapshot.forEach(doc => batch.delete(doc.ref));
        await batch.commit();

        logger.info(`Convites expirados removidos.`);
    } catch (e) {
        logger.error("Erro na limpeza de convites:", e.message);
    }
}

module.exports = { cleanupDeletedBoloes, cleanupExpiredInvitations };
