package com.example.protypeapp.controller.listeners

import com.example.protypeapp.models.review.Review

interface ReviewListListener {
    fun onReviewsReceived(reviews: List<Review>,totalPages: Int)
    fun onUnauthorized()

    fun onError(message: String)
}