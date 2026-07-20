package com.lpstudio.bolaodagalera.presentation.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.domain.model.RankingEntry
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import com.lpstudio.bolaodagalera.domain.repository.PredictionRepository
import com.lpstudio.bolaodagalera.domain.usecase.CalculatePointsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ParticipantHit(
    val match: Match,
    val prediction: Prediction,
    val points: Int
)

data class RankingUiState(
    val entries: List<RankingEntry> = emptyList(),
    val allMatches: List<Match> = emptyList(),
    val currentUserId: String = "",
    val selectedParticipantHits: List<ParticipantHit> = emptyList(),
    val selectedParticipantName: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

class RankingViewModel(
    private val predictionRepository: PredictionRepository,
    private val bolaoRepository: BolaoRepository,
    private val matchRepository: MatchRepository,
    authRepository: AuthRepository,
    private val calculatePointsUseCase: CalculatePointsUseCase = CalculatePointsUseCase(),
    private val bolaoId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    private var allMatches: List<Match> = emptyList()
    private var allPredictions: List<Prediction> = emptyList()
    private var pointsExact: Int = 3
    private var pointsWinner: Int = 1

    init {
        val userId = authRepository.currentUser?.id ?: ""
        _uiState.update { it.copy(currentUserId = userId) }
        loadData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadData() {
        viewModelScope.launch {
            try {
                val bolao = bolaoRepository.getBolao(bolaoId)
                val championshipId = bolao.championshipId
                pointsExact = bolao.pointsExactScore
                pointsWinner = bolao.pointsWinnerOrDraw

                // Ranking em tempo real
                predictionRepository.getRanking(bolaoId, championshipId, bolao.participants)
                    .onEach { entries ->
                        _uiState.update { it.copy(entries = entries, isLoading = false) }
                    }
                    .catch { e ->
                        _uiState.update { it.copy(error = e.message, isLoading = false) }
                    }
                    .launchIn(viewModelScope)

                // Outros dados (jogos e palpites)
                combine(
                    matchRepository.getMatches(championshipId),
                    predictionRepository.getBolaoAllPredictions(bolaoId)
                ) { matches, predictions ->
                    allMatches = matches
                    allPredictions = predictions
                    _uiState.update { it.copy(allMatches = matches) }
                }.launchIn(viewModelScope)

            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun selectParticipant(entry: RankingEntry) {
        val hits = allPredictions
            .filter { it.userId == entry.userId }
            .mapNotNull { pred ->
                val match = allMatches.find { it.id == pred.matchId }
                if (match?.isFinished == true && match.homeScore != null && match.awayScore != null) {
                    val points = calculatePointsUseCase(
                        pred, 
                        match.homeScore, 
                        match.awayScore,
                        pointsExact,
                        pointsWinner
                    )
                    if (points > 0) {
                        ParticipantHit(match, pred, points)
                    } else null
                } else null
            }
            .sortedByDescending { it.match.matchDateMillis }

        _uiState.update { it.copy(
            selectedParticipantHits = hits,
            selectedParticipantName = entry.userNickname.ifBlank { entry.userName }
        ) }
    }

    fun clearSelectedParticipant() {
        _uiState.update { it.copy(selectedParticipantHits = emptyList(), selectedParticipantName = "") }
    }
}
