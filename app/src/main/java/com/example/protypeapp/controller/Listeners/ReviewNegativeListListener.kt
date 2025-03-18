package com.example.protypeapp.controller.Listeners

import com.example.protypeapp.models.Review.ReviewN

interface ReviewNegativeListListener {
    fun onReviewsReceived(reviews: List<ReviewN>)
    fun onUnauthorized()
}