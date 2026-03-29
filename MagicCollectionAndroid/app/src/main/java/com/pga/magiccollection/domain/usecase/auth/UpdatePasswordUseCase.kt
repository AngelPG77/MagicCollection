package com.pga.magiccollection.domain.usecase.auth

import com.pga.magiccollection.data.repository.AuthRepository
import javax.inject.Inject

class UpdatePasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(current: String, new: String): String {
        return authRepository.updatePassword(current, new)
    }
}
