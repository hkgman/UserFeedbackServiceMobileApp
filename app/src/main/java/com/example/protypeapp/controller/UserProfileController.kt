package com.example.protypeapp.controller

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.protypeapp.API.ApiClient
import com.example.protypeapp.API.ApiService
import com.example.protypeapp.controller.Listeners.UserProfileListener
import com.example.protypeapp.models.User.UserInfo
import com.example.protypeapp.userStorage.UserPreferences
import android.util.Base64
import android.widget.Toast
import com.example.protypeapp.models.User.UpdateUserRequest
import com.example.protypeapp.models.User.UpdateUserResponse
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class UserProfileController(private val context: Context, private val listener: UserProfileListener) {
    private val userPreferences = UserPreferences(context)
    private val apiService = ApiClient.getClient(context).create(ApiService::class.java)

    fun fetchUserInfo() {
        val call = apiService.getUser()

        call.enqueue(object : Callback<UserInfo> {
            override fun onResponse(call: Call<UserInfo>, response: Response<UserInfo>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        listener.onUserInfoReceived(user)

                        val imageBitmap = decodeBase64(user.image)
                        listener.onUserImageReceived(imageBitmap)
                    } else {
                        showMessage("Полученные данные пусты")
                    }
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<UserInfo>, t: Throwable) {
                showMessage("Ошибка сети: ${t.message}")
            }
        })
    }

    fun updateUser(fio: String, email: String, image: Bitmap?, password: String) {
        val fioParts = fio.split(" ")
        if (fioParts.size != 3) {
            showMessage("Введите ФИО в формате: Фамилия Имя Отчество")
            return
        }

        val surname = fioParts[0]
        val name = fioParts[1]
        val patronymic = fioParts[2]

        val imageBase64 = imageToBase64(image)

        val updateUserRequest = UpdateUserRequest(
            name = name,
            surname = surname,
            patronymic = patronymic,
            email = email,
            image = imageBase64,
            password = password
        )

        val call = apiService.updateUser(updateUserRequest)
        call.enqueue(object : Callback<UpdateUserResponse> {
            override fun onResponse(call: Call<UpdateUserResponse>, response: Response<UpdateUserResponse>) {
                if (response.isSuccessful) {
                    val updateResponse = response.body()
                    if (updateResponse?.user_id != null) {
                        showMessage(updateResponse.message)
                    } else {
                        showMessage("Не удалось обновить пользователя")
                    }
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<UpdateUserResponse>, t: Throwable) {
                showMessage("Ошибка сети: ${t.message}")
            }
        })
    }

    private fun imageToBase64(image: Bitmap?): String? {
        if (image == null) return null
        val outputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    fun decodeBase64(base64String: String?): Bitmap? {
        return try {
            if (base64String != null) {
                val options = BitmapFactory.Options()
                options.inSampleSize = 2
                val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
            } else {
                null
            }
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun handleErrorResponse(response: Response<*>) {
        if (response.code() == 401) {
            handleUnauthorizedError()
        } else {
            val errorResponse = response.errorBody()?.string()
            val jsonObject = JSONObject(errorResponse ?: "{}")
            val errorMessage = jsonObject.optString("message", "Неизвестная ошибка")
            showMessage(errorMessage)
        }
    }

    private fun handleUnauthorizedError() {
        userPreferences.logout()
        listener.onUnauthorized()
    }

    private fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

