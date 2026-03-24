package com.pga.magiccollection.data.local.entities

import androidx.room.Entity

@Entity(
    tableName = "cards_owned",
    primaryKeys = ["scryfallId", "collectionId", "language", "condition", "isFoil"]
)
data class CardOwnedEntity(
    val scryfallId: String,
    val collectionId: Long,
    val remoteId: Long? = null,
    val quantity: Int = 1,
    val isFoil: Boolean = false,
    val condition: String,
    val language: String = "ENGLISH",
    val Synced: Boolean = false
)


