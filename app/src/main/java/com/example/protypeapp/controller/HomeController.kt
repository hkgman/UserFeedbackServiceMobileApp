package com.example.protypeapp.controller

import android.content.Context
import com.example.protypeapp.api.ApiClient
import com.example.protypeapp.api.ApiService
import com.example.protypeapp.controller.listeners.HomeListener
import com.example.protypeapp.models.product.Product
import com.example.protypeapp.models.product.ProductAdd
import com.example.protypeapp.models.user.UserInfo
import com.example.protypeapp.userStorage.UserPreferences
import com.example.protypeapp.utils.Utils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeController(private val context: Context, private val listener: HomeListener) {
    private val apiService = ApiClient.getClient(context).create(ApiService::class.java)
    private val userPreferences = UserPreferences(context)

    fun checkAuthorization() {
        if (!userPreferences.isLoggedIn()) {
            listener.onUserNotAuthorized()
        }
    }

    fun fetchUserInfo() {
        val call = apiService.getUser()

        call.enqueue(object : Callback<UserInfo> {
            override fun onResponse(call: Call<UserInfo>, response: Response<UserInfo>) {
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        listener.onUserInfoReceived(user)
                        listener.onUserImageReceived(Utils.decodeBase64(user.image))
                    } ?: listener.onError("Полученные данные пусты")
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<UserInfo>, t: Throwable) {
                listener.onError("Ошибка сети: ${t.message}")
            }
        })
    }

    fun fetchProducts() {
        val call = apiService.getProducts()

        call.enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful) {
                    listener.onProductsReceived(response.body()?.toMutableList() ?: mutableListOf())
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                listener.onError("Ошибка сети: ${t.message}")
            }
        })
    }

    fun addProduct(newProduct: ProductAdd) {
        val call = apiService.addProduct(newProduct)

        call.enqueue(object : Callback<Product> {
            override fun onResponse(call: Call<Product>, response: Response<Product>) {
                if (response.isSuccessful) {
                    listener.onProductAdded()
                    fetchProducts()
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<Product>, t: Throwable) {
                listener.onError("Ошибка сети: ${t.message}")
            }
        })
    }

    fun deleteProduct(product: Product) {
        val call = apiService.deleteProduct(product.id)

        call.enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    listener.onProductDeleted()
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                listener.onError("Ошибка сети: ${t.message}")
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
            listener.onError(errorMessage)
        }
    }
    private fun handleUnauthorizedError() {
        userPreferences.logout()
        listener.onUserNotAuthorized()
    }
}
