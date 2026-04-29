package com.pga.magiccollection.domain.usecase.auth

import com.pga.magiccollection.data.repository.SessionRepository
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

class ObserveSessionExpiredUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(): SharedFlow<Unit> = sessionRepository.sessionExpiredEvent
}
