package com.pga.magiccollection.data.repository

import com.pga.magiccollection.data.remote.api.CardsApi
import com.pga.magiccollection.data.repository.CardSearchIndexRepository
import com.pga.magiccollection.data.remote.dto.CardSuggestionDto
import com.pga.magiccollection.data.remote.dto.ScryfallCardDto

class CardRepository(
    private val cardsApi: CardsApi,
    private val cardSearchIndexRepository: CardSearchIndexRepository
) {
    suspend fun discoverCards(
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
        val cards = cardsApi.searchCards(
            query = query?.trim()?.ifEmpty { null },
            colors = colors,
            colorIdentity = colorIdentity,
            colorLogic = colorLogic,
            type = type?.trim()?.ifEmpty { null },
            text = text?.trim()?.ifEmpty { null },
            manaCost = manaCost?.trim()?.ifEmpty { null },
            set = set?.trim()?.ifEmpty { null },
            rarity = rarity,
            artist = artist?.trim()?.ifEmpty { null },
            lang = lang
        )
        // Sincronizamos con el índice local para que estén disponibles sin conexión
        cardSearchIndexRepository.ingestRemoteCards(cards, lang ?: "en")
        return cards
    }

    suspend fun getAllKnownCards(): List<ScryfallCardDto> {
        val cards = cardsApi.getAllKnownCards()
        cardSearchIndexRepository.ingestRemoteCards(cards, "en")
        return cards
    }

    suspend fun getCardById(id: Long): ScryfallCardDto {
        val card = cardsApi.getCardById(id)
        cardSearchIndexRepository.ingestRemoteCards(listOf(card), "en")
        return card
    }

    suspend fun getCardByName(name: String, lang: String? = null): ScryfallCardDto {
        val card = cardsApi.getCardByName(name, lang)
        cardSearchIndexRepository.ingestRemoteCards(listOf(card), lang ?: "en")
        return card
    }

    suspend fun getCardByScryfallId(id: String, lang: String? = null): ScryfallCardDto {
        val card = cardsApi.getCardByScryfallId(id, lang)
        cardSearchIndexRepository.ingestRemoteCards(listOf(card), lang ?: "en")
        return card
    }

    suspend fun getRandomCard(): ScryfallCardDto {
        return cardsApi.getRandomCard()
    }

    suspend fun getAutocomplete(query: String): List<CardSuggestionDto> {
        return if (query.length < 3) emptyList() else cardsApi.getAutocomplete(query)
    }
}
