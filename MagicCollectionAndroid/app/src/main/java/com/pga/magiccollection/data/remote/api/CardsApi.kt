package com.pga.magiccollection.data.remote.api

import com.pga.magiccollection.data.remote.dto.CardSuggestionDto
import com.pga.magiccollection.data.remote.dto.CardMetadataIndexPageDto
import com.pga.magiccollection.data.remote.dto.CardSyncStatusDto
import com.pga.magiccollection.data.remote.dto.LanguageIndexDeltaDto
import com.pga.magiccollection.data.remote.dto.LanguageIndexInfoDto
import com.pga.magiccollection.data.remote.dto.LanguageIndexManifestDto
import com.pga.magiccollection.data.remote.dto.ScryfallCardDto
import com.pga.magiccollection.data.remote.dto.IndexVersionDto
import com.pga.magiccollection.data.remote.dto.MtgSetDto
import okhttp3.ResponseBody
import retrofit2.http.*

interface CardsApi {
    @GET("cards/search")
    suspend fun getCardByName(@Query("name") name: String, @Query("lang") lang: String?): ScryfallCardDto

    @GET("cards/library")
    suspend fun getAllKnownCards(): List<ScryfallCardDto>

    @GET("cards/index/version")
    suspend fun getIndexVersion(): IndexVersionDto

    @GET("cards/index/sync-status")
    suspend fun getSyncStatus(@Query("langs") langs: String? = null): CardSyncStatusDto

    @POST("cards/sync-full")
    suspend fun syncFullCatalog()

    @GET("cards/index/{lang}/page")
    suspend fun getIndexPageForLanguage(
        @Path("lang") lang: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int = 1000
    ): CardMetadataIndexPageDto

    @GET("cards/index/languages")
    suspend fun getIndexLanguages(): List<LanguageIndexInfoDto>

    @GET("cards/index/{lang}/manifest")
    suspend fun getLanguageManifest(@Path("lang") lang: String): LanguageIndexManifestDto

    @GET("cards/index/{lang}/delta")
    suspend fun getLanguageDelta(
        @Path("lang") lang: String,
        @Query("sinceVersion") sinceVersion: String
    ): LanguageIndexDeltaDto

    @Streaming
    @GET("cards/index/{lang}/snapshot")
    suspend fun downloadLanguageNamesSnapshot(
        @Path("lang") lang: String
    ): ResponseBody

    @GET("cards/discover")
    suspend fun searchCards(
        @Query("query") query: String?,
        @Query("colors") colors: String? = null,
        @Query("colorIdentity") colorIdentity: Boolean = false,
        @Query("colorLogic") colorLogic: String? = null,
        @Query("type") type: String? = null,
        @Query("text") text: String? = null,
        @Query("manaCost") manaCost: String? = null,
        @Query("set") set: String? = null,
        @Query("rarity") rarity: String? = null,
        @Query("artist") artist: String? = null,
        @Query("lang") lang: String? = null
    ): List<ScryfallCardDto>

    @GET("cards/autocomplete")
    suspend fun getAutocomplete(@Query("query") query: String): List<CardSuggestionDto>

    @GET("cards/random")
    suspend fun getRandomCard(): ScryfallCardDto

    @GET("cards/scryfall/{id}")
    suspend fun getCardByScryfallId(@Path("id") id: String, @Query("lang") lang: String?): ScryfallCardDto

    @GET("cards/{id}")
    suspend fun getCardById(@Path("id") id: Long): ScryfallCardDto

    @GET("cards/sets")
    suspend fun getSets(): List<MtgSetDto>
}
