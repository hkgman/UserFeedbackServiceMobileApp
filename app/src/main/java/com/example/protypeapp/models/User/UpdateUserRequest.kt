package com.example.protypeapp.models.User

data class UpdateUserRequest(
    val name: String?,
    val surname: String?,
    val patronymic: String?,
    val email: String?,
    val image: String?,
    val password: String?
)