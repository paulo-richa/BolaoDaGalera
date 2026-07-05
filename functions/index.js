const { onSchedule } = require("firebase-functions/v2/scheduler");
const { logger } = require("firebase-functions");
const admin = require("firebase-admin");
const axios = require("axios");

admin.initializeApp();
const db = admin.firestore();
const API_KEY = "***REMOVED_SECRET***";

// Dicionário para garantir nomes em Português e Bandeiras corretas
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

        const manualFixes = {
            'KO-16-1': { homeTeam: "Canadá", homeTeamFlag: "🇨🇦", awayTeam: "Marrocos", awayTeamFlag: "🇲🇦", homeScore: 0, awayScore: 1, status: 'FINISHED' },
            'KO-16-2': { homeTeam: "Paraguai", homeTeamFlag: "🇵🇾", awayTeam: "França", awayTeamFlag: "🇫🇷", homeScore: 0, awayScore: 3, status: 'FINISHED' },
            'KO-32-2': { homeScore: 3, awayScore: 0, status: 'FINISHED' },
            'KO-32-3': { homeScore: 0, awayScore: 1, status: 'FINISHED' }
        };

        for (const [id, data] of Object.entries(manualFixes)) {
            await matchesRef.doc(id).set({ ...data, isManual: true }, { merge: true });
        }

        const res = await axios.get("https://api.football-data.org/v4/competitions/WC/matches", {
            headers: { 'X-Auth-Token': API_KEY },
            timeout: 10000
        });

        const matches = res.data.matches || [];
        const apiToInternal = {
            "537415": "KO-32-1", "537416": "KO-32-2", "537417": "KO-32-3", "537418": "KO-32-4",
            "537419": "KO-32-5", "537420": "KO-32-6", "537421": "KO-32-7", "537422": "KO-32-8",
            "537423": "KO-32-9", "537424": "KO-32-10", "537425": "KO-32-11", "537426": "KO-32-12",
            "537427": "KO-32-13", "537428": "KO-32-14", "537429": "KO-32-15", "537430": "KO-32-16",
            "537379": "KO-16-3", "537380": "KO-16-4", "537377": "KO-16-5", "537378": "KO-16-6",
            "537381": "KO-16-7", "537382": "KO-16-8", "537383": "KO-QF-1", "537384": "KO-QF-2",
            "537385": "KO-QF-3", "537386": "KO-QF-4", "537387": "KO-SF-1", "537388": "KO-SF-2",
            "537389": "KO-SF-3", "537390": "KO-FINAL"
        };

        for (const m of matches) {
            const id = apiToInternal[m.id.toString()];
            if (!id || manualFixes[id]) continue;

            const docRef = matchesRef.doc(id);
            const doc = await docRef.get();

            if (doc.exists && !doc.data().isManual) {
                const updateData = {};

                // 1. Placar (Ignorando Pênaltis)
                const s = m.score;
                if (s.duration === "PENALTY_SHOOTOUT" || s.penalties) {
                    updateData.homeScore = (s.regularTime?.home ?? 0) + (s.extraTime?.home ?? 0);
                    updateData.awayScore = (s.regularTime?.away ?? 0) + (s.extraTime?.away ?? 0);
                } else {
                    updateData.homeScore = s.fullTime?.home ?? 0;
                    updateData.awayScore = s.fullTime?.away ?? 0;
                }
                updateData.status = m.status;

                // 2. Horário Oficial (Converte UTC da API para Millis)
                if (m.utcDate) {
                    updateData.matchDateMillis = Date.parse(m.utcDate);
                }

                // 3. Promoção de Times (Atualiza TBD para nomes reais)
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
    } catch (e) {
        logger.error("Erro na sincronização:", e.message);
    }
});
