package com.pga.magiccollection.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_languages")
data class CardLanguageEntity(
    @PrimaryKey
    val languageCode: String,
    val downloadedAt: Long = System.currentTimeMillis()
)
