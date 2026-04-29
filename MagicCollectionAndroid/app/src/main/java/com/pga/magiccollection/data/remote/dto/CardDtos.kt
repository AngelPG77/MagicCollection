package com.pga.magiccollection.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ScryfallCardDto(
    val scryfallId: String?,
    val name: String,
    val printedName: String?,
    val setCode: String?,
    val typeLine: String?,
    val oracleText: String?,
    val manaCost: String?,
    val convertedManaCost: Double?,
    val cmc: Double? = null,
    val rarity: String? = null,
    val rarityRank: Int? = null,
    val colorMask: Int? = null,
    val identityMask: Int? = null,
    val colors: List<String>? = null,
    val colorIdentity: List<String>? = null,
    val power: String?,
    val toughness: String?,
    val imageUris: ImageUrisDto?,
    val setName: String? = null,
    val foil: Boolean? = null,
    val nonfoil: Boolean? = null
)

data class ImageUrisDto(
    val small: String?,
    val normal: String?,
    val large: String?,
    val png: String?,
    val artCrop: String?,
    val borderCrop: String?
)

data class CardMetadataIndexDto(
    val scryfallId: String,
    val defaultName: String,
    val localizedName: String?,
    val colorMask: Int,
    val identityMask: Int,
    val manaCost: String?,
    val cmc: Float?,
    val rarityRank: Int,
    val typeLine: String?,
    val setCode: String?,
    val imageUrl: String?
)

data class CardMetadataIndexPageDto(
    val items: List<CardMetadataIndexDto>,
    val offset: Int,
    val limit: Int,
    val hasMore: Boolean,
    val totalCards: Long
)

data class LanguageIndexInfoDto(
    val languageCode: String,
    val version: String?,
    val checksum: String?,
    val totalRows: Long,
    val generatedAt: String?,
    val status: String
)

data class LanguageIndexManifestDto(
    val languageCode: String,
    val version: String,
    val checksum: String,
    val totalRows: Long,
    val generatedAt: String?,
    val sourceLastUpdated: String?,
    val status: String,
    val artifactPath: String?,
    val deltaAvailable: Boolean
)

data class LanguageDeltaItemDto(
    val scryfallId: String,
    val localizedName: String
)

data class LanguageIndexDeltaDto(
    val languageCode: String,
    val fromVersion: String?,
    val targetVersion: String,
    val checksum: String,
    val totalRows: Long,
    val upserts: List<LanguageDeltaItemDto>,
    val deletes: List<String>
)
