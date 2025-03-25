package com.example.protypeapp.api

import okhttp3.Interceptor
import okhttp3.Response
import android.content.Context
import android.content.SharedPreferences

class AuthInterceptor(context: Context) : Interceptor {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sharedPreferences.getString("auth_token", null)

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

