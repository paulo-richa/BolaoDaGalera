const { logger } = require("firebase-functions");

const SHORT_NAMES = {
    "Estudiantes de La Plata": "Estudiantes",
    "CD Universidad Católica": "Univ. Católica",
    "CA Rosario Central": "Rosario",
    "SC Corinthians Paulista": "Corinthians",
    "SE Palmeiras": "Palmeiras",
    "CR Flamengo": "Flamengo",
    "CA Mineiro": "Atlético-MG",
    "EC Bahia": "Bahia",
    "Fortaleza EC": "Fortaleza",
    "Botafogo FR": "Botafogo",
    "Fluminense FC": "Fluminense",
    "Cruzeiro EC": "Cruzeiro",
    "CA Paranaense": "Athletico-PR",
    "EC Vitória": "Vitória",
    "Independiente del Valle": "Ind. del Valle",
    "River Plate": "River Plate",
    "Peñarol": "Peñarol",
    "Nacional": "Nacional",
    "Colo Colo": "Colo-Colo",
    "Olimpia": "Olimpia",
    "CA Platense": "Platense",
    "CD Coquimbo Unido": "Coquimbo",
    "CS Independiente Rivadavia": "Ind. Rivadavia",
    "LDU de Quito": "LDU",
    "Mirassol": "Mirassol",
    "Club Cerro Porteño": "Cerro Porteño"
};

function getShortName(fullName) {
    if (!fullName) return "";
    if (SHORT_NAMES[fullName]) return SHORT_NAMES[fullName];
    return fullName.replace("CA ", "").replace("CD ", "").replace("SC ", "").replace("FC ", "").replace("CAR ", "").replace("CS ", "").trim().split(" ").shift();
}

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
                // Prioriza as bandeiras (emojis) se existirem
                const flagA = (ida.homeTeamFlag && ida.homeTeamFlag !== "🏳️") ? ida.homeTeamFlag : getShortName(ida.homeTeam);
                const flagB = (ida.awayTeamFlag && ida.awayTeamFlag !== "🏳️") ? ida.awayTeamFlag : getShortName(ida.awayTeam);

                const candidates = {
                    name: `${flagA} ou ${flagB}`,
                    code: "TBD",
                    flag: "🏳️"
                };
                await updateNextPhase(db, admin, championshipId, "QUARTERFINALS", parseInt(order), candidates, false);
            }
        }

        // 2. QUARTAS -> SEMIS (Apenas se as Quartas já tiverem times definidos ou candidatos)
        const matchesQF = allMatches.filter(m => m.phase === "QUARTERFINALS");
        const groupsQF = groupByOrder(matchesQF);
        for (const order in groupsQF) {
            const pair = groupsQF[order];
            const ida = pair.find(m => m.id.includes("-L1"));
            const volta = pair.find(m => m.id.includes("-L2"));

            if (volta && (volta.status === "FINISHED" || volta.homeScore !== null)) {
                const winner = determineWinner(ida, volta);
                if (winner) await updateNextPhase(db, admin, championshipId, "SEMIFINALS", parseInt(order), winner, true);
            } else if (ida && ida.homeTeam !== "TBD" && !ida.homeTeam.startsWith("Vencedor")) {
                 // Candidatos para as Semis baseados nas Quartas
                 const flagA = (ida.homeTeamFlag && ida.homeTeamFlag !== "🏳️") ? ida.homeTeamFlag : getShortName(ida.homeTeam);
                 const flagB = (ida.awayTeamFlag && ida.awayTeamFlag !== "🏳️") ? ida.awayTeamFlag : getShortName(ida.awayTeam);
                 const candidates = {
                     name: `${flagA} ou ${flagB}`,
                     code: "TBD",
                     flag: "🏳️"
                 };
                 await updateNextPhase(db, admin, championshipId, "SEMIFINALS", parseInt(order), candidates, false);
            }
        }

    } catch (e) {
        logger.error("Erro no avanço de chave:", e.message);
    }
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
    if (hScore > aScore) return { name: ida.homeTeam, code: ida.homeTeamCode, flag: ida.homeTeamFlag };
    if (aScore > hScore) return { name: ida.awayTeam, code: ida.awayTeamCode, flag: ida.awayTeamFlag };
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
                updates.homeTeamFlag = isFinalWinner ? (winner.flag || "🏳️") : "🏳️";
            } else {
                updates.awayTeam = winner.name;
                updates.awayTeamCode = isFinalWinner ? (winner.code || "TBD") : "TBD";
                updates.awayTeamFlag = isFinalWinner ? (winner.flag || "🏳️") : "🏳️";
            }
            await doc.ref.update(updates);
        }
    }
}

module.exports = { advanceTeams };
