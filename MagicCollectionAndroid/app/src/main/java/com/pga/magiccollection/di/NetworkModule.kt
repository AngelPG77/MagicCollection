package com.pga.magiccollection.di

import com.pga.magiccollection.BuildConfig
import com.pga.magiccollection.data.local.security.SessionManager
import com.pga.magiccollection.data.remote.AuthTokenInterceptor
import com.pga.magiccollection.data.remote.api.AuthApi
import com.pga.magiccollection.data.remote.api.CardsApi
import com.pga.magiccollection.data.remote.api.CollectionsApi
import com.pga.magiccollection.data.remote.api.InventoryApi
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

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            // Redactar tokens de autorización en los logs (seguridad)
            val sanitizedMessage = if (message.contains("Authorization:", ignoreCase = true)) {
                message.replace(Regex("Bearer [A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+"), "Bearer [REDACTED]")
            } else {
                message
            }
            android.util.Log.i("okhttp.OkHttpClient", sanitizedMessage)
        }.apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
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
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(
        retrofit: Retrofit,
        sessionManager: SessionManager,
        loggingInterceptor: HttpLoggingInterceptor
    ): AuthApi {
        // Create an authenticated client specifically for AuthApi requests that need it
        val authenticatedClient = OkHttpClient.Builder()
            .addInterceptor(AuthTokenInterceptor(sessionManager))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
        
        return retrofit.newBuilder()
            .client(authenticatedClient)
            .build()
            .create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCardsApi(
        retrofit: Retrofit,
        sessionManager: SessionManager,
        loggingInterceptor: HttpLoggingInterceptor
    ): CardsApi {
        val authenticatedClient = OkHttpClient.Builder()
            .addInterceptor(AuthTokenInterceptor(sessionManager))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
        
        return retrofit.newBuilder()
            .client(authenticatedClient)
            .build()
            .create(CardsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCollectionsApi(
        retrofit: Retrofit,
        sessionManager: SessionManager,
        loggingInterceptor: HttpLoggingInterceptor
    ): CollectionsApi {
        val authenticatedClient = OkHttpClient.Builder()
            .addInterceptor(AuthTokenInterceptor(sessionManager))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
        
        return retrofit.newBuilder()
            .client(authenticatedClient)
            .build()
            .create(CollectionsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideInventoryApi(
        retrofit: Retrofit,
        sessionManager: SessionManager,
        loggingInterceptor: HttpLoggingInterceptor
    ): InventoryApi {
        val authenticatedClient = OkHttpClient.Builder()
            .addInterceptor(AuthTokenInterceptor(sessionManager))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
        
        return retrofit.newBuilder()
            .client(authenticatedClient)
            .build()
            .create(InventoryApi::class.java)
    }
}
