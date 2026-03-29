package com.pga.magiccollection.domain.usecase.collection

import com.pga.magiccollection.data.repository.CollectionRepository
import javax.inject.Inject

class SyncCollectionsUseCase @Inject constructor(
    private val collectionRepository: CollectionRepository
) {
    suspend operator fun invoke(userId: Long): Int {
        // Primero descargamos lo que haya en el servidor (Pull)
        collectionRepository.fetchRemoteCollections(userId)
        // Luego subimos lo que tengamos pendiente localmente (Push)
        return collectionRepository.syncPendingCollections(userId)
    }
}
