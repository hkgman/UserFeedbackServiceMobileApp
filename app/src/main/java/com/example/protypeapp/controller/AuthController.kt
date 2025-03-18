package com.example.protypeapp.controller

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.protypeapp.API.ApiClient
import com.example.protypeapp.API.AuthService
import com.example.protypeapp.HomeActivity
import com.example.protypeapp.MainActivity
import com.example.protypeapp.RegisterActivity
import com.example.protypeapp.models.Auth.LoginResponse
import com.example.protypeapp.models.Auth.RegisterRequest
import com.example.protypeapp.models.Auth.RegisterResponse
import com.example.protypeapp.models.Auth.UserRequest
import com.example.protypeapp.userStorage.UserPreferences
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthController(private val context: Context) {
    private val userPreferences = UserPreferences(context)
    private val apiService = ApiClient.getClient(context).create(AuthService::class.java)

    fun isUserLoggedIn():Boolean{
        return userPreferences.isLoggedIn()
    }

    fun loginUser(user:UserRequest){
        val call = apiService.login(user)

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody?.token != null) {
                        userPreferences.saveToken(responseBody.token)
                        userPreferences.saveLoginStatus(true)

                        showMessage("Успешный вход")

                        val intent = Intent(context, HomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        context.startActivity(intent)
                    } else {
                        showMessage(responseBody?.error ?: "Ошибка авторизации")
                    }
                } else {
                    val errorResponse = response.errorBody()?.string()
                    val jsonObject = JSONObject(errorResponse ?: "{}")
                    val errorMessage = jsonObject.optString("message", "Неизвестная ошибка")
                    showMessage(errorMessage)
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                showMessage("Ошибка сети: ${t.message}")
            }
        })
    }

    fun registerUser(newUser: RegisterRequest) {
        val call = apiService.register(newUser)

        call.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        if (responseBody.error == null) {
                            showMessage("Регистрация успешна")
                            val intent = Intent(context, MainActivity::class.java)
                            context.startActivity(intent)
                            if (context is RegisterActivity) {
                                context.finish()
                            }
                        } else {
                            showMessage("Ошибка: ${responseBody.error}")
                        }
                    }
                } else {
                    val errorResponse = response.errorBody()?.string()
                    val jsonObject = JSONObject(errorResponse!!)
                    val errorMessage = jsonObject.optString("message", "Неизвестная ошибка")
                    showMessage(errorMessage)
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                showMessage("Ошибка сети: ${t.message}")
            }
        })
    }

    private fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}