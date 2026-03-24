package com.pga.magiccollection.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ScryfallCardDto(
    val name: String,
    @SerializedName("setCode")
    val setCode: String?,
    @SerializedName("typeLine")
    val typeLine: String?,
    @SerializedName("oracleText")
    val oracleText: String?,
    @SerializedName("manaCost")
    val manaCost: String?,
    @SerializedName("cmc")
    val cmc: Double?
)

