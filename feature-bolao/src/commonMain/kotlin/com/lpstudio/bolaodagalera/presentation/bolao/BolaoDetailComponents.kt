package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
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

private fun filterChipBorderColor(
    isSelected: Boolean,
    isUnlocked: Boolean,
    isPast: Boolean,
    isCurrent: Boolean,
    selectedAccent: Color
): Color = when {
    isSelected && isUnlocked -> selectedAccent
    isCurrent -> Gold
    isPast -> Color.Transparent
    isUnlocked -> GlassBorder
    else -> Color.Transparent
}

private fun filterChipBackgroundColor(
    isSelected: Boolean,
    isUnlocked: Boolean,
    isPast: Boolean,
    isCurrent: Boolean,
    selectedAccent: Color
): Color = when {
    isSelected && isUnlocked -> selectedAccent.copy(alpha = 0.12f)
    isCurrent -> Gold.copy(alpha = 0.12f)
    isPast -> DeepNavy
    isUnlocked -> NavyElevated
    else -> NavyCard.copy(alpha = 0.5f)
}

private fun filterChipTextColor(
    isSelected: Boolean,
    isUnlocked: Boolean,
    isPast: Boolean,
    isCurrent: Boolean,
    selectedAccent: Color
): Color = when {
    isSelected && isUnlocked -> selectedAccent
    isCurrent -> Gold
    isPast -> TextMuted.copy(alpha = 0.55f)
    isUnlocked -> Color.White
    else -> TextMuted.copy(alpha = 0.4f)
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier,
    isPast: Boolean = false,
    isCurrent: Boolean = false,
    selectedAccent: Color = Neon,
    onClick: () -> Unit
) {
    val bColor by animateColorAsState(
        filterChipBorderColor(isSelected, isUnlocked, isPast, isCurrent, selectedAccent),
        label = "border_$label"
    )
    val cColor by animateColorAsState(
        filterChipBackgroundColor(isSelected, isUnlocked, isPast, isCurrent, selectedAccent),
        label = "bg_$label"
    )
    val tColor by animateColorAsState(
        filterChipTextColor(isSelected, isUnlocked, isPast, isCurrent, selectedAccent),
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

/** One chip's state/behavior in a [TabSelectorRow], decoupled from what it represents (a day, a round, a phase leg...). */
data class TabChipSpec(
    val key: String,
    val label: String,
    val isSelected: Boolean,
    val isUnlocked: Boolean,
    val isPast: Boolean = false,
    val isCurrent: Boolean = false,
    val selectedAccent: Color = Neon,
    val onClick: () -> Unit
)

/** Whether the chip at [index] is comfortably visible - not clipped at either edge, and not off-screen entirely. */
private fun LazyListState.isChipFullyVisible(index: Int): Boolean {
    val item = layoutInfo.visibleItemsInfo.find { it.index == index } ?: return false
    return item.offset >= layoutInfo.viewportStartOffset && (item.offset + item.size) <= layoutInfo.viewportEndOffset
}

/**
 * Shared horizontal chip row for tab/round/phase selection (used by both [RodadaSelector] for
 * round-robin/group-stage rounds and [KnockoutTab]'s phase/leg tabs). Only auto-scrolls when the
 * newly selected chip isn't already comfortably visible - i.e. tapping the last chip shown on
 * screen (partially clipped) or a chip fully hidden off-screen brings it (and its neighbors) into
 * frame; tapping a chip already fully visible elsewhere in the row doesn't cause any jump.
 * Also fades whichever edge still has more chips to scroll toward.
 */
@Composable
fun TabSelectorRow(chips: List<TabChipSpec>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    val selectedIndex = remember(chips) { chips.indexOfFirst { it.isSelected } }
    LaunchedEffect(selectedIndex, chips.size) {
        if (selectedIndex != -1 && !listState.isChipFullyVisible(selectedIndex)) {
            listState.animateScrollToItem(selectedIndex)
        }
    }
    val canScrollBack by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 } }
    val canScrollForward by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            if (last == null) {
                false
            } else {
                last.index < listState.layoutInfo.totalItemsCount - 1 || (last.offset + last.size) > listState.layoutInfo.viewportEndOffset
            }
        }
    }
    Box(modifier = modifier.fillMaxWidth()) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth().padding(vertical = BolaoSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.sm),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(chips, key = { it.key }) { chip ->
                FilterChip(
                    label = chip.label,
                    isSelected = chip.isSelected,
                    isUnlocked = chip.isUnlocked,
                    isPast = chip.isPast,
                    isCurrent = chip.isCurrent,
                    selectedAccent = chip.selectedAccent,
                    onClick = chip.onClick
                )
            }
        }
        if (canScrollBack) {
            Box(
                modifier =
                Modifier.align(Alignment.CenterStart).width(40.dp).matchParentSize()
                    .background(Brush.horizontalGradient(listOf(DeepNavy, Color.Transparent)))
            )
        }
        if (canScrollForward) {
            Box(
                modifier =
                Modifier.align(Alignment.CenterEnd).width(40.dp).matchParentSize()
                    .background(Brush.horizontalGradient(listOf(Color.Transparent, DeepNavy)))
            )
        }
    }
}

/**
 * Two stacked [TabSelectorRow]s - finished phases/rounds up top (marked in red, with a subtle
 * border so they still read as tabs even unselected), current/upcoming ones below - so a
 * long-running knockout or round-robin doesn't force scrolling past every already-decided
 * phase/round just to reach today's tab. The top row is entirely omitted when nothing's finished yet.
 */
@Composable
fun TwoTierTabSelector(pastChips: List<TabChipSpec>, presentFutureChips: List<TabChipSpec>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (pastChips.isNotEmpty()) {
            TabSelectorRow(pastChips)
        }
        TabSelectorRow(presentFutureChips)
    }
}

private fun groupHeaderBorderColor(enabled: Boolean): Color = when {
    !enabled -> Color.Transparent
    else -> Neon.copy(alpha = 0.3f)
}

private fun groupHeaderBackground(enabled: Boolean): Brush = when {
    !enabled -> Brush.linearGradient(listOf(NavyCard.copy(alpha = 0.5f), NavyCard.copy(alpha = 0.5f)))
    else -> Brush.linearGradient(listOf(Neon.copy(alpha = 0.08f), Neon.copy(alpha = 0.02f)))
}

/** ARGB for the amber used to flag a group header as "in progress" (not completed, not disabled). */
private const val GROUP_IN_PROGRESS_AMBER_ARGB = 0xFFFFC107

private fun groupHeaderBarColor(enabled: Boolean, isCompleted: Boolean): Color = when {
    !enabled -> TextMuted.copy(alpha = 0.3f)
    isCompleted -> Neon
    else -> Color(GROUP_IN_PROGRESS_AMBER_ARGB)
}

@Composable
private fun GroupHeaderStatusEmoji(enabled: Boolean, isCompleted: Boolean) {
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

@Composable
private fun GroupHeaderLabel(group: String, isCompleted: Boolean, enabled: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.md)) {
        Box(
            modifier =
            Modifier.width(3.dp).height(16.dp).clip(BolaoRadiusShape.xs).background(
                groupHeaderBarColor(enabled, isCompleted)
            )
        )
        BolaoText(
            stringResource(Res.string.bolao_detail_components_group_label, group),
            fontSize = BolaoTypography.bodyLarge.fontSize,
            fontWeight = FontWeight.Bold,
            color = if (enabled) Color.White else TextMuted.copy(alpha = 0.5f)
        )
        GroupHeaderStatusEmoji(enabled, isCompleted)
    }
}

@Composable
fun GroupHeader(group: String, isCompleted: Boolean, enabled: Boolean = true) {
    val bColor by animateColorAsState(
        groupHeaderBorderColor(enabled),
        label = "header_border_$group"
    )
    val bg = groupHeaderBackground(enabled)
    Column {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .clip(BolaoRadiusShape.lg)
                .background(bg)
                .border(1.dp, bColor, BolaoRadiusShape.lg)
                .padding(horizontal = BolaoSpacing.xl, vertical = BolaoSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GroupHeaderLabel(group, isCompleted, enabled)
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
private fun dayOfWeekLabel(dayOfWeek: DayOfWeek): String = stringResource(
    when (dayOfWeek) {
        DayOfWeek.MONDAY -> Res.string.bolao_common_day_seg
        DayOfWeek.TUESDAY -> Res.string.bolao_common_day_ter
        DayOfWeek.WEDNESDAY -> Res.string.bolao_common_day_qua
        DayOfWeek.THURSDAY -> Res.string.bolao_common_day_qui
        DayOfWeek.FRIDAY -> Res.string.bolao_common_day_sex
        DayOfWeek.SATURDAY -> Res.string.bolao_common_day_sab
        DayOfWeek.SUNDAY -> Res.string.bolao_common_day_dom
    }
)

@Composable
private fun monthLabel(month: Month): String = stringResource(
    when (month) {
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

@Composable
fun formatMatchDate(millis: Long): String {
    if (millis == Match.NO_DATE_MILLIS) return stringResource(Res.string.bolao_common_date_tbd)
    val tz = TimeZone.currentSystemDefault()
    val dt = Instant.fromEpochMilliseconds(millis).toLocalDateTime(tz)
    val dayOfWeek = dayOfWeekLabel(dt.dayOfWeek)
    val monthName = monthLabel(dt.month)
    val dayValue = dt.dayOfMonth.toString().padStart(2, '0')
    val hour = dt.hour.toString().padStart(2, '0')
    val minute = dt.minute.toString().padStart(2, '0')

    return "$dayOfWeek, $dayValue $monthName $hour:$minute"
}
