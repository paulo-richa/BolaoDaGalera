const { LIB_TEAMS } = require("./teams_lib");
const { mapPhase } = require("./utils");
const { migratePredictionsIfMatchChanged } = require("./knockout");
const { getLibertadoresData } = require("./fallback-api");
const { logger } = require("firebase-functions");

async function syncLibertadores(db, admin, axios) {
    logger.info("Iniciando sincronização da Libertadores...");
    const matchesRef = db.collection("championships").doc("LIBERTADORES").collection("matches");

    try {
        // Busca dados com fallback automático
        const apiData = await getLibertadoresData(axios);

        if (apiData && apiData.matches) {
            const resCLI = { data: apiData };
            const batch = db.batch();
            const migrationsNeeded = [];
            const now = Date.now();
            const knockoutPairs = {};

            // Mapeamento Estrito de Oitavas (Ida e Volta para a mesma Chave)
            const r16Mapping = {
                564456: 1, 564465: 1, // Estudiantes vs Católica
                564462: 2, 564470: 2, // Rosario vs Corinthians
                564460: 3, 564468: 3, // Cruzeiro vs Flamengo
                564457: 4, 564464: 4, // Tolima vs Ind. del Valle
                564461: 5, 564469: 5, // Mirassol vs LDU
                564459: 6, 564466: 6, // Palmeiras vs Cerro
                564458: 7, 564467: 7, // Platense vs Coquimbo
                564455: 8, 564463: 8  // Fluminense vs Rivadavia
            };

            for (const m of resCLI.data.matches) {
                const knockoutStages = ["ROUND_OF_16", "PLAY_OFFS", "QUARTER_FINALS", "SEMI_FINALS", "FINAL"];
                if (m.stage !== "GROUP_STAGE" && !knockoutStages.includes(m.stage)) continue;

                const isKO = knockoutStages.includes(m.stage);
                const mappedPhase = mapPhase(m.stage);

                let matchId = `CLI-2026-M${m.id}`;
                let knockoutOrder = 0;

                let hName = m.homeTeam?.name || "A definir";
                let aName = m.awayTeam?.name || "A definir";
                let hTeam = LIB_TEAMS[hName] || { name: hName, flag: "", code: m.homeTeam?.tla || "TBD", crest: null };
                let aTeam = LIB_TEAMS[aName] || { name: aName, flag: "", code: m.awayTeam?.tla || "TBD", crest: null };

                if (mappedPhase === "ROUND_OF_16") {
                    knockoutOrder = r16Mapping[m.id] || 0;
                    const isVolta = m.matchday === 2 || m.id > 564462;

                    // MAPA DE IDs DE PRODUÇÃO (Baseado nos palpites reais dos usuários)
                    const productionIds = {
                        "564456-L1": "CLI-2026-R16-1-L1", // Estudiantes Ida
                        "564462-L1": "CLI-2026-R16-2-L1", // Corinthians Ida
                        "564459-L1": "CLI-2026-R16-6-L1", // Palmeiras Ida
                        "564470-L2": "CLI-2026-M564470-L2", // Corinthians Volta
                        "564466-L2": "CLI-2026-M564466-L2", // Palmeiras Volta
                        "564464-L2": "CLI-2026-M564464-L2", // Ind. del Valle Volta
                        "564469-L2": "CLI-2026-M564469-L2", // LDU Volta
                        "564465-L2": "CLI-2026-M564465-L2",
                        "564468-L2": "CLI-2026-M564468-L2",
                        "564467-L2": "CLI-2026-M564467-L2",
                        "564463-L2": "CLI-2026-M564463-L2"
                    };

                    const key = `${m.id}-${isVolta ? "L2" : "L1"}`;
                    matchId = productionIds[key] || `CLI-2026-R16-${knockoutOrder}-${isVolta ? "L2" : "L1"}`;
                } else if (mappedPhase === "QUARTERFINALS") {
                    // Quartas: API traz em pares (Ida/Volta para cada confronto)
                    // Mapeamos por ordem de aparecer e alternância
                    const qfMatches = resCLI.data.matches
                        .filter(x => x.stage === "QUARTER_FINALS")
                        .sort((a, b) => a.id - b.id);
                    const idx = qfMatches.findIndex(x => x.id === m.id);

                    if (idx !== -1) {
                        knockoutOrder = Math.floor(idx / 2) + 1;
                        const isVolta = idx % 2 === 1; // 0,1 -> QF1; 2,3 -> QF2; etc
                        matchId = `CLI-2026-QF${knockoutOrder}-${isVolta ? "L2" : "L1"}`;

                        // ⚠️  IMPORTANTE: Na Volta (L2), times devem estar INVERTIDOS
                        // Se for Volta e times são iguais aos da Ida, inverte
                        // (troca as REFERÊNCIAS das variáveis, nunca os campos dos
                        // objetos de LIB_TEAMS — esses são compartilhados/mutáveis
                        // e mutar seus campos corromperia o cadastro do time para
                        // todos os outros jogos processados nesta e em futuras
                        // execuções da function)
                        if (isVolta && idx > 0) {
                            const idaMatch = qfMatches[idx - 1];
                            if (hName === idaMatch.homeTeam?.name && aName === idaMatch.awayTeam?.name) {
                                const tempName = hName;
                                hName = aName;
                                aName = tempName;
                                const tempTeam = hTeam;
                                hTeam = aTeam;
                                aTeam = tempTeam;
                            }
                        }
                    } else {
                        // Fallback (nunca deve acontecer)
                        matchId = `CLI-2026-M${m.id}`;
                    }
                } else if (mappedPhase === "SEMIFINALS") {
                    // Semis: Mesmo padrão das Quartas
                    const sfMatches = resCLI.data.matches
                        .filter(x => x.stage === "SEMI_FINALS")
                        .sort((a, b) => a.id - b.id);
                    const idx = sfMatches.findIndex(x => x.id === m.id);

                    if (idx !== -1) {
                        knockoutOrder = Math.floor(idx / 2) + 1;
                        const isVolta = idx % 2 === 1; // 0,1 -> SF1; 2,3 -> SF2
                        matchId = `CLI-2026-SF${knockoutOrder}-${isVolta ? "L2" : "L1"}`;

                        // ⚠️  IMPORTANTE: Na Volta (L2), times devem estar INVERTIDOS
                        // (troca as referências, não os campos dos objetos — ver
                        // comentário equivalente no bloco de QUARTERFINALS acima)
                        if (isVolta && idx > 0) {
                            const idaMatch = sfMatches[idx - 1];
                            if (hName === idaMatch.homeTeam?.name && aName === idaMatch.awayTeam?.name) {
                                const tempName = hName;
                                hName = aName;
                                aName = tempName;
                                const tempTeam = hTeam;
                                hTeam = aTeam;
                                aTeam = tempTeam;
                            }
                        }
                    } else {
                        matchId = `CLI-2026-M${m.id}`;
                    }
                } else if (mappedPhase === "FINAL") {
                    // Final: sempre um único jogo (sem Ida/Volta)
                    matchId = `CLI-2026-FINAL`;
                    knockoutOrder = 1;
                }

                const matchTime = Date.parse(m.utcDate);
                let targetStatus = m.status;

                // LÓGICA DE PLACAR: Sempre priorizar tempo normal (90 min)
                const s = m.score;
                let hScore = null, aScore = null;
                if (s) {
                    // Se existe regularTime, ele é o nosso 90 minutos oficial (independente de duration)
                    if (s.regularTime && s.regularTime.home !== null) {
                        hScore = s.regularTime.home;
                        aScore = s.regularTime.away;
                    } else {
                        // Fallback para fullTime se for o único disponível
                        hScore = s.fullTime?.home ?? null;
                        aScore = s.fullTime?.away ?? null;
                    }
                }

                if (m.status !== "FINISHED" && (now - matchTime > 4 * 3600000) && hScore !== null) {
                    targetStatus = "FINISHED";
                }

                const currentDoc = await matchesRef.doc(matchId).get();
                if (currentDoc.exists && currentDoc.data().isManual) {
                    continue;
                }

                // TRAVA DE SEGURANÇA: nunca aceitar um retrocesso da API (ex.: um jogo
                // já FINISHED com placar real virar algo diferente com placar nulo).
                const existingData = currentDoc.exists ? currentDoc.data() : null;
                // Usa m.status (valor CRU da API), não targetStatus, pelo mesmo
                // motivo do brasileirao.js: a promoção heurística para FINISHED
                // após 4h parado não deve disfarçar um dado ainda incompleto.
                const existingIsFinal = existingData && existingData.status === "FINISHED" &&
                    existingData.homeScore !== null && existingData.awayScore !== null;
                if (existingIsFinal && (m.status !== "FINISHED" || hScore === null || aScore === null)) {
                    logger.warn(
                        `⚠️ Ignorando retrocesso da API para ${matchId}: ` +
                        `era FINISHED ${existingData.homeScore}x${existingData.awayScore}, API mandou status=${m.status} ${hScore}x${aScore}`
                    );
                    continue;
                }

                const updates = {
                    status: targetStatus,
                    homeScore: hScore,
                    awayScore: aScore,
                    apiWinner: m.score?.winner || null,
                    championshipId: "LIBERTADORES",
                    phase: mappedPhase,
                    matchOrder: knockoutOrder,
                    group: m.group || (m.matchday ? `Rodada ${m.matchday}` : m.stage),
                    matchDateMillis: matchTime,
                    lastSync: admin.firestore.FieldValue.serverTimestamp(),
                    source: "api"
                };

                // SÓ ATUALIZA TIMES SE A API TROUXER NOMES REAIS ou se o campo no banco estiver vazio/TBD
                const existing = currentDoc.data();
                if (hTeam.code !== "TBD" || !existing || existing.homeTeamCode === "TBD") {
                    updates.homeTeam = hTeam.name;
                    updates.homeTeamCode = hTeam.code;
                    updates.homeTeamFlag = hTeam.flag;
                    updates.homeTeamCrest = hTeam.crest || m.homeTeam?.crest;
                }
                if (aTeam.code !== "TBD" || !existing || existing.awayTeamCode === "TBD") {
                    updates.awayTeam = aTeam.name;
                    updates.awayTeamCode = aTeam.code;
                    updates.awayTeamFlag = aTeam.flag;
                    updates.awayTeamCrest = aTeam.crest || m.awayTeam?.crest;
                }

                batch.set(matchesRef.doc(matchId), updates, { merge: true });

                // Detectar mudança de mandos e migrar palpites se necessário (Quartas/Semis/Final)
                if (existing && (mappedPhase === "QUARTERFINALS" || mappedPhase === "SEMIFINALS" || mappedPhase === "FINAL")) {
                    const oldHomeCode = existing.homeTeamCode;
                    const oldAwayCode = existing.awayTeamCode;
                    const newHomeCode = updates.homeTeamCode;
                    const newAwayCode = updates.awayTeamCode;

                    if (oldHomeCode && oldAwayCode && (oldHomeCode !== newHomeCode || oldAwayCode !== newAwayCode)) {
                        // Times mudaram - vai precisar migrar palpites após o batch
                        migrationsNeeded.push({
                            matchId,
                            oldMatch: existing,
                            newMatch: updates
                        });
                    }
                }
            }
            await batch.commit();

            // Processar migrações de palpites (depois do commit para evitar locks)
            for (const migration of migrationsNeeded) {
                await migratePredictionsIfMatchChanged(db, migration.matchId, migration.oldMatch, migration.newMatch);
            }

            logger.info(`Libertadores sincronizada.`);
        }
    } catch (e) {
        logger.error("Erro na sincronização da Libertadores:", e.message);
    }
}

module.exports = { syncLibertadores };
