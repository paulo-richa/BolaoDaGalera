const { logger } = require("firebase-functions");
const { notifyUser } = require("./notifications");

/** "Today" in America/Sao_Paulo (same timezone the client uses to decide "today's matches"). */
function todayWindowMillis(now = new Date()) {
    const parts = new Intl.DateTimeFormat("en-CA", {
        timeZone: "America/Sao_Paulo",
        year: "numeric",
        month: "2-digit",
        day: "2-digit"
    }).formatToParts(now);
    const y = parts.find((p) => p.type === "year").value;
    const m = parts.find((p) => p.type === "month").value;
    const d = parts.find((p) => p.type === "day").value;

    const startMillis = Date.parse(`${y}-${m}-${d}T00:00:00-03:00`);
    const endMillis = startMillis + 24 * 3600 * 1000 - 1;
    return { startMillis, endMillis, dateKey: `${y}-${m}-${d}` };
}

/**
 * Today's matches relevant to THIS bolao, respecting its scope
 * (ONLY_GROUPS/ONLY_KNOCKOUT/specificMatchId) - without this, a
 * knockout-only bolao would remind users about group-stage matches they
 * can't even see in their list. Does not replicate the round cutoff logic
 * from the client's FilterBolaoMatchesUseCase - a rare case (bolao created
 * mid-season) not worth duplicating that logic here for now.
 */
function relevantMatchesForBolao(bolao, matchesTodayByChampionship) {
    const matches = matchesTodayByChampionship[bolao.championshipId] || [];
    if (bolao.specificMatchId) {
        return matches.filter((m) => m.id === bolao.specificMatchId);
    }
    if (bolao.scope === "ONLY_GROUPS") {
        return matches.filter((m) => m.phase === "GROUP_STAGE");
    }
    if (bolao.scope === "ONLY_KNOCKOUT") {
        return matches.filter((m) => m.phase !== "GROUP_STAGE");
    }
    return matches;
}

/**
 * Builds one entry per (bolao, user) with matches still missing a prediction
 * today - kept per-bolao (never summed across bolões) since a user in
 * several bolões covering the same real-world match would otherwise see an
 * inflated count that doesn't match "today's games" from their point of view.
 */
async function computeMissingPredictionsByBolao(db) {
    const { startMillis, endMillis } = todayWindowMillis();

    const matchesSnap = await db.collectionGroup("matches")
        .where("matchDateMillis", ">=", startMillis)
        .where("matchDateMillis", "<=", endMillis)
        .get();

    if (matchesSnap.empty) return [];

    const matchesTodayByChampionship = {};
    matchesSnap.forEach((doc) => {
        const m = doc.data();
        const champId = m.championshipId;
        if (!champId) return;
        if (!matchesTodayByChampionship[champId]) matchesTodayByChampionship[champId] = [];
        matchesTodayByChampionship[champId].push({ id: doc.id, phase: m.phase });
    });

    if (Object.keys(matchesTodayByChampionship).length === 0) return [];

    const boloesSnap = await db.collection("boloes").get();
    const entries = [];

    for (const bolaoDoc of boloesSnap.docs) {
        const bolao = bolaoDoc.data();
        const relevant = relevantMatchesForBolao(bolao, matchesTodayByChampionship);
        if (relevant.length === 0) continue;

        const participants = bolao.participants || [];
        if (participants.length === 0) continue;

        const relevantIds = relevant.map((m) => m.id);
        const predictedCountByUser = {};
        // "in" accepts up to 30 values - a single bolao's matches for ONE
        // day never come close to that limit.
        const predictionsSnap = await bolaoDoc.ref.collection("predictions")
            .where("matchId", "in", relevantIds)
            .get();
        predictionsSnap.forEach((doc) => {
            const userId = doc.data().userId;
            predictedCountByUser[userId] = (predictedCountByUser[userId] || 0) + 1;
        });

        for (const userId of participants) {
            const missing = relevantIds.length - (predictedCountByUser[userId] || 0);
            if (missing > 0) {
                entries.push({
                    bolaoId: bolaoDoc.id,
                    bolaoName: bolao.name || "seu bolão",
                    userId,
                    missing
                });
            }
        }
    }

    return entries;
}

/**
 * Prevents the same (day, bolao, user) reminder from being sent twice, e.g.
 * on a Cloud Scheduler retry. create() fails if the marker already exists:
 * whoever creates it first is the one who sends the notification.
 */
async function claimDailyDigest(db, dateKey, bolaoId, userId) {
    try {
        await db.collection("dailyDigestsSent").doc(`${dateKey}_${bolaoId}_${userId}`).create({ sentAtMillis: Date.now() });
        return true;
    } catch (e) {
        if (e.code === 6 || (e.message && e.message.includes("already exists"))) return false;
        throw e;
    }
}

async function sendDailyDigest(db, admin) {
    let entries;
    try {
        entries = await computeMissingPredictionsByBolao(db);
    } catch (e) {
        logger.error("Erro ao calcular resumo diário:", e.message);
        return;
    }

    const { dateKey } = todayWindowMillis();
    logger.info(`Resumo diário: ${entries.length} lembrete(s) de bolão a enviar.`);

    for (const entry of entries) {
        const claimed = await claimDailyDigest(db, dateKey, entry.bolaoId, entry.userId);
        if (!claimed) continue;

        await notifyUser(db, admin, entry.userId, {
            title: "Jogos de Hoje! ⚽",
            message: `Você tem ${entry.missing} jogo(s) sem palpite hoje no bolão "${entry.bolaoName}". Não perca pontos!`,
            type: "MATCH_REMINDER",
            bolaoId: entry.bolaoId,
            deepLink: `bolaodagalera://bolao?bolaoId=${entry.bolaoId}`
        });
    }
}

module.exports = { todayWindowMillis, relevantMatchesForBolao, computeMissingPredictionsByBolao, sendDailyDigest };
