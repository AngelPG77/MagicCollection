package com.pga.magiccollection.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pga.magiccollection.data.local.entities.CollectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CollectionEntity): Long

    @Query("SELECT * FROM collections WHERE localId = :localId")
    suspend fun getCollectionById(localId: Long): CollectionEntity?

    @Query("SELECT * FROM collections WHERE localId = :localId")
    fun getCollectionByIdAsFlow(localId: Long): Flow<CollectionEntity?>

    @Query("SELECT * FROM collections WHERE userId = :userId")
    fun getCollectionsByUserId(userId: Long): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collections WHERE userId = :userId")
    suspend fun getCollectionsByUserIdSync(userId: Long): List<CollectionEntity>

    @Query("SELECT * FROM collections WHERE name = :name AND userId = :userId")
    suspend fun getCollectionByNameAndUserId(name: String, userId: Long): CollectionEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM collections WHERE name = :name AND userId = :userId)")
    suspend fun existsByNameAndUserId(name: String, userId: Long): Boolean

    @Query("SELECT * FROM collections WHERE localId = :localId")
    fun observeCollection(localId: Long): Flow<CollectionEntity?>

    @Query("SELECT * FROM collections WHERE userId = :userId ORDER BY localId DESC")
    fun observeCollectionsByUserId(userId: Long): Flow<List<CollectionEntity>>

    @Update
    suspend fun updateCollection(collection: CollectionEntity): Int

    @Delete
    suspend fun deleteCollection(collection: CollectionEntity): Int

    @Query("DELETE FROM collections WHERE localId = :localId")
    suspend fun deleteCollectionById(localId: Long): Int

    @Query("UPDATE collections SET synced = 1, remoteId = :remoteId WHERE localId = :localId")
    suspend fun markAsSynced(localId: Long, remoteId: Long): Int

    @Query("UPDATE collections SET synced = 0 WHERE localId = :localId")
    suspend fun markAsNotSynced(localId: Long): Int

    @Query("SELECT * FROM collections WHERE synced = 0")
    suspend fun getUnsyncedCollections(): List<CollectionEntity>

    @Query("SELECT * FROM collections WHERE synced = 0 AND userId = :userId")
    suspend fun getUnsyncedCollectionsByUserId(userId: Long): List<CollectionEntity>

    @Query("SELECT COUNT(*) FROM collections WHERE userId = :userId")
    suspend fun countCollectionsByUserId(userId: Long): Int

    // Statistics operations
    @Query("""
        SELECT COALESCE(SUM(quantity), 0) FROM cards_owned 
        WHERE collectionId = :collectionId
    """)
    suspend fun getTotalCardsQuantity(collectionId: Long): Int

    @Query("""
        SELECT COUNT(DISTINCT scryfallId) FROM cards_owned 
        WHERE collectionId = :collectionId
    """)
    suspend fun getUniqueCardsCount(collectionId: Long): Int
}
