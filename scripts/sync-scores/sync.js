const admin = require('firebase-admin');
const axios = require('axios');

// Configurações
const API_URL = "https://api.football-data.org/v4/competitions/WC/matches";
const API_KEY = "c366f1ec224c43a28100c97ea5aab282";

// Inicializa Firebase com a Service Account (vinda do ambiente GitHub)
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
        console.log("⚽ Buscando placares da API...");
        const response = await axios.get(API_URL, {
            headers: { 'X-Auth-Token': API_KEY }
        });

        const matches = response.data.matches;
        console.log(`✅ ${matches.length} jogos encontrados.`);

        for (const fdMatch of matches) {
            const hScore = fdMatch.score?.fullTime?.home;
            const aScore = fdMatch.score?.fullTime?.away;
            const hCode = fdMatch.homeTeam?.tla;
            const aCode = fdMatch.awayTeam?.tla;

            if (hScore === null || aScore === null || !hCode || !aCode) continue;

            // Busca o jogo correspondente no Firestore usando os códigos de país
            // (Mapping simplificado baseado nos códigos TLA)
            const matchesRef = db.collection('matches');
            const snapshot = await matchesRef
                .where('homeTeamCode', '==', hCode)
                .where('awayTeamCode', '==', aCode)
                .get();

            if (snapshot.empty) continue;

            const matchDoc = snapshot.docs[0];
            const matchData = matchDoc.data();

            // REGRAS DE SEGURANÇA (As mesmas que colocamos no App)

            // 1. Se foi manual, ignora
            if (matchData.isManual) {
                console.log(`⏩ [${hCode} x ${aCode}] Ignorado: Alterado manualmente pelo Admin.`);
                continue;
            }

            // 2. Se a soma de gols diminuiu, ignora (Reset da API)
            const currentTotal = (matchData.homeScore || 0) + (matchData.awayScore || 0);
            const apiTotal = hScore + aScore;

            if (apiTotal < currentTotal) {
                console.log(`⏩ [${hCode} x ${aCode}] Ignorado: API retornou placar menor (${hScore}x${aScore}) que o atual (${matchData.homeScore}x${matchData.awayScore}).`);
                continue;
            }

            // 3. Atualiza se houver mudança
            if (hScore !== matchData.homeScore || aScore !== matchData.awayScore) {
                await matchDoc.ref.update({
                    homeScore: hScore,
                    awayScore: aScore
                });
                console.log(`🔥 [${hCode} x ${aCode}] ATUALIZADO para ${hScore}x${aScore}.`);
            }
        }

        console.log("🏁 Sincronização concluída.");
    } catch (error) {
        console.error("❌ Erro na sincronização:", error.message);
    }
}

syncScores();
