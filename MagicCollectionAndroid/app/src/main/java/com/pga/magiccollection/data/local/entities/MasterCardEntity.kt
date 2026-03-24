package com.pga.magiccollection.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "master_cards")
data class MasterCardEntity(
    @PrimaryKey val scryfallId: String,
    val remoteId: Long? = null,
    val name: String,
    val setCode: String,
    val oracleText: String?,
    val typeLine: String?,
    val manaCost: String? = null,
    val convertedManaCost: Int? = null,
    val imageUrl: String?
)
