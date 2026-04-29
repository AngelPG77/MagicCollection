package com.pga.magiccollection.data.repository

import com.pga.magiccollection.data.local.dao.UserDao
import com.pga.magiccollection.data.local.entities.UserEntity
import com.pga.magiccollection.data.local.security.SessionManager
import com.pga.magiccollection.data.remote.api.AuthApi
import com.pga.magiccollection.data.remote.dto.*

class AuthRepository(
    private val authApi: AuthApi,
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) {
    suspend fun register(username: String, password: String): String {
        val normalizedUsername = username.trim()
        require(normalizedUsername.isNotEmpty()) { "El username es obligatorio." }
        require(password.isNotBlank()) { "La password es obligatoria." }
        return authApi.register(RegisterRequestDto(normalizedUsername, password)).message
    }

    suspend fun login(username: String, password: String, rememberMe: Boolean = false) {
        val normalizedUsername = username.trim()
        require(normalizedUsername.isNotEmpty()) { "El username es obligatorio." }
        require(password.isNotBlank()) { "La password es obligatoria." }

        val response = authApi.login(LoginRequestDto(normalizedUsername, password, rememberMe))
        val token = response.token
        val userId = response.userId
        val refreshToken = response.refreshToken
        
        // Limpiar cualquier usuario previo con el mismo nombre pero distinto ID (poco probable, pero por seguridad)
        val existingUser = userDao.getUserByUsername(normalizedUsername)
        if (existingUser != null && existingUser.id != userId) {
            userDao.deleteUserById(existingUser.id)
        }

        userDao.insertUser(UserEntity(id = userId, username = normalizedUsername))
        sessionManager.saveSession(token, userId, normalizedUsername, refreshToken)
    }

    suspend fun ensureUserExists() {
        if (sessionManager.isUserLoggedIn()) {
            val userId = sessionManager.getUserId()
            val username = sessionManager.getUsername()
            if (userId != -1L && username != null) {
                if (!userDao.existsById(userId)) {
                    userDao.insertUser(UserEntity(id = userId, username = username))
                }
            }
        }
    }

    suspend fun updateUsername(newUsername: String): String {
        val response = authApi.updateUsername(UpdateUserRequestDto(newUsername))
        if (response.success) {
            val userId = sessionManager.getUserId()
            userDao.insertUser(UserEntity(id = userId, username = newUsername))
            // Guardar el nuevo token si viene en la respuesta
            val newToken = response.token ?: sessionManager.getAuthToken() ?: ""
            sessionManager.saveSession(
                token = newToken,
                userId = userId,
                username = newUsername,
                refreshToken = sessionManager.getRefreshToken()
            )
        }
        return response.message
    }

    suspend fun updatePassword(current: String, new: String): String {
        return authApi.updatePassword(UpdatePasswordRequestDto(current, new)).message
    }

    suspend fun deleteUser(): String {
        val response = authApi.deleteUser()
        if (response.success) {
            sessionManager.clearSession()
        }
        return response.message
    }
}
