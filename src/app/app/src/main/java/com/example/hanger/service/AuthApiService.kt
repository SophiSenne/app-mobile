package com.hanger.app.data.network

import com.hanger.app.data.model.AuthResponse
import com.hanger.app.data.model.LoginRequest
import com.hanger.app.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>
}
