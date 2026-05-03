package com.pga.magiccollection.data.remote.dto

data class CollectionRequestDto(
    val name: String
)

data class CollectionResponseDto(
    val id: Long,
    val name: String,
    val ownerUsername: String?,
    val cardCount: Int?,
    val cards: List<CollectionCardDto>?
)

data class CollectionCardDto(
    val id: Long,
    val scryfallId: String,
    val name: String,
    val typeLine: String?,
    val manaCost: String?,
    val imageUrl: String?,
    val quantity: Int,
    val foil: Boolean,
    val language: String,
    val condition: String,
    val collectionId: Long?,
    val collectionName: String?
)

data class AddCardToCollectionRequest(
    val scryfallId: String,
    val name: String,
    val typeLine: String?,
    val manaCost: String?,
    val imageUrl: String?,
    val quantity: Int = 1,
    val foil: Boolean = false,
    val language: String = "en",
    val condition: String = "NEAR_MINT"
)

data class UpdateCardInCollectionRequest(
    val quantity: Int?,
    val foil: Boolean?,
    val language: String?,
    val condition: String?
)
