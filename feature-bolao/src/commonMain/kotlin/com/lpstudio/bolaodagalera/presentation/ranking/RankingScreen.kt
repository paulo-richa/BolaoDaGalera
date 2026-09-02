package com.lpstudio.bolaodagalera.presentation.ranking

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import bolaodagalera.feature_bolao.generated.resources.Res
import bolaodagalera.feature_bolao.generated.resources.ranking_correct_emoji
import bolaodagalera.feature_bolao.generated.resources.ranking_empty_message
import bolaodagalera.feature_bolao.generated.resources.ranking_exact_emoji
import bolaodagalera.feature_bolao.generated.resources.ranking_header_participant
import bolaodagalera.feature_bolao.generated.resources.ranking_header_points
import bolaodagalera.feature_bolao.generated.resources.ranking_header_position
import bolaodagalera.feature_bolao.generated.resources.ranking_hit_item_group_prefix
import bolaodagalera.feature_bolao.generated.resources.ranking_hit_item_match_names
import bolaodagalera.feature_bolao.generated.resources.ranking_hit_item_prediction_label
import bolaodagalera.feature_bolao.generated.resources.ranking_hit_item_prediction_score
import bolaodagalera.feature_bolao.generated.resources.ranking_hit_item_round_prefix
import bolaodagalera.feature_bolao.generated.resources.ranking_hit_item_score_label
import bolaodagalera.feature_bolao.generated.resources.ranking_hits_dialog_close
import bolaodagalera.feature_bolao.generated.resources.ranking_hits_dialog_empty
import bolaodagalera.feature_bolao.generated.resources.ranking_hits_dialog_summary
import bolaodagalera.feature_bolao.generated.resources.ranking_hits_dialog_title
import bolaodagalera.feature_bolao.generated.resources.ranking_legend_correct_description
import bolaodagalera.feature_bolao.generated.resources.ranking_legend_correct_title
import bolaodagalera.feature_bolao.generated.resources.ranking_legend_exact_description
import bolaodagalera.feature_bolao.generated.resources.ranking_legend_exact_title
import bolaodagalera.feature_bolao.generated.resources.ranking_legend_title
import bolaodagalera.feature_bolao.generated.resources.ranking_podium_crown_emoji
import bolaodagalera.feature_bolao.generated.resources.ranking_podium_medal_bronze
import bolaodagalera.feature_bolao.generated.resources.ranking_podium_medal_gold
import bolaodagalera.feature_bolao.generated.resources.ranking_podium_medal_silver
import bolaodagalera.feature_bolao.generated.resources.ranking_podium_points_label
import bolaodagalera.feature_bolao.generated.resources.ranking_row_you_suffix
import com.lpstudio.bolaodagalera.designsystem.components.BolaoLoadingIndicator
import com.lpstudio.bolaodagalera.designsystem.components.BolaoSurface
import com.lpstudio.bolaodagalera.designsystem.components.BolaoText
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTextButton
import com.lpstudio.bolaodagalera.designsystem.components.UserAvatar
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoRadius
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoRadiusShape
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoSpacing
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTypography
import com.lpstudio.bolaodagalera.designsystem.theme.DeepNavy
import com.lpstudio.bolaodagalera.designsystem.theme.GlassBorder
import com.lpstudio.bolaodagalera.designsystem.theme.Gold
import com.lpstudio.bolaodagalera.designsystem.theme.NavyCard
import com.lpstudio.bolaodagalera.designsystem.theme.NavyElevated
import com.lpstudio.bolaodagalera.designsystem.theme.Neon
import com.lpstudio.bolaodagalera.designsystem.theme.TextMuted
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.RankingEntry
import com.lpstudio.bolaodagalera.util.getInitials
import com.lpstudio.bolaodagalera.util.resolveDisplayName
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun RankingScreen(bolaoId: String) {
    val viewModel: RankingViewModel = koinViewModel(key = bolaoId) { parametersOf(bolaoId) }
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
            BolaoSurface(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = BolaoSpacing.lg),
                shape = BolaoRadiusShape.xxl,
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
        uiState.isLoading ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                BolaoLoadingIndicator()
            }

        uiState.entries.isEmpty() && uiState.error == null ->
            Box(
                Modifier.fillMaxSize().padding(BolaoSpacing.xxxl),
                contentAlignment = Alignment.Center
            ) {
                BolaoText(stringResource(Res.string.ranking_empty_message), color = TextMuted)
            }

        else ->
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 60.dp),
                    verticalArrangement = Arrangement.spacedBy(BolaoSpacing.sm)
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
                            Spacer(Modifier.height(20.dp))
                        }
                    }

                    // ── Header Stats ──────────────────────────────────────────────────
                    item(key = "header-stats", contentType = "header") {
                        BolaoSurface(
                            color = NavyCard.copy(alpha = 0.5f),
                            shape = BolaoRadiusShape.md,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = BolaoSpacing.lg, vertical = BolaoSpacing.md),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BolaoText(
                                    stringResource(Res.string.ranking_header_position),
                                    modifier = Modifier.width(30.dp),
                                    fontSize = BolaoTypography.bodyMedium.fontSize,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                                BolaoText(
                                    stringResource(Res.string.ranking_header_participant),
                                    modifier = Modifier.weight(1f),
                                    fontSize = BolaoTypography.bodyMedium.fontSize,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    modifier = Modifier.width(110.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    BolaoText(
                                        stringResource(Res.string.ranking_header_points),
                                        modifier = Modifier.width(40.dp),
                                        fontSize = BolaoTypography.bodyMedium.fontSize,
                                        color = TextMuted,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    BolaoText(
                                        stringResource(Res.string.ranking_exact_emoji),
                                        modifier = Modifier.width(30.dp),
                                        fontSize = BolaoTypography.bodyMedium.fontSize,
                                        textAlign = TextAlign.Center
                                    )
                                    BolaoText(
                                        stringResource(Res.string.ranking_correct_emoji),
                                        modifier = Modifier.width(30.dp),
                                        fontSize = BolaoTypography.bodyMedium.fontSize,
                                        textAlign = TextAlign.Center
                                    )
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
                            isCurrentUser = entry.userId == uiState.currentUserId,
                            onClick = {
                                viewModel.selectParticipant(entry)
                                showHitsDialog = true
                            }
                        )
                    }

                    // ── Legend Section ───────────────────────────────────────────────
                    item(key = "legend") {
                        Spacer(Modifier.height(24.dp))
                        Column(
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = BolaoSpacing.xs)
                                .clip(BolaoRadiusShape.lg)
                                .background(NavyCard.copy(alpha = 0.4f))
                                .border(1.dp, GlassBorder, BolaoRadiusShape.lg)
                                .padding(BolaoSpacing.xl),
                            verticalArrangement = Arrangement.spacedBy(BolaoSpacing.md)
                        ) {
                            BolaoText(
                                stringResource(Res.string.ranking_legend_title),
                                fontSize = BolaoTypography.bodyMedium.fontSize,
                                fontWeight = FontWeight.Black,
                                color = Color.White.copy(alpha = 0.5f),
                                letterSpacing = 1.5.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BolaoText(stringResource(Res.string.ranking_exact_emoji), fontSize = BolaoTypography.bodyLarge.fontSize)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    BolaoText(
                                        stringResource(Res.string.ranking_legend_exact_title),
                                        fontSize = BolaoTypography.bodyLarge.fontSize,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    BolaoText(
                                        stringResource(Res.string.ranking_legend_exact_description),
                                        fontSize = BolaoTypography.bodyMedium.fontSize,
                                        color = TextMuted
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BolaoText(stringResource(Res.string.ranking_correct_emoji), fontSize = BolaoTypography.bodyLarge.fontSize)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    BolaoText(
                                        stringResource(Res.string.ranking_legend_correct_title),
                                        fontSize = BolaoTypography.bodyLarge.fontSize,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    BolaoText(
                                        stringResource(Res.string.ranking_legend_correct_description),
                                        fontSize = BolaoTypography.bodyMedium.fontSize,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                // Top shadow/blur
                Box(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .background(Brush.verticalGradient(listOf(DeepNavy, Color.Transparent)))
                        .align(Alignment.TopCenter)
                )

                // Bottom shadow/blur
                Box(
                    modifier =
                    Modifier
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
    currentUserId: String,
    onEntryClick: (RankingEntry) -> Unit
) {
    Box(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = BolaoSpacing.xs),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.md),
            verticalAlignment = Alignment.Bottom
        ) {
            PodiumPillar(
                entry = second,
                position = 2,
                isCurrentUser = second.userId == currentUserId,
                height = 100.dp,
                modifier = Modifier.weight(1f).clickable { onEntryClick(second) }
            )
            PodiumPillar(
                entry = first,
                position = 1,
                isCurrentUser = first.userId == currentUserId,
                height = 140.dp,
                modifier = Modifier.weight(1.1f).clickable { onEntryClick(first) }
            )
            PodiumPillar(
                entry = third,
                position = 3,
                isCurrentUser = third.userId == currentUserId,
                height = 85.dp,
                modifier = Modifier.weight(1f).clickable { onEntryClick(third) }
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
    val accentColor =
        when (position) {
            1 -> Gold
            2 -> Color(0xFFC0C0C0)
            else -> Color(0xFFCD7F32)
        }

    val medal =
        when (position) {
            1 -> stringResource(Res.string.ranking_podium_medal_gold)
            2 -> stringResource(Res.string.ranking_podium_medal_silver)
            else -> stringResource(Res.string.ranking_podium_medal_bronze)
        }

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
                BolaoText(
                    stringResource(Res.string.ranking_podium_crown_emoji),
                    modifier = Modifier.offset(y = (-18).dp),
                    fontSize = BolaoTypography.headlineMedium.fontSize
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        BolaoText(
            text = entry.userNickname.ifBlank { entry.userName.split(" ").first() },
            fontSize = BolaoTypography.bodyMedium.fontSize,
            fontWeight = FontWeight.Bold,
            color = if (isCurrentUser) Neon else Color.White,
            maxLines = 1
        )

        Spacer(Modifier.height(12.dp))

        // Pillar
        BolaoSurface(
            color = NavyCard,
            shape = RoundedCornerShape(topStart = BolaoRadius.lg, topEnd = BolaoRadius.lg),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
            modifier =
            Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            Box(
                modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(accentColor.copy(alpha = 0.15f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BolaoText(medal, fontSize = BolaoTypography.titleLarge.fontSize)
                    Spacer(Modifier.height(4.dp))
                    BolaoText(
                        text = entry.points.toString(),
                        fontSize = if (position == 1) 24.sp else 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    BolaoText(
                        stringResource(Res.string.ranking_podium_points_label),
                        fontSize = BolaoTypography.labelSmall.fontSize,
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
private fun RankingRow(position: Int, entry: RankingEntry, isCurrentUser: Boolean, onClick: () -> Unit) {
    val surfaceColor = if (isCurrentUser) NavyElevated else NavyCard
    val borderColor = if (isCurrentUser) Neon.copy(alpha = 0.5f) else GlassBorder

    BolaoSurface(
        color = surfaceColor,
        shape = BolaoRadiusShape.lg,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier =
            Modifier
                .padding(horizontal = BolaoSpacing.lg, vertical = BolaoSpacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position
            Box(
                modifier = Modifier.width(30.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BolaoText(
                    text = position.toString(),
                    fontSize = BolaoTypography.bodyLarge.fontSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (position <= 3) Gold else TextMuted
                )
            }

            // Avatar
            UserAvatar(
                initials = entry.userName.getInitials(),
                size = 36.dp,
                fontSize = BolaoTypography.bodyLarge.fontSize,
                borderColor = if (isCurrentUser) Neon else Neon.copy(alpha = 0.3f)
            )

            Spacer(Modifier.width(12.dp))

            // Name
            Column(modifier = Modifier.weight(1f)) {
                val displayName = entry.userNickname.ifBlank { entry.userName }
                val label =
                    if (isCurrentUser) {
                        stringResource(Res.string.ranking_row_you_suffix, displayName)
                    } else {
                        displayName
                    }
                BolaoText(
                    text = label,
                    fontSize = BolaoTypography.bodyLarge.fontSize,
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
                BolaoText(
                    text = entry.points.toString(),
                    modifier = Modifier.width(40.dp),
                    fontSize = BolaoTypography.titleLarge.fontSize,
                    fontWeight = FontWeight.Black,
                    color = if (isCurrentUser) Neon else Color.White,
                    textAlign = TextAlign.Center
                )
                BolaoText(
                    text = entry.exactScores.toString(),
                    modifier = Modifier.width(30.dp),
                    fontSize = BolaoTypography.bodyLarge.fontSize,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
                BolaoText(
                    text = entry.correctResults.toString(),
                    modifier = Modifier.width(30.dp),
                    fontSize = BolaoTypography.bodyLarge.fontSize,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ParticipantHitsContent(name: String, hits: List<ParticipantHit>, allMatches: List<Match>, onClose: () -> Unit) {
    Column(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(BolaoSpacing.xxl)
    ) {
        BolaoText(
            text = stringResource(Res.string.ranking_hits_dialog_title, name),
            fontSize = BolaoTypography.headlineMedium.fontSize,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        val totalPoints = hits.sumOf { it.points }
        BolaoText(
            text = stringResource(Res.string.ranking_hits_dialog_summary, totalPoints, hits.size),
            fontSize = BolaoTypography.bodyLarge.fontSize,
            color = TextMuted
        )

        Spacer(Modifier.height(24.dp))

        if (hits.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = BolaoSpacing.huge),
                contentAlignment = Alignment.Center
            ) {
                BolaoText(
                    stringResource(Res.string.ranking_hits_dialog_empty),
                    color = TextMuted,
                    fontSize = BolaoTypography.bodyLarge.fontSize
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(BolaoSpacing.md),
                modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)
            ) {
                items(hits.size) { index ->
                    val hit = hits[index]
                    HitItem(hit, allMatches)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        BolaoTextButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.End)
        ) {
            BolaoText(stringResource(Res.string.ranking_hits_dialog_close), color = Neon, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HitItem(hit: ParticipantHit, allMatches: List<Match>) {
    val date =
        remember(hit.match.matchDateMillis) {
            val dt =
                Instant.fromEpochMilliseconds(hit.match.matchDateMillis)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
            val d = dt.dayOfMonth.toString().padStart(2, '0')
            val m = dt.monthNumber.toString().padStart(2, '0')
            "$d/$m"
        }

    val (hName, _) =
        remember(hit.match.id, hit.match.homeTeam, hit.match.homeTeamFlag, allMatches) {
            resolveDisplayName(hit.match.id, hit.match.homeTeam, hit.match.homeTeamFlag, allMatches, true)
        }
    val (aName, _) =
        remember(hit.match.id, hit.match.awayTeam, hit.match.awayTeamFlag, allMatches) {
            resolveDisplayName(hit.match.id, hit.match.awayTeam, hit.match.awayTeamFlag, allMatches, false)
        }

    val accentColor = if (hit.points == 3) Neon else Gold
    val roundPrefix = stringResource(Res.string.ranking_hit_item_round_prefix)
    val groupPrefix = stringResource(Res.string.ranking_hit_item_group_prefix)
    val scoreLabel =
        stringResource(
            Res.string.ranking_hit_item_score_label,
            hit.match.homeScore.toString(),
            hit.match.awayScore.toString()
        )

    BolaoSurface(
        modifier = Modifier.fillMaxWidth(),
        color = accentColor.copy(alpha = 0.08f),
        shape = BolaoRadiusShape.md,
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(BolaoSpacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                BolaoText(
                    text = stringResource(Res.string.ranking_hit_item_match_names, hName, aName),
                    fontSize = BolaoTypography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(Modifier.height(4.dp))

                val groupInfo =
                    hit.match.group?.let { g ->
                        val clean =
                            g.replace("Grupo", "", ignoreCase = true)
                                .replace("Rodada", "", ignoreCase = true)
                                .trim()
                        if (clean.toIntOrNull() != null) roundPrefix.replace("%1\$s", clean) else groupPrefix.replace("%1\$s", clean)
                    } ?: ""

                val separator = if (groupInfo.isNotEmpty()) " • " else ""

                BolaoText(
                    text = "$groupInfo$separator$date • $scoreLabel",
                    fontSize = BolaoTypography.bodyMedium.fontSize,
                    color = TextMuted
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Prediction
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BolaoText(
                        text = stringResource(Res.string.ranking_hit_item_prediction_label),
                        fontSize = BolaoTypography.labelSmall.fontSize,
                        fontWeight = FontWeight.Black,
                        color = TextMuted,
                        letterSpacing = 0.5.sp
                    )
                    BolaoText(
                        text =
                        stringResource(
                            Res.string.ranking_hit_item_prediction_score,
                            hit.prediction.homeScore.toString(),
                            hit.prediction.awayScore.toString()
                        ),
                        fontSize = BolaoTypography.titleLarge.fontSize,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(Modifier.width(16.dp))

                // Points
                Box(
                    modifier =
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    BolaoText(
                        text = "+${hit.points}",
                        fontSize = BolaoTypography.bodyLarge.fontSize,
                        fontWeight = FontWeight.Black,
                        color = DeepNavy
                    )
                }
            }
        }
    }
}
