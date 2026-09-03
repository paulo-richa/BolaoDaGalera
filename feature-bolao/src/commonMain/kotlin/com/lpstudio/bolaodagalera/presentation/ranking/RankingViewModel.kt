package com.lpstudio.bolaodagalera.presentation.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.ParticipantHit
import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.domain.model.RankingEntry
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import com.lpstudio.bolaodagalera.domain.repository.PredictionRepository
import com.lpstudio.bolaodagalera.domain.usecase.CalculatePointsUseCase
import com.lpstudio.bolaodagalera.domain.usecase.GetParticipantHitsUseCase
import com.lpstudio.bolaodagalera.domain.usecase.GetRankingUseCase
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.ErrorReporter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    private val authRepository: AuthRepository,
    private val crashReporter: CrashReporter,
    private val calculatePointsUseCase: CalculatePointsUseCase = CalculatePointsUseCase(),
    private val getRankingUseCase: GetRankingUseCase = GetRankingUseCase(calculatePointsUseCase),
    private val getParticipantHits: GetParticipantHitsUseCase = GetParticipantHitsUseCase(calculatePointsUseCase),
    private val bolaoId: String
) : ViewModel() {
    private val errorReporter = ErrorReporter(crashReporter)
    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    private var allMatches: List<Match> = emptyList()
    private var allPredictions: List<Prediction> = emptyList()
    private var currentBolao: Bolao = Bolao()

    init {
        val userId = authRepository.currentUser?.id ?: ""
        _uiState.update { it.copy(currentUserId = userId) }
        loadData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadData() {
        viewModelScope.launch {
            try {
                // Observes the pool, matches, and predictions simultaneously. The ranking
                // uses the official points value computed by the Cloud Function when
                // available, and only estimates locally the matches it hasn't processed
                // yet — see GetRankingUseCase.
                bolaoRepository.getBolaoFlow(bolaoId).flatMapLatest { bolao ->
                    currentBolao = bolao
                    val participants = bolao.participants

                    combine(
                        matchRepository.getMatches(bolao.championshipId),
                        predictionRepository.getBolaoAllPredictions(bolaoId),
                        flow { emit(authRepository.getUsers(participants)) }
                    ) { matches, predictions, users ->
                        allMatches = matches
                        allPredictions = predictions
                        getRankingUseCase(bolao, predictions, matches, users)
                    }
                }.onEach { entries ->
                    _uiState.update { it.copy(entries = entries, isLoading = false) }
                }.catch { e ->
                    if (e is CancellationException) throw e
                    val message = errorReporter.reportAndClassify(e, "Erro ao observar ranking do bolão $bolaoId")
                    _uiState.update { it.copy(error = message, isLoading = false) }
                }.launchIn(viewModelScope)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = errorReporter.reportAndClassify(e, "Erro ao carregar ranking do bolão $bolaoId")
                _uiState.update { it.copy(error = message, isLoading = false) }
            }
        }
    }

    fun selectParticipant(entry: RankingEntry) {
        val hits = getParticipantHits(entry.userId, allPredictions, allMatches, currentBolao)

        _uiState.update {
            it.copy(
                selectedParticipantHits = hits,
                selectedParticipantName = entry.userNickname.ifBlank { entry.userName }
            )
        }
    }

    fun clearSelectedParticipant() {
        _uiState.update { it.copy(selectedParticipantHits = emptyList(), selectedParticipantName = "") }
    }
}
