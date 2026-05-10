package com.pga.magiccollection.data.remote.dto

data class CardSyncStatusDto(
    val scryfallInSync: Boolean,
    val catalogStateMissing: Boolean,
    val lastSyncedAt: String? = null,
    val defaultCardsRemoteToken: String? = null,
    val allCardsRemoteToken: String? = null,
    val languages: List<LanguageSyncStatusDto> = emptyList()
)

data class LanguageSyncStatusDto(
    val languageCode: String,
    val version: String? = null,
    val checksum: String? = null,
    val totalRows: Long = 0L,
    val status: String? = null
)
