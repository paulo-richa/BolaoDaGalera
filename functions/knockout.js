const { logger } = require("firebase-functions");
const { LIB_TEAMS } = require("./teams_lib");

/**
 * Detecta mudança de mandos de campo entre L1 e L2 (inversão normal em mata-mata)
 * E migra palpites automaticamente quando a API sobrescreve com dados reais
 */
async function migratePredictionsIfMatchChanged(db, matchId, oldMatch, newMatch) {
    try {
        // Verificar se houve mudança significativa de times
        const teamsChanged =
            oldMatch.homeTeamCode !== newMatch.homeTeamCode ||
            oldMatch.awayTeamCode !== newMatch.awayTeamCode;

        if (!teamsChanged) {
            return; // Sem mudança, sem necessidade de migração
        }

        logger.info(`🔄 Detectada mudança de mandos em ${matchId}. Iniciando migração de palpites...`);

        // Buscar todos os palpites desta partida
        const predictionsRef = db.collection("predictions");
        const snapshot = await predictionsRef.where("matchId", "==", matchId).get();

        if (snapshot.empty) {
            logger.info(`✅ Nenhum palpite encontrado para ${matchId}. Migração concluída.`);
            return;
        }

        // Preparar batch de migração
        const batch = db.batch();
        let migratedCount = 0;

        snapshot.forEach(doc => {
            const prediction = doc.data();
            const oldHome = prediction.homeScore || 0;
            const oldAway = prediction.awayScore || 0;

            // Inverte o palpite (L2 tem mandos invertidos)
            const updates = {
                homeScore: oldAway,
                awayScore: oldHome,
                migratedFromAPIUpdate: true,
                migratedAt: admin.firestore.FieldValue.serverTimestamp(),
                migrationReason: `Mandos de campo invertidos em ${matchId}`
            };

            batch.update(doc.ref, updates);
            migratedCount++;

            logger.info(`  📝 Palpite ${doc.id}: ${oldHome}-${oldAway} → ${oldAway}-${oldHome}`);
        });

        await batch.commit();
        logger.info(`✅ ${migratedCount} palpites migraram com sucesso para ${matchId}`);

    } catch (e) {
        logger.error(`❌ Erro ao migrar palpites de ${matchId}:`, e.message);
    }
}

/**
 * Lógica para avançar times no mata-mata da Libertadores.
 */
async function advanceTeams(db, admin, championshipId) {
    if (championshipId !== "LIBERTADORES") return;
    const matchesRef = db.collection("championships").doc(championshipId).collection("matches");

    try {
        const snapshot = await matchesRef.get();
        const allMatches = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));

        // 1. OITAVAS -> QUARTAS
        // Agrupamos pela Ordem (1-8) definida na sincronização
        const matches16 = allMatches.filter(m => m.phase === "ROUND_OF_16");
        const groups16 = groupByOrder(matches16);

        for (const order in groups16) {
            const pair = groups16[order];
            // Ida é L1, Volta é L2
            const ida = pair.find(m => m.id.endsWith("-L1"));
            const volta = pair.find(m => m.id.endsWith("-L2"));

            // Apenas avança quando Volta estiver concluída
            if (volta && (volta.status === "FINISHED" || (volta.apiWinner && volta.apiWinner !== "DRAW"))) {
                const winner = determineWinner(ida, volta);
                if (winner) await updateNextPhase(db, admin, championshipId, "QUARTERFINALS", parseInt(order), winner, true);
            }
        }

        // 2. QUARTAS -> SEMIS
        const matchesQF = allMatches.filter(m => m.phase === "QUARTERFINALS");
        const groupsQF = groupByOrder(matchesQF);
        for (const order in groupsQF) {
            const pair = groupsQF[order];
            const ida = pair.find(m => m.id.includes("-L1"));
            const volta = pair.find(m => m.id.includes("-L2"));

            // Apenas avança quando Volta estiver concluída
            if (volta && (volta.status === "FINISHED" || (volta.apiWinner && volta.apiWinner !== "DRAW"))) {
                const winner = determineWinner(ida, volta);
                if (winner) await updateNextPhase(db, admin, championshipId, "SEMIFINALS", parseInt(order), winner, true);
            }
        }

        // 3. SEMIS -> FINAL
        // Final só é atualizada quando ambos vencedores das Semis estiverem confirmados
        const matchesSF = allMatches.filter(m => m.phase === "SEMIFINALS");
        const groupsSF = groupByOrder(matchesSF);

        const finalCandidates = [];
        for (const order in groupsSF) {
            const pair = groupsSF[order];
            const ida = pair.find(m => m.id.includes("-L1"));
            const volta = pair.find(m => m.id.includes("-L2"));

            // Apenas adiciona quando Volta estiver confirmada
            if (volta && (volta.status === "FINISHED" || (volta.apiWinner && volta.apiWinner !== "DRAW"))) {
                const winner = determineWinner(ida, volta);
                if (winner) finalCandidates.push(winner);
            }
        }

        // Atualizar FINAL apenas com vencedores confirmados
        if (finalCandidates.length >= 2) {
            const finalMatch = allMatches.find(m => m.phase === "FINAL");
            if (finalMatch) {
                const updates = {
                    homeTeam: finalCandidates[0].name,
                    homeTeamCode: finalCandidates[0].code,
                    homeTeamFlag: finalCandidates[0].flag || "",
                    homeTeamCrest: finalCandidates[0].crest || null,
                    awayTeam: finalCandidates[1].name,
                    awayTeamCode: finalCandidates[1].code,
                    awayTeamFlag: finalCandidates[1].flag || "",
                    awayTeamCrest: finalCandidates[1].crest || null,
                    source: "autoAdvance_confirmed",
                    lastUpdated: admin.firestore.FieldValue.serverTimestamp()
                };
                await matchesRef.doc(finalMatch.id).update(updates);
            }
        }

    } catch (e) {
        logger.error("Erro no avanço de chave:", e.message);
    }
}

function getCleanName(fullName) {
    if (!fullName) return "";
    // Busca no LIB_TEAMS pelo nome limpo oficial
    for (const [key, data] of Object.entries(LIB_TEAMS)) {
        if (key === fullName || data.name === fullName) return data.name;
    }
    return fullName.replace("CA ", "").replace("CD ", "").replace("SC ", "").replace("FC ", "").replace("CR ", "").replace("SE ", "").trim().split(" ").shift();
}

function groupByOrder(matches) {
    const groups = {};
    matches.forEach(m => {
        if (m.matchOrder) {
            if (!groups[m.matchOrder]) groups[m.matchOrder] = [];
            groups[m.matchOrder].push(m);
        }
    });
    return groups;
}

function determineWinner(ida, volta) {
    if (!volta) return null;

    // Se a API já disse quem ganhou a série (útil para pênaltis)
    if (volta.apiWinner === "HOME_TEAM") return { name: volta.homeTeam, code: volta.homeTeamCode, flag: volta.homeTeamFlag, crest: volta.homeTeamCrest };
    if (volta.apiWinner === "AWAY_TEAM") return { name: volta.awayTeam, code: volta.awayTeamCode, flag: volta.awayTeamFlag, crest: volta.awayTeamCrest };

    // Caso a API diga DRAW mas houve disputa de pênaltis no placar
    const s = volta.score;
    if (s && s.penalties) {
        if (s.penalties.home > s.penalties.away) return { name: volta.homeTeam, code: volta.homeTeamCode, flag: volta.homeTeamFlag, crest: volta.homeTeamCrest };
        if (s.penalties.away > s.penalties.home) return { name: volta.awayTeam, code: volta.awayTeamCode, flag: volta.awayTeamFlag, crest: volta.awayTeamCrest };
    }

    // Agregado correto: mandante Ida + mandante Volta vs visitante Ida + visitante Volta
    const hScore = (ida?.homeScore || 0) + (volta?.homeScore || 0);
    const aScore = (ida?.awayScore || 0) + (volta?.awayScore || 0);

    if (hScore > aScore) return { name: ida.homeTeam, code: ida.homeTeamCode, flag: ida.homeTeamFlag, crest: ida.homeTeamCrest };
    if (aScore > hScore) return { name: ida.awayTeam, code: ida.awayTeamCode, flag: ida.awayTeamFlag, crest: ida.awayTeamCrest };

    // Empate no agregado (Pênaltis - simplificação: quem joga em casa na volta passa se não houver info)
    return null;
}

async function updateNextPhase(db, admin, champId, nextPhase, currentOrder, winner, isFinalWinner) {
    const matchesRef = db.collection("championships").doc(champId).collection("matches");
    let targetOrder = 0;
    let isHome = true;

    if (nextPhase === "QUARTERFINALS") {
        const mapping = { 1: [1, true], 2: [1, false], 3: [2, true], 4: [2, false], 5: [3, true], 6: [3, false], 7: [4, true], 8: [4, false] };
        if (mapping[currentOrder]) [targetOrder, isHome] = mapping[currentOrder];
    } else if (nextPhase === "SEMIFINALS") {
        const mapping = { 1: [1, true], 2: [1, false], 3: [2, true], 4: [2, false] };
        if (mapping[currentOrder]) [targetOrder, isHome] = mapping[currentOrder];
    } else if (nextPhase === "FINAL") {
        const mapping = { 1: [1, true], 2: [1, false] };
        if (mapping[currentOrder]) [targetOrder, isHome] = mapping[currentOrder];
    }

    if (targetOrder > 0) {
        const matches = await matchesRef.where("phase", "==", nextPhase).where("matchOrder", "==", targetOrder).get();
        for (const doc of matches.docs) {
            const data = doc.data();
            // Na Volta (L2), o mando é sempre invertido em relação à Ida (L1).
            // Sem isso, Ida e Volta ficavam com os mesmos mandantes/visitantes.
            const isL2 = doc.id.endsWith("-L2");
            const effectiveIsHome = isL2 ? !isHome : isHome;

            // NÃO SOBRESCREVER se já tiver um time real definido (pela API ou Manualmente)
            if (effectiveIsHome && data.homeTeamCode && data.homeTeamCode !== "TBD") continue;
            if (!effectiveIsHome && data.awayTeamCode && data.awayTeamCode !== "TBD") continue;

            const updates = {
                isTemporary: !isFinalWinner,
                source: isFinalWinner ? "autoAdvance_confirmed" : "autoAdvance_provisional",
                lastUpdated: admin.firestore.FieldValue.serverTimestamp()
            };
            if (effectiveIsHome) {
                updates.homeTeam = winner.name;
                updates.homeTeamCode = isFinalWinner ? (winner.code || "TBD") : "TBD";
                updates.homeTeamFlag = winner.flag || "";
                updates.homeTeamCrest = winner.crest || null;
            } else {
                updates.awayTeam = winner.name;
                updates.awayTeamCode = isFinalWinner ? (winner.code || "TBD") : "TBD";
                updates.awayTeamFlag = winner.flag || "";
                updates.awayTeamCrest = winner.crest || null;
            }
            await doc.ref.update(updates);
        }
    }
}

module.exports = { advanceTeams, migratePredictionsIfMatchChanged };
