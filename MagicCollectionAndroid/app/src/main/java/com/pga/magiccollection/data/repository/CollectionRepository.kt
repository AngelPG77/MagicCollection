package com.pga.magiccollection.data.repository

import com.pga.magiccollection.data.local.dao.CollectionCardDao
import com.pga.magiccollection.data.local.dao.CollectionDao
import com.pga.magiccollection.data.local.dao.CollectionWithCount
import com.pga.magiccollection.data.local.entities.CollectionCardEntity
import com.pga.magiccollection.data.local.entities.CollectionEntity
import com.pga.magiccollection.data.remote.api.CollectionsApi
import com.pga.magiccollection.data.remote.dto.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CollectionRepository @Inject constructor(
    private val collectionDao: CollectionDao,
    private val collectionCardDao: CollectionCardDao,
    private val collectionsApi: CollectionsApi
) {
    fun observeCollections(userId: Long): Flow<List<CollectionWithCount>> {
        return collectionDao.observeByUserIdWithCount(userId)
    }

    fun observeGlobalCardCount(userId: Long): Flow<Int> {
        return collectionCardDao.observeAllUserCards(userId).map { cards ->
            cards.sumOf { it.quantity }
        }
    }

    fun observeCollectionCards(collectionLocalId: Long): Flow<List<CollectionCardEntity>> {
        return collectionCardDao.observeByCollectionId(collectionLocalId)
    }

    fun observeAllUserCards(userId: Long): Flow<List<CollectionCardEntity>> {
        return collectionCardDao.observeAllUserCards(userId)
    }

    suspend fun getCollectionById(localId: Long): CollectionEntity? {
        return collectionDao.getById(localId)
    }

    suspend fun createCollection(name: String, userId: Long): CollectionEntity {
        val entity = CollectionEntity(name = name, userId = userId, synced = false)
        val localId = collectionDao.insert(entity)
        
        return try {
            val response = collectionsApi.createCollection(CollectionRequestDto(name))
            collectionDao.markAsSynced(localId, response.id)
            entity.copy(localId = localId, remoteId = response.id, synced = true)
        } catch (e: Exception) {
            entity.copy(localId = localId)
        }
    }

    suspend fun updateCollection(localId: Long, newName: String) {
        val entity = collectionDao.getById(localId) ?: return
        val updated = entity.copy(name = newName, synced = false)
        collectionDao.update(updated)

        try {
            entity.remoteId?.let { remoteId ->
                collectionsApi.updateCollection(remoteId, CollectionRequestDto(newName))
                collectionDao.update(updated.copy(synced = true))
            }
        } catch (e: Exception) {
        }
    }

    suspend fun deleteCollection(localId: Long) {
        val entity = collectionDao.getById(localId) ?: return
        
        if (entity.remoteId == null) {
            collectionDao.deleteById(localId)
        } else {
            collectionDao.markForDeletion(localId)
            try {
                collectionsApi.deleteCollection(entity.remoteId)
                collectionDao.deleteById(localId)
            } catch (e: Exception) {
            }
        }
    }

    suspend fun addCardToCollection(
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
        val collection = collectionDao.getById(collectionLocalId) ?: return
        
        val existingCard = collectionCardDao.getExactCard(
            collectionLocalId = collectionLocalId,
            scryfallId = scryfallId,
            foil = foil,
            language = language,
            condition = condition
        )
        
        val localCardId = if (existingCard != null) {
            val updated = existingCard.copy(quantity = existingCard.quantity + quantity, synced = false)
            collectionCardDao.update(updated)
            existingCard.localId
        } else {
            val cardEntity = CollectionCardEntity(
                collectionLocalId = collectionLocalId,
                scryfallId = scryfallId,
                name = name,
                typeLine = typeLine,
                manaCost = manaCost,
                imageUrl = imageUrl,
                quantity = quantity,
                foil = foil,
                language = language,
                condition = condition,
                synced = false
            )
            collectionCardDao.insert(cardEntity)
        }
        
        try {
            collection.remoteId?.let { remoteId ->
                val serverCardId = collectionsApi.addCardToCollection(
                    remoteId,
                    AddCardToCollectionRequest(scryfallId, name, typeLine, manaCost, imageUrl, quantity, foil, language, condition)
                )
                collectionCardDao.markAsSynced(localCardId, serverCardId)
            }
        } catch (e: Exception) {
        }
    }

    suspend fun removeCardFromCollection(collectionLocalId: Long, cardLocalId: Long) {
        val card = collectionCardDao.getById(cardLocalId) ?: return
        val collection = collectionDao.getById(collectionLocalId)

        if (card.remoteId == null) {
            collectionCardDao.deleteById(cardLocalId)
        } else {
            collectionCardDao.markForDeletion(cardLocalId)
            try {
                if (collection?.remoteId != null) {
                    collectionsApi.removeCardFromCollection(collection.remoteId, card.remoteId)
                    collectionCardDao.deleteById(cardLocalId)
                }
            } catch (e: Exception) {
            }
        }
    }

    suspend fun updateCardInCollection(
        collectionLocalId: Long,
        cardLocalId: Long,
        quantity: Int,
        foil: Boolean,
        language: String,
        condition: String
    ) {
        val card = collectionCardDao.getById(cardLocalId) ?: return
        val collection = collectionDao.getById(collectionLocalId)
        
        val updated = card.copy(
            quantity = quantity,
            foil = foil,
            language = language,
            condition = condition,
            synced = false
        )
        collectionCardDao.update(updated)

        try {
            if (collection?.remoteId != null && card.remoteId != null) {
                collectionsApi.updateCardInCollection(
                    collection.remoteId,
                    card.remoteId,
                    UpdateCardInCollectionRequest(quantity, foil, language, condition)
                )
                collectionCardDao.update(updated.copy(synced = true))
            }
        } catch (e: Exception) {
        }
    }

    suspend fun syncCollections(userId: Long): Int {
        processPendingDeletions(userId)
        pushPendingCollections(userId)
        pushPendingCards(userId)

        return try {
            val remoteCollections = collectionsApi.getCollections()
            val remoteIds = remoteCollections.map { it.id }.toSet()

            val localLists = collectionDao.getByUserId(userId)
            for (local in localLists) {
                if (local.remoteId != null && !remoteIds.contains(local.remoteId)) {
                    collectionDao.deleteById(local.localId)
                }
            }

            for (remote in remoteCollections) {
                var localCol = collectionDao.getByRemoteId(remote.id)
                if (localCol == null) {
                    localCol = collectionDao.getByNameAndUserId(remote.name, userId)
                }

                val localId = if (localCol == null) {
                    collectionDao.insert(
                        CollectionEntity(
                            remoteId = remote.id,
                            name = remote.name,
                            userId = userId,
                            synced = true
                        )
                    )
                } else {
                    val updated = localCol.copy(remoteId = remote.id, name = remote.name, synced = true)
                    collectionDao.update(updated)
                    localCol.localId
                }
                
                // Detailed sync if cards are present in DTO
                remote.cards?.let { remoteCards ->
                    val remoteCardIds = remoteCards.map { it.id }.toSet()
                    val localCards = collectionCardDao.getByCollectionId(localId)
                    for (lc in localCards) {
                        if (lc.remoteId != null && !remoteCardIds.contains(lc.remoteId)) {
                            collectionCardDao.deleteById(lc.localId)
                        }
                    }

                    for (card in remoteCards) {
                        val existingCard = collectionCardDao.getExactCard(
                            collectionLocalId = localId,
                            scryfallId = card.scryfallId,
                            foil = card.foil,
                            language = card.language,
                            condition = card.condition
                        )

                        if (existingCard == null) {
                            collectionCardDao.insert(
                                CollectionCardEntity(
                                    remoteId = card.id,
                                    collectionLocalId = localId,
                                    scryfallId = card.scryfallId,
                                    name = card.name,
                                    typeLine = card.typeLine,
                                    manaCost = card.manaCost,
                                    imageUrl = card.imageUrl,
                                    quantity = card.quantity,
                                    foil = card.foil,
                                    language = card.language,
                                    condition = card.condition,
                                    synced = true
                                )
                            )
                        } else if (!existingCard.synced) {
                            // CLIENT-WINS: local has unsynced changes (push must have failed).
                            // Preserve user's quantity, only attach the remoteId so the next
                            // push knows it's an UPDATE rather than a CREATE.
                            if (existingCard.remoteId != card.id) {
                                collectionCardDao.update(existingCard.copy(remoteId = card.id))
                            }
                        } else {
                            collectionCardDao.update(existingCard.copy(
                                remoteId = card.id,
                                quantity = card.quantity,
                                synced = true
                            ))
                        }
                    }
                }
            }
            
            remoteCollections.size
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun processPendingDeletions(userId: Long) {
        val pendingLists = collectionDao.getPendingDeletions(userId)
        for (list in pendingLists) {
            try {
                list.remoteId?.let { collectionsApi.deleteCollection(it) }
                collectionDao.deleteById(list.localId)
            } catch (e: Exception) {}
        }

        val myLists = collectionDao.getByUserId(userId)
        for (list in myLists) {
            val pendingCards = collectionCardDao.getPendingDeletions(list.localId)
            for (card in pendingCards) {
                try {
                    if (list.remoteId != null && card.remoteId != null) {
                        collectionsApi.removeCardFromCollection(list.remoteId, card.remoteId)
                        collectionCardDao.deleteById(card.localId)
                    }
                } catch (e: Exception) {}
            }
        }
    }

    private suspend fun pushPendingCollections(userId: Long) {
        val pending = collectionDao.getUnsyncedCollectionsByUserId(userId)
        for (item in pending) {
            try {
                val response = collectionsApi.createCollection(CollectionRequestDto(item.name))
                collectionDao.markAsSynced(item.localId, response.id)
            } catch (e: Exception) {
            }
        }
    }

    private suspend fun pushPendingCards(userId: Long) {
        val pendingCards = collectionCardDao.getUnsyncedCardsByUserId(userId)
        for (card in pendingCards) {
            try {
                val col = collectionDao.getById(card.collectionLocalId)
                col?.remoteId?.let { remoteId ->
                    if (card.remoteId != null && card.remoteId != 0L) {
                        collectionsApi.updateCardInCollection(
                            remoteId,
                            card.remoteId,
                            UpdateCardInCollectionRequest(
                                card.quantity, card.foil, card.language, card.condition
                            )
                        )
                    } else {
                        val serverCardId = collectionsApi.addCardToCollection(
                            remoteId,
                            AddCardToCollectionRequest(
                                card.scryfallId, card.name, card.typeLine, card.manaCost, 
                                card.imageUrl, card.quantity, card.foil, card.language, card.condition
                            )
                        )
                        collectionCardDao.markAsSynced(card.localId, serverCardId)
                        return@let
                    }
                    collectionCardDao.markAsSynced(card.localId, card.remoteId ?: 0L)
                }
            } catch (e: Exception) { }
        }
    }

    suspend fun existsByName(name: String, userId: Long): Boolean {
        return collectionDao.existsByNameAndUserId(name, userId)
    }
}
