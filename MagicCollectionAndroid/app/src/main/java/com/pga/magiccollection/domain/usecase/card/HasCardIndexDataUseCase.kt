package com.pga.magiccollection.domain.usecase.card

import com.pga.magiccollection.data.repository.CardSearchIndexRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HasCardIndexDataUseCase @Inject constructor(
    private val cardSearchIndexRepository: CardSearchIndexRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return cardSearchIndexRepository.hasIndexData()
    }
}
