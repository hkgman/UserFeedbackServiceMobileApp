package com.example.protypeapp.controller


import android.content.Context
import android.widget.Toast
import com.example.protypeapp.API.ApiClient
import com.example.protypeapp.API.ApiService
import com.example.protypeapp.controller.Listeners.ReviewNegativeListListener
import com.example.protypeapp.models.Review.ReviewNResponse
import com.example.protypeapp.userStorage.UserPreferences
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReviewNegativeListController(private val context: Context,private var  reviewListListener: ReviewNegativeListListener) {
    private val userPreferences = UserPreferences(context)
    private val apiService = ApiClient.getClient(context).create(ApiService::class.java)

    fun fetchNegativeFromServer(productId: Int, page: Int = 1, perPage: Int = 10) {
        val call = apiService.getNegativeReviews(productId, page, perPage)

        call.enqueue(object : Callback<ReviewNResponse> {
            override fun onResponse(call: Call<ReviewNResponse>, response: Response<ReviewNResponse>) {
                if (response.isSuccessful) {
                    val reviewResponse = response.body()
                    val reviews = reviewResponse?.reviews ?: emptyList()
                    if (reviewResponse != null) {
                        reviewListListener.onReviewsReceived(reviews,reviewResponse.total)
                    }
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<ReviewNResponse>, t: Throwable) {
                reviewListListener.onError("Ошибка сети: ${t.message}")
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
            reviewListListener.onError(errorMessage)
        }
    }

    private fun handleUnauthorizedError() {
        userPreferences.logout()
        reviewListListener.onUnauthorized()
    }

}