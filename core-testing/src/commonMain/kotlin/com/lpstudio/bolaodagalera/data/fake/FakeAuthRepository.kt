package com.lpstudio.bolaodagalera.data.fake

import com.lpstudio.bolaodagalera.domain.model.User
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

val FAKE_USER =
    User(
        id = "pauloricha",
        name = "Paulo Teste Silva",
        email = "pauloteste@email.com",
        phone = "11911112222",
        nickname = "Paulão",
        username = "pauloricha"
    )

val FAKE_FRIEND =
    User(
        id = "livialima",
        name = "Lívia Teste Souza",
        email = "livialima@email.com",
        phone = "11933334444",
        nickname = "Lívia",
        username = "livialima"
    )

class FakeAuthRepository : AuthRepository {
    private val userState = MutableStateFlow<User?>(FAKE_USER)
    private val allUsers =
        mutableListOf(
            FAKE_USER,
            FAKE_FRIEND,
            User("u3", "Ricardo Oliveira", "ricardo@email.com", "", "Rick", "rick"),
            User("u4", "Ana Beatriz", "ana@email.com", "", "Bia", "bia"),
            User("u5", "Fernando Costa", "fernando@email.com", "", "Fernandão", "fernando"),
            User("u6", "Juliana Mendes", "ju@email.com", "", "Ju", "ju"),
            User("u7", "Marcelo Santos", "marcelo@email.com", "", "Tchelo", "marcelo"),
            User("u8", "Patrícia Lima", "pati@email.com", "", "Paty", "pati"),
            User("u9", "Gustavo Lima", "gustavo@email.com", "", "Guga", "guga")
        )

    /** Correct password used to validate signIn (all fake accounts share the same one). */
    var validPassword: String = "123456"

    /** When non-null, the next relevant call throws this exception, to simulate errors. */
    var signInException: Exception? = null
    var registerException: Exception? = null
    var resetPasswordException: Exception? = null

    override val currentUser: User?
        get() = userState.value

    override val authStateFlow: Flow<User?> = userState.asStateFlow()

    fun setUser(user: User?) {
        userState.value = user
    }

    override suspend fun signIn(email: String, password: String): User {
        signInException?.let { throw it }
        val user = allUsers.find { it.email.equals(email, ignoreCase = true) }
            ?: error("user-not-found: nenhuma conta com esse e-mail")
        if (password != validPassword) {
            error("invalid-credential: senha incorreta")
        }
        userState.value = user
        return user
    }

    override suspend fun register(email: String, password: String, name: String, phone: String, nickname: String, username: String): User {
        registerException?.let { throw it }
        val user = User("fake-${name.hashCode().and(0xFFFF)}", name, email, phone, nickname, username)
        allUsers.add(user)
        userState.value = user
        return user
    }

    override suspend fun signOut() {
        userState.value = null
    }

    override suspend fun updateProfile(name: String, phone: String, nickname: String) {
        userState.value = userState.value?.copy(name = name, phone = phone, nickname = nickname)
    }

    override suspend fun isEmailInUse(email: String): Boolean = allUsers.any { it.email.equals(email, ignoreCase = true) }

    override suspend fun isPhoneInUse(phone: String): Boolean = allUsers.any { it.phone == phone }

    override suspend fun isNicknameInUse(nickname: String): Boolean = allUsers.any { it.nickname.equals(nickname, ignoreCase = true) }

    override suspend fun isUsernameInUse(username: String): Boolean = allUsers.any { it.username.equals(username, ignoreCase = true) }

    override suspend fun sendPasswordResetEmail(email: String) {
        resetPasswordException?.let { throw it }
    }

    override suspend fun getUser(userId: String): User? = allUsers.find { it.id == userId }

    override suspend fun getUsers(userIds: List<String>): List<User> = allUsers.filter { it.id in userIds }
}
