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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.presentation.theme.DeepNavy
import com.lpstudio.bolaodagalera.presentation.theme.ErrorRed
import com.lpstudio.bolaodagalera.presentation.theme.GlassBorder
import com.lpstudio.bolaodagalera.presentation.theme.GlassWhite
import com.lpstudio.bolaodagalera.presentation.theme.Gold
import com.lpstudio.bolaodagalera.presentation.theme.NavyElevated
import com.lpstudio.bolaodagalera.presentation.theme.Neon
import com.lpstudio.bolaodagalera.presentation.theme.OrangeNeon
import com.lpstudio.bolaodagalera.presentation.theme.TextMuted
import com.lpstudio.bolaodagalera.util.TimeSource
import com.lpstudio.bolaodagalera.util.resolveDisplayName

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
                            withStyle(style = SpanStyle(fontSize = 12.sp)) {
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
                            withStyle(style = SpanStyle(fontSize = 12.sp)) {
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isLive) Color.Transparent else NavyElevated,
        shape = RoundedCornerShape(12.dp),
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
                Surface(
                    onClick = onShowAllPredictions,
                    color = OrangeNeon.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(
                        bottomStart = 10.dp,
                        bottomEnd = 10.dp
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
                            horizontal = 12.dp,
                            vertical = 5.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Check, null, tint = OrangeNeon, modifier = Modifier.size(12.dp))
                        Text(
                            "PALPITES DA GALERA",
                            color = OrangeNeon,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.6.sp
                        )
                    }
                }
            }

            if (ida != null) {
                Surface(
                    color = Gold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(bottomEnd = 10.dp, topStart = 12.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Gold.copy(alpha = 0.3f)),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "IDA: $ida",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = Gold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            if (!(isFin && hasPrediction)) {
                Text(
                    // Enquanto o confronto não estiver confirmado (times TBD), não
                    // mostra a data mesmo que a API já tenha publicado uma para o
                    // "slot" da fase - evita sugerir um confronto que ainda não existe.
                    text = if (isTbd) "Data a definir" else formatMatchDate(match.matchDateMillis),
                    fontSize = 9.sp,
                    color = Color.White,
                    letterSpacing = 0.2.sp,
                    modifier =
                    Modifier
                        .align(if (!showGalera) Alignment.TopCenter else Alignment.TopEnd)
                        .padding(top = 10.dp, end = if (!showGalera) 0.dp else 12.dp)
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
                Surface(
                    color =
                    when (pts) {
                        3 -> Neon.copy(alpha = 0.15f)
                        1 -> Gold.copy(alpha = 0.15f)
                        else -> ErrorRed.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(bottomStart = 10.dp, topEnd = 16.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = if (pts == 1) "+1 PONTO" else "+$pts PONTOS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color =
                        when (pts) {
                            3 -> Neon
                            1 -> Gold
                            else -> ErrorRed
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Spacer(Modifier.height(if (isLock || canPred) 32.dp else 16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = if (hName.isEmpty()) Arrangement.Center else Arrangement.spacedBy(10.dp)
                    ) {
                        TeamIcon(crestUrl = hCrest ?: match.homeTeamCrest, flag = hAnn, isTbd = isTbd, size = 32.dp)
                        if (hName.isNotEmpty()) TeamNameText(name = hName, modifier = Modifier.weight(1f))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
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
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Brush.linearGradient(listOf(sColor.copy(0.15f), sColor.copy(0.05f))))
                                    .then(if (isExact) Modifier.border(2.dp, Neon, RoundedCornerShape(12.dp)) else Modifier)
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("$hP", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = sColor)
                                    Text(
                                        "×",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = sColor.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    Text("$aP", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = sColor)
                                }
                            }
                        } else {
                            Box(
                                modifier =
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Brush.linearGradient(listOf(GlassWhite, GlassWhite)))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("vs", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted.copy(alpha = 0.7f))
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = if (aName.isEmpty()) Arrangement.Center else Arrangement.spacedBy(10.dp, Alignment.End)
                    ) {
                        if (aName.isNotEmpty()) TeamNameText(name = aName, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        TeamIcon(crestUrl = aCrest ?: match.awayTeamCrest, flag = aAnn, isTbd = isTbd, size = 32.dp)
                    }
                }
                if (canPred) {
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = GlassBorder, thickness = 0.5.dp)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier =
                        Modifier.fillMaxWidth().clip(
                            RoundedCornerShape(10.dp)
                        ).background(Neon.copy(alpha = 0.08f)).padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(13.dp), tint = Neon)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (hasPrediction) "EDITAR PALPITE" else "TOQUE PARA PALPITAR",
                            fontSize = 11.sp,
                            color = Neon,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                } else if (isLock) {
                    val dColor = if (isLive) Neon.copy(alpha = 0.3f) else GlassBorder
                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = dColor, thickness = 0.5.dp)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if ((forceLocked || isTbd) && !match.isFinished) {
                            Text(
                                text = "EM BREVE VOCÊ PODERÁ PALPITAR",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Neon.copy(alpha = 0.6f),
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else {
                            val sT =
                                when {
                                    isFin -> "JOGO ENCERRADO"
                                    match.status == "EXTRA_TIME" -> "PRORROGAÇÃO"
                                    match.status == "PENALTIES" -> "PÊNALTIS"
                                    match.status == "PAUSED_EXTRA_TIME" -> "INDO PARA PRORROGAÇÃO"
                                    match.status == "PAUSED_PENALTIES" -> "INDO PARA PÊNALTIS"
                                    match.status == "PAUSED" -> "INTERVALO"
                                    else -> "JOGO EM ANDAMENTO"
                                }
                            val aC = if (isFin) Color.White else Neon
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(top = 4.dp)
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
                                Text(
                                    text = sT,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = aC.copy(alpha = 0.7f),
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Box(
                                modifier =
                                Modifier.padding(
                                    top = 2.dp
                                ).clip(RoundedCornerShape(6.dp)).background(aC.copy(alpha = 0.08f)).then(
                                    if (isAdmin) {
                                        Modifier.clickable {
                                            onOpenAdminScoreDialog()
                                        }
                                    } else {
                                        Modifier
                                    }
                                ).padding(horizontal = 8.dp, vertical = 1.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${match.homeScore ?: 0}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = aC)
                                    Text(
                                        "×",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = aC.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    Text("${match.awayScore ?: 0}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = aC)
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
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Ajustar Placar Oficial", color = Color.White) }, text = {
        Column(modifier = Modifier.imePadding().padding(bottom = 24.dp)) {
            Text(
                "Defina o placar real de ${match.homeTeam} x ${match.awayTeam}",
                fontSize = 14.sp,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = hS,
                    onValueChange = {
                        if (it.length <= 2) {
                            hS =
                                it.filter { c ->
                                    c.isDigit()
                                }
                        }
                    },
                    modifier =
                    Modifier.width(
                        64.dp
                    ),
                    textStyle =
                    LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    ),
                    colors =
                    TextFieldDefaults.colors(
                        focusedContainerColor = GlassWhite,
                        unfocusedContainerColor = GlassWhite.copy(alpha = 0.5f)
                    )
                )
                Text("x", modifier = Modifier.padding(horizontal = 16.dp), color = Color.White, fontWeight = FontWeight.Bold)
                TextField(
                    value = aS,
                    onValueChange = {
                        if (it.length <= 2) {
                            aS =
                                it.filter { c ->
                                    c.isDigit()
                                }
                        }
                    },
                    modifier =
                    Modifier.width(
                        64.dp
                    ),
                    textStyle =
                    LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    ),
                    colors =
                    TextFieldDefaults.colors(
                        focusedContainerColor = GlassWhite,
                        unfocusedContainerColor = GlassWhite.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }, confirmButton = {
        TextButton(onClick = {
            onConfirm(hS.toIntOrNull() ?: 0, aS.toIntOrNull() ?: 0)
        }) {
            Text("SALVAR", color = Neon, fontWeight = FontWeight.Bold)
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text("CANCELAR", color = Color.White.copy(alpha = 0.6f))
        }
    }, containerColor = DeepNavy, shape = RoundedCornerShape(16.dp))
}
