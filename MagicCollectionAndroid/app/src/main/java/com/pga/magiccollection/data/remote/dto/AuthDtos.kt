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
    val token: String
)

