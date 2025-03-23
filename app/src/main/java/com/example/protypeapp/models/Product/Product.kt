package com.example.protypeapp.models.Product

data class Product (
    val id: Int,
    val productName: String,
    val supplierArticle: String,
    val supplierName: String = "Default Supplier",
    val brandName: String = "Default Brand",
    val productUrl: String,
    val status: String,
    val imageUrl: String
)
