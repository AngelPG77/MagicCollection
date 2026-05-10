package com.pga.magiccollection.data.repository

import com.pga.magiccollection.data.local.dao.WantListCardDao
import com.pga.magiccollection.data.local.dao.WantListDao
import com.pga.magiccollection.data.local.entities.WantListCardEntity
import com.pga.magiccollection.data.local.entities.WantListEntity
import com.pga.magiccollection.data.remote.api.WantListApi
import com.pga.magiccollection.data.remote.dto.AddCardToWantListRequest
import com.pga.magiccollection.data.remote.dto.UpdateCardInWantListRequest
import com.pga.magiccollection.data.remote.dto.CreateWantListRequest
import com.pga.magiccollection.data.remote.dto.UpdateWantListRequest
import com.pga.magiccollection.data.remote.dto.WantListDto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WantListRepository @Inject constructor(
    private val wantListDao: WantListDao,
    private val wantListCardDao: WantListCardDao,
    private val wantListApi: WantListApi
) {
    fun observeWantLists(userId: Long): Flow<List<com.pga.magiccollection.data.local.dao.WantListWithCount>> {
        return wantListDao.observeByUserIdWithCount(userId)
    }

    fun observeWantListCards(wantListLocalId: Long): Flow<List<WantListCardEntity>> {
        return wantListCardDao.observeByWantListId(wantListLocalId)
    }

    suspend fun getWantListById(localId: Long): WantListEntity? {
        return wantListDao.getById(localId)
    }

    suspend fun createWantList(name: String, userId: Long): WantListEntity {
        // Create locally first
        val entity = WantListEntity(name = name, userId = userId, synced = false)
        val localId = wantListDao.insert(entity)
        
        return try {
            // Try to sync with server
            val response = wantListApi.createWantList(CreateWantListRequest(name))
            wantListDao.markAsSynced(localId, response.id)
            entity.copy(localId = localId, remoteId = response.id, synced = true)
        } catch (e: Exception) {
            entity.copy(localId = localId)
        }
    }

    suspend fun updateWantList(localId: Long, newName: String) {
        val entity = wantListDao.getById(localId) ?: return
        val updated = entity.copy(name = newName, synced = false)
        wantListDao.update(updated)

        try {
            entity.remoteId?.let { remoteId ->
                wantListApi.updateWantList(remoteId, UpdateWantListRequest(newName))
                wantListDao.update(updated.copy(synced = true))
            }
        } catch (e: Exception) {
            // Keep unsynced
        }
    }

    suspend fun deleteWantList(localId: Long) {
        val entity = wantListDao.getById(localId) ?: return
        
        if (entity.remoteId == null) {
            // If never synced, just delete locally
            wantListDao.deleteById(localId)
        } else {
            // Mark for deletion and try to sync
            wantListDao.markForDeletion(localId)
            try {
                wantListApi.deleteWantList(entity.remoteId)
                wantListDao.deleteById(localId)
            } catch (e: Exception) {
                // Keep marked as pendingDelete for next sync
            }
        }
    }

    suspend fun addCardToWantList(
        wantListLocalId: Long,
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
        val wantList = wantListDao.getById(wantListLocalId) ?: return
        
        // Check if card already exists
        val existingCard = wantListCardDao.getExactCard(
            wantListLocalId = wantListLocalId,
            scryfallId = scryfallId,
            foil = foil,
            language = language,
            condition = condition
        )
        
        val localCardId = if (existingCard != null) {
            // Update quantity locally
            val updated = existingCard.copy(quantity = existingCard.quantity + quantity, synced = false)
            wantListCardDao.update(updated)
            existingCard.localId
        } else {
            // Insert new locally
            val cardEntity = WantListCardEntity(
                wantListLocalId = wantListLocalId,
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
            wantListCardDao.insert(cardEntity)
        }
        
        // Try to sync with server if we have a remoteId for the list
        try {
            wantList.remoteId?.let { remoteId ->
                val serverCardId = wantListApi.addCardToWantList(
                    remoteId,
                    AddCardToWantListRequest(scryfallId, name, typeLine, manaCost, imageUrl, quantity, foil, language, condition)
                )
                // If success, mark as synced with the real remoteId
                wantListCardDao.markAsSynced(localCardId, serverCardId)
            }
        } catch (e: Exception) {
            // Stay unsynced, will be pushed later by syncWantLists/pushPendingCards
        }
    }

    suspend fun removeCardFromWantList(wantListLocalId: Long, cardLocalId: Long) {
        val card = wantListCardDao.getById(cardLocalId) ?: return
        val wantList = wantListDao.getById(wantListLocalId)

        if (card.remoteId == null) {
            wantListCardDao.deleteById(cardLocalId)
        } else {
            wantListCardDao.markForDeletion(cardLocalId)
            try {
                if (wantList?.remoteId != null) {
                    wantListApi.removeCardFromWantList(wantList.remoteId, card.remoteId)
                    wantListCardDao.deleteById(cardLocalId)
                }
            } catch (e: Exception) {
                // Keep pending
            }
        }
    }

    suspend fun updateCardInWantList(
        localId: Long,
        quantity: Int,
        foil: Boolean,
        language: String,
        condition: String
    ) {
        val card = wantListCardDao.getById(localId) ?: return
        val updated = card.copy(
            quantity = quantity,
            foil = foil,
            language = language,
            condition = condition,
            synced = false
        )
        wantListCardDao.update(updated)
    }

    suspend fun syncWantLists(userId: Long): Int {
        // 1. Process local deletions (Soft Deletes)
        processPendingDeletions(userId)

        // 2. Subir cambios locales (Push)
        pushPendingWantLists(userId)
        pushPendingCards(userId)

        return try {
            val remoteWantLists = wantListApi.getMyWantLists()
            val remoteIds = remoteWantLists.map { it.id }.toSet()

            // 3. Local Reconciliation (Borrados en servidor -> Borrar local)
            val localLists = wantListDao.getByUserId(userId)
            for (local in localLists) {
                if (local.remoteId != null && !remoteIds.contains(local.remoteId)) {
                    wantListDao.deleteById(local.localId)
                }
            }

            // 4. Pull and Upsert
            for (remote in remoteWantLists) {
                // Buscar si ya existe localmente por remoteId o por nombre
                var localList = wantListDao.getByRemoteId(remote.id)
                if (localList == null) {
                    localList = wantListDao.getByNameAndUserId(remote.name, userId)
                }

                val localId = if (localList == null) {
                    // Si no existe, insertar
                    wantListDao.insert(
                        WantListEntity(
                            remoteId = remote.id,
                            name = remote.name,
                            userId = userId,
                            synced = true
                        )
                    )
                } else {
                    // Si existe, actualizar remoteId y marcar como sincronizada
                    val updated = localList.copy(remoteId = remote.id, name = remote.name, synced = true)
                    wantListDao.update(updated)
                    localList.localId
                }
                
                // Reconcile cards in this list
                val remoteCardIds = remote.cards.map { it.id }.toSet()
                val localCards = wantListCardDao.getByWantListId(localId)
                for (localCard in localCards) {
                    if (localCard.remoteId != null && !remoteCardIds.contains(localCard.remoteId)) {
                        wantListCardDao.deleteById(localCard.localId)
                    }
                }

                // Sincronizar cartas de esta lista
                for (card in remote.cards) {
                    val existingCard = wantListCardDao.getExactCard(
                        wantListLocalId = localId,
                        scryfallId = card.scryfallId,
                        foil = card.foil,
                        language = card.language,
                        condition = card.condition
                    )
                    
                    if (existingCard == null) {
                        wantListCardDao.insert(
                            WantListCardEntity(
                                remoteId = card.id,
                                wantListLocalId = localId,
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
                        // CLIENT-WINS: local tiene cambios pendientes (push debió fallar).
                        // Conservamos la quantity local, solo enlazamos el remoteId para que
                        // el siguiente push sea un UPDATE en vez de un CREATE.
                        if (existingCard.remoteId != card.id) {
                            wantListCardDao.update(existingCard.copy(remoteId = card.id))
                        }
                    } else {
                        // Carta ya sincronizada: aceptamos los datos del servidor.
                        val updated = existingCard.copy(
                            remoteId = card.id,
                            quantity = card.quantity,
                            synced = true
                        )
                        wantListCardDao.update(updated)
                    }
                }
            }
            
            remoteWantLists.size
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun processPendingDeletions(userId: Long) {
        val pendingLists = wantListDao.getPendingDeletions(userId)
        for (list in pendingLists) {
            try {
                list.remoteId?.let { wantListApi.deleteWantList(it) }
                wantListDao.deleteById(list.localId)
            } catch (e: Exception) {}
        }

        val myLists = wantListDao.getByUserId(userId)
        for (list in myLists) {
            val pendingCards = wantListCardDao.getPendingDeletions(list.localId)
            for (card in pendingCards) {
                try {
                    if (list.remoteId != null && card.remoteId != null) {
                        wantListApi.removeCardFromWantList(list.remoteId, card.remoteId)
                        wantListCardDao.deleteById(card.localId)
                    }
                } catch (e: Exception) {}
            }
        }
    }

    private suspend fun pushPendingWantLists(userId: Long) {
        val pendingLists = wantListDao.getUnsyncedWantLists(userId)
        for (list in pendingLists) {
            try {
                val response = wantListApi.createWantList(CreateWantListRequest(list.name))
                wantListDao.markAsSynced(list.localId, response.id)
            } catch (e: Exception) {
                // Ignorar error individual
            }
        }
    }

    private suspend fun pushPendingCards(userId: Long) {
        val pendingCards = wantListCardDao.getUnsyncedCardsByUserId(userId)
        for (card in pendingCards) {
            try {
                val wantList = wantListDao.getById(card.wantListLocalId)
                wantList?.remoteId?.let { remoteId ->
                    if (card.remoteId != null && card.remoteId != 0L) {
                        // UPDATE
                        wantListApi.updateCardInWantList(
                            remoteId,
                            card.remoteId,
                            UpdateCardInWantListRequest(
                                card.quantity, card.foil, card.language, card.condition
                            )
                        )
                    } else {
                        // ADD
                        wantListApi.addCardToWantList(
                            remoteId,
                            AddCardToWantListRequest(
                                card.scryfallId, card.name, card.typeLine, card.manaCost, 
                                card.imageUrl, card.quantity, card.foil, card.language, card.condition
                            )
                        )
                    }
                    wantListCardDao.markAsSynced(card.localId, card.remoteId ?: 0L)
                }
            } catch (e: Exception) { }
        }
    }

    suspend fun existsByName(name: String, userId: Long): Boolean {
        return wantListDao.existsByNameAndUserId(name, userId)
    }
}
