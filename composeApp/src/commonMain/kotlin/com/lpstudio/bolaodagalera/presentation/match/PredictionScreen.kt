package com.lpstudio.bolaodagalera.presentation.match

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.presentation.theme.*
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionScreen(
    bolaoId: String,
    matchId: String,
    onSaved: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel: PredictionViewModel = koinInject(parameters = { parametersOf(bolaoId, matchId) })
    val uiState by viewModel.uiState.collectAsState()
    val authRepository = koinInject<AuthRepository>()
    val userId = authRepository.currentUser?.id ?: ""

    LaunchedEffect(userId) { viewModel.load(userId) }
    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onSaved() }

    var homeScore by remember { mutableIntStateOf(0) }
    var awayScore by remember { mutableIntStateOf(0) }

    LaunchedEffect(uiState.existingPrediction) {
        uiState.existingPrediction?.let {
            homeScore = it.homeScore
            awayScore = it.awayScore
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        when {
            uiState.isLoading && uiState.match == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Neon, strokeWidth = 2.dp)
                }
            }

            uiState.match != null -> {
                val match = uiState.match!!

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ── Stadium header ────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GradientHero)
                            .padding(top = 16.dp, bottom = 24.dp, start = 8.dp, end = 20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = onNavigateBack, modifier = Modifier.size(44.dp)) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Voltar",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                                match.group?.let { group ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(NavyElevated)
                                            .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            "Grupo $group • ${match.phase.label}",
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TeamHero(flag = match.homeTeamFlag, name = match.homeTeam)

                                Text(
                                    "VS",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextMuted.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(bottom = 36.dp),
                                    letterSpacing = 2.sp
                                )

                                TeamHero(flag = match.awayTeamFlag, name = match.awayTeam)
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // ── Score picker ──────────────────────────────────────────
                    Text(
                        "Qual será o placar?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(24.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 40.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ScoreStepper(
                            value = homeScore,
                            onIncrement = { homeScore++ },
                            onDecrement = { if (homeScore > 0) homeScore-- },
                            teamName = match.homeTeam.take(10)
                        )

                        Text(
                            "×",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )

                        ScoreStepper(
                            value = awayScore,
                            onIncrement = { awayScore++ },
                            onDecrement = { if (awayScore > 0) awayScore-- },
                            teamName = match.awayTeam.take(10)
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Points info ───────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(NavyCard)
                            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            PointBadge(
                                emoji = "🎯", 
                                pts = (uiState.bolao?.pointsExactScore ?: 3).toString(), 
                                label = "Placar exato"
                            )
                            VerticalDivider(color = GlassBorder, modifier = Modifier.height(48.dp))
                            PointBadge(
                                emoji = "✅", 
                                pts = (uiState.bolao?.pointsWinnerOrDraw ?: 1).toString(), 
                                label = "Resultado certo"
                            )
                            VerticalDivider(color = GlassBorder, modifier = Modifier.height(48.dp))
                            PointBadge(
                                emoji = "❌", 
                                pts = "0", 
                                label = "Errou"
                            )
                        }
                    }

                    uiState.error?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(it, color = ErrorRed, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 20.dp))
                    }

                    Spacer(Modifier.weight(1f))

                    // ── Save button ───────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 32.dp)
                    ) {
                        GradientSaveButton(
                            text = if (uiState.existingPrediction != null) "Atualizar palpite" else "Salvar palpite",
                            isLoading = uiState.isLoading,
                            onClick = { viewModel.savePrediction(userId, homeScore, awayScore) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamHero(flag: String, name: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(120.dp)
    ) {
        Box(
            modifier = Modifier
                .size(86.dp)
                .clip(CircleShape)
                .background(NavyElevated)
                .border(2.dp, GlassBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(flag, fontSize = 42.sp, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(12.dp))
        Text(
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
private fun ScoreStepper(
    value: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    teamName: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // + button
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Transparent)
                .border(1.dp, Neon.copy(alpha = 0.5f), CircleShape)
                .clickable(onClick = onIncrement),
            contentAlignment = Alignment.Center
        ) {
            Text("+", fontSize = 24.sp, fontWeight = FontWeight.Medium, color = Neon)
        }

        Spacer(Modifier.height(12.dp))

        // Score display
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(NavyElevated)
                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$value",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }

        Spacer(Modifier.height(12.dp))

        // – button
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Transparent)
                .border(1.dp, if (value > 0) TextMuted.copy(alpha = 0.5f) else GlassBorder, CircleShape)
                .clickable(enabled = value > 0, onClick = onDecrement),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "–",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = if (value > 0) TextMuted else TextSubtle
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
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
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            "$pts pt${if (pts != "1") "s" else ""}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label, 
            fontSize = 10.sp, 
            color = TextMuted, 
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun GradientSaveButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(GradientPrimary)
            .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp,
                color = DeepNavy
            )
        } else {
            Text(
                text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DeepNavy
            )
        }
    }
}
