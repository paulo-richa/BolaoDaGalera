const { logger } = require("firebase-functions");
const { notifyUser } = require("./notifications");
const { relevantMatchesForBolao } = require("./dailyDigest");

/** Notifies users without a prediction once less than this remains before the match locks. */
const REMINDER_WINDOW_MILLIS = 60 * 60 * 1000;

/**
 * Marks matchReminders/{bolaoId}_{matchId}_{userId} to never send the same
 * reminder twice, even when running every few minutes and covering the
 * same 1h window across successive runs. create() fails if the document
 * already exists (atomic idempotency: whoever creates the marker first is
 * the one who sends the notification).
 */
async function claimReminder(db, key) {
    try {
        await db.collection("matchReminders").doc(key).create({ sentAtMillis: Date.now() });
        return true;
    } catch (e) {
        if (e.code === 6 || (e.message && e.message.includes("already exists"))) return false;
        throw e;
    }
}

async function findMatchesStartingSoon(db, now) {
    const windowEnd = now + REMINDER_WINDOW_MILLIS;
    const snap = await db.collectionGroup("matches")
        .where("matchDateMillis", ">=", now)
        .where("matchDateMillis", "<=", windowEnd)
        .get();

    const matchesByChampionship = {};
    snap.forEach((doc) => {
        const m = doc.data();
        if (!m.championshipId) return;
        if (!matchesByChampionship[m.championshipId]) matchesByChampionship[m.championshipId] = [];
        matchesByChampionship[m.championshipId].push({
            id: doc.id,
            phase: m.phase,
            homeTeam: m.homeTeam,
            awayTeam: m.awayTeam
        });
    });
    return matchesByChampionship;
}

async function sendMatchReminders(db, admin, now = Date.now()) {
    let matchesByChampionship;
    try {
        matchesByChampionship = await findMatchesStartingSoon(db, now);
    } catch (e) {
        logger.error("Erro ao buscar jogos prestes a começar:", e.message);
        return;
    }
    if (Object.keys(matchesByChampionship).length === 0) return;

    const boloesSnap = await db.collection("boloes").get();

    for (const bolaoDoc of boloesSnap.docs) {
        const bolao = bolaoDoc.data();
        const relevant = relevantMatchesForBolao(bolao, matchesByChampionship);
        if (relevant.length === 0) continue;

        const participants = bolao.participants || [];
        if (participants.length === 0) continue;

        for (const match of relevant) {
            const predSnap = await bolaoDoc.ref.collection("predictions")
                .where("matchId", "==", match.id)
                .get();
            const predictedUserIds = new Set(predSnap.docs.map((d) => d.data().userId));

            for (const userId of participants) {
                if (predictedUserIds.has(userId)) continue;

                const claimed = await claimReminder(db, `${bolaoDoc.id}_${match.id}_${userId}`);
                if (!claimed) continue;

                await notifyUser(db, admin, userId, {
                    title: "Fechando em breve! ⏰",
                    message: `${match.homeTeam || "Time A"} x ${match.awayTeam || "Time B"} começa em menos de 1h ` +
                        "e você ainda não palpitou.",
                    type: "MATCH_REMINDER",
                    bolaoId: bolaoDoc.id,
                    matchId: match.id,
                    deepLink: `bolaodagalera://predict?bolaoId=${bolaoDoc.id}&matchId=${match.id}`
                });
            }
        }
    }
}

module.exports = { sendMatchReminders, REMINDER_WINDOW_MILLIS };
