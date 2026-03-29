package com.pga.magiccollection.domain.usecase.inventory

import com.pga.magiccollection.data.local.entities.CardOwnedEntity
import com.pga.magiccollection.data.repository.InventoryRepository
import javax.inject.Inject

class DeleteCardUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository
) {
    suspend operator fun invoke(card: CardOwnedEntity) {
        inventoryRepository.deleteCard(card)
    }
}
