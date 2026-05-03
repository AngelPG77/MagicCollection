package com.pga.magiccollection.domain.usecase.collection

import com.pga.magiccollection.data.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveGlobalCardCountUseCase @Inject constructor(
    private val collectionRepository: CollectionRepository
) {
    operator fun invoke(userId: Long): Flow<Int> {
        return collectionRepository.observeGlobalCardCount(userId)
    }
}
