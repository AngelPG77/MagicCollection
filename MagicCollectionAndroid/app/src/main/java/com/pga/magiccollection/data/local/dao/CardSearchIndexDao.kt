package com.pga.magiccollection.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.pga.magiccollection.data.local.entities.MasterCardEntity
import com.pga.magiccollection.data.local.entities.CardNameFtsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardSearchIndexDao {

    @RawQuery(observedEntities = [MasterCardEntity::class, CardNameFtsEntity::class])
    fun observeCards(query: SupportSQLiteQuery): Flow<List<CardSearchRow>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMasterCards(cards: List<MasterCardEntity>)

    @Query("SELECT COUNT(*) FROM master_cards")
    suspend fun countMetadata(): Int

    @Query("SELECT COUNT(*) FROM master_cards")
    fun observeMetadataCount(): Flow<Int>

    @Query("DELETE FROM card_names_fts WHERE card_id IN (:cardIds) AND language = :language")
    suspend fun deleteNamesByLanguage(cardIds: List<String>, language: String)

    @Query("DELETE FROM card_names_fts WHERE language = :language")
    suspend fun deleteAllNamesByLanguage(language: String)

    @Query("SELECT COUNT(*) FROM card_names_fts WHERE language = :language")
    suspend fun countNamesByLanguage(language: String): Long

    @Query("SELECT COUNT(*) FROM card_names_fts WHERE language = :language")
    fun observeNamesCountByLanguage(language: String): Flow<Long>

    @Insert
    suspend fun insertNames(names: List<CardNameFtsEntity>)
}

data class CardSearchRow(
    val scryfallId: String,
    val name: String,
    val imageUrl: String?
)
