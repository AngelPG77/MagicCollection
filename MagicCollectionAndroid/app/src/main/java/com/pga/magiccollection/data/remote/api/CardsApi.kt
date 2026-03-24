package com.pga.magiccollection.data.remote.api

import com.pga.magiccollection.data.remote.dto.ScryfallCardDto
import retrofit2.http.GET
import retrofit2.http.Query

interface CardsApi {
    @GET("cards/discover")
    suspend fun discoverCards(@Query("query") query: String): List<ScryfallCardDto>
}

