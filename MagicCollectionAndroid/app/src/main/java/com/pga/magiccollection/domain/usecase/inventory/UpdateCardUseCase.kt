package com.pga.magiccollection.domain.usecase.inventory

import com.pga.magiccollection.data.repository.InventoryRepository
import javax.inject.Inject

class UpdateCardUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository
) {
    suspend operator fun invoke(
        remoteId: Long,
        quantity: Int,
        condition: String,
        isFoil: Boolean,
        language: String
    ) {
        inventoryRepository.updateCard(remoteId, quantity, condition, isFoil, language)
    }
}
