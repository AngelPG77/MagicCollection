package com.pga.magiccollection.domain.usecase.collection

import com.pga.magiccollection.data.repository.CollectionRepository
import javax.inject.Inject

class CheckCollectionNameExistsUseCase @Inject constructor(
    private val collectionRepository: CollectionRepository
) {
    suspend operator fun invoke(name: String, userId: Long): Boolean {
        return collectionRepository.existsByName(name.trim(), userId)
    }
}
