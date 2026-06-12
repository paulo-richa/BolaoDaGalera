package com.lpstudio.bolaodagalera.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String = "",
    val nickname: String = "",
    val username: String = "" // O "ID" único do usuário (apenas letras)
)
