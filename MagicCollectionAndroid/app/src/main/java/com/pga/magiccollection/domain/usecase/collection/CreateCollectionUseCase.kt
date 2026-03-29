package com.pga.magiccollection.domain.usecase.collection

import com.pga.magiccollection.data.repository.CollectionRepository
import com.pga.magiccollection.data.local.entities.CollectionEntity
import javax.inject.Inject

class CreateCollectionUseCase @Inject constructor(
    private val collectionRepository: CollectionRepository
) {
    suspend operator fun invoke(name: String, userId: Long): Long {
        return collectionRepository.createLocalCollection(name, userId)
    }
}
