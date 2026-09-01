const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { notifyUser } = require("./notifications");

/**
 * inviteeIdentifier é o e-mail, username (minúsculos) ou telefone (só
 * dígitos) que o convidante digitou (AddParticipantsScreen) - nunca o uid
 * direto. Resolve pra um uid tentando cada campo, na mesma ordem que o
 * client já usa pra procurar convites (ver HomeViewModel.loadUserData).
 */
async function findUserIdByIdentifier(db, identifier) {
    const usersRef = db.collection("users");
    for (const field of ["email", "username", "phone"]) {
        const snap = await usersRef.where(field, "==", identifier).limit(1).get();
        if (!snap.empty) return snap.docs[0].id;
    }
    return null;
}

function makeNotificationTriggers(db, admin) {
    const onInvitationCreated = onDocumentCreated("invitations/{inviteId}", async (event) => {
        const data = event.data?.data();
        if (!data) return;

        const { inviteeIdentifier, bolaoName, inviterName, bolaoId } = data;
        if (!inviteeIdentifier || !bolaoId) return;

        const userId = await findUserIdByIdentifier(db, inviteeIdentifier);
        // Convite pra alguém sem conta ainda (ou identifier que não bate com
        // nenhum campo) - o convite continua funcionando por código/link,
        // só não tem push pra mandar.
        if (!userId) return;

        await notifyUser(db, admin, userId, {
            title: "Novo convite! 📩",
            message: `${inviterName || "Alguém"} te convidou para o bolão "${bolaoName}".`,
            type: "INVITATION",
            bolaoId
        });
    });

    return { onInvitationCreated };
}

module.exports = { makeNotificationTriggers };
