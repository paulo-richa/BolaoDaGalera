package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bolaodagalera.feature_bolao.generated.resources.Res
import bolaodagalera.feature_bolao.generated.resources.bolao_common_date_tbd
import bolaodagalera.feature_bolao.generated.resources.bolao_common_day_dom
import bolaodagalera.feature_bolao.generated.resources.bolao_common_day_qua
import bolaodagalera.feature_bolao.generated.resources.bolao_common_day_qui
import bolaodagalera.feature_bolao.generated.resources.bolao_common_day_sab
import bolaodagalera.feature_bolao.generated.resources.bolao_common_day_seg
import bolaodagalera.feature_bolao.generated.resources.bolao_common_day_sex
import bolaodagalera.feature_bolao.generated.resources.bolao_common_day_ter
import bolaodagalera.feature_bolao.generated.resources.bolao_common_month_abr
import bolaodagalera.feature_bolao.generated.resources.bolao_common_month_ago
import bolaodagalera.feature_bolao.generated.resources.bolao_common_month_dez
import bolaodagalera.feature_bolao.generated.resources.bolao_common_month_fev
import bolaodagalera.feature_bolao.generated.resources.bolao_common_month_jan
import bolaodagalera.feature_bolao.generated.resources.bolao_common_month_jul
import bolaodagalera.feature_bolao.generated.resources.bolao_common_month_jun
import bolaodagalera.feature_bolao.generated.resources.bolao_common_month_mai
import bolaodagalera.feature_bolao.generated.resources.bolao_common_month_mar
import bolaodagalera.feature_bolao.generated.resources.bolao_common_month_nov
import bolaodagalera.feature_bolao.generated.resources.bolao_common_month_out
import bolaodagalera.feature_bolao.generated.resources.bolao_common_month_set
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_components_group_completed_emoji
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_components_group_label
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_components_group_locked_emoji
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_components_group_pending_emoji
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.svg.SvgDecoder
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
import com.lpstudio.bolaodagalera.designsystem.theme.ErrorRed
import com.lpstudio.bolaodagalera.designsystem.theme.GlassBorder
import com.lpstudio.bolaodagalera.designsystem.theme.Gold
import com.lpstudio.bolaodagalera.designsystem.theme.NavyCard
import com.lpstudio.bolaodagalera.designsystem.theme.NavyElevated
import com.lpstudio.bolaodagalera.designsystem.theme.Neon
import com.lpstudio.bolaodagalera.designsystem.theme.TextMuted
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.User
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier,
    isPast: Boolean = false,
    isCurrent: Boolean = false,
    onClick: () -> Unit
) {
    val bColor by animateColorAsState(
        when {
            isSelected && isUnlocked -> Neon
            isCurrent -> Gold
            isPast -> Color.Transparent
            isUnlocked -> GlassBorder
            else -> Color.Transparent
        },
        label = "border_$label"
    )
    val cColor by animateColorAsState(
        when {
            isSelected && isUnlocked -> Neon.copy(alpha = 0.12f)
            isCurrent -> Gold.copy(alpha = 0.12f)
            isPast -> DeepNavy
            isUnlocked -> NavyElevated
            else -> NavyCard.copy(alpha = 0.5f)
        },
        label = "bg_$label"
    )
    val tColor by animateColorAsState(
        when {
            isSelected && isUnlocked -> Neon
            isCurrent -> Gold
            isPast -> TextMuted.copy(alpha = 0.55f)
            isUnlocked -> Color.White
            else -> TextMuted.copy(alpha = 0.4f)
        },
        label = "text_$label"
    )
    Box(
        modifier =
        modifier.clip(
            BolaoRadiusShape.lg
        ).background(cColor).border(1.dp, bColor, BolaoRadiusShape.lg).then(
            if (isUnlocked) {
                Modifier.clickable {
                    onClick()
                }
            } else {
                Modifier
            }
        ).padding(vertical = BolaoSpacing.md, horizontal = BolaoSpacing.lg),
        contentAlignment = Alignment.Center
    ) {
        BolaoText(
            label,
            color = tColor,
            fontSize = BolaoTypography.bodyMedium.fontSize,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
            maxLines = 1,
            textAlign = TextAlign.Center,
            softWrap = false
        )
    }
}

@Composable
fun GroupHeader(group: String, isExpanded: Boolean, isCompleted: Boolean, enabled: Boolean = true, onToggle: () -> Unit) {
    val rot by animateFloatAsState(if (isExpanded) 90f else 0f, tween(200), label = "chevron_$group")
    val bColor by animateColorAsState(
        when {
            !enabled -> Color.Transparent
            isExpanded -> Neon.copy(alpha = 0.3f)
            else -> GlassBorder
        },
        label = "header_border_$group"
    )
    val bg =
        when {
            !enabled -> Brush.linearGradient(listOf(NavyCard.copy(alpha = 0.5f), NavyCard.copy(alpha = 0.5f)))
            isExpanded -> Brush.linearGradient(listOf(Neon.copy(alpha = 0.08f), Neon.copy(alpha = 0.02f)))
            else -> Brush.linearGradient(listOf(NavyElevated, NavyCard))
        }
    Column {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .clip(BolaoRadiusShape.lg)
                .background(bg)
                .border(1.dp, bColor, BolaoRadiusShape.lg)
                .then(if (enabled) Modifier.clickable(onClick = onToggle) else Modifier)
                .padding(horizontal = BolaoSpacing.xl, vertical = BolaoSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.md)) {
                Box(
                    modifier =
                    Modifier.width(3.dp).height(16.dp).clip(BolaoRadiusShape.xs).background(
                        when {
                            !enabled -> TextMuted.copy(alpha = 0.3f)
                            isCompleted -> Neon
                            else -> Color(0xFFFFC107)
                        }
                    )
                )
                BolaoText(
                    stringResource(Res.string.bolao_detail_components_group_label, group),
                    fontSize = BolaoTypography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) Color.White else TextMuted.copy(alpha = 0.5f)
                )
                if (enabled) {
                    if (isCompleted) {
                        BolaoText(
                            stringResource(Res.string.bolao_detail_components_group_completed_emoji),
                            fontSize = BolaoTypography.bodyMedium.fontSize
                        )
                    } else {
                        BolaoText(
                            stringResource(Res.string.bolao_detail_components_group_pending_emoji),
                            fontSize = BolaoTypography.bodyMedium.fontSize
                        )
                    }
                } else {
                    BolaoText(
                        stringResource(Res.string.bolao_detail_components_group_locked_emoji),
                        fontSize = BolaoTypography.bodySmall.fontSize,
                        modifier = Modifier.padding(bottom = BolaoSpacing.xs)
                    )
                }
            }
            if (enabled) {
                BolaoIcon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp).rotate(rot)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun TeamNameText(name: String, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.Start) {
    var fontSize by remember(name) { mutableIntStateOf(13) }
    var ready by remember(name) { mutableStateOf(false) }
    BolaoText(
        text = name,
        modifier = modifier.drawWithContent {
            if (ready) drawContent()
        },
        fontSize = fontSize.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        maxLines = 1,
        softWrap = false,
        textAlign = textAlign,
        onTextLayout = { res ->
            if (res.hasVisualOverflow && fontSize > 8) fontSize -= 1 else ready = true
        }
    )
}

@Composable
fun TeamIcon(crestUrl: String?, flag: AnnotatedString, isTbd: Boolean, size: androidx.compose.ui.unit.Dp = 32.dp) {
    val hasCrest = !crestUrl.isNullOrBlank()
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(if (hasCrest) Color.Transparent else NavyElevated.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        if (hasCrest) {
            SubcomposeAsyncImage(
                model =
                ImageRequest.Builder(
                    LocalPlatformContext.current
                ).data(
                    crestUrl
                ).decoderFactory(
                    SvgDecoder.Factory()
                ).crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(0.8f),
                loading = {
                    BolaoLoadingIndicator(modifier = Modifier.size(size * 0.5f))
                },
                error = { BolaoText(text = flag, fontSize = (size.value * 0.7f).sp, fontWeight = FontWeight.Bold, color = Color.White) }
            )
        } else {
            BolaoText(
                text = flag,
                fontSize = if (isTbd) (size.value * 0.5f).sp else (size.value * 0.7f).sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun PendingRequestItem(user: User, label: String, accentColor: Color = Neon, onApprove: () -> Unit, onDeny: () -> Unit) {
    BolaoSurface(
        color = NavyElevated,
        shape = BolaoRadiusShape.lg,
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = BolaoSpacing.lg, vertical = BolaoSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                initials = user.name.take(1).uppercase(),
                size = 40.dp,
                fontSize = BolaoTypography.bodyLarge.fontSize,
                borderColor = accentColor.copy(alpha = 0.5f)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                BolaoText(
                    text = user.name,
                    color = Color.White,
                    fontSize = BolaoTypography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                BolaoText(text = label, color = accentColor, fontSize = BolaoTypography.bodyMedium.fontSize, fontWeight = FontWeight.Medium)
            }
            BolaoIconButton(onClick = onDeny, modifier = Modifier.size(32.dp)) {
                BolaoIcon(Icons.Default.Close, null, tint = ErrorRed, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(8.dp))
            BolaoIconButton(onClick = onApprove, modifier = Modifier.size(32.dp)) {
                BolaoIcon(Icons.Default.Check, null, tint = Neon, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun formatMatchDate(millis: Long): String {
    if (millis == Match.NO_DATE_MILLIS) return stringResource(Res.string.bolao_common_date_tbd)
    val tz = TimeZone.currentSystemDefault()
    val dt = Instant.fromEpochMilliseconds(millis).toLocalDateTime(tz)
    val dayOfWeek =
        stringResource(
            when (dt.dayOfWeek) {
                DayOfWeek.MONDAY -> Res.string.bolao_common_day_seg
                DayOfWeek.TUESDAY -> Res.string.bolao_common_day_ter
                DayOfWeek.WEDNESDAY -> Res.string.bolao_common_day_qua
                DayOfWeek.THURSDAY -> Res.string.bolao_common_day_qui
                DayOfWeek.FRIDAY -> Res.string.bolao_common_day_sex
                DayOfWeek.SATURDAY -> Res.string.bolao_common_day_sab
                DayOfWeek.SUNDAY -> Res.string.bolao_common_day_dom
            }
        )
    val monthName =
        stringResource(
            when (dt.month) {
                Month.JANUARY -> Res.string.bolao_common_month_jan
                Month.FEBRUARY -> Res.string.bolao_common_month_fev
                Month.MARCH -> Res.string.bolao_common_month_mar
                Month.APRIL -> Res.string.bolao_common_month_abr
                Month.MAY -> Res.string.bolao_common_month_mai
                Month.JUNE -> Res.string.bolao_common_month_jun
                Month.JULY -> Res.string.bolao_common_month_jul
                Month.AUGUST -> Res.string.bolao_common_month_ago
                Month.SEPTEMBER -> Res.string.bolao_common_month_set
                Month.OCTOBER -> Res.string.bolao_common_month_out
                Month.NOVEMBER -> Res.string.bolao_common_month_nov
                Month.DECEMBER -> Res.string.bolao_common_month_dez
            }
        )
    val dayValue = dt.dayOfMonth.toString().padStart(2, '0')
    val hour = dt.hour.toString().padStart(2, '0')
    val minute = dt.minute.toString().padStart(2, '0')

    return "$dayOfWeek, $dayValue $monthName $hour:$minute"
}
