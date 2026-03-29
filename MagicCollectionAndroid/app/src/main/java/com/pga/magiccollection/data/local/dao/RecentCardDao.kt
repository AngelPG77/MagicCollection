package com.pga.magiccollection.data.local.dao

import androidx.room.*
import com.pga.magiccollection.data.local.entities.RecentCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentCardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentCard(card: RecentCardEntity)

    @Query("SELECT * FROM recent_cards ORDER BY visitedAt DESC LIMIT 20")
    fun observeRecentCards(): Flow<List<RecentCardEntity>>

    @Query("DELETE FROM recent_cards WHERE scryfallId NOT IN (SELECT scryfallId FROM recent_cards ORDER BY visitedAt DESC LIMIT 20)")
    suspend fun deleteOldRecentCards()

    @Transaction
    suspend fun insertAndTrim(card: RecentCardEntity) {
        insertRecentCard(card)
        deleteOldRecentCards()
    }
}
