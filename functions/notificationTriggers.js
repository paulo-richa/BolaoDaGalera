const { onDocumentCreated, onDocumentWritten } = require("firebase-functions/v2/firestore");
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

    // Diff simples entre before/after: só o que apareceu de novo no array
    // vira notificação. Assim o gatilho fica idempotente mesmo disparando em
    // toda escrita do bolão (aprovação, edição, etc.) - um uid só "aparece"
    // uma vez, quando é adicionado.
    function newEntries(beforeList, afterList) {
        const before = new Set(beforeList || []);
        return (afterList || []).filter((id) => !before.has(id));
    }

    const onBolaoUpdated = onDocumentWritten("boloes/{bolaoId}", async (event) => {
        const afterData = event.data?.after?.data();
        if (!afterData) return; // bolão apagado, nada a notificar

        const beforeData = event.data?.before?.data();
        const bolaoId = event.params.bolaoId;
        const bolaoName = afterData.name || "seu bolão";
        const ownerId = afterData.ownerId;
        if (!ownerId) return;

        const newJoinRequests = newEntries(beforeData?.pendingParticipants, afterData.pendingParticipants);
        for (const requesterId of newJoinRequests) {
            await notifyUser(db, admin, ownerId, {
                title: "Pedido para entrar 👤",
                message: `Alguém quer entrar no seu bolão "${bolaoName}".`,
                type: "JOIN_REQUEST",
                bolaoId,
                matchId: requesterId,
                deepLink: `bolaodagalera://bolao?bolaoId=${bolaoId}`
            });
        }

        const newExitRequests = newEntries(beforeData?.pendingExits, afterData.pendingExits);
        for (const requesterId of newExitRequests) {
            await notifyUser(db, admin, ownerId, {
                title: "Pedido para sair 🚩",
                message: `Alguém quer sair do seu bolão "${bolaoName}".`,
                type: "EXIT_REQUEST",
                bolaoId,
                matchId: requesterId,
                deepLink: `bolaodagalera://bolao?bolaoId=${bolaoId}`
            });
        }
    });

    return { onInvitationCreated, onBolaoUpdated };
}

module.exports = { makeNotificationTriggers };
