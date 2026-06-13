package com.lpstudio.bolaodagalera.data.firebase

import com.lpstudio.bolaodagalera.domain.model.User
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
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
    
    private var _cachedUser: User? = null

    override val currentUser: User?
        get() = _cachedUser ?: auth.currentUser?.let { User(it.uid, it.displayName ?: "Usuário", it.email ?: "", "", "", "") }

    override val authStateFlow: Flow<User?> = auth.authStateChanged.flatMapLatest { firebaseUser ->
        if (firebaseUser == null) {
            _cachedUser = null
            flowOf(null)
        } else {
            usersCollection.document(firebaseUser.uid).snapshots.map { doc ->
                val user = if (doc.exists) {
                    val dto = doc.data<UserDto>()
                    User(firebaseUser.uid, dto.name, dto.email, dto.phone, dto.nickname, dto.username)
                } else {
                    User(firebaseUser.uid, firebaseUser.displayName ?: "Usuário", firebaseUser.email ?: "", "", "", "")
                }
                _cachedUser = user
                user
            }
        }
    }

    override suspend fun signIn(email: String, password: String): User {
        val result = auth.signInWithEmailAndPassword(email.trim(), password)
        val firebaseUser = result.user ?: error("Login falhou")
        val doc = usersCollection.document(firebaseUser.uid).get()
        val dto = doc.data<UserDto>()
        return User(firebaseUser.uid, dto.name, dto.email, dto.phone, dto.nickname, dto.username)
    }

    override suspend fun register(email: String, password: String, name: String, phone: String, nickname: String, username: String): User {
        val result = try {
            auth.createUserWithEmailAndPassword(email, password)
        } catch (e: Exception) {
            val msg = e.message?.lowercase() ?: ""
            // Verifica múltiplos padrões de erro de e-mail já em uso
            val isEmailInUse = msg.contains("already-in-use") || 
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
        } catch (e: Exception) { }

        // Salva ou atualiza o perfil no Firestore
        usersCollection.document(user.uid).set(
            UserDto(name = name, email = user.email ?: "", phone = phone, nickname = nickname, username = username),
            merge = true
        )
        
        val finalUser = User(user.uid, name, user.email ?: "", phone, nickname, username)
        _cachedUser = finalUser
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
        } catch (e: Exception) { }
        
        // 2. Prepara os dados para o Firestore
        val updateMap = mutableMapOf<String, Any>(
            "name" to name,
            "phone" to phone,
            "nickname" to nickname,
            "email" to email
        )
        
        // 3. Grava no Firestore usando set com merge (mais resiliente que update)
        usersCollection.document(uid).set(updateMap, merge = true)
        
        // Atualiza o cache local para refletir na UI imediatamente preservando o username atual
        val currentUsername = _cachedUser?.username ?: ""
        _cachedUser = User(uid, name, email, phone, nickname, currentUsername)
    }

    override suspend fun isEmailInUse(email: String): Boolean {
        val snapshot = usersCollection.where { "email" equalTo email }.get()
        return !snapshot.documents.isEmpty()
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
}
