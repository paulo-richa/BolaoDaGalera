const { logger } = require("firebase-functions");

/**
 * Lógica para avançar times no mata-mata da Libertadores.
 * Baseado no chaveamento padrão:
 * Oitavas 1 vs 8 -> QF1
 * Oitavas 2 vs 7 -> QF2
 * ...
 */
async function advanceTeams(db, admin, championshipId) {
    if (championshipId !== "LIBERTADORES") return;

    logger.info("Iniciando processamento de avanço de chave para Libertadores...");
    const matchesRef = db.collection("championships").doc(championshipId).collection("matches");

    try {
        const snapshot = await matchesRef.where("phase", "==", "ROUND_OF_16").get();
        const matches = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));

        // Agrupar por confronto (Ida e Volta)
        const groups = {};
        matches.forEach(m => {
            const order = m.matchOrder;
            if (!groups[order]) groups[order] = [];
            groups[order].push(m);
        });

        for (const order in groups) {
            const pair = groups[order];
            const ida = pair.find(m => m.id.includes("-L1"));
            const volta = pair.find(m => m.id.includes("-L2"));

            if (volta && volta.status === "FINISHED") {
                const winner = determineWinner(ida, volta);
                if (winner) {
                    await updateNextPhase(db, admin, championshipId, "QUARTERFINALS", parseInt(order), winner);
                }
            }
        }
    } catch (e) {
        logger.error("Erro ao avançar times:", e.message);
    }
}

function determineWinner(ida, volta) {
    if (!volta || volta.status !== "FINISHED") return null;

    // Placar Agregado
    const homeScoreTotal = (ida?.homeScore || 0) + (volta?.awayScore || 0); // Time 1 foi Home na Ida
    const awayScoreTotal = (ida?.awayScore || 0) + (volta?.homeScore || 0); // Time 2 foi Away na Ida

    if (homeScoreTotal > awayScoreTotal) {
        return { name: ida.homeTeam, code: ida.homeTeamCode, flag: ida.homeTeamFlag };
    } else if (awayScoreTotal > homeScoreTotal) {
        return { name: ida.awayTeam, code: ida.awayTeamCode, flag: ida.awayTeamFlag };
    } else {
        // Se houver empate, verificamos o vencedor por pênaltis se a API forneceu
        // (Isso depende da API, mas por segurança, se empatou no agregado o time que venceu a volta ganha)
        // Idealmente aqui checaríamos o campo "winner" da API.
        return null;
    }
}

async function updateNextPhase(db, admin, champId, nextPhase, currentOrder, winner) {
    const matchesRef = db.collection("championships").doc(champId).collection("matches");

    // Mapeamento de Oitavas para Quartas (Exemplo: 1 e 8 fazem a QF1)
    let targetOrder = 0;
    let isHome = true;

    if (nextPhase === "QUARTERFINALS") {
        // Exemplo de chaveamento: 1x8 (QF1), 2x7 (QF2), 3x6 (QF3), 4x5 (QF4)
        const mapping = { 1: [1, true], 8: [1, false], 2: [2, true], 7: [2, false], 3: [3, true], 6: [3, false], 4: [4, true], 5: [4, false] };
        if (mapping[currentOrder]) {
            [targetOrder, isHome] = mapping[currentOrder];
        }
    }

    if (targetOrder > 0) {
        const prefix = nextPhase === "QUARTERFINALS" ? "QF" : "SF";
        const matches = await matchesRef.where("phase", "==", nextPhase).where("matchOrder", "==", targetOrder).get();

        for (const doc of matches.docs) {
            const updates = {};
            if (isHome) {
                updates.homeTeam = winner.name;
                updates.homeTeamCode = winner.code;
                updates.homeTeamFlag = winner.flag;
            } else {
                updates.awayTeam = winner.name;
                updates.awayTeamCode = winner.code;
                updates.awayTeamFlag = winner.flag;
            }
            await doc.ref.update(updates);
        }
    }
}

module.exports = { advanceTeams };
