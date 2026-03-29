package com.pga.magiccollection.domain.usecase.inventory

import com.pga.magiccollection.data.local.entities.CardOwnedEntity
import com.pga.magiccollection.data.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCardsInCollectionUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository
) {
    operator fun invoke(collectionId: Long): Flow<List<CardOwnedEntity>> {
        return inventoryRepository.observeCardsInCollection(collectionId)
    }

    suspend fun fetch(collectionId: Long) {
        inventoryRepository.fetchCardsInCollection(collectionId)
    }
}
