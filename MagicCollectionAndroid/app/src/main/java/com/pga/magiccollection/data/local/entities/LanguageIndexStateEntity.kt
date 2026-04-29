package com.pga.magiccollection.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "language_index_state")
data class LanguageIndexStateEntity(
    @PrimaryKey
    val languageCode: String,
    val installedVersion: String?,
    val checksum: String?,
    val rowCount: Long,
    val lastSyncAt: Long,
    val status: String,
    val lastError: String? = null
)
