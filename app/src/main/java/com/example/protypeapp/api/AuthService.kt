package com.example.protypeapp.api

import com.example.protypeapp.models.auth.LoginResponse
import com.example.protypeapp.models.auth.RegisterRequest
import com.example.protypeapp.models.auth.RegisterResponse
import com.example.protypeapp.models.auth.UserRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("/auth/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("/auth/login")
    fun login(@Body request: UserRequest): Call<LoginResponse>
}