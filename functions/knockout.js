const { logger } = require("firebase-functions");
const { LIB_TEAMS } = require("./teams_lib");

/**
 * Lógica para avançar times no mata-mata da Libertadores.
 */
async function advanceTeams(db, admin, championshipId) {
    if (championshipId !== "LIBERTADORES") return;
    const matchesRef = db.collection("championships").doc(championshipId).collection("matches");

    try {
        const snapshot = await matchesRef.get();
        const allMatches = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));

        // 1. OITAVAS -> QUARTAS
        const matches16 = allMatches.filter(m => m.phase === "ROUND_OF_16");
        const groups16 = groupByOrder(matches16);

        for (const order in groups16) {
            const pair = groups16[order];
            const ida = pair.find(m => m.id.includes("-L1"));
            const volta = pair.find(m => m.id.includes("-L2"));

            if (volta && (volta.status === "FINISHED" || volta.homeScore !== null)) {
                const winner = determineWinner(ida, volta);
                if (winner) await updateNextPhase(db, admin, championshipId, "QUARTERFINALS", parseInt(order), winner, true);
            } else if (ida) {
                // Candidatos para as Quartas
                const nameA = getCleanName(ida.homeTeam);
                const nameB = getCleanName(ida.awayTeam);
                const candidates = { name: `${nameA} ou ${nameB}`, code: "TBD", flag: "", crest: null };
                await updateNextPhase(db, admin, championshipId, "QUARTERFINALS", parseInt(order), candidates, false);
            }
        }

        // 2. QUARTAS -> SEMIS
        const matchesQF = allMatches.filter(m => m.phase === "QUARTERFINALS");
        const groupsQF = groupByOrder(matchesQF);
        for (const order in groupsQF) {
            const pair = groupsQF[order];
            const ida = pair.find(m => m.id.includes("-L1"));
            const volta = pair.find(m => m.id.includes("-L2"));

            if (volta && (volta.status === "FINISHED" || volta.homeScore !== null)) {
                const winner = determineWinner(ida, volta);
                if (winner) await updateNextPhase(db, admin, championshipId, "SEMIFINALS", parseInt(order), winner, true);
            } else if (ida && ida.homeTeamCode !== "TBD" && ida.awayTeamCode !== "TBD") {
                const nameA = getCleanName(ida.homeTeam);
                const nameB = getCleanName(ida.awayTeam);
                const candidates = { name: `${nameA} ou ${nameB}`, code: "TBD", flag: "", crest: null };
                await updateNextPhase(db, admin, championshipId, "SEMIFINALS", parseInt(order), candidates, false);
            }
        }

    } catch (e) {
        logger.error("Erro no avanço de chave:", e.message);
    }
}

function getCleanName(fullName) {
    if (!fullName) return "";
    // Busca no LIB_TEAMS pelo nome limpo oficial
    for (const [key, data] of Object.entries(LIB_TEAMS)) {
        if (key === fullName || data.name === fullName) return data.name;
    }
    return fullName.replace("CA ", "").replace("CD ", "").replace("SC ", "").replace("FC ", "").replace("CR ", "").replace("SE ", "").trim().split(" ").shift();
}

function groupByOrder(matches) {
    const groups = {};
    matches.forEach(m => {
        if (m.matchOrder) {
            if (!groups[m.matchOrder]) groups[m.matchOrder] = [];
            groups[m.matchOrder].push(m);
        }
    });
    return groups;
}

function determineWinner(ida, volta) {
    if (!volta) return null;
    const hScore = (ida?.homeScore || 0) + (volta?.awayScore || 0);
    const aScore = (ida?.awayScore || 0) + (volta?.homeScore || 0);

    if (hScore > aScore) return { name: ida.homeTeam, code: ida.homeTeamCode, flag: ida.homeTeamFlag, crest: ida.homeTeamCrest };
    if (aScore > hScore) return { name: ida.awayTeam, code: ida.awayTeamCode, flag: ida.awayTeamFlag, crest: ida.awayTeamCrest };

    // Empate no agregado (Pênaltis - simplificação: quem joga em casa na volta passa se não houver info)
    return null;
}

async function updateNextPhase(db, admin, champId, nextPhase, currentOrder, winner, isFinalWinner) {
    const matchesRef = db.collection("championships").doc(champId).collection("matches");
    let targetOrder = 0;
    let isHome = true;

    if (nextPhase === "QUARTERFINALS") {
        const mapping = { 1: [1, true], 8: [1, false], 2: [2, true], 7: [2, false], 3: [3, true], 6: [3, false], 4: [4, true], 5: [4, false] };
        if (mapping[currentOrder]) [targetOrder, isHome] = mapping[currentOrder];
    } else if (nextPhase === "SEMIFINALS") {
        const mapping = { 1: [1, true], 4: [1, false], 2: [2, true], 3: [2, false] };
        if (mapping[currentOrder]) [targetOrder, isHome] = mapping[currentOrder];
    }

    if (targetOrder > 0) {
        const matches = await matchesRef.where("phase", "==", nextPhase).where("matchOrder", "==", targetOrder).get();
        for (const doc of matches.docs) {
            const updates = {};
            if (isHome) {
                updates.homeTeam = winner.name;
                updates.homeTeamCode = isFinalWinner ? (winner.code || "TBD") : "TBD";
                updates.homeTeamFlag = winner.flag || "";
                updates.homeTeamCrest = winner.crest || null;
            } else {
                updates.awayTeam = winner.name;
                updates.awayTeamCode = isFinalWinner ? (winner.code || "TBD") : "TBD";
                updates.awayTeamFlag = winner.flag || "";
                updates.awayTeamCrest = winner.crest || null;
            }
            await doc.ref.update(updates);
        }
    }
}

module.exports = { advanceTeams };
