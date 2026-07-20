const { onSchedule } = require("firebase-functions/v2/scheduler");
const { logger } = require("firebase-functions");
const admin = require("firebase-admin");
const axios = require("axios");

admin.initializeApp();
const db = admin.firestore();
const API_KEY = "***REMOVED_SECRET***";

const TEAM_DATA = {
    // BRASILEIRÃO
    "SE Palmeiras": { name: "Palmeiras", flag: "🏳️", code: "PAL", crest: "https://crests.football-data.org/1765.svg" },
    "CR Flamengo": { name: "Flamengo", flag: "🏳️", code: "FLA", crest: "https://crests.football-data.org/1783.svg" },
    "SC Corinthians Paulista": { name: "Corinthians", flag: "🏳️", code: "COR", crest: "https://crests.football-data.org/1779.svg" },
    "São Paulo FC": { name: "São Paulo", flag: "🏳️", code: "SAO", crest: "https://crests.football-data.org/1768.svg" },
    "Botafogo FR": { name: "Botafogo", flag: "🏳️", code: "BOT", crest: "https://crests.football-data.org/267.svg" },
    "Fluminense FC": { name: "Fluminense", flag: "🏳️", code: "FLU", crest: "https://crests.football-data.org/1766.svg" },
    "CA Mineiro": { name: "Atlético-MG", flag: "🏳️", code: "CAM", crest: "https://crests.football-data.org/1767.svg" },
    "Grêmio FBPA": { name: "Grêmio", flag: "🏳️", code: "GRE", crest: "https://crests.football-data.org/1769.svg" },
    "SC Internacional": { name: "Internacional", flag: "🏳️", code: "INT", crest: "https://crests.football-data.org/1771.svg" },
    "Cruzeiro EC": { name: "Cruzeiro", flag: "🏳️", code: "CRU", crest: "https://crests.football-data.org/1773.svg" },
    "CR Vasco da Gama": { name: "Vasco", flag: "🏳️", code: "VAS", crest: "https://crests.football-data.org/1780.svg" },
    "Vasco da Gama": { name: "Vasco", flag: "🏳️", code: "VAS", crest: "https://crests.football-data.org/1780.svg" },
    "EC Bahia": { name: "Bahia", flag: "🏳️", code: "BAH", crest: "https://crests.football-data.org/1777.svg" },
    "CA Paranaense": { name: "Athletico-PR", flag: "🏳️", code: "CAP", crest: "https://crests.football-data.org/1770.svg" },
    "Fortaleza EC": { name: "Fortaleza", flag: "🏳️", code: "FOR", crest: "https://crests.football-data.org/3432.svg" },
    "EC Vitória": { name: "Vitória", flag: "🏳️", code: "VIT", crest: "https://crests.football-data.org/1772.svg" },
    "Santos FC": { name: "Santos", flag: "🏳️", code: "SAN", crest: "https://crests.football-data.org/1774.svg" },
    "RB Bragantino": { name: "Bragantino", flag: "🏳️", code: "RBB", crest: "https://crests.football-data.org/4286.svg" },
    "Coritiba FBC": { name: "Coritiba", flag: "🏳️", code: "CFC", crest: "https://crests.football-data.org/4241.svg" },
    "Mirassol FC": { name: "Mirassol", flag: "🏳️", code: "MIR" },
    "Chapecoense AF": { name: "Chapecoense", flag: "🏳️", code: "CHA" },
    "Clube do Remo": { name: "Remo", flag: "🏳️", code: "CRE" },
    // LIBERTADORES
    "River Plate": { name: "River Plate", flag: "🇦🇷", code: "RIV", crest: "https://crests.football-data.org/430.svg" },
    "CA Boca Juniors": { name: "Boca Juniors", flag: "🇦🇷", code: "BOC", crest: "https://crests.football-data.org/431.svg" },
    "Peñarol": { name: "Peñarol", flag: "🇺🇾", code: "PEN", crest: "https://crests.football-data.org/1066.svg" },
    "Club Nacional de Football": { name: "Nacional", flag: "🇺🇾", code: "NAC", crest: "https://crests.football-data.org/1065.svg" },
    "Colo Colo": { name: "Colo-Colo", flag: "🇨🇱", code: "COL", crest: "https://crests.football-data.org/1071.svg" },
    "Olimpia": { name: "Olimpia", flag: "🇵🇾", code: "OLI", crest: "https://crests.football-data.org/1073.svg" },
    "Cerro Porteño": { name: "Cerro Porteño", flag: "🇵🇾", code: "CCP", crest: "https://crests.football-data.org/1074.svg" },
    "LDU Quito": { name: "LDU Quito", flag: "🇪🇨", code: "LDU", crest: "https://crests.football-data.org/1075.svg" },
    "Independiente del Valle": { name: "Ind. del Valle", flag: "🇪🇨", code: "IDV", crest: "https://crests.football-data.org/1076.svg" },
    "Bolívar": { name: "Bolívar", flag: "🇧🇴", code: "BOL", crest: "https://crests.football-data.org/1077.svg" },
    "The Strongest": { name: "The Strongest", flag: "🇧🇴", code: "STR", crest: "https://crests.football-data.org/1078.svg" },
    "San Lorenzo": { name: "San Lorenzo", flag: "🇦🇷", code: "SLO", crest: "https://crests.football-data.org/1079.svg" },
    "Junior": { name: "Junior", flag: "🇨🇴", code: "JUN", crest: "https://crests.football-data.org/1080.svg" },
    "Talleres": { name: "Talleres", flag: "🇦🇷", code: "TAL", crest: "https://crests.football-data.org/1081.svg" }
};

function mapPhase(stage) {
    switch (stage) {
        case "GROUP_STAGE": return "GROUP_STAGE";
        case "ROUND_OF_16": return "ROUND_OF_16";
        case "QUARTER_FINALS": return "QUARTERFINALS";
        case "SEMI_FINALS": return "SEMIFINALS";
        case "FINAL": return "FINAL";
        default: return "GROUP_STAGE";
    }
}

exports.syncScores = onSchedule({
    schedule: "every 1 minutes",
    timeZone: "America/Sao_Paulo",
    region: "southamerica-east1",
    memory: "256MiB",
    timeoutSeconds: 120
}, async (event) => {
    try {
        const matchesRef = db.collection('matches');

        // 1. BRASILEIRÃO 2026
        const resBSA = await axios.get(`https://api.football-data.org/v4/competitions/BSA/matches`, {
            headers: { 'X-Auth-Token': API_KEY },
            timeout: 15000
        }).catch(() => null);

        if (resBSA && resBSA.data && resBSA.data.matches) {
            const batch = db.batch();
            for (const m of resBSA.data.matches) {
                const matchId = `BSA-2026-R${m.matchday}-${m.id}`;
                const s = m.score;
                batch.set(matchesRef.doc(matchId), {
                    status: m.status,
                    homeScore: s?.fullTime?.home ?? s?.regularTime?.home ?? null,
                    awayScore: s?.fullTime?.away ?? s?.regularTime?.away ?? null,
                    championshipId: "BRASILEIRAO",
                    lastSync: admin.firestore.FieldValue.serverTimestamp()
                }, { merge: true });
            }
            await batch.commit();
        }

        // 2. LIBERTADORES 2026
        const resCLI = await axios.get(`https://api.football-data.org/v4/competitions/CLI/matches`, {
            headers: { 'X-Auth-Token': API_KEY },
            timeout: 15000
        }).catch(() => null);

        if (resCLI && resCLI.data && resCLI.data.matches) {
            const batch = db.batch();
            for (const m of resCLI.data.matches) {
                const isVolta = m.matchday === 2 || m.stage.includes("LEG2");
                const legSuffix = isVolta ? "-L2" : "-L1";
                const internalPhase = mapPhase(m.stage);
                if (m.stage.includes("ROUND_1")) continue;

                const geOrderMap = {
                    '564455': { o: 8, l: 1 }, '564463': { o: 8, l: 2 },
                    '564456': { o: 1, l: 1 }, '564465': { o: 1, l: 2 },
                    '564462': { o: 2, l: 1 }, '564470': { o: 2, l: 2 },
                    '564460': { o: 3, l: 1 }, '564468': { o: 3, l: 2 },
                    '564457': { o: 4, l: 1 }, '564464': { o: 4, l: 2 },
                    '564461': { o: 5, l: 1 }, '564469': { o: 5, l: 2 },
                    '564459': { o: 6, l: 1 }, '564466': { o: 6, l: 2 },
                    '564458': { o: 7, l: 1 }, '564467': { o: 7, l: 2 }
                };

                const matchInfo = geOrderMap[m.id.toString()];
                if (internalPhase !== "GROUP_STAGE" && !matchInfo) continue;

                let matchOrder = matchInfo ? matchInfo.o : (m.matchday || 99);
                let matchId = `CLI-2026-M${m.id}${legSuffix}`;
                if (internalPhase === "ROUND_OF_16") matchId = `CLI-2026-R16-${matchOrder}${legSuffix}`;

                const hTeam = TEAM_DATA[m.homeTeam.name] || { name: m.homeTeam.name, flag: "🏳️", code: m.homeTeam.tla };
                const aTeam = TEAM_DATA[m.awayTeam.name] || { name: m.awayTeam.name, flag: "🏳️", code: m.awayTeam.tla };

                batch.set(matchesRef.doc(matchId), {
                    status: m.status,
                    homeTeam: hTeam.name, homeTeamCode: hTeam.code || "TBD", homeTeamFlag: hTeam.flag,
                    awayTeam: aTeam.name, awayTeamCode: aTeam.code || "TBD", awayTeamFlag: aTeam.flag,
                    homeScore: m.score?.fullTime?.home ?? null,
                    awayScore: m.score?.fullTime?.away ?? null,
                    championshipId: "LIBERTADORES", matchOrder, phase: internalPhase,
                    group: m.group || `Rodada ${m.matchday}`,
                    matchDateMillis: Date.parse(m.utcDate),
                    lastSync: admin.firestore.FieldValue.serverTimestamp()
                }, { merge: true });
            }
            await batch.commit();
        }
    } catch (e) {
        logger.error("Erro na sincronização:", e.message);
    }
});
