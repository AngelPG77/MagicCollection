package com.pga.magiccollection.domain.usecase.auth

import com.pga.magiccollection.data.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException
import kotlin.test.assertFailsWith

class LoginUseCaseTest {

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val loginUseCase = LoginUseCase(authRepository)

    @Test
    fun `invoke delegates to repository with correct params`(): Unit = runTest {
        loginUseCase("alice", "pass123", true)

        coVerify { authRepository.login("alice", "pass123", true) }
    }

    @Test
    fun `invoke propagates exception when repository throws`() = runTest {
        coEvery { authRepository.login(any(), any(), any()) } throws IOException("network error")

        assertFailsWith<IOException> {
            loginUseCase("alice", "pass123", false)
        }
    }

    @Test
    fun `invoke defaults rememberMe to false`() = runTest {
        loginUseCase("alice", "pass123")

        coVerify { authRepository.login("alice", "pass123", false) }
    }
}
