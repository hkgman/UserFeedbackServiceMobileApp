package com.example.protypeapp.models.Review

data class ReviewResponse(
    val reviews: List<Review>,
    val total: Int,
    val pages: Int,
    val current_page: Int
)