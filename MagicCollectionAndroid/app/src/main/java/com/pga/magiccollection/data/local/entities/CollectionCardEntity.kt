package com.pga.magiccollection.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "collection_cards",
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["localId"],
            childColumns = ["collectionLocalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["collectionLocalId"])]
)
data class CollectionCardEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val remoteId: Long? = null,
    val collectionLocalId: Long,
    val scryfallId: String,
    val name: String,
    val typeLine: String?,
    val manaCost: String?,
    val imageUrl: String?,
    val quantity: Int,
    val foil: Boolean,
    val language: String,
    val condition: String,
    val synced: Boolean = false,
    val pendingDelete: Boolean = false
)
