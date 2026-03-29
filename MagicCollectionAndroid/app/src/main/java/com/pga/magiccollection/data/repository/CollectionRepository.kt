package com.pga.magiccollection.data.repository

import com.pga.magiccollection.data.local.dao.CollectionDao
import com.pga.magiccollection.data.local.entities.CollectionEntity
import com.pga.magiccollection.data.remote.api.CollectionsApi
import com.pga.magiccollection.data.remote.dto.CollectionRequestDto
import kotlinx.coroutines.flow.Flow

class CollectionRepository(
    private val collectionDao: CollectionDao,
    private val collectionsApi: CollectionsApi
) {
    fun observeCollections(userId: Long): Flow<List<CollectionEntity>> {
        return collectionDao.observeCollectionsByUserId(userId)
    }

    suspend fun createLocalCollection(name: String, userId: Long): Long {
        val normalized = name.trim()
        require(normalized.isNotEmpty()) { "El nombre de la coleccion es obligatorio." }
        return collectionDao.insertCollection(
            CollectionEntity(
                name = normalized,
                userId = userId,
                synced = false
            )
        )
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
        collection.remoteId?.let { collectionsApi.deleteCollection(it) }
        collectionDao.deleteCollectionById(localId)
    }

    suspend fun fetchRemoteCollections(userId: Long) {
        val remoteCollections = collectionsApi.getCollections()
        for (remote in remoteCollections) {
            val exists = collectionDao.getCollectionByNameAndUserId(remote.name, userId)
            if (exists == null) {
                collectionDao.insertCollection(
                    CollectionEntity(
                        remoteId = remote.id,
                        name = remote.name,
                        userId = userId,
                        synced = true
                    )
                )
            } else if (exists.remoteId == null) {
                collectionDao.markAsSynced(exists.localId, remote.id)
            }
        }
    }

    suspend fun syncPendingCollections(userId: Long): Int {
        val pending = collectionDao.getUnsyncedCollectionsByUserId(userId)
        var syncedCount = 0
        for (localCollection in pending) {
            try {
                val remote = collectionsApi.createCollection(
                    CollectionRequestDto(name = localCollection.name)
                )
                collectionDao.markAsSynced(localCollection.localId, remote.id)
                syncedCount += 1
            } catch (e: Exception) {
                // Log error
            }
        }
        return syncedCount
    }
}
