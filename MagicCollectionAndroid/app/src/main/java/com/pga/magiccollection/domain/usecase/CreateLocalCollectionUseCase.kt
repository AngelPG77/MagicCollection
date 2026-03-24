package com.pga.magiccollection.domain.usecase

import com.pga.magiccollection.data.repository.CollectionSyncRepository

class CreateLocalCollectionUseCase(
    private val collectionSyncRepository: CollectionSyncRepository
) {
    suspend operator fun invoke(name: String, userId: Long): Long {
        return collectionSyncRepository.createLocalCollection(name, userId)
    }
}

