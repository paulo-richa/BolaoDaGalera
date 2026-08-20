const { API_KEY } = require("./config");
const { LIB_TEAMS } = require("./teams_lib");
const { mapPhase } = require("./utils");
const { logger } = require("firebase-functions");

async function syncLibertadores(db, admin, axios) {
    logger.info("Iniciando sincronização da Libertadores...");
    const matchesRef = db.collection("championships").doc("LIBERTADORES").collection("matches");

    try {
        // 1. GARANTIR PLACEHOLDERS DO MATA-MATA (QF, SF, FINAL)
        const createKnockout = async (phase, count, dates, prefix) => {
            for (let i = 1; i <= count; i++) {
                const idaId = `CLI-2026-${prefix}${i}-L1`;
                const voltaId = `CLI-2026-${prefix}${i}-L2`;

                const doc = await matchesRef.doc(idaId).get();
                if (doc.exists && doc.data().isManual) continue;

                const baseData = {
                    homeTeam: "A definir", awayTeam: "A definir", homeTeamCode: "TBD", awayTeamCode: "TBD",
                    homeTeamFlag: "", awayTeamFlag: "", championshipId: "LIBERTADORES", phase: phase, matchOrder: i, status: "SCHEDULED"
                };
                await matchesRef.doc(idaId).set({ ...baseData, matchDateMillis: dates[i-1] }, { merge: true });
                await matchesRef.doc(voltaId).set({ ...baseData, matchDateMillis: dates[i-1] + (7 * 24 * 3600000) }, { merge: true });
            }
        };

        await createKnockout("QUARTERFINALS", 4, [1788825600000, 1788912000000, 1788998400000, 1789084800000], "QF");
        await createKnockout("SEMIFINALS", 2, [1791244800000, 1791331200000], "SF");

        const finalDoc = await matchesRef.doc("CLI-2026-FINAL").get();
        if (!finalDoc.exists || !finalDoc.data().isManual) {
            await matchesRef.doc("CLI-2026-FINAL").set({
                homeTeam: "A definir", awayTeam: "A definir", homeTeamCode: "TBD", awayTeamCode: "TBD",
                homeTeamFlag: "", awayTeamFlag: "", matchDateMillis: 1793659200000,
                phase: "FINAL", championshipId: "LIBERTADORES", matchOrder: 1, status: "SCHEDULED"
            }, { merge: true });
        }

        // 2. SINCRONIZAÇÃO COM A API
        const resCLI = await axios.get("https://api.football-data.org/v4/competitions/CLI/matches", {
            headers: { 'X-Auth-Token': API_KEY },
            timeout: 15000
        }).catch(() => null);

        if (resCLI && resCLI.data && resCLI.data.matches) {
            const batch = db.batch();
            const now = Date.now();
            const knockoutPairs = {};

            resCLI.data.matches.forEach(m => {
                if ((m.stage === "PLAY_OFFS" || m.stage === "ROUND_OF_16") && m.homeTeam.name && m.awayTeam.name) {
                    const pairKey = [m.homeTeam.name, m.awayTeam.name].sort().join("|");
                    if (!knockoutPairs[pairKey]) knockoutPairs[pairKey] = [];
                    knockoutPairs[pairKey].push(m);
                }
            });

            const allKnockoutMatches = resCLI.data.matches
                .filter(m => m.stage === "ROUND_OF_16" || m.stage === "PLAY_OFFS")
                .sort((a, b) => a.id - b.id);

            for (const m of resCLI.data.matches) {
                if (m.stage !== "GROUP_STAGE" && m.stage !== "ROUND_OF_16" && m.stage !== "PLAY_OFFS") continue;

                const isKnockout = m.stage === "ROUND_OF_16" || m.stage === "PLAY_OFFS";
                let isVolta = m.matchday === 2 || (m.stage && m.stage.includes("LEG2"));
                if (isKnockout && !isVolta && m.homeTeam.name && m.awayTeam.name) {
                    const pair = knockoutPairs[[m.homeTeam.name, m.awayTeam.name].sort().join("|")];
                    if (pair && pair.length === 2 && pair[1].id === m.id) isVolta = true;
                }

                const matchId = `CLI-2026-M${m.id}${isKnockout ? (isVolta ? "-L2" : "-L1") : ""}`;

                // VERIFICAÇÃO DE EDIÇÃO MANUAL (EXISTENTE)
                const currentDoc = await matchesRef.doc(matchId).get();
                if (currentDoc.exists && currentDoc.data().isManual) {
                    continue;
                }

                if (['POSTPONED', 'CANCELLED', 'SUSPENDED'].includes(m.status)) {
                    batch.delete(matchesRef.doc(matchId));
                    continue;
                }

                const s = m.score;
                // LÓGICA DE PLACAR: Priorizar regularTime para pegar os 90 minutos
                let hScore, aScore;
                if (s && s.duration === "REGULAR") {
                    hScore = s.fullTime.home;
                    aScore = s.fullTime.away;
                } else if (s && (s.duration === "PENALTY_SHOOTOUT" || s.duration === "EXTRA_TIME")) {
                    hScore = s.regularTime.home;
                    aScore = s.regularTime.away;
                } else {
                    hScore = s?.fullTime?.home;
                    aScore = s?.fullTime?.away;
                }

                const matchTime = Date.parse(m.utcDate);
                let targetStatus = m.status;
                if (m.status !== "FINISHED" && (now - matchTime > 4 * 3600000) && hScore !== undefined && aScore !== undefined) {
                    targetStatus = "FINISHED";
                }

                const hTeam = LIB_TEAMS[m.homeTeam.name] || { name: m.homeTeam.name, flag: "", code: m.homeTeam.tla || "TBD", crest: null };
                const aTeam = LIB_TEAMS[m.awayTeam.name] || { name: m.awayTeam.name, flag: "", code: m.awayTeam.tla || "TBD", crest: null };

                batch.set(matchesRef.doc(matchId), {
                    status: targetStatus,
                    homeTeam: hTeam.name, homeTeamCode: hTeam.code, homeTeamFlag: hTeam.flag, homeTeamCrest: hTeam.crest || m.homeTeam.crest,
                    awayTeam: aTeam.name, awayTeamCode: aTeam.code, awayTeamFlag: aTeam.flag, awayTeamCrest: aTeam.crest || m.awayTeam.crest,
                    homeScore: hScore !== undefined ? hScore : null,
                    awayScore: aScore !== undefined ? aScore : null,
                    championshipId: "LIBERTADORES",
                    phase: mapPhase(m.stage),
                    matchOrder: isKnockout ? allKnockoutMatches.findIndex(x => x.id === m.id) + 1 : (m.matchday || 0),
                    group: m.group || (m.matchday ? `Rodada ${m.matchday}` : m.stage),
                    matchDateMillis: matchTime,
                    lastSync: admin.firestore.FieldValue.serverTimestamp()
                }, { merge: true });
            }
            await batch.commit();
            logger.info(`Libertadores sincronizada.`);
        }
    } catch (e) {
        logger.error("Erro na sincronização da Libertadores:", e.message);
    }
}

module.exports = { syncLibertadores };
