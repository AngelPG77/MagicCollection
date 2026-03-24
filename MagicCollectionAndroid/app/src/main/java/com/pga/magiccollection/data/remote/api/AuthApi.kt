package com.pga.magiccollection.data.remote.api

import com.pga.magiccollection.data.remote.dto.LoginRequestDto
import com.pga.magiccollection.data.remote.dto.LoginResponseDto
import com.pga.magiccollection.data.remote.dto.RegisterRequestDto
import com.pga.magiccollection.data.remote.dto.RegisterResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): RegisterResponseDto

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto
}

