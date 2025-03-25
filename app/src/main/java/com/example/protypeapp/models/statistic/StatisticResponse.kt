package com.example.protypeapp.models.statistic

data class StatisticResponse(
    val problemMain: String,
    val topProblems: List<String>,
    val answer: String
)