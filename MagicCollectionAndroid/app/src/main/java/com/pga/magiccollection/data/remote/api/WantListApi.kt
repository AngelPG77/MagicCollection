package com.pga.magiccollection.data.remote.api

import com.pga.magiccollection.data.remote.dto.*
import retrofit2.http.*

interface WantListApi {
    @GET("wantlists")
    suspend fun getMyWantLists(): List<WantListDto>

    @GET("wantlists/{id}")
    suspend fun getWantListById(@Path("id") id: Long): WantListDto

    @POST("wantlists")
    suspend fun createWantList(@Body request: CreateWantListRequest): CreateWantListResponse

    @PUT("wantlists/{id}")
    suspend fun updateWantList(@Path("id") id: Long, @Body request: UpdateWantListRequest)

    @DELETE("wantlists/{id}")
    suspend fun deleteWantList(@Path("id") id: Long)

    @POST("wantlists/{id}/cards")
    suspend fun addCardToWantList(@Path("id") id: Long, @Body request: AddCardToWantListRequest): Long

    @PUT("wantlists/{id}/cards/{cardId}")
    suspend fun updateCardInWantList(
        @Path("id") id: Long,
        @Path("cardId") cardId: Long,
        @Body request: UpdateCardInWantListRequest
    )

    @DELETE("wantlists/{id}/cards/{cardId}")
    suspend fun removeCardFromWantList(@Path("id") id: Long, @Path("cardId") cardId: Long)
}
