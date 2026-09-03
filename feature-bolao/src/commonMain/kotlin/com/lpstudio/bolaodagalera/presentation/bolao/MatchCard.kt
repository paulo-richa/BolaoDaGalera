package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bolaodagalera.feature_bolao.generated.resources.Res
import bolaodagalera.feature_bolao.generated.resources.bolao_common_date_tbd
import bolaodagalera.feature_bolao.generated.resources.match_card_admin_dialog_cancel
import bolaodagalera.feature_bolao.generated.resources.match_card_admin_dialog_message
import bolaodagalera.feature_bolao.generated.resources.match_card_admin_dialog_save
import bolaodagalera.feature_bolao.generated.resources.match_card_admin_dialog_title
import bolaodagalera.feature_bolao.generated.resources.match_card_admin_dialog_versus
import bolaodagalera.feature_bolao.generated.resources.match_card_coming_soon
import bolaodagalera.feature_bolao.generated.resources.match_card_edit_prediction
import bolaodagalera.feature_bolao.generated.resources.match_card_first_leg_score
import bolaodagalera.feature_bolao.generated.resources.match_card_make_prediction
import bolaodagalera.feature_bolao.generated.resources.match_card_points_plural
import bolaodagalera.feature_bolao.generated.resources.match_card_points_singular
import bolaodagalera.feature_bolao.generated.resources.match_card_social_badge_label
import bolaodagalera.feature_bolao.generated.resources.match_card_status_extra_time
import bolaodagalera.feature_bolao.generated.resources.match_card_status_finished
import bolaodagalera.feature_bolao.generated.resources.match_card_status_going_extra_time
import bolaodagalera.feature_bolao.generated.resources.match_card_status_going_penalties
import bolaodagalera.feature_bolao.generated.resources.match_card_status_halftime
import bolaodagalera.feature_bolao.generated.resources.match_card_status_in_progress
import bolaodagalera.feature_bolao.generated.resources.match_card_status_penalties
import bolaodagalera.feature_bolao.generated.resources.match_card_vs_label
import com.lpstudio.bolaodagalera.designsystem.components.BolaoDialog
import com.lpstudio.bolaodagalera.designsystem.components.BolaoHorizontalDivider
import com.lpstudio.bolaodagalera.designsystem.components.BolaoIcon
import com.lpstudio.bolaodagalera.designsystem.components.BolaoScoreField
import com.lpstudio.bolaodagalera.designsystem.components.BolaoSurface
import com.lpstudio.bolaodagalera.designsystem.components.BolaoText
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTextButton
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoRadius
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoRadiusShape
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoSpacing
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTypography
import com.lpstudio.bolaodagalera.designsystem.theme.DeepNavy
import com.lpstudio.bolaodagalera.designsystem.theme.ErrorRed
import com.lpstudio.bolaodagalera.designsystem.theme.GlassBorder
import com.lpstudio.bolaodagalera.designsystem.theme.GlassWhite
import com.lpstudio.bolaodagalera.designsystem.theme.Gold
import com.lpstudio.bolaodagalera.designsystem.theme.NavyElevated
import com.lpstudio.bolaodagalera.designsystem.theme.Neon
import com.lpstudio.bolaodagalera.designsystem.theme.OrangeNeon
import com.lpstudio.bolaodagalera.designsystem.theme.TextMuted
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.util.TimeSource
import com.lpstudio.bolaodagalera.util.resolveDisplayName
import org.jetbrains.compose.resources.stringResource

/**
 * Resolved team names, flags (as annotated strings, since a "team A ou team B" placeholder
 * renders the " ou " separator in a smaller font) and crest URLs for both sides of a match.
 */
@Immutable
data class MatchTeamDisplay(
    val hName: String,
    val hFlag: String,
    val hCrest: String?,
    val hAnn: AnnotatedString,
    val aName: String,
    val aFlag: String,
    val aCrest: String?,
    val aAnn: AnnotatedString
)

/**
 * Derived, purely-computed status flags for a match card. Kept as plain (non-remembered) values
 * because the source computation is cheap and was not memoized in the original implementation.
 */
@Immutable
data class MatchCardStatus(
    val isFin: Boolean,
    val isLive: Boolean,
    val isGhost: Boolean,
    val isTbd: Boolean,
    val canPred: Boolean,
    val borderColor: Color,
    val isExp: Boolean,
    val isLock: Boolean,
    val cardBackground: Brush?
)

/** Callbacks bundled together to keep [MatchCardBody]'s parameter list manageable. */
@Immutable
data class MatchCardActions(val onClick: () -> Unit, val onShowAllPredictions: () -> Unit, val onOpenAdminScoreDialog: () -> Unit)

/** Display/behavior flags bundled together to keep [MatchCard]'s parameter list manageable. */
@Immutable
data class MatchCardOptions(
    val isAdmin: Boolean = false,
    val bolaoCreatedAt: Long = 0L,
    val forceLocked: Boolean = false,
    val showSocialBadge: Boolean = true,
    val allMatches: List<Match> = emptyList(),
    val isTwoLegged: Boolean = false
)

@Composable
fun MatchCard(
    match: Match,
    prediction: Prediction?,
    options: MatchCardOptions = MatchCardOptions(),
    onShowAllPredictions: () -> Unit = {},
    onOpenAdminScoreDialog: () -> Unit = {},
    onClick: () -> Unit
) {
    val isFinished = match.isFinished
    val now = TimeSource.nowMillis()
    val start = match.matchDateMillis
    val teams = rememberMatchTeamDisplay(match, options.allMatches)
    val ida = rememberFirstLegScore(match, options.allMatches, options.isTwoLegged)
    val status =
        computeMatchCardStatus(
            match = match,
            prediction = prediction,
            isFinished = isFinished,
            now = now,
            start = start,
            bolaoCreatedAt = options.bolaoCreatedAt,
            forceLocked = options.forceLocked,
            hFlag = teams.hFlag,
            aFlag = teams.aFlag
        )

    BolaoSurface(
        modifier = Modifier.fillMaxWidth(),
        color = if (status.isLive) Color.Transparent else NavyElevated,
        shape = BolaoRadiusShape.md,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (status.isLive) Neon.copy(alpha = 0.5f) else status.borderColor)
    ) {
        MatchCardBody(
            match = match,
            teams = teams,
            prediction = prediction,
            ida = ida,
            status = status,
            isAdmin = options.isAdmin,
            forceLocked = options.forceLocked,
            showSocialBadge = options.showSocialBadge,
            actions = MatchCardActions(onClick, onShowAllPredictions, onOpenAdminScoreDialog)
        )
    }
}

@Composable
fun MatchCardBody(
    match: Match,
    teams: MatchTeamDisplay,
    prediction: Prediction?,
    ida: String?,
    status: MatchCardStatus,
    isAdmin: Boolean,
    forceLocked: Boolean,
    showSocialBadge: Boolean,
    actions: MatchCardActions
) {
    val hasPrediction = prediction != null
    Box(
        modifier =
        Modifier.fillMaxWidth().then(
            if (status.cardBackground != null) Modifier.background(status.cardBackground) else Modifier
        ).clickable(
            enabled = matchCardClickEnabled(status, isAdmin, showSocialBadge),
            onClick = {
                if (status.canPred) {
                    actions.onClick()
                } else if (isAdmin) {
                    actions.onOpenAdminScoreDialog()
                } else if (status.isExp && showSocialBadge) {
                    actions.onShowAllPredictions()
                }
            }
        )
    ) {
        val showGalera = showSocialBadge && (isAdmin || status.isExp) && !status.isTbd && !status.isGhost
        MatchCardOverlayBadges(showGalera, ida, actions.onShowAllPredictions)
        MatchCardTopStatusBadge(
            isFin = status.isFin,
            hasPrediction = hasPrediction,
            prediction = prediction,
            match = match,
            isTbd = status.isTbd,
            showGalera = showGalera
        )
        Column(modifier = Modifier.padding(BolaoSpacing.md)) {
            Spacer(Modifier.height(if (status.isLock || status.canPred) 32.dp else 16.dp))
            MatchTeamsRow(
                match = match,
                teams = teams,
                prediction = prediction,
                isFin = status.isFin,
                canPred = status.canPred,
                isLock = status.isLock,
                isTbd = status.isTbd
            )
            if (status.canPred) {
                PredictionActionRow(hasPrediction)
            } else if (status.isLock) {
                LockedStatusSection(
                    match = match,
                    isFin = status.isFin,
                    isLive = status.isLive,
                    forceLocked = forceLocked,
                    isTbd = status.isTbd,
                    isAdmin = isAdmin,
                    onOpenAdminScoreDialog = actions.onOpenAdminScoreDialog
                )
            }
        }
    }
}

/**
 * Whether the card's outer clickable surface should react to taps, independent of which
 * action the tap triggers (see the `onClick` handler built in [MatchCardBody]).
 */
fun matchCardClickEnabled(status: MatchCardStatus, isAdmin: Boolean, showSocialBadge: Boolean): Boolean = when {
    status.isGhost -> isAdmin
    status.canPred -> true
    status.isFin -> isAdmin
    status.isExp -> (!isAdmin && showSocialBadge) || isAdmin
    else -> false
}

@Composable
fun rememberMatchTeamDisplay(match: Match, allMatches: List<Match>): MatchTeamDisplay {
    val (hName, hFlag, hCrest) =
        remember(match.id, match.homeTeam, match.homeTeamFlag, allMatches) {
            resolveDisplayName(match.id, match.homeTeam, match.homeTeamFlag, allMatches, true)
        }
    val (aName, aFlag, aCrest) =
        remember(match.id, match.awayTeam, match.awayTeamFlag, allMatches) {
            resolveDisplayName(match.id, match.awayTeam, match.awayTeamFlag, allMatches, false)
        }
    val hAnn = remember(hFlag) { buildFlagAnnotatedString(hFlag) }
    val aAnn = remember(aFlag) { buildFlagAnnotatedString(aFlag) }
    return MatchTeamDisplay(hName, hFlag, hCrest, hAnn, aName, aFlag, aCrest, aAnn)
}

/**
 * Renders the " ou " separator between two placeholder team names at a smaller font size,
 * while leaving a resolved single flag/name untouched.
 */
fun buildFlagAnnotatedString(flag: String): AnnotatedString {
    val parts = flag.split(" ou ")
    return if (parts.size > 1) {
        buildAnnotatedString {
            parts.forEachIndexed { i, part ->
                append(part)
                if (i < parts.size - 1) {
                    withStyle(style = SpanStyle(fontSize = BolaoTypography.bodyMedium.fontSize)) {
                        append(" ou ")
                    }
                }
            }
        }
    } else {
        AnnotatedString(flag)
    }
}

/**
 * First-leg aggregate score shown on a two-legged knockout match's return leg, resolved by
 * matching the corresponding leg-1 fixture in [allMatches].
 */
@Composable
fun rememberFirstLegScore(match: Match, allMatches: List<Match>, isTwoLegged: Boolean): String? {
    val isVolta = match.id.contains("-L2")
    return remember(match.id, allMatches, isTwoLegged, isVolta) {
        if (isTwoLegged && isVolta) {
            val firstLeg =
                allMatches.find { m ->
                    m.championshipId == match.championshipId &&
                        m.phase == match.phase &&
                        m.id != match.id &&
                        !m.id.contains("-L2") &&
                        (
                            (match.matchOrder > 0 && m.matchOrder == match.matchOrder) ||
                                m.id.replace("-L1", "") == match.id.replace("-L2", "") ||
                                (m.homeTeamCode == match.awayTeamCode && m.awayTeamCode == match.homeTeamCode)
                            )
                }
            if (firstLeg != null && firstLeg.homeScore != null && firstLeg.awayScore != null) {
                "${firstLeg.homeScore}×${firstLeg.awayScore}"
            } else {
                null
            }
        } else {
            null
        }
    }
}

/**
 * Whether the match is over, and whether it is currently being played live. Grouped together
 * because "live" is only meaningful relative to "finished" (a match can't be both).
 */
/** A match with a score but no live status update is assumed finished after this long. */
private const val ASSUMED_FINISHED_AFTER_MILLIS = 3 * 3_600_000L

/** Predictions lock, and the "live" window opens, this long before kickoff. */
private const val PREDICTION_LOCK_LEAD_MILLIS = 60_000L

fun computeFinishedAndLiveFlags(match: Match, now: Long, start: Long): Pair<Boolean, Boolean> {
    val isFin = match.status == "FINISHED" ||
        match.status == "PENALTIES" ||
        match.status == "PAUSED_PENALTIES" ||
        (
            match.homeScore != null &&
                match.awayScore != null &&
                now > (start + ASSUMED_FINISHED_AFTER_MILLIS)
            )
    val statusLive = listOf("IN_PLAY", "PAUSED", "EXTRA_TIME", "PENALTIES", "LIVE")
    val isLive = !isFin &&
        (
            match.status in statusLive ||
                (now >= (start - PREDICTION_LOCK_LEAD_MILLIS) && now < (start + ASSUMED_FINISHED_AFTER_MILLIS))
            )
    return isFin to isLive
}

/** Card border color: reflects prediction accuracy once the match is finished, else whether a prediction exists at all. */
fun computeMatchBorderColor(match: Match, prediction: Prediction?, isFin: Boolean): Color = when {
    isFin && prediction != null -> pointsToColor(calculateMatchPoints(match, prediction))
    prediction != null -> Gold.copy(alpha = 0.4f)
    else -> GlassBorder
}

fun computeMatchCardStatus(
    match: Match,
    prediction: Prediction?,
    isFinished: Boolean,
    now: Long,
    start: Long,
    bolaoCreatedAt: Long,
    forceLocked: Boolean,
    hFlag: String,
    aFlag: String
): MatchCardStatus {
    val (isFin, isLive) = computeFinishedAndLiveFlags(match, now, start)
    val isGhost = start < bolaoCreatedAt
    val isTbd = (match.homeTeamCode == "TBD" || match.awayTeamCode == "TBD") || hFlag.contains("ou") || aFlag.contains("ou")
    val canPred = !isFinished && now < (match.matchDateMillis - PREDICTION_LOCK_LEAD_MILLIS) && !forceLocked && !isTbd
    val borderColor = computeMatchBorderColor(match, prediction, isFin)
    val isExp = now >= (match.matchDateMillis - PREDICTION_LOCK_LEAD_MILLIS) || isFinished
    val isLock = isExp || forceLocked || isGhost || isTbd
    val cardBackground = if (isLive) Brush.verticalGradient(listOf(NavyElevated, DeepNavy)) else null
    return MatchCardStatus(
        isFin = isFin,
        isLive = isLive,
        isGhost = isGhost,
        isTbd = isTbd,
        canPred = canPred,
        borderColor = borderColor,
        isExp = isExp,
        isLock = isLock,
        cardBackground = cardBackground
    )
}

private const val EXACT_SCORE_POINTS = 3
private const val CORRECT_OUTCOME_POINTS = 1

/** Points earned by [prediction] against the match's final score: exact score, correct outcome, or none. */
fun calculateMatchPoints(match: Match, prediction: Prediction): Int {
    val hR = match.homeScore ?: 0
    val aR = match.awayScore ?: 0
    val hP = prediction.homeScore
    val aP = prediction.awayScore
    return when {
        hP == hR && aP == aR -> EXACT_SCORE_POINTS
        (hP > aP && hR > aR) || (hP < aP && hR < aR) || (hP == aP && hR == aR) -> CORRECT_OUTCOME_POINTS
        else -> 0
    }
}

fun pointsToColor(points: Int): Color = when (points) {
    EXACT_SCORE_POINTS -> Neon
    CORRECT_OUTCOME_POINTS -> Gold
    else -> ErrorRed
}

/**
 * Small decorative overlays anchored to the card's corners: the "galera" social badge
 * (top-center, only when [showGalera]) and the first-leg aggregate score badge (top-start,
 * only when [ida] is non-null, two-legged knockout matches).
 */
@Composable
fun BoxScope.MatchCardOverlayBadges(showGalera: Boolean, ida: String?, onShowAllPredictions: () -> Unit) {
    if (showGalera) {
        BolaoSurface(
            onClick = onShowAllPredictions,
            color = OrangeNeon.copy(alpha = 0.2f),
            shape = RoundedCornerShape(
                bottomStart = BolaoRadius.md,
                bottomEnd = BolaoRadius.md
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                OrangeNeon.copy(alpha = 0.4f)
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-6).dp)
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = BolaoSpacing.md,
                    vertical = BolaoSpacing.xs
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.sm)
            ) {
                BolaoIcon(Icons.Default.Check, null, tint = OrangeNeon, modifier = Modifier.size(12.dp))
                BolaoText(
                    stringResource(Res.string.match_card_social_badge_label),
                    color = OrangeNeon,
                    fontSize = BolaoTypography.labelSmall.fontSize,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.6.sp
                )
            }
        }
    }

    if (ida != null) {
        BolaoSurface(
            color = Gold.copy(alpha = 0.15f),
            shape = RoundedCornerShape(bottomEnd = BolaoRadius.md, topStart = BolaoRadius.md),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Gold.copy(alpha = 0.3f)),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            BolaoText(
                text = stringResource(Res.string.match_card_first_leg_score, ida),
                fontSize = BolaoTypography.labelSmall.fontSize,
                fontWeight = FontWeight.Black,
                color = Gold,
                modifier = Modifier.padding(horizontal = BolaoSpacing.md, vertical = BolaoSpacing.sm)
            )
        }
    }
}

/**
 * Top-right/top-center status overlay: the match date (or "TBD") until the match has finished
 * with a recorded prediction, at which point it flips to the earned-points badge.
 */
@Composable
fun BoxScope.MatchCardTopStatusBadge(
    isFin: Boolean,
    hasPrediction: Boolean,
    prediction: Prediction?,
    match: Match,
    isTbd: Boolean,
    showGalera: Boolean
) {
    if (!(isFin && hasPrediction)) {
        BolaoText(
            // While the matchup isn't confirmed (teams TBD), don't show a date
            // even if the API already published one for the phase "slot" —
            // avoids suggesting a matchup that doesn't exist yet.
            text = if (isTbd) stringResource(Res.string.bolao_common_date_tbd) else formatMatchDate(match.matchDateMillis),
            fontSize = BolaoTypography.bodySmall.fontSize,
            color = Color.White,
            letterSpacing = 0.2.sp,
            modifier =
            Modifier
                .align(if (!showGalera) Alignment.TopCenter else Alignment.TopEnd)
                .padding(top = BolaoSpacing.md, end = if (!showGalera) BolaoSpacing.xs else BolaoSpacing.md)
        )
    }
    if (isFin && prediction != null) {
        val points = calculateMatchPoints(match, prediction)
        BolaoSurface(
            color =
            when (points) {
                3 -> Neon.copy(alpha = 0.15f)
                1 -> Gold.copy(alpha = 0.15f)
                else -> ErrorRed.copy(alpha = 0.1f)
            },
            shape = RoundedCornerShape(bottomStart = BolaoRadius.md, topEnd = BolaoRadius.lg),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            BolaoText(
                text = if (points == 1) {
                    stringResource(Res.string.match_card_points_singular)
                } else {
                    stringResource(Res.string.match_card_points_plural, points)
                },
                fontSize = BolaoTypography.bodySmall.fontSize,
                fontWeight = FontWeight.Black,
                color = pointsToColor(points),
                modifier = Modifier.padding(horizontal = BolaoSpacing.md, vertical = BolaoSpacing.sm)
            )
        }
    }
}

@Composable
fun MatchTeamsRow(
    match: Match,
    teams: MatchTeamDisplay,
    prediction: Prediction?,
    isFin: Boolean,
    canPred: Boolean,
    isLock: Boolean,
    isTbd: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (teams.hName.isEmpty()) Arrangement.Center else Arrangement.spacedBy(BolaoSpacing.md)
        ) {
            TeamIcon(crestUrl = teams.hCrest ?: match.homeTeamCrest, flag = teams.hAnn, isTbd = isTbd, size = 32.dp)
            if (teams.hName.isNotEmpty()) TeamNameText(name = teams.hName, modifier = Modifier.weight(1f))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = BolaoSpacing.sm)) {
            if (prediction != null) {
                PredictionScoreBox(match = match, prediction = prediction, isFin = isFin, canPred = canPred, isLock = isLock)
            } else {
                Box(
                    modifier =
                    Modifier
                        .clip(BolaoRadiusShape.sm)
                        .background(Brush.linearGradient(listOf(GlassWhite, GlassWhite)))
                        .padding(horizontal = BolaoSpacing.md, vertical = BolaoSpacing.sm),
                    contentAlignment = Alignment.Center
                ) {
                    BolaoText(
                        stringResource(Res.string.match_card_vs_label),
                        fontSize = BolaoTypography.bodyMedium.fontSize,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted.copy(alpha = 0.7f)
                    )
                }
            }
        }
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (teams.aName.isEmpty()) {
                Arrangement.Center
            } else {
                Arrangement.spacedBy(
                    BolaoSpacing.md,
                    Alignment.End
                )
            }
        ) {
            if (teams.aName.isNotEmpty()) TeamNameText(name = teams.aName, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            TeamIcon(crestUrl = teams.aCrest ?: match.awayTeamCrest, flag = teams.aAnn, isTbd = isTbd, size = 32.dp)
        }
    }
}

/** Color of the prediction score box, reflecting whether it is still editable, exact, or a partial/no hit. */
fun predictionScoreColor(hR: Int, aR: Int, hP: Int, aP: Int, isFin: Boolean, canPred: Boolean, isLock: Boolean): Color = when {
    !isFin && !canPred -> TextMuted
    !isLock -> Gold
    hP == hR && aP == aR -> Neon
    (hP > aP && hR > aR) || (hP < aP && hR < aR) || (hP == aP && hR == aR) -> Gold
    else -> ErrorRed
}

@Composable
fun PredictionScoreBox(match: Match, prediction: Prediction, isFin: Boolean, canPred: Boolean, isLock: Boolean) {
    val hR = match.homeScore ?: 0
    val aR = match.awayScore ?: 0
    val hP = prediction.homeScore
    val aP = prediction.awayScore
    val sColor = predictionScoreColor(hR, aR, hP, aP, isFin, canPred, isLock)
    val isExact = isFin && hP == hR && aP == aR
    Box(
        modifier =
        Modifier
            .clip(BolaoRadiusShape.md)
            .background(Brush.linearGradient(listOf(sColor.copy(0.15f), sColor.copy(0.05f))))
            .then(if (isExact) Modifier.border(2.dp, Neon, BolaoRadiusShape.md) else Modifier)
            .padding(horizontal = BolaoSpacing.md, vertical = BolaoSpacing.md),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BolaoText(
                "$hP",
                fontSize = BolaoTypography.displaySmall.fontSize,
                fontWeight = FontWeight.ExtraBold,
                color = sColor
            )
            BolaoText(
                "×",
                fontSize = BolaoTypography.titleLarge.fontSize,
                fontWeight = FontWeight.Bold,
                color = sColor.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = BolaoSpacing.sm)
            )
            BolaoText(
                "$aP",
                fontSize = BolaoTypography.displaySmall.fontSize,
                fontWeight = FontWeight.ExtraBold,
                color = sColor
            )
        }
    }
}

@Composable
fun PredictionActionRow(hasPrediction: Boolean) {
    Spacer(Modifier.height(10.dp))
    BolaoHorizontalDivider(color = GlassBorder, thickness = 0.5.dp)
    Spacer(Modifier.height(12.dp))
    Row(
        modifier =
        Modifier.fillMaxWidth().clip(
            BolaoRadiusShape.md
        ).background(Neon.copy(alpha = 0.08f)).padding(vertical = BolaoSpacing.sm),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BolaoIcon(Icons.Default.Edit, null, modifier = Modifier.size(13.dp), tint = Neon)
        Spacer(Modifier.width(8.dp))
        BolaoText(
            text =
            if (hasPrediction) {
                stringResource(Res.string.match_card_edit_prediction)
            } else {
                stringResource(Res.string.match_card_make_prediction)
            },
            fontSize = BolaoTypography.bodyMedium.fontSize,
            color = Neon,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun LockedStatusSection(
    match: Match,
    isFin: Boolean,
    isLive: Boolean,
    forceLocked: Boolean,
    isTbd: Boolean,
    isAdmin: Boolean,
    onOpenAdminScoreDialog: () -> Unit
) {
    val dColor = if (isLive) Neon.copy(alpha = 0.3f) else GlassBorder
    Spacer(Modifier.height(14.dp))
    BolaoHorizontalDivider(color = dColor, thickness = 0.5.dp)
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if ((forceLocked || isTbd) && !match.isFinished) {
            BolaoText(
                text = stringResource(Res.string.match_card_coming_soon),
                fontSize = BolaoTypography.bodySmall.fontSize,
                fontWeight = FontWeight.Black,
                color = Neon.copy(alpha = 0.6f),
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(top = BolaoSpacing.sm)
            )
        } else {
            val accentColor = if (isFin) Color.White else Neon
            MatchStatusRow(match, isFin, isLive, accentColor)
            MatchScoreChip(match, accentColor, isAdmin, onOpenAdminScoreDialog)
        }
    }
}

@Composable
fun MatchStatusRow(match: Match, isFin: Boolean, isLive: Boolean, accentColor: Color) {
    val statusLabel =
        when {
            isFin -> stringResource(Res.string.match_card_status_finished)
            match.status == "EXTRA_TIME" -> stringResource(Res.string.match_card_status_extra_time)
            match.status == "PENALTIES" -> stringResource(Res.string.match_card_status_penalties)
            match.status == "PAUSED_EXTRA_TIME" -> stringResource(Res.string.match_card_status_going_extra_time)
            match.status == "PAUSED_PENALTIES" -> stringResource(Res.string.match_card_status_going_penalties)
            match.status == "PAUSED" -> stringResource(Res.string.match_card_status_halftime)
            else -> stringResource(Res.string.match_card_status_in_progress)
        }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(top = BolaoSpacing.xs)
    ) {
        if (isLive) {
            val inf = rememberInfiniteTransition()
            val alpha by inf.animateFloat(
                0.3f,
                1f,
                infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse)
            )
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Neon.copy(alpha = alpha)))
            Spacer(Modifier.width(6.dp))
        }
        BolaoText(
            text = statusLabel,
            fontSize = BolaoTypography.bodySmall.fontSize,
            fontWeight = FontWeight.Black,
            color = accentColor.copy(alpha = 0.7f),
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
fun MatchScoreChip(match: Match, accentColor: Color, isAdmin: Boolean, onOpenAdminScoreDialog: () -> Unit) {
    Box(
        modifier =
        Modifier.padding(
            top = BolaoSpacing.xs
        ).clip(BolaoRadiusShape.sm).background(accentColor.copy(alpha = 0.08f)).then(
            if (isAdmin) {
                Modifier.clickable {
                    onOpenAdminScoreDialog()
                }
            } else {
                Modifier
            }
        ).padding(horizontal = BolaoSpacing.sm, vertical = BolaoSpacing.xs),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BolaoText(
                "${match.homeScore ?: 0}",
                fontSize = BolaoTypography.titleLarge.fontSize,
                fontWeight = FontWeight.Black,
                color = accentColor
            )
            BolaoText(
                "×",
                fontSize = BolaoTypography.bodyMedium.fontSize,
                fontWeight = FontWeight.Bold,
                color = accentColor.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = BolaoSpacing.xs)
            )
            BolaoText(
                "${match.awayScore ?: 0}",
                fontSize = BolaoTypography.titleLarge.fontSize,
                fontWeight = FontWeight.Black,
                color = accentColor
            )
        }
    }
}

@Composable
fun MatchStatusAndScore(match: Match, isFin: Boolean, isLive: Boolean, isAdmin: Boolean, onOpenAdminScoreDialog: () -> Unit) {
    val accentColor = if (isFin) Color.White else Neon
    MatchStatusRow(match, isFin, isLive, accentColor)
    MatchScoreChip(match, accentColor, isAdmin, onOpenAdminScoreDialog)
}

private fun sanitizeScoreInput(value: String): String = if (value.length <= 2) value.filter { it.isDigit() } else value

@Composable
private fun AdminScoreRow(homeScore: String, onHomeScoreChange: (String) -> Unit, awayScore: String, onAwayScoreChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BolaoScoreField(value = homeScore, onValueChange = { onHomeScoreChange(sanitizeScoreInput(it)) })
        BolaoText(
            stringResource(Res.string.match_card_admin_dialog_versus),
            modifier = Modifier.padding(horizontal = BolaoSpacing.lg),
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        BolaoScoreField(value = awayScore, onValueChange = { onAwayScoreChange(sanitizeScoreInput(it)) })
    }
}

@Composable
fun AdminScoreDialog(match: Match, onDismiss: () -> Unit, onConfirm: (Int?, Int?) -> Unit) {
    var hS by remember { mutableStateOf(match.homeScore?.toString() ?: "0") }
    var aS by remember { mutableStateOf(match.awayScore?.toString() ?: "0") }
    BolaoDialog(
        onDismissRequest = onDismiss,
        title = { BolaoText(stringResource(Res.string.match_card_admin_dialog_title), color = Color.White) },
        text = {
            Column(modifier = Modifier.imePadding().padding(bottom = BolaoSpacing.xxl)) {
                BolaoText(
                    stringResource(Res.string.match_card_admin_dialog_message, match.homeTeam, match.awayTeam),
                    fontSize = BolaoTypography.bodyLarge.fontSize,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = BolaoSpacing.lg)
                )
                AdminScoreRow(hS, { hS = it }, aS, { aS = it })
            }
        },
        confirmButton = {
            BolaoTextButton(onClick = {
                onConfirm(hS.toIntOrNull() ?: 0, aS.toIntOrNull() ?: 0)
            }) {
                BolaoText(stringResource(Res.string.match_card_admin_dialog_save), color = Neon, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            BolaoTextButton(onClick = onDismiss) {
                BolaoText(stringResource(Res.string.match_card_admin_dialog_cancel), color = Color.White.copy(alpha = 0.6f))
            }
        },
        containerColor = DeepNavy
    )
}
