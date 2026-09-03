package com.lpstudio.bolaodagalera.observability

/**
 * Catalog of every Firebase Performance custom trace name fired by the app, so call sites
 * reference a constant instead of a loose string. Naming convention: `<module>_<action>`,
 * snake_case, shared with [AnalyticsEvents] for the write actions both track - read/load flows
 * only appear here (no matching analytics event), since they're operational latency signals,
 * not user-intent business events.
 */
object PerformanceTraces {
    const val AUTH_LOGIN = AnalyticsEvents.AUTH_LOGIN
    const val AUTH_SIGN_UP = AnalyticsEvents.AUTH_SIGN_UP
    const val AUTH_UPDATE_PROFILE = AnalyticsEvents.AUTH_UPDATE_PROFILE

    const val BOLAO_CREATE = AnalyticsEvents.BOLAO_CREATE
    const val BOLAO_EDIT = AnalyticsEvents.BOLAO_EDIT
    const val BOLAO_DELETE = AnalyticsEvents.BOLAO_DELETE
    const val BOLAO_JOIN_REQUEST = AnalyticsEvents.BOLAO_JOIN_REQUEST
    const val BOLAO_LEAVE = AnalyticsEvents.BOLAO_LEAVE
    const val BOLAO_APPROVE_JOIN = AnalyticsEvents.BOLAO_APPROVE_JOIN
    const val BOLAO_APPROVE_LEAVE = AnalyticsEvents.BOLAO_APPROVE_LEAVE
    const val BOLAO_REMOVE_PARTICIPANT = AnalyticsEvents.BOLAO_REMOVE_PARTICIPANT
    const val BOLAO_ADMIN_UPDATE_SCORE = AnalyticsEvents.BOLAO_ADMIN_UPDATE_SCORE
    const val BOLAO_LOAD_DETAIL = "bolao_load_detail"

    const val PREDICTION_SAVE = AnalyticsEvents.PREDICTION_SAVE
    const val PREDICTION_LOAD = "prediction_load"

    const val INVITATION_SEND = AnalyticsEvents.INVITATION_SEND
    const val INVITATION_RESPOND = AnalyticsEvents.INVITATION_RESPOND

    const val SUPPORT_SEND = AnalyticsEvents.SUPPORT_SEND

    const val HOME_LOAD = "home_load"
    const val RANKING_LOAD = "ranking_load"
}
