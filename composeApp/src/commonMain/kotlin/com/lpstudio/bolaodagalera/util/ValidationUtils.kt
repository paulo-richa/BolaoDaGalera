package com.lpstudio.bolaodagalera.util

object ValidationUtils {
    private val EMAIL_REGEX = """^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""".toRegex()

    private val COMMON_TYPOS =
        mapOf(
            "gmali.com" to "gmail.com",
            "gamil.com" to "gmail.com",
            "gmal.com" to "gmail.com",
            "gmial.com" to "gmail.com",
            "gmail.coom" to "gmail.com",
            "gmail.con" to "gmail.com",
            "gmail.cm" to "gmail.com",
            "hotmial.com" to "hotmail.com",
            "hotmail.coom" to "hotmail.com",
            "hotmail.con" to "hotmail.com",
            "hotmail.cm" to "hotmail.com",
            "outlook.coom" to "outlook.com",
            "outlook.con" to "outlook.com",
            "yahoo.coom" to "yahoo.com",
            "yahoo.con" to "yahoo.com",
            "icloud.coom" to "icloud.com",
        )

    fun validateEmail(email: String): String? {
        if (email.isBlank()) return "E-mail obrigatório"
        if (!EMAIL_REGEX.matches(email)) return "E-mail inválido"

        val domain = email.substringAfter("@", "")
        if (COMMON_TYPOS.containsKey(domain)) {
            return "Você quis dizer @${COMMON_TYPOS[domain]}?"
        }

        return null
    }

    fun isValidFullName(name: String): Boolean {
        val parts = name.trim().split(" ").filter { it.isNotBlank() }
        return (parts.size >= 2) && parts.all { it.length >= 2 }
    }
}
