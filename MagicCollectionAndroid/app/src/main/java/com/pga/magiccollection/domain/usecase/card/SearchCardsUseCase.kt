package com.pga.magiccollection.domain.usecase.card

import com.pga.magiccollection.data.remote.dto.ScryfallCardDto
import com.pga.magiccollection.data.repository.CardRepository
import javax.inject.Inject

class SearchCardsUseCase @Inject constructor(
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(
        query: String?,
        colors: String? = null,
        colorIdentity: Boolean = false,
        colorLogic: String? = null,
        type: String? = null,
        text: String? = null,
        manaCost: String? = null,
        set: String? = null,
        rarity: String? = null,
        artist: String? = null,
        lang: String? = null
    ): List<ScryfallCardDto> {
        return cardRepository.discoverCards(
            query, colors, colorIdentity, colorLogic, type, text, manaCost, set, rarity, artist, lang
        )
    }
}
