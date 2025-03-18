package com.example.protypeapp.controller.Listeners

import android.graphics.Bitmap
import com.example.protypeapp.models.Product.Product
import com.example.protypeapp.models.User.UserInfo

interface HomeListener {
    fun onUserInfoReceived(user: UserInfo)
    fun onUserImageReceived(bitmap: Bitmap?)
    fun onProductsReceived(products: List<Product>)
}