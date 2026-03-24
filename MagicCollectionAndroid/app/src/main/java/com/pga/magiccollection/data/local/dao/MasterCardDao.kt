package com.pga.magiccollection.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pga.magiccollection.data.local.entities.MasterCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MasterCardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: MasterCardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<MasterCardEntity>)

    @Query("SELECT * FROM master_cards WHERE scryfallId = :scryfallId")
    suspend fun getCardByScryfallId(scryfallId: String): MasterCardEntity?

    @Query("SELECT * FROM master_cards WHERE scryfallId = :scryfallId")
    fun getCardByScryfallIdAsFlow(scryfallId: String): Flow<MasterCardEntity?>

    @Query("SELECT * FROM master_cards WHERE name = :name COLLATE NOCASE")
    suspend fun getCardByName(name: String): MasterCardEntity?

    @Query("SELECT * FROM master_cards WHERE name LIKE '%' || :name || '%' COLLATE NOCASE")
    suspend fun searchCardsByName(name: String): List<MasterCardEntity>

    @Query("SELECT * FROM master_cards WHERE name LIKE '%' || :name || '%' COLLATE NOCASE")
    fun searchCardsByNameAsFlow(name: String): Flow<List<MasterCardEntity>>

    @Query("SELECT * FROM master_cards WHERE setCode = :setCode")
    suspend fun getCardsBySetCode(setCode: String): List<MasterCardEntity>

    @Query("SELECT * FROM master_cards WHERE setCode = :setCode")
    fun getCardsBySetCodeAsFlow(setCode: String): Flow<List<MasterCardEntity>>

    @Query("SELECT * FROM master_cards WHERE typeLine LIKE '%' || :type || '%'")
    suspend fun searchCardsByType(type: String): List<MasterCardEntity>

    @Query("SELECT * FROM master_cards WHERE typeLine LIKE '%' || :type || '%'")
    fun searchCardsByTypeAsFlow(type: String): Flow<List<MasterCardEntity>>

    @Query("SELECT * FROM master_cards WHERE name LIKE '%' || :term || '%' OR oracleText LIKE '%' || :term || '%'")
    suspend fun globalSearch(term: String): List<MasterCardEntity>

    @Query("SELECT * FROM master_cards WHERE name LIKE '%' || :term || '%' OR oracleText LIKE '%' || :term || '%'")
    fun globalSearchAsFlow(term: String): Flow<List<MasterCardEntity>>

    @Query("SELECT * FROM master_cards")
    fun getAllCards(): Flow<List<MasterCardEntity>>

    @Query("SELECT * FROM master_cards ORDER BY name ASC")
    suspend fun getAllCardsSync(): List<MasterCardEntity>

    @Query("SELECT COUNT(*) FROM master_cards")
    suspend fun getCardCount(): Int

    @Query("SELECT * FROM master_cards LIMIT :limit OFFSET :offset")
    suspend fun getCardsPaginated(limit: Int, offset: Int): List<MasterCardEntity>

    @Update
    suspend fun updateCard(card: MasterCardEntity): Int

    @Delete
    suspend fun deleteCard(card: MasterCardEntity): Int

    @Query("DELETE FROM master_cards WHERE scryfallId = :scryfallId")
    suspend fun deleteCardByScryfallId(scryfallId: String): Int

    @Query("DELETE FROM master_cards")
    suspend fun deleteAllCards(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM master_cards WHERE scryfallId = :scryfallId)")
    suspend fun existsByScryfallId(scryfallId: String): Boolean

    @Query("SELECT * FROM master_cards WHERE scryfallId IN (:scryfallIds)")
    suspend fun getCardsByScryfallIds(scryfallIds: List<String>): List<MasterCardEntity>
}
