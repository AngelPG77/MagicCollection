package com.pga.magiccollection.domain.usecase.card

import com.pga.magiccollection.data.remote.dto.ScryfallCardDto
import com.pga.magiccollection.data.repository.CardRepository
import javax.inject.Inject

class GetCardByIdUseCase @Inject constructor(
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(id: Long): ScryfallCardDto {
        return cardRepository.getCardById(id)
    }
}
