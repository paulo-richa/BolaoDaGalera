const { calculatePoints } = require("./scoring");
const { logger } = require("firebase-functions");

/**
 * Updates the points of each prediction and the bolao's overall ranking for a specific match.
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

        // Get the bolao's scoring rules (falling back to 3 and 1)
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
 * Recalculates the total points of specific users in a bolao.
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
 * Utility function to recalculate the ENTIRE ranking of a bolao.
 */
async function fullRecalculateRanking(db, admin, bolaoId) {
    const bolaoDoc = await db.collection("boloes").doc(bolaoId).get();
    if (!bolaoDoc.exists) return;

    const bolaoData = bolaoDoc.data();
    const championshipId = bolaoData.championshipId;
    const pExact = bolaoData.pointsExactScore || 3;
    const pResult = bolaoData.pointsWinnerOrDraw || 1;

    // 1. Get all real results for the championship
    const matchesSnapshot = await db.collection("championships").doc(championshipId).collection("matches").get();
    const matchScores = {};
    matchesSnapshot.forEach(doc => {
        const d = doc.data();
        if (d.homeScore !== null && d.awayScore !== null) {
            matchScores[doc.id] = { homeScore: d.homeScore, awayScore: d.awayScore };
        }
    });

    // 2. Update all predictions with the correct points based on THIS bolao's rules
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

    // 3. Update the participants' ranking
    const participants = bolaoData.participants || [];
    await refreshUserRankings(db, admin, bolaoId, participants);
}

module.exports = { updateMatchRankings, refreshUserRankings, fullRecalculateRanking };
