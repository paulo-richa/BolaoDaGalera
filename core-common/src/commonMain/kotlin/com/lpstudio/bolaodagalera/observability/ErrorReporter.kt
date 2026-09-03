package com.lpstudio.bolaodagalera.observability

import com.lpstudio.bolaodagalera.domain.model.ErrorCategory
import com.lpstudio.bolaodagalera.domain.usecase.ClassifyExceptionUseCase

/**
 * Centralizes what a ViewModel does when it catches an exception from a repository/use-case
 * call: log it, report it to Crashlytics with context, classify it, and produce the Portuguese
 * message a screen should show. This collapses the per-screen `friendlyError`-style duplication
 * that previously existed only in [com.lpstudio.bolaodagalera.presentation.auth.AuthViewModel].
 *
 * Callers are responsible for catching [kotlinx.coroutines.CancellationException] separately
 * and rethrowing it before reaching a generic `catch (e: Exception)` that could route here -
 * this class does not (and cannot, since it only ever sees already-caught exceptions) guard
 * against that itself.
 */
class ErrorReporter(
    private val crashReporter: CrashReporter,
    private val classifyException: ClassifyExceptionUseCase = ClassifyExceptionUseCase()
) {
    private val logger = appLogger("ErrorReporter")

    fun classify(e: Throwable): ErrorCategory = classifyException(e as? Exception ?: Exception(e))

    /**
     * Logs + reports [e] to Crashlytics with [context], then returns the Portuguese message for
     * its [ErrorCategory]. Pass [messageOverride] to replace the message for specific categories
     * (e.g. a screen-specific PERMISSION wording) while still sharing the rest of the mapping.
     * Takes [Throwable] (not just [Exception]) so it can be called directly from a Flow's
     * `.catch { e -> ... }`, whose parameter is typed as [Throwable].
     */
    fun reportAndClassify(e: Throwable, context: String, messageOverride: (ErrorCategory) -> String? = { null }): String {
        val category = classify(e)
        logger.e(e) { context }
        crashReporter.recordException(e, context)
        return messageOverride(category) ?: messageFor(category)
    }

    /** Logs + reports [e] with [context] without producing a message, for best-effort/background catches. */
    fun report(e: Throwable, context: String) {
        logger.w(e) { context }
        crashReporter.recordException(e, context)
    }

    private fun messageFor(category: ErrorCategory): String = when (category) {
        ErrorCategory.INVALID_CREDENTIALS -> "E-mail ou senha incorretos. Verifique os dados e tente novamente."
        ErrorCategory.USER_NOT_FOUND -> "Usuário não encontrado. Crie uma conta para acessar."
        ErrorCategory.NOT_FOUND -> "Não encontrado. Verifique os dados e tente novamente."
        ErrorCategory.ALREADY_IN_USE -> "Este dado já está sendo usado em outra conta."
        ErrorCategory.NETWORK -> "Erro de conexão. Verifique sua internet e tente novamente."
        ErrorCategory.RATE_LIMITED -> "Muitas tentativas falhas. Tente novamente em instantes."
        ErrorCategory.WEAK_PASSWORD -> "A senha é muito fraca. Use pelo menos 6 caracteres."
        ErrorCategory.INVALID_EMAIL -> "O formato do e-mail é inválido."
        ErrorCategory.PERMISSION -> "Você não tem permissão para realizar esta ação."
        ErrorCategory.UNKNOWN -> "Ocorreu um erro inesperado. Por favor, tente novamente."
    }
}
