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
    timeoutSeconds: 120
}, async (event) => {
    try {
        const matchesRef = db.collection('matches');

        // 1. LIMPEZA DE LIXO
        const snapshot = await matchesRef.get();
        for (const doc of snapshot.docs) {
            if (/^\d+$/.test(doc.id)) {
                await doc.ref.delete();
            }
        }

        // 2. CORREÇÕES MANUAIS (Fixando placares e travando isManual)
        const baseTime = 1783260000000;

        // Paraguai 0 x 1 França (ENCERRADO - Oitavas)
        await matchesRef.doc('KO-16-1').set({
            homeScore: 0, awayScore: 1, status: 'FINISHED', isManual: true
        }, { merge: true });

        // Canadá 0 x 3 Marrocos (ENCERRADO - Oitavas)
        await matchesRef.doc('KO-16-2').set({
            homeScore: 0, awayScore: 3, status: 'FINISHED', isManual: true
        }, { merge: true });

        // França 3 x 0 Suécia (ENCERRADO - 16 avos)
        await matchesRef.doc('KO-32-2').set({
            homeScore: 3, awayScore: 0, status: 'FINISHED', isManual: true
        }, { merge: true });

        // África do Sul 0 x 1 Canadá (ENCERRADO - 16 avos)
        await matchesRef.doc('KO-32-3').set({
            homeScore: 0, awayScore: 1, status: 'FINISHED', isManual: true
        }, { merge: true });

        // Alemanha x Paraguai (KO-32-1) - Ajuste para ignorar pênaltis (ex: 1x1)
        // Se precisar de um placar exato para este também, me avise.
        // Por enquanto a API vai calcular ignorando pênaltis para os outros.

        // 3. API SYNC com Lógica de "Ignorar Pênaltis"
        const res = await axios.get("https://api.football-data.org/v4/competitions/WC/matches", {
            headers: { 'X-Auth-Token': API_KEY },
            timeout: 10000
        });

        const matches = res.data.matches || [];
        const apiToInternal = {
            "537415": "KO-32-1", "537416": "KO-32-2", "537417": "KO-32-3", "537418": "KO-32-4",
            "537419": "KO-32-5", "537420": "KO-32-6", "537421": "KO-32-7", "537422": "KO-32-8",
            "537423": "KO-32-9", "537424": "KO-32-10", "537425": "KO-32-11", "537426": "KO-32-12",
            "537427": "KO-32-13", "537428": "KO-32-14", "537429": "KO-32-15", "537430": "KO-32-16",
            "537379": "KO-16-3", "537380": "KO-16-4", "537377": "KO-16-5", "537378": "KO-16-6",
            "537381": "KO-16-7", "537382": "KO-16-8", "537383": "KO-QF-1", "537384": "KO-QF-2",
            "537385": "KO-QF-3", "537386": "KO-QF-4", "537387": "KO-SF-1", "537388": "KO-SF-2",
            "537389": "KO-SF-3", "537390": "KO-FINAL"
        };

        for (const m of matches) {
            const id = apiToInternal[m.id.toString()];
            if (!id || id === 'KO-16-1' || id === 'KO-16-2' || id === 'KO-32-2' || id === 'KO-32-3') continue;

            const docRef = matchesRef.doc(id);
            const doc = await docRef.get();

            if (doc.exists && !doc.data().isManual) {
                const s = m.score;
                let h, a;

                if (s.duration === "PENALTY_SHOOTOUT" || s.penalties) {
                    h = (s.regularTime?.home ?? 0) + (s.extraTime?.home ?? 0);
                    a = (s.regularTime?.away ?? 0) + (s.extraTime?.away ?? 0);
                } else {
                    h = s.fullTime?.home ?? 0;
                    a = s.fullTime?.away ?? 0;
                }

                await docRef.update({
                    homeScore: h,
                    awayScore: a,
                    status: m.status
                });
            }
        }

    } catch (e) {
        logger.error("Erro:", e.message);
    }
});
