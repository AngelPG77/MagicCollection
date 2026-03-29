package com.pga.magiccollection.domain.usecase.collection

import com.pga.magiccollection.data.repository.CollectionRepository
import javax.inject.Inject

class UpdateCollectionUseCase @Inject constructor(
    private val collectionRepository: CollectionRepository
) {
    suspend operator fun invoke(localId: Long, newName: String) {
        collectionRepository.updateCollection(localId, newName)
    }
}
