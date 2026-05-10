package com.pga.magiccollection.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pga.magiccollection.data.local.entities.CollectionCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionCardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: CollectionCardEntity): Long

    @Update
    suspend fun update(card: CollectionCardEntity)

    @Delete
    suspend fun delete(card: CollectionCardEntity)

    @Query("SELECT * FROM collection_cards WHERE collectionLocalId = :collectionLocalId AND pendingDelete = 0 ORDER BY name ASC")
    fun observeByCollectionId(collectionLocalId: Long): Flow<List<CollectionCardEntity>>

    @Query("SELECT * FROM collection_cards WHERE collectionLocalId = :collectionLocalId AND pendingDelete = 0 ORDER BY name ASC")
    suspend fun getByCollectionId(collectionLocalId: Long): List<CollectionCardEntity>

    @Query("SELECT * FROM collection_cards WHERE collectionLocalId = :collectionLocalId AND pendingDelete = 1")
    suspend fun getPendingDeletions(collectionLocalId: Long): List<CollectionCardEntity>

    @Query("SELECT * FROM collection_cards WHERE localId = :localId")
    suspend fun getById(localId: Long): CollectionCardEntity?

    @Query("SELECT * FROM collection_cards WHERE remoteId = :remoteId")
    suspend fun getByRemoteId(remoteId: Long): CollectionCardEntity?

    @Query("DELETE FROM collection_cards WHERE collectionLocalId = :collectionLocalId")
    suspend fun deleteAllByCollectionId(collectionLocalId: Long)

    @Query("SELECT * FROM collection_cards WHERE collectionLocalId = :collectionLocalId AND scryfallId = :scryfallId AND foil = :foil AND language = :language AND condition = :condition")
    suspend fun getExactCard(collectionLocalId: Long, scryfallId: String, foil: Boolean, language: String, condition: String): CollectionCardEntity?

    @Query("SELECT * FROM collection_cards WHERE synced = 0")
    suspend fun getUnsyncedCards(): List<CollectionCardEntity>

    @Query("""
        SELECT c.* FROM collection_cards c
        INNER JOIN collections col ON c.collectionLocalId = col.localId
        WHERE col.userId = :userId AND c.synced = 0
    """)
    suspend fun getUnsyncedCardsByUserId(userId: Long): List<CollectionCardEntity>

    @Query("""
        SELECT c.* FROM collection_cards c
        INNER JOIN collections col ON c.collectionLocalId = col.localId
        WHERE col.userId = :userId AND c.pendingDelete = 0
    """)
    fun observeAllUserCards(userId: Long): Flow<List<CollectionCardEntity>>

    @Query("""
        SELECT c.* FROM collection_cards c
        INNER JOIN collections col ON c.collectionLocalId = col.localId
        WHERE col.userId = :userId AND c.pendingDelete = 0
    """)
    suspend fun getAllUserCards(userId: Long): List<CollectionCardEntity>

    @Query("""
        SELECT COUNT(*) FROM collection_cards c
        INNER JOIN collections col ON c.collectionLocalId = col.localId
        WHERE col.userId = :userId AND c.synced = 0
    """)
    fun observeUnsyncedCardsCount(userId: Long): Flow<Int>

    @Query("""
        SELECT c.imageUrl FROM collection_cards c
        INNER JOIN collections col ON c.collectionLocalId = col.localId
        WHERE col.userId = :userId AND c.pendingDelete = 0 AND c.imageUrl IS NOT NULL
    """)
    suspend fun getAllImageUrls(userId: Long): List<String>

    @Query("DELETE FROM collection_cards WHERE localId = :localId")
    suspend fun deleteById(localId: Long): Int

    @Query("UPDATE collection_cards SET synced = 1, remoteId = :remoteId WHERE localId = :localId")
    suspend fun markAsSynced(localId: Long, remoteId: Long): Int

    @Query("UPDATE collection_cards SET synced = 0 WHERE localId = :localId")
    suspend fun markAsNotSynced(localId: Long): Int

    @Query("UPDATE collection_cards SET pendingDelete = 1 WHERE localId = :localId")
    suspend fun markForDeletion(localId: Long): Int
}
