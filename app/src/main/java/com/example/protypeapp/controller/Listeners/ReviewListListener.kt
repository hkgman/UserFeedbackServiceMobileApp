package com.example.protypeapp.controller.Listeners

import com.example.protypeapp.models.Review.Review

interface ReviewListListener {
    fun onReviewsReceived(reviews: List<Review>,totalPages: Int)
    fun onUnauthorized()

    fun onError(message: String)
}