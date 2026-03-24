package com.pga.magiccollection.data.remote.dto

data class CollectionRequestDto(
    val name: String
)

data class CollectionDto(
    val id: Long,
    val name: String,
    val ownerUsername: String
)

