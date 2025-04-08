package com.example.protypeapp.models.user

data class UpdateUserResponse(
    val message: String,
    val userId: Int?,
    val token: String? = null
)