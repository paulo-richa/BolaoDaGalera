package com.lpstudio.bolaodagalera.domain.repository

import com.lpstudio.bolaodagalera.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: User?
    val authStateFlow: Flow<User?>
    suspend fun signIn(email: String, password: String): User
    suspend fun register(email: String, password: String, name: String, phone: String = "", nickname: String = "", username: String = ""): User
    suspend fun signOut()
    suspend fun updateProfile(name: String, phone: String, nickname: String)
    suspend fun isEmailInUse(email: String): Boolean
    suspend fun isPhoneInUse(phone: String): Boolean
    suspend fun isNicknameInUse(nickname: String): Boolean
    suspend fun isUsernameInUse(username: String): Boolean
    suspend fun sendPasswordResetEmail(email: String)
}
