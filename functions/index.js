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

        // 1. GARANTIR PLACEHOLDERS DO MATA-MATA (LIBERTADORES)
        const createKnockout = async (phase, count, dates, prefix) => {
            for (let i = 1; i <= count; i++) {
                const idaId = `CLI-2026-${prefix}${i}-L1`;
                const voltaId = `CLI-2026-${prefix}${i}-L2`;

                const baseData = {
                    homeTeam: `Vencedor ${prefix}${i} (Ida)`,
                    awayTeam: `Vencedor ${prefix}${i} (Volta)`,
                    homeTeamCode: "TBD", awayTeamCode: "TBD",
                    homeTeamFlag: "🏳️", awayTeamFlag: "🏳️",
                    championshipId: "LIBERTADORES",
                    phase: phase,
                    matchOrder: i,
                    status: "SCHEDULED"
                };

                const docIda = await matchesRef.doc(idaId).get();
                if (!docIda.exists) {
                    await matchesRef.doc(idaId).set({ ...baseData, matchDateMillis: dates[i-1] }, { merge: true });
                }
                const docVolta = await matchesRef.doc(voltaId).get();
                if (!docVolta.exists) {
                    await matchesRef.doc(voltaId).set({
                        ...baseData,
                        homeTeam: baseData.awayTeam,
                        awayTeam: baseData.homeTeam,
                        matchDateMillis: dates[i-1] + (7 * 24 * 3600000)
                    }, { merge: true });
                }
            }
        };

        // Quartas (Setembro 2026)
        await createKnockout("QUARTERFINALS", 4, [1788825600000, 1788912000000, 1788998400000, 1789084800000], "QF");
        // Semis (Outubro 2026)
        await createKnockout("SEMIFINALS", 2, [1791244800000, 1791331200000], "SF");
        // Final (Novembro 2026)
        const finalId = "CLI-2026-FINAL";
        const docFinal = await matchesRef.doc(finalId).get();
        if (!docFinal.exists) {
            await matchesRef.doc(finalId).set({
                homeTeam: "Finalista 1", awayTeam: "Finalista 2", homeTeamCode: "TBD", awayTeamCode: "TBD",
                homeTeamFlag: "🏳️", awayTeamFlag: "🏳️", matchDateMillis: 1793659200000,
                phase: "FINAL", championshipId: "LIBERTADORES", matchOrder: 1, status: "SCHEDULED"
            }, { merge: true });
        }

        // 2. APLICAR FIXES MANUAIS
        const manualFixes = {
            'KO-QF-1': { homeTeam: "França", homeTeamCode: "FRA", homeTeamFlag: "🇫🇷", awayTeam: "Marrocos", awayTeamCode: "MAR", awayTeamFlag: "🇲🇦", homeScore: 2, awayScore: 0, status: 'FINISHED', isManual: true, phase: 'QUARTERFINALS', group: 'Quartas', matchDateMillis: 1783627200000 },
            'KO-QF-2': { homeTeam: "Espanha", homeTeamCode: "ESP", homeTeamFlag: "🇪🇸", awayTeam: "Bélgica", awayTeamCode: "BEL", awayTeamFlag: "🇧🇪", homeScore: 2, awayScore: 1, status: 'FINISHED', isManual: true, phase: 'QUARTERFINALS', group: 'Quartas', matchDateMillis: 1783710000000 },
            'KO-QF-3': { homeTeam: "Noruega", homeTeamCode: "NOR", homeTeamFlag: "🇳🇴", awayTeam: "Inglaterra", awayTeamCode: "ENG", awayTeamFlag: "🇬🇧", homeScore: 1, awayScore: 2, status: 'FINISHED', isManual: true, phase: 'QUARTERFINALS', group: 'Quartas', matchDateMillis: 1783803600000 },
            'KO-QF-4': { homeTeam: "Argentina", homeTeamCode: "ARG", homeTeamFlag: "🇦🇷", awayTeam: "Suíça", awayTeamCode: "SUI", awayTeamFlag: "🇨🇭", homeScore: 3, awayScore: 1, status: 'FINISHED', isManual: true, phase: 'QUARTERFINALS', group: 'Quartas', matchDateMillis: 1783818000000 },
            'KO-SF-1': { homeTeam: "França", homeTeamCode: "FRA", homeTeamFlag: "🇫🇷", awayTeam: "Espanha", awayTeamCode: "ESP", awayTeamFlag: "🇪🇸", homeScore: 0, awayScore: 2, status: 'FINISHED', isManual: true, phase: 'SEMIFINALS', group: 'Semifinal', matchDateMillis: 1784055600000 },
            'KO-SF-2': { homeTeam: "Inglaterra", homeTeamCode: "ENG", homeTeamFlag: "🏴󠁧󠁢󠁥󠁮󠁧󠁿", awayTeam: "Argentina", awayTeamCode: "ARG", awayTeamFlag: "🇦🇷", homeScore: 1, awayScore: 2, status: 'FINISHED', isManual: true, phase: 'SEMIFINALS', group: 'Semifinal', matchDateMillis: 1784142000000 },
            'KO-SF-3': { homeTeam: "França", homeTeamCode: "FRA", homeTeamFlag: "🇫🇷", awayTeam: "Inglaterra", awayTeamCode: "ENG", awayTeamFlag: "🏴󠁧󠁢󠁥󠁮󠁧󠁿", isManual: false, phase: 'THIRD_PLACE', group: '3º Lugar', matchDateMillis: 1784408400000 },
            'KO-FINAL': { homeTeam: "Espanha", homeTeamCode: "ESP", homeTeamFlag: "🇪🇸", awayTeam: "Argentina", awayTeamCode: "ARG", awayTeamFlag: "🇦🇷", isManual: false, phase: 'FINAL', group: 'Final', matchDateMillis: 1784487600000 },
            'KO-16-1': { homeTeam: "Paraguai", homeTeamCode: "PAR", homeTeamFlag: "🇵🇾", awayTeam: "França", awayTeamCode: "FRA", awayTeamFlag: "🇫🇷", homeScore: 0, awayScore: 1, status: 'FINISHED', isManual: true, phase: 'ROUND_OF_16', group: 'Oitavas' },
            'KO-16-2': { homeTeam: "Canadá", homeTeamCode: "CAN", homeTeamFlag: "🇨🇦", awayTeam: "Marrocos", awayTeamCode: "MAR", awayTeamFlag: "🇲🇦", homeScore: 0, awayScore: 3, status: 'FINISHED', isManual: true, phase: 'ROUND_OF_16', group: 'Oitavas' },
            'KO-16-7': { homeTeam: "Argentina", homeTeamCode: "ARG", homeTeamFlag: "🇦🇷", awayTeam: "Egito", awayTeamCode: "EGY", awayTeamFlag: "🇪🇬", homeScore: 3, awayScore: 2, status: 'FINISHED', isManual: true, phase: 'ROUND_OF_16', group: 'Oitavas' },
            'KO-16-8': { homeTeam: "Suíça", homeTeamCode: "SUI", homeTeamFlag: "🇨🇭", awayTeam: "Colômbia", awayTeamCode: "COL", awayTeamFlag: "🇨🇴", homeScore: 0, awayScore: 0, status: 'FINISHED', isManual: true, phase: 'ROUND_OF_16', group: 'Oitavas' },
        };

        for (const id in manualFixes) {
            await matchesRef.doc(id).set(manualFixes[id], { merge: true });
        }

        // 3. BRASILEIRAO SYNC
        const resBSA = await axios.get("https://api.football-data.org/v4/competitions/BSA/matches", {
            headers: { 'X-Auth-Token': API_KEY },
            timeout: 15000
        }).catch(() => null);

        if (resBSA && resBSA.data && resBSA.data.matches) {
            const batch = db.batch();
            for (const m of resBSA.data.matches) {
                const matchId = `BSA-2026-R${m.matchday}-${m.id}`;
                const s = m.score;
                const hScore = s?.fullTime?.home ?? s?.regularTime?.home;
                const aScore = s?.fullTime?.away ?? s?.regularTime?.away;
                batch.set(matchesRef.doc(matchId), {
                    status: m.status,
                    homeScore: hScore !== undefined ? hScore : null,
                    awayScore: aScore !== undefined ? aScore : null,
                    championshipId: "BRASILEIRAO",
                    lastSync: admin.firestore.FieldValue.serverTimestamp()
                }, { merge: true });
            }
            await batch.commit();
        }

        // 4. LIBERTADORES SYNC
        const resCLI = await axios.get("https://api.football-data.org/v4/competitions/CLI/matches", {
            headers: { 'X-Auth-Token': API_KEY },
            timeout: 15000
        }).catch(() => null);

        if (resCLI && resCLI.data && resCLI.data.matches) {
            const batch = db.batch();
            for (const m of resCLI.data.matches) {
                const isVolta = m.matchday === 2 || m.stage.includes("LEG2");
                const legSuffix = isVolta ? "-L2" : "-L1";
                const matchId = `CLI-2026-M${m.id}${legSuffix}`;

                const s = m.score;
                const hScore = s?.fullTime?.home ?? s?.regularTime?.home;
                const aScore = s?.fullTime?.away ?? s?.regularTime?.away;

                let hName = m.homeTeam.name || "";
                let aName = m.awayTeam.name || "";
                if (m.stage !== "GROUP_STAGE") {
                    if (hName === "" || hName.includes("Winner") || hName.includes("To Be Determined")) hName = "Vencedor Oitavas";
                    if (aName === "" || aName.includes("Winner") || aName.includes("To Be Determined")) aName = "Vencedor Oitavas";
                }

                const hTeam = TEAM_DATA[m.homeTeam.name] || { name: hName, flag: "🏳️", code: m.homeTeam.tla || "TBD" };
                const aTeam = TEAM_DATA[m.awayTeam.name] || { name: aName, flag: "🏳️", code: m.awayTeam.tla || "TBD" };

                batch.set(matchesRef.doc(matchId), {
                    status: m.status,
                    homeTeam: hTeam.name, homeTeamCode: hTeam.code, homeTeamFlag: hTeam.flag,
                    awayTeam: aTeam.name, awayTeamCode: aTeam.code, awayTeamFlag: aTeam.flag,
                    homeScore: hScore !== undefined ? hScore : null,
                    awayScore: aScore !== undefined ? aScore : null,
                    championshipId: "LIBERTADORES",
                    phase: mapPhase(m.stage),
                    group: m.group || (m.matchday ? `Rodada ${m.matchday}` : m.stage),
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

exports.cleanupDeletedBoloes = onSchedule({
    schedule: "0 3 * * *",
    timeZone: "America/Sao_Paulo",
    region: "southamerica-east1",
    memory: "256MiB"
}, async (event) => {
    try {
        const sevenDaysAgo = Date.now() - (7 * 24 * 60 * 60 * 1000);
        const snapshot = await db.collection("boloes").where("deletedAtMillis", "<=", sevenDaysAgo).get();
        for (const doc of snapshot.docs) {
            const predsSnap = await doc.ref.collection("predictions").get();
            const batch = db.batch();
            predsSnap.forEach(pDoc => batch.delete(pDoc.ref));
            await batch.commit();
            const invitesSnap = await db.collection("invitations").where("bolaoId", "==", doc.id).get();
            const inviteBatch = db.batch();
            invitesSnap.forEach(iDoc => inviteBatch.delete(iDoc.ref));
            await inviteBatch.commit();
            await doc.ref.delete();
        }
    } catch (e) {
        logger.error("Erro na limpeza de bolões:", e.message);
    }
});
