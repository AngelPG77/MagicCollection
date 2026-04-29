package com.pga.magiccollection.domain.usecase.card

import com.pga.magiccollection.data.repository.CardSearchIndexRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BootstrapCardIndexUseCase @Inject constructor(
    private val cardSearchIndexRepository: CardSearchIndexRepository
) {
    suspend operator fun invoke(language: String): Int {
        return cardSearchIndexRepository.bootstrapIndex(language)
    }

    fun hasIndexData(): Flow<Boolean> = cardSearchIndexRepository.hasIndexData()
}
