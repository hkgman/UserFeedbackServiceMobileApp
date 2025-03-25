package com.example.protypeapp.controller

import android.content.Context
import android.graphics.Bitmap
import com.example.protypeapp.api.ApiClient
import com.example.protypeapp.api.ApiService
import com.example.protypeapp.controller.listeners.UserProfileListener
import com.example.protypeapp.models.user.UpdateUserRequest
import com.example.protypeapp.models.user.UpdateUserResponse
import com.example.protypeapp.models.user.UserInfo
import com.example.protypeapp.userStorage.UserPreferences
import com.example.protypeapp.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserProfileController(private val context: Context, private val listener: UserProfileListener) {
    private val userPreferences = UserPreferences(context)
    private val apiService = ApiClient.getClient(context).create(ApiService::class.java)

    fun checkAuthorization() {
        if (!userPreferences.isLoggedIn()) {
            listener.onUnauthorized()
        }
    }

    fun fetchUserInfo() {
        apiService.getUser().enqueue(object : Callback<UserInfo> {
            override fun onResponse(call: Call<UserInfo>, response: Response<UserInfo>) {
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        listener.onUserInfoReceived(user)
                        listener.onUserImageReceived(Utils.decodeBase64(user.image))
                    } ?: listener.showError("Полученные данные пусты")
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<UserInfo>, t: Throwable) {
                listener.showError("Ошибка сети: ${t.message}")
            }
        })
    }

    fun updateUser(fio: String, email: String, image: Bitmap?, password: String) {
        val fioParts = fio.split(" ")
        if (fioParts.size != 3) {
            listener.showError("Введите ФИО в формате: Фамилия Имя Отчество")
            return
        }

        val imageBase64 = Utils.imageToBase64(image)
        val updateUserRequest = UpdateUserRequest(fioParts[1], fioParts[0], fioParts[2], email, imageBase64, password)

        apiService.updateUser(updateUserRequest).enqueue(object : Callback<UpdateUserResponse> {
            override fun onResponse(call: Call<UpdateUserResponse>, response: Response<UpdateUserResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        listener.showMessage(it.message)
                    } ?: listener.showError("Не удалось обновить пользователя")
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<UpdateUserResponse>, t: Throwable) {
                listener.showError("Ошибка сети: ${t.message}")
            }
        })
    }

    private fun handleErrorResponse(response: Response<*>) {
        if (response.code() == 401) {
            userPreferences.logout()
            listener.onUnauthorized()
        } else {
            listener.showError("Неизвестная ошибка: ${response.message()}")
        }
    }
}
