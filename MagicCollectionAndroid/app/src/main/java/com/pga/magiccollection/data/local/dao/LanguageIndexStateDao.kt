package com.pga.magiccollection.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pga.magiccollection.data.local.entities.LanguageIndexStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LanguageIndexStateDao {
    @Query("SELECT * FROM language_index_state")
    fun observeStates(): Flow<List<LanguageIndexStateEntity>>

    @Query("SELECT * FROM language_index_state WHERE languageCode = :languageCode LIMIT 1")
    suspend fun getState(languageCode: String): LanguageIndexStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: LanguageIndexStateEntity)
}
