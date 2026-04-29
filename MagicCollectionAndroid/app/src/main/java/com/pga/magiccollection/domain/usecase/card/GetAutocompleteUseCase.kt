package com.pga.magiccollection.domain.usecase.card

import com.pga.magiccollection.data.remote.dto.CardSuggestionDto
import com.pga.magiccollection.data.repository.CardRepository
import javax.inject.Inject

class GetAutocompleteUseCase @Inject constructor(
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(query: String): List<CardSuggestionDto> {
        return cardRepository.getAutocomplete(query)
    }
}
