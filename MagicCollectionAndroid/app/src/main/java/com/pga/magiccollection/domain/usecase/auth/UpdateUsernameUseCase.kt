package com.pga.magiccollection.domain.usecase.auth

import com.pga.magiccollection.data.repository.AuthRepository
import javax.inject.Inject

class UpdateUsernameUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(newUsername: String): String {
        return authRepository.updateUsername(newUsername)
    }
}
