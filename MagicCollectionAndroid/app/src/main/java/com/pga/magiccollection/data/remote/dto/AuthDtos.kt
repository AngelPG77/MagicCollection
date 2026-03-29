package com.pga.magiccollection.data.remote.dto

data class RegisterRequestDto(
    val username: String,
    val password: String
)

data class RegisterResponseDto(
    val message: String
)

data class LoginRequestDto(
    val username: String,
    val password: String
)

data class LoginResponseDto(
    val token: String,
    val userId: Long
)

data class UpdateUserRequestDto(
    val newUsername: String
)

data class UpdateUserResponseDto(
    val success: Boolean,
    val message: String
)

data class UpdatePasswordRequestDto(
    val currentPassword: String,
    val newPassword: String
)

data class UpdatePasswordResponseDto(
    val success: Boolean,
    val message: String
)

data class DeleteUserResponseDto(
    val success: Boolean,
    val message: String
)
