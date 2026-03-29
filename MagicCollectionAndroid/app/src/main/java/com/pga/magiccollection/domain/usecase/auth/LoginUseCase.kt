package com.pga.magiccollection.domain.usecase.auth

import com.pga.magiccollection.data.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String) {
        authRepository.login(username, password)
    }
}
