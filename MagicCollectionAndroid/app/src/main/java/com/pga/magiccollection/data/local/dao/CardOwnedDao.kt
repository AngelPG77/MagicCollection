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

    @Query("SELECT * FROM cards_owned WHERE collectionId = :collectionId ORDER BY scryfallId")
    fun observeCardsInCollection(collectionId: Long): Flow<List<CardOwnedEntity>>

    @Update
    suspend fun updateCardOwned(card: CardOwnedEntity): Int

    @Delete
    suspend fun deleteCardOwned(card: CardOwnedEntity): Int

    @Query("SELECT * FROM cards_owned WHERE synced = 0")
    suspend fun getUnsyncedCards(): List<CardOwnedEntity>

    @Query("""
        UPDATE cards_owned SET synced = 1, remoteId = :remoteId
        WHERE scryfallId = :scryfallId 
        AND collectionId = :collectionId 
        AND language = :language 
        AND condition = :condition 
        AND isFoil = :isFoil
    """)
    suspend fun markAsSynced(
        scryfallId: String,
        collectionId: Long,
        language: String,
        condition: String,
        isFoil: Boolean,
        remoteId: Long
    ): Int
}
