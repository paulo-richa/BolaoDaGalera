const { onSchedule } = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");
const axios = require("axios");
const { logger } = require("firebase-functions");

admin.initializeApp();
const db = admin.firestore();

const API_KEY = "***REMOVED_SECRET***";

// Dicionário completo para tradução e busca de códigos
const NAME_TO_CODE = {
    "Mexico": "MEX", "México": "MEX", "South Africa": "RSA", "África do Sul": "RSA",
    "Korea Republic": "KOR", "South Korea": "KOR", "Coreia do Sul": "KOR",
    "Czech Republic": "CZE", "Czechia": "CZE", "República Tcheca": "CZE",
    "Canada": "CAN", "Canadá": "CAN", "Switzerland": "SUI", "Suíça": "SUI",
    "Qatar": "QAT", "Catar": "QAT", "Bosnia-Herzegovina": "BIH", "Bósnia": "BIH",
    "Brazil": "BRA", "Brasil": "BRA", "Morocco": "MAR", "Marrocos": "MAR",
    "Scotland": "SCO", "Escócia": "SCO", "Haiti": "HAI",
    "United States": "USA", "USA": "USA", "EUA": "USA", "Paraguay": "PAR", "Paraguai": "PAR",
    "Australia": "AUS", "Austrália": "AUS", "Turkey": "TUR", "Turquia": "TUR", "Türkiye": "TUR",
    "Germany": "GER", "Alemanha": "GER", "Ecuador": "ECU", "Equador": "ECU",
    "Ivory Coast": "CIV", "Costa do Marfim": "CIV", "Curaçao": "CUW",
    "Netherlands": "NED", "Holanda": "NED", "Japan": "JPN", "Japão": "JPN",
    "Tunisia": "TUN", "Tunísia": "TUN", "Sweden": "SWE", "Suécia": "SWE",
    "Belgium": "BEL", "Bélgica": "BEL", "Egypt": "EGY", "Egito": "EGY",
    "Iran": "IRN", "Irã": "IRN", "New Zealand": "NZL", "Nova Zelândia": "NZL",
    "Spain": "ESP", "Espanha": "ESP", "Uruguay": "URU", "Uruguai": "URU",
    "Saudi Arabia": "KSA", "Arábia Saudita": "KSA", "Cape Verde": "CPV", "Cabo Verde": "CPV",
    "France": "FRA", "França": "FRA", "Senegal": "SEN", "Norway": "NOR", "Noruega": "NOR",
    "Iraq": "IRQ", "Iraque": "IRQ", "Argentina": "ARG", "Austria": "AUT", "Áustria": "AUT",
    "Algeria": "ALG", "Argélia": "ALG", "Jordan": "JOR", "Jordânia": "JOR",
    "Portugal": "POR", "Colombia": "COL", "Colômbia": "COL", "Uzbekistan": "UZB", "Uzbequistão": "UZB",
    "DR Congo": "COD", "Rep. Congo": "COD", "England": "ENG", "Inglaterra": "ENG",
    "Croatia": "CRO", "Croácia": "CRO", "Panama": "PAN", "Panamá": "PAN", "Ghana": "GHA", "Gana": "GHA"
};

// Mapeamento de nomes PT e Bandeiras
const TEAM_INFO = {
    "MEX": ["México", "🇲🇽"], "RSA": ["África do Sul", "🇿🇦"], "KOR": ["Coreia do Sul", "🇰🇷"], "CZE": ["Rep. Tcheca", "🇨🇿"],
    "CAN": ["Canadá", "🇨🇦"], "BIH": ["Bósnia", "🇧🇦"], "QAT": ["Catar", "🇶🇦"], "SUI": ["Suíça", "🇨🇭"],
    "BRA": ["Brasil", "🇧🇷"], "MAR": ["Marrocos", "🇲🇦"], "HAI": ["Haiti", "🇭🇹"], "SCO": ["Escócia", "🏴󠁧󠁢󠁳󠁣󠁴󠁿"],
    "USA": ["EUA", "🇺🇸"], "PAR": ["Paraguai", "🇵🇾"], "AUS": ["Austrália", "🇦🇺"], "TUR": ["Turquia", "🇹🇷"],
    "GER": ["Alemanha", "🇩🇪"], "CUW": ["Curaçao", "🇨🇼"], "CIV": ["Costa do Marfim", "🇨🇮"], "ECU": ["Equador", "🇪🇨"],
    "NED": ["Holanda", "🇳🇱"], "JPN": ["Japão", "🇯🇵"], "SWE": ["Suécia", "🇸🇪"], "TUN": ["Tunísia", "🇹🇳"],
    "BEL": ["Bélgica", "🇧🇪"], "EGY": ["Egito", "🇪🇬"], "IRN": ["Irã", "🇮🇷"], "NZL": ["Nova Zelândia", "🇳🇿"],
    "ESP": ["Espanha", "🇪🇸"], "CPV": ["Cabo Verde", "🇨🇻"], "KSA": ["Arábia Saudita", "🇸🇦"], "URU": ["Uruguai", "🇺🇾"],
    "FRA": ["França", "🇫🇷"], "SEN": ["Senegal", "🇸🇳"], "IRQ": ["Iraque", "🇮🇶"], "NOR": ["Noruega", "🇳🇴"],
    "ARG": ["Argentina", "🇦🇷"], "ALG": ["Argélia", "🇩🇿"], "AUT": ["Áustria", "🇦🇹"], "JOR": ["Jordânia", "🇯🇴"],
    "POR": ["Portugal", "🇵🇹"], "COD": ["RD Congo", "🇨🇩"], "UZB": ["Uzbequistão", "🇺🇿"], "COL": ["Colômbia", "🇨🇴"],
    "ENG": ["Inglaterra", "🏴󠁧󠁢󠁥󠁮󠁧󠁿"], "CRO": ["Croácia", "🇭🇷"], "GHA": ["Gana", "🇬🇭"], "PAN": ["Panamá", "🇵🇦"]
};

exports.syncScores = onSchedule({
    schedule: "every 5 minutes",
    timeZone: "America/Sao_Paulo",
    region: "southamerica-east1",
    memory: "256MiB"
}, async (event) => {
    try {
        logger.info("Iniciando Sincronização Automática...");

        const response = await axios.get("https://api.football-data.org/v4/competitions/WC/matches", {
            headers: { 'X-Auth-Token': API_KEY }
        });
        const matches = response.data.matches || [];

        let updatedCount = 0;
        for (const m of matches) {
            const wasUpdated = await updateMatchInFirestore(m);
            if (wasUpdated) updatedCount++;
        }
        logger.info(`Fim do ciclo. ${updatedCount} jogos afetados.`);
    } catch (error) {
        logger.error("Erro na sincronização:", error.message);
    }
});

async function updateMatchInFirestore(apiMatch) {
    const hCode = apiMatch.homeTeam?.tla || NAME_TO_CODE[apiMatch.homeTeam?.name];
    const aCode = apiMatch.awayTeam?.tla || NAME_TO_CODE[apiMatch.awayTeam?.name];

    if (!hCode || !aCode) return false;

    const matchesRef = db.collection('matches');
    let snapshot = await matchesRef.where('homeTeamCode', '==', hCode).where('awayTeamCode', '==', aCode).get();
    let isInverted = false;

    if (snapshot.empty) {
        snapshot = await matchesRef.where('homeTeamCode', '==', aCode).where('awayTeamCode', '==', hCode).get();
        isInverted = true;
    }

    if (snapshot.empty) {
        // Se for mata-mata, tentamos achar pela fase/ordem se os times ainda forem genéricos no DB
        if (apiMatch.stage !== 'GROUP_STAGE') {
            return await updateKnockoutByStage(apiMatch, hCode, aCode);
        }
        return false;
    }

    const matchDoc = snapshot.docs[0];
    const matchData = matchDoc.data();
    if (matchData.isManual) return false;

    const apiHomeScore = isInverted ? apiMatch.score?.fullTime?.away : apiMatch.score?.fullTime?.home;
    const apiAwayScore = isInverted ? apiMatch.score?.fullTime?.home : apiMatch.score?.fullTime?.away;

    let updateObj = {};
    let changed = false;

    // 1. Atualizar Placar
    if (apiHomeScore !== null && apiAwayScore !== null) {
        if (apiHomeScore !== matchData.homeScore || apiAwayScore !== matchData.awayScore) {
            updateObj.homeScore = apiHomeScore;
            updateObj.awayScore = apiAwayScore;
            changed = true;
        }
    }

    // 2. Atualizar Status e Data
    if (apiMatch.status !== matchData.status) {
        updateObj.status = apiMatch.status;
        changed = true;
    }

    const apiDateMillis = new Date(apiMatch.utcDate).getTime();
    if (apiDateMillis !== matchData.matchDateMillis) {
        updateObj.matchDateMillis = apiDateMillis;
        changed = true;
    }

    if (changed) {
        await matchDoc.ref.update(updateObj);
        return true;
    }
    return false;
}

async function updateKnockoutByStage(apiMatch, hCode, aCode) {
    const stageMap = {
        'LAST_32': 'ROUND_OF_32',
        'LAST_16': 'ROUND_OF_16',
        'QUARTER_FINALS': 'QUARTERFINALS',
        'SEMI_FINALS': 'SEMIFINALS',
        'THIRD_PLACE': 'THIRD_PLACE',
        'FINAL': 'FINAL'
    };

    const phase = stageMap[apiMatch.stage];
    if (!phase) return false;

    const matchesRef = db.collection('matches');
    const snap = await matchesRef.where('phase', '==', phase).get();
    const localMatchesList = snap.docs.map(d => ({id: d.id, ...d.data()}));

    // 1. Tentar encontrar por time (Sigla) - Mais seguro para mata-mata definido
    const matchByTeam = localMatchesList.find(m =>
        (m.homeTeamCode === hCode || m.awayTeamCode === hCode) && m.homeTeamCode !== "TBD"
    );

    let target = matchByTeam;

    // 2. Se não achou por time, tenta por índice cronológico (Para o primeiro preenchimento)
    if (!target) {
        const localMatchesSorted = [...localMatchesList].sort((a, b) => a.matchDateMillis - b.matchDateMillis);
        const apiMatchesInStage = await getApiMatchesInStage(apiMatch.stage); // Já ordenado por data
        const index = apiMatchesInStage.findIndex(m => m.id === apiMatch.id);
        if (index !== -1) target = localMatchesSorted[index];
    }

    if (target) {
        if (target.isManual) return false;

        const hInfo = TEAM_INFO[hCode];
        const aInfo = TEAM_INFO[aCode];

        await matchesRef.doc(target.id).update({
            homeTeam: hInfo ? hInfo[0] : apiMatch.homeTeam.name,
            homeTeamCode: hCode,
            homeTeamFlag: hInfo ? hInfo[1] : "🏳️",
            awayTeam: aInfo ? aInfo[0] : apiMatch.awayTeam.name,
            awayTeamCode: aCode,
            awayTeamFlag: aInfo ? aInfo[1] : "🏳️",
            homeScore: apiMatch.score?.fullTime?.home,
            awayScore: apiMatch.score?.fullTime?.away,
            status: apiMatch.status,
            matchDateMillis: new Date(apiMatch.utcDate).getTime()
        });
        return true;
    }
    return false;
}

async function getApiMatchesInStage(stage) {
    const response = await axios.get("https://api.football-data.org/v4/competitions/WC/matches", {
        headers: { 'X-Auth-Token': API_KEY }
    });
    return response.data.matches
        .filter(m => m.stage === stage)
        .sort((a, b) => a.id - b.id);
}
