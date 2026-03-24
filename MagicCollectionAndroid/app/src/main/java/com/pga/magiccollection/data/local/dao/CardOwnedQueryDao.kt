package com.pga.magiccollection.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.pga.magiccollection.data.local.entities.CardOwnedEntity
import kotlinx.coroutines.flow.Flow

/**
 * Specialised DAO for complex queries on owned cards.
 * Separates search/filter operations from CRUD operations (CardOwnedDao).
 * Follows Single Responsibility Principle: only handles query operations.
 */
@Dao
interface CardOwnedQueryDao {

    // Global inventory search (across all user collections)
    @Query("""
        SELECT DISTINCT co.* FROM cards_owned co
        INNER JOIN master_cards mc ON co.scryfallId = mc.scryfallId
        INNER JOIN collections c ON co.collectionId = c.localId
        WHERE c.userId = :userId
        AND (mc.name LIKE '%' || :term || '%' COLLATE NOCASE
             OR mc.oracleText LIKE '%' || :term || '%')
        ORDER BY mc.name
    """)
    suspend fun searchInGlobalInventory(userId: Long, term: String): List<CardOwnedEntity>

    @Query("""
        SELECT DISTINCT co.* FROM cards_owned co
        INNER JOIN master_cards mc ON co.scryfallId = mc.scryfallId
        INNER JOIN collections c ON co.collectionId = c.localId
        WHERE c.userId = :userId
        AND (mc.name LIKE '%' || :term || '%' COLLATE NOCASE
             OR mc.oracleText LIKE '%' || :term || '%')
        ORDER BY mc.name
    """)
    fun searchInGlobalInventoryAsFlow(userId: Long, term: String): Flow<List<CardOwnedEntity>>

    // Specific collection search
    @Query("""
        SELECT co.* FROM cards_owned co
        INNER JOIN master_cards mc ON co.scryfallId = mc.scryfallId
        WHERE co.collectionId = :collectionId
        AND (mc.name LIKE '%' || :term || '%' COLLATE NOCASE
             OR mc.oracleText LIKE '%' || :term || '%')
        ORDER BY mc.name
    """)
    suspend fun searchInCollection(collectionId: Long, term: String): List<CardOwnedEntity>

    @Query("""
        SELECT co.* FROM cards_owned co
        INNER JOIN master_cards mc ON co.scryfallId = mc.scryfallId
        WHERE co.collectionId = :collectionId
        AND (mc.name LIKE '%' || :term || '%' COLLATE NOCASE
             OR mc.oracleText LIKE '%' || :term || '%')
        ORDER BY mc.name
    """)
    fun searchInCollectionAsFlow(collectionId: Long, term: String): Flow<List<CardOwnedEntity>>

    // Search by card type/subtype
    @Query("""
        SELECT DISTINCT co.* FROM cards_owned co
        INNER JOIN master_cards mc ON co.scryfallId = mc.scryfallId
        INNER JOIN collections c ON co.collectionId = c.localId
        WHERE c.userId = :userId
        AND mc.typeLine LIKE '%' || :type || '%'
        ORDER BY mc.name
    """)
    suspend fun searchByCardType(userId: Long, type: String): List<CardOwnedEntity>

    @Query("""
        SELECT DISTINCT co.* FROM cards_owned co
        INNER JOIN master_cards mc ON co.scryfallId = mc.scryfallId
        INNER JOIN collections c ON co.collectionId = c.localId
        WHERE c.userId = :userId
        AND mc.typeLine LIKE '%' || :type || '%'
        ORDER BY mc.name
    """)
    fun searchByCardTypeAsFlow(userId: Long, type: String): Flow<List<CardOwnedEntity>>

    // Filter by card properties
    @Query("""
        SELECT * FROM cards_owned 
        WHERE collectionId = :collectionId AND condition = :condition
        ORDER BY scryfallId
    """)
    suspend fun getCardsByCondition(collectionId: Long, condition: String): List<CardOwnedEntity>

    @Query("""
        SELECT * FROM cards_owned 
        WHERE collectionId = :collectionId AND isFoil = 1
        ORDER BY scryfallId
    """)
    suspend fun getFoilCards(collectionId: Long): List<CardOwnedEntity>

    @Query("""
        SELECT * FROM cards_owned 
        WHERE collectionId = :collectionId AND language = :language
        ORDER BY scryfallId
    """)
    suspend fun getCardsByLanguage(collectionId: Long, language: String): List<CardOwnedEntity>

    // Advanced search: by color in mana cost
    @Query("""
        SELECT DISTINCT co.* FROM cards_owned co
        INNER JOIN master_cards mc ON co.scryfallId = mc.scryfallId
        WHERE co.collectionId = :collectionId
        AND mc.manaCost LIKE '%' || :color || '%'
        ORDER BY mc.name
    """)
    suspend fun searchByColor(collectionId: Long, color: String): List<CardOwnedEntity>

    // Advanced search: by converted mana cost
    @Query("""
        SELECT DISTINCT co.* FROM cards_owned co
        INNER JOIN master_cards mc ON co.scryfallId = mc.scryfallId
        WHERE co.collectionId = :collectionId
        AND mc.convertedManaCost = :cmc
        ORDER BY mc.name
    """)
    suspend fun searchByConvertedManaCost(collectionId: Long, cmc: Int): List<CardOwnedEntity>

    // Pagination support for large collections
    @Query("""
        SELECT * FROM cards_owned
        WHERE collectionId = :collectionId
        ORDER BY scryfallId
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getCardsPaginated(
        collectionId: Long,
        limit: Int,
        offset: Int
    ): List<CardOwnedEntity>
}
