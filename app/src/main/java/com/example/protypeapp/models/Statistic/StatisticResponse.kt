package com.example.protypeapp.models.Statistic

data class StatisticResponse(
    val problemMain: String,
    val topProblems: List<String>,
    val answer: String
)