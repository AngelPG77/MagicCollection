package com.pga.magiccollection.data.repository

import com.pga.magiccollection.data.local.dao.CardOwnedDao
import com.pga.magiccollection.data.local.entities.CardOwnedEntity
import com.pga.magiccollection.data.remote.api.InventoryApi
import com.pga.magiccollection.data.remote.dto.CardYouOwnDto
import com.pga.magiccollection.data.remote.dto.CardYouOwnRequestDto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InventoryRepository @Inject constructor(
    private val cardOwnedDao: CardOwnedDao,
    private val inventoryApi: InventoryApi
) {
    fun observeCardsInCollection(collectionId: Long): Flow<List<CardOwnedEntity>> {
        return cardOwnedDao.observeCardsInCollection(collectionId)
    }

    suspend fun addCard(
        collectionId: Long,
        cardName: String,
        quantity: Int,
        condition: String,
        isFoil: Boolean,
        language: String
    ) {
        val remote = inventoryApi.addCard(
            CardYouOwnRequestDto(
                collectionId = collectionId,
                cardName = cardName,
                quantity = quantity,
                condition = condition,
                isFoil = isFoil,
                language = language
            )
        )
        
        cardOwnedDao.insertCardOwned(mapDtoToEntity(remote))
    }

    suspend fun updateCard(
        remoteId: Long,
        quantity: Int,
        condition: String,
        isFoil: Boolean,
        language: String
    ) {
        // Find local entity by remoteId
        // In a real app we'd have a DAO method for this
        // For now we'll call API and update local
        val remote = inventoryApi.updateCard(
            remoteId,
            CardYouOwnRequestDto(0, "", quantity, condition, isFoil, language)
        )
        cardOwnedDao.insertCardOwned(mapDtoToEntity(remote))
    }

    suspend fun fetchCardsInCollection(collectionId: Long) {
        val remoteCards = inventoryApi.getCardsByCollection(collectionId)
        cardOwnedDao.insertCardsOwned(remoteCards.map { mapDtoToEntity(it) })
    }

    suspend fun deleteCard(card: CardOwnedEntity) {
        card.remoteId?.let { inventoryApi.deleteCard(it) }
        cardOwnedDao.deleteCardOwned(card)
    }

    suspend fun searchGlobal(term: String): List<CardYouOwnDto> {
        return inventoryApi.searchGlobal(term)
    }

    suspend fun searchInCollection(collectionId: Long, term: String): List<CardYouOwnDto> {
        return inventoryApi.searchInCollection(collectionId, term)
    }

    private fun mapDtoToEntity(dto: CardYouOwnDto): CardOwnedEntity {
        return CardOwnedEntity(
            scryfallId = dto.scryfallId,
            collectionId = dto.collectionId,
            remoteId = dto.id,
            quantity = dto.quantity,
            isFoil = dto.isFoil,
            condition = dto.condition,
            language = dto.language,
            synced = true
        )
    }
}
