package com.lpstudio.bolaodagalera.presentation.match

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bolaodagalera.feature_bolao.generated.resources.Res
import bolaodagalera.feature_bolao.generated.resources.prediction_back_cd
import bolaodagalera.feature_bolao.generated.resources.prediction_button_save
import bolaodagalera.feature_bolao.generated.resources.prediction_button_update
import bolaodagalera.feature_bolao.generated.resources.prediction_group_label
import bolaodagalera.feature_bolao.generated.resources.prediction_point_correct_emoji
import bolaodagalera.feature_bolao.generated.resources.prediction_point_correct_label
import bolaodagalera.feature_bolao.generated.resources.prediction_point_exact_emoji
import bolaodagalera.feature_bolao.generated.resources.prediction_point_exact_label
import bolaodagalera.feature_bolao.generated.resources.prediction_point_wrong_emoji
import bolaodagalera.feature_bolao.generated.resources.prediction_point_wrong_label
import bolaodagalera.feature_bolao.generated.resources.prediction_points_badge_plural
import bolaodagalera.feature_bolao.generated.resources.prediction_points_badge_singular
import bolaodagalera.feature_bolao.generated.resources.prediction_rule_emoji
import bolaodagalera.feature_bolao.generated.resources.prediction_rule_text
import bolaodagalera.feature_bolao.generated.resources.prediction_score_question
import bolaodagalera.feature_bolao.generated.resources.prediction_score_separator
import bolaodagalera.feature_bolao.generated.resources.prediction_stepper_decrement
import bolaodagalera.feature_bolao.generated.resources.prediction_stepper_increment
import bolaodagalera.feature_bolao.generated.resources.prediction_success_subtitle
import bolaodagalera.feature_bolao.generated.resources.prediction_success_title
import bolaodagalera.feature_bolao.generated.resources.prediction_vs_label
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import com.lpstudio.bolaodagalera.designsystem.components.BolaoHorizontalDivider
import com.lpstudio.bolaodagalera.designsystem.components.BolaoIcon
import com.lpstudio.bolaodagalera.designsystem.components.BolaoIconButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoLoadingIndicator
import com.lpstudio.bolaodagalera.designsystem.components.BolaoText
import com.lpstudio.bolaodagalera.designsystem.components.BolaoVerticalDivider
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoRadiusShape
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoSpacing
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTypography
import com.lpstudio.bolaodagalera.designsystem.theme.DeepNavy
import com.lpstudio.bolaodagalera.designsystem.theme.ErrorRed
import com.lpstudio.bolaodagalera.designsystem.theme.GlassBorder
import com.lpstudio.bolaodagalera.designsystem.theme.Gold
import com.lpstudio.bolaodagalera.designsystem.theme.GradientHero
import com.lpstudio.bolaodagalera.designsystem.theme.GradientPrimary
import com.lpstudio.bolaodagalera.designsystem.theme.NavyCard
import com.lpstudio.bolaodagalera.designsystem.theme.NavyElevated
import com.lpstudio.bolaodagalera.designsystem.theme.Neon
import com.lpstudio.bolaodagalera.designsystem.theme.PinkNeon
import com.lpstudio.bolaodagalera.designsystem.theme.TextMuted
import com.lpstudio.bolaodagalera.designsystem.theme.TextSubtle
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.util.resolveDisplayName
import kotlin.random.Random
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PredictionScreen(bolaoId: String, matchId: String, onSaved: () -> Unit, onNavigateBack: () -> Unit) {
    val viewModel: PredictionViewModel = koinViewModel(key = "$bolaoId/$matchId") { parametersOf(bolaoId, matchId) }
    val uiState by viewModel.uiState.collectAsState()
    val authRepository = koinInject<AuthRepository>()
    val userId = authRepository.currentUser?.id ?: ""

    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(userId) { viewModel.load(userId) }
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            showSuccess = true
            delay(1000)
            onSaved()
        }
    }

    var homeScore by remember { mutableIntStateOf(0) }
    var awayScore by remember { mutableIntStateOf(0) }

    val strings = rememberPredictionStrings()

    LaunchedEffect(uiState.existingPrediction) {
        uiState.existingPrediction?.let {
            homeScore = it.homeScore
            awayScore = it.awayScore
        }
    }

    val score =
        ScoreState(
            homeScore = homeScore,
            awayScore = awayScore,
            onHomeIncrement = { homeScore++ },
            onHomeDecrement = { if (homeScore > 0) homeScore-- },
            onAwayIncrement = { awayScore++ },
            onAwayDecrement = { if (awayScore > 0) awayScore-- }
        )

    PredictionScreenBody(
        uiState = uiState,
        showSuccess = showSuccess,
        score = score,
        onNavigateBack = onNavigateBack,
        onSave = { viewModel.savePrediction(userId, homeScore, awayScore) },
        strings = strings
    )
}

@Composable
private fun PredictionScreenBody(
    uiState: PredictionUiState,
    showSuccess: Boolean,
    score: ScoreState,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    strings: PredictionStrings
) {
    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        when {
            uiState.isLoading && uiState.match == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    BolaoLoadingIndicator()
                }
            }

            uiState.match != null -> {
                val match = uiState.match!!
                val allMatches = uiState.allMatches

                val (homeDisplayName, homeDisplayFlag, homeResolvedCrest) =
                    remember(match.id, match.homeTeam, match.homeTeamFlag, allMatches) {
                        resolveDisplayName(match.id, match.homeTeam, match.homeTeamFlag, allMatches, true)
                    }
                val (awayDisplayName, awayDisplayFlag, awayResolvedCrest) =
                    remember(match.id, match.awayTeam, match.awayTeamFlag, allMatches) {
                        resolveDisplayName(match.id, match.awayTeam, match.awayTeamFlag, allMatches, false)
                    }

                PredictionMatchContent(
                    uiState = uiState,
                    match = match,
                    home = PredictionTeamDisplay(homeDisplayName, homeDisplayFlag, homeResolvedCrest ?: match.homeTeamCrest),
                    away = PredictionTeamDisplay(awayDisplayName, awayDisplayFlag, awayResolvedCrest ?: match.awayTeamCrest),
                    score = score,
                    onNavigateBack = onNavigateBack,
                    onSave = onSave,
                    strings = strings
                )
            }
        }

        if (showSuccess) {
            SuccessOverlay()
        }
    }
}

/** Pre-resolved user-facing strings threaded through the prediction content tree, avoiding repeated stringResource lookups. */
private data class PredictionStrings(
    val backCd: String,
    val groupLabelTemplate: String,
    val vsLabel: String,
    val scoreQuestion: String,
    val scoreSeparator: String,
    val pointExactEmoji: String,
    val pointExactLabel: String,
    val pointCorrectEmoji: String,
    val pointCorrectLabel: String,
    val pointWrongEmoji: String,
    val pointWrongLabel: String,
    val ruleEmoji: String,
    val ruleText: String,
    val updateButtonText: String,
    val saveButtonText: String
)

@Composable
private fun rememberPredictionStrings(): PredictionStrings = PredictionStrings(
    backCd = stringResource(Res.string.prediction_back_cd),
    groupLabelTemplate = stringResource(Res.string.prediction_group_label),
    vsLabel = stringResource(Res.string.prediction_vs_label),
    scoreQuestion = stringResource(Res.string.prediction_score_question),
    scoreSeparator = stringResource(Res.string.prediction_score_separator),
    pointExactEmoji = stringResource(Res.string.prediction_point_exact_emoji),
    pointExactLabel = stringResource(Res.string.prediction_point_exact_label),
    pointCorrectEmoji = stringResource(Res.string.prediction_point_correct_emoji),
    pointCorrectLabel = stringResource(Res.string.prediction_point_correct_label),
    pointWrongEmoji = stringResource(Res.string.prediction_point_wrong_emoji),
    pointWrongLabel = stringResource(Res.string.prediction_point_wrong_label),
    ruleEmoji = stringResource(Res.string.prediction_rule_emoji),
    ruleText = stringResource(Res.string.prediction_rule_text),
    updateButtonText = stringResource(Res.string.prediction_button_update),
    saveButtonText = stringResource(Res.string.prediction_button_save)
)

/** Resolved display data (name/flag/crest) for one team in the prediction header and score picker. */
private data class PredictionTeamDisplay(val name: String, val flag: String, val crestUrl: String?)

/** Current score selection plus the stepper callbacks, bundled to keep composable parameter lists short. */
private data class ScoreState(
    val homeScore: Int,
    val awayScore: Int,
    val onHomeIncrement: () -> Unit,
    val onHomeDecrement: () -> Unit,
    val onAwayIncrement: () -> Unit,
    val onAwayDecrement: () -> Unit
)

@Composable
private fun PredictionMatchContent(
    uiState: PredictionUiState,
    match: com.lpstudio.bolaodagalera.domain.model.Match,
    home: PredictionTeamDisplay,
    away: PredictionTeamDisplay,
    score: ScoreState,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    strings: PredictionStrings
) {
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
            Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PredictionStadiumHeader(match = match, home = home, away = away, onNavigateBack = onNavigateBack, strings = strings)

            Spacer(Modifier.height(32.dp))

            PredictionScorePicker(score = score, home = home, away = away, strings = strings)

            Spacer(Modifier.height(32.dp))

            PredictionPointsInfoCard(
                pointsExactScore = uiState.bolao?.pointsExactScore ?: 3,
                pointsWinnerOrDraw = uiState.bolao?.pointsWinnerOrDraw ?: 1,
                strings = strings
            )

            uiState.error?.let {
                Spacer(Modifier.height(12.dp))
                BolaoText(
                    it,
                    color = ErrorRed,
                    fontSize = BolaoTypography.bodyMedium.fontSize,
                    modifier = Modifier.padding(horizontal = BolaoSpacing.xl)
                )
            }

            Spacer(Modifier.height(24.dp))
            Spacer(Modifier.imePadding())
        }

        // ── Save button (Sticky) ──────────────────────────────────
        Box(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = BolaoSpacing.xl)
                .padding(bottom = BolaoSpacing.xxl, top = BolaoSpacing.sm)
        ) {
            GradientSaveButton(
                text = if (uiState.existingPrediction != null) strings.updateButtonText else strings.saveButtonText,
                isLoading = uiState.isLoading,
                onClick = onSave
            )
        }
    }
}

@Composable
private fun PredictionStadiumHeader(
    match: com.lpstudio.bolaodagalera.domain.model.Match,
    home: PredictionTeamDisplay,
    away: PredictionTeamDisplay,
    onNavigateBack: () -> Unit,
    strings: PredictionStrings
) {
    Box(
        modifier =
        Modifier
            .fillMaxWidth()
            .background(GradientHero)
            .padding(top = BolaoSpacing.lg, bottom = BolaoSpacing.xxl, start = BolaoSpacing.sm, end = BolaoSpacing.xl)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BolaoIconButton(onClick = onNavigateBack, modifier = Modifier.size(44.dp)) {
                    BolaoIcon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.backCd,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                match.group?.let { group ->
                    PredictionGroupBadge(group = group, phaseLabel = match.phase.label, groupLabelTemplate = strings.groupLabelTemplate)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Teams display
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = BolaoSpacing.lg),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeamHero(flag = home.flag, name = home.name, crestUrl = home.crestUrl)

                BolaoText(
                    strings.vsLabel,
                    fontSize = BolaoTypography.titleLarge.fontSize,
                    fontWeight = FontWeight.Black,
                    color = TextMuted.copy(alpha = 0.4f),
                    modifier = Modifier.padding(bottom = BolaoSpacing.huge),
                    letterSpacing = 2.sp
                )

                TeamHero(flag = away.flag, name = away.name, crestUrl = away.crestUrl)
            }
        }
    }
}

@Composable
private fun PredictionGroupBadge(group: String, phaseLabel: String, groupLabelTemplate: String) {
    Box(
        modifier =
        Modifier
            .clip(BolaoRadiusShape.sm)
            .background(NavyElevated)
            .border(1.dp, GlassBorder, BolaoRadiusShape.sm)
            .padding(horizontal = BolaoSpacing.md, vertical = BolaoSpacing.sm)
    ) {
        BolaoText(
            groupLabelTemplate.replace("%1\$s", group).replace("%2\$s", phaseLabel),
            fontSize = BolaoTypography.bodyMedium.fontSize,
            color = TextMuted,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PredictionScorePicker(score: ScoreState, home: PredictionTeamDisplay, away: PredictionTeamDisplay, strings: PredictionStrings) {
    BolaoText(
        strings.scoreQuestion,
        fontSize = BolaoTypography.headlineSmall.fontSize,
        fontWeight = FontWeight.Black,
        color = Color.White,
        letterSpacing = 0.5.sp
    )
    Spacer(Modifier.height(24.dp))

    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = BolaoSpacing.huge),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScoreStepper(
            value = score.homeScore,
            onIncrement = score.onHomeIncrement,
            onDecrement = score.onHomeDecrement,
            teamName = home.name
        )

        BolaoText(
            strings.scoreSeparator,
            fontSize = BolaoTypography.displayMedium.fontSize,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )

        ScoreStepper(
            value = score.awayScore,
            onIncrement = score.onAwayIncrement,
            onDecrement = score.onAwayDecrement,
            teamName = away.name
        )
    }
}

@Composable
private fun PredictionPointsInfoCard(pointsExactScore: Int, pointsWinnerOrDraw: Int, strings: PredictionStrings) {
    Column(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = BolaoSpacing.xl)
            .clip(BolaoRadiusShape.lg)
            .background(NavyCard)
            .border(1.dp, GlassBorder, BolaoRadiusShape.lg)
            .padding(BolaoSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(BolaoSpacing.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PointBadge(
                emoji = strings.pointExactEmoji,
                pts = pointsExactScore.toString(),
                label = strings.pointExactLabel
            )
            BolaoVerticalDivider(color = GlassBorder, modifier = Modifier.height(48.dp))
            PointBadge(
                emoji = strings.pointCorrectEmoji,
                pts = pointsWinnerOrDraw.toString(),
                label = strings.pointCorrectLabel
            )
            BolaoVerticalDivider(color = GlassBorder, modifier = Modifier.height(48.dp))
            PointBadge(
                emoji = strings.pointWrongEmoji,
                pts = "0",
                label = strings.pointWrongLabel
            )
        }

        BolaoHorizontalDivider(color = GlassBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.md),
            modifier = Modifier.padding(horizontal = BolaoSpacing.xs)
        ) {
            BolaoText(strings.ruleEmoji, fontSize = BolaoTypography.bodyLarge.fontSize)
            BolaoText(
                strings.ruleText,
                fontSize = BolaoTypography.bodyMedium.fontSize,
                color = TextMuted,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun SuccessOverlay() {
    var startAnim by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec =
        spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val confettiParticles =
        remember {
            List(30) {
                ConfettiState(
                    color = listOf(Neon, Gold, Color.Cyan, PinkNeon).random(),
                    angle = Random.nextFloat() * 360f,
                    speed = Random.nextFloat() * 15f + 10f,
                    rotationSpeed = Random.nextFloat() * 10f - 5f
                )
            }
        }

    LaunchedEffect(Unit) {
        startAnim = true
    }

    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        // Confetti
        if (startAnim) {
            confettiParticles.forEach { particle ->
                ConfettiPiece(particle)
            }
        }

        SuccessOverlayMessage(scale = scale)
    }
}

@Composable
private fun SuccessOverlayMessage(scale: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier =
            Modifier
                .size(120.dp)
                .scale(scale)
                .drawBehind {
                    drawCircle(
                        color = Neon,
                        style = Stroke(width = 4.dp.toPx())
                    )
                }
                .padding(BolaoSpacing.md)
                .clip(CircleShape)
                .background(Neon.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            BolaoIcon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Neon,
                modifier = Modifier.size(72.dp)
            )
        }
        Spacer(Modifier.height(32.dp))
        BolaoText(
            stringResource(Res.string.prediction_success_title),
            color = Color.White,
            fontSize = BolaoTypography.displayMedium.fontSize,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        BolaoText(
            stringResource(Res.string.prediction_success_subtitle),
            color = TextMuted,
            fontSize = BolaoTypography.titleLarge.fontSize,
            fontWeight = FontWeight.Medium
        )
    }
}

private data class ConfettiState(val color: Color, val angle: Float, val speed: Float, val rotationSpeed: Float)

@Composable
private fun ConfettiPiece(state: ConfettiState) {
    val infiniteTransition = rememberInfiniteTransition()
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
        infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val xOffset = remember(progress) { (kotlin.math.cos(state.angle) * state.speed * progress * 100) }
    val yOffset = remember(progress) { (kotlin.math.sin(state.angle) * state.speed * progress * 100) }
    val rotation = remember(progress) { state.rotationSpeed * progress * 360f }

    Box(
        modifier =
        Modifier
            .graphicsLayer {
                translationX = xOffset
                translationY = yOffset
                rotationZ = rotation
                alpha = 1f - progress
                scaleX = 1f - progress
                scaleY = 1f - progress
            }
            .size(8.dp)
            .background(state.color, BolaoRadiusShape.xs)
    )
}

/** Splits a two-option flag label (e.g. "A ou B" for undecided qualifiers) into a de-emphasized separator span. */
@Composable
private fun rememberAnnotatedFlag(flag: String): AnnotatedString = remember(flag) {
    if (flag.contains(" ou ")) {
        buildAnnotatedString {
            val parts = flag.split(" ou ")
            parts.forEachIndexed { index, part ->
                append(part)
                if (index < parts.size - 1) {
                    withStyle(style = SpanStyle(fontSize = BolaoTypography.bodyLarge.fontSize, fontWeight = FontWeight.Normal)) {
                        append(" ou ")
                    }
                }
            }
        }
    } else {
        AnnotatedString(flag)
    }
}

@Composable
private fun TeamHero(flag: String, name: String, crestUrl: String?) {
    val annotatedFlag = rememberAnnotatedFlag(flag)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(120.dp)
    ) {
        TeamHeroCrest(crestUrl = crestUrl, annotatedFlag = annotatedFlag)

        Spacer(Modifier.height(12.dp))
        BolaoText(
            name,
            fontSize = BolaoTypography.titleLarge.fontSize,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun TeamHeroCrest(crestUrl: String?, annotatedFlag: AnnotatedString) {
    Box(
        modifier =
        Modifier
            .size(86.dp)
            .clip(CircleShape)
            .background(NavyElevated)
            .border(2.dp, GlassBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!crestUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model =
                ImageRequest.Builder(LocalPlatformContext.current)
                    .data(crestUrl)
                    .decoderFactory(SvgDecoder.Factory())
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                loading = {
                    BolaoLoadingIndicator(modifier = Modifier.size(24.dp))
                },
                error = {
                    BolaoText(
                        text = annotatedFlag,
                        fontSize = BolaoTypography.displaySmall.fontSize,
                        textAlign = TextAlign.Center
                    )
                }
            )
        } else {
            BolaoText(
                text = annotatedFlag,
                fontSize = BolaoTypography.displaySmall.fontSize,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ScoreStepper(value: Int, onIncrement: () -> Unit, onDecrement: () -> Unit, teamName: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        StepperCircleButton(
            label = stringResource(Res.string.prediction_stepper_increment),
            enabled = true,
            borderColor = Neon.copy(alpha = 0.5f),
            textColor = Neon,
            onClick = onIncrement
        )

        Spacer(Modifier.height(12.dp))

        // Score display
        Box(
            modifier =
            Modifier
                .size(84.dp)
                .clip(BolaoRadiusShape.xl)
                .background(NavyElevated)
                .border(1.dp, GlassBorder, BolaoRadiusShape.xl),
            contentAlignment = Alignment.Center
        ) {
            BolaoText(
                "$value",
                fontSize = BolaoTypography.displayLarge.fontSize,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }

        Spacer(Modifier.height(12.dp))

        StepperCircleButton(
            label = stringResource(Res.string.prediction_stepper_decrement),
            enabled = value > 0,
            borderColor = if (value > 0) TextMuted.copy(alpha = 0.5f) else GlassBorder,
            textColor = if (value > 0) TextMuted else TextSubtle,
            onClick = onDecrement
        )

        Spacer(Modifier.height(10.dp))

        BolaoText(
            teamName,
            fontSize = BolaoTypography.bodyMedium.fontSize,
            color = TextMuted,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun StepperCircleButton(label: String, enabled: Boolean, borderColor: Color, textColor: Color, onClick: () -> Unit) {
    Box(
        modifier =
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.Transparent)
            .border(1.dp, borderColor, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        BolaoText(
            label,
            fontSize = BolaoTypography.displaySmall.fontSize,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun PointBadge(emoji: String, pts: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = BolaoSpacing.xs)
    ) {
        BolaoText(emoji, fontSize = BolaoTypography.headlineLarge.fontSize)
        Spacer(Modifier.height(6.dp))
        BolaoText(
            text =
            if (pts == "1") {
                stringResource(Res.string.prediction_points_badge_singular, pts)
            } else {
                stringResource(Res.string.prediction_points_badge_plural, pts)
            },
            fontSize = BolaoTypography.titleLarge.fontSize,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Spacer(Modifier.height(2.dp))
        BolaoText(
            label,
            fontSize = BolaoTypography.bodySmall.fontSize,
            color = TextMuted,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun GradientSaveButton(text: String, isLoading: Boolean, onClick: () -> Unit) {
    Box(
        modifier =
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(BolaoRadiusShape.lg)
            .background(GradientPrimary)
            .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            BolaoLoadingIndicator(modifier = Modifier.size(24.dp), color = DeepNavy, strokeWidth = 2.5.dp)
        } else {
            BolaoText(
                text,
                fontSize = BolaoTypography.titleLarge.fontSize,
                fontWeight = FontWeight.Bold,
                color = DeepNavy
            )
        }
    }
}
