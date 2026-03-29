package com.pga.magiccollection.domain.usecase.auth

import com.pga.magiccollection.data.repository.AuthRepository
import javax.inject.Inject

class DeleteUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): String {
        return authRepository.deleteUser()
    }
}
