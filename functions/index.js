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
    "DR Congo": "COD", "Rep. Congo": "COD", "Congo DR": "COD",
 "England": "ENG", "Inglaterra": "ENG",
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
    schedule: "every 1 minutes",
    timeZone: "America/Sao_Paulo",
    region: "southamerica-east1",
    memory: "256MiB"
}, async (event) => {
    try {
        // 1. VERIFICAÇÃO DE ECONOMIA: Existe jogo rolando agora ou próximo?
        const matchesRef = db.collection('matches');
        const now = Date.now();
        const buffer = 30 * 60 * 1000; // 30 minutos de margem (pré e pós)
        const totalWindow = 150 * 60 * 1000; // 2h30 (Tempo médio de jogo + 30min buffer pós)

        // Query A: Jogos com status de "Em andamento"
        const activeMatchesSnap = await matchesRef
            .where('status', 'in', ['IN_PLAY', 'PAUSED', 'EXTRA_TIME', 'PENALTIES', 'LIVE'])
            .get();

        // Query B: Janela de tempo (30min antes de começar até 30min depois de tecnicamente terminar)
        const hotWindowSnap = await matchesRef
            .where('matchDateMillis', '>=', now - totalWindow)
            .where('matchDateMillis', '<=', now + buffer)
            .get();

        const hasActiveMatch = !activeMatchesSnap.empty || !hotWindowSnap.empty;

        // 2. Se não tem jogo ativo e não está na janela de pico, verificamos a última execução global
        if (!hasActiveMatch) {
            const configDoc = await db.collection('config').doc('sync_status').get();
            const lastSync = configDoc.exists ? configDoc.data().lastFullSync : 0;
            const thirtyMinutesMillis = 30 * 60 * 1000;

            // Se passaram menos de 30 min desde a última carga completa, pulamos a chamada da API
            if (Date.now() - lastSync < thirtyMinutesMillis) {
                //logger.info("Economia: Sem jogos ativos. Pulando chamada da API (Próxima carga completa em 30min).");
                return;
            }
        }

        logger.info(hasActiveMatch ? "Jogos ativos detectados! Sincronizando a cada 1 min..." : "Iniciando carga completa de rotina (30 min)...");

        const response = await axios.get("https://api.football-data.org/v4/competitions/WC/matches", {
            headers: {
                'X-Auth-Token': API_KEY,
                'Connection': 'keep-alive'
            },
            timeout: 15000 // 15 segundos de limite
        });

        const matches = response.data.matches || [];

        let updatedCount = 0;
        for (const m of matches) {
            // Log detalhado para jogos em andamento ou próximos
            if (m.status !== 'FINISHED' && m.status !== 'TIMED') {
                logger.info(`API: ${m.homeTeam.name} ${m.score.fullTime?.home ?? 0}x${m.score.fullTime?.away ?? 0} ${m.awayTeam.name} (${m.status})`, {
                    matchId: m.id,
                    status: m.status,
                    score: m.score,
                    homeTeam: m.homeTeam,
                    awayTeam: m.awayTeam,
                    utcDate: m.utcDate
                });
            }

            const wasUpdated = await updateMatchInFirestore(m, matches);
            if (wasUpdated) updatedCount++;
        }

        // Atualiza o timestamp da última carga completa
        await db.collection('config').doc('sync_status').set({
            lastFullSync: Date.now(),
            hasActiveMatches: hasActiveMatch
        }, { merge: true });

        logger.info(`Fim do ciclo. ${updatedCount} jogos afetados.`);
    } catch (error) {
        logger.error("Erro na sincronização:", error.message);
    }
});

async function updateMatchInFirestore(apiMatch, allApiMatches) {
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

    const internalId = apiToInternal[apiMatch.id.toString()];
    let matchDoc = null;

    if (internalId) {
        const directDoc = await matchesRef.doc(internalId).get();
        if (directDoc.exists) {
            matchDoc = directDoc;
        }
    }

    if (!matchDoc) {
        // Fallback por códigos
        const hCode = apiMatch.homeTeam?.tla || NAME_TO_CODE[apiMatch.homeTeam?.name];
        const aCode = apiMatch.awayTeam?.tla || NAME_TO_CODE[apiMatch.awayTeam?.name];

        if (hCode && aCode) {
            let snapshot = await matchesRef.where('homeTeamCode', '==', hCode).where('awayTeamCode', '==', aCode).get();
            if (snapshot.empty) {
                snapshot = await matchesRef.where('homeTeamCode', '==', aCode).where('awayTeamCode', '==', hCode).get();
            }
            if (!snapshot.empty) {
                matchDoc = snapshot.docs[0];
            }
        }
    }

    if (matchDoc && (matchDoc.exists || matchDoc.id)) {
        const docRef = matchDoc.ref || matchesRef.doc(matchDoc.id);
        const matchData = matchDoc.data ? matchDoc.data() : null;
        let isInverted = false;
        if (matchData) {
            isInverted = matchData.homeTeamCode === (apiMatch.awayTeam?.tla || NAME_TO_CODE[apiMatch.awayTeam?.name]);
        }

        // Se estiver no manual, só atualizamos se o status da API for FINISHED (para garantir o fim do jogo)
        // ou se o placar for explicitamente diferente e o admin não tiver bloqueado
        if (matchData && matchData.isManual && apiMatch.status !== 'FINISHED') return false;

        // CÁLCULO DO PLACAR: Ignorar Pênaltis
        let apiHome, apiAway;
        const s = apiMatch.score;

        if (s.duration === "PENALTY_SHOOTOUT") {
            apiHome = (s.regularTime?.home ?? 0) + (s.extraTime?.home ?? 0);
            apiAway = (s.regularTime?.away ?? 0) + (s.extraTime?.away ?? 0);
        } else {
            apiHome = s.fullTime?.home;
            apiAway = s.fullTime?.away;
        }

        const rawHome = isInverted ? apiAway : apiHome;
        const rawAway = isInverted ? apiHome : apiAway;

        const isUpcoming = ['TIMED', 'SCHEDULED'].includes(apiMatch.status);
        const apiHomeScore = isUpcoming ? null : (rawHome ?? 0);
        const apiAwayScore = isUpcoming ? null : (rawAway ?? 0);

        let updateObj = {};
        let changed = false;

        if (apiHomeScore !== matchData.homeScore || apiAwayScore !== matchData.awayScore) {
            updateObj.homeScore = apiHomeScore;
            updateObj.awayScore = apiAwayScore;
            changed = true;
        }

        let derivedStatus = apiMatch.status;
        if (apiMatch.status === "IN_PLAY") {
            if (apiMatch.score?.duration === "EXTRA_TIME") derivedStatus = "EXTRA_TIME";
            else if (apiMatch.score?.duration === "PENALTY_SHOOTOUT") derivedStatus = "PENALTIES";
        } else if (apiMatch.status === "PAUSED") {
            if (apiMatch.score?.duration === "EXTRA_TIME") derivedStatus = "PAUSED_EXTRA_TIME";
            else if (apiMatch.score?.duration === "PENALTY_SHOOTOUT") derivedStatus = "PAUSED_PENALTIES";
        }

        if (derivedStatus !== matchData.status) {
            updateObj.status = derivedStatus;
            changed = true;
        }

        const apiDateMillis = new Date(apiMatch.utcDate).getTime();
        if (apiDateMillis !== matchData.matchDateMillis) {
            updateObj.matchDateMillis = apiDateMillis;
            changed = true;
        }

        if (changed) {
            await docRef.update(updateObj);
            return true;
        }
        return false;
    }

    // Se não achou de jeito nenhum e for mata-mata, tenta pela lógica de stage
    if (apiMatch.stage !== 'GROUP_STAGE') {
        const hCode = apiMatch.homeTeam?.tla || NAME_TO_CODE[apiMatch.homeTeam?.name];
        const aCode = apiMatch.awayTeam?.tla || NAME_TO_CODE[apiMatch.awayTeam?.name];
        return await updateKnockoutByStage(apiMatch, hCode, aCode, allApiMatches);
    }
    return false;
}

async function updateKnockoutByStage(apiMatch, hCode, aCode, allApiMatches) {
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
        const apiMatchesInStage = allApiMatches
            .filter(m => m.stage === apiMatch.stage)
            .sort((a, b) => a.id - b.id);

        const index = apiMatchesInStage.findIndex(m => m.id === apiMatch.id);
        if (index !== -1) target = localMatchesSorted[index];
    }

    if (target) {
        if (target.isManual) return false;

        const hInfo = hCode ? TEAM_INFO[hCode] : null;
        const aInfo = aCode ? TEAM_INFO[aCode] : null;

        let derivedStatus = apiMatch.status;
        if (apiMatch.status === "IN_PLAY") {
            if (apiMatch.score?.duration === "EXTRA_TIME") derivedStatus = "EXTRA_TIME";
            else if (apiMatch.score?.duration === "PENALTY_SHOOTOUT") derivedStatus = "PENALTIES";
        } else if (apiMatch.status === "PAUSED") {
            if (apiMatch.score?.duration === "EXTRA_TIME") derivedStatus = "PAUSED_EXTRA_TIME";
            else if (apiMatch.score?.duration === "PENALTY_SHOOTOUT") derivedStatus = "PAUSED_PENALTIES";
        }

        let updateData = {
            status: derivedStatus,
            matchDateMillis: new Date(apiMatch.utcDate).getTime()
        };

        // Só atualiza os times se a API trouxer códigos válidos
        if (hCode) {
            updateData.homeTeam = hInfo ? hInfo[0] : apiMatch.homeTeam.name;
            updateData.homeTeamCode = hCode;
            updateData.homeTeamFlag = hInfo ? hInfo[1] : "🏳️";
        }

        if (aCode) {
            updateData.awayTeam = aInfo ? aInfo[0] : apiMatch.awayTeam.name;
            updateData.awayTeamCode = aCode;
            updateData.awayTeamFlag = aInfo ? aInfo[1] : "🏳️";
        }

        // Atualiza placar se houver (Ignora Pênaltis)
        let apiHome, apiAway;
        const s = apiMatch.score;
        if (s.duration === "PENALTY_SHOOTOUT") {
            apiHome = (s.regularTime?.home ?? 0) + (s.extraTime?.home ?? 0);
            apiAway = (s.regularTime?.away ?? 0) + (s.extraTime?.away ?? 0);
        } else {
            apiHome = s.fullTime?.home;
            apiAway = s.fullTime?.away;
        }

        const apiHomeScore = (apiHome === null && apiMatch.status === 'TIMED') ? null : (apiHome ?? 0);
        const apiAwayScore = (apiAway === null && apiMatch.status === 'TIMED') ? null : (apiAway ?? 0);

        if (apiHomeScore !== target.homeScore || apiAwayScore !== target.awayScore) {
            updateData.homeScore = apiHomeScore;
            updateData.awayScore = apiAwayScore;
        }

        await matchesRef.doc(target.id).update(updateData);
        return true;
    }
    return false;
}
