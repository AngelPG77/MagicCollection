package com.pga.magiccollection.domain.usecase.auth

import app.cash.turbine.test
import com.pga.magiccollection.data.repository.SessionRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ObserveSessionExpiredUseCaseTest {

    private val sessionExpiredFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val sessionRepository: SessionRepository = mockk {
        every { sessionExpiredEvent } returns sessionExpiredFlow
    }
    private val useCase = ObserveSessionExpiredUseCase(sessionRepository)

    @Test
    fun `invoke exposes sessionExpiredEvent from repository`() = runTest {
        useCase().test {
            sessionExpiredFlow.emit(Unit)
            awaitItem()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke does not emit when no session expiry occurs`() = runTest {
        useCase().test {
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
