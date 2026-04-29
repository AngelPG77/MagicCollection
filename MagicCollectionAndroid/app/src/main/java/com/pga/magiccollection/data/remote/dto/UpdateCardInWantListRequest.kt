package com.pga.magiccollection.data.remote.dto

data class UpdateCardInWantListRequest(
    val quantity: Int?,
    val foil: Boolean?,
    val language: String?,
    val condition: String?
)
