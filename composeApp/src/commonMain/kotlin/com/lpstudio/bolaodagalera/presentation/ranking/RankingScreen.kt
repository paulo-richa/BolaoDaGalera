package com.lpstudio.bolaodagalera.presentation.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lpstudio.bolaodagalera.domain.model.RankingEntry
import com.lpstudio.bolaodagalera.presentation.theme.*
import com.lpstudio.bolaodagalera.presentation.components.UserAvatar
import com.lpstudio.bolaodagalera.util.getInitials
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun RankingScreen(bolaoId: String) {
    val viewModel: RankingViewModel = koinInject(parameters = { parametersOf(bolaoId) })
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Neon, strokeWidth = 2.dp)
        }

        uiState.entries.isEmpty() && uiState.error == null -> Box(
            Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Nenhum participante encontrado", color = TextMuted)
        }

        else -> Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 60.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ── Podium Section ───────────────────────────────────────────────
                if (uiState.entries.size >= 3) {
                    item(key = "podium", contentType = "podium") {
                        Spacer(Modifier.height(12.dp))
                        Podium(
                            first = uiState.entries[0],
                            second = uiState.entries[1],
                            third = uiState.entries[2],
                            currentUserId = uiState.currentUserId
                        )
                        Spacer(Modifier.height(20.dp))
                    }
                }

                // ── Header Stats ──────────────────────────────────────────────────
                item(key = "header-stats", contentType = "header") {
                    Surface(
                        color = NavyCard.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("#", modifier = Modifier.width(30.dp), fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Text("PARTICIPANTE", modifier = Modifier.weight(1f), fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            
                            Row(
                                modifier = Modifier.width(110.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("PTS", modifier = Modifier.width(40.dp), fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                Text("🎯", modifier = Modifier.width(30.dp), fontSize = 12.sp, textAlign = TextAlign.Center)
                                Text("✅", modifier = Modifier.width(30.dp), fontSize = 12.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                // ── List Section ─────────────────────────────────────────────────
                itemsIndexed(
                    items = uiState.entries,
                    key = { _, entry -> "rank-${entry.userId}" },
                    contentType = { _, _ -> "ranking-row" }
                ) { index, entry ->
                    RankingRow(
                        position = index + 1,
                        entry = entry,
                        isCurrentUser = entry.userId == uiState.currentUserId
                    )
                }

                // ── Legend Section ───────────────────────────────────────────────
                item(key = "legend") {
                    Spacer(Modifier.height(24.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(NavyCard.copy(alpha = 0.4f))
                            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "CRITÉRIOS DE DESEMPATE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White.copy(alpha = 0.5f),
                            letterSpacing = 1.5.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎯", fontSize = 14.sp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Placar Exato", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Acerto do resultado cheio da partida.", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✅", fontSize = 14.sp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Resultado", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Acerto apenas do vencedor ou do empate.", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }

            // Sombra/Blur no topo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(Brush.verticalGradient(listOf(DeepNavy, Color.Transparent)))
                    .align(Alignment.TopCenter)
            )

            // Sombra/Blur na base
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, DeepNavy)))
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun Podium(
    first: RankingEntry,
    second: RankingEntry,
    third: RankingEntry,
    currentUserId: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            PodiumPillar(
                entry = second,
                position = 2,
                isCurrentUser = second.userId == currentUserId,
                height = 100.dp,
                modifier = Modifier.weight(1f)
            )
            PodiumPillar(
                entry = first,
                position = 1,
                isCurrentUser = first.userId == currentUserId,
                height = 140.dp,
                modifier = Modifier.weight(1.1f)
            )
            PodiumPillar(
                entry = third,
                position = 3,
                isCurrentUser = third.userId == currentUserId,
                height = 85.dp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PodiumPillar(
    entry: RankingEntry,
    position: Int,
    isCurrentUser: Boolean,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val accentColor = when (position) {
        1 -> Gold
        2 -> Color(0xFFC0C0C0)
        else -> Color(0xFFCD7F32)
    }
    
    val medal = when (position) { 1 -> "🥇"; 2 -> "🥈"; else -> "🥉" }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Avatar with crown for #1
        Box(contentAlignment = Alignment.TopCenter) {
            UserAvatar(
                initials = entry.userName.getInitials(),
                size = if (position == 1) 64.dp else 52.dp,
                fontSize = if (position == 1) 24.sp else 20.sp,
                borderColor = if (isCurrentUser) Neon else accentColor.copy(alpha = 0.5f)
            )
            if (position == 1) {
                Text("👑", modifier = Modifier.offset(y = (-18).dp), fontSize = 20.sp)
            }
        }

        Spacer(Modifier.height(8.dp))
        
        Text(
            text = entry.userNickname.ifBlank { entry.userName.split(" ").first() },
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isCurrentUser) Neon else Color.White,
            maxLines = 1
        )
        
        Spacer(Modifier.height(12.dp))

        // Pillar
        Surface(
            color = NavyCard,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(accentColor.copy(alpha = 0.15f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(medal, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = entry.points.toString(),
                        fontSize = if (position == 1) 24.sp else 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        "PONTOS",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RankingRow(position: Int, entry: RankingEntry, isCurrentUser: Boolean) {
    val surfaceColor = if (isCurrentUser) NavyElevated else NavyCard
    val borderColor = if (isCurrentUser) Neon.copy(alpha = 0.5f) else GlassBorder

    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position
            Box(
                modifier = Modifier.width(30.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = position.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (position <= 3) Gold else TextMuted
                )
            }

            // Avatar
            UserAvatar(
                initials = entry.userName.getInitials(),
                size = 36.dp,
                fontSize = 14.sp,
                borderColor = if (isCurrentUser) Neon else Neon.copy(alpha = 0.3f)
            )

            Spacer(Modifier.width(12.dp))

            // Name
            Column(modifier = Modifier.weight(1f)) {
                val displayName = entry.userNickname.ifBlank { entry.userName }
                Text(
                    text = if (isCurrentUser) "$displayName (Você)" else displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
            }

            // Stats
            Row(
                modifier = Modifier.width(110.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = entry.points.toString(),
                    modifier = Modifier.width(40.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isCurrentUser) Neon else Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = entry.exactScores.toString(),
                    modifier = Modifier.width(30.dp),
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = entry.correctResults.toString(),
                    modifier = Modifier.width(30.dp),
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
