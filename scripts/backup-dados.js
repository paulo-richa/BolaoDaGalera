const admin = require("firebase-admin");
const fs = require("fs");

const serviceAccount = require("./serviceAccountKey.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function exportCollections() {
  const collections = ["predictions", "users", "boloes", "matches"];
  const backup = {};

  console.log("🚀 Iniciando Backup...");

  for (const colName of collections) {
    console.log(`📦 Baixando coleção: ${colName}...`);
    const snapshot = await db.collection(colName).get();
    backup[colName] = {};

    snapshot.forEach(doc => {
      backup[colName][doc.id] = doc.data();
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
