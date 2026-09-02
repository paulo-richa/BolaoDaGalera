const admin = require("firebase-admin");
const fs = require("fs");

const serviceAccount = require("./serviceAccountKey.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function exportCollections() {
  const collections = ["users", "boloes"];
  const backup = {};

  console.log("🚀 Iniciando Backup...");

  // 1. Top-level collections
  for (const colName of collections) {
    console.log(`📦 Baixando coleção: ${colName}...`);
    const snapshot = await db.collection(colName).get();
    backup[colName] = {};

    snapshot.forEach(doc => {
      backup[colName][doc.id] = doc.data();
    });
  }

  // 1.1 Predictions inside boloes (subcollections)
  console.log(`📦 Baixando palpites dentro de cada bolão...`);
  const boloesSnap = await db.collection("boloes").get();
  for (const bolaoDoc of boloesSnap.docs) {
    const bolaoId = bolaoDoc.id;
    if (!backup["boloes"][bolaoId]) continue;

    backup["boloes"][bolaoId].predictions = {};
    const predsSnap = await db.collection("boloes").doc(bolaoId).collection("predictions").get();
    predsSnap.forEach(pDoc => {
      backup["boloes"][bolaoId].predictions[pDoc.id] = pDoc.data();
    });
  }

  // 2. Championships and their subcollections (matches)
  console.log(`📦 Baixando campeonatos e subcoleções de jogos...`);
  backup["championships"] = {};
  const championshipsSnap = await db.collection("championships").get();

  for (const champDoc of championshipsSnap.docs) {
    const champId = champDoc.id;
    backup["championships"][champId] = {
      ...champDoc.data(),
      matches: {}
    };

    const matchesSnap = await db.collection("championships").doc(champId).collection("matches").get();
    matchesSnap.forEach(matchDoc => {
      backup["championships"][champId].matches[matchDoc.id] = matchDoc.data();
    });
  }

  // 3. Legacy matches collection (if it still exists)
  const oldMatchesSnap = await db.collection("matches").get();
  if (!oldMatchesSnap.empty) {
    console.log(`⚠️ Backup da antiga coleção 'matches' (ainda contém dados)...`);
    backup["old_matches"] = {};
    oldMatchesSnap.forEach(doc => {
      backup["old_matches"][doc.id] = doc.data();
    });
  }

  // 4. Legacy predictions collection (if it still exists)
  const oldPredictionsSnap = await db.collection("predictions").get();
  if (!oldPredictionsSnap.empty) {
    console.log(`⚠️ Backup da antiga coleção 'predictions' (ainda contém dados)...`);
    backup["old_predictions"] = {};
    oldPredictionsSnap.forEach(doc => {
      backup["old_predictions"][doc.id] = doc.data();
    });
  }

  const fileName = `backup_bolao_${new Date().toISOString().split('T')[0]}.json`;
  fs.writeFileSync(fileName, JSON.stringify(backup, null, 2));

  console.log(`✅ Backup concluído com sucesso: ${fileName}`);
  process.exit();
}

exportCollections().catch(err => {
  console.error("❌ Erro no backup:", err);
  process.exit(1);
});
