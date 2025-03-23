package com.example.protypeapp.models.Review

data class ReviewNResponse(
    val reviews: List<ReviewN>,
    val total: Int,
    val pages: Int,
    val currentPage: Int
)