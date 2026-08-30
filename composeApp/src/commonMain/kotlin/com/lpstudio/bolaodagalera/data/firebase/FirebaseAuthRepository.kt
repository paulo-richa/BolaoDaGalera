package com.lpstudio.bolaodagalera.data.firebase

import com.lpstudio.bolaodagalera.domain.model.User
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class FirebaseAuthRepository : AuthRepository {
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
                        println("BOLAOLOG: Erro no snapshots de user: ${e.message}")
                        emit(null)
                    }
                } catch (e: Exception) {
                    println("BOLAOLOG: Erro crítico ao iniciar snapshots de user: ${e.message}")
                    flowOf(null)
                }
            }
        }.catch { e ->
            println("BOLAOLOG: Erro no authStateFlow: ${e.message}")
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
                // Verifica múltiplos padrões de erro de e-mail já em uso
                val isEmailInUse =
                    msg.contains("already-in-use") ||
                        msg.contains("already in use") ||
                        msg.contains("email-already") ||
                        msg.contains("collision")

                if (isEmailInUse) {
                    // Tenta fazer o login. Se a senha estiver errada, o erro será capturado pelo ViewModel
                    auth.signInWithEmailAndPassword(email, password)
                } else {
                    throw e
                }
            }

        val user = result.user ?: error("Cadastro falhou")
        try {
            user.updateProfile(displayName = name)
        } catch (e: Exception) {
        }

        // Salva ou atualiza o perfil no Firestore
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
        val firebaseUser = auth.currentUser ?: throw Exception("Usuário não autenticado")
        val uid = firebaseUser.uid
        val email = firebaseUser.email ?: ""

        // 1. Tenta atualizar o nome no Auth (opcional, não bloqueia se falhar)
        try {
            firebaseUser.updateProfile(displayName = name)
        } catch (e: Exception) {
        }

        // 2. Prepara os dados para o Firestore
        val updateMap =
            mutableMapOf<String, Any>(
                "name" to name,
                "phone" to phone,
                "nickname" to nickname,
                "email" to email
            )

        // 3. Grava no Firestore usando set com merge (mais resiliente que update)
        usersCollection.document(uid).set(updateMap, merge = true)

        // Atualiza o cache local para refletir na UI imediatamente preservando o username atual
        val currentUsername = cachedUser?.username ?: ""
        cachedUser = User(uid, name, email, phone, nickname, currentUsername)
    }

    override suspend fun isEmailInUse(email: String): Boolean = try {
        // Tenta usar o método do Auth que não exige permissões de Firestore
        val methods = auth.fetchSignInMethodsForEmail(email)
        methods.isNotEmpty()
    } catch (e: Exception) {
        // Fallback para Firestore apenas se o Auth falhar ou não estiver disponível
        try {
            val snapshot = usersCollection.where { "email" equalTo email }.get()
            !snapshot.documents.isEmpty()
        } catch (e2: Exception) {
            // Se ambos falharem, relança a exceção original para o ViewModel tratar
            throw e
        }
    }

    override suspend fun isPhoneInUse(phone: String): Boolean {
        if (phone.isBlank()) return false
        return try {
            val snapshot = usersCollection.where { "phone" equalTo phone }.get()
            !snapshot.documents.isEmpty()
        } catch (e: Exception) {
            // Se não puder verificar (ex: falta de permissão por estar deslogado), assume falso e deixa o fluxo seguir
            false
        }
    }

    override suspend fun isNicknameInUse(nickname: String): Boolean {
        if (nickname.isBlank()) return false
        return try {
            val snapshot = usersCollection.where { "nickname" equalTo nickname }.get()
            !snapshot.documents.isEmpty()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun isUsernameInUse(username: String): Boolean {
        if (username.isBlank()) return false
        return try {
            val snapshot = usersCollection.where { "username" equalTo username.lowercase() }.get()
            !snapshot.documents.isEmpty()
        } catch (e: Exception) {
            false
        }
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

    override suspend fun getUsers(userIds: List<String>): List<User> {
        if (userIds.isEmpty()) return emptyList()
        return userIds.mapNotNull { getUser(it) }
    }
}
