package com.example.protypeapp.controller.Listeners

import android.graphics.Bitmap
import com.example.protypeapp.models.User.UserInfo

interface UserProfileListener {
    fun onUserInfoReceived(user: UserInfo)
    fun onUserImageReceived(image: Bitmap?)
    fun showMessage(message: String)
    fun showError(message: String)
    fun onUnauthorized()
}