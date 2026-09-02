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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

    val backCd = stringResource(Res.string.prediction_back_cd)
    val groupLabelTemplate = stringResource(Res.string.prediction_group_label)
    val vsLabel = stringResource(Res.string.prediction_vs_label)
    val scoreQuestion = stringResource(Res.string.prediction_score_question)
    val scoreSeparator = stringResource(Res.string.prediction_score_separator)
    val pointExactEmoji = stringResource(Res.string.prediction_point_exact_emoji)
    val pointExactLabel = stringResource(Res.string.prediction_point_exact_label)
    val pointCorrectEmoji = stringResource(Res.string.prediction_point_correct_emoji)
    val pointCorrectLabel = stringResource(Res.string.prediction_point_correct_label)
    val pointWrongEmoji = stringResource(Res.string.prediction_point_wrong_emoji)
    val pointWrongLabel = stringResource(Res.string.prediction_point_wrong_label)
    val ruleEmoji = stringResource(Res.string.prediction_rule_emoji)
    val ruleText = stringResource(Res.string.prediction_rule_text)
    val updateButtonText = stringResource(Res.string.prediction_button_update)
    val saveButtonText = stringResource(Res.string.prediction_button_save)

    LaunchedEffect(uiState.existingPrediction) {
        uiState.existingPrediction?.let {
            homeScore = it.homeScore
            awayScore = it.awayScore
        }
    }

    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .background(DeepNavy)
            .systemBarsPadding()
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
                val scrollState = rememberScrollState()

                val (homeDisplayName, homeDisplayFlag, homeResolvedCrest) =
                    remember(match.id, match.homeTeam, match.homeTeamFlag, allMatches) {
                        resolveDisplayName(match.id, match.homeTeam, match.homeTeamFlag, allMatches, true)
                    }
                val (awayDisplayName, awayDisplayFlag, awayResolvedCrest) =
                    remember(match.id, match.awayTeam, match.awayTeamFlag, allMatches) {
                        resolveDisplayName(match.id, match.awayTeam, match.awayTeamFlag, allMatches, false)
                    }

                Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier =
                        Modifier
                            .weight(1f)
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // ── Stadium header ────────────────────────────────────────
                        Box(
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(GradientHero)
                                .padding(top = 16.dp, bottom = 24.dp, start = 8.dp, end = 20.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BolaoIconButton(onClick = onNavigateBack, modifier = Modifier.size(44.dp)) {
                                        BolaoIcon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = backCd,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(Modifier.weight(1f))
                                    match.group?.let { group ->
                                        Box(
                                            modifier =
                                            Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(NavyElevated)
                                                .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            BolaoText(
                                                groupLabelTemplate.replace("%1\$s", group).replace("%2\$s", match.phase.label),
                                                fontSize = 11.sp,
                                                color = TextMuted,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(24.dp))

                                // Teams display
                                Row(
                                    modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TeamHero(
                                        flag = homeDisplayFlag,
                                        name = homeDisplayName,
                                        crestUrl = homeResolvedCrest ?: match.homeTeamCrest
                                    )

                                    BolaoText(
                                        vsLabel,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextMuted.copy(alpha = 0.4f),
                                        modifier = Modifier.padding(bottom = 36.dp),
                                        letterSpacing = 2.sp
                                    )

                                    TeamHero(
                                        flag = awayDisplayFlag,
                                        name = awayDisplayName,
                                        crestUrl = awayResolvedCrest ?: match.awayTeamCrest
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        // ── Score picker ──────────────────────────────────────────
                        BolaoText(
                            scoreQuestion,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(Modifier.height(24.dp))

                        Row(
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 40.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ScoreStepper(
                                value = homeScore,
                                onIncrement = { homeScore++ },
                                onDecrement = { if (homeScore > 0) homeScore-- },
                                teamName = homeDisplayName
                            )

                            BolaoText(
                                scoreSeparator,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )

                            ScoreStepper(
                                value = awayScore,
                                onIncrement = { awayScore++ },
                                onDecrement = { if (awayScore > 0) awayScore-- },
                                teamName = awayDisplayName
                            )
                        }

                        Spacer(Modifier.height(32.dp))

                        // ── Points info ───────────────────────────────────────────
                        Column(
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(NavyCard)
                                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                PointBadge(
                                    emoji = pointExactEmoji,
                                    pts = (uiState.bolao?.pointsExactScore ?: 3).toString(),
                                    label = pointExactLabel
                                )
                                BolaoVerticalDivider(color = GlassBorder, modifier = Modifier.height(48.dp))
                                PointBadge(
                                    emoji = pointCorrectEmoji,
                                    pts = (uiState.bolao?.pointsWinnerOrDraw ?: 1).toString(),
                                    label = pointCorrectLabel
                                )
                                BolaoVerticalDivider(color = GlassBorder, modifier = Modifier.height(48.dp))
                                PointBadge(
                                    emoji = pointWrongEmoji,
                                    pts = "0",
                                    label = pointWrongLabel
                                )
                            }

                            BolaoHorizontalDivider(color = GlassBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                BolaoText(ruleEmoji, fontSize = 14.sp)
                                BolaoText(
                                    ruleText,
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        uiState.error?.let {
                            Spacer(Modifier.height(12.dp))
                            BolaoText(it, color = ErrorRed, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 20.dp))
                        }

                        Spacer(Modifier.height(24.dp))
                        Spacer(Modifier.imePadding())
                    }

                    // ── Save button (Sticky) ──────────────────────────────────
                    Box(
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 24.dp, top = 8.dp)
                    ) {
                        GradientSaveButton(
                            text = if (uiState.existingPrediction != null) updateButtonText else saveButtonText,
                            isLoading = uiState.isLoading,
                            onClick = { viewModel.savePrediction(userId, homeScore, awayScore) }
                        )
                    }
                }
            }
        }

        if (showSuccess) {
            SuccessOverlay()
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
                    .padding(12.dp)
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
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            BolaoText(
                stringResource(Res.string.prediction_success_subtitle),
                color = TextMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
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
            .background(state.color, RoundedCornerShape(2.dp))
    )
}

@Composable
private fun TeamHero(flag: String, name: String, crestUrl: String?) {
    val annotatedFlag =
        remember(flag) {
            if (flag.contains(" ou ")) {
                buildAnnotatedString {
                    val parts = flag.split(" ou ")
                    parts.forEachIndexed { index, part ->
                        append(part)
                        if (index < parts.size - 1) {
                            withStyle(style = SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal)) {
                                append(" ou ")
                            }
                        }
                    }
                }
            } else {
                AnnotatedString(flag)
            }
        }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(120.dp)
    ) {
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
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                )
            } else {
                BolaoText(
                    text = annotatedFlag,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        BolaoText(
            name,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun ScoreStepper(value: Int, onIncrement: () -> Unit, onDecrement: () -> Unit, teamName: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // + button
        Box(
            modifier =
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Transparent)
                .border(1.dp, Neon.copy(alpha = 0.5f), CircleShape)
                .clickable(onClick = onIncrement),
            contentAlignment = Alignment.Center
        ) {
            BolaoText(
                stringResource(Res.string.prediction_stepper_increment),
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Neon
            )
        }

        Spacer(Modifier.height(12.dp))

        // Score display
        Box(
            modifier =
            Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(NavyElevated)
                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            BolaoText(
                "$value",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }

        Spacer(Modifier.height(12.dp))

        // – button
        Box(
            modifier =
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Transparent)
                .border(1.dp, if (value > 0) TextMuted.copy(alpha = 0.5f) else GlassBorder, CircleShape)
                .clickable(enabled = value > 0, onClick = onDecrement),
            contentAlignment = Alignment.Center
        ) {
            BolaoText(
                stringResource(Res.string.prediction_stepper_decrement),
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = if (value > 0) TextMuted else TextSubtle
            )
        }

        Spacer(Modifier.height(10.dp))

        BolaoText(
            teamName,
            fontSize = 11.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun PointBadge(emoji: String, pts: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        BolaoText(emoji, fontSize = 22.sp)
        Spacer(Modifier.height(6.dp))
        BolaoText(
            text =
            if (pts == "1") {
                stringResource(Res.string.prediction_points_badge_singular, pts)
            } else {
                stringResource(Res.string.prediction_points_badge_plural, pts)
            },
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Spacer(Modifier.height(2.dp))
        BolaoText(
            label,
            fontSize = 10.sp,
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
            .clip(RoundedCornerShape(16.dp))
            .background(GradientPrimary)
            .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            BolaoLoadingIndicator(modifier = Modifier.size(24.dp), color = DeepNavy, strokeWidth = 2.5.dp)
        } else {
            BolaoText(
                text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DeepNavy
            )
        }
    }
}
