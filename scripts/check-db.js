const admin = require("firebase-admin");

// Usando o SDK default ou service account se disponível
if (process.env.GOOGLE_APPLICATION_CREDENTIALS || true) {
    admin.initializeApp({
        projectId: "bolaodagalera-bb002"
    });
}

const db = admin.firestore();

async function check() {
    console.log("--- Verificando Grupo Stage ---");
    const groups = await db.collection("matches")
        .where("id", ">=", "GS-")
        .where("id", "<=", "GS-Z")
        .get();
    
    let allFinished = true;
    let maxDate = 0;

    groups.forEach(doc => {
        const data = doc.data();
        if (data.status !== "FINISHED") {
            allFinished = false;
            console.log("Jogo pendente:", data.id, "Status:", data.status);
        }
        if (data.matchDateMillis > maxDate) maxDate = data.matchDateMillis;
    });

    console.log("Todos os grupos finalizados no DB:", allFinished);
    console.log("Data do último jogo de grupo (millis):", maxDate);
    console.log("Data atual do servidor (millis):", Date.now());
    
    console.log("\n--- Verificando Jogo Específico (Brasil x Noruega) ---");
    const brMatch = await db.collection("matches").doc("KO-16-5").get();
    if (brMatch.exists) {
        console.log("KO-16-5:", JSON.stringify(brMatch.data(), null, 2));
    } else {
        console.log("KO-16-5 não existe no banco!");
    }
    
    process.exit();
}

check().catch(console.error);
