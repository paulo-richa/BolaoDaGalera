package com.lpstudio.bolaodagalera.data.firebase

import com.lpstudio.bolaodagalera.domain.model.Championship
import com.lpstudio.bolaodagalera.domain.repository.ChampionshipRepository
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.appLogger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class FirebaseChampionshipRepository(private val crashReporter: CrashReporter) : ChampionshipRepository {
    private val logger = appLogger("FirebaseChampionshipRepository")
    private val db = Firebase.firestore
    private val collection = db.collection("championships")

    override fun getChampionships(): Flow<List<Championship>> = try {
        collection
            .snapshots
            .map { snap ->
                snap.documents.map { doc ->
                    doc.data<Championship>().copy(id = doc.id)
                }
            }
            .onEach { list ->
                Championship.setCache(list)
            }
            .catch { e ->
                crashReporter.recordException(e, "Erro ao observar campeonatos")
                logger.e(e) { "Erro ao observar campeonatos" }
                emit(emptyList<Championship>())
            }
    } catch (e: Exception) {
        crashReporter.recordException(e, "Erro crítico ao observar campeonatos")
        logger.e(e) { "Erro crítico ao observar campeonatos" }
        kotlinx.coroutines.flow.flowOf(emptyList<Championship>())
    }

    override suspend fun refreshCache() {
        try {
            val snap = collection.get()
            val list =
                snap.documents.map { doc ->
                    doc.data<Championship>().copy(id = doc.id)
                }
            Championship.setCache(list)
        } catch (e: Exception) {
            crashReporter.recordException(e, "Erro ao carregar campeonatos")
            logger.e(e) { "Erro ao carregar campeonatos" }
        }
    }
}
