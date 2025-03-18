package com.example.protypeapp.API

import com.example.protypeapp.models.Auth.LoginResponse
import com.example.protypeapp.models.Auth.RegisterRequest
import com.example.protypeapp.models.Auth.RegisterResponse
import com.example.protypeapp.models.Auth.UserRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("/auth/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("/auth/login")
    fun login(@Body request: UserRequest): Call<LoginResponse>
}