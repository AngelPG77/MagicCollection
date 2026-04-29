package com.pga.magiccollection.data.repository

import com.pga.magiccollection.data.local.dao.CardOwnedDao
import com.pga.magiccollection.data.local.dao.CollectionDao
import com.pga.magiccollection.data.local.entities.CardOwnedEntity
import com.pga.magiccollection.data.local.entities.CollectionEntity
import com.pga.magiccollection.data.remote.api.CollectionsApi
import com.pga.magiccollection.data.remote.api.InventoryApi
import com.pga.magiccollection.data.remote.dto.CardYouOwnRequestDto
import com.pga.magiccollection.data.remote.dto.CollectionRequestDto
import kotlinx.coroutines.flow.Flow

class CollectionRepository(
    private val collectionDao: CollectionDao,
    private val cardOwnedDao: CardOwnedDao,
    private val collectionsApi: CollectionsApi,
    private val inventoryApi: InventoryApi
) {
    fun observeCollections(userId: Long): Flow<List<CollectionEntity>> {
        return collectionDao.observeCollectionsByUserId(userId)
    }

    suspend fun createLocalCollection(name: String, userId: Long): Long {
        val normalized = name.trim()
        require(normalized.isNotEmpty()) { "El nombre de la coleccion es obligatorio." }
        val localId = collectionDao.insertCollection(
            CollectionEntity(
                name = normalized,
                userId = userId,
                synced = false
            )
        )

        // Intentar sincronizar inmediatamente
        try {
            val remote = collectionsApi.createCollection(CollectionRequestDto(name = normalized))
            collectionDao.markAsSynced(localId, remote.id)
        } catch (e: Exception) {
            // Queda como no sincronizada
        }

        return localId
    }

    suspend fun updateCollection(localId: Long, newName: String) {
        val collection = collectionDao.getCollectionById(localId) ?: return
        collection.remoteId?.let { 
            collectionsApi.updateCollection(it, CollectionRequestDto(newName))
            collectionDao.updateCollection(collection.copy(name = newName, synced = true))
        } ?: run {
            collectionDao.updateCollection(collection.copy(name = newName, synced = false))
        }
    }

    suspend fun deleteCollection(localId: Long) {
        val collection = collectionDao.getCollectionById(localId) ?: return
        
        if (collection.remoteId == null) {
            collectionDao.deleteCollectionById(localId)
        } else {
            collectionDao.markForDeletion(localId)
            try {
                collectionsApi.deleteCollection(collection.remoteId)
                collectionDao.deleteCollectionById(localId)
            } catch (e: Exception) {
                // Keep marked for next sync
            }
        }
    }

    suspend fun syncAll(userId: Long) {
        // 0. Procesar borrados locales
        processPendingDeletions(userId)

        // 1. Sincronizar colecciones pendientes de subir
        val pendingCollections = collectionDao.getUnsyncedCollectionsByUserId(userId)
        for (col in pendingCollections) {
            try {
                val remote = collectionsApi.createCollection(CollectionRequestDto(name = col.name))
                collectionDao.markAsSynced(col.localId, remote.id)
            } catch (e: Exception) {
            }
        }

        // 2. Descargar colecciones y Reconciliación
        val remoteCollections = collectionsApi.getCollections()
        val remoteIds = remoteCollections.map { it.id }.toSet()

        // Borrar localmente lo que ya no existe en el servidor
        val localCollections = collectionDao.getCollectionsByUserIdSync(userId)
        for (local in localCollections) {
            if (local.remoteId != null && !remoteIds.contains(local.remoteId)) {
                collectionDao.deleteCollectionById(local.localId)
            }
        }

        for (remote in remoteCollections) {
            val local = collectionDao.getCollectionByNameAndUserId(remote.name, userId)
            if (local == null) {
                collectionDao.insertCollection(
                    CollectionEntity(
                        remoteId = remote.id,
                        name = remote.name,
                        userId = userId,
                        synced = true
                    )
                )
            } else if (local.remoteId == null) {
                collectionDao.markAsSynced(local.localId, remote.id)
            }
        }

        // 3. Sincronizar cartas pendientes de subir
        val pendingCards = cardOwnedDao.getUnsyncedCardsByUserId(userId)
        for (card in pendingCards) {
            try {
                val collection = collectionDao.getCollectionById(card.collectionId)
                collection?.remoteId?.let { remoteColId ->
                    val remoteCard = inventoryApi.addCard(
                        CardYouOwnRequestDto(
                            collectionId = remoteColId,
                            cardName = "",
                            quantity = card.quantity,
                            condition = card.condition,
                            isFoil = card.isFoil,
                            language = card.language,
                            scryfallId = card.scryfallId
                        )
                    )
                    // Marcar como sincronizada y actualizar remoteId
                    cardOwnedDao.updateCardOwned(card.copy(remoteId = remoteCard.id, synced = true))
                }
            } catch (e: Exception) {
            }
        }

        // 4. Descargar cartas y Reconciliar para cada colección
        val allCollections = collectionDao.getCollectionsByUserIdSync(userId)
        for (col in allCollections) {
            col.remoteId?.let { remoteId ->
                try {
                    val remoteCards = inventoryApi.getCardsByCollection(remoteId)
                    val remoteCardIds = remoteCards.map { it.id }.toSet()

                    // Reconciliación de cartas
                    // Nota: Necesitaríamos un getCardsByCollectionIdSync en CardOwnedDao para hacerlo eficiente
                    // Por ahora, solo descargaremos y actualizaremos (Upsert)

                    for (rc in remoteCards) {
                        val existing = cardOwnedDao.getExactCard(
                            scryfallId = rc.scryfallId,
                            collectionId = col.localId,
                            language = rc.language,
                            condition = rc.condition,
                            isFoil = rc.isFoil
                        )
                        if (existing == null) {
                            cardOwnedDao.insertCardOwned(
                                CardOwnedEntity(
                                    scryfallId = rc.scryfallId,
                                    collectionId = col.localId,
                                    remoteId = rc.id,
                                    quantity = rc.quantity,
                                    isFoil = rc.isFoil,
                                    condition = rc.condition,
                                    language = rc.language,
                                    synced = true
                                )
                            )
                        } else {
                            // UPSERT
                            cardOwnedDao.updateCardOwned(existing.copy(
                                remoteId = rc.id,
                                quantity = rc.quantity,
                                synced = true
                            ))
                        }
                    }
                } catch (e: Exception) {
                }
            }
        }
    }

    private suspend fun processPendingDeletions(userId: Long) {
        val pendingCollections = collectionDao.getPendingDeletions(userId)
        for (col in pendingCollections) {
            try {
                col.remoteId?.let { collectionsApi.deleteCollection(it) }
                collectionDao.deleteCollectionById(col.localId)
            } catch (e: Exception) {}
        }
    }
}
