const { onDocumentCreated, onDocumentWritten } = require("firebase-functions/v2/firestore");
const { notifyUser } = require("./notifications");

/**
 * inviteeIdentifier is the email, username (lowercase), or phone number
 * (digits only) the inviter typed in (AddParticipantsScreen) - never the
 * uid directly. Resolves to a uid by trying each field, in the same order
 * the client already uses to look up invitations (see
 * HomeViewModel.loadUserData).
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
        // Invitation for someone without an account yet (or an identifier
        // matching no field) - the invitation still works via code/link,
        // there's just no push to send.
        if (!userId) return;

        await notifyUser(db, admin, userId, {
            title: "Novo convite! 📩",
            message: `${inviterName || "Alguém"} te convidou para o bolão "${bolaoName}".`,
            type: "INVITATION",
            bolaoId
        });
    });

    // Simple before/after diff: only what's newly appeared in the array
    // triggers a notification. This keeps the trigger idempotent even
    // when firing on every write to the bolao (approval, edit, etc.) - a
    // uid only "appears" once, when it's added.
    function newEntries(beforeList, afterList) {
        const before = new Set(beforeList || []);
        return (afterList || []).filter((id) => !before.has(id));
    }

    const onBolaoUpdated = onDocumentWritten("boloes/{bolaoId}", async (event) => {
        const afterData = event.data?.after?.data();
        if (!afterData) return; // bolao deleted, nothing to notify

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
