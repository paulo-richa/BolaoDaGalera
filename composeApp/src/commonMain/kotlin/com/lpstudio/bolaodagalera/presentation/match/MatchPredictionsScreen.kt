package com.lpstudio.bolaodagalera.presentation.match

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.domain.model.RankingEntry
import com.lpstudio.bolaodagalera.util.TimeSource
import com.lpstudio.bolaodagalera.util.getInitials
import com.lpstudio.bolaodagalera.presentation.bolao.BolaoViewModel
import com.lpstudio.bolaodagalera.presentation.components.UserAvatar
import com.lpstudio.bolaodagalera.presentation.theme.*
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun MatchPredictionsScreen(
    bolaoId: String,
    matchId: String,
    onNavigateBack: () -> Unit
) {
    val viewModel: BolaoViewModel = koinInject(parameters = { parametersOf(bolaoId) })
    val uiState by viewModel.uiState.collectAsState()
    val launcherProvider = com.lpstudio.bolaodagalera.rememberLauncherProvider()
    
    val authRepository: com.lpstudio.bolaodagalera.domain.repository.AuthRepository = koinInject()
    val currentUserId = authRepository.currentUser?.id ?: ""
    val isOwner = uiState.bolao?.ownerId == currentUserId

    val match = uiState.matches.find { it.id == matchId }
    val predictions = uiState.allPredictions.filter { it.matchId == matchId }
    val participants = uiState.participants

    val now = TimeSource.nowMillis()
    val calculatePointsUseCase = remember { com.lpstudio.bolaodagalera.domain.usecase.CalculatePointsUseCase() }
    
    val hReal = match?.homeScore ?: 0
    val aReal = match?.awayScore ?: 0
    val isFinished = match?.isFinished ?: false
    val matchDate = match?.matchDateMillis ?: 0L
    val isActuallyFinished = match?.status == "FINISHED" || (isFinished && now > (matchDate + 7200_000L))
    val hasStarted = now >= matchDate
    val isAdminViewingBeforeStart = isOwner && !hasStarted

    val itemsList = remember(predictions, participants, hReal, aReal, isAdminViewingBeforeStart) {
        participants.map { participant ->
            val pred = predictions.find { it.userId == participant.userId }
            val pts = if (pred != null && !isAdminViewingBeforeStart) {
                calculatePointsUseCase(pred, hReal, aReal)
            } else 0
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        if (match == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Neon)
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                // ── Header Premium unificado (Igual Imagem 1) ───────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GradientHero)
                        .drawBehind {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.05f), Color.Transparent),
                                    endY = size.height * 0.5f
                                )
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Neon.copy(alpha = 0.15f), Color.Transparent),
                                    center = Offset(size.width * 0.9f, 0f),
                                    radius = 220.dp.toPx()
                                ),
                                radius = 220.dp.toPx(),
                                center = Offset(size.width * 0.9f, 0f)
                            )
                        }
                        .padding(top = 12.dp, bottom = 24.dp)
                ) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    "Voltar",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            
                            Text(
                                "Palpites da Galera",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.weight(1f),
                                letterSpacing = (-0.5).sp
                            )

                            IconButton(onClick = {
                                val header = buildString {
                                    append("📊 *PALPITES DA GALERA*\n\n")
                                    append("${match.homeTeamFlag} ${match.homeTeam} ")
                                    if (hasStarted || isActuallyFinished) {
                                        append("$hReal x $aReal ")
                                    } else {
                                        append("x ")
                                    }
                                    append("${match.awayTeam} ${match.awayTeamFlag}")
                                    
                                    if (hasStarted || isActuallyFinished) {
                                        val label = if (isActuallyFinished) " - Jogo encerrado" else " - Jogo em andamento"
                                        append(label)
                                    }
                                    append("\n\n")
                                }
                                
                                val list = itemsList.mapIndexed { index, item ->
                                    val p = item.first
                                    val pred = item.second
                                    val pts = item.third
                                    val name = p.userNickname.ifBlank { p.userName }
                                    
                                    if (pred != null) {
                                        val score = if (isAdminViewingBeforeStart && p.userId != currentUserId) "Palpitou 🔒" 
                                                   else "${pred.homeScore} x ${pred.awayScore}"
                                        
                                        val ptsIcon = if (!isAdminViewingBeforeStart && isActuallyFinished) {
                                            when (pts) {
                                                uiState.bolao?.pointsExactScore ?: 3 -> " 🎯"
                                                uiState.bolao?.pointsWinnerOrDraw ?: 1 -> " ✅"
                                                else -> " ❌"
                                            }
                                        } else ""

                                        val pointsLabel = if (!isAdminViewingBeforeStart && (hasStarted || isActuallyFinished)) {
                                            " - *${pts} ${if (pts == 1) "PONTO" else "PONTOS"}*"
                                        } else ""
                                        "${index + 1}. $name: $score$pointsLabel$ptsIcon"
                                    } else {
                                        "${index + 1}. $name: Sem palpite 😶‍🌫️"
                                    }
                                }.joinToString("\n")
                                
                                val inviteUrl = uiState.bolao?.let { "https://bolaodagalera-bb002.web.app/invite?code=${it.code}" } ?: ""
                                val storeLink = "https://play.google.com/store/apps/details?id=com.lpstudio.bolaodagalera"
                                val footer = "\n\nBaixe o *Bolão da Galera* ($storeLink) e participe! 🏆"
                                
                                launcherProvider.shareText(header + list + footer)
                            }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Share, "Compartilhar", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Score Info (DENTRO do Header para dar a altura correta)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier.size(64.dp).clip(CircleShape).background(NavyElevated.copy(alpha = 0.6f)).border(1.dp, GlassBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) { Text(match.homeTeamFlag, fontSize = 34.sp) }
                                Spacer(Modifier.height(8.dp))
                                Text(match.homeTeam, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            }

                            val statusLabel = when {
                                isAdminViewingBeforeStart -> "Visualização Admin"
                                isActuallyFinished -> "Jogo encerrado"
                                else -> "Jogo em andamento"
                            }

                            Column(Modifier.padding(horizontal = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(statusLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isAdminViewingBeforeStart) Gold else TextMuted, letterSpacing = 0.5.sp)
                                Spacer(Modifier.height(8.dp))
                                if (!isAdminViewingBeforeStart) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("$hReal", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Neon)
                                        Text("×", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                        Text("$aReal", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Neon)
                                    }
                                } else {
                                    Icon(Icons.Default.Lock, null, tint = TextMuted.copy(alpha = 0.5f), modifier = Modifier.size(28.dp))
                                }
                            }

                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier.size(64.dp).clip(CircleShape).background(NavyElevated.copy(alpha = 0.6f)).border(1.dp, GlassBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) { Text(match.awayTeamFlag, fontSize = 34.sp) }
                                Spacer(Modifier.height(8.dp))
                                Text(match.awayTeam, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }

                // ── Lista de Palpites ────────────────────────────────────────────────
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
                ) {
                    item {
                        HorizontalDivider(
                            color = GlassBorder, 
                            thickness = 1.dp, 
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
                        )
                    }

                    items(itemsList) { item ->
                        val participant = item.first
                        val pred = item.second
                        val pts = item.third

                        Surface(
                            color = NavyElevated,
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(
                                    initials = participant.userName.getInitials(),
                                    size = 36.dp,
                                    fontSize = 14.sp,
                                    borderColor = Neon.copy(alpha = 0.5f)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    val hasNickname = participant.userNickname.isNotBlank()
                                    Text(text = if (hasNickname) participant.userNickname else participant.userName, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    if (hasNickname) { Text(text = participant.userName, color = TextMuted, fontSize = 11.sp, maxLines = 1) }
                                }
                                if (pred != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        if (isAdminViewingBeforeStart && participant.userId != currentUserId) {
                                            Text("Palpitou", color = Neon, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        } else {
                                            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(DeepNavy.copy(alpha = 0.6f)).border(1.dp, GlassBorder, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                                                Text("${pred.homeScore} × ${pred.awayScore}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        if (!isAdminViewingBeforeStart) {
                                            val pointsColor = when (pts) { 3 -> Neon; 1 -> Gold; else -> TextMuted.copy(alpha = 0.4f) }
                                            Box(modifier = Modifier.width(44.dp).clip(RoundedCornerShape(10.dp)).background(pointsColor.copy(alpha = 0.12f)).border(1.dp, pointsColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp)).padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                                Text(text = if (pts > 0) "+$pts" else "0", color = pointsColor, fontSize = 15.sp, fontWeight = FontWeight.Black)
                                            }
                                        }
                                    }
                                } else { Text("Sem palpite", color = TextSubtle, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                            }
                        }
                    }
                }
            }
        }
    }
}
