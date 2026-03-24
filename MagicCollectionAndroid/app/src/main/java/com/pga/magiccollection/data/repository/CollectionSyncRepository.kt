package com.pga.magiccollection.data.repository

import com.pga.magiccollection.data.local.dao.CollectionDao
import com.pga.magiccollection.data.local.entities.CollectionEntity
import com.pga.magiccollection.data.remote.api.CollectionsApi
import com.pga.magiccollection.data.remote.dto.CollectionRequestDto
import kotlinx.coroutines.flow.Flow

class CollectionSyncRepository(
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

    suspend fun syncPendingCollections(userId: Long): Int {
        val pending = collectionDao.getUnsyncedCollectionsByUserId(userId)
        var syncedCount = 0
        for (localCollection in pending) {
            val remote = collectionsApi.createCollection(
                CollectionRequestDto(name = localCollection.name)
            )
            collectionDao.markAsSynced(localCollection.localId, remote.id)
            syncedCount += 1
        }
        return syncedCount
    }
}

