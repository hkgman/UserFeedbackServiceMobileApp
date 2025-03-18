package com.example.protypeapp.models.Product

class Product (
    val id: Int,
    val product_name: String,
    val supplier_article: String,
    val supplier_name: String = "Default Supplier",
    val brand_name: String = "Default Brand",
    val product_url: String,
    val status: String,
    val image_url: String
)
