package com.pga.magiccollection.domain.usecase.auth

import com.pga.magiccollection.data.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String): String {
        return authRepository.register(username, password)
    }
}
