const { API_KEY } = require("./config");
const { BR_TEAMS } = require("./teams_br");
const { logger } = require("firebase-functions");

async function syncBrasileirao(db, admin, axios) {
    logger.info("Iniciando sincronização inteligente do Brasileirão...");
    const matchesRef = db.collection("championships").doc("BRASILEIRAO").collection("matches");

    try {
        // 1. Obter detalhes da competição para saber a rodada atual
        const compRes = await axios.get("https://api.football-data.org/v4/competitions/BSA", {
            headers: { 'X-Auth-Token': API_KEY }
        }).catch(() => null);

        if (!compRes || !compRes.data || !compRes.data.currentSeason) {
            logger.error("Não foi possível obter os dados da competição BSA.");
            return;
        }

        const currentMatchday = compRes.data.currentSeason.currentMatchday;
        // 2. Sincronizar as últimas 3 rodadas (atual, anterior e a retrasada)
        // Isso cobre jogos adiados ou correções tardias da API
        const roundsToSync = [currentMatchday, currentMatchday - 1, currentMatchday - 2].filter(r => r > 0);

        // 3. BUSCA ADICIONAL: Verificar se existem jogos "travados" no nosso banco
        // Busca jogos que não estão FINISHED e nem POSTPONED nas últimas semanas
        const pendingMatchesSnapshot = await matchesRef
            .where('status', 'in', ['IN_PLAY', 'TIMED', 'LIVE'])
            .limit(10)
            .get();

        const pendingRounds = pendingMatchesSnapshot.docs.map(doc => {
            const data = doc.data();
            // Extrair a rodada do ID (ex: BSA-2026-R22-...) ou do campo se existir
            const match = doc.id.match(/-R(\d+)-/);
            return match ? parseInt(match[1]) : null;
        }).filter(r => r !== null && !roundsToSync.includes(r));

        // Unir as rodadas atuais com as rodadas que possuem jogos pendentes
        const allRoundsToSync = [...new Set([...roundsToSync, ...pendingRounds])];

        for (const rd of allRoundsToSync) {
            logger.info(`Sincronizando Rodada ${rd}...`);
            const resBSA = await axios.get(`https://api.football-data.org/v4/competitions/BSA/matches?matchday=${rd}`, {
                headers: { 'X-Auth-Token': API_KEY },
                timeout: 15000
            }).catch(() => null);

            if (resBSA && resBSA.data && resBSA.data.matches) {
                const batch = db.batch();
                let hasUpdates = false;

                for (const m of resBSA.data.matches) {
                    const matchId = `BSA-2026-R${m.matchday}-${m.id}`;

                    // Verificar se os dados já existem
                    const doc = await matchesRef.doc(matchId).get();
                    const existing = doc.exists ? doc.data() : null;

                    // 1. Tratamento de Jogos Adiados, Cancelados ou Suspensos
                    // Se o jogo não vai acontecer agora, removemos do banco para limpar a rodada
                    if (['POSTPONED', 'CANCELLED', 'SUSPENDED'].includes(m.status)) {
                        if (existing) {
                            batch.delete(matchesRef.doc(matchId));
                            hasUpdates = true;
                            logger.info(`Limpando jogo ${m.status}: ${matchId} (${m.homeTeam.name} x ${m.awayTeam.name})`);
                        }
                        continue;
                    }

                    const s = m.score;
                    const hScore = s?.fullTime?.home ?? s?.regularTime?.home;
                    const aScore = s?.fullTime?.away ?? s?.regularTime?.away;

                    const newHScore = hScore !== undefined ? hScore : null;
                    const newAScore = aScore !== undefined ? aScore : null;

                    // Só adiciona ao batch se houver mudança de status ou placar
                    if (!existing || existing.status !== m.status || existing.homeScore !== newHScore || existing.awayScore !== newAScore) {
                        const hTeam = BR_TEAMS[m.homeTeam.name] || { name: m.homeTeam.name, flag: "", code: m.homeTeam.tla || "TBD", crest: null };
                        const aTeam = BR_TEAMS[m.awayTeam.name] || { name: m.awayTeam.name, flag: "", code: m.awayTeam.tla || "TBD", crest: null };

                        let homeCrest = hTeam.crest || (m.homeTeam.crest && !m.homeTeam.crest.includes("wikipedia") ? m.homeTeam.crest : null);
                        let awayCrest = aTeam.crest || (m.awayTeam.crest && !m.awayTeam.crest.includes("wikipedia") ? m.awayTeam.crest : null);

                        batch.set(matchesRef.doc(matchId), {
                            status: m.status,
                            homeTeam: hTeam.name, homeTeamCode: hTeam.code, homeTeamFlag: hTeam.flag, homeTeamCrest: homeCrest,
                            awayTeam: aTeam.name, awayTeamCode: aTeam.code, awayTeamFlag: aTeam.flag, awayTeamCrest: awayCrest,
                            homeScore: newHScore,
                            awayScore: newAScore,
                            championshipId: "BRASILEIRAO",
                            lastSync: admin.firestore.FieldValue.serverTimestamp()
                        }, { merge: true });
                        hasUpdates = true;
                    }
                }

                if (hasUpdates) {
                    await batch.commit();
                    logger.info(`Rodada ${rd} atualizada com sucesso.`);
                } else {
                    logger.info(`Rodada ${rd} sem alterações.`);
                }
            }
        }
    } catch (e) {
        logger.error("Erro na sincronização do Brasileirão:", e.message);
    }
}

module.exports = { syncBrasileirao };
