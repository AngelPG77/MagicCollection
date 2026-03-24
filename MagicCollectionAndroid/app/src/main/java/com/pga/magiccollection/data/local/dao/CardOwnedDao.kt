package com.pga.magiccollection.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pga.magiccollection.data.local.entities.CardOwnedEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for CardOwnedEntity - Core CRUD operations and direct access patterns.
 * Search and complex filter operations are handled by CardOwnedQueryDao.
 * This separation respects Single Responsibility Principle and Clean Architecture.
 */
@Dao
interface CardOwnedDao {

    // ========== INSERT OPERATIONS ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCardOwned(card: CardOwnedEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCardsOwned(cards: List<CardOwnedEntity>)

    // ========== READ OPERATIONS - Exact Card Access ==========
    /**
     * Get a specific card instance with all its properties matched.
     * Used to identify duplicates with different conditions/foil/language.
     */
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

    @Query("""
        SELECT * FROM cards_owned 
        WHERE scryfallId = :scryfallId 
        AND collectionId = :collectionId 
        AND language = :language 
        AND condition = :condition 
        AND isFoil = :isFoil
    """)
    fun observeExactCard(
        scryfallId: String,
        collectionId: Long,
        language: String,
        condition: String,
        isFoil: Boolean
    ): Flow<CardOwnedEntity?>

    // ========== READ OPERATIONS - Collection-Based ==========
    @Query("SELECT * FROM cards_owned WHERE collectionId = :collectionId ORDER BY scryfallId")
    fun getCardsInCollection(collectionId: Long): Flow<List<CardOwnedEntity>>

    @Query("SELECT * FROM cards_owned WHERE collectionId = :collectionId ORDER BY scryfallId")
    suspend fun getCardsInCollectionSync(collectionId: Long): List<CardOwnedEntity>

    @Query("SELECT * FROM cards_owned WHERE collectionId = :collectionId ORDER BY scryfallId")
    fun observeCardsInCollection(collectionId: Long): Flow<List<CardOwnedEntity>>

    // ========== STATISTICS ==========
    @Query("SELECT COUNT(*) FROM cards_owned WHERE collectionId = :collectionId")
    suspend fun countCardsInCollection(collectionId: Long): Int

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

    // ========== UPDATE OPERATIONS ==========
    @Update
    suspend fun updateCardOwned(card: CardOwnedEntity): Int

    @Query("""
        UPDATE cards_owned 
        SET quantity = :quantity
        WHERE scryfallId = :scryfallId 
        AND collectionId = :collectionId 
        AND language = :language 
        AND condition = :condition 
        AND isFoil = :isFoil
    """)
    suspend fun updateQuantity(
        scryfallId: String,
        collectionId: Long,
        language: String,
        condition: String,
        isFoil: Boolean,
        quantity: Int
    ): Int

    // ========== DELETE OPERATIONS ==========
    @Delete
    suspend fun deleteCardOwned(card: CardOwnedEntity): Int

    @Query("""
        DELETE FROM cards_owned 
        WHERE scryfallId = :scryfallId 
        AND collectionId = :collectionId 
        AND language = :language 
        AND condition = :condition 
        AND isFoil = :isFoil
    """)
    suspend fun deleteExactCard(
        scryfallId: String,
        collectionId: Long,
        language: String,
        condition: String,
        isFoil: Boolean
    ): Int

    @Query("DELETE FROM cards_owned WHERE collectionId = :collectionId")
    suspend fun deleteCardsInCollection(collectionId: Long): Int

    // ========== SYNC OPERATIONS ==========
    @Query("SELECT * FROM cards_owned WHERE Synced = 0 ORDER BY scryfallId")
    suspend fun getUnsyncedCards(): List<CardOwnedEntity>

    @Query("""
        UPDATE cards_owned SET Synced = 1, remoteId = :remoteId
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

    @Query("""
        UPDATE cards_owned SET Synced = 0
        WHERE scryfallId = :scryfallId 
        AND collectionId = :collectionId 
        AND language = :language 
        AND condition = :condition 
        AND isFoil = :isFoil
    """)
    suspend fun markAsNotSynced(
        scryfallId: String,
        collectionId: Long,
        language: String,
        condition: String,
        isFoil: Boolean
    ): Int

    // ========== EXISTENCE CHECKS ==========
    @Query("""
        SELECT EXISTS(SELECT 1 FROM cards_owned 
        WHERE scryfallId = :scryfallId 
        AND collectionId = :collectionId 
        AND language = :language 
        AND condition = :condition 
        AND isFoil = :isFoil)
    """)
    suspend fun existsExactCard(
        scryfallId: String,
        collectionId: Long,
        language: String,
        condition: String,
        isFoil: Boolean
    ): Boolean
}
