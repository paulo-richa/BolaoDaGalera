package com.lpstudio.bolaodagalera.presentation.bolao

import com.lpstudio.bolaodagalera.domain.model.Match

/**
 * Same value as MatchCard.kt's own PREDICTION_LOCK_LEAD_MILLIS - kept
 * file-local to avoid growing MatchCard.kt past its function-count budget.
 */
private const val PREDICTION_LOCK_LEAD_MILLIS = 60_000L

/**
 * A missing/unconfirmed matchDateMillis (external source hasn't published the
 * real kickoff time yet, e.g. an unresolved knockout tie) can surface as
 * either Match.NO_DATE_MILLIS (the documented sentinel) or a null/zeroed
 * field read back as epoch 0.
 */
internal fun isMatchDateUnknown(match: Match): Boolean = match.hasNoConfirmedDate || match.matchDateMillis <= 0

internal fun isMatchTeamUnknown(match: Match, hFlag: String, aFlag: String): Boolean =
    match.homeTeamCode == "TBD" || match.awayTeamCode == "TBD" || hFlag.contains("ou") || aFlag.contains("ou")

/**
 * A match with real, known teams but an unconfirmed kickoff time can still be
 * predicted - there's just no real deadline to enforce yet, so it's
 * deliberately not locked the way an unresolved-team match is (predicting a
 * "Vencedor Oitavas x Vencedor Oitavas" makes no sense; predicting a real
 * "Fluminense x Platense" whose exact kickoff isn't confirmed does).
 */
internal fun computeCanPredict(
    match: Match,
    isFinished: Boolean,
    forceLocked: Boolean,
    isTeamUnknown: Boolean,
    isDateUnknown: Boolean,
    now: Long
): Boolean = !isFinished &&
    !forceLocked &&
    !isTeamUnknown &&
    (isDateUnknown || now < (match.matchDateMillis - PREDICTION_LOCK_LEAD_MILLIS))
