package com.pga.magiccollection.di

import com.pga.magiccollection.data.local.security.SessionManager
import com.pga.magiccollection.data.remote.AuthTokenInterceptor
import com.pga.magiccollection.data.remote.api.AuthApi
import com.pga.magiccollection.data.remote.api.CardsApi
import com.pga.magiccollection.data.remote.api.CollectionsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCardsApi(
        retrofit: Retrofit,
        sessionManager: SessionManager
    ): CardsApi {
        val authenticatedClient = retrofit.callFactory() as OkHttpClient
        val newClient = authenticatedClient.newBuilder()
            .addInterceptor(AuthTokenInterceptor(sessionManager))
            .build()
        
        return retrofit.newBuilder()
            .client(newClient)
            .build()
            .create(CardsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCollectionsApi(
        retrofit: Retrofit,
        sessionManager: SessionManager
    ): CollectionsApi {
        val authenticatedClient = retrofit.callFactory() as OkHttpClient
        val newClient = authenticatedClient.newBuilder()
            .addInterceptor(AuthTokenInterceptor(sessionManager))
            .build()
        
        return retrofit.newBuilder()
            .client(newClient)
            .build()
            .create(CollectionsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideInventoryApi(
        retrofit: Retrofit,
        sessionManager: SessionManager
    ): InventoryApi {
        val authenticatedClient = retrofit.callFactory() as OkHttpClient
        val newClient = authenticatedClient.newBuilder()
            .addInterceptor(AuthTokenInterceptor(sessionManager))
            .build()
        
        return retrofit.newBuilder()
            .client(newClient)
            .build()
            .create(InventoryApi::class.java)
    }
}
