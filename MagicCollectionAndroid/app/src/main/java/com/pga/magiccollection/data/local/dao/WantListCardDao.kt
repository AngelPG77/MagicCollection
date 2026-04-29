package com.pga.magiccollection.data.local.dao

import androidx.room.*
import com.pga.magiccollection.data.local.entities.WantListCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WantListCardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: WantListCardEntity): Long

    @Update
    suspend fun update(card: WantListCardEntity)

    @Delete
    suspend fun delete(card: WantListCardEntity)

    @Query("DELETE FROM want_list_cards WHERE localId = :localId")
    suspend fun deleteById(localId: Long)

    @Query("SELECT * FROM want_list_cards WHERE wantListLocalId = :wantListLocalId AND pendingDelete = 0 ORDER BY name ASC")
    fun observeByWantListId(wantListLocalId: Long): Flow<List<WantListCardEntity>>

    @Query("SELECT * FROM want_list_cards WHERE wantListLocalId = :wantListLocalId AND pendingDelete = 0 ORDER BY name ASC")
    suspend fun getByWantListId(wantListLocalId: Long): List<WantListCardEntity>

    @Query("UPDATE want_list_cards SET pendingDelete = 1 WHERE localId = :localId")
    suspend fun markForDeletion(localId: Long)

    @Query("SELECT * FROM want_list_cards WHERE wantListLocalId = :wantListLocalId AND pendingDelete = 1")
    suspend fun getPendingDeletions(wantListLocalId: Long): List<WantListCardEntity>

    @Query("SELECT * FROM want_list_cards WHERE localId = :localId")
    suspend fun getById(localId: Long): WantListCardEntity?

    @Query("SELECT * FROM want_list_cards WHERE remoteId = :remoteId")
    suspend fun getByRemoteId(remoteId: Long): WantListCardEntity?

    @Query("DELETE FROM want_list_cards WHERE wantListLocalId = :wantListLocalId")
    suspend fun deleteAllByWantListId(wantListLocalId: Long)

    @Query("UPDATE want_list_cards SET synced = 1, remoteId = :remoteId WHERE localId = :localId")
    suspend fun markAsSynced(localId: Long, remoteId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM want_list_cards WHERE wantListLocalId = :wantListLocalId AND scryfallId = :scryfallId AND foil = :foil AND language = :language)")
    suspend fun existsByScryfallIdFoilAndLanguage(wantListLocalId: Long, scryfallId: String, foil: Boolean, language: String): Boolean

    @Query("SELECT * FROM want_list_cards WHERE wantListLocalId = :wantListLocalId AND scryfallId = :scryfallId AND foil = :foil AND language = :language AND condition = :condition")
    suspend fun getExactCard(wantListLocalId: Long, scryfallId: String, foil: Boolean, language: String, condition: String): WantListCardEntity?

    @Query("SELECT * FROM want_list_cards WHERE synced = 0")
    suspend fun getUnsyncedCards(): List<WantListCardEntity>

    @Query("""
        SELECT c.* FROM want_list_cards c
        INNER JOIN want_lists wl ON c.wantListLocalId = wl.localId
        WHERE c.synced = 0 AND wl.userId = :userId
    """)
    suspend fun getUnsyncedCardsByUserId(userId: Long): List<WantListCardEntity>

    @Query("SELECT COUNT(*) FROM want_list_cards WHERE synced = 0")
    suspend fun countUnsyncedCards(): Int

    @Query("SELECT COUNT(*) FROM want_list_cards WHERE synced = 0")
    fun observeUnsyncedCardsCount(): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM want_list_cards c
        INNER JOIN want_lists wl ON c.wantListLocalId = wl.localId
        WHERE c.synced = 0 AND wl.userId = :userId
    """)
    fun observeUnsyncedCardsCount(userId: Long): Flow<Int>
}
