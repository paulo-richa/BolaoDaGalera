const { logger } = require("firebase-functions");
const { notifyUser } = require("./notifications");

/** "Hoje" em America/Sao_Paulo (mesmo fuso que o client usa pra decidir "jogos de hoje"). */
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
    return { startMillis, endMillis };
}

/**
 * Jogos de hoje relevantes pra ESSE bolão, respeitando o escopo
 * (ONLY_GROUPS/ONLY_KNOCKOUT/specificMatchId) - sem isso um bolão só de
 * mata-mata lembraria o usuário de jogos da fase de grupos que ele nem
 * consegue ver na lista dele. Não replica o corte de rodada (round cutoff)
 * do FilterBolaoMatchesUseCase do client - caso raro (bolão criado no meio
 * da temporada) e o custo de duplicar aquela lógica aqui não compensa por
 * enquanto.
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
 * Monta { userId -> quantidade de jogos de hoje ainda sem palpite }, somando
 * em todos os bolões que o usuário participa (cada bolão conta separado,
 * mesmo que seja o mesmo jogo real - são palpites independentes).
 */
async function computeMissingPredictionsByUser(db) {
    const { startMillis, endMillis } = todayWindowMillis();

    const matchesSnap = await db.collectionGroup("matches")
        .where("matchDateMillis", ">=", startMillis)
        .where("matchDateMillis", "<=", endMillis)
        .get();

    if (matchesSnap.empty) return {};

    const matchesTodayByChampionship = {};
    matchesSnap.forEach((doc) => {
        const m = doc.data();
        const champId = m.championshipId;
        if (!champId) return;
        if (!matchesTodayByChampionship[champId]) matchesTodayByChampionship[champId] = [];
        matchesTodayByChampionship[champId].push({ id: doc.id, phase: m.phase });
    });

    if (Object.keys(matchesTodayByChampionship).length === 0) return {};

    const boloesSnap = await db.collection("boloes").get();
    const missingByUser = {};

    for (const bolaoDoc of boloesSnap.docs) {
        const bolao = bolaoDoc.data();
        const relevant = relevantMatchesForBolao(bolao, matchesTodayByChampionship);
        if (relevant.length === 0) continue;

        const participants = bolao.participants || [];
        if (participants.length === 0) continue;

        const relevantIds = relevant.map((m) => m.id);
        const predictedCountByUser = {};
        // "in" aceita até 30 valores - jogos de UM dia num único bolão nunca
        // chegam nem perto disso.
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
                missingByUser[userId] = (missingByUser[userId] || 0) + missing;
            }
        }
    }

    return missingByUser;
}

async function sendDailyDigest(db, admin) {
    let missingByUser;
    try {
        missingByUser = await computeMissingPredictionsByUser(db);
    } catch (e) {
        logger.error("Erro ao calcular resumo diário:", e.message);
        return;
    }

    const userIds = Object.keys(missingByUser);
    logger.info(`Resumo diário: ${userIds.length} usuário(s) com jogos de hoje sem palpite.`);

    for (const userId of userIds) {
        const count = missingByUser[userId];
        await notifyUser(db, admin, userId, {
            title: "Jogos de Hoje! ⚽",
            message: `Você tem ${count} jogo(s) hoje sem palpite. Não perca pontos!`,
            type: "MATCH_REMINDER"
        });
    }
}

module.exports = { todayWindowMillis, relevantMatchesForBolao, computeMissingPredictionsByUser, sendDailyDigest };
