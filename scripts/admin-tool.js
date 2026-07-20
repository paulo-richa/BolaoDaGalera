const admin = require("firebase-admin");
const axios = require("axios");

// Inicialização do Firebase Admin (ajuste o caminho se necessário)
const serviceAccount = require("./serviceAccountKey.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

/**
 * Script de administração para forçar placares.
 * Agora suporta subcollections: championships/{id}/matches
 */
async function fixMatchScore(championshipId, matchId, homeScore, awayScore, status = 'FINISHED') {
  console.log(`📦 Ajustando ${matchId} em ${championshipId}...`);
  const matchRef = db.collection('championships').document(championshipId).collection('matches').doc(matchId);

  await matchRef.set({
    homeScore,
    awayScore,
    status,
    isManual: true,
    lastSync: admin.firestore.FieldValue.serverTimestamp()
  }, { merge: true });

  console.log(`✅ ${matchId} fixado com ${homeScore}x${awayScore}`);
}

async function run() {
  // Exemplo de uso:
  // await fixMatchScore('LIBERTADORES', 'CLI-M12345', 2, 1);

  console.log("🏁 Operação concluída.");
  process.exit();
}

run();
