package com.pga.magiccollection.domain.usecase.inventory

import com.pga.magiccollection.data.repository.InventoryRepository
import javax.inject.Inject

class AddCardUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository
) {
    suspend operator fun invoke(
        collectionId: Long,
        cardName: String,
        quantity: Int,
        condition: String,
        isFoil: Boolean,
        language: String
    ) {
        inventoryRepository.addCard(collectionId, cardName, quantity, condition, isFoil, language)
    }
}
