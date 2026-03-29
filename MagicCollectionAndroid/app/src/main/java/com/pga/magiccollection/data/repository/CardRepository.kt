package com.pga.magiccollection.data.repository

import com.pga.magiccollection.data.remote.api.CardsApi
import com.pga.magiccollection.data.remote.dto.ScryfallCardDto

class CardRepository(
    private val cardsApi: CardsApi
) {
    suspend fun discoverCards(query: String): List<ScryfallCardDto> {
        val normalized = query.trim()
        require(normalized.isNotEmpty()) { "El termino de busqueda es obligatorio." }
        return cardsApi.searchCards(normalized)
    }

    suspend fun getAllKnownCards(): List<ScryfallCardDto> {
        return cardsApi.getAllKnownCards()
    }

    suspend fun getCardById(id: Long): ScryfallCardDto {
        return cardsApi.getCardById(id)
    }

    suspend fun getCardByName(name: String): ScryfallCardDto {
        return cardsApi.getCardByName(name)
    }
}
