package com.pga.magiccollection.data.local.dao

import androidx.room.*
import com.pga.magiccollection.data.local.entities.WantListEntity
import kotlinx.coroutines.flow.Flow

data class WantListWithCount(
    val localId: Long,
    val remoteId: Long?,
    val name: String,
    val userId: Long,
    val synced: Boolean,
    val cardCount: Int
)

@Dao
interface WantListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wantList: WantListEntity): Long

    @Query("""
        SELECT w.localId, w.remoteId, w.name, w.userId, w.synced, COALESCE(SUM(wc.quantity), 0) as cardCount 
        FROM want_lists w
        LEFT JOIN want_list_cards wc ON w.localId = wc.wantListLocalId AND wc.pendingDelete = 0
        WHERE w.userId = :userId AND w.pendingDelete = 0
        GROUP BY w.localId
        ORDER BY w.name ASC
    """)
    fun observeByUserIdWithCount(userId: Long): Flow<List<WantListWithCount>>
    @Update
    suspend fun update(wantList: WantListEntity)

    @Delete
    suspend fun delete(wantList: WantListEntity)

    @Query("DELETE FROM want_lists WHERE localId = :localId")
    suspend fun deleteById(localId: Long)

    @Query("SELECT * FROM want_lists WHERE userId = :userId AND pendingDelete = 0 ORDER BY name ASC")
    fun observeByUserId(userId: Long): Flow<List<WantListEntity>>

    @Query("SELECT * FROM want_lists WHERE userId = :userId AND pendingDelete = 0 ORDER BY name ASC")
    suspend fun getByUserId(userId: Long): List<WantListEntity>

    @Query("UPDATE want_lists SET pendingDelete = 1 WHERE localId = :localId")
    suspend fun markForDeletion(localId: Long)

    @Query("SELECT * FROM want_lists WHERE userId = :userId AND pendingDelete = 1")
    suspend fun getPendingDeletions(userId: Long): List<WantListEntity>

    @Query("SELECT * FROM want_lists WHERE localId = :localId")
    suspend fun getById(localId: Long): WantListEntity?

    @Query("SELECT * FROM want_lists WHERE remoteId = :remoteId")
    suspend fun getByRemoteId(remoteId: Long): WantListEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM want_lists WHERE name = :name AND userId = :userId)")
    suspend fun existsByNameAndUserId(name: String, userId: Long): Boolean

    @Query("DELETE FROM want_lists WHERE userId = :userId")
    suspend fun deleteAllByUserId(userId: Long)

    @Query("UPDATE want_lists SET synced = 1, remoteId = :remoteId WHERE localId = :localId")
    suspend fun markAsSynced(localId: Long, remoteId: Long)

    @Query("UPDATE want_lists SET synced = 0 WHERE localId = :localId")
    suspend fun markAsNotSynced(localId: Long)

    @Query("SELECT * FROM want_lists WHERE userId = :userId AND synced = 0")
    suspend fun getUnsyncedWantLists(userId: Long): List<WantListEntity>

    @Query("SELECT * FROM want_lists WHERE name = :name AND userId = :userId")
    suspend fun getByNameAndUserId(name: String, userId: Long): WantListEntity?

    @Query("SELECT COUNT(*) FROM want_lists WHERE userId = :userId AND synced = 0")
    suspend fun countUnsyncedWantLists(userId: Long): Int

    @Query("SELECT COUNT(*) FROM want_lists WHERE userId = :userId AND synced = 0")
    fun observeUnsyncedWantListsCount(userId: Long): Flow<Int>
}
