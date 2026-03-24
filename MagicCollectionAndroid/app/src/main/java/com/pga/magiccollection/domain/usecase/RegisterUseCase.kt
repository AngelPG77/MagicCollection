package com.pga.magiccollection.domain.usecase

import com.pga.magiccollection.data.repository.AuthRepository

class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String): String {
        return authRepository.register(username, password)
    }
}

