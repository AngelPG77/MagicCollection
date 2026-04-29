package com.pga.magiccollection.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "master_cards",
    indices = [
        Index("name"),
        Index("typeLine"),
        Index("colorMask"),
        Index("identityMask"),
        Index("rarityRank"),
        Index("cmc")
    ]
)
data class MasterCardEntity(
    @PrimaryKey val scryfallId: String,
    val name: String,
    val printedName: String? = null,
    val setCode: String? = null,
    val typeLine: String? = null,
    val manaCost: String? = null,
    val convertedManaCost: Int? = null,
    val cmc: Float? = null,
    val rarityRank: Int = 0,
    val colorMask: Int = 0,
    val identityMask: Int = 0,
    val isDigital: Boolean = false,
    val imageUrl: String? = null
)
