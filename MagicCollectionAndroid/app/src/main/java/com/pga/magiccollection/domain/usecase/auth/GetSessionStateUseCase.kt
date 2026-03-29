package com.pga.magiccollection.domain.usecase.auth

import com.pga.magiccollection.data.repository.SessionRepository
import com.pga.magiccollection.data.repository.SessionState
import javax.inject.Inject

class GetSessionStateUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(): SessionState = sessionRepository.getSessionState()
}
