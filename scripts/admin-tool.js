const admin = require("firebase-admin");
const axios = require("axios");

// Firebase Admin initialization (adjust the path if needed)
const serviceAccount = require("./serviceAccountKey.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

/**
 * Administration script to force match scores.
 * Now supports subcollections: championships/{id}/matches
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
  // Usage example:
  // await fixMatchScore('LIBERTADORES', 'CLI-M12345', 2, 1);

  console.log("🏁 Operação concluída.");
  process.exit();
}

run();
