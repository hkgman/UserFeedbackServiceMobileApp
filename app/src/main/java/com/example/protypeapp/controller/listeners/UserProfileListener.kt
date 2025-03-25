package com.example.protypeapp.controller.listeners

import android.graphics.Bitmap
import com.example.protypeapp.models.user.UserInfo

interface UserProfileListener {
    fun onUserInfoReceived(user: UserInfo)
    fun onUserImageReceived(image: Bitmap?)
    fun showMessage(message: String)
    fun showError(message: String)
    fun onUnauthorized()
}