package com.pga.magiccollection.data.remote.api

import com.pga.magiccollection.data.remote.dto.CardYouOwnDto
import com.pga.magiccollection.data.remote.dto.CardYouOwnRequestDto
import retrofit2.http.*

interface InventoryApi {
    @POST("your-cards/add")
    suspend fun addCard(@Body request: CardYouOwnRequestDto): CardYouOwnDto

    @PUT("your-cards/update/{id}")
    suspend fun updateCard(@Path("id") id: Long, @Body request: CardYouOwnRequestDto): CardYouOwnDto

    @DELETE("your-cards/delete/{id}")
    suspend fun deleteCard(@Path("id") id: Long)

    @GET("your-cards/collection/{collectionId}")
    suspend fun getCardsByCollection(@Path("collectionId") collectionId: Long): List<CardYouOwnDto>

    @GET("your-cards/search/global")
    suspend fun searchGlobal(@Query("term") term: String): List<CardYouOwnDto>

    @GET("your-cards/search/collection/{collectionId}")
    suspend fun searchInCollection(@Path("collectionId") collectionId: Long, @Query("term") term: String): List<CardYouOwnDto>

    @GET("your-cards/search/type")
    suspend fun searchByType(@Query("type") type: String): List<CardYouOwnDto>
}
