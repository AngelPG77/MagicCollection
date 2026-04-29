package com.pga.magiccollection.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "want_list_cards",
    foreignKeys = [
        ForeignKey(
            entity = WantListEntity::class,
            parentColumns = ["localId"],
            childColumns = ["wantListLocalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["wantListLocalId"])]
)
data class WantListCardEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val remoteId: Long? = null,
    val wantListLocalId: Long,
    val scryfallId: String,
    val name: String,
    val typeLine: String?,
    val manaCost: String?,
    val imageUrl: String?,
    val quantity: Int = 1,
    val foil: Boolean = false,
    val condition: String = "NEAR_MINT",
    val language: String = "en",
    val synced: Boolean = false,
    val pendingDelete: Boolean = false
)
