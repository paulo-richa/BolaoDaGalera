const { calculatePoints } = require("./scoring");
const { logger } = require("firebase-functions");

/**
 * Atualiza os pontos de cada palpite e o ranking geral do bolão para uma partida específica.
 */
async function updateMatchRankings(db, admin, championshipId, matchId, actualScore) {
    logger.info(`Atualizando rankings para o jogo ${matchId} do campeonato ${championshipId}`);

    const boloesSnapshot = await db.collection("boloes")
        .where("championshipId", "==", championshipId)
        .get();

    if (boloesSnapshot.empty) return;

    for (const bolaoDoc of boloesSnapshot.docs) {
        const bolaoId = bolaoDoc.id;
        const bolaoData = bolaoDoc.data();

        // Pega as regras de pontuação do bolão (com fallback para 3 e 1)
        const pExact = bolaoData.pointsExactScore || 3;
        const pResult = bolaoData.pointsWinnerOrDraw || 1;

        const predictionsRef = db.collection("boloes").doc(bolaoId).collection("predictions");
        const predictionsSnapshot = await predictionsRef.where("matchId", "==", matchId).get();

        if (predictionsSnapshot.empty) continue;

        const batch = db.batch();
        const affectedUsers = new Set();

        predictionsSnapshot.forEach(doc => {
            const prediction = doc.data();
            const points = calculatePoints(prediction, actualScore, pExact, pResult);

            batch.update(doc.ref, {
                points: points,
                calculatedAt: admin.firestore.FieldValue.serverTimestamp()
            });

            affectedUsers.add(prediction.userId);
        });

        await batch.commit();
        await refreshUserRankings(db, admin, bolaoId, Array.from(affectedUsers));
    }
}

/**
 * Recalcula o total de pontos de usuários específicos em um bolão.
 */
async function refreshUserRankings(db, admin, bolaoId, userIds) {
    const bolaoDoc = await db.collection("boloes").doc(bolaoId).get();
    if (!bolaoDoc.exists) return;

    const bolaoData = bolaoDoc.data();
    const pExact = bolaoData.pointsExactScore || 3;
    const pResult = bolaoData.pointsWinnerOrDraw || 1;

    const rankingRef = db.collection("boloes").doc(bolaoId).collection("rankings");

    for (const userId of userIds) {
        const userPredictionsSnapshot = await db.collection("boloes").doc(bolaoId).collection("predictions")
            .where("userId", "==", userId)
            .get();

        let totalPoints = 0;
        let exactScores = 0;
        let correctResults = 0;
        let matchesPlayed = 0;

        userPredictionsSnapshot.forEach(doc => {
            const p = doc.data();
            if (p.points !== undefined && p.points !== null) {
                totalPoints += p.points;
                if (p.points > 0) matchesPlayed++;
                if (p.points === pExact) exactScores++;
                if (p.points === pResult) correctResults++;
            }
        });

        const userDoc = await db.collection("users").doc(userId).get();
        const userData = userDoc.exists ? userDoc.data() : {};

        await rankingRef.doc(userId).set({
            userId: userId,
            userName: userData.name || "Usuário",
            userNickname: userData.nickname || userData.username || "",
            totalPoints: totalPoints,
            totalExactScores: exactScores,
            totalCorrectResults: correctResults,
            matchesPlayed: matchesPlayed,
            lastUpdate: admin.firestore.FieldValue.serverTimestamp()
        }, { merge: true });
    }
}

/**
 * Função utilitária para recalcular TODO o ranking de um bolão.
 */
async function fullRecalculateRanking(db, admin, bolaoId) {
    const bolaoDoc = await db.collection("boloes").doc(bolaoId).get();
    if (!bolaoDoc.exists) return;

    const bolaoData = bolaoDoc.data();
    const championshipId = bolaoData.championshipId;
    const pExact = bolaoData.pointsExactScore || 3;
    const pResult = bolaoData.pointsWinnerOrDraw || 1;

    // 1. Pega todos os resultados reais do campeonato
    const matchesSnapshot = await db.collection("championships").doc(championshipId).collection("matches").get();
    const matchScores = {};
    matchesSnapshot.forEach(doc => {
        const d = doc.data();
        if (d.homeScore !== null && d.awayScore !== null) {
            matchScores[doc.id] = { homeScore: d.homeScore, awayScore: d.awayScore };
        }
    });

    // 2. Atualiza todos os palpites com os pontos corretos baseados nas regras DESTE bolão
    const predictionsSnapshot = await db.collection("boloes").doc(bolaoId).collection("predictions").get();
    const batch = db.batch();

    predictionsSnapshot.forEach(doc => {
        const p = doc.data();
        const actual = matchScores[p.matchId];
        if (actual) {
            const points = calculatePoints(p, actual, pExact, pResult);
            batch.update(doc.ref, { points: points });
        }
    });

    await batch.commit();

    // 3. Atualiza o ranking dos participantes
    const participants = bolaoData.participants || [];
    await refreshUserRankings(db, admin, bolaoId, participants);
}

module.exports = { updateMatchRankings, refreshUserRankings, fullRecalculateRanking };
