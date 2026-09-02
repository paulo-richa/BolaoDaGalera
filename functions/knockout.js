const admin = require("firebase-admin");
const { logger } = require("firebase-functions");

/**
 * Detects home/away swaps between L1 and L2 (normal inversion in knockout ties)
 * and automatically migrates predictions when the API overwrites with real data.
 */
async function migratePredictionsIfMatchChanged(db, matchId, oldMatch, newMatch) {
    try {
        // Check whether there was a significant change in teams
        const teamsChanged =
            oldMatch.homeTeamCode !== newMatch.homeTeamCode ||
            oldMatch.awayTeamCode !== newMatch.awayTeamCode;

        if (!teamsChanged) {
            return; // No change, no migration needed
        }

        logger.info(`🔄 Detectada mudança de mandos em ${matchId}. Iniciando migração de palpites...`);

        // Fetch all predictions for this match (predictions live under
        // boloes/{bolaoId}/predictions, hence the collectionGroup query)
        const snapshot = await db.collectionGroup("predictions").where("matchId", "==", matchId).get();

        if (snapshot.empty) {
            logger.info(`✅ Nenhum palpite encontrado para ${matchId}. Migração concluída.`);
            return;
        }

        // Prepare migration batch
        const batch = db.batch();
        let migratedCount = 0;

        snapshot.forEach(doc => {
            const prediction = doc.data();
            const oldHome = prediction.homeScore || 0;
            const oldAway = prediction.awayScore || 0;

            // Invert the prediction (L2 has home/away swapped)
            const updates = {
                homeScore: oldAway,
                awayScore: oldHome,
                migratedFromAPIUpdate: true,
                migratedAt: admin.firestore.FieldValue.serverTimestamp(),
                migrationReason: `Mandos de campo invertidos em ${matchId}`
            };

            batch.update(doc.ref, updates);
            migratedCount++;

            logger.info(`  📝 Palpite ${doc.id}: ${oldHome}-${oldAway} → ${oldAway}-${oldHome}`);
        });

        await batch.commit();
        logger.info(`✅ ${migratedCount} palpites migraram com sucesso para ${matchId}`);

    } catch (e) {
        logger.error(`❌ Erro ao migrar palpites de ${matchId}:`, e.message);
    }
}

module.exports = { migratePredictionsIfMatchChanged };
