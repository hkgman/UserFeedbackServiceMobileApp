package com.example.protypeapp.models.review

class Review (
    val createdDate: String,
    val isGeneric: Boolean,
    val isPositive: Boolean? = null,
    val mark: Int,
    val problem: String,
    val text: String,
    val userName: String
)