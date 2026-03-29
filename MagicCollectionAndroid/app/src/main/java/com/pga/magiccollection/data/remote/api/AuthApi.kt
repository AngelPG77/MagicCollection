package com.pga.magiccollection.data.remote.api

import com.pga.magiccollection.data.remote.dto.*
import retrofit2.http.*

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): RegisterResponseDto

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @PUT("auth/update-username")
    suspend fun updateUsername(@Body request: UpdateUserRequestDto): UpdateUserResponseDto

    @PUT("auth/update-password")
    suspend fun updatePassword(@Body request: UpdatePasswordRequestDto): UpdatePasswordResponseDto

    @DELETE("auth/delete")
    suspend fun deleteUser(): DeleteUserResponseDto
}
