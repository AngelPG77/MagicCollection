package com.pga.magiccollection.domain.usecase

import com.pga.magiccollection.data.repository.SessionRepository
import com.pga.magiccollection.data.repository.SessionState

class GetSessionStateUseCase(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(): SessionState = sessionRepository.getSessionState()
}

