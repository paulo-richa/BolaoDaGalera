const { logger } = require("firebase-functions");
const { calculatePoints } = require("./scoring");
const { notifyUser } = require("./notifications");

const PHASE_LABELS = {
    GROUP_STAGE: "Fase de Grupos",
    ROUND_OF_32: "16-avos",
    ROUND_OF_16: "Oitavas",
    QUARTERFINALS: "Quartas",
    SEMIFINALS: "Semifinal",
    THIRD_PLACE: "Disputa de 3º Lugar",
    FINAL: "Final"
};

/**
 * Pontos corridos (Brasileirão) usam "Rodada N" (extraído do campo group,
 * que brasileirao.js já grava exatamente nesse formato) - mata-mata/grupos
 * usa a fase (Oitavas, Quartas...), por decisão do produto: só as fases de
 * mata-mata geram resumo pra campeonatos não pontos-corridos.
 */
function roundKeyForMatch(championship, match) {
    if (championship && championship.isPointsBased) {
        const m = /Rodada (\d+)/.exec(match.group || "");
        return m ? `RODADA_${m[1]}` : null;
    }
    return match.phase || null;
}

function roundLabel(championship, roundKey) {
    if (championship && championship.isPointsBased) {
        return roundKey.replace("RODADA_", "Rodada ");
    }
    return PHASE_LABELS[roundKey] || roundKey;
}

/** Essa rodada/fase é relevante pro escopo desse bolão? */
function roundAppliesToBolao(bolao, roundKey, roundMatchIds) {
    if (bolao.specificMatchId) return roundMatchIds.includes(bolao.specificMatchId);
    if (bolao.scope === "ONLY_GROUPS") return roundKey === "GROUP_STAGE" || roundKey.startsWith("RODADA_");
    if (bolao.scope === "ONLY_KNOCKOUT") return roundKey !== "GROUP_STAGE" && !roundKey.startsWith("RODADA_");
    return true;
}

function chunk(list, size) {
    const chunks = [];
    for (let i = 0; i < list.length; i += size) chunks.push(list.slice(i, i + size));
    return chunks;
}

/** Firestore "in" aceita no máximo 30 valores por query. */
async function fetchPredictionsForMatches(predictionsRef, matchIds) {
    const docs = [];
    for (const ids of chunk(matchIds, 30)) {
        const snap = await predictionsRef.where("matchId", "in", ids).get();
        snap.forEach((doc) => docs.push(doc.data()));
    }
    return docs;
}

/**
 * Tenta "trancar" essa rodada/fase pra essa notificação nunca ser
 * processada duas vezes (mesma partida disparando onMatchUpdate de novo, ou
 * duas partidas do mesmo round finalizando quase juntas). create() falha se
 * o documento já existe.
 */
async function claimRoundCompletion(db, lockKey) {
    try {
        await db.collection("roundCompletions").doc(lockKey).create({ completedAtMillis: Date.now() });
        return true;
    } catch (e) {
        if (e.code === 6 || (e.message && e.message.includes("already exists"))) return false;
        throw e;
    }
}

/**
 * Chamado depois que um jogo é marcado FINISHED (ver onMatchUpdate). Verifica
 * se isso fecha a rodada/fase inteira do campeonato e, se sim, manda o
 * resumo (pontos, acertos, erros) pra cada participante de cada bolão
 * daquele campeonato que tem essa rodada no escopo.
 */
async function checkRoundCompletionAndNotify(db, admin, championshipId, match) {
    try {
        const champDoc = await db.collection("championships").doc(championshipId).get();
        const championship = champDoc.exists ? champDoc.data() : {};

        const roundKey = roundKeyForMatch(championship, match);
        if (!roundKey) return;

        const matchesRef = db.collection("championships").doc(championshipId).collection("matches");
        const roundSnap = championship.isPointsBased
            ? await matchesRef.where("group", "==", `Rodada ${roundKey.replace("RODADA_", "")}`).get()
            : await matchesRef.where("phase", "==", roundKey).get();
        if (roundSnap.empty) return;

        const roundMatches = roundSnap.docs.map((d) => d.data());
        const allFinished = roundMatches.every(
            (m) => m.status === "FINISHED" && m.homeScore !== null && m.homeScore !== undefined &&
                m.awayScore !== null && m.awayScore !== undefined
        );
        if (!allFinished) return;

        const claimed = await claimRoundCompletion(db, `${championshipId}_${roundKey}`);
        if (!claimed) return;

        const roundMatchIds = roundSnap.docs.map((d) => d.id);
        const scoresByMatchId = {};
        roundSnap.docs.forEach((d) => {
            const m = d.data();
            scoresByMatchId[d.id] = { homeScore: m.homeScore, awayScore: m.awayScore };
        });

        const boloesSnap = await db.collection("boloes").where("championshipId", "==", championshipId).get();
        const label = roundLabel(championship, roundKey);

        for (const bolaoDoc of boloesSnap.docs) {
            const bolao = bolaoDoc.data();
            if (!roundAppliesToBolao(bolao, roundKey, roundMatchIds)) continue;

            const participants = bolao.participants || [];
            if (participants.length === 0) continue;

            const pExact = bolao.pointsExactScore || 3;
            const pResult = bolao.pointsWinnerOrDraw || 1;

            const predictions = await fetchPredictionsForMatches(bolaoDoc.ref.collection("predictions"), roundMatchIds);

            const byUser = {};
            predictions.forEach((p) => {
                const actual = scoresByMatchId[p.matchId];
                if (!actual) return;
                if (!byUser[p.userId]) byUser[p.userId] = { points: 0, hits: 0, misses: 0 };
                const pts = calculatePoints(p, actual, pExact, pResult);
                byUser[p.userId].points += pts;
                if (pts > 0) byUser[p.userId].hits += 1;
                else byUser[p.userId].misses += 1;
            });

            for (const userId of participants) {
                const stats = byUser[userId];
                if (!stats) continue; // não palpitou nada nessa rodada nesse bolão

                await notifyUser(db, admin, userId, {
                    title: `Fim da ${label}! 🏁`,
                    message: `Você fez ${stats.points} ponto(s): ${stats.hits} acerto(s) e ${stats.misses} erro(s).`,
                    type: "ROUND_SUMMARY",
                    bolaoId: bolaoDoc.id,
                    deepLink: `bolaodagalera://bolao?bolaoId=${bolaoDoc.id}`
                });
            }
        }
    } catch (e) {
        logger.error(`Erro no resumo de rodada de ${championshipId}:`, e.message);
    }
}

module.exports = { checkRoundCompletionAndNotify, roundKeyForMatch, roundLabel, roundAppliesToBolao };
