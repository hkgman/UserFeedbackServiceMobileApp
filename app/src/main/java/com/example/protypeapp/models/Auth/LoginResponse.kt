package com.example.protypeapp.models.Auth

data class LoginResponse(
    val msg: String,
    val token: String? = null,
    val error: String? = null
)