package com.lpstudio.bolaodagalera.data.firebase

import com.lpstudio.bolaodagalera.domain.model.Banner
import com.lpstudio.bolaodagalera.domain.repository.BannerRepository
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.appLogger
import com.lpstudio.bolaodagalera.observability.reportAndRethrow
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class FirebaseBannerRepository(private val crashReporter: CrashReporter) : BannerRepository {
    private val logger = appLogger("FirebaseBannerRepository")
    private val db = Firebase.firestore
    private val collection = db.collection("banners")

    // isActive is filtered client-side rather than added to the query to
    // avoid requiring a Firestore composite index for order+isActive.
    override fun getBanners(): Flow<List<Banner>> = try {
        collection
            .orderBy("order")
            .snapshots
            .map { snap ->
                snap.documents
                    .map { doc -> doc.data<Banner>().copy(id = doc.id) }
                    .filter { it.isActive }
            }
            .reportAndRethrow(crashReporter, "Erro ao observar banners")
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        crashReporter.recordException(e, "Erro crítico ao observar banners")
        logger.e(e) { "Erro crítico ao observar banners" }
        flow { throw e }
    }
}
