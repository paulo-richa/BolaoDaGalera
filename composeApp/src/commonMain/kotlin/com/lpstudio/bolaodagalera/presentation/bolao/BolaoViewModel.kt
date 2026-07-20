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
                
                // O sync agora é feito via Backend (Cloud Functions).
                // O App apenas exibe os dados que o servidor popula.
                println("BOLAOLOG: Bolão carregado (${bolao.name}). Aguardando sync do servidor...")
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeMatchesPredictionsAndRanking() {
        dataCollectionJob = viewModelScope.launch {
            val bolaoFlow = bolaoRepository.getBolaoFlow(bolaoId)
                .onEach { bolao -> _uiState.update { it.copy(bolao = bolao) } }

            val participantsFlow = bolaoFlow
                .onEach { bolao ->
                    val pendingJoin = authRepository.getUsers(bolao.pendingParticipants)
                    val pendingExit = authRepository.getUsers(bolao.pendingExits)
                    _uiState.update { it.copy(
                        pendingJoinUsers = pendingJoin,
                        pendingExitUsers = pendingExit
                    ) }
                }
                .map { it.participants }
                .distinctUntilChanged()
            
            _userId.filter { it.isNotBlank() }
                .flatMapLatest { currentUserId ->
                    combine(
                        bolaoFlow,
                        matchRepository.getMatches(),
                        predictionRepository.getUserPredictions(currentUserId, bolaoId),
                        predictionRepository.getBolaoAllPredictions(bolaoId),
                        participantsFlow.flatMapLatest { participants ->
                            predictionRepository.getRanking(bolaoId, participants)
                        }
                    ) { bolao, matches, predictions, allPredictions, ranking ->
                        val championshipId = bolao.championshipId
                        
                        // 1. Filtrar por Campeonato e Escopo
                        var filteredMatches = matches.filter { m ->
                            val matchesChamp = m.championshipId == championshipId
                        val matchesScope = when {
                            bolao.specificMatchId != null -> m.id == bolao.specificMatchId
                            bolao.scope == BolaoScope.ONLY_GROUPS -> m.phase == Phase.GROUP_STAGE
                            bolao.scope == BolaoScope.ONLY_KNOCKOUT -> m.phase != Phase.GROUP_STAGE
                            else -> true
                        }
                        matchesChamp && matchesScope
                    }
                    
                    println("BOLAOLOG: Filtered matches for $championshipId: ${filteredMatches.size} (Total: ${matches.size})")
                    if (filteredMatches.isEmpty() && matches.isNotEmpty()) {
                        matches.take(5).forEach { 
                            println("BOLAOLOG: Sample Match: ID=${it.id} Champ=${it.championshipId} Phase=${it.phase}")
                        }
                    }

                        // TRATAMENTO DE DUPLICADOS/GHOSTS: Mesmo usando IDs padronizados,
                        // podem existir documentos antigos com IDs diferentes no Firestore.
                        // Agrupamos por Times e Rodada apenas para a Fase de Grupos.
                        // Para Mata-Mata, usamos o ID único para evitar que jogos TBD sejam colapsados.
                        filteredMatches = filteredMatches.groupBy { 
                            if (it.phase == Phase.GROUP_STAGE) "${it.homeTeamCode}-${it.awayTeamCode}-${it.groupRound()}"
                            else it.id 
                        }
                            .map { (_, matchGroup) ->
                                matchGroup.maxByOrNull { 
                                    if (it.status == "FINISHED") 3 
                                    else if (it.homeScore != null) 2 
                                    else if (it.id.contains("-")) 1 // Prefere IDs padronizados se tudo for null
                                    else 0 
                                }!!
                            }

                        // 2. Filtro de Rodada de Corte (Apenas para campeonatos baseados em pontos/rodadas)
                        val championship = Championship.fromId(championshipId)
                        if (championship.isPointsBased) {
                            val matchesByRound = filteredMatches.groupBy { it.groupRound() }
                            
                            // Encontramos a maior rodada onde a maioria dos jogos (>50%) já terminou ou começou
                            // antes da criação do bolão. Isso define o que é "passado".
                            val lastMostlyFinishedRound = matchesByRound.keys
                                .filter { round ->
                                    val roundMatches = matchesByRound[round] ?: emptyList()
                                    val finishedCount = roundMatches.count { it.matchDateMillis < bolao.createdAtMillis }
                                    finishedCount > (roundMatches.size / 2)
                                }
                                .maxOrNull() ?: 0
                            
                            // A rodada de exibição inicial será a próxima após a última rodada "passada".
                            val startFromRound = lastMostlyFinishedRound + 1
                            
                            // Filtramos TUDO que for de rodada anterior numericamente.
                            filteredMatches = filteredMatches.filter { it.groupRound() >= startFromRound }
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
                }.collect()
        }
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

    fun syncMatchesWithApi(forcedChampionshipId: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val championshipId = forcedChampionshipId ?: _uiState.value.bolao?.championshipId ?: "LIBERTADORES"
                println("BOLAOLOG: Iniciando sincronização ativa para $championshipId...")
                
                val currentMatches = _uiState.value.allMatches
                val remoteRepo = com.lpstudio.bolaodagalera.data.remote.FootballDataMatchRepository()
                val remoteMatches = remoteRepo.getUpdatedMatches(currentMatches, championshipId)

                if (remoteMatches.isEmpty()) {
                    println("BOLAOLOG: API não retornou jogos para $championshipId.")
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }

                println("BOLAOLOG: API retornou ${remoteMatches.size} jogos. Iniciando escrita no Firestore...")

                var updatedCount = 0
                
                // Lógica de Upsert direto para campeonatos ativos (Brasileirão, Libertadores, etc)
                remoteMatches.forEach { remoteMatch ->
                    matchRepository.upsertMatch(remoteMatch)
                    updatedCount++
                }
                
                _uiState.update { it.copy(
                    isLoading = false, 
                    error = "Sincronização finalizada. $updatedCount registros processados."
                ) }
            } catch (e: Exception) {
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
