package com.example.protypeapp.models.auth

data class RegisterRequest(
    val surname: String,
    val name: String,
    val email: String,
    val patronymic: String,
    val password: String
)