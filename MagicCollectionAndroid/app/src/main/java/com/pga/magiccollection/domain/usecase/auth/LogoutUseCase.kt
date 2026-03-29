package com.pga.magiccollection.domain.usecase.auth

import com.pga.magiccollection.data.repository.SessionRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke() {
        sessionRepository.clearSession()
    }
}
