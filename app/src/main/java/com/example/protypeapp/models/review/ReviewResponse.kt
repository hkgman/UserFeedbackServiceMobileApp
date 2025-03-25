package com.example.protypeapp.models.review

data class ReviewResponse(
    val reviews: List<Review>,
    val total: Int,
    val pages: Int,
    val currentPage: Int,
)