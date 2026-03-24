package com.pga.magiccollection.data.repository

import com.pga.magiccollection.data.remote.api.CardsApi
import com.pga.magiccollection.data.remote.dto.ScryfallCardDto

class CardSearchRepository(
    private val cardsApi: CardsApi
) {
    suspend fun discoverCards(query: String): List<ScryfallCardDto> {
        val normalized = query.trim()
        require(normalized.isNotEmpty()) { "La busqueda no puede estar vacia." }
        return cardsApi.discoverCards(normalized)
    }
}

