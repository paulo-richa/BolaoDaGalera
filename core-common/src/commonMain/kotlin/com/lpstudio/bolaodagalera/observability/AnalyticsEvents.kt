package com.lpstudio.bolaodagalera.observability

/**
 * Catalog of every Firebase Analytics event name fired by the app, so call sites reference a
 * constant instead of a loose string - avoids typos and duplicate/inconsistent names across
 * screens. Naming convention: `<module>_<action>`, snake_case. Only user-intent write actions
 * are logged here (create/update/delete/approve/send) - screen loads and other read flows are
 * tracked via [PerformanceTraces] and per-screen traces instead, to keep the analytics funnel
 * focused on actions rather than page views.
 */
object AnalyticsEvents {
    const val AUTH_LOGIN = "auth_login"
    const val AUTH_SIGN_UP = "auth_sign_up"
    const val AUTH_UPDATE_PROFILE = "auth_update_profile"
    const val AUTH_SIGN_OUT = "auth_sign_out"
    const val AUTH_PASSWORD_RESET = "auth_password_reset"

    const val BOLAO_CREATE = "bolao_create"
    const val BOLAO_EDIT = "bolao_edit"
    const val BOLAO_DELETE = "bolao_delete"
    const val BOLAO_JOIN_REQUEST = "bolao_join_request"
    const val BOLAO_LEAVE = "bolao_leave"
    const val BOLAO_APPROVE_JOIN = "bolao_approve_join"
    const val BOLAO_APPROVE_LEAVE = "bolao_approve_leave"
    const val BOLAO_REMOVE_PARTICIPANT = "bolao_remove_participant"
    const val BOLAO_ADMIN_UPDATE_SCORE = "bolao_admin_update_score"

    const val PREDICTION_SAVE = "prediction_save"

    const val INVITATION_SEND = "invitation_send"
    const val INVITATION_RESPOND = "invitation_respond"

    const val SUPPORT_SEND = "support_send"
}
