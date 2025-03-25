package com.example.protypeapp.controller.listeners

import com.example.protypeapp.models.statistic.GraphData
import com.example.protypeapp.models.statistic.StatisticResponse

interface StatisticListener {
    fun onUnauthorized()
    fun onGraphInfoReceived(graphDataList: List<GraphData>)
    fun onStatisticInfoReceived(statisticResponse: StatisticResponse?)
    fun onError(message: String)
}