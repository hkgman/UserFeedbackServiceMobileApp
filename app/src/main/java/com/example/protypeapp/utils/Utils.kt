package com.example.protypeapp.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

object Utils {
    fun decodeBase64(base64String: String?): Bitmap? {
        return try {
            if (base64String != null) {
                val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            } else {
                null
            }
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun imageToBase64(image: Bitmap?): String? {
        if (image == null) return null
        val outputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }
}