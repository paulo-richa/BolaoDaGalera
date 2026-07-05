const admin = require('firebase-admin');

// Tenta carregar a conta de serviço do ambiente ou de um arquivo
let serviceAccount;
try {
    serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
} catch (e) {
    console.error("ERRO: Defina a variável de ambiente FIREBASE_SERVICE_ACCOUNT com o conteúdo do seu JSON de credenciais.");
    process.exit(1);
}

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function fix() {
    console.log("🚀 Forçando correção no Firestore...");
    const matchesRef = db.collection('matches');

    try {
        // Canadá 0 x 2 Marrocos (ENCERRADO)
        await matchesRef.doc('KO-16-1').set({
            homeScore: 0,
            awayScore: 2,
            status: 'FINISHED',
            isManual: true,
            lastUpdate: new Date().toISOString()
        }, { merge: true });
        console.log("✅ KO-16-1 (Canadá) atualizado para 0x2 FINISHED");

        // França 3 x 0 Paraguai (EM ANDAMENTO)
        await matchesRef.doc('KO-16-2').set({
            homeScore: 0,
            awayScore: 3,
            status: 'IN_PLAY',
            isManual: true,
            lastUpdate: new Date().toISOString()
        }, { merge: true });
        console.log("✅ KO-16-2 (França) atualizado para 0x3 IN_PLAY");

        // Limpeza de possíveis duplicatas numéricas
        const numericDocs = ['537376', '537375'];
        for (const id of numericDocs) {
            await matchesRef.doc(id).delete();
            console.log(`🗑️ Documento numérico deletado (se existia): ${id}`);
        }

        console.log("🏁 Tudo pronto! Verifique o app agora.");
    } catch (error) {
        console.error("❌ Erro ao atualizar:", error.message);
    }
}

fix();
