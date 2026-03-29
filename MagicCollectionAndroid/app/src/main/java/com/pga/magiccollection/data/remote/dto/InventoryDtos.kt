package com.pga.magiccollection.data.remote.dto

data class CardYouOwnRequestDto(
    val collectionId: Long,
    val cardName: String,
    val quantity: Int,
    val condition: String,
    val isFoil: Boolean,
    val language: String
)

data class CardYouOwnDto(
    val id: Long,
    val scryfallId: String,
    val name: String,
    val quantity: Int,
    val condition: String,
    val isFoil: Boolean,
    val language: String,
    val collectionId: Long
)
