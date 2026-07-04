package com.lpstudio.bolaodagalera.presentation.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.RankingEntry
import com.lpstudio.bolaodagalera.presentation.theme.*
import com.lpstudio.bolaodagalera.presentation.components.UserAvatar
import com.lpstudio.bolaodagalera.util.getInitials
import com.lpstudio.bolaodagalera.util.resolveDisplayName
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(bolaoId: String) {
    val viewModel: RankingViewModel = koinInject(parameters = { parametersOf(bolaoId) })
    val uiState by viewModel.uiState.collectAsState()
    
    var showHitsDialog by remember { mutableStateOf(false) }

    if (showHitsDialog) {
        Dialog(
            onDismissRequest = { 
                showHitsDialog = false 
                viewModel.clearSelectedParticipant()
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(28.dp),
                color = DeepNavy,
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                ParticipantHitsContent(
                    name = uiState.selectedParticipantName,
                    hits = uiState.selectedParticipantHits,
                    allMatches = uiState.allMatches,
                    onClose = {
                        showHitsDialog = false
                        viewModel.clearSelectedParticipant()
                    }
                )
            }
        }
    }

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
                            currentUserId = uiState.currentUserId,
                            onEntryClick = { 
                                viewModel.selectParticipant(it)
                                showHitsDialog = true
                            }
                        )
                        Spacer(Modifier.height(24.dp))
                    }
                }

                // ── Remaining Rankings List ──────────────────────────────────────
                val remaining = if (uiState.entries.size >= 3) uiState.entries.drop(3) else uiState.entries
                itemsIndexed(remaining, key = { _, entry -> entry.userId }) { index, entry ->
                    val rank = if (uiState.entries.size >= 3) index + 4 else index + 1
                    RankingItem(
                        rank = rank,
                        entry = entry,
                        isCurrentUser = entry.userId == uiState.currentUserId,
                        onClick = {
                            viewModel.selectParticipant(entry)
                            showHitsDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun Podium(
    first: RankingEntry,
    second: RankingEntry,
    third: RankingEntry,
    currentUserId: String,
    onEntryClick: (RankingEntry) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        PodiumItem(entry = second, rank = 2, height = 150.dp, color = Color(0xFFC0C0C0), isMe = second.userId == currentUserId, modifier = Modifier.weight(1f), onClick = onEntryClick)
        PodiumItem(entry = first, rank = 1, height = 180.dp, color = Gold, isMe = first.userId == currentUserId, modifier = Modifier.weight(1.1f), onClick = onEntryClick)
        PodiumItem(entry = third, rank = 3, height = 130.dp, color = Color(0xFFCD7F32), isMe = third.userId == currentUserId, modifier = Modifier.weight(1f), onClick = onEntryClick)
    }
}

@Composable
private fun PodiumItem(
    entry: RankingEntry,
    rank: Int,
    height: androidx.compose.ui.unit.Dp,
    color: Color,
    isMe: Boolean,
    modifier: Modifier = Modifier,
    onClick: (RankingEntry) -> Unit
) {
    Column(
        modifier = modifier.clickable { onClick(entry) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            UserAvatar(
                initials = entry.userName.getInitials(),
                size = if (rank == 1) 72.dp else 60.dp,
                borderColor = if (isMe) Neon else color
            )
            
            // Badge com a posição
            Surface(
                color = color,
                shape = CircleShape,
                modifier = Modifier.size(24.dp).offset(y = 12.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, DeepNavy)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("$rank", color = DeepNavy, fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        
        Spacer(Modifier.height(18.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth().height(height),
            color = NavyElevated,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isMe) Neon.copy(alpha = 0.5f) else color.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = entry.userNickname.ifBlank { entry.userName }.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${entry.points}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = color
                )
                Text(
                    text = "PONTOS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = color.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun RankingItem(
    rank: Int,
    entry: RankingEntry,
    isCurrentUser: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = if (isCurrentUser) Neon.copy(alpha = 0.05f) else NavyElevated,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (isCurrentUser) Neon.copy(alpha = 0.4f) else GlassBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rank",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = if (isCurrentUser) Neon else TextMuted,
                modifier = Modifier.width(32.dp)
            )
            
            UserAvatar(
                initials = entry.userName.getInitials(),
                size = 40.dp
            )
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.userNickname.ifBlank { entry.userName },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (entry.userNickname.isNotBlank()) {
                    Text(
                        text = "@${entry.userName.lowercase().replace(" ", "")}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${entry.points}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isCurrentUser) Neon else Color.White
                )
                Text(
                    text = "PTS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun ParticipantHitsContent(
    name: String,
    hits: List<ParticipantHit>,
    allMatches: List<Match>,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Acertos de $name",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        val totalPoints = hits.sumOf { it.points }
        Text(
            text = "$totalPoints pontos ganhos em ${hits.size} palpites",
            fontSize = 13.sp,
            color = TextMuted
        )
        
        Spacer(Modifier.height(24.dp))
        
        if (hits.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhum acerto registrado ainda.", color = TextMuted, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)
            ) {
                items(hits.size) { index ->
                    val hit = hits[index]
                    HitItem(hit, allMatches)
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        TextButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Fechar", color = Neon, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HitItem(hit: ParticipantHit, allMatches: List<Match>) {
    val date = remember(hit.match.matchDateMillis) {
        val dt = Instant.fromEpochMilliseconds(hit.match.matchDateMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val d = dt.dayOfMonth.toString().padStart(2, '0')
        val m = dt.monthNumber.toString().padStart(2, '0')
        "$d/$m"
    }

    val (hName, hFlag) = remember(hit.match.id, hit.match.homeTeam, hit.match.homeTeamFlag, allMatches) {
        resolveDisplayName(hit.match.id, hit.match.homeTeam, hit.match.homeTeamFlag, allMatches, true)
    }
    val (aName, aFlag) = remember(hit.match.id, hit.match.awayTeam, hit.match.awayTeamFlag, allMatches) {
        resolveDisplayName(hit.match.id, hit.match.awayTeam, hit.match.awayTeamFlag, allMatches, false)
    }

    val hAnnotatedFlag = remember(hFlag) {
        if (hFlag.contains(" ou ")) {
            buildAnnotatedString {
                val parts = hFlag.split(" ou ")
                parts.forEachIndexed { index, part ->
                    append(part)
                    if (index < parts.size - 1) {
                        withStyle(style = SpanStyle(fontSize = 11.sp, fontWeight = FontWeight.Normal)) {
                            append(" ou ")
                        }
                    }
                }
            }
        } else {
            AnnotatedString(hFlag)
        }
    }

    val aAnnotatedFlag = remember(aFlag) {
        if (aFlag.contains(" ou ")) {
            buildAnnotatedString {
                val parts = aFlag.split(" ou ")
                parts.forEachIndexed { index, part ->
                    append(part)
                    if (index < parts.size - 1) {
                        withStyle(style = SpanStyle(fontSize = 11.sp, fontWeight = FontWeight.Normal)) {
                            append(" ou ")
                        }
                    }
                }
            }
        } else {
            AnnotatedString(aFlag)
        }
    }

    val accentColor = if (hit.points == 3) Neon else Gold

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = accentColor.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = hAnnotatedFlag, fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "$hName x $aName",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = aAnnotatedFlag, fontSize = 16.sp)
                }
                
                val groupText = hit.match.group?.let { "Grupo $it • " } ?: ""
                Text(
                    text = "$groupText$date • Placar: ${hit.match.homeScore}x${hit.match.awayScore}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Palpite
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PALPITE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = TextMuted,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${hit.prediction.homeScore}x${hit.prediction.awayScore}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                
                Spacer(Modifier.width(16.dp))
                
                // Pontos
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+${hit.points}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = DeepNavy
                    )
                }
            }
        }
    }
}
