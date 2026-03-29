package com.pga.magiccollection.data.remote.dto

data class CollectionRequestDto(
    val name: String
)

data class CollectionResponseDto(
    val id: Long,
    val name: String
)
