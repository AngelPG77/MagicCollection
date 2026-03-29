package com.pga.magiccollection.domain.usecase.collection

import com.pga.magiccollection.data.local.entities.CollectionEntity
import com.pga.magiccollection.data.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCollectionsUseCase @Inject constructor(
    private val collectionRepository: CollectionRepository
) {
    operator fun invoke(userId: Long): Flow<List<CollectionEntity>> {
        return collectionRepository.observeCollections(userId)
    }
}
