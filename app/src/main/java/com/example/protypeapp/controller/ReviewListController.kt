package com.example.protypeapp.controller

import android.content.Context
import android.widget.Toast
import com.example.protypeapp.API.ApiClient
import com.example.protypeapp.API.ApiService
import com.example.protypeapp.controller.Listeners.ReviewListListener
import com.example.protypeapp.models.Review.Review
import com.example.protypeapp.userStorage.UserPreferences
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReviewListController(private val context: Context,private var  reviewListListener: ReviewListListener) {
    private val userPreferences = UserPreferences(context)
    private val apiService = ApiClient.getClient(context).create(ApiService::class.java)

    fun fetchPositiveFromServer(productId:Int) {
        val call = apiService.getPositiveReviews(productId)

        call.enqueue(object : Callback<List<Review>> {
            override fun onResponse(call: Call<List<Review>>, response: Response<List<Review>>) {
                if (response.isSuccessful) {
                    val itemsFromServer = response.body()?.toMutableList() ?: mutableListOf()
                    reviewListListener.onReviewsReceived(itemsFromServer)
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<List<Review>>, t: Throwable) {
                showMessage("Ошибка сети: ${t.message}")
            }
        })
    }
    fun fetchGenericFromServer(productId:Int) {
        val call = apiService.getGenericReviews(productId)

        call.enqueue(object : Callback<List<Review>> {
            override fun onResponse(call: Call<List<Review>>, response: Response<List<Review>>) {
                if (response.isSuccessful) {
                    val itemsFromServer = response.body()?.toMutableList() ?: mutableListOf()
                    reviewListListener.onReviewsReceived(itemsFromServer)
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<List<Review>>, t: Throwable) {
                showMessage("Ошибка сети: ${t.message}")
            }
        })
    }

    fun fetchNotGenericFromServer(productId:Int) {
        val call = apiService.getNotGenericReviews(productId)

        call.enqueue(object : Callback<List<Review>> {
            override fun onResponse(call: Call<List<Review>>, response: Response<List<Review>>) {
                if (response.isSuccessful) {
                    val itemsFromServer = response.body()?.toMutableList() ?: mutableListOf()
                    reviewListListener.onReviewsReceived(itemsFromServer)
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<List<Review>>, t: Throwable) {
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
        reviewListListener.onUnauthorized()
    }

    private fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}