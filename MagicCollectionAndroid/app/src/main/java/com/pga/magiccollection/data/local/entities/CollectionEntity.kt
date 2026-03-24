package com.pga.magiccollection.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "collections",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]

)
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val remoteId: Long? = null,
    val name: String,
    val userId: Long,
    val synced: Boolean = false
)