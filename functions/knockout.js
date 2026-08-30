const admin = require("firebase-admin");
const { logger } = require("firebase-functions");

/**
 * Detecta mudança de mandos de campo entre L1 e L2 (inversão normal em mata-mata)
 * E migra palpites automaticamente quando a API sobrescreve com dados reais
 */
async function migratePredictionsIfMatchChanged(db, matchId, oldMatch, newMatch) {
    try {
        // Verificar se houve mudança significativa de times
        const teamsChanged =
            oldMatch.homeTeamCode !== newMatch.homeTeamCode ||
            oldMatch.awayTeamCode !== newMatch.awayTeamCode;

        if (!teamsChanged) {
            return; // Sem mudança, sem necessidade de migração
        }

        logger.info(`🔄 Detectada mudança de mandos em ${matchId}. Iniciando migração de palpites...`);

        // Buscar todos os palpites desta partida
        const predictionsRef = db.collection("predictions");
        const snapshot = await predictionsRef.where("matchId", "==", matchId).get();

        if (snapshot.empty) {
            logger.info(`✅ Nenhum palpite encontrado para ${matchId}. Migração concluída.`);
            return;
        }

        // Preparar batch de migração
        const batch = db.batch();
        let migratedCount = 0;

        snapshot.forEach(doc => {
            const prediction = doc.data();
            const oldHome = prediction.homeScore || 0;
            const oldAway = prediction.awayScore || 0;

            // Inverte o palpite (L2 tem mandos invertidos)
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
