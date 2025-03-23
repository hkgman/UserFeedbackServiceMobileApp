package com.example.protypeapp.controller

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.protypeapp.API.ApiClient
import com.example.protypeapp.API.AuthService
import com.example.protypeapp.HomeActivity
import com.example.protypeapp.MainActivity
import com.example.protypeapp.RegisterActivity
import com.example.protypeapp.controller.Listeners.AuthListener
import com.example.protypeapp.models.Auth.LoginResponse
import com.example.protypeapp.models.Auth.RegisterRequest
import com.example.protypeapp.models.Auth.RegisterResponse
import com.example.protypeapp.models.Auth.UserRequest
import com.example.protypeapp.userStorage.UserPreferences
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthController(private val context: Context,private val authListener: AuthListener) {
    private val userPreferences = UserPreferences(context)
    private val apiService = ApiClient.getClient(context).create(AuthService::class.java)

    fun isUserLoggedIn():Boolean{
        return userPreferences.isLoggedIn()
    }

    fun loginUser(login:String,password: String){
        val errorMessage = validateLoginData(login, password)
        if (errorMessage != null) {
            authListener.onMessage(errorMessage)
            return
        }

        val user = UserRequest(
            email = login,
            password = password
        )
        val call = apiService.login(user)

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody?.token != null) {
                        userPreferences.saveToken(responseBody.token)
                        userPreferences.saveLoginStatus(true)

                        authListener.onMessage("Успешный вход")

                        val intent = Intent(context, HomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        context.startActivity(intent)
                    } else {
                        authListener.onMessage(responseBody?.error ?: "Ошибка авторизации")
                    }
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                authListener.onMessage("Ошибка сети: ${t.message}")
            }
        })
    }
    private fun validateLoginData(
        email: String,
        password: String,
    ): String? {
        return when {
            email.isEmpty() -> "Введите email"
            password.isEmpty() -> "Введите пароль"
            else -> null
        }
    }

    private fun validateRegistrationData(
        email: String,
        fullName: String,
        password: String,
        confirmPassword: String
    ): String? {
        return when {
            email.isEmpty() -> "Введите email"
            fullName.isEmpty() -> "Введите полное имя (Фамилия Имя Отчество)"
            password.isEmpty() -> "Введите пароль"
            confirmPassword.isEmpty() -> "Повторите пароль"
            password != confirmPassword -> "Пароли не совпадают"
            fullName.split(" ").size < 3 -> "Введите полное имя (Фамилия Имя Отчество)"
            else -> null
        }
    }


    fun registerUser(email: String, fullName: String, password: String, confirmPassword: String) {
        val errorMessage = validateRegistrationData(email, fullName, password, confirmPassword)
        if (errorMessage != null) {
            authListener.onMessage(errorMessage)
            return
        }

        val nameParts = fullName.split(" ")
        val newUser = RegisterRequest(
            surname = nameParts[0],
            name = nameParts[1],
            patronymic = nameParts[2],
            email = email,
            password = password
        )

        val call = apiService.register(newUser)

        call.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody?.error == null) {
                        authListener.onMessage("Регистрация успешна")
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)
                        if (context is RegisterActivity) {
                            context.finish()
                        }
                    } else {
                        authListener.onMessage("Ошибка: ${responseBody.error}")
                    }
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                authListener.onMessage("Ошибка сети: ${t.message}")
            }
        })
    }


    private fun handleErrorResponse(response: Response<*>) {
        val errorResponse = response.errorBody()?.string()
        val jsonObject = JSONObject(errorResponse!!)
        val errorMessage = jsonObject.optString("message", "Неизвестная ошибка")
        authListener.onMessage(errorMessage)
    }

}