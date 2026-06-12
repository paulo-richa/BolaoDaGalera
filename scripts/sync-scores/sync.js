const admin = require('firebase-admin');
const axios = require('axios');

// Configurações
const API_URL = "https://api.football-data.org/v4/competitions/WC/matches";
const API_KEY = "c366f1ec224c43a28100c97ea5aab282";
const OPENFOOTBALL_URL = "https://raw.githubusercontent.com/openfootball/worldcup.json/master/2026/worldcup.json";

const NAME_TO_CODE = {
    // Grupo A
    "Mexico": "MEX", "México": "MEX",
    "South Africa": "RSA", "África do Sul": "RSA",
    "Korea Republic": "KOR", "South Korea": "KOR", "Coreia do Sul": "KOR",
    "Czech Republic": "CZE", "Czechia": "CZE", "República Tcheca": "CZE", "Rep. Tcheca": "CZE",
    // Grupo B
    "Canada": "CAN", "Canadá": "CAN",
    "Switzerland": "SUI", "Suíça": "SUI",
    "Qatar": "QAT", "Catar": "QAT",
    "Bosnia and Herzegovina": "BIH", "Bosnia & Herzegovina": "BIH", "Bosnia-Herzegovina": "BIH", "Bosnia": "BIH", "Bósnia": "BIH",
    // Grupo C
    "Brazil": "BRA", "Brasil": "BRA",
    "Morocco": "MAR", "Marrocos": "MAR",
    "Scotland": "SCO", "Escócia": "SCO",
    "Haiti": "HAI",
    // Grupo D
    "United States": "USA", "USA": "USA", "EUA": "USA", "United States of America": "USA",
    "Australia": "AUS", "Austrália": "AUS",
    "Paraguay": "PAR", "Paraguai": "PAR",
    "Turkey": "TUR", "Türkiye": "TUR", "Turquia": "TUR",
    // Grupo E
    "Germany": "GER", "Alemanha": "GER",
    "Ecuador": "ECU", "Equador": "ECU",
    "Ivory Coast": "CIV", "Côte d'Ivoire": "CIV", "Cote d'Ivoire": "CIV", "Costa do Marfim": "CIV",
    "Curaçao": "CUW", "Curacao": "CUW",
    // Grupo F
    "Netherlands": "NED", "Holland": "NED", "Holanda": "NED", "Países Baixos": "NED",
    "Japan": "JPN", "Japão": "JPN",
    "Tunisia": "TUN", "Tunísia": "TUN",
    "Sweden": "SWE", "Suécia": "SWE",
    // Grupo G
    "Belgium": "BEL", "Bélgica": "BEL",
    "Iran": "IRN", "Irã": "IRN",
    "Egypt": "EGY", "Egito": "EGY",
    "New Zealand": "NZL", "Nova Zelândia": "NZL",
    // Grupo H
    "Spain": "ESP", "Espanha": "ESP",
    "Uruguay": "URU", "Uruguai": "URU",
    "Saudi Arabia": "KSA", "Arábia Saudita": "KSA",
    "Cape Verde Islands": "CPV", "Cape Verde": "CPV", "Cabo Verde": "CPV",
    // Grupo I
    "France": "FRA", "França": "FRA",
    "Senegal": "SEN",
    "Norway": "NOR", "Noruega": "NOR",
    "Iraq": "IRQ", "Iraque": "IRQ",
    // Grupo J
    "Argentina": "ARG",
    "Austria": "AUT", "Áustria": "AUT",
    "Algeria": "ALG", "Argélia": "ALG",
    "Jordan": "JOR", "Jordânia": "JOR",
    // Grupo K
    "Portugal": "POR",
    "Colombia": "COL", "Colômbia": "COL",
    "Uzbekistan": "UZB", "Uzbequistão": "UZB",
    "DR Congo": "COD", "Congo DR": "COD", "Democratic Republic of Congo": "COD", "Congo, DR": "COD", "Rep. Congo": "COD",
    // Grupo L
    "England": "ENG", "Inglaterra": "ENG",
    "Croatia": "CRO", "Croácia": "CRO",
    "Panama": "PAN", "Panamá": "PAN",
    "Ghana": "GHA", "Gana": "GHA",
    // Outros
    "Chile": "CHI",
    "Iceland": "ISL", "Islândia": "ISL"
};

if (!process.env.FIREBASE_SERVICE_ACCOUNT) {
    console.error("ERRO: FIREBASE_SERVICE_ACCOUNT não configurada.");
    process.exit(1);
}

const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });
const db = admin.firestore();

async function syncScores() {
    try {
        console.log(`🚀 [${new Date().toISOString()}] Iniciando Sincronização...`);
        await syncFromFootballData();
        await syncFromOpenFootball();
        console.log("🏁 Sincronização concluída.");
    } catch (error) {
        console.error("❌ Erro fatal:", error.message);
    }
}

async function syncFromFootballData() {
    console.log("⚽ [Football-Data] Verificando...");
    try {
        const response = await axios.get(API_URL, { headers: { 'X-Auth-Token': API_KEY } });
        const matches = response.data.matches || [];
        console.log(`📊 Recebidos ${matches.length} jogos do Football-Data`);

        for (const m of matches) {
            const hCode = m.homeTeam?.tla;
            const aCode = m.awayTeam?.tla;
            const status = m.status; // TIMED, IN_PLAY, FINISHED
            const hScore = m.score?.fullTime?.home;
            const aScore = m.score?.fullTime?.away;

            // Log preventivo para jogos conhecidos (ex: MEX x RSA)
            if (hCode === 'MEX' || aCode === 'MEX' || hCode === 'KOR' || aCode === 'KOR') {
                console.log(`🔎 Debug [${hCode} x ${aCode}]: Status=${status}, Placar=${hScore}x${aScore}`);
            }

            await updateMatchInFirestore(hCode, aCode, {
                homeScore: hScore,
                awayScore: aScore,
                status: status,
                utcDate: m.utcDate
            }, "Football-Data");
        }
    } catch (e) { console.error("⚠️ Football-Data erro:", e.message); }
}

async function syncFromOpenFootball() {
    console.log("⚽ [OpenFootball] Verificando...");
    try {
        const response = await axios.get(OPENFOOTBALL_URL);
        const matches = response.data.matches || [];
        for (const m of matches) {
            const hCode = NAME_TO_CODE[m.team1];
            const aCode = NAME_TO_CODE[m.team2];
            if (hCode && aCode) {
                await updateMatchInFirestore(hCode, aCode, {
                    homeScore: m.score?.ft?.[0],
                    awayScore: m.score?.ft?.[1]
                }, "OpenFootball");
            }
        }
    } catch (e) { console.error("⚠️ OpenFootball erro:", e.message); }
}

async function updateMatchInFirestore(hCode, aCode, data, source) {
    if (!hCode || !aCode) return;

    const matchesRef = db.collection('matches');
    let snapshot = await matchesRef.where('homeTeamCode', '==', hCode).where('awayTeamCode', '==', aCode).get();
    let isInverted = false;

    if (snapshot.empty) {
        snapshot = await matchesRef.where('homeTeamCode', '==', aCode).where('awayTeamCode', '==', hCode).get();
        isInverted = true;
    }

    if (snapshot.empty) return;

    const matchDoc = snapshot.docs[0];
    const matchData = matchDoc.data();

    // TRAVA 1: Se o placar foi definido manualmente no app, nunca sobrescrevemos.
    if (matchData.isManual) return;

    let updateObj = {};
    let changed = false;

    let apiHomeScore = isInverted ? data.awayScore : data.homeScore;
    let apiAwayScore = isInverted ? data.homeScore : data.awayScore;

    // TRAVA 2: Se o jogo ainda não começou (TIMED) e a API manda 0x0,
    // ignoramos para não sobrescrever o null (que no app aparece como agendado).
    if (data.status === 'TIMED' && apiHomeScore === 0 && apiAwayScore === 0) {
        return;
    }

    // TRAVA 3: Para o OpenFootball (que não manda status), ignoramos 0x0 se já tivermos um placar.
    if (source === "OpenFootball" && apiHomeScore === 0 && apiAwayScore === 0 && matchData.homeScore !== null && matchData.homeScore !== undefined) {
        return;
    }

    // Só atualizamos se o placar vindo da API não for nulo
    if (apiHomeScore !== undefined && apiAwayScore !== undefined && apiHomeScore !== null && apiAwayScore !== null) {

        // CÁLCULO DE SEGURANÇA: Soma de gols atual vs nova
        const currentTotal = (matchData.homeScore || 0) + (matchData.awayScore || 0);
        const newTotal = apiHomeScore + apiAwayScore;

        // Só atualizamos se:
        // 1. O placar atual for nulo (primeira sincronização)
        // 2. OU se o novo total de gols for maior ou igual ao atual (evita voltar para 0x0)
        const isProgression = matchData.homeScore === null || matchData.homeScore === undefined || newTotal >= currentTotal;

        if (isProgression) {
            if (apiHomeScore !== matchData.homeScore || apiAwayScore !== matchData.awayScore) {
                updateObj.homeScore = apiHomeScore;
                updateObj.awayScore = apiAwayScore;
                changed = true;
            }
        } else {
            console.log(`⚠️ [${hCode} x ${aCode}] Ignorada tentativa de regressão: ${matchData.homeScore}x${matchData.awayScore} -> ${apiHomeScore}x${apiAwayScore}`);
        }
    }

    if (data.utcDate) {
        const newDateMillis = new Date(data.utcDate).getTime();
        if (newDateMillis && Math.abs(newDateMillis - (matchData.matchDateMillis || 0)) > 60000) {
            updateObj.matchDateMillis = newDateMillis;
            changed = true;
        }
    }

    if (changed) {
        await matchDoc.ref.update(updateObj);
        console.log(`🔥 [${hCode} x ${aCode}] Sincronizado via ${source}: ${apiHomeScore}x${apiAwayScore}`);
    }
}

syncScores();
