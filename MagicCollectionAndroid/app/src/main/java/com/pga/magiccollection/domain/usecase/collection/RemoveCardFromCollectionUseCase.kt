package com.pga.magiccollection.domain.usecase.collection

import com.pga.magiccollection.data.repository.CollectionRepository
import javax.inject.Inject

class RemoveCardFromCollectionUseCase @Inject constructor(
    private val collectionRepository: CollectionRepository
) {
    suspend operator fun invoke(collectionLocalId: Long, cardLocalId: Long) {
        collectionRepository.removeCardFromCollection(collectionLocalId, cardLocalId)
    }
}
