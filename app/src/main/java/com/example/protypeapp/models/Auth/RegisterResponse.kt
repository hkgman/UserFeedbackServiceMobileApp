package com.example.protypeapp.models.Auth

data class RegisterResponse(
    val msg: String,
    val user_id: Int? = null,
    val error: String? = null
)