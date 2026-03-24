package com.pga.magiccollection.data.remote.api

import com.pga.magiccollection.data.remote.dto.CollectionDto
import com.pga.magiccollection.data.remote.dto.CollectionRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CollectionsApi {
    @POST("collections")
    suspend fun createCollection(@Body request: CollectionRequestDto): CollectionDto

    @GET("collections")
    suspend fun getCollections(): List<CollectionDto>
}

