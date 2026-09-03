package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lpstudio.bolaodagalera.LauncherProvider
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTheme
import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.domain.model.RankingEntry
import com.lpstudio.bolaodagalera.util.TimeSource

private const val PREVIEW_USER_ID = "pauloricha"

@Suppress("MagicNumber")
private fun previewMockMatches(now: Long): List<Match> = listOf(
    Match(
        id = "GS-A-1",
        homeTeam = "River Plate",
        awayTeam = "Nacional",
        homeTeamCode = "RIV",
        awayTeamCode = "NAC",
        homeTeamFlag = "🇦🇷",
        awayTeamFlag = "🇺🇾",
        matchDateMillis = now - (2 * 60 * 60 * 1000),
        phase = Phase.GROUP_STAGE,
        group = "A",
        homeScore = 1,
        awayScore = 0
    ),
    Match(
        id = "GS-A-2",
        homeTeam = "Palmeiras",
        awayTeam = "River Plate",
        homeTeamCode = "PAL",
        awayTeamCode = "RIV",
        homeTeamFlag = "🐷",
        awayTeamFlag = "⚪️",
        matchDateMillis = now + (30 * 60 * 1000),
        phase = Phase.GROUP_STAGE,
        group = "A"
    ),
    Match(
        id = "GS-B-1",
        homeTeam = "Flamengo",
        awayTeam = "Peñarol",
        homeTeamCode = "FLA",
        awayTeamCode = "PEN",
        homeTeamFlag = "🔴",
        awayTeamFlag = "🟡",
        matchDateMillis = now + (24 * 60 * 60 * 1000),
        phase = Phase.GROUP_STAGE,
        group = "B"
    ),
    Match(
        id = "KO-1",
        homeTeam = "Atlético-MG",
        awayTeam = "Boca Juniors",
        homeTeamCode = "CAM",
        awayTeamCode = "BOC",
        homeTeamFlag = "🐔",
        awayTeamFlag = "🟦",
        matchDateMillis = now + (25 * 60 * 60 * 1000),
        phase = Phase.ROUND_OF_16
    )
)

@Suppress("MagicNumber")
private fun buildBolaoDetailPreviewUiState(): BolaoUiState {
    val mockBolao =
        Bolao(
            id = "bolao-1",
            name = "Bolão da Libertadores",
            description = "Participe do maior bolão de futebol!",
            code = "LIB26",
            ownerId = PREVIEW_USER_ID,
            participants = listOf(PREVIEW_USER_ID, "user-2"),
            createdAtMillis = 1781136000000L
        )
    val mockParticipants =
        listOf(
            RankingEntry(PREVIEW_USER_ID, "Paulo Teste Silva", "Paulão", 10, 2, 4),
            RankingEntry("user-2", "Maria Silva", "Maria", 8, 1, 5)
        )
    val mockMatches = previewMockMatches(TimeSource.nowMillis())
    val mockPredictions =
        mapOf("GS-A-1" to Prediction(userId = PREVIEW_USER_ID, matchId = "GS-A-1", homeScore = 1, awayScore = 0))
    return BolaoUiState(
        bolao = mockBolao,
        matches = mockMatches,
        userPredictions = mockPredictions,
        participants = mockParticipants,
        isLoading = false
    )
}

@Preview
@Composable
fun BolaoDetailScreenPreview() {
    val uiState = buildBolaoDetailPreviewUiState()
    BolaoTheme {
        BolaoDetailContent(
            bolaoId = "bolao-1",
            uiState = uiState,
            isOwner = true,
            isAppOwner = true,
            launcherProvider =
            object : LauncherProvider {
                override fun shareText(text: String) {}

                override fun sendEmail(address: String, subject: String, body: String) {}

                override fun sendWhatsApp(phone: String, text: String) {}
            },
            callbacks =
            BolaoDetailCallbacks(
                onLeaveBolao = {},
                onApproveJoin = { _, _ -> },
                onApproveLeave = { _, _ -> },
                onNavigateToPrediction = {},
                onNavigateToAllPredictions = {},
                onNavigateToEdit = {},
                onNavigateToAddParticipants = {},
                onNavigateToHelp = {},
                onSaveAdminScore = { _, _, _ -> },
                onNavigateBack = {}
            )
        )
    }
}
