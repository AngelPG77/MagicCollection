package com.pga.magiccollection.data.repository

import com.pga.magiccollection.data.local.dao.UserDao
import com.pga.magiccollection.data.local.entities.UserEntity
import com.pga.magiccollection.data.local.security.SessionManager
import com.pga.magiccollection.data.remote.api.AuthApi
import com.pga.magiccollection.data.remote.dto.LoginRequestDto
import com.pga.magiccollection.data.remote.dto.RegisterRequestDto

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

    suspend fun login(username: String, password: String) {
        val normalizedUsername = username.trim()
        require(normalizedUsername.isNotEmpty()) { "El username es obligatorio." }
        require(password.isNotBlank()) { "La password es obligatoria." }

        val token = authApi.login(LoginRequestDto(normalizedUsername, password)).token
        val existingUser = userDao.getUserByUsername(normalizedUsername)
        val userId = existingUser?.id ?: (System.currentTimeMillis() / 1000L)
        if (existingUser == null) {
            userDao.insertUser(UserEntity(id = userId, username = normalizedUsername))
        }
        sessionManager.saveSession(token, userId, normalizedUsername)
    }
}

