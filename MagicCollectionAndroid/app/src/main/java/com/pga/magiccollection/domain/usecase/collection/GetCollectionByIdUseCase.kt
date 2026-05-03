package com.pga.magiccollection.domain.usecase.collection

import com.pga.magiccollection.data.local.entities.CollectionEntity
import com.pga.magiccollection.data.repository.CollectionRepository
import javax.inject.Inject

class GetCollectionByIdUseCase @Inject constructor(
    private val collectionRepository: CollectionRepository
) {
    suspend operator fun invoke(localId: Long): CollectionEntity? {
        return collectionRepository.getCollectionById(localId)
    }
}
