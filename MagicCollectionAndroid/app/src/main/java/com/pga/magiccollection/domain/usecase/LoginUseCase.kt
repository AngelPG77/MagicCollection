package com.pga.magiccollection.domain.usecase

import com.pga.magiccollection.data.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String) {
        authRepository.login(username, password)
    }
}

