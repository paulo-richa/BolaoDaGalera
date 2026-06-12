package com.lpstudio.bolaodagalera.data.fake

import com.lpstudio.bolaodagalera.domain.model.User
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

val FAKE_USER = User(
    id = "pauloricha",
    name = "Paulo George Moreira Richa",
    email = "paulo.richa@hotmail.com",
    phone = "11975148477",
    nickname = "Paulão",
    username = "pauloricha"
)

val FAKE_FRIEND = User(
    id = "livialima",
    name = "Lívia Cristina de Lima",
    email = "liviac.lima@hotmail.com",
    phone = "11943612890",
    nickname = "Lívia",
    username = "livialima"
)

class FakeAuthRepository : AuthRepository {

    private val _user = MutableStateFlow<User?>(FAKE_USER)
    private val allUsers = mutableListOf(
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
    
    override val currentUser: User?
        get() = _user.value

    override val authStateFlow: Flow<User?> = _user.asStateFlow()

    override suspend fun signIn(email: String, password: String): User {
        val user = allUsers.find { it.email == email } ?: FAKE_USER
        _user.value = user
        return user
    }

    override suspend fun register(email: String, password: String, name: String, phone: String, nickname: String, username: String): User {
        val user = User("fake-${name.hashCode().and(0xFFFF)}", name, email, phone, nickname, username)
        allUsers.add(user)
        _user.value = user
        return user
    }

    override suspend fun signOut() {
        _user.value = null
    }

    override suspend fun updateProfile(name: String, phone: String, nickname: String) {
        _user.value = _user.value?.copy(name = name, phone = phone, nickname = nickname)
    }

    override suspend fun isEmailInUse(email: String): Boolean {
        return allUsers.any { it.email.equals(email, ignoreCase = true) }
    }

    override suspend fun isPhoneInUse(phone: String): Boolean {
        return allUsers.any { it.phone == phone }
    }

    override suspend fun isNicknameInUse(nickname: String): Boolean {
        return allUsers.any { it.nickname.equals(nickname, ignoreCase = true) }
    }

    override suspend fun isUsernameInUse(username: String): Boolean {
        return allUsers.any { it.username.equals(username, ignoreCase = true) }
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        // No-op for fake
    }
}
