package com.pga.magiccollection.data.remote.api

import com.pga.magiccollection.data.remote.dto.CollectionRequestDto
import com.pga.magiccollection.data.remote.dto.CollectionResponseDto
import retrofit2.http.*

interface CollectionsApi {
    @GET("collections")
    suspend fun getCollections(): List<CollectionResponseDto>

    @POST("collections")
    suspend fun createCollection(@Body request: CollectionRequestDto): CollectionResponseDto

    @PUT("collections/{id}")
    suspend fun updateCollection(@Path("id") id: Long, @Body request: CollectionRequestDto): CollectionResponseDto

    @DELETE("collections/{id}")
    suspend fun deleteCollection(@Path("id") id: Long)
}
