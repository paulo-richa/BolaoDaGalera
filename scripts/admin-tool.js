const admin = require("firebase-admin");
const axios = require("axios");

// Inicialização do Firebase Admin (ajuste o caminho se necessário)
const serviceAccount = require("./serviceAccountKey.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();
const API_KEY = "***REMOVED_SECRET***";

/**
 * Script de administração para forçar placares e sincronizar API localmente.
 */
async function runAdminTool() {
  console.log("🚀 Iniciando Admin Tool...");
  const matchesRef = db.collection('matches');

  // 1. APLICAR CORREÇÕES MANUAIS (Força o estado correto no Firestore)
  const manualFixes = {
    'KO-16-1': { homeScore: 0, awayScore: 1, status: 'FINISHED', isManual: true },
    'KO-16-2': { homeScore: 0, awayScore: 3, status: 'FINISHED', isManual: true },
    'KO-32-2': { homeScore: 3, awayScore: 0, status: 'FINISHED', isManual: true },
    'KO-32-3': { homeScore: 0, awayScore: 1, status: 'FINISHED', isManual: true }
  };

  console.log("📦 Aplicando correções manuais...");
  for (const [id, data] of Object.entries(manualFixes)) {
    await matchesRef.doc(id).set(data, { merge: true });
    console.log(`✅ ${id} fixado.`);
  }

  // 2. SINCRONIZAR RESTANTE VIA API
  console.log("🌐 Sincronizando outros jogos via API...");
  try {
    const res = await axios.get("https://api.football-data.org/v4/competitions/WC/matches", {
      headers: { 'X-Auth-Token': API_KEY }
    });

    const apiToInternal = {
        "537415": "KO-32-1", "537416": "KO-32-2", "537417": "KO-32-3", "537418": "KO-32-4",
        "537419": "KO-32-5", "537420": "KO-32-6", "537421": "KO-32-7", "537422": "KO-32-8",
        "537423": "KO-32-9", "537424": "KO-32-10", "537425": "KO-32-11", "537426": "KO-32-12",
        "537427": "KO-32-13", "537428": "KO-32-14", "537429": "KO-32-15", "537430": "KO-32-16",
        "537379": "KO-16-3", "537380": "KO-16-4", "537377": "KO-16-5", "537378": "KO-16-6",
        "537381": "KO-16-7", "537382": "KO-16-8"
    };

    for (const m of res.data.matches) {
      const id = apiToInternal[m.id.toString()];
      if (id && !manualFixes[id]) {
        const s = m.score;
        let h = (s.regularTime?.home ?? 0) + (s.extraTime?.home ?? 0);
        let a = (s.regularTime?.away ?? 0) + (s.extraTime?.away ?? 0);

        await matchesRef.doc(id).update({
          homeScore: h,
          awayScore: a,
          status: m.status
        });
        console.log(`🔄 ${id} atualizado via API.`);
      }
    }
  } catch (error) {
    console.error("❌ Erro na API:", error.message);
  }

  console.log("🏁 Operação concluída.");
  process.exit();
}

runAdminTool();
