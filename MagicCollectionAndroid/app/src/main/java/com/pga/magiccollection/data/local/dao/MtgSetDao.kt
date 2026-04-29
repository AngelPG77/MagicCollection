package com.pga.magiccollection.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pga.magiccollection.data.local.entities.MtgSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MtgSetDao {
    @Query("SELECT * FROM mtg_sets ORDER BY releaseDate DESC")
    fun getAllSets(): Flow<List<MtgSetEntity>>

    @Query("SELECT * FROM mtg_sets WHERE code = :code")
    suspend fun getSetByCode(code: String): MtgSetEntity?

    @Query("SELECT * FROM mtg_sets WHERE name LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%' ORDER BY releaseDate DESC")
    fun searchSets(query: String): Flow<List<MtgSetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<MtgSetEntity>)
}
