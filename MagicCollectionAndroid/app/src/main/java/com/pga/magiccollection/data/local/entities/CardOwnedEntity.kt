package com.pga.magiccollection.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "cards_owned",
    primaryKeys = ["scryfallId", "collectionId", "language", "condition", "isFoil"],
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["localId"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["collectionId"])]
)
data class CardOwnedEntity(
    val scryfallId: String,
    val collectionId: Long,
    val remoteId: Long? = null,
    val quantity: Int = 1,
    val isFoil: Boolean = false,
    val condition: String,
    val language: String = "ENGLISH",
    val synced: Boolean = false
)
