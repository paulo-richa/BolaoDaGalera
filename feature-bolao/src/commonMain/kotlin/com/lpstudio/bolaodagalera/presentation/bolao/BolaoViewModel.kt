package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.domain.model.RankingEntry
import com.lpstudio.bolaodagalera.domain.model.User
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import com.lpstudio.bolaodagalera.domain.repository.PredictionRepository
import com.lpstudio.bolaodagalera.domain.usecase.EnrichRankingWithParticipantNamesUseCase
import com.lpstudio.bolaodagalera.domain.usecase.FilterBolaoMatchesUseCase
import com.lpstudio.bolaodagalera.observability.AnalyticsEvents
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.ErrorReporter
import com.lpstudio.bolaodagalera.observability.PerformanceTraces
import com.lpstudio.bolaodagalera.observability.Telemetry
import com.lpstudio.bolaodagalera.observability.appLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BolaoUiState(
    val bolao: Bolao? = null,
    val matches: List<Match> = emptyList(),
    /** Full unfiltered list */
    val allMatches: List<Match> = emptyList(),
    /** matchId -> prediction */
    val userPredictions: Map<String, Prediction> = emptyMap(),
    val participants: List<RankingEntry> = emptyList(),
    val pendingJoinUsers: List<User> = emptyList(),
    val pendingExitUsers: List<User> = emptyList(),
    val allPredictions: List<Prediction> = emptyList(),
    val isLoading: Boolean = true,
    val isLeaveSuccess: Boolean = false,
    val error: String? = null
)

class BolaoViewModel(
    private val bolaoRepository: BolaoRepository,
    private val matchRepository: MatchRepository,
    private val predictionRepository: PredictionRepository,
    private val authRepository: AuthRepository,
    private val bolaoId: String,
    private val crashReporter: CrashReporter,
    private val telemetry: Telemetry,
    private val errorReporter: ErrorReporter = ErrorReporter(crashReporter)
) : ViewModel() {
    private val performanceMonitor get() = telemetry.performanceMonitor
    private val analyticsTracker get() = telemetry.analyticsTracker
    private val filterBolaoMatches = FilterBolaoMatchesUseCase()
    private val enrichRankingWithParticipantNames = EnrichRankingWithParticipantNamesUseCase()
    private val logger = appLogger("BolaoViewModel")
    private val _uiState = MutableStateFlow(BolaoUiState())
    val uiState: StateFlow<BolaoUiState> = _uiState.asStateFlow()

    private val userId = MutableStateFlow(authRepository.currentUser?.id ?: "")
    private var dataCollectionJob: kotlinx.coroutines.Job? = null

    init {
        authRepository.authStateFlow.onEach { user ->
            dataCollectionJob?.cancel()

            if (user == null) {
                _uiState.update { BolaoUiState(isLoading = false) }
            } else {
                userId.value = user.id
                loadBolao()
                observeMatchesPredictionsAndRanking()
            }
        }.launchIn(viewModelScope)
    }

    private fun loadBolao() {
        viewModelScope.launch {
            try {
                val bolao = performanceMonitor.trace(PerformanceTraces.BOLAO_LOAD_DETAIL) { bolaoRepository.getBolao(bolaoId) }
                _uiState.update { it.copy(bolao = bolao) }
                logger.d { "Bolão carregado (${bolao.name})." }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = errorReporter.reportAndClassify(e, "Erro ao carregar bolão $bolaoId")
                _uiState.update { it.copy(error = message, isLoading = false) }
            }
        }
    }

    /** Participant ids for [bolaoFlow], refreshing pending-join/exit user details as a side effect. */
    private fun participantIdsFlow(bolaoFlow: Flow<Bolao>): Flow<List<String>> = bolaoFlow
        .onEach { bolao ->
            val pendingJoin = authRepository.getUsers(bolao.pendingParticipants)
            val pendingExit = authRepository.getUsers(bolao.pendingExits)
            _uiState.update { it.copy(pendingJoinUsers = pendingJoin, pendingExitUsers = pendingExit) }
        }
        .map { it.participants }
        .distinctUntilChanged()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun rankingFlow(bolaoId: String, championshipId: String, participantsFlow: Flow<List<String>>) =
        participantsFlow.flatMapLatest { participants ->
            combine(
                predictionRepository.getRanking(bolaoId, championshipId, participants),
                flow { emit(authRepository.getUsers(participants)) }
            ) { ranking, users -> enrichRankingWithParticipantNames(ranking, users) }
        }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeMatchesPredictionsAndRanking() {
        dataCollectionJob =
            viewModelScope.launch {
                val bolaoFlow =
                    bolaoRepository.getBolaoFlow(bolaoId)
                        .onEach { bolao -> _uiState.update { it.copy(bolao = bolao) } }
                val participantsFlow = participantIdsFlow(bolaoFlow)

                userId.filter { it.isNotBlank() }
                    .flatMapLatest { currentUserId ->
                        bolaoFlow.flatMapLatest { bolao ->
                            val championshipId = bolao.championshipId
                            combine(
                                flowOf(bolao),
                                matchRepository.getMatches(championshipId),
                                predictionRepository.getUserPredictions(currentUserId, bolaoId),
                                predictionRepository.getBolaoAllPredictions(bolaoId),
                                rankingFlow(bolaoId, championshipId, participantsFlow)
                            ) { b, matches, predictions, allPredictions, ranking ->
                                _uiState.update {
                                    it.copy(
                                        matches = filterBolaoMatches(b, matches),
                                        allMatches = matches,
                                        userPredictions = predictions.associateBy { p -> p.matchId },
                                        allPredictions = allPredictions,
                                        participants = ranking,
                                        isLoading = false
                                    )
                                }
                            }
                        }
                    }
                    .catch { e ->
                        if (e is CancellationException) throw e
                        val message = errorReporter.reportAndClassify(e, "Erro ao observar dados do bolão $bolaoId")
                        _uiState.update { it.copy(error = message, isLoading = false) }
                    }
                    .collect { }
            }
    }

    fun setUserId(id: String) {
        if (id.isNotBlank()) {
            userId.value = id
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    fun updateMatchScore(matchId: String, home: Int?, away: Int?) {
        val championshipId = _uiState.value.bolao?.championshipId ?: return
        viewModelScope.launch {
            try {
                performanceMonitor.trace(PerformanceTraces.BOLAO_ADMIN_UPDATE_SCORE) {
                    matchRepository.updateMatchScore(championshipId, matchId, home, away)
                }
                analyticsTracker.logEvent(AnalyticsEvents.BOLAO_ADMIN_UPDATE_SCORE, mapOf("bolao_id" to bolaoId, "match_id" to matchId))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorReporter.report(e, "Erro ao atualizar placar manual")
                _uiState.update { it.copy(error = "Você não tem permissão para alterar placares oficiais.") }
            }
        }
    }

    fun approveParticipant(userId: String, approve: Boolean) {
        viewModelScope.launch {
            try {
                performanceMonitor.trace(PerformanceTraces.BOLAO_APPROVE_JOIN) {
                    bolaoRepository.approveJoinRequest(bolaoId, userId, approve)
                }
                analyticsTracker.logEvent(
                    AnalyticsEvents.BOLAO_APPROVE_JOIN,
                    mapOf("bolao_id" to bolaoId, "approved" to approve)
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = errorReporter.reportAndClassify(e, "Erro ao aprovar participante")
                _uiState.update { it.copy(error = message) }
            }
        }
    }

    fun approveLeaveRequest(userId: String, approve: Boolean) {
        viewModelScope.launch {
            try {
                performanceMonitor.trace(PerformanceTraces.BOLAO_APPROVE_LEAVE) {
                    bolaoRepository.approveLeaveRequest(bolaoId, userId, approve)
                }
                analyticsTracker.logEvent(
                    AnalyticsEvents.BOLAO_APPROVE_LEAVE,
                    mapOf("bolao_id" to bolaoId, "approved" to approve)
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = errorReporter.reportAndClassify(e, "Erro ao aprovar saída")
                _uiState.update { it.copy(error = message) }
            }
        }
    }

    fun leaveBolao() {
        val currentUserId = userId.value
        if (currentUserId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val bolao = _uiState.value.bolao
                performanceMonitor.trace(PerformanceTraces.BOLAO_LEAVE) {
                    if (bolao?.ownerId == currentUserId) {
                        bolaoRepository.leaveBolao(bolaoId, currentUserId)
                    } else {
                        bolaoRepository.requestLeaveBolao(bolaoId, currentUserId)
                    }
                }
                analyticsTracker.logEvent(AnalyticsEvents.BOLAO_LEAVE, mapOf("bolao_id" to bolaoId))
                _uiState.update { it.copy(isLeaveSuccess = true, isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = errorReporter.reportAndClassify(e, "Erro ao sair do bolão")
                _uiState.update { it.copy(error = message, isLoading = false) }
            }
        }
    }
}
