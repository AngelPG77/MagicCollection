package com.pga.magiccollection.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pga.magiccollection.data.local.entities.CardOwnedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardOwnedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCardOwned(card: CardOwnedEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCardsOwned(cards: List<CardOwnedEntity>)

    @Query("""
        SELECT * FROM cards_owned 
        WHERE scryfallId = :scryfallId 
        AND collectionId = :collectionId 
        AND language = :language 
        AND condition = :condition 
        AND isFoil = :isFoil
    """)
    suspend fun getExactCard(
        scryfallId: String,
        collectionId: Long,
        language: String,
        condition: String,
        isFoil: Boolean
    ): CardOwnedEntity?

    @Query("SELECT * FROM cards_owned WHERE collectionId = :collectionId AND pendingDelete = 0 ORDER BY scryfallId")
    fun observeCardsInCollection(collectionId: Long): Flow<List<CardOwnedEntity>>

    @Query("""
        SELECT co.* FROM cards_owned co
        INNER JOIN collections c ON co.collectionId = c.localId
        WHERE c.userId = :userId AND co.pendingDelete = 0
        ORDER BY co.scryfallId
    """)
    fun observeCardsOwnedByUser(userId: Long): Flow<List<CardOwnedEntity>>

    @Query("""
        UPDATE cards_owned SET pendingDelete = 1 
        WHERE scryfallId = :scryfallId 
        AND collectionId = :collectionId 
        AND language = :language 
        AND condition = :condition 
        AND isFoil = :isFoil
    """)
    suspend fun markForDeletion(
        scryfallId: String,
        collectionId: Long,
        language: String,
        condition: String,
        isFoil: Boolean
    ): Int

    @Query("SELECT * FROM cards_owned WHERE collectionId = :collectionId AND pendingDelete = 1")
    suspend fun getPendingDeletions(collectionId: Long): List<CardOwnedEntity>

    @Update
    suspend fun updateCardOwned(card: CardOwnedEntity): Int

    @Delete
    suspend fun deleteCardOwned(card: CardOwnedEntity): Int

    @Query("SELECT * FROM cards_owned WHERE synced = 0")
    suspend fun getUnsyncedCards(): List<CardOwnedEntity>

    @Query("""
        SELECT c.* FROM cards_owned c
        INNER JOIN collections col ON c.collectionId = col.localId
        WHERE c.synced = 0 AND col.userId = :userId
    """)
    suspend fun getUnsyncedCardsByUserId(userId: Long): List<CardOwnedEntity>

    @Query("SELECT COUNT(*) FROM cards_owned WHERE synced = 0")
    suspend fun countUnsyncedCards(): Int

    @Query("SELECT COUNT(*) FROM cards_owned WHERE synced = 0")
    fun observeUnsyncedCardsCount(): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM cards_owned c
        INNER JOIN collections col ON c.collectionId = col.localId
        WHERE c.synced = 0 AND col.userId = :userId
    """)
    fun observeUnsyncedCardsCount(userId: Long): Flow<Int>

    @Query("""
        SELECT DISTINCT m.imageUrl FROM master_cards m
        INNER JOIN cards_owned c ON m.scryfallId = c.scryfallId
        INNER JOIN collections col ON c.collectionId = col.localId
        WHERE col.userId = :userId AND c.pendingDelete = 0 AND m.imageUrl IS NOT NULL
    """)
    suspend fun getAllOwnedImageUrls(userId: Long): List<String>
}
