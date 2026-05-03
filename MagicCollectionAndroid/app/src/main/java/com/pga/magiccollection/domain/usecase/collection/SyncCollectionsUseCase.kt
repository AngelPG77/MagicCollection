package com.pga.magiccollection.domain.usecase.collection

import com.pga.magiccollection.data.repository.CollectionRepository
import javax.inject.Inject

class SyncCollectionsUseCase @Inject constructor(
    private val collectionRepository: CollectionRepository
) {
    suspend operator fun invoke(userId: Long) {
        collectionRepository.syncCollections(userId)
    }
}
