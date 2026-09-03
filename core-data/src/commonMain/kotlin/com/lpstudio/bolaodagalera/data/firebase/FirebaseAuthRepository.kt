package com.lpstudio.bolaodagalera.data.firebase

import com.lpstudio.bolaodagalera.domain.model.User
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.appLogger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
private data class UserDto(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val nickname: String = "",
    val username: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseAuthRepository(private val crashReporter: CrashReporter) : AuthRepository {
    private val logger = appLogger("FirebaseAuthRepository")
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val usersCollection = db.collection("users")

    private var cachedUser: User? = null

    override val currentUser: User?
        get() = cachedUser ?: auth.currentUser?.let { User(it.uid, it.displayName ?: "Usuário", it.email ?: "", "", "", "") }

    override val authStateFlow: Flow<User?> =
        auth.authStateChanged.flatMapLatest { firebaseUser ->
            if (firebaseUser == null) {
                cachedUser = null
                flowOf(null)
            } else {
                try {
                    usersCollection.document(firebaseUser.uid).snapshots.map { doc ->
                        val user =
                            if (doc.exists) {
                                val dto = doc.data<UserDto>()
                                User(firebaseUser.uid, dto.name, dto.email, dto.phone, dto.nickname, dto.username)
                            } else {
                                User(firebaseUser.uid, firebaseUser.displayName ?: "Usuário", firebaseUser.email ?: "", "", "", "")
                            }
                        cachedUser = user
                        user as User?
                    }.catch { e ->
                        if (e is CancellationException) throw e
                        crashReporter.recordException(e, "Erro no snapshots de user")
                        logger.e(e) { "Erro no snapshots de user" }
                        emit(null)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    crashReporter.recordException(e, "Erro crítico ao iniciar snapshots de user")
                    logger.e(e) { "Erro crítico ao iniciar snapshots de user" }
                    flowOf(null)
                }
            }
        }.catch { e ->
            if (e is CancellationException) throw e
            crashReporter.recordException(e, "Erro no authStateFlow")
            logger.e(e) { "Erro no authStateFlow" }
            emit(null)
        }

    override suspend fun signIn(email: String, password: String): User {
        val result = auth.signInWithEmailAndPassword(email.trim(), password)
        val firebaseUser = result.user ?: error("Login falhou")
        val doc = usersCollection.document(firebaseUser.uid).get()
        val dto = doc.data<UserDto>()
        return User(firebaseUser.uid, dto.name, dto.email, dto.phone, dto.nickname, dto.username)
    }

    override suspend fun register(email: String, password: String, name: String, phone: String, nickname: String, username: String): User {
        val result =
            try {
                auth.createUserWithEmailAndPassword(email, password)
            } catch (e: Exception) {
                val msg = e.message?.lowercase() ?: ""
                // Check multiple error patterns for "email already in use"
                val isEmailInUse =
                    msg.contains("already-in-use") ||
                        msg.contains("already in use") ||
                        msg.contains("email-already") ||
                        msg.contains("collision")

                if (isEmailInUse) {
                    // Attempt sign-in; if the password is wrong, the ViewModel will catch the error
                    auth.signInWithEmailAndPassword(email, password)
                } else {
                    throw e
                }
            }

        val user = result.user ?: error("Cadastro falhou")
        try {
            user.updateProfile(displayName = name)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Best-effort: the Firestore profile write right below is the source of truth for the display name.
            logger.w(e) { "Falha ao atualizar displayName no Auth (não bloqueia o cadastro)" }
        }

        // Save or update the profile in Firestore
        usersCollection.document(user.uid).set(
            UserDto(name = name, email = user.email ?: "", phone = phone, nickname = nickname, username = username),
            merge = true
        )

        val finalUser = User(user.uid, name, user.email ?: "", phone, nickname, username)
        cachedUser = finalUser
        return finalUser
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun updateProfile(name: String, phone: String, nickname: String) {
        val firebaseUser = auth.currentUser ?: error("Usuário não autenticado")
        val uid = firebaseUser.uid
        val email = firebaseUser.email ?: ""

        // 1. Try updating the name in Auth (optional, doesn't block on failure)
        try {
            firebaseUser.updateProfile(displayName = name)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Best-effort: the Firestore write right below is the source of truth for the display name.
            logger.w(e) { "Falha ao atualizar displayName no Auth (não bloqueia a atualização de perfil)" }
        }

        // 2. Prepare the data for Firestore
        val updateMap =
            mutableMapOf<String, Any>(
                "name" to name,
                "phone" to phone,
                "nickname" to nickname,
                "email" to email
            )

        // 3. Write to Firestore using set with merge (more resilient than update)
        usersCollection.document(uid).set(updateMap, merge = true)

        // Update the local cache so the UI reflects the change immediately, preserving the current username
        val currentUsername = cachedUser?.username ?: ""
        cachedUser = User(uid, name, email, phone, nickname, currentUsername)
    }

    override suspend fun isEmailInUse(email: String): Boolean = try {
        // Prefer the Auth method, which doesn't require Firestore permissions
        val methods = auth.fetchSignInMethodsForEmail(email)
        methods.isNotEmpty()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        // Fall back to Firestore only if Auth fails or is unavailable
        try {
            val snapshot = usersCollection.where { "email" equalTo email }.get()
            !snapshot.documents.isEmpty()
        } catch (e2: CancellationException) {
            throw e2
        } catch (e2: Exception) {
            // If both fail, rethrow the original exception for the caller to handle -
            // a network/permission failure here must never be reported as "not in use".
            throw e
        }
    }

    override suspend fun isPhoneInUse(phone: String): Boolean {
        if (phone.isBlank()) return false
        val snapshot = usersCollection.where { "phone" equalTo phone }.get()
        return !snapshot.documents.isEmpty()
    }

    override suspend fun isNicknameInUse(nickname: String): Boolean {
        if (nickname.isBlank()) return false
        val snapshot = usersCollection.where { "nickname" equalTo nickname }.get()
        return !snapshot.documents.isEmpty()
    }

    override suspend fun isUsernameInUse(username: String): Boolean {
        if (username.isBlank()) return false
        val snapshot = usersCollection.where { "username" equalTo username.lowercase() }.get()
        return !snapshot.documents.isEmpty()
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email.trim())
    }

    override suspend fun getUser(userId: String): User? {
        val doc = usersCollection.document(userId).get()
        return if (doc.exists) {
            val dto = doc.data<UserDto>()
            User(userId, dto.name, dto.email, dto.phone, dto.nickname, dto.username)
        } else {
            null
        }
    }

    // Fetch each user in parallel instead of sequentially - a bolão with many
    // participants re-runs this on every match/prediction snapshot, and reading
    // one at a time multiplied the latency by the number of people.
    override suspend fun getUsers(userIds: List<String>): List<User> {
        if (userIds.isEmpty()) return emptyList()
        return coroutineScope {
            userIds.map { async { getUser(it) } }.mapNotNull { it.await() }
        }
    }
}
