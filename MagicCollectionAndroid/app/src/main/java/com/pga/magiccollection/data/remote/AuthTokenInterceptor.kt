package com.pga.magiccollection.data.remote

import com.pga.magiccollection.data.local.security.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthTokenInterceptor(
    private val sessionManager: SessionManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sessionManager.getAuthToken()
        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()
        return chain.proceed(request)
    }
}

