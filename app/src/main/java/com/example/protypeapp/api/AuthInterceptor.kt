package com.example.protypeapp.api

import okhttp3.Interceptor
import okhttp3.Response
import android.content.Context
import android.content.SharedPreferences
import com.example.protypeapp.userStorage.UserPreferences

class AuthInterceptor(context: Context) : Interceptor {
    private val userPreferences = UserPreferences(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = userPreferences.getToken()

        val request = chain.request()
        val url = request.url().toString()

        if (url.contains("login") || url.contains("register")) {
            return chain.proceed(request)
        }
        val modifiedRequest = request.newBuilder().apply {
            token?.let {
                addHeader("Authorization", "Bearer $it")
            }
        }.build()

        return chain.proceed(modifiedRequest)
    }
}

