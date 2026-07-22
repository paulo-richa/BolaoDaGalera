const { API_KEY } = require("./config");
const { TEAM_DATA } = require("./teams");
const { mapPhase } = require("./utils");
const { logger } = require("firebase-functions");

async function syncLibertadores(db, admin, axios) {
    logger.info("Iniciando sincronização da Libertadores...");
    const matchesRef = db.collection("championships").doc("LIBERTADORES").collection("matches");

    try {
        // 1. GARANTIR PLACEHOLDERS DO MATA-MATA (Apenas se não existirem)
        const createKnockout = async (phase, count, dates, prefix) => {
            for (let i = 1; i <= count; i++) {
                const idaId = `CLI-2026-${prefix}${i}-L1`;
                const voltaId = `CLI-2026-${prefix}${i}-L2`;

                const baseData = {
                    homeTeam: `Vencedor ${prefix}${i} (Ida)`,
                    awayTeam: `Vencedor ${prefix}${i} (Volta)`,
                    homeTeamCode: "TBD", awayTeamCode: "TBD",
                    homeTeamFlag: "🏳️", awayTeamFlag: "🏳️",
                    championshipId: "LIBERTADORES",
                    phase: phase,
                    matchOrder: i,
                    status: "SCHEDULED"
                };

                const docIda = await matchesRef.doc(idaId).get();
                if (!docIda.exists) {
                    await matchesRef.doc(idaId).set({ ...baseData, matchDateMillis: dates[i-1] }, { merge: true });
                }
                const docVolta = await matchesRef.doc(voltaId).get();
                if (!docVolta.exists) {
                    await matchesRef.doc(voltaId).set({
                        ...baseData,
                        homeTeam: baseData.awayTeam,
                        awayTeam: baseData.homeTeam,
                        matchDateMillis: dates[i-1] + (7 * 24 * 3600000)
                    }, { merge: true });
                }
            }
        };

        await createKnockout("QUARTERFINALS", 4, [1788825600000, 1788912000000, 1788998400000, 1789084800000], "QF");
        await createKnockout("SEMIFINALS", 2, [1791244800000, 1791331200000], "SF");

        const finalId = "CLI-2026-FINAL";
        const docFinal = await matchesRef.doc(finalId).get();
        if (!docFinal.exists) {
            await matchesRef.doc(finalId).set({
                homeTeam: "Finalista 1", awayTeam: "Finalista 2", homeTeamCode: "TBD", awayTeamCode: "TBD",
                homeTeamFlag: "🏳️", awayTeamFlag: "🏳️", matchDateMillis: 1793659200000,
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
            for (const m of resCLI.data.matches) {
                const isVolta = m.matchday === 2 || m.stage.includes("LEG2");
                const legSuffix = isVolta ? "-L2" : "-L1";
                const matchId = `CLI-2026-M${m.id}${legSuffix}`;

                const s = m.score;
                const hScore = s?.fullTime?.home ?? s?.regularTime?.home;
                const aScore = s?.fullTime?.away ?? s?.regularTime?.away;

                let hName = m.homeTeam.name || "";
                let aName = m.awayTeam.name || "";
                if (m.stage !== "GROUP_STAGE") {
                    if (hName === "" || hName.includes("Winner") || hName.includes("To Be Determined")) hName = "Vencedor Oitavas";
                    if (aName === "" || aName.includes("Winner") || aName.includes("To Be Determined")) aName = "Vencedor Oitavas";
                }

                const hTeam = TEAM_DATA[m.homeTeam.name] || { name: hName, flag: "🏳️", code: m.homeTeam.tla || "TBD" };
                const aTeam = TEAM_DATA[m.awayTeam.name] || { name: aName, flag: "🏳️", code: m.awayTeam.tla || "TBD" };

                batch.set(matchesRef.doc(matchId), {
                    status: m.status,
                    homeTeam: hTeam.name, homeTeamCode: hTeam.code, homeTeamFlag: hTeam.flag,
                    awayTeam: aTeam.name, awayTeamCode: aTeam.code, awayTeamFlag: aTeam.flag,
                    homeScore: hScore !== undefined ? hScore : null,
                    awayScore: aScore !== undefined ? aScore : null,
                    championshipId: "LIBERTADORES",
                    phase: mapPhase(m.stage),
                    group: m.group || (m.matchday ? `Rodada ${m.matchday}` : m.stage),
                    matchDateMillis: Date.parse(m.utcDate),
                    lastSync: admin.firestore.FieldValue.serverTimestamp()
                }, { merge: true });
            }
            await batch.commit();
            logger.info(`Libertadores sincronizada: ${resCLI.data.matches.length} jogos.`);
        }
    } catch (e) {
        logger.error("Erro na sincronização da Libertadores:", e.message);
    }
}

module.exports = { syncLibertadores };
