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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.User
import com.lpstudio.bolaodagalera.presentation.components.UserAvatar
import com.lpstudio.bolaodagalera.presentation.theme.DeepNavy
import com.lpstudio.bolaodagalera.presentation.theme.ErrorRed
import com.lpstudio.bolaodagalera.presentation.theme.GlassBorder
import com.lpstudio.bolaodagalera.presentation.theme.Gold
import com.lpstudio.bolaodagalera.presentation.theme.NavyCard
import com.lpstudio.bolaodagalera.presentation.theme.NavyElevated
import com.lpstudio.bolaodagalera.presentation.theme.Neon
import com.lpstudio.bolaodagalera.presentation.theme.TextMuted
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
            RoundedCornerShape(14.dp)
        ).background(cColor).border(1.dp, bColor, RoundedCornerShape(14.dp)).then(
            if (isUnlocked) {
                Modifier.clickable {
                    onClick()
                }
            } else {
                Modifier
            }
        ).padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = tColor,
            fontSize = 11.sp,
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
                .clip(RoundedCornerShape(16.dp))
                .background(bg)
                .border(1.dp, bColor, RoundedCornerShape(16.dp))
                .then(if (enabled) Modifier.clickable(onClick = onToggle) else Modifier)
                .padding(horizontal = 18.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier =
                    Modifier.width(3.dp).height(16.dp).clip(RoundedCornerShape(2.dp)).background(
                        when {
                            !enabled -> TextMuted.copy(alpha = 0.3f)
                            isCompleted -> Neon
                            else -> Color(0xFFFFC107)
                        }
                    )
                )
                Text(
                    "Grupo $group",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) Color.White else TextMuted.copy(alpha = 0.5f)
                )
                if (enabled) {
                    if (isCompleted) Text("✅", fontSize = 12.sp) else Text("⏳", fontSize = 12.sp)
                } else {
                    Text("🔒", fontSize = 10.sp, modifier = Modifier.padding(bottom = 1.dp))
                }
            }
            if (enabled) {
                Icon(
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
    Text(
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
                    CircularProgressIndicator(modifier = Modifier.size(size * 0.5f), strokeWidth = 1.dp, color = Neon)
                },
                error = { Text(text = flag, fontSize = (size.value * 0.7f).sp, fontWeight = FontWeight.Bold, color = Color.White) }
            )
        } else {
            Text(
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
    Surface(
        color = NavyElevated,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                initials = user.name.take(1).uppercase(),
                size = 40.dp,
                fontSize = 14.sp,
                borderColor = accentColor.copy(alpha = 0.5f)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(text = label, color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
            IconButton(onClick = onDeny, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, null, tint = ErrorRed, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onApprove, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Check, null, tint = Neon, modifier = Modifier.size(20.dp))
            }
        }
    }
}

fun formatMatchDate(millis: Long): String {
    if (millis == Match.NO_DATE_MILLIS) return "Data a definir"
    val tz = TimeZone.currentSystemDefault()
    val dt = Instant.fromEpochMilliseconds(millis).toLocalDateTime(tz)
    val dayOfWeek =
        when (dt.dayOfWeek) {
            DayOfWeek.MONDAY -> "SEG"
            DayOfWeek.TUESDAY -> "TER"
            DayOfWeek.WEDNESDAY -> "QUA"
            DayOfWeek.THURSDAY -> "QUI"
            DayOfWeek.FRIDAY -> "SEX"
            DayOfWeek.SATURDAY -> "SÁB"
            DayOfWeek.SUNDAY -> "DOM"
        }
    val monthName =
        when (dt.month) {
            Month.JANUARY -> "JAN"
            Month.FEBRUARY -> "FEV"
            Month.MARCH -> "MAR"
            Month.APRIL -> "ABR"
            Month.MAY -> "MAI"
            Month.JUNE -> "JUN"
            Month.JULY -> "JUL"
            Month.AUGUST -> "AGO"
            Month.SEPTEMBER -> "SET"
            Month.OCTOBER -> "OUT"
            Month.NOVEMBER -> "NOV"
            Month.DECEMBER -> "DEZ"
        }
    val dayValue = dt.dayOfMonth.toString().padStart(2, '0')
    val hour = dt.hour.toString().padStart(2, '0')
    val minute = dt.minute.toString().padStart(2, '0')

    return "$dayOfWeek, $dayValue $monthName $hour:$minute"
}
