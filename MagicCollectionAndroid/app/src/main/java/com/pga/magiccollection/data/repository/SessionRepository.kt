package com.pga.magiccollection.data.repository

import com.pga.magiccollection.data.local.security.SessionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

import javax.inject.Inject

data class SessionState(
    val isLoggedIn: Boolean,
    val userId: Long,
    val username: String
)

class SessionRepository @Inject constructor(
    private val sessionManager: SessionManager
) {
    private val _sessionExpiredEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpiredEvent: SharedFlow<Unit> = _sessionExpiredEvent.asSharedFlow()

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

    fun notifySessionExpired() {
        sessionManager.clearSession()
        _sessionExpiredEvent.tryEmit(Unit)
    }
}

