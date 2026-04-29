package com.pga.magiccollection.domain.usecase.card

import com.pga.magiccollection.data.remote.dto.ScryfallCardDto
import com.pga.magiccollection.data.repository.CardSearchIndexRepository
import javax.inject.Inject

class IngestRemoteCardsUseCase @Inject constructor(
    private val cardSearchIndexRepository: CardSearchIndexRepository
) {
    suspend operator fun invoke(cards: List<ScryfallCardDto>, language: String) {
        cardSearchIndexRepository.ingestRemoteCards(cards, language)
    }
}
