package com.pga.magiccollection.domain.usecase

import com.pga.magiccollection.data.repository.CollectionSyncRepository

class SyncCollectionsUseCase(
    private val collectionSyncRepository: CollectionSyncRepository
) {
    suspend operator fun invoke(userId: Long): Int {
        return collectionSyncRepository.syncPendingCollections(userId)
    }
}

