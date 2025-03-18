package com.example.protypeapp.controller.Listeners

import com.example.protypeapp.models.Statistic.GraphData
import com.example.protypeapp.models.Statistic.StatisticResponse

interface StatisticListener {
    fun onUnauthorized()
    fun onGraphInfoReceived(graphDataList: List<GraphData>)
    fun onStatisticInfoReceived(statisticResponse: StatisticResponse?)
}