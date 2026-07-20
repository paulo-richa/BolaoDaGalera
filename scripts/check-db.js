const admin = require("firebase-admin");

admin.initializeApp({
    projectId: "bolaodagalera-bb002"
});

const db = admin.firestore();

async function check(championshipId = "LIBERTADORES") {
    console.log(`--- Verificando Campeonato: ${championshipId} ---`);

    const matches = await db.collection("championships")
        .doc(championshipId)
        .collection("matches")
        .get();
    
    console.log(`Total de jogos encontrados: ${matches.size}`);

    matches.forEach(doc => {
        const data = doc.data();
        if (data.status !== "FINISHED") {
            console.log("Jogo pendente:", doc.id, "Status:", data.status, "Time:", data.homeTeam);
        }
    });

    process.exit();
}

check("LIBERTADORES").catch(console.error);
