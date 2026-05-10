package com.pga.magiccollection.di

import com.pga.magiccollection.BuildConfig
import com.pga.magiccollection.data.local.security.SessionManager
import com.pga.magiccollection.data.remote.AuthInterceptor
import com.pga.magiccollection.data.remote.api.*
import com.pga.magiccollection.data.repository.SessionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            // Redact authorization tokens in logs (security)
            val sanitizedMessage = if (message.contains("Authorization:", ignoreCase = true)) {
                message.replace(Regex("Bearer [A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+"), "Bearer [REDACTED]")
            } else {
                message
            }
            android.util.Log.i("okhttp.OkHttpClient", sanitizedMessage)
        }.apply {
            level = if (BuildConfig.DEBUG) {
                // Changed from BODY to HEADERS to avoid OOM with large responses (like the card index)
                HttpLoggingInterceptor.Level.HEADERS
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        }
    }

    @Provides
    @Singleton
    @Named("BaseClient")
    fun provideBaseOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .build()
    }

    @Provides
    @Singleton
    @Named("AuthenticatedClient")
    fun provideAuthenticatedOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        sessionManager: SessionManager,
        sessionRepository: SessionRepository,
        authApiProvider: javax.inject.Provider<AuthApi>
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager, sessionRepository, authApiProvider))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        @Named("BaseClient") okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(
        retrofit: Retrofit,
        @Named("AuthenticatedClient") authenticatedClient: OkHttpClient
    ): AuthApi {
        return retrofit.newBuilder()
            .client(authenticatedClient)
            .build()
            .create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCardsApi(
        retrofit: Retrofit,
        @Named("AuthenticatedClient") authenticatedClient: OkHttpClient
    ): CardsApi {
        return retrofit.newBuilder()
            .client(authenticatedClient)
            .build()
            .create(CardsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCollectionsApi(
        retrofit: Retrofit,
        @Named("AuthenticatedClient") authenticatedClient: OkHttpClient
    ): CollectionsApi {
        return retrofit.newBuilder()
            .client(authenticatedClient)
            .build()
            .create(CollectionsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWantListApi(
        retrofit: Retrofit,
        @Named("AuthenticatedClient") authenticatedClient: OkHttpClient
    ): WantListApi {
        return retrofit.newBuilder()
            .client(authenticatedClient)
            .build()
            .create(WantListApi::class.java)
    }
}
