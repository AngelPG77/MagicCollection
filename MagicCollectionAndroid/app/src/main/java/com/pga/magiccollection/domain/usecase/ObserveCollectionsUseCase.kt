package com.pga.magiccollection.domain.usecase

import com.pga.magiccollection.data.local.entities.CollectionEntity
import com.pga.magiccollection.data.repository.CollectionSyncRepository
import kotlinx.coroutines.flow.Flow

class ObserveCollectionsUseCase(
    private val collectionSyncRepository: CollectionSyncRepository
) {
    operator fun invoke(userId: Long): Flow<List<CollectionEntity>> {
        return collectionSyncRepository.observeCollections(userId)
    }
}

