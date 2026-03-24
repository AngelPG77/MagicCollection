package com.pga.magiccollection.domain.usecase

import com.pga.magiccollection.data.remote.dto.ScryfallCardDto
import com.pga.magiccollection.data.repository.CardSearchRepository

class SearchCardsUseCase(
    private val cardSearchRepository: CardSearchRepository
) {
    suspend operator fun invoke(query: String): List<ScryfallCardDto> {
        return cardSearchRepository.discoverCards(query)
    }
}

