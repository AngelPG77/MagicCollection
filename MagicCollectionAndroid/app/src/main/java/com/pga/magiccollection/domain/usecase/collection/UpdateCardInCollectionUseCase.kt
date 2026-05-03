package com.pga.magiccollection.domain.usecase.collection

import com.pga.magiccollection.data.repository.CollectionRepository
import javax.inject.Inject

class UpdateCardInCollectionUseCase @Inject constructor(
    private val collectionRepository: CollectionRepository
) {
    suspend operator fun invoke(
        collectionLocalId: Long,
        cardLocalId: Long,
        quantity: Int,
        foil: Boolean,
        language: String,
        condition: String
    ) {
        collectionRepository.updateCardInCollection(
            collectionLocalId = collectionLocalId,
            cardLocalId = cardLocalId,
            quantity = quantity,
            foil = foil,
            language = language,
            condition = condition
        )
    }
}
