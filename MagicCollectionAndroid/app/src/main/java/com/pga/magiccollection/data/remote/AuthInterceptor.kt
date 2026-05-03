package com.pga.magiccollection.data.remote

import com.pga.magiccollection.data.local.security.SessionManager
import com.pga.magiccollection.data.remote.api.AuthApi
import com.pga.magiccollection.data.repository.SessionRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Provider

class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager,
    private val sessionRepository: SessionRepository,
    private val authApiProvider: Provider<AuthApi> // Use Provider to avoid circular dependency
) : Interceptor {
    companion object {
        private const val AUTH_LOGIN_PATH = "/auth/login"
        private const val AUTH_REGISTER_PATH = "/auth/register"
        private const val AUTH_REFRESH_PATH = "/auth/refresh-token"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestPath = originalRequest.url.encodedPath
        val token = sessionManager.getAuthToken()
        val isPublicAuthEndpoint = requestPath == AUTH_LOGIN_PATH ||
                requestPath == AUTH_REGISTER_PATH ||
                requestPath == AUTH_REFRESH_PATH
        val isRefreshEndpoint = requestPath == AUTH_REFRESH_PATH
        
        val requestBuilder = originalRequest.newBuilder()
        if (!isPublicAuthEndpoint && !token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        
        var response = chain.proceed(requestBuilder.build())
        
        if (response.code == 401 && !isRefreshEndpoint) {
            val refreshToken = sessionManager.getRefreshToken()
            if (!refreshToken.isNullOrBlank()) {
                // Try to refresh token
                synchronized(this) {
                    val currentToken = sessionManager.getAuthToken()
                    // If token was already refreshed by another thread, retry original request
                    if (!currentToken.isNullOrBlank() && currentToken != token) {
                        response.close()
                        return chain.proceed(originalRequest.newBuilder()
                            .header("Authorization", "Bearer $currentToken")
                            .build())
                    }

                    try {
                        val refreshResponse = runBlocking {
                            authApiProvider.get().refreshToken(mapOf("refreshToken" to refreshToken))
                        }
                        
                        // Save new tokens
                        sessionManager.saveSession(
                            token = refreshResponse.token,
                            userId = sessionManager.getUserId(),
                            username = sessionManager.getUsername() ?: "",
                            refreshToken = refreshResponse.refreshToken ?: refreshToken
                        )

                        // Retry original request with new token
                        response.close()
                        val newRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer ${refreshResponse.token}")
                            .build()
                        response = chain.proceed(newRequest)
                    } catch (e: Exception) {
                        // Refresh failed, notify session expired
                        if (sessionManager.isUserLoggedIn()) {
                            sessionRepository.notifySessionExpired()
                        }
                    }
                }
            } else {
                // No refresh token, notify session expired
                if (sessionManager.isUserLoggedIn()) {
                    sessionRepository.notifySessionExpired()
                }
            }
        }
        
        return response
    }
}
