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
    "Ivory Coast": { name: "Costa do Marfim", flag: "🇨🇮", code: "CIV" }
};

exports.syncScores = onSchedule({
    schedule: "every 1 minutes",
    timeZone: "America/Sao_Paulo",
    region: "southamerica-east1",
    memory: "256MiB",
    timeoutSeconds: 120
}, async (event) => {
    try {
        const matchesRef = db.collection('matches');

        // 1. GARANTIR TIMES E ESTRUTURA (isManual: false permite placar ao vivo)
        const manualFixes = {
            'KO-QF-1': { homeTeam: "França", homeTeamCode: "FRA", homeTeamFlag: "🇫🇷", awayTeam: "Marrocos", awayTeamCode: "MAR", awayTeamFlag: "🇲🇦", homeScore: 2, awayScore: 0, status: 'FINISHED', isManual: true, phase: 'QUARTERFINALS', group: 'Quartas', matchDateMillis: 1783627200000 },
            'KO-QF-2': { homeTeam: "Espanha", homeTeamCode: "ESP", homeTeamFlag: "🇪🇸", awayTeam: "Bélgica", awayTeamCode: "BEL", awayTeamFlag: "🇧🇪", isManual: false, phase: 'QUARTERFINALS', group: 'Quartas', matchDateMillis: 1783710000000 },
            'KO-QF-3': { homeTeam: "Noruega", homeTeamCode: "NOR", homeTeamFlag: "🇳🇴", awayTeam: "Inglaterra", awayTeamCode: "ENG", awayTeamFlag: "🏴󠁧󠁢󠁥󠁮󠁧󠁿", isManual: false, phase: 'QUARTERFINALS', group: 'Quartas', matchDateMillis: 1783803600000 },
            'KO-QF-4': { homeTeam: "Argentina", homeTeamCode: "ARG", homeTeamFlag: "🇦🇷", awayTeam: "Suíça", awayTeamCode: "SUI", awayTeamFlag: "🇨🇭", isManual: false, phase: 'QUARTERFINALS', group: 'Quartas', matchDateMillis: 1783818000000 },

            // Semifinais, 3º Lugar e Final (Preparados para Live Update)
            'KO-SF-1': { homeTeam: "Vencedor QF1", awayTeam: "Vencedor QF2", isManual: false, phase: 'SEMIFINALS', group: 'Semifinal', matchDateMillis: 1784055600000 },
            'KO-SF-2': { homeTeam: "Vencedor QF3", awayTeam: "Vencedor QF4", isManual: false, phase: 'SEMIFINALS', group: 'Semifinal', matchDateMillis: 1784142000000 },
            'KO-SF-3': { homeTeam: "Perdedor SF1", awayTeam: "Perdedor SF2", isManual: false, phase: 'THIRD_PLACE', group: '3º Lugar', matchDateMillis: 1784408400000 },
            'KO-FINAL': { homeTeam: "Vencedor SF1", awayTeam: "Vencedor SF2", isManual: false, phase: 'FINAL', group: 'Final', matchDateMillis: 1784487600000 },

            'KO-16-1': { homeTeam: "Paraguai", homeTeamCode: "PAR", homeTeamFlag: "🇵🇾", awayTeam: "França", awayTeamCode: "FRA", awayTeamFlag: "🇫🇷", homeScore: 0, awayScore: 1, status: 'FINISHED', isManual: true, phase: 'ROUND_OF_16', group: 'Oitavas' },
            'KO-16-2': { homeTeam: "Canadá", homeTeamCode: "CAN", homeTeamFlag: "🇨🇦", awayTeam: "Marrocos", awayTeamCode: "MAR", awayTeamFlag: "🇲🇦", homeScore: 0, awayScore: 3, status: 'FINISHED', isManual: true, phase: 'ROUND_OF_16', group: 'Oitavas' },
            'KO-16-7': { homeTeam: "Argentina", homeTeamCode: "ARG", homeTeamFlag: "🇦🇷", awayTeam: "Egito", awayTeamCode: "EGY", awayTeamFlag: "🇪🇬", homeScore: 3, awayScore: 2, status: 'FINISHED', isManual: true, phase: 'ROUND_OF_16', group: 'Oitavas' },
            'KO-16-8': { homeTeam: "Suíça", homeTeamCode: "SUI", homeTeamFlag: "🇨🇭", awayTeam: "Colômbia", awayTeamCode: "COL", awayTeamFlag: "🇨🇴", homeScore: 0, awayScore: 0, status: 'FINISHED', isManual: true, phase: 'ROUND_OF_16', group: 'Oitavas' }
        };

        for (const id in manualFixes) {
            await matchesRef.doc(id).set(manualFixes[id], { merge: true });
        }

        // 2. SINCRONIZAÇÃO COM API
        const res = await axios.get("https://api.football-data.org/v4/competitions/WC/matches", {
            headers: { 'X-Auth-Token': API_KEY },
            timeout: 10000
        }).catch(() => null);

        if (res && res.data) {
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

            for (const m of res.data.matches || []) {
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

                    // Se a API trouxer times reais, vamos atualizar também (exceto se bloqueado)
                    if (m.homeTeam && m.homeTeam.name && m.homeTeam.name !== "TBD") {
                        const homeInfo = TEAM_DATA[m.homeTeam.name];
                        if (homeInfo) {
                            updateData.homeTeam = homeInfo.name;
                            updateData.homeTeamFlag = homeInfo.flag;
                            updateData.homeTeamCode = homeInfo.code;
                        }
                    }
                    if (m.awayTeam && m.awayTeam.name && m.awayTeam.name !== "TBD") {
                        const awayInfo = TEAM_DATA[m.awayTeam.name];
                        if (awayInfo) {
                            updateData.awayTeam = awayInfo.name;
                            updateData.awayTeamFlag = awayInfo.flag;
                            updateData.awayTeamCode = awayInfo.code;
                        }
                    }

                    await docRef.update(updateData);
                }
            }
        }
    } catch (e) {
        logger.error("Erro na sincronização:", e.message);
    }
});
