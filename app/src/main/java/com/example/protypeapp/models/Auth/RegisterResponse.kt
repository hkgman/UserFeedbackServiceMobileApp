package com.example.protypeapp.models.Auth

data class RegisterResponse(
    val msg: String,
    val userId: Int? = null,
    val error: String? = null
)