package com.pga.magiccollection.data.remote.api

import com.pga.magiccollection.data.remote.dto.AddCardToCollectionRequest
import com.pga.magiccollection.data.remote.dto.CollectionCardDto
import com.pga.magiccollection.data.remote.dto.CollectionRequestDto
import com.pga.magiccollection.data.remote.dto.CollectionResponseDto
import com.pga.magiccollection.data.remote.dto.UpdateCardInCollectionRequest
import retrofit2.http.*

interface CollectionsApi {
    @GET("collections")
    suspend fun getCollections(): List<CollectionResponseDto>

    @GET("collections/{id}")
    suspend fun getCollectionById(@Path("id") id: Long): CollectionResponseDto

    @POST("collections")
    suspend fun createCollection(@Body request: CollectionRequestDto): CollectionResponseDto

    @PUT("collections/{id}")
    suspend fun updateCollection(@Path("id") id: Long, @Body request: CollectionRequestDto): CollectionResponseDto

    @DELETE("collections/{id}")
    suspend fun deleteCollection(@Path("id") id: Long)

    @POST("collections/{id}/cards")

    suspend fun addCardToCollection(@Path("id") id: Long, @Body request: AddCardToCollectionRequest): Long

    @PUT("collections/{id}/cards/{cardId}")
    suspend fun updateCardInCollection(
        @Path("id") id: Long,
        @Path("cardId") cardId: Long,
        @Body request: UpdateCardInCollectionRequest
    )

    @DELETE("collections/{id}/cards/{cardId}")
    suspend fun removeCardFromCollection(@Path("id") id: Long, @Path("cardId") cardId: Long)

    @GET("collections/all-cards")
    suspend fun getAllUserCards(): List<CollectionCardDto>
}
