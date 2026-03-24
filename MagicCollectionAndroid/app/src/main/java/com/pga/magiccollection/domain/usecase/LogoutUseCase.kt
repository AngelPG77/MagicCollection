package com.pga.magiccollection.domain.usecase

import com.pga.magiccollection.data.repository.SessionRepository

class LogoutUseCase(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke() {
        sessionRepository.clearSession()
    }
}

