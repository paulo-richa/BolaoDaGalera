const { onSchedule } = require("firebase-functions/v2/scheduler");
const { logger } = require("firebase-functions");
const admin = require("firebase-admin");
const axios = require("axios");

admin.initializeApp();
const db = admin.firestore();
const API_KEY = "***REMOVED_SECRET***";

const TEAM_DATA = {
    "Argentina": { name: "Argentina", flag: "🇦🇷", code: "ARG" },
    "Brazil": { name: "Brasil", flag: "🇧🇷", code: "BRA" },
    "France": { name: "França", flag: "🇫🇷", code: "FRA" },
    "Germany": { name: "Alemanha", flag: "🇩🇪", code: "GER" },
    "Portugal": { name: "Portugal", flag: "🇵🇹", code: "POR" },
    "Spain": { name: "Espanha", flag: "🇪🇸", code: "ESP" },
    "England": { name: "Inglaterra", flag: "🏴󠁧󠁢󠁥󠁮󠁧󠁿", code: "ENG" },
    "Netherlands": { name: "Holanda", flag: "🇳🇱", code: "NED" },
    "Morocco": { name: "Marrocos", flag: "🇲🇦", code: "MAR" },
    "Switzerland": { name: "Suíça", flag: "🇨🇭", code: "SUI" },
    "Croatia": { name: "Croácia", flag: "🇭🇷", code: "CRO" },
    "Belgium": { name: "Bélgica", flag: "🇧🇪", code: "BEL" },
    "Mexico": { name: "México", flag: "🇲🇽", code: "MEX" },
    "Sweden": { name: "Suécia", flag: "🇸🇪", code: "SWE" },
    "Norway": { name: "Noruega", flag: "🇳🇴", code: "NOR" },
    "Paraguay": { name: "Paraguai", flag: "🇵🇾", code: "PAR" },
    "Canada": { name: "Canadá", flag: "🇨🇦", code: "CAN" },
    "South Africa": { name: "África do Sul", flag: "🇿🇦", code: "RSA" },
    "Ecuador": { name: "Equador", flag: "🇪🇨", code: "ECU" },
    "Senegal": { name: "Senegal", flag: "🇸🇳", code: "SEN" },
    "Japan": { name: "Japão", flag: "🇯🇵", code: "JPN" },
    "USA": { name: "EUA", flag: "🇺🇸", code: "USA" },
    "Australia": { name: "Austrália", flag: "🇦🇺", code: "AUS" },
    "Egypt": { name: "Egito", flag: "🇪🇬", code: "EGY" },
    "Colombia": { name: "Colômbia", flag: "🇨🇴", code: "COL" },
    "Ghana": { name: "Gana", flag: "🇬🇭", code: "GHA" },
    "Algeria": { name: "Argélia", flag: "🇩🇿", code: "ALG" },
    "Austria": { name: "Áustria", flag: "🇦🇹", code: "AUT" },
    "Bosnia-Herzegovina": { name: "Bósnia", flag: "🇧🇦", code: "BIH" },
    "Ivory Coast": { name: "Costa do Marfim", flag: "🇨🇮", code: "CIV" },
    "England": { name: "Inglaterra", flag: "🏴󠁧󠁢󠁥󠁮󠁧󠁿", code: "ENG" },
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

        // 1. APLICAR FIXES MANUAIS
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

        // 2. WC SYNC (Legado)
        const resWC = await axios.get("https://api.football-data.org/v4/competitions/WC/matches", {
            headers: { 'X-Auth-Token': API_KEY },
            timeout: 10000
        }).catch(() => null);

        if (resWC && resWC.data) {
            const apiToInternal = {
                "537415": "KO-32-1", "537416": "KO-32-2", "537417": "KO-32-3", "537418": "KO-32-4",
                "537419": "KO-32-5", "537420": "KO-32-6", "537421": "KO-32-7", "537422": "KO-32-8",
                "537423": "KO-32-9", "537424": "KO-32-10", "537425": "KO-32-11", "537426": "KO-32-12",
                "537427": "KO-32-13", "537428": "KO-32-14", "537429": "KO-32-15", "537430": "KO-32-16",
                "537375": "KO-16-1", "537376": "KO-16-2", "537379": "KO-16-3", "537380": "KO-16-4",
                "537377": "KO-16-5", "537378": "KO-16-6", "537381": "KO-16-7", "537382": "KO-16-8",
                "537383": "KO-QF-1", "537384": "KO-QF-2", "537385": "KO-QF-3", "537386": "KO-QF-4",
                "537387": "KO-SF-1", "537388": "KO-SF-2", "537389": "KO-SF-3", "537390": "KO-FINAL"
            };

            for (const m of resWC.data.matches || []) {
                const id = apiToInternal[m.id.toString()];
                if (!id) continue;
                const docRef = matchesRef.doc(id);
                const doc = await docRef.get();
                if (doc.exists && !doc.data().isManual) {
                    const updateData = { status: m.status };
                    const s = m.score;
                    if (s.duration === "PENALTY_SHOOTOUT" || s.penalties) {
                        updateData.homeScore = (s.regularTime?.home ?? 0) + (s.extraTime?.home ?? 0);
                        updateData.awayScore = (s.regularTime?.away ?? 0) + (s.extraTime?.away ?? 0);
                    } else {
                        updateData.homeScore = s.fullTime?.home ?? 0;
                        updateData.awayScore = s.fullTime?.away ?? 0;
                    }
                    if (m.utcDate) updateData.matchDateMillis = Date.parse(m.utcDate);
                    await docRef.update(updateData);
                }
            }
        }

        // 3. BRASILEIRÃO 2026 (Série A)
        const leagues = ["BSA"];
        for (const leagueCode of leagues) {
            const res = await axios.get(`https://api.football-data.org/v4/competitions/${leagueCode}/matches`, {
                headers: { 'X-Auth-Token': API_KEY },
                timeout: 15000
            }).catch(() => null);

            if (res && res.data && res.data.matches) {
                const batch = db.batch();
                let count = 0;

                const matchesSnap = await matchesRef.where('championshipId', '==', 'BRASILEIRAO').get();
                const existingDocs = {};
                matchesSnap.docs.forEach(d => existingDocs[d.id] = d.data());

                for (const m of res.data.matches) {
                    const matchId = `BSA-2026-R${m.matchday}-${m.id}`;

                    if (existingDocs[matchId] && existingDocs[matchId].isManual) continue;

                    const s = m.score;
                    const hScore = s?.fullTime?.home ?? s?.regularTime?.home;
                    const aScore = s?.fullTime?.away ?? s?.regularTime?.away;

                    const updateData = {
                        status: m.status,
                        homeScore: hScore !== undefined ? hScore : null,
                        awayScore: aScore !== undefined ? aScore : null,
                        championshipId: "BRASILEIRAO",
                        lastSync: admin.firestore.FieldValue.serverTimestamp()
                    };

                    batch.set(matchesRef.doc(matchId), updateData, { merge: true });
                    count++;
                }
                if (count > 0) await batch.commit();
            }
        }

        // 4. LIBERTADORES 2026 (CLI)
        const leaguesLib = ["CLI"];
        for (const leagueCode of leaguesLib) {
            const res = await axios.get(`https://api.football-data.org/v4/competitions/${leagueCode}/matches`, {
                headers: { 'X-Auth-Token': API_KEY },
                timeout: 15000
            }).catch(() => null);

            if (res && res.data && res.data.matches) {
                const batch = db.batch();
                let count = 0;

                const matchesSnap = await matchesRef.where('championshipId', '==', 'LIBERTADORES').get();
                const existingDocs = {};
                matchesSnap.docs.forEach(d => existingDocs[d.id] = d.data());

                for (const m of res.data.matches) {
                    const isVolta = m.matchday === 2 || m.stage.includes("LEG2");
                    const legSuffix = isVolta ? "-L2" : "-L1";

                    // Mantemos o ID original para evitar duplicados, mas adicionamos a ordem do GE
                    const matchId = `CLI-2026-M${m.id}${legSuffix}`;

                    const geOrderMap = {
                        '564456': 1, '564465': 1, // Estudiantes
                        '564462': 2, '564470': 2, // Rosario
                        '564460': 3, '564468': 3, // Cruzeiro
                        '564457': 4, '564464': 4, // Tolima
                        '564461': 5, '564469': 5, // Mirassol
                        '564459': 6, '564466': 6, // Palmeiras
                        '564458': 7, '564467': 7, // Platense
                        '564455': 8, '564463': 8  // Fluminense
                    };

                    if (existingDocs[matchId] && existingDocs[matchId].isManual) continue;

                    const s = m.score;
                    const hScore = s?.fullTime?.home ?? s?.regularTime?.home;
                    const aScore = s?.fullTime?.away ?? s?.regularTime?.away;

                    const hTeam = TEAM_DATA[m.homeTeam.name] || { name: m.homeTeam.name, flag: "🏳️", code: m.homeTeam.tla };
                    const aTeam = TEAM_DATA[m.awayTeam.name] || { name: m.awayTeam.name, flag: "🏳️", code: m.awayTeam.tla };

                    const updateData = {
                        status: m.status,
                        homeTeam: hTeam.name,
                        homeTeamCode: hTeam.code || m.homeTeam.tla || "TBD",
                        homeTeamFlag: hTeam.flag,
                        homeTeamCrest: m.homeTeam.crest,
                        awayTeam: aTeam.name,
                        awayTeamCode: aTeam.code || m.awayTeam.tla || "TBD",
                        awayTeamFlag: aTeam.flag,
                        awayTeamCrest: m.awayTeam.crest,
                        homeScore: hScore !== undefined ? hScore : null,
                        awayScore: aScore !== undefined ? aScore : null,
                        championshipId: "LIBERTADORES",
                        matchOrder: geOrderMap[m.id.toString()] || 99,
                        phase: mapPhase(m.stage),
                        group: m.group || (m.matchday ? `Rodada ${m.matchday}` : m.stage),
                        matchDateMillis: Date.parse(m.utcDate),
                        lastSync: admin.firestore.FieldValue.serverTimestamp()
                    };

                    // EXCLUSÃO DE DUPLICADOS: Se o ID for novo, tentamos limpar o antigo se existir
                    const oldId = `CLI-2026-R${m.matchday}-${m.id}`;
                    if (oldId !== matchId) {
                        batch.delete(matchesRef.doc(oldId));
                    }

                    batch.set(matchesRef.doc(matchId), updateData, { merge: true });
                    count++;
                }
                if (count > 0) await batch.commit();
            }
        }
    } catch (e) {
        logger.error("Erro na sincronização:", e.message);
    }
});
