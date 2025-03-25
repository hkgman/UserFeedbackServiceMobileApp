package com.example.protypeapp.controller.listeners

import android.graphics.Bitmap
import com.example.protypeapp.models.product.Product
import com.example.protypeapp.models.user.UserInfo

interface HomeListener {
    fun onUserInfoReceived(user: UserInfo)
    fun onUserImageReceived(bitmap: Bitmap?)
    fun onProductsReceived(products: List<Product>)
    fun onProductAdded()
    fun onProductDeleted()
    fun onUserNotAuthorized()
    fun onError(message: String)
}