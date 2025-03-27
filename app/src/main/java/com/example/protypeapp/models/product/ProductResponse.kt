package com.example.protypeapp.models.product

data class ProductResponse(
    val products: List<Product>,
    val total: Int,
    val pages: Int,
    val currentPage: Int,
)