package com.pga.magiccollection.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_cards")
data class RecentCardEntity(
    @PrimaryKey val scryfallId: String,
    val name: String,
    val imageUrl: String?,
    val visitedAt: Long = System.currentTimeMillis()
)
