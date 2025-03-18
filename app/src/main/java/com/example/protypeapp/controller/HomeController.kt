package com.example.protypeapp.controller

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import com.example.protypeapp.API.ApiClient
import com.example.protypeapp.API.ApiService
import com.example.protypeapp.MainActivity
import com.example.protypeapp.controller.Listeners.HomeListener
import com.example.protypeapp.models.Product.Product
import com.example.protypeapp.models.Product.ProductAdd
import com.example.protypeapp.models.User.UserInfo
import com.example.protypeapp.userStorage.UserPreferences
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeController(private val context: Context, private val listener: HomeListener) {
    private val apiService = ApiClient.getClient(context).create(ApiService::class.java)
    private val userPreferences = UserPreferences(context)

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

    fun fetchProducts() {
        val call = apiService.getProducts()

        call.enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful) {
                    val itemsFromServer = response.body()?.toMutableList() ?: mutableListOf()
                    listener.onProductsReceived(itemsFromServer)
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                showMessage("Ошибка сети: ${t.message}")
            }
        })
    }

    fun addProduct(newProduct: ProductAdd) {
        val call = apiService.addProduct(newProduct)

        call.enqueue(object : Callback<Product> {
            override fun onResponse(call: Call<Product>, response: Response<Product>) {
                if (response.isSuccessful) {
                    showMessage("Продукт добавлен")
                    fetchProducts()
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<Product>, t: Throwable) {
                showMessage("Ошибка сети: ${t.message}")
            }
        })
    }

    fun deleteProduct(product: Product) {
        val call = apiService.deleteProduct(product.id)

        call.enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    showMessage("Продукт удален")
                    fetchProducts()
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                showMessage("Ошибка сети: ${t.message}")
            }
        })
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

        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }

    private fun decodeBase64(base64String: String?): Bitmap? {
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

    private fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
