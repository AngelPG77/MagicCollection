package com.pga.magiccollection.domain.usecase.wantlist

import com.pga.magiccollection.data.repository.WantListRepository
import javax.inject.Inject

class AddCardToWantListUseCase @Inject constructor(
    private val wantListRepository: WantListRepository
) {
    suspend operator fun invoke(
        wantListLocalId: Long,
        scryfallId: String,
        name: String,
        typeLine: String?,
        manaCost: String?,
        imageUrl: String?,
        quantity: Int = 1,
        foil: Boolean = false,
        language: String = "en",
        condition: String = "NEAR_MINT"
    ) {
        wantListRepository.addCardToWantList(
            wantListLocalId, scryfallId, name, typeLine, manaCost, imageUrl, quantity, foil, language, condition
        )
    }
}
