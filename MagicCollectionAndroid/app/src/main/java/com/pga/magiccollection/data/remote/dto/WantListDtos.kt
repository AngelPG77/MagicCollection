package com.pga.magiccollection.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WantListDto(
    val id: Long,
    val name: String,
    val ownerId: Long,
    val cards: List<WantListCardDto> = emptyList()
)

data class WantListCardDto(
    val id: Long,
    val scryfallId: String,
    val name: String,
    val typeLine: String?,
    val manaCost: String?,
    val imageUrl: String?,
    val quantity: Int,
    val foil: Boolean,
    val language: String,
    val condition: String
)

data class CreateWantListRequest(
    val name: String
)

data class UpdateWantListRequest(
    val name: String
)

data class AddCardToWantListRequest(
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

data class CreateWantListResponse(
    val id: Long,
    val name: String
)
