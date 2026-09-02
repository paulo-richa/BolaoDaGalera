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

@Composable
fun MatchCard(
    match: Match,
    prediction: Prediction?,
    isAdmin: Boolean = false,
    bolaoCreatedAt: Long = 0L,
    forceLocked: Boolean = false,
    showSocialBadge: Boolean = true,
    allMatches: List<Match> = emptyList(),
    isTwoLegged: Boolean = false,
    onShowAllPredictions: () -> Unit = {},
    onOpenAdminScoreDialog: () -> Unit = {},
    onClick: () -> Unit
) {
    val hasPrediction = prediction != null
    val isFinished = match.isFinished
    val now = TimeSource.nowMillis()
    val start = match.matchDateMillis
    val (hName, hFlag, hCrest) =
        remember(match.id, match.homeTeam, match.homeTeamFlag, allMatches) {
            resolveDisplayName(match.id, match.homeTeam, match.homeTeamFlag, allMatches, true)
        }
    val (aName, aFlag, aCrest) =
        remember(match.id, match.awayTeam, match.awayTeamFlag, allMatches) {
            resolveDisplayName(match.id, match.awayTeam, match.awayTeamFlag, allMatches, false)
        }
    val isVolta = match.id.contains("-L2")
    val ida =
        remember(match.id, allMatches, isTwoLegged, isVolta) {
            if (isTwoLegged && isVolta) {
                val m =
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
                if (m != null && m.homeScore != null && m.awayScore != null) "${m.homeScore}×${m.awayScore}" else null
            } else {
                null
            }
        }
    val hAnn =
        remember(hFlag) {
            val p = hFlag.split(" ou ")
            if (p.size > 1) {
                buildAnnotatedString {
                    p.forEachIndexed { i, _ ->
                        append(p[i])
                        if (i < p.size - 1) {
                            withStyle(style = SpanStyle(fontSize = BolaoTypography.bodyMedium.fontSize)) {
                                append(" ou ")
                            }
                        }
                    }
                }
            } else {
                AnnotatedString(hFlag)
            }
        }
    val aAnn =
        remember(aFlag) {
            val p = aFlag.split(" ou ")
            if (p.size > 1) {
                buildAnnotatedString {
                    p.forEachIndexed { i, _ ->
                        append(p[i])
                        if (i < p.size - 1) {
                            withStyle(style = SpanStyle(fontSize = BolaoTypography.bodyMedium.fontSize)) {
                                append(" ou ")
                            }
                        }
                    }
                }
            } else {
                AnnotatedString(aFlag)
            }
        }
    val isFin = match.status == "FINISHED" ||
        match.status == "PENALTIES" ||
        match.status == "PAUSED_PENALTIES" ||
        (
            match.homeScore != null &&
                match.awayScore != null &&
                now > (start + 3 * 3600_000L)
            )
    val statusLive = listOf("IN_PLAY", "PAUSED", "EXTRA_TIME", "PENALTIES", "LIVE")
    val isLive = !isFin &&
        (
            match.status in statusLive ||
                (now >= (start - 60_000) && now < (start + 3 * 3600_000L))
            )
    val isGhost = start < bolaoCreatedAt
    val isTbd = (match.homeTeamCode == "TBD" || match.awayTeamCode == "TBD") || hFlag.contains("ou") || aFlag.contains("ou")
    val canPred = !isFinished && now < (match.matchDateMillis - 60_000) && !forceLocked && !isTbd
    val bColor =
        when {
            isFin && hasPrediction -> {
                val hR = match.homeScore ?: 0
                val aR = match.awayScore ?: 0
                val hP = prediction.homeScore
                val aP = prediction.awayScore
                val pts = when {
                    hP == hR && aP == aR -> 3
                    (hP > aP && hR > aR) || (hP < aP && hR < aR) || (hP == aP && hR == aR) -> 1
                    else -> 0
                }
                when (pts) {
                    3 -> Neon
                    1 -> Gold
                    else -> ErrorRed
                }
            }
            hasPrediction -> Gold.copy(alpha = 0.4f)
            else -> GlassBorder
        }
    val isExp = now >= (match.matchDateMillis - 60_000) || isFinished
    val isLock = isExp || forceLocked || isGhost || isTbd
    val cardBg = if (isLive) Brush.verticalGradient(listOf(NavyElevated, DeepNavy)) else null
    BolaoSurface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isLive) Color.Transparent else NavyElevated,
        shape = BolaoRadiusShape.md,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isLive) Neon.copy(alpha = 0.5f) else bColor)
    ) {
        Box(
            modifier =
            Modifier.fillMaxWidth().then(if (cardBg != null) Modifier.background(cardBg) else Modifier).clickable(
                enabled =
                when {
                    isGhost -> isAdmin
                    canPred -> true
                    isFin -> isAdmin
                    isExp -> (!isAdmin && showSocialBadge) || isAdmin
                    else -> false
                },
                onClick = {
                    if (canPred) {
                        onClick()
                    } else if (isAdmin) {
                        onOpenAdminScoreDialog()
                    } else if (isExp && showSocialBadge) {
                        onShowAllPredictions()
                    }
                }
            )
        ) {
            val showGalera = showSocialBadge && (isAdmin || isExp) && !isTbd && !isGhost
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

            if (!(isFin && hasPrediction)) {
                BolaoText(
                    // Enquanto o confronto não estiver confirmado (times TBD), não
                    // mostra a data mesmo que a API já tenha publicado uma para o
                    // "slot" da fase - evita sugerir um confronto que ainda não existe.
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
            if (isFin && hasPrediction) {
                val hR = match.homeScore ?: 0
                val aR = match.awayScore ?: 0
                val hP = prediction.homeScore
                val aP = prediction.awayScore
                val pts =
                    when {
                        hP == hR && aP == aR -> 3
                        (hP > aP && hR > aR) || (hP < aP && hR < aR) || (hP == aP && hR == aR) -> 1
                        else -> 0
                    }
                BolaoSurface(
                    color =
                    when (pts) {
                        3 -> Neon.copy(alpha = 0.15f)
                        1 -> Gold.copy(alpha = 0.15f)
                        else -> ErrorRed.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(bottomStart = BolaoRadius.md, topEnd = BolaoRadius.lg),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    BolaoText(
                        text = if (pts ==
                            1
                        ) {
                            stringResource(Res.string.match_card_points_singular)
                        } else {
                            stringResource(Res.string.match_card_points_plural, pts)
                        },
                        fontSize = BolaoTypography.bodySmall.fontSize,
                        fontWeight = FontWeight.Black,
                        color =
                        when (pts) {
                            3 -> Neon
                            1 -> Gold
                            else -> ErrorRed
                        },
                        modifier = Modifier.padding(horizontal = BolaoSpacing.md, vertical = BolaoSpacing.sm)
                    )
                }
            }
            Column(modifier = Modifier.padding(BolaoSpacing.md)) {
                Spacer(Modifier.height(if (isLock || canPred) 32.dp else 16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = if (hName.isEmpty()) Arrangement.Center else Arrangement.spacedBy(BolaoSpacing.md)
                    ) {
                        TeamIcon(crestUrl = hCrest ?: match.homeTeamCrest, flag = hAnn, isTbd = isTbd, size = 32.dp)
                        if (hName.isNotEmpty()) TeamNameText(name = hName, modifier = Modifier.weight(1f))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = BolaoSpacing.sm)) {
                        if (hasPrediction) {
                            val hR = match.homeScore ?: 0
                            val aR = match.awayScore ?: 0
                            val hP = prediction.homeScore
                            val aP = prediction.awayScore
                            val sColor = when {
                                !isFin && !canPred -> TextMuted
                                !isLock -> Gold
                                hP == hR && aP == aR -> Neon
                                (hP > aP && hR > aR) || (hP < aP && hR < aR) || (hP == aP && hR == aR) -> Gold
                                else -> ErrorRed
                            }
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
                        horizontalArrangement = if (aName.isEmpty()) {
                            Arrangement.Center
                        } else {
                            Arrangement.spacedBy(
                                BolaoSpacing.md,
                                Alignment.End
                            )
                        }
                    ) {
                        if (aName.isNotEmpty()) TeamNameText(name = aName, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        TeamIcon(crestUrl = aCrest ?: match.awayTeamCrest, flag = aAnn, isTbd = isTbd, size = 32.dp)
                    }
                }
                if (canPred) {
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
                } else if (isLock) {
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
                            val sT =
                                when {
                                    isFin -> stringResource(Res.string.match_card_status_finished)
                                    match.status == "EXTRA_TIME" -> stringResource(Res.string.match_card_status_extra_time)
                                    match.status == "PENALTIES" -> stringResource(Res.string.match_card_status_penalties)
                                    match.status == "PAUSED_EXTRA_TIME" -> stringResource(Res.string.match_card_status_going_extra_time)
                                    match.status == "PAUSED_PENALTIES" -> stringResource(Res.string.match_card_status_going_penalties)
                                    match.status == "PAUSED" -> stringResource(Res.string.match_card_status_halftime)
                                    else -> stringResource(Res.string.match_card_status_in_progress)
                                }
                            val aC = if (isFin) Color.White else Neon
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
                                    text = sT,
                                    fontSize = BolaoTypography.bodySmall.fontSize,
                                    fontWeight = FontWeight.Black,
                                    color = aC.copy(alpha = 0.7f),
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Box(
                                modifier =
                                Modifier.padding(
                                    top = BolaoSpacing.xs
                                ).clip(BolaoRadiusShape.sm).background(aC.copy(alpha = 0.08f)).then(
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
                                        color = aC
                                    )
                                    BolaoText(
                                        "×",
                                        fontSize = BolaoTypography.bodyMedium.fontSize,
                                        fontWeight = FontWeight.Bold,
                                        color = aC.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(horizontal = BolaoSpacing.xs)
                                    )
                                    BolaoText(
                                        "${match.awayScore ?: 0}",
                                        fontSize = BolaoTypography.titleLarge.fontSize,
                                        fontWeight = FontWeight.Black,
                                        color = aC
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BolaoScoreField(
                        value = hS,
                        onValueChange = {
                            if (it.length <= 2) {
                                hS =
                                    it.filter { c ->
                                        c.isDigit()
                                    }
                            }
                        }
                    )
                    BolaoText(
                        stringResource(Res.string.match_card_admin_dialog_versus),
                        modifier = Modifier.padding(horizontal = BolaoSpacing.lg),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    BolaoScoreField(
                        value = aS,
                        onValueChange = {
                            if (it.length <= 2) {
                                aS =
                                    it.filter { c ->
                                        c.isDigit()
                                    }
                            }
                        }
                    )
                }
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
