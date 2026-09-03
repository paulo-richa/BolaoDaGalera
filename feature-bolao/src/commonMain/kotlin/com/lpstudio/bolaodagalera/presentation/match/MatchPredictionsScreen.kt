package com.lpstudio.bolaodagalera.presentation.match

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
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
import bolaodagalera.feature_bolao.generated.resources.match_predictions_back_cd
import bolaodagalera.feature_bolao.generated.resources.match_predictions_locked_badge
import bolaodagalera.feature_bolao.generated.resources.match_predictions_no_prediction
import bolaodagalera.feature_bolao.generated.resources.match_predictions_score_separator
import bolaodagalera.feature_bolao.generated.resources.match_predictions_share_cd
import bolaodagalera.feature_bolao.generated.resources.match_predictions_share_correct_icon
import bolaodagalera.feature_bolao.generated.resources.match_predictions_share_exact_icon
import bolaodagalera.feature_bolao.generated.resources.match_predictions_share_header
import bolaodagalera.feature_bolao.generated.resources.match_predictions_share_locked
import bolaodagalera.feature_bolao.generated.resources.match_predictions_share_no_prediction
import bolaodagalera.feature_bolao.generated.resources.match_predictions_share_points_plural
import bolaodagalera.feature_bolao.generated.resources.match_predictions_share_points_singular
import bolaodagalera.feature_bolao.generated.resources.match_predictions_share_status_finished
import bolaodagalera.feature_bolao.generated.resources.match_predictions_share_status_ongoing
import bolaodagalera.feature_bolao.generated.resources.match_predictions_share_wrong_icon
import bolaodagalera.feature_bolao.generated.resources.match_predictions_status_admin_view
import bolaodagalera.feature_bolao.generated.resources.match_predictions_status_extra_time
import bolaodagalera.feature_bolao.generated.resources.match_predictions_status_finished
import bolaodagalera.feature_bolao.generated.resources.match_predictions_status_going_extra_time
import bolaodagalera.feature_bolao.generated.resources.match_predictions_status_going_penalties
import bolaodagalera.feature_bolao.generated.resources.match_predictions_status_halftime
import bolaodagalera.feature_bolao.generated.resources.match_predictions_status_in_progress
import bolaodagalera.feature_bolao.generated.resources.match_predictions_status_penalties
import bolaodagalera.feature_bolao.generated.resources.match_predictions_title
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import com.lpstudio.bolaodagalera.designsystem.components.BolaoHorizontalDivider
import com.lpstudio.bolaodagalera.designsystem.components.BolaoIcon
import com.lpstudio.bolaodagalera.designsystem.components.BolaoIconButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoLoadingIndicator
import com.lpstudio.bolaodagalera.designsystem.components.BolaoSurface
import com.lpstudio.bolaodagalera.designsystem.components.BolaoText
import com.lpstudio.bolaodagalera.designsystem.components.UserAvatar
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoRadiusShape
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoSpacing
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTypography
import com.lpstudio.bolaodagalera.designsystem.theme.DeepNavy
import com.lpstudio.bolaodagalera.designsystem.theme.GlassBorder
import com.lpstudio.bolaodagalera.designsystem.theme.Gold
import com.lpstudio.bolaodagalera.designsystem.theme.GradientHero
import com.lpstudio.bolaodagalera.designsystem.theme.NavyElevated
import com.lpstudio.bolaodagalera.designsystem.theme.Neon
import com.lpstudio.bolaodagalera.designsystem.theme.TextMuted
import com.lpstudio.bolaodagalera.designsystem.theme.TextSubtle
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.domain.model.RankingEntry
import com.lpstudio.bolaodagalera.domain.usecase.CalculatePointsUseCase
import com.lpstudio.bolaodagalera.presentation.bolao.BolaoViewModel
import com.lpstudio.bolaodagalera.util.TimeSource
import com.lpstudio.bolaodagalera.util.getInitials
import com.lpstudio.bolaodagalera.util.resolveDisplayName
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val DEFAULT_POINTS_EXACT_SCORE = 3
private const val DEFAULT_POINTS_WINNER_OR_DRAW = 1

@Composable
fun MatchPredictionsScreen(bolaoId: String, matchId: String, onNavigateBack: () -> Unit) {
    val viewModel: BolaoViewModel = koinViewModel(key = bolaoId) { parametersOf(bolaoId) }
    val uiState by viewModel.uiState.collectAsState()
    val launcherProvider = com.lpstudio.bolaodagalera.rememberLauncherProvider()

    val authRepository: com.lpstudio.bolaodagalera.domain.repository.AuthRepository = koinInject()
    val currentUserId = authRepository.currentUser?.id ?: ""
    val isOwner = uiState.bolao?.ownerId == currentUserId

    val match = uiState.matches.find { it.id == matchId }
    val predictions = uiState.allPredictions.filter { it.matchId == matchId }
    val participants = uiState.participants

    val now = TimeSource.nowMillis()
    val calculatePointsUseCase = remember { CalculatePointsUseCase() }

    val strings = loadMatchPredictionsStrings()
    val timing = computeMatchTimingState(match, now, isOwner)

    val itemsList =
        rememberPredictionItems(
            predictions = predictions,
            participants = participants,
            hReal = timing.hReal,
            aReal = timing.aReal,
            isAdminViewingBeforeStart = timing.isAdminViewingBeforeStart,
            calculatePointsUseCase = calculatePointsUseCase
        )

    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        if (match == null) {
            MatchPredictionsLoadingState()
        } else {
            MatchPredictionsContent(
                match = match,
                allMatches = uiState.matches,
                timing = timing,
                strings = strings,
                itemsList = itemsList,
                shareContext =
                MatchPredictionsShareContext(
                    currentUserId = currentUserId,
                    pointsExactScore = uiState.bolao?.pointsExactScore ?: DEFAULT_POINTS_EXACT_SCORE,
                    pointsWinnerOrDraw = uiState.bolao?.pointsWinnerOrDraw ?: DEFAULT_POINTS_WINNER_OR_DRAW,
                    launcherProvider = launcherProvider,
                    onNavigateBack = onNavigateBack
                )
            )
        }
    }
}

private data class MatchPredictionsShareContext(
    val currentUserId: String,
    val pointsExactScore: Int,
    val pointsWinnerOrDraw: Int,
    val launcherProvider: com.lpstudio.bolaodagalera.LauncherProvider,
    val onNavigateBack: () -> Unit
)

@Composable
private fun MatchPredictionsContent(
    match: Match,
    allMatches: List<Match>,
    timing: MatchTimingState,
    strings: MatchPredictionsStrings,
    itemsList: List<Triple<RankingEntry, Prediction?, Int>>,
    shareContext: MatchPredictionsShareContext
) {
    val (hName, hFlag, hResolvedCrest) = resolveDisplayName(match.id, match.homeTeam, match.homeTeamFlag, allMatches, true)
    val (aName, aFlag, aResolvedCrest) = resolveDisplayName(match.id, match.awayTeam, match.awayTeamFlag, allMatches, false)

    val statusLabel = resolveStatusLabel(match.status, timing.isAdminViewingBeforeStart, timing.isActuallyFinished, strings.status)
    val isLive = !timing.isActuallyFinished && !timing.isAdminViewingBeforeStart && timing.hasStarted

    Column(Modifier.fillMaxSize()) {
        MatchPredictionsHeader(
            home = TeamDisplay(hName, hFlag, hResolvedCrest ?: match.homeTeamCrest),
            away = TeamDisplay(aName, aFlag, aResolvedCrest ?: match.awayTeamCrest),
            score =
            ScoreDisplay(
                hReal = timing.hReal,
                aReal = timing.aReal,
                statusLabel = statusLabel,
                isLive = isLive,
                isAdminViewingBeforeStart = timing.isAdminViewingBeforeStart,
                scoreSeparator = strings.scoreSeparator
            ),
            backCd = strings.backCd,
            shareCd = strings.shareCd,
            title = strings.title,
            onNavigateBack = shareContext.onNavigateBack,
            onShare = {
                val text =
                    buildShareText(
                        ShareTextInput(
                            hName = hName,
                            hFlag = hFlag,
                            aName = aName,
                            aFlag = aFlag,
                            hReal = timing.hReal,
                            aReal = timing.aReal,
                            hasStarted = timing.hasStarted,
                            isActuallyFinished = timing.isActuallyFinished,
                            isAdminViewingBeforeStart = timing.isAdminViewingBeforeStart,
                            currentUserId = shareContext.currentUserId,
                            pointsExactScore = shareContext.pointsExactScore,
                            pointsWinnerOrDraw = shareContext.pointsWinnerOrDraw,
                            items = itemsList,
                            strings = strings.share
                        )
                    )
                shareContext.launcherProvider.shareText(text)
            }
        )

        MatchPredictionsList(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            items = itemsList,
            isAdminViewingBeforeStart = timing.isAdminViewingBeforeStart,
            currentUserId = shareContext.currentUserId,
            lockedBadge = strings.lockedBadge,
            noPredictionLabel = strings.noPrediction
        )
    }
}

private data class MatchTimingState(
    val hReal: Int,
    val aReal: Int,
    val isActuallyFinished: Boolean,
    val hasStarted: Boolean,
    val isAdminViewingBeforeStart: Boolean
)

/** Computes score/timing derived state, forcing "Finished" when the match started more than 3 hours ago and has a score. */
/** A match with a score but no live status update is assumed finished after this long. */
private const val ASSUMED_FINISHED_AFTER_MILLIS = 3 * 3_600_000L

private fun computeMatchTimingState(match: Match?, now: Long, isOwner: Boolean): MatchTimingState {
    val hReal = match?.homeScore ?: 0
    val aReal = match?.awayScore ?: 0
    val matchDate = match?.matchDateMillis ?: 0L
    val isActuallyFinished =
        (match?.status == "FINISHED") ||
            (match?.status == "PENALTIES") ||
            (match?.status == "PAUSED_PENALTIES") ||
            (match?.homeScore != null && match?.awayScore != null && now > (matchDate + ASSUMED_FINISHED_AFTER_MILLIS))
    val hasStarted = now >= matchDate
    val isAdminViewingBeforeStart = isOwner && !hasStarted
    return MatchTimingState(hReal, aReal, isActuallyFinished, hasStarted, isAdminViewingBeforeStart)
}

private fun resolveStatusLabel(
    matchStatus: String?,
    isAdminViewingBeforeStart: Boolean,
    isActuallyFinished: Boolean,
    strings: StatusStrings
): String = when {
    isAdminViewingBeforeStart -> strings.adminView
    isActuallyFinished -> strings.finished
    matchStatus == "EXTRA_TIME" -> strings.extraTime
    matchStatus == "PENALTIES" -> strings.penalties
    matchStatus == "PAUSED_EXTRA_TIME" -> strings.goingExtraTime
    matchStatus == "PAUSED_PENALTIES" -> strings.goingPenalties
    matchStatus == "PAUSED" -> strings.halftime
    else -> strings.inProgress
}

@Composable
private fun rememberPredictionItems(
    predictions: List<Prediction>,
    participants: List<RankingEntry>,
    hReal: Int,
    aReal: Int,
    isAdminViewingBeforeStart: Boolean,
    calculatePointsUseCase: CalculatePointsUseCase
): List<Triple<RankingEntry, Prediction?, Int>> = remember(predictions, participants, hReal, aReal, isAdminViewingBeforeStart) {
    participants.map { participant ->
        val pred = predictions.find { it.userId == participant.userId }
        val pts =
            if (pred != null && !isAdminViewingBeforeStart) {
                calculatePointsUseCase(pred, hReal, aReal)
            } else {
                0
            }
        Triple(participant, pred, pts)
    }.sortedWith(
        if (isAdminViewingBeforeStart) {
            compareBy { it.first.userName.lowercase() }
        } else {
            compareByDescending<Triple<RankingEntry, Prediction?, Int>> { it.third }
                .thenBy { it.first.userName.lowercase() }
        }
    )
}

private data class StatusStrings(
    val adminView: String,
    val finished: String,
    val extraTime: String,
    val penalties: String,
    val goingExtraTime: String,
    val goingPenalties: String,
    val halftime: String,
    val inProgress: String
)

private data class MatchPredictionsStrings(
    val backCd: String,
    val shareCd: String,
    val title: String,
    val scoreSeparator: String,
    val share: ShareStrings,
    val status: StatusStrings,
    val lockedBadge: String,
    val noPrediction: String
)

@Composable
private fun loadMatchPredictionsStrings(): MatchPredictionsStrings = MatchPredictionsStrings(
    backCd = stringResource(Res.string.match_predictions_back_cd),
    shareCd = stringResource(Res.string.match_predictions_share_cd),
    title = stringResource(Res.string.match_predictions_title),
    scoreSeparator = stringResource(Res.string.match_predictions_score_separator),
    share =
    ShareStrings(
        header = stringResource(Res.string.match_predictions_share_header),
        statusFinished = stringResource(Res.string.match_predictions_share_status_finished),
        statusOngoing = stringResource(Res.string.match_predictions_share_status_ongoing),
        locked = stringResource(Res.string.match_predictions_share_locked),
        exactIcon = stringResource(Res.string.match_predictions_share_exact_icon),
        correctIcon = stringResource(Res.string.match_predictions_share_correct_icon),
        wrongIcon = stringResource(Res.string.match_predictions_share_wrong_icon),
        pointsSingular = stringResource(Res.string.match_predictions_share_points_singular),
        pointsPlural = stringResource(Res.string.match_predictions_share_points_plural),
        noPrediction = stringResource(Res.string.match_predictions_share_no_prediction)
    ),
    status =
    StatusStrings(
        adminView = stringResource(Res.string.match_predictions_status_admin_view),
        finished = stringResource(Res.string.match_predictions_status_finished),
        extraTime = stringResource(Res.string.match_predictions_status_extra_time),
        penalties = stringResource(Res.string.match_predictions_status_penalties),
        goingExtraTime = stringResource(Res.string.match_predictions_status_going_extra_time),
        goingPenalties = stringResource(Res.string.match_predictions_status_going_penalties),
        halftime = stringResource(Res.string.match_predictions_status_halftime),
        inProgress = stringResource(Res.string.match_predictions_status_in_progress)
    ),
    lockedBadge = stringResource(Res.string.match_predictions_locked_badge),
    noPrediction = stringResource(Res.string.match_predictions_no_prediction)
)

@Composable
private fun MatchPredictionsLoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        BolaoLoadingIndicator()
    }
}

private data class TeamDisplay(val name: String, val flag: String, val crestUrl: String?)

private data class ScoreDisplay(
    val hReal: Int,
    val aReal: Int,
    val statusLabel: String,
    val isLive: Boolean,
    val isAdminViewingBeforeStart: Boolean,
    val scoreSeparator: String
)

@Composable
private fun MatchPredictionsHeader(
    home: TeamDisplay,
    away: TeamDisplay,
    score: ScoreDisplay,
    backCd: String,
    shareCd: String,
    title: String,
    onNavigateBack: () -> Unit,
    onShare: () -> Unit
) {
    Box(
        modifier =
        Modifier
            .fillMaxWidth()
            .background(GradientHero)
            .drawBehind {
                drawRect(
                    brush =
                    Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.05f), Color.Transparent),
                        endY = size.height * 0.5f
                    )
                )
                drawCircle(
                    brush =
                    Brush.radialGradient(
                        colors = listOf(Neon.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width * 0.9f, 0f),
                        radius = 220.dp.toPx()
                    ),
                    radius = 220.dp.toPx(),
                    center = Offset(size.width * 0.9f, 0f)
                )
            }
            .padding(top = BolaoSpacing.md, bottom = BolaoSpacing.xxl)
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = BolaoSpacing.xl)) {
            MatchPredictionsTopBar(
                backCd = backCd,
                shareCd = shareCd,
                title = title,
                onNavigateBack = onNavigateBack,
                onShare = onShare
            )

            Spacer(Modifier.height(24.dp))

            MatchPredictionsTeamsRow(home = home, away = away, score = score)
        }
    }
}

@Composable
private fun MatchPredictionsTopBar(backCd: String, shareCd: String, title: String, onNavigateBack: () -> Unit, onShare: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.xs)
    ) {
        BolaoIconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
            BolaoIcon(
                Icons.AutoMirrored.Filled.ArrowBack,
                backCd,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        BolaoText(
            title,
            fontSize = BolaoTypography.headlineMedium.fontSize,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            modifier = Modifier.weight(1f),
            letterSpacing = (-0.5).sp
        )

        BolaoIconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
            BolaoIcon(Icons.Default.Share, shareCd, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun MatchPredictionsTeamsRow(home: TeamDisplay, away: TeamDisplay, score: ScoreDisplay) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MatchPredictionsTeamColumn(team = home, modifier = Modifier.weight(1f))
        MatchPredictionsScoreStatus(score = score)
        MatchPredictionsTeamColumn(team = away, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MatchPredictionsTeamColumn(team: TeamDisplay, modifier: Modifier = Modifier) {
    val annotatedFlag = remember(team.flag) { buildFlagAnnotatedString(team.flag) }

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        TeamCrestCircle(
            url = team.crestUrl,
            flag = annotatedFlag,
            isTbd = team.flag.contains(" ou "),
            flagSize = 34.sp
        )
        if (team.name.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            ShrinkToFitTeamName(team.name)
        }
    }
}

@Composable
private fun ShrinkToFitTeamName(name: String) {
    var fontSize by remember(name) { mutableIntStateOf(13) }
    var readyToDraw by remember(name) { mutableStateOf(false) }
    BolaoText(
        name,
        color = Color.White,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        maxLines = 1,
        softWrap = false,
        modifier = Modifier.drawWithContent { if (readyToDraw) drawContent() },
        onTextLayout = { result ->
            if (result.hasVisualOverflow && fontSize > 8) {
                fontSize -= 1
            } else {
                readyToDraw = true
            }
        }
    )
}

@Composable
private fun MatchPredictionsScoreStatus(score: ScoreDisplay) {
    Column(Modifier.padding(horizontal = BolaoSpacing.md), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (score.isLive) {
                val infiniteTransition = rememberInfiniteTransition()
                val liveDotAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec =
                    infiniteRepeatable(
                        animation = tween(800, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                Box(
                    modifier =
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Neon.copy(alpha = liveDotAlpha))
                )
                Spacer(Modifier.width(6.dp))
            }
            BolaoText(
                score.statusLabel,
                fontSize = BolaoTypography.bodyMedium.fontSize,
                fontWeight = FontWeight.Bold,
                color = if (score.isAdminViewingBeforeStart) Gold else TextMuted,
                letterSpacing = 0.5.sp
            )
        }
        Spacer(Modifier.height(8.dp))
        if (!score.isAdminViewingBeforeStart) {
            MatchPredictionsScoreNumbers(hReal = score.hReal, aReal = score.aReal, scoreSeparator = score.scoreSeparator)
        } else {
            BolaoIcon(
                Icons.Default.Lock,
                null,
                tint = TextMuted.copy(alpha = 0.5f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun MatchPredictionsScoreNumbers(hReal: Int, aReal: Int, scoreSeparator: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.sm)
    ) {
        BolaoText(
            hReal.toString(),
            fontSize = BolaoTypography.displayMedium.fontSize,
            fontWeight = FontWeight.Black,
            color = Neon
        )
        BolaoText(
            scoreSeparator,
            fontSize = BolaoTypography.headlineMedium.fontSize,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )
        BolaoText(
            aReal.toString(),
            fontSize = BolaoTypography.displayMedium.fontSize,
            fontWeight = FontWeight.Black,
            color = Neon
        )
    }
}

@Composable
private fun MatchPredictionsList(
    modifier: Modifier,
    items: List<Triple<RankingEntry, Prediction?, Int>>,
    isAdminViewingBeforeStart: Boolean,
    currentUserId: String,
    lockedBadge: String,
    noPredictionLabel: String
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
    ) {
        item {
            BolaoHorizontalDivider(
                color = GlassBorder,
                thickness = 1.dp,
                modifier = Modifier.padding(start = BolaoSpacing.xxl, end = BolaoSpacing.xxl, bottom = BolaoSpacing.md)
            )
        }

        items(items) { item ->
            MatchPredictionsListItem(
                participant = item.first,
                pred = item.second,
                pts = item.third,
                isAdminViewingBeforeStart = isAdminViewingBeforeStart,
                currentUserId = currentUserId,
                lockedBadge = lockedBadge,
                noPredictionLabel = noPredictionLabel
            )
        }
    }
}

@Composable
private fun MatchPredictionsListItem(
    participant: RankingEntry,
    pred: Prediction?,
    pts: Int,
    isAdminViewingBeforeStart: Boolean,
    currentUserId: String,
    lockedBadge: String,
    noPredictionLabel: String
) {
    BolaoSurface(
        color = NavyElevated,
        shape = BolaoRadiusShape.lg,
        border = BorderStroke(1.dp, GlassBorder),
        modifier = Modifier.padding(horizontal = BolaoSpacing.xl, vertical = BolaoSpacing.xs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = BolaoSpacing.lg, vertical = BolaoSpacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                initials = participant.userName.getInitials(),
                size = 36.dp,
                fontSize = BolaoTypography.bodyLarge.fontSize,
                borderColor = Neon.copy(alpha = 0.5f)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val hasNickname = participant.userNickname.isNotBlank()
                BolaoText(
                    text = if (hasNickname) participant.userNickname else participant.userName,
                    color = Color.White,
                    fontSize = BolaoTypography.titleLarge.fontSize,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                if (hasNickname) {
                    BolaoText(
                        text = participant.userName,
                        color = TextMuted,
                        fontSize = BolaoTypography.bodyMedium.fontSize,
                        maxLines = 1
                    )
                }
            }
            if (pred != null) {
                MatchPredictionsListItemPrediction(
                    pred = pred,
                    pts = pts,
                    isAdminViewingBeforeStart = isAdminViewingBeforeStart,
                    participantUserId = participant.userId,
                    currentUserId = currentUserId,
                    lockedBadge = lockedBadge
                )
            } else {
                BolaoText(
                    noPredictionLabel,
                    color = TextSubtle,
                    fontSize = BolaoTypography.bodyMedium.fontSize,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MatchPredictionsListItemPrediction(
    pred: Prediction,
    pts: Int,
    isAdminViewingBeforeStart: Boolean,
    participantUserId: String,
    currentUserId: String,
    lockedBadge: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.md)
    ) {
        MatchPredictionsScoreBadge(
            pred = pred,
            isAdminViewingBeforeStart = isAdminViewingBeforeStart,
            participantUserId = participantUserId,
            currentUserId = currentUserId,
            lockedBadge = lockedBadge
        )
        if (!isAdminViewingBeforeStart) {
            MatchPredictionsPointsBadge(pts)
        }
    }
}

@Composable
private fun MatchPredictionsScoreBadge(
    pred: Prediction,
    isAdminViewingBeforeStart: Boolean,
    participantUserId: String,
    currentUserId: String,
    lockedBadge: String
) {
    if (isAdminViewingBeforeStart && participantUserId != currentUserId) {
        BolaoText(
            lockedBadge,
            color = Neon,
            fontSize = BolaoTypography.bodyLarge.fontSize,
            fontWeight = FontWeight.Bold
        )
    } else {
        Box(
            modifier =
            Modifier.clip(
                BolaoRadiusShape.sm
            ).background(
                DeepNavy.copy(alpha = 0.6f)
            ).border(
                1.dp,
                GlassBorder,
                BolaoRadiusShape.sm
            ).padding(horizontal = BolaoSpacing.md, vertical = BolaoSpacing.sm)
        ) {
            BolaoText(
                "${pred.homeScore} × ${pred.awayScore}",
                color = Color.White,
                fontSize = BolaoTypography.bodyLarge.fontSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MatchPredictionsPointsBadge(pts: Int) {
    val pointsColor =
        when (pts) {
            3 -> Neon
            1 -> Gold
            else -> TextMuted.copy(alpha = 0.4f)
        }
    Box(
        modifier =
        Modifier.width(
            44.dp
        ).clip(
            BolaoRadiusShape.md
        ).background(
            pointsColor.copy(alpha = 0.12f)
        ).border(
            1.dp,
            pointsColor.copy(alpha = 0.2f),
            BolaoRadiusShape.md
        ).padding(vertical = BolaoSpacing.sm),
        contentAlignment = Alignment.Center
    ) {
        BolaoText(
            text = if (pts > 0) "+$pts" else "0",
            color = pointsColor,
            fontSize = BolaoTypography.titleLarge.fontSize,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun TeamCrestCircle(url: String?, flag: AnnotatedString, isTbd: Boolean, flagSize: androidx.compose.ui.unit.TextUnit) {
    Box(
        modifier =
        Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(NavyElevated.copy(alpha = 0.6f))
            .border(1.dp, GlassBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!url.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model =
                ImageRequest.Builder(LocalPlatformContext.current)
                    .data(url)
                    .decoderFactory(SvgDecoder.Factory())
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                loading = {
                    BolaoLoadingIndicator(modifier = Modifier.size(16.dp))
                },
                error = {
                    BolaoText(
                        text = flag,
                        fontSize = if (isTbd) 16.sp else flagSize,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            )
        } else {
            BolaoText(
                text = flag,
                fontSize = if (isTbd) 16.sp else flagSize,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/** Splits a "TeamA ou TeamB" placeholder flag into a two-style annotated string; other flags render as-is. */
private fun buildFlagAnnotatedString(flag: String): AnnotatedString {
    val parts = flag.split(" ou ")
    return if (parts.size > 1) {
        buildAnnotatedString {
            parts.forEachIndexed { index, part ->
                append(part)
                if (index < parts.size - 1) {
                    withStyle(
                        style = SpanStyle(
                            fontSize = BolaoTypography.bodyMedium.fontSize,
                            fontWeight = FontWeight.Normal
                        )
                    ) {
                        append(" ou ")
                    }
                }
            }
        }
    } else {
        AnnotatedString(flag)
    }
}

private data class ShareStrings(
    val header: String,
    val statusFinished: String,
    val statusOngoing: String,
    val locked: String,
    val exactIcon: String,
    val correctIcon: String,
    val wrongIcon: String,
    val pointsSingular: String,
    val pointsPlural: String,
    val noPrediction: String
)

private data class ShareTextInput(
    val hName: String,
    val hFlag: String,
    val aName: String,
    val aFlag: String,
    val hReal: Int,
    val aReal: Int,
    val hasStarted: Boolean,
    val isActuallyFinished: Boolean,
    val isAdminViewingBeforeStart: Boolean,
    val currentUserId: String,
    val pointsExactScore: Int,
    val pointsWinnerOrDraw: Int,
    val items: List<Triple<RankingEntry, Prediction?, Int>>,
    val strings: ShareStrings
)

/** Builds the plain-text share message for the match predictions, matching the on-screen ordering and labels. */
private fun buildShareText(input: ShareTextInput): String {
    val isOngoing = input.hasStarted && !input.isActuallyFinished

    // Alphabetical sort by displayed nickname/name while the match is ongoing
    val shareItems =
        if (isOngoing) {
            input.items.sortedBy { (it.first.userNickname.ifBlank { it.first.userName }).lowercase() }
        } else {
            input.items
        }

    val list = shareItems.mapIndexed { index, item -> buildShareParticipantLine(item, index, input) }.joinToString("\n")

    return buildShareHeaderText(input) + list
}

private fun buildShareHeaderText(input: ShareTextInput): String = buildString {
    append(input.strings.header)
    append("${input.hFlag} ${input.hName} ")
    if (input.hasStarted || input.isActuallyFinished) {
        append("${input.hReal} x ${input.aReal} ")
    } else {
        append("x ")
    }
    append("${input.aName} ${input.aFlag}")

    if (input.hasStarted || input.isActuallyFinished) {
        val label = if (input.isActuallyFinished) input.strings.statusFinished else input.strings.statusOngoing
        append(label)
    }
    append("\n\n")
}

private fun buildShareParticipantLine(item: Triple<RankingEntry, Prediction?, Int>, index: Int, input: ShareTextInput): String {
    val participant = item.first
    val pred = item.second
    val pts = item.third
    val name = participant.userNickname.ifBlank { participant.userName }

    if (pred == null) return "${index + 1}. $name: ${input.strings.noPrediction}"

    val score =
        if (input.isAdminViewingBeforeStart && participant.userId != input.currentUserId) {
            input.strings.locked
        } else {
            "${pred.homeScore} x ${pred.awayScore}"
        }

    val ptsIcon =
        if (!input.isAdminViewingBeforeStart && input.isActuallyFinished) {
            when (pts) {
                input.pointsExactScore -> input.strings.exactIcon
                input.pointsWinnerOrDraw -> input.strings.correctIcon
                else -> input.strings.wrongIcon
            }
        } else {
            ""
        }

    val pointsLabel =
        if (!input.isAdminViewingBeforeStart && input.isActuallyFinished) {
            if (pts == 1) {
                input.strings.pointsSingular.replace("%1\$d", pts.toString())
            } else {
                input.strings.pointsPlural.replace("%1\$d", pts.toString())
            }
        } else {
            ""
        }

    return "${index + 1}. $name: $score$pointsLabel$ptsIcon"
}
