package com.lpstudio.bolaodagalera.data.firebase

import com.lpstudio.bolaodagalera.domain.model.Championship
import com.lpstudio.bolaodagalera.domain.repository.ChampionshipRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class FirebaseChampionshipRepository : ChampionshipRepository {
    private val db = Firebase.firestore
    private val collection = db.collection("championships")

    override fun getChampionships(): Flow<List<Championship>> = collection
        .snapshots
        .map { snap ->
            snap.documents.map { doc ->
                doc.data<Championship>().copy(id = doc.id)
            }
        }
        .onEach { list ->
            Championship.setCache(list)
        }

    override suspend fun refreshCache() {
        try {
            val snap = collection.get()
            val list = snap.documents.map { doc ->
                doc.data<Championship>().copy(id = doc.id)
            }
            Championship.setCache(list)
        } catch (e: Exception) {
            println("BOLAOLOG: Erro ao carregar campeonatos: ${e.message}")
        }
    }
}
