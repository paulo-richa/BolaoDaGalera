package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpstudio.bolaodagalera.domain.model.*
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import com.lpstudio.bolaodagalera.domain.repository.PredictionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BolaoUiState(
    val bolao: Bolao? = null,
    val matches: List<Match> = emptyList(),
    val allMatches: List<Match> = emptyList(), // Lista completa sem filtros
    val userPredictions: Map<String, Prediction> = emptyMap(), // matchId -> prediction
    val participants: List<RankingEntry> = emptyList(),
    val pendingJoinUsers: List<User> = emptyList(),
    val pendingExitUsers: List<User> = emptyList(),
    val allPredictions: List<Prediction> = emptyList(),
    val isLoading: Boolean = true,
    val isLeaveSuccess: Boolean = false,
    val error: String? = null,
)

class BolaoViewModel(
    private val bolaoRepository: BolaoRepository,
    private val matchRepository: MatchRepository,
    private val predictionRepository: PredictionRepository,
    private val authRepository: AuthRepository,
    private val bolaoId: String,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BolaoUiState())
    val uiState: StateFlow<BolaoUiState> = _uiState.asStateFlow()

    private val _userId = MutableStateFlow(authRepository.currentUser?.id ?: "")
    private var dataCollectionJob: kotlinx.coroutines.Job? = null

    init {
        authRepository.authStateFlow.onEach { user ->
            dataCollectionJob?.cancel()

            if (user == null) {
                _uiState.update { BolaoUiState(isLoading = false) }
            } else {
                _userId.value = user.id
                loadBolao()
                observeMatchesPredictionsAndRanking()
            }
        }.launchIn(viewModelScope)
    }

    private fun loadBolao() {
        viewModelScope.launch {
            try {
                val bolao = bolaoRepository.getBolao(bolaoId)
                _uiState.update { it.copy(bolao = bolao) }
                println("BOLAOLOG: Bolão carregado (${bolao.name}).")
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeMatchesPredictionsAndRanking() {
        dataCollectionJob =
            viewModelScope.launch {
                val bolaoFlow =
                    bolaoRepository.getBolaoFlow(bolaoId)
                        .onEach { bolao -> _uiState.update { it.copy(bolao = bolao) } }

                val participantsFlow =
                    bolaoFlow
                        .onEach { bolao ->
                            val pendingJoin = authRepository.getUsers(bolao.pendingParticipants)
                            val pendingExit = authRepository.getUsers(bolao.pendingExits)
                            _uiState.update {
                                it.copy(
                                    pendingJoinUsers = pendingJoin,
                                    pendingExitUsers = pendingExit,
                                )
                            }
                        }
                        .map { it.participants }
                        .distinctUntilChanged()

                _userId.filter { it.isNotBlank() }
                    .flatMapLatest { currentUserId ->
                        bolaoFlow.flatMapLatest { bolao ->
                            val championshipId = bolao.championshipId

                            combine(
                                flowOf(bolao),
                                matchRepository.getMatches(championshipId),
                                predictionRepository.getUserPredictions(currentUserId, bolaoId),
                                predictionRepository.getBolaoAllPredictions(bolaoId),
                                participantsFlow.flatMapLatest { participants ->
                                    combine(
                                        predictionRepository.getRanking(bolaoId, championshipId, participants),
                                        flow { emit(authRepository.getUsers(participants)) },
                                    ) { ranking, users ->
                                        val userMap = users.associateBy { it.id }
                                        ranking.map { entry ->
                                            val user = userMap[entry.userId]
                                            if (user != null && (entry.userName.isBlank() || entry.userName == "Novo Participante" || entry.userName == "Usuário")) {
                                                entry.copy(
                                                    userName = user.name,
                                                    userNickname = user.nickname.ifBlank { user.username },
                                                )
                                            } else {
                                                entry
                                            }
                                        }
                                    }
                                },
                            ) { b, matches, predictions, allPredictions, ranking ->
                                // 1. Filtrar por Escopo
                                var filteredMatches =
                                    matches.filter { m ->
                                        when {
                                            b.specificMatchId != null -> m.id == b.specificMatchId
                                            b.scope == BolaoScope.ONLY_GROUPS -> m.phase == Phase.GROUP_STAGE
                                            b.scope == BolaoScope.ONLY_KNOCKOUT -> m.phase != Phase.GROUP_STAGE
                                            else -> true
                                        }
                                    }

                                // INJEÇÃO LOCAL (LIBERTADORES): Removida para priorizar dados reais do Firestore

                                // TRATAMENTO DE DUPLICADOS/GHOSTS
                                filteredMatches =
                                    filteredMatches.groupBy {
                                        if (it.phase == Phase.GROUP_STAGE) {
                                            "${it.homeTeamCode}-${it.awayTeamCode}-${it.groupRound()}"
                                        } else {
                                            it.id
                                        }
                                    }
                                        .map { (_, matchGroup) ->
                                            matchGroup.maxByOrNull {
                                                if (it.status == "FINISHED") {
                                                    3
                                                } else if (it.homeScore != null) {
                                                    2
                                                } else if (it.id.contains("-")) {
                                                    1
                                                } else {
                                                    0
                                                }
                                            }!!
                                        }

                                // 2. Filtro de Rodada de Corte
                                val championship = Championship.fromId(championshipId)
                                if (championship.isPointsBased) {
                                    val matchesByRound = filteredMatches.groupBy { it.groupRound() }
                                    val lastMostlyFinishedRound =
                                        matchesByRound.keys
                                            .filter { round ->
                                                val roundMatches = matchesByRound[round] ?: emptyList()
                                                val finishedCount = roundMatches.count { it.matchDateMillis < b.createdAtMillis }
                                                finishedCount > (roundMatches.size / 2)
                                            }
                                            .maxOrNull() ?: 0

                                    val startFromRound = lastMostlyFinishedRound + 1
                                    filteredMatches = filteredMatches.filter { it.groupRound() >= startFromRound }
                                }

                                val predictionMap = predictions.associateBy { it.matchId }
                                _uiState.update {
                                    it.copy(
                                        matches = filteredMatches,
                                        allMatches = matches,
                                        userPredictions = predictionMap,
                                        allPredictions = allPredictions,
                                        participants = ranking,
                                        isLoading = false,
                                    )
                                }
                            }
                        }
                    }
                    .catch { e ->
                        println("BOLAOLOG: Erro no observeMatchesPredictionsAndRanking: ${e.message}")
                        _uiState.update { it.copy(error = "Erro ao carregar dados do bolão.", isLoading = false) }
                    }
                    .collect { }
            }
    }

    fun setUserId(id: String) {
        if (id.isNotBlank()) {
            _userId.value = id
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    fun updateMatchScore(
        matchId: String,
        home: Int?,
        away: Int?,
    ) {
        val championshipId = _uiState.value.bolao?.championshipId ?: return
        viewModelScope.launch {
            try {
                matchRepository.updateMatchScore(championshipId, matchId, home, away)
            } catch (e: Exception) {
                println("BOLAOLOG: Erro ao atualizar placar manual: ${e.message}")
                _uiState.update { it.copy(error = "Você não tem permissão para alterar placares oficiais.") }
            }
        }
    }

    fun approveParticipant(
        userId: String,
        approve: Boolean,
    ) {
        viewModelScope.launch {
            try {
                bolaoRepository.approveJoinRequest(bolaoId, userId, approve)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun approveLeaveRequest(
        userId: String,
        approve: Boolean,
    ) {
        viewModelScope.launch {
            try {
                bolaoRepository.approveLeaveRequest(bolaoId, userId, approve)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun syncMatchesWithApi() {
        // Implementação do sync (placeholder para acionamento via app se necessário)
        loadBolao()
    }

    fun leaveBolao() {
        val currentUserId = _userId.value
        if (currentUserId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val bolao = _uiState.value.bolao
                if (bolao?.ownerId == currentUserId) {
                    bolaoRepository.leaveBolao(bolaoId, currentUserId)
                } else {
                    bolaoRepository.requestLeaveBolao(bolaoId, currentUserId)
                }
                _uiState.update { it.copy(isLeaveSuccess = true, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
