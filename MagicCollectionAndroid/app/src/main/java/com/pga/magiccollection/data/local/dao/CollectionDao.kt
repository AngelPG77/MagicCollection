package com.pga.magiccollection.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pga.magiccollection.data.local.entities.CollectionEntity
import kotlinx.coroutines.flow.Flow

data class CollectionWithCount(
    val localId: Long,
    val remoteId: Long?,
    val name: String,
    val userId: Long,
    val synced: Boolean,
    val cardCount: Int
)

@Dao
interface CollectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(collection: CollectionEntity): Long

    @Query("""
        SELECT c.localId, c.remoteId, c.name, c.userId, c.synced, COALESCE(SUM(cc.quantity), 0) as cardCount 
        FROM collections c
        LEFT JOIN collection_cards cc ON c.localId = cc.collectionLocalId AND cc.pendingDelete = 0
        WHERE c.userId = :userId AND c.pendingDelete = 0
        GROUP BY c.localId
    """)
    fun observeByUserIdWithCount(userId: Long): Flow<List<CollectionWithCount>>
    @Query("SELECT * FROM collections WHERE localId = :localId")
    suspend fun getById(localId: Long): CollectionEntity?

    @Query("SELECT * FROM collections WHERE remoteId = :remoteId")
    suspend fun getByRemoteId(remoteId: Long): CollectionEntity?

    @Query("SELECT * FROM collections WHERE localId = :localId")
    fun getByIdAsFlow(localId: Long): Flow<CollectionEntity?>

    @Query("SELECT * FROM collections WHERE userId = :userId AND pendingDelete = 0")
    fun observeByUserId(userId: Long): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collections WHERE userId = :userId AND pendingDelete = 0")
    suspend fun getByUserId(userId: Long): List<CollectionEntity>

    @Query("SELECT * FROM collections WHERE name = :name AND userId = :userId")
    suspend fun getByNameAndUserId(name: String, userId: Long): CollectionEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM collections WHERE name = :name AND userId = :userId)")
    suspend fun existsByNameAndUserId(name: String, userId: Long): Boolean

    @Query("SELECT * FROM collections WHERE localId = :localId")
    fun observeCollection(localId: Long): Flow<CollectionEntity?>

    @Query("UPDATE collections SET pendingDelete = 1 WHERE localId = :localId")
    suspend fun markForDeletion(localId: Long): Int

    @Query("SELECT * FROM collections WHERE userId = :userId AND pendingDelete = 1")
    suspend fun getPendingDeletions(userId: Long): List<CollectionEntity>

    @Update
    suspend fun update(collection: CollectionEntity): Int

    @Delete
    suspend fun delete(collection: CollectionEntity): Int

    @Query("DELETE FROM collections WHERE localId = :localId")
    suspend fun deleteById(localId: Long): Int

    @Query("UPDATE collections SET synced = 1, remoteId = :remoteId WHERE localId = :localId")
    suspend fun markAsSynced(localId: Long, remoteId: Long): Int

    @Query("UPDATE collections SET synced = 0 WHERE localId = :localId")
    suspend fun markAsNotSynced(localId: Long): Int

    @Query("SELECT * FROM collections WHERE synced = 0")
    suspend fun getUnsyncedCollections(): List<CollectionEntity>

    @Query("SELECT * FROM collections WHERE synced = 0 AND userId = :userId")
    suspend fun getUnsyncedCollectionsByUserId(userId: Long): List<CollectionEntity>

    @Query("SELECT COUNT(*) FROM collections WHERE userId = :userId AND synced = 0")
    suspend fun countUnsyncedCollections(userId: Long): Int

    @Query("SELECT COUNT(*) FROM collections WHERE userId = :userId AND synced = 0")
    fun observeUnsyncedCollectionsCount(userId: Long): Flow<Int>
}
