package com.lpstudio.bolaodagalera.data.firebase

import com.lpstudio.bolaodagalera.domain.model.User
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.appLogger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val SAVE_PROFILE_MAX_ATTEMPTS = 3
private const val SAVE_PROFILE_RETRY_DELAY_MILLIS = 800L

// Public (no auth) Cloud Function - see the comment on checkIdentifierAvailability
// in functions/index.js for why this exists instead of a direct Firestore query.
private const val CHECK_IDENTIFIER_AVAILABILITY_URL =
    "https://us-central1-bolaodagalera-bb002.cloudfunctions.net/checkIdentifierAvailability"

@Serializable
private data class AvailabilityResponse(val inUse: Boolean = false)

// The response also carries a "status" field this repository doesn't need.
private val lenientJson = Json { ignoreUnknownKeys = true }

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
    private val httpClient = HttpClient()

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

        // Save or update the profile in Firestore. Right after createUserWithEmailAndPassword,
        // the ID token backing this write hasn't always propagated to the Firestore client yet -
        // security rules require request.auth.uid == userId, and that check can transiently see
        // no/stale auth on the first attempt, failing with a permission-denied that resolves on
        // its own a moment later. Retrying a couple of times covers that race instead of forcing
        // the new user to tap "criar conta" again themselves.
        saveUserProfileWithRetry(
            user.uid,
            UserDto(name = name, email = user.email ?: "", phone = phone, nickname = nickname, username = username)
        )

        val finalUser = User(user.uid, name, user.email ?: "", phone, nickname, username)
        cachedUser = finalUser
        return finalUser
    }

    /** See the comment at the register() call site for why this retries on permission-denied. */
    private suspend fun saveUserProfileWithRetry(uid: String, dto: UserDto) {
        var attempt = 0
        while (true) {
            try {
                usersCollection.document(uid).set(dto, merge = true)
                return
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                attempt++
                val isPermissionError = e.message?.lowercase()?.contains("permission") == true
                if (!isPermissionError || attempt >= SAVE_PROFILE_MAX_ATTEMPTS) throw e
                logger.w(e) { "Permissão negada ao salvar perfil (tentativa $attempt), tentando de novo" }
                delay(SAVE_PROFILE_RETRY_DELAY_MILLIS)
            }
        }
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
        return checkIdentifierAvailability("phone", phone)
    }

    override suspend fun isNicknameInUse(nickname: String): Boolean {
        if (nickname.isBlank()) return false
        return checkIdentifierAvailability("nickname", nickname)
    }

    override suspend fun isUsernameInUse(username: String): Boolean {
        if (username.isBlank()) return false
        return checkIdentifierAvailability("username", username.lowercase())
    }

    /**
     * Runs server-side (Admin SDK), bypassing the users collection's isSignedIn() rule - a
     * direct Firestore query here would fail with permission-denied while the caller is
     * still signed out, which is exactly when register() needs these checks the most.
     */
    private suspend fun checkIdentifierAvailability(field: String, value: String): Boolean {
        val response =
            httpClient.get(CHECK_IDENTIFIER_AVAILABILITY_URL) {
                parameter("field", field)
                parameter("value", value)
            }
        return lenientJson.decodeFromString<AvailabilityResponse>(response.bodyAsText()).inUse
    }

    override suspend fun findUserIdByIdentifier(identifier: String): String? {
        for (field in listOf("email", "username", "phone")) {
            val snapshot = usersCollection.where { field equalTo identifier }.get()
            if (!snapshot.documents.isEmpty()) return snapshot.documents.first().id
        }
        return null
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
