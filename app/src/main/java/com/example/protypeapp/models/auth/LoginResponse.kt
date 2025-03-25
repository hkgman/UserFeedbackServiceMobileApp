package com.example.protypeapp.models.auth

data class LoginResponse(
    val msg: String,
    val token: String? = null,
    val error: String? = null
)