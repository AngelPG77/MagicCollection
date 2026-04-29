package com.pga.magiccollection.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pga.magiccollection.data.local.entities.CardLanguageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardLanguageDao {
    @Query("SELECT * FROM downloaded_languages")
    fun observeDownloadedLanguages(): Flow<List<CardLanguageEntity>>

    @Query("SELECT * FROM downloaded_languages")
    suspend fun getDownloadedLanguages(): List<CardLanguageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLanguage(language: CardLanguageEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_languages WHERE languageCode = :langCode)")
    suspend fun isLanguageDownloaded(langCode: String): Boolean
}
