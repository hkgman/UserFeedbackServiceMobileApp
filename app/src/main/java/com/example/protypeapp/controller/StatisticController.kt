package com.example.protypeapp.controller

import android.content.Context
import android.widget.Toast
import com.example.protypeapp.API.ApiClient
import com.example.protypeapp.API.ApiService
import com.example.protypeapp.controller.Listeners.StatisticListener
import com.example.protypeapp.models.Statistic.GraphData
import com.example.protypeapp.models.Statistic.StatisticResponse
import com.example.protypeapp.userStorage.UserPreferences
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StatisticController(private val context: Context,private val listener: StatisticListener) {
    private val userPreferences = UserPreferences(context)
    private val apiService = ApiClient.getClient(context).create(ApiService::class.java)

    fun fetchGraphData(productId: Int) {
        val call = apiService.getGraphInfo(productId)

        call.enqueue(object : Callback<List<GraphData>> {
            override fun onResponse(call: Call<List<GraphData>>, response: Response<List<GraphData>>) {
                if (response.isSuccessful) {
                    val graphDataList = response.body()
                    graphDataList?.let {
                        listener.onGraphInfoReceived(it)
                    }
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<List<GraphData>>, t: Throwable) {
                listener.onError("Ошибка сети: ${t.message}")
            }
        })
    }

     fun fetchStatisticData(product_id:Int) {
        val call = apiService.getStatistic(product_id)

        call.enqueue(object : Callback<StatisticResponse> {
            override fun onResponse(call: Call<StatisticResponse>, response: Response<StatisticResponse>) {
                if (response.isSuccessful) {
                    val statisticResponse = response.body()
                    listener.onStatisticInfoReceived(statisticResponse)
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<StatisticResponse>, t: Throwable) {
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
        listener.onUnauthorized()
    }
}