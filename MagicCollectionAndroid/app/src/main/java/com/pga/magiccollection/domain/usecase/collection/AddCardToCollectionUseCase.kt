package com.pga.magiccollection.domain.usecase.collection

import com.pga.magiccollection.data.repository.CollectionRepository
import javax.inject.Inject

class AddCardToCollectionUseCase @Inject constructor(
    private val collectionRepository: CollectionRepository
) {
    suspend operator fun invoke(
        collectionLocalId: Long,
        scryfallId: String,
        name: String,
        typeLine: String?,
        manaCost: String?,
        imageUrl: String?,
        quantity: Int,
        foil: Boolean,
        language: String,
        condition: String
    ) {
        collectionRepository.addCardToCollection(
            collectionLocalId = collectionLocalId,
            scryfallId = scryfallId,
            name = name,
            typeLine = typeLine,
            manaCost = manaCost,
            imageUrl = imageUrl,
            quantity = quantity,
            foil = foil,
            language = language,
            condition = condition
        )
    }
}
