const { onSchedule } = require("firebase-functions/v2/scheduler");
const { logger } = require("firebase-functions");
const admin = require("firebase-admin");
const axios = require("axios");

admin.initializeApp();
const db = admin.firestore();
const API_KEY = "***REMOVED_SECRET***";

exports.syncScores = onSchedule({
    schedule: "every 1 minutes",
    timeZone: "America/Sao_Paulo",
    region: "southamerica-east1",
    memory: "256MiB",
    timeoutSeconds: 60
}, async (event) => {
    try {
        const matchesRef = db.collection('matches');
        const now = Date.now();

        // Busca simplificada para evitar timeout no Firestore
        const activeSnap = await matchesRef.where('status', 'in', ['IN_PLAY', 'PAUSED', 'EXTRA_TIME', 'PENALTIES', 'LIVE', 'PAUSED_EXTRA_TIME', 'PAUSED_PENALTIES']).get();
        const hasActiveMatch = !activeSnap.empty;

        if (!hasActiveMatch) {
            const config = await db.collection('config').doc('sync_status').get();
            if (config.exists && (now - config.data().lastFullSync < 25 * 60 * 1000)) return;
        }

        const res = await axios.get("https://api.football-data.org/v4/competitions/WC/matches", {
            headers: { 'X-Auth-Token': API_KEY },
            timeout: 10000
        });

        const matches = res.data.matches || [];
        for (const m of matches) {
            await updateMatch(m, matches);
        }

        await db.collection('config').doc('sync_status').set({ lastFullSync: now }, { merge: true });
    } catch (e) {
        logger.error("Erro:", e.message);
    }
});

async function updateMatch(apiMatch, allMatches) {
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

    const id = apiToInternal[apiMatch.id.toString()];
    if (!id) return;

    const doc = await db.collection('matches').doc(id).get();
    if (!doc.exists) return;
    const data = doc.data();

    let status = apiMatch.status;
    if (status === "IN_PLAY") {
        if (apiMatch.score.duration === "EXTRA_TIME") status = "EXTRA_TIME";
        else if (apiMatch.score.duration === "PENALTY_SHOOTOUT") status = "PENALTIES";
    } else if (status === "PAUSED") {
        if (apiMatch.score.duration === "EXTRA_TIME") status = "PAUSED_EXTRA_TIME";
        else if (apiMatch.score.duration === "PENALTY_SHOOTOUT") status = "PAUSED_PENALTIES";
    }

    let h, a;
    if (apiMatch.score.duration === "PENALTY_SHOOTOUT") {
        h = (apiMatch.score.regularTime?.home ?? 0) + (apiMatch.score.extraTime?.home ?? 0);
        a = (apiMatch.score.regularTime?.away ?? 0) + (apiMatch.score.extraTime?.away ?? 0);
    } else {
        h = apiMatch.score.fullTime?.home ?? 0;
        a = apiMatch.score.fullTime?.away ?? 0;
    }

    const upd = {};
    if (h !== data.homeScore) upd.homeScore = h;
    if (a !== data.awayScore) upd.awayScore = a;
    if (status !== data.status) upd.status = status;

    if (Object.keys(upd).length > 0) {
        await doc.ref.update(upd);
    }
}
