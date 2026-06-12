const admin = require('firebase-admin');
const axios = require('axios');

// Configurações
const API_URL = "https://api.football-data.org/v4/competitions/WC/matches";
const API_KEY = "c366f1ec224c43a28100c97ea5aab282";
const OPENFOOTBALL_URL = "https://raw.githubusercontent.com/openfootball/worldcup.json/master/2026/worldcup.json";

// Mapeamento de Nomes (OpenFootball) -> Códigos TLA (Usados no App)
const NAME_TO_CODE = {
    "Mexico": "MEX", "South Africa": "RSA", "Korea Republic": "KOR", "South Korea": "KOR", "Czechia": "CZE", "Czech Republic": "CZE",
    "Canada": "CAN", "Switzerland": "SUI", "Qatar": "QAT", "Bosnia and Herzegovina": "BIH",
    "Brazil": "BRA", "Brasil": "BRA", "Morocco": "MAR", "Marrocos": "MAR", "Scotland": "SCO", "Escócia": "SCO", "Haiti": "HAI",
    "United States": "USA", "USA": "USA", "Australia": "AUS", "Austrália": "AUS", "Paraguay": "PAR", "Paraguai": "PAR", "Turkey": "TUR", "Turquia": "TUR",
    "Germany": "GER", "Alemanha": "GER", "Ecuador": "ECU", "Equador": "ECU", "Ivory Coast": "CIV", "Netherlands": "NED", "Holanda": "NED",
    "Japan": "JPN", "Japão": "JPN", "Tunisia": "TUN", "Tunísia": "TUN", "Sweden": "SWE", "Suécia": "SWE",
    "Belgium": "BEL", "Bélgica": "BEL", "Iran": "IRN", "Irã": "IRN", "Egypt": "EGY", "Egito": "EGY", "New Zealand": "NZL",
    "Spain": "ESP", "Espanha": "ESP", "Uruguay": "URU", "Uruguai": "URU", "Saudi Arabia": "KSA", "Arábia Saudita": "KSA",
    "France": "FRA", "França": "FRA", "Senegal": "SEN", "Norway": "NOR", "Noruega": "NOR",
    "Argentina": "ARG", "Austria": "AUT", "Áustria": "AUT", "Algeria": "ALG", "Argélia": "ALG",
    "Uzbekistan": "UZB", "Uzbequistão": "UZB"
};

if (!process.env.FIREBASE_SERVICE_ACCOUNT) {
    console.error("ERRO: FIREBASE_SERVICE_ACCOUNT não configurada.");
    process.exit(1);
}

const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function syncScores() {
    try {
        console.log("🚀 Iniciando Sincronização Centralizada...");
        await syncFromFootballData();
        await syncFromOpenFootball();
        console.log("🏁 Sincronização concluída com sucesso.");
    } catch (error) {
        console.error("❌ Erro fatal na sincronização:", error.message);
    }
}

async function syncFromFootballData() {
    console.log("⚽ [Football-Data] Buscando placares...");
    try {
        const response = await axios.get(API_URL, { headers: { 'X-Auth-Token': API_KEY } });
        for (const m of response.data.matches) {
            await updateMatchInFirestore(m.homeTeam?.tla, m.awayTeam?.tla, {
                homeScore: m.score?.fullTime?.home,
                awayScore: m.score?.fullTime?.away,
                utcDate: m.utcDate
            }, "Football-Data");
        }
    } catch (e) {
        console.error("⚠️ Football-Data indisponível:", e.message);
    }
}

async function syncFromOpenFootball() {
    console.log("⚽ [OpenFootball] Buscando placares...");
    try {
        const response = await axios.get(OPENFOOTBALL_URL);
        for (const m of response.data.matches) {
            const hCode = NAME_TO_CODE[m.team1];
            const aCode = NAME_TO_CODE[m.team2];
            if (hCode && aCode) {
                await updateMatchInFirestore(hCode, aCode, {
                    homeScore: m.score?.ft?.[0],
                    awayScore: m.score?.ft?.[1]
                }, "OpenFootball");
            }
        }
    } catch (e) {
        console.error("⚠️ OpenFootball indisponível:", e.message);
    }
}

async function updateMatchInFirestore(hCode, aCode, data, source) {
    if (!hCode || !aCode) return;

    const matchesRef = db.collection('matches');

    // Tenta encontrar o jogo na ordem direta (A x B)
    let snapshot = await matchesRef
        .where('homeTeamCode', '==', hCode)
        .where('awayTeamCode', '==', aCode)
        .get();

    // Se não encontrou, tenta na ordem invertida (B x A)
    let isInverted = false;
    if (snapshot.empty) {
        snapshot = await matchesRef
            .where('homeTeamCode', '==', aCode)
            .where('awayTeamCode', '==', hCode)
            .get();
        isInverted = true;
    }

    if (snapshot.empty) return;

    const matchDoc = snapshot.docs[0];
    const matchData = matchDoc.data();

    if (matchData.isManual) return;

    let updateObj = {};
    let changed = false;

    // 1. Atualização de Placares
    let apiHomeScore = isInverted ? data.awayScore : data.homeScore;
    let apiAwayScore = isInverted ? data.homeScore : data.awayScore;

    if (apiHomeScore !== undefined && apiAwayScore !== undefined && apiHomeScore !== null) {
        const apiTotal = apiHomeScore + apiAwayScore;
        const currentTotal = (matchData.homeScore || 0) + (matchData.awayScore || 0);

        if (apiTotal >= currentTotal && (apiHomeScore !== matchData.homeScore || apiAwayScore !== matchData.awayScore)) {
            updateObj.homeScore = apiHomeScore;
            updateObj.awayScore = apiAwayScore;
            changed = true;
        }
    }

    // 2. Atualização de Horários
    if (data.utcDate) {
        const newDateMillis = new Date(data.utcDate).getTime();
        // Se a diferença for maior que 1 minuto, atualizamos para o horário oficial da FIFA
        if (newDateMillis && Math.abs(newDateMillis - (matchData.matchDateMillis || 0)) > 60000) {
            updateObj.matchDateMillis = newDateMillis;
            changed = true;
        }
    }

    if (changed) {
        await matchDoc.ref.update(updateObj);
        console.log(`🔥 [${hCode} x ${aCode}] Sincronizado via ${source}`);
    }
}

syncScores();
