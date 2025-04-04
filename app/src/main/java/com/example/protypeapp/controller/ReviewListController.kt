package com.example.protypeapp.controller

import android.content.Context
import com.example.protypeapp.api.ApiClient
import com.example.protypeapp.api.ApiService
import com.example.protypeapp.controller.listeners.ReviewListListener
import com.example.protypeapp.models.review.ReviewResponse
import com.example.protypeapp.userStorage.UserPreferences
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReviewListController(private val context: Context,private var  reviewListListener: ReviewListListener) {
    private val userPreferences = UserPreferences(context)
    private val apiService = ApiClient.getClient(context).create(ApiService::class.java)

    fun checkAuthorization() {
        if (!userPreferences.isLoggedIn()) {
            reviewListListener.onUnauthorized()
        }
    }
    fun fetchAllFromServer(productId: Int, page: Int = 1, perPage: Int = 10,searchString: String) {
        val call = apiService.getAllReviews(productId, page, perPage,searchString)

        call.enqueue(object : Callback<ReviewResponse> {
            override fun onResponse(call: Call<ReviewResponse>, response: Response<ReviewResponse>) {
                if (response.isSuccessful) {
                    val reviewResponse = response.body()
                    val reviews = reviewResponse?.reviews ?: emptyList()
                    if (reviewResponse != null) {
                        reviewListListener.onReviewsReceived(reviews,reviewResponse.total,reviewResponse.currentPage)
                    }
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<ReviewResponse>, t: Throwable) {
                reviewListListener.onError("Ошибка сети: ${t.message}")
            }
        })
    }

    fun fetchPositiveFromServer(productId: Int, page: Int = 1, perPage: Int = 10,searchString: String) {
        val call = apiService.getPositiveReviews(productId, page, perPage,searchString)

        call.enqueue(object : Callback<ReviewResponse> {
            override fun onResponse(call: Call<ReviewResponse>, response: Response<ReviewResponse>) {
                if (response.isSuccessful) {
                    val reviewResponse = response.body()
                    val reviews = reviewResponse?.reviews ?: emptyList()
                    if (reviewResponse != null) {
                        reviewListListener.onReviewsReceived(reviews,reviewResponse.total,reviewResponse.currentPage)
                    }
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<ReviewResponse>, t: Throwable) {
                reviewListListener.onError("Ошибка сети: ${t.message}")
            }
        })
    }
    fun fetchGenericFromServer(productId: Int, page: Int = 1, perPage: Int = 10,searchString: String) {
        val call = apiService.getGenericReviews(productId, page, perPage,searchString)

        call.enqueue(object : Callback<ReviewResponse> {
            override fun onResponse(call: Call<ReviewResponse>, response: Response<ReviewResponse>) {
                if (response.isSuccessful) {
                    val reviewResponse = response.body()
                    val reviews = reviewResponse?.reviews ?: emptyList()
                    if (reviewResponse != null) {
                        reviewListListener.onReviewsReceived(reviews,reviewResponse.total,reviewResponse.currentPage)
                    }
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<ReviewResponse>, t: Throwable) {
                reviewListListener.onError("Ошибка сети: ${t.message}")
            }
        })
    }

    fun fetchNotGenericFromServer(productId: Int, page: Int = 1, perPage: Int = 10,searchString: String) {
        val call = apiService.getNotGenericReviews(productId, page, perPage,searchString)

        call.enqueue(object : Callback<ReviewResponse> {
            override fun onResponse(call: Call<ReviewResponse>, response: Response<ReviewResponse>) {
                if (response.isSuccessful) {
                    val reviewResponse = response.body()
                    val reviews = reviewResponse?.reviews ?: emptyList()
                    if (reviewResponse != null) {
                        reviewListListener.onReviewsReceived(reviews,reviewResponse.total,reviewResponse.currentPage)
                    }
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<ReviewResponse>, t: Throwable) {
                reviewListListener.onError("Ошибка сети: ${t.message}")
            }
        })
    }
    fun fetchNegativeFromServer(productId: Int, page: Int = 1, perPage: Int = 10,searchString: String) {
        val call = apiService.getNegativeReviews(productId, page, perPage,searchString)

        call.enqueue(object : Callback<ReviewResponse> {
            override fun onResponse(call: Call<ReviewResponse>, response: Response<ReviewResponse>) {
                if (response.isSuccessful) {
                    val reviewResponse = response.body()
                    val reviews = reviewResponse?.reviews ?: emptyList()
                    if (reviewResponse != null) {
                        reviewListListener.onReviewsReceived(reviews,reviewResponse.total,reviewResponse.currentPage)
                    }
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<ReviewResponse>, t: Throwable) {
                reviewListListener.onError("Ошибка сети: ${t.message}")
            }
        })
    }
    private fun handleErrorResponse(response: Response<*>) {
        if (response.code() == 401) {
            handleUnauthorizedError()
            reviewListListener.onError("Время сеанса истекло. Пожалуйста, войдите снова.")
        }
        else
        {
            if(response.code() == 404){
                handleNotFoundError()
            }
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

    private fun handleNotFoundError() {
        reviewListListener.onNotFound()
    }
}