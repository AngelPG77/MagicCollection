package com.pga.magiccollection.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mtg_sets")
data class MtgSetEntity(
    @PrimaryKey
    val code: String,
    val name: String,
    val releaseDate: String?
)
