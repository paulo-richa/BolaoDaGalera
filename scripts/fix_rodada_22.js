const admin = require("firebase-admin");

// Inicializa com o ID do projeto (ajuste se necessário)
if (admin.apps.length === 0) {
    admin.initializeApp({
        projectId: "bolaodagalera-bb002"
    });
}

const db = admin.firestore();

const matchesToFix = [
    { id: "BSA-2026-R22-554956", home: 2, away: 1 }, // Grêmio x São Paulo
    { id: "BSA-2026-R22-554958", home: 2, away: 2 }, // Remo x Atlético-MG
    { id: "BSA-2026-R22-554953", home: 2, away: 1 }, // Coritiba x Chapecoense
    { id: "BSA-2026-R22-554951", home: 1, away: 1 }, // Botafogo x Fluminense
    { id: "BSA-2026-R22-554954", home: 3, away: 1 }, // Cruzeiro x Mirassol
    { id: "BSA-2026-R22-554950", home: 0, away: 0 }, // Bahia x Vasco
    { id: "BSA-2026-R22-554957", home: 0, away: 0 }, // Palmeiras x Internacional
    { id: "BSA-2026-R22-554952", home: 0, away: 2 }, // Bragantino x Corinthians
    { id: "BSA-2026-R22-554959", home: 0, away: 2 }, // Santos x Athletico-PR
    { id: "BSA-2026-R22-554955", home: 2, away: 0 }  // Flamengo x Vitória
];

async function fix() {
    console.log("🚀 Iniciando correção da Rodada 22...");
    const batch = db.batch();
    const matchesRef = db.collection("championships").doc("BRASILEIRAO").collection("matches");

    for (const m of matchesToFix) {
        const docRef = matchesRef.doc(m.id);
        batch.update(docRef, {
            homeScore: m.home,
            awayScore: m.away,
            status: "FINISHED",
            lastSync: admin.firestore.FieldValue.serverTimestamp()
        });
        console.log(`✅ Preparado: ${m.id} -> ${m.home}x${m.away}`);
    }

    await batch.commit();
    console.log("✨ Todos os jogos da Rodada 22 foram atualizados e o ranking será recalculado!");
    process.exit();
}

fix().catch(err => {
    console.error("❌ Erro ao atualizar:", err);
    process.exit(1);
});
