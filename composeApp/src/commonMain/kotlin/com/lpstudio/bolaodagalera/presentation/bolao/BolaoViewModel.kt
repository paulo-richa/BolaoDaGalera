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
    val pendingExitUsers: List<User> = emptyMap<String, User>().values.toList(), // apenas inicialização
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
    private val bolaoId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(BolaoUiState())
    val uiState: StateFlow<BolaoUiState> = _uiState.asStateFlow()

    private val _userId = MutableStateFlow(authRepository.currentUser?.id ?: "")

    init {
        loadBolao()
        observeMatchesPredictionsAndRanking()
    }

    private fun loadBolao() {
        viewModelScope.launch {
            try {
                val bolao = bolaoRepository.getBolao(bolaoId)
                _uiState.update { it.copy(bolao = bolao) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeMatchesPredictionsAndRanking() {
        // Observa o Bolão em tempo real para atualizar participantes e dados gerais
        val bolaoFlow = bolaoRepository.getBolaoFlow(bolaoId)
            .onEach { bolao -> _uiState.update { it.copy(bolao = bolao) } }

        val participantsFlow = bolaoFlow
            .onEach { bolao ->
                viewModelScope.launch {
                    val pendingJoin = authRepository.getUsers(bolao.pendingParticipants)
                    val pendingExit = authRepository.getUsers(bolao.pendingExits)
                    _uiState.update { it.copy(
                        pendingJoinUsers = pendingJoin,
                        pendingExitUsers = pendingExit
                    ) }
                }
            }
            .map { it.participants }
            .distinctUntilChanged()
        
        _userId.filter { it.isNotBlank() }
            .flatMapLatest { currentUserId ->
                combine(
                    matchRepository.getMatches(),
                    predictionRepository.getUserPredictions(currentUserId, bolaoId),
                    predictionRepository.getBolaoAllPredictions(bolaoId),
                    participantsFlow.flatMapLatest { participants ->
                        predictionRepository.getRanking(bolaoId, participants)
                    }
                ) { matches, predictions, allPredictions, ranking ->
                    val bolao = _uiState.value.bolao
                    val filteredMatches = when {
                        bolao?.specificMatchId != null -> matches.filter { it.id == bolao.specificMatchId }
                        bolao?.scope == BolaoScope.ONLY_GROUPS -> matches.filter { it.phase == Phase.GROUP_STAGE }
                        bolao?.scope == BolaoScope.ONLY_KNOCKOUT -> matches.filter { it.phase != Phase.GROUP_STAGE }
                        bolao?.scope == BolaoScope.ONLY_BRAZIL -> matches.filter { it.homeTeamCode == "BRA" || it.awayTeamCode == "BRA" }
                        else -> matches
                    }

                    val predictionMap = predictions.associateBy { it.matchId }
                    _uiState.update { it.copy(
                        matches = filteredMatches,
                        allMatches = matches,
                        userPredictions = predictionMap,
                        allPredictions = allPredictions,
                        participants = ranking,
                        isLoading = false
                    ) }
                }
            }.launchIn(viewModelScope)
    }

    fun setUserId(id: String) {
        if (id.isNotBlank()) {
            _userId.value = id
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    fun updateMatchScore(matchId: String, home: Int?, away: Int?) {
        viewModelScope.launch {
            matchRepository.updateMatchScore(matchId, home, away)
        }
    }

    fun syncKnockoutWithApi() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                println("BOLAOLOG: Iniciando sincronização ativa...")
                val currentMatches = _uiState.value.allMatches
                println("BOLAOLOG: Total de jogos locais: ${currentMatches.size}")

                val remoteRepo = com.lpstudio.bolaodagalera.data.remote.FootballDataMatchRepository()
                val remoteMatches = try {
                    remoteRepo.getUpdatedMatches(currentMatches)
                } catch (e: Exception) {
                    println("BOLAOLOG: Erro na API: ${e.message}")
                    throw Exception("Falha ao acessar API Football-Data: ${e.message}")
                }

                if (remoteMatches.isEmpty()) throw Exception("API Football-Data não retornou nenhum jogo.")

                var updatedCount = 0
                remoteMatches.forEach { remoteMatch ->
                    val localMatch = currentMatches.find { it.id == remoteMatch.id }
                    
                    if (localMatch != null) {
                        // 1. Atualizar Status e Data para TODOS os jogos
                        val needsStatusUpdate = remoteMatch.status != null && localMatch.status != remoteMatch.status
                        val needsDateUpdate = remoteMatch.matchDateMillis != 0L && localMatch.matchDateMillis != remoteMatch.matchDateMillis
                        
                        if (needsStatusUpdate || needsDateUpdate) {
                            matchRepository.updateMatchTeams(
                                matchId = localMatch.id,
                                homeTeam = localMatch.homeTeam,
                                homeTeamCode = localMatch.homeTeamCode,
                                homeTeamFlag = localMatch.homeTeamFlag,
                                awayTeam = localMatch.awayTeam,
                                awayTeamCode = localMatch.awayTeamCode,
                                awayTeamFlag = localMatch.awayTeamFlag,
                                dateMillis = if (needsDateUpdate) remoteMatch.matchDateMillis else null,
                                status = if (needsStatusUpdate) remoteMatch.status else null,
                                isManual = false
                            )
                            updatedCount++
                        }

                        // 2. Atualizar Times (Apenas Mata-Mata)
                        if (localMatch.phase != Phase.GROUP_STAGE) {
                            val needsTeamUpdate = remoteMatch.homeTeamCode != "TBD" && 
                                (localMatch.homeTeamCode != remoteMatch.homeTeamCode || localMatch.awayTeamCode != remoteMatch.awayTeamCode)
                            
                            if (needsTeamUpdate || localMatch.isManual) {
                                matchRepository.updateMatchTeams(
                                    matchId = localMatch.id,
                                    homeTeam = if (remoteMatch.homeTeamCode != "TBD") remoteMatch.homeTeam else localMatch.homeTeam,
                                    homeTeamCode = if (remoteMatch.homeTeamCode != "TBD") remoteMatch.homeTeamCode else localMatch.homeTeamCode,
                                    homeTeamFlag = if (remoteMatch.homeTeamCode != "TBD") remoteMatch.homeTeamFlag else localMatch.homeTeamFlag,
                                    awayTeam = if (remoteMatch.awayTeamCode != "TBD") remoteMatch.awayTeam else localMatch.awayTeam,
                                    awayTeamCode = if (remoteMatch.awayTeamCode != "TBD") remoteMatch.awayTeamCode else localMatch.awayTeamCode,
                                    awayTeamFlag = if (remoteMatch.awayTeamCode != "TBD") remoteMatch.awayTeamFlag else localMatch.awayTeamFlag,
                                    dateMillis = null,
                                    status = null,
                                    isManual = false
                                )
                                updatedCount++
                            }
                        }
                        
                        // 3. Atualizar Placar (Para todos)
                        if (localMatch.homeScore != remoteMatch.homeScore || localMatch.awayScore != remoteMatch.awayScore) {
                            matchRepository.updateMatchScore(
                                localMatch.id, 
                                remoteMatch.homeScore, 
                                remoteMatch.awayScore,
                                isManual = false
                            )
                            updatedCount++
                        }
                    }
                }
                
                println("BOLAOLOG: Sincronização finalizada. Atualizados: $updatedCount")
                _uiState.update { it.copy(
                    isLoading = false, 
                    error = if (updatedCount > 0) "Sucesso! $updatedCount jogos atualizados." 
                           else "Sincronização concluída (Nenhuma mudança detectada)"
                ) }
            } catch (e: Exception) {
                println("BOLAOLOG: Erro Fatal: ${e.message}")
                _uiState.update { it.copy(error = "Erro: ${e.message}", isLoading = false) }
            }
        }
    }

    fun approveParticipant(userId: String, approve: Boolean) {
        viewModelScope.launch {
            try {
                bolaoRepository.approveJoinRequest(bolaoId, userId, approve)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun approveLeaveRequest(userId: String, approve: Boolean) {
        viewModelScope.launch {
            try {
                bolaoRepository.approveLeaveRequest(bolaoId, userId, approve)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun leaveBolao() {
        val currentUserId = _userId.value
        if (currentUserId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val bolao = _uiState.value.bolao
                if (bolao?.ownerId == currentUserId) {
                    // Se for o dono, sai direto (ou deleta, dependendo da regra, mas vamos manter o leave direto por enquanto)
                    bolaoRepository.leaveBolao(bolaoId, currentUserId)
                } else {
                    // Se não for o dono, apenas solicita a saída
                    bolaoRepository.requestLeaveBolao(bolaoId, currentUserId)
                }
                _uiState.update { it.copy(isLeaveSuccess = true, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
