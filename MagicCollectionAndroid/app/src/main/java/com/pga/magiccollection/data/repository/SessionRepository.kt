package com.pga.magiccollection.data.repository

import com.pga.magiccollection.data.local.security.SessionManager

data class SessionState(
    val isLoggedIn: Boolean,
    val userId: Long,
    val username: String
)

class SessionRepository(
    private val sessionManager: SessionManager
) {
    fun getSessionState(): SessionState {
        return SessionState(
            isLoggedIn = sessionManager.isUserLoggedIn(),
            userId = sessionManager.getUserId(),
            username = sessionManager.getUsername().orEmpty()
        )
    }

    fun clearSession() {
        sessionManager.clearSession()
    }
}

