package com.pga.magiccollection.data.remote.api

import com.pga.magiccollection.data.remote.dto.ScryfallCardDto
import retrofit2.http.*

interface CardsApi {
    @GET("cards/search")
    suspend fun getCardByName(@Query("name") name: String): ScryfallCardDto

    @GET("cards/library")
    suspend fun getAllKnownCards(): List<ScryfallCardDto>

    @GET("cards/discover")
    suspend fun searchCards(@Query("query") query: String): List<ScryfallCardDto>

    @GET("cards/{id}")
    suspend fun getCardById(@Path("id") id: Long): ScryfallCardDto
}
