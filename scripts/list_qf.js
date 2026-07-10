const admin = require("firebase-admin");

admin.initializeApp({
    projectId: "bolaodagalera-bb002"
});

const db = admin.firestore();

async function list() {
    console.log("--- LISTING QUARTER FINALS ---");
    const snap = await db.collection("matches").get();

    snap.forEach(doc => {
        const d = doc.data();
        if (d.phase === "QUARTERFINALS" || doc.id.includes("QF")) {
            console.log(`ID: ${doc.id} | ${d.homeTeam} vs ${d.awayTeam} | Phase: ${d.phase} | Group: ${d.group}`);
        }
    });
    process.exit();
}

list().catch(console.error);
