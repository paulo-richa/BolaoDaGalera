package com.lpstudio.bolaodagalera.observability

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Encaminha logs de nível Info ou acima para o Crashlytics como breadcrumbs,
 * dando contexto do que aconteceu no app pouco antes de um crash reportado.
 */
class CrashlyticsLogWriter : LogWriter() {
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        if (severity < Severity.Info) return
        try {
            FirebaseCrashlytics.getInstance().log("[$tag] $message")
        } catch (e: IllegalStateException) {
            // Firebase pode não estar inicializado (ex: testes Robolectric) - breadcrumb é
            // um "nice to have", nunca deve derrubar o app ou quebrar um teste por causa disso.
        }
    }
}
