package com.lpstudio.bolaodagalera.presentation.match

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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

@OptIn(ExperimentalMaterial3Api::class)
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
    val isActuallyFinished = match?.status == "FINISHED" || (isFinished && now > ((match?.matchDateMillis ?: 0L) + 7200_000L))
    val hasStarted = now >= (match?.matchDateMillis ?: 0L)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Palpites da Galera", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (match != null) {
                        IconButton(onClick = {
                            val statusText = when {
                                now < match.matchDateMillis -> "Próximo Jogo ⏳"
                                isActuallyFinished -> "Resultado Final: $hReal x $aReal ✅"
                                else -> "Em Andamento: $hReal x $aReal ⚽"
                            }
                            
                            val header = "📊 *PALPITES DA GALERA*\n${match.homeTeamFlag} ${match.homeTeam} x ${match.awayTeam} ${match.awayTeamFlag}\n$statusText\n\n"
                            
                            val list = itemsList.mapIndexed { index, item ->
                                val p = item.first
                                val pred = item.second
                                val name = p.userNickname.ifBlank { p.userName }
                                val predText = if (pred != null) {
                                    if (isAdminViewingBeforeStart && p.userId != currentUserId) "Palpitou 🔒" 
                                    else "${pred.homeScore} x ${pred.awayScore}"
                                } else "Sem palpite 😶‍🌫️"
                                "${index + 1}. $name: $predText"
                            }.joinToString("\n")
                            
                            val inviteUrl = uiState.bolao?.let { "https://bolaodagalera.app/invite?code=${it.code}" } ?: ""
                            val footer = "\n\nBaixe o *Bolão da Galera* e participe! 🏆"
                            val link = if (inviteUrl.isNotBlank()) "\nLink do Bolão: $inviteUrl" else ""
                            
                            launcherProvider.shareText(header + list + link + footer)
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Compartilhar", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepNavy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = DeepNavy
    ) { padding ->
        if (match == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Neon)
            }
        } else {
            val statusLabel = when {
                isAdminViewingBeforeStart -> "Visualização Admin"
                isActuallyFinished -> "Jogo encerrado"
                else -> "Jogo em andamento"
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 40.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(NavyElevated)
                                        .border(1.5.dp, GlassBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(match.homeTeamFlag, fontSize = 38.sp)
                                }
                                Text(
                                    match.homeTeam,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    statusLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAdminViewingBeforeStart) Gold else TextMuted,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(Modifier.height(10.dp))
                                
                                if (!isAdminViewingBeforeStart) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text("$hReal", fontSize = 36.sp, fontWeight = FontWeight.Black, color = Neon)
                                        Text("×", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                        Text("$aReal", fontSize = 36.sp, fontWeight = FontWeight.Black, color = Neon)
                                    }
                                } else {
                                    Icon(Icons.Default.Lock, null, tint = TextMuted.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(NavyElevated)
                                        .border(1.5.dp, GlassBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(match.awayTeamFlag, fontSize = 38.sp)
                                }
                                Text(
                                    match.awayTeam,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider(
                        color = GlassBorder, 
                        thickness = 1.dp, 
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 20.dp)
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
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
                                Text(
                                    text = if (hasNickname) participant.userNickname else participant.userName,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                if (hasNickname) {
                                    Text(text = participant.userName, color = TextMuted, fontSize = 11.sp, maxLines = 1)
                                }
                            }

                            if (pred != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (isAdminViewingBeforeStart && participant.userId != currentUserId) {
                                        Text("Palpitou", color = Neon, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(DeepNavy.copy(alpha = 0.6f))
                                                .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("${pred.homeScore} × ${pred.awayScore}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    
                                    if (!isAdminViewingBeforeStart) {
                                        val pointsColor = when (pts) {
                                            3 -> Neon
                                            1 -> Gold
                                            else -> TextMuted.copy(alpha = 0.4f)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .width(44.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(pointsColor.copy(alpha = 0.12f))
                                                .border(1.dp, pointsColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = if (pts > 0) "+$pts" else "0", color = pointsColor, fontSize = 15.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            } else {
                                Text("Sem palpite", color = TextSubtle, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}
