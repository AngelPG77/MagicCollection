package com.pga.magiccollection.data.local.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager(context: Context) {
    private val mainKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "session_prefs",
        mainKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_JWT_TOKEN = "jwt_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
    }

    fun saveSession(token: String, userId: Long, username: String, refreshToken: String? = null) {
        sharedPreferences.edit().apply {
            putString(KEY_JWT_TOKEN, token)
            putLong(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            if (refreshToken != null) {
                putString(KEY_REFRESH_TOKEN, refreshToken)
            }
            apply()
        }
    }

    fun getAuthToken(): String? = sharedPreferences.getString(KEY_JWT_TOKEN, null)

    fun getRefreshToken(): String? = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)

    fun getUserId(): Long = sharedPreferences.getLong(KEY_USER_ID, -1L)

    fun getUsername(): String? = sharedPreferences.getString(KEY_USERNAME, null)

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }

    fun isUserLoggedIn(): Boolean = getAuthToken() != null
}
