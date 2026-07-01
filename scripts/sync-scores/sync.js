const admin = require('firebase-admin');
const axios = require('axios');

// Configurações
const API_URL = "https://api.football-data.org/v4/competitions/WC/matches";
const API_KEY = "***REMOVED_SECRET***";
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
            const status = m.status;

            // CÁLCULO DO PLACAR: Ignorar Pênaltis
            // No Football-Data, se o jogo vai para pênaltis, 'fullTime' inclui os gols da disputa.
            // Queremos apenas o placar até o fim da prorrogação.
            let hScore, aScore;
            const s = m.score;
            if (s?.duration === "PENALTY_SHOOTOUT") {
                hScore = (s.regularTime?.home ?? 0) + (s.extraTime?.home ?? 0);
                aScore = (s.regularTime?.away ?? 0) + (s.extraTime?.away ?? 0);
            } else {
                hScore = s?.fullTime?.home;
                aScore = s?.fullTime?.away;
            }

            // Log preventivo para jogos conhecidos
            if (hCode === 'MEX' || aCode === 'MEX' || hCode === 'KOR' || aCode === 'KOR') {
                console.log(`🔎 Debug [${hCode} x ${aCode}]: Status=${status}, Placar=${hScore}x${aScore}`);
            }

            await updateMatchInFirestore(hCode, aCode, {
                homeScore: hScore,
                awayScore: aScore,
                status: status,
                utcDate: m.utcDate,
                duration: m.score?.duration
            }, "Football-Data", m.id);
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

async function updateMatchInFirestore(hCode, aCode, data, source, apiId) {
    const matchesRef = db.collection('matches');

    // Tradutor de IDs da API para os IDs originais do Firestore (Proteção de Palpites)
    const apiToInternal = {
        "537415": "KO-32-1", "537416": "KO-32-2", "537417": "KO-32-3", "537418": "KO-32-4",
        "537419": "KO-32-5", "537420": "KO-32-6", "537421": "KO-32-7", "537422": "KO-32-8",
        "537423": "KO-32-9", "537424": "KO-32-10", "537425": "KO-32-11", "537426": "KO-32-12",
        "537427": "KO-32-13", "537428": "KO-32-14", "537429": "KO-32-15", "537430": "KO-32-16",
        "537375": "KO-16-1", "537376": "KO-16-2", "537377": "KO-16-3", "537378": "KO-16-4",
        "537379": "KO-16-5", "537380": "KO-16-6", "537381": "KO-16-7", "537382": "KO-16-8",
        "537383": "KO-QF-1", "537384": "KO-QF-2", "537385": "KO-QF-3", "537386": "KO-QF-4",
        "537387": "KO-SF-1", "537388": "KO-SF-2", "537389": "KO-SF-3", "537390": "KO-FINAL"
    };

    const internalId = apiToInternal[apiId?.toString()];
    let matchDoc = null;

    if (internalId) {
        let doc = await matchesRef.doc(internalId).get();
        if (doc.exists) {
            matchDoc = doc;
        }
    }

    if (!matchDoc) {
        // Fallback por códigos
        let snapshot = await matchesRef.where('homeTeamCode', '==', hCode).where('awayTeamCode', '==', aCode).get();
        if (snapshot.empty) {
            snapshot = await matchesRef.where('homeTeamCode', '==', aCode).where('awayTeamCode', '==', hCode).get();
        }
        if (!snapshot.empty) {
            matchDoc = snapshot.docs[0];
        }
    }

    if (!matchDoc || (!matchDoc.exists && !matchDoc.id)) return;

    const docRef = matchDoc.ref || matchesRef.doc(matchDoc.id);
    const matchData = matchDoc.data ? matchDoc.data() : null;
    let isInverted = matchData && matchData.homeTeamCode === aCode;

    // TRAVA 1: Se o placar foi definido manualmente no app, nunca
obrescrevemos.
    if (matchData.isManual) return;

    let updateObj = {};
    let changed = false;

    let apiHomeScore = isInverted ? data.awayScore : data.homeScore;
    let apiAwayScore = isInverted ? data.homeScore : data.awayScore;

    // Se o jogo ainda não começou (TIMED/SCHEDULED), limpamos obrigatoriamente o placar
    // Isso evita que versões antigas do app mostrem "Em Andamento" com 0x0
    const isUpcoming = ['TIMED', 'SCHEDULED'].includes(data.status);
    const finalHomeScore = isUpcoming ? null : (apiHomeScore ?? 0);
    const finalAwayScore = isUpcoming ? null : (apiAwayScore ?? 0);

    // Só atualizamos se:
    // 1. O placar for diferente do atual
    // 2. E não for uma regressão (novo total de gols < total atual), a menos que o novo placar seja null (correção)
    if (finalHomeScore !== matchData.homeScore || finalAwayScore !== matchData.awayScore) {
        const currentTotal = (matchData.homeScore || 0) + (matchData.awayScore || 0);
        const newTotal = (finalHomeScore || 0) + (finalAwayScore || 0);

        const isProgression = matchData.homeScore === null || finalHomeScore === null || newTotal >= currentTotal;

        if (isProgression) {
            updateObj.homeScore = finalHomeScore;
            updateObj.awayScore = finalAwayScore;
            changed = true;
        } else {
            console.log(`⚠️ [${hCode} x ${aCode}] Ignorada regressão: ${matchData.homeScore}x${matchData.awayScore} -> ${finalHomeScore}x${finalAwayScore}`);
        }
    }

    if (data.utcDate) {
        const newDateMillis = new Date(data.utcDate).getTime();
        if (newDateMillis && Math.abs(newDateMillis - (matchData.matchDateMillis || 0)) > 60000) {
            updateObj.matchDateMillis = newDateMillis;
            changed = true;
        }
    }

    if (data.status) {
        // Mapeamento de status especial para Prorrogação e Pênaltis
        let derivedStatus = data.status;
        if (data.status === "IN_PLAY" && data.duration === "EXTRA_TIME") derivedStatus = "EXTRA_TIME";
        if (data.status === "IN_PLAY" && data.duration === "PENALTY_SHOOTOUT") derivedStatus = "PENALTIES";

        if (derivedStatus !== matchData.status) {
            updateObj.status = derivedStatus;
            changed = true;
        }
    }

    if (changed) {
        await matchDoc.ref.update(updateObj);
        console.log(`🔥 [${hCode} x ${aCode}] Sincronizado via ${source}: ${apiHomeScore}x${apiAwayScore} (${data.status || 'N/A'})`);
    }
}

syncScores();
