package com.example.protypeapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.protypeapp.controller.listeners.StatisticListener
import com.example.protypeapp.controller.StatisticController
import com.example.protypeapp.models.statistic.GraphData
import com.example.protypeapp.models.statistic.ProblemAdapter
import com.example.protypeapp.models.statistic.StatisticResponse
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class StatisticActivity : AppCompatActivity(),StatisticListener {
    private lateinit var statisticController: StatisticController
    private lateinit var buttonAll: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic)
        statisticController = StatisticController(this,this)
        statisticController.checkAuthorization()
        setupViews()
    }

    private fun setupViews() {
        buttonAll = findViewById<Button>(R.id.buttonAll)

        buttonAll.setOnClickListener {
            navigateToReviewActivity(ReviewListActivity::class.java)
        }
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
    override fun onResume() {
        super.onResume()
        val productId = intent.getIntExtra("productId", -1)
        if (productId != -1) {
            statisticController.fetchStatisticData(productId)
        } else {
            showToast("Ошибка: ID продукта не найден")
        }
        statisticController.fetchGraphData(productId)
    }

    private fun generateGraph(graphDataList: List<GraphData>) {
        val barChart = findViewById<BarChart>(R.id.barChart)

        val positiveEntries = mutableListOf<BarEntry>()
        val negativeEntries = mutableListOf<BarEntry>()
        val dates = mutableListOf<String>()
        val positiveColors = mutableListOf<Int>()
        val negativeColors = mutableListOf<Int>()

        for (i in graphDataList.indices) {
            val positiveCount = graphDataList[i].positiveCount as Int
            val negativeCount = graphDataList[i].negativeCount as Int

            if (positiveCount > negativeCount) {
                positiveEntries.add(BarEntry(i.toFloat(), positiveCount.toFloat()))
                negativeEntries.add(BarEntry(i.toFloat(), negativeCount.toFloat()))
                positiveColors.add(android.graphics.Color.GREEN)
                negativeColors.add(android.graphics.Color.RED)
            } else if (negativeCount > positiveCount) {
                positiveEntries.add(BarEntry(i.toFloat(), negativeCount.toFloat()))
                negativeEntries.add(BarEntry(i.toFloat(), positiveCount.toFloat()))
                positiveColors.add(android.graphics.Color.RED)
                negativeColors.add(android.graphics.Color.GREEN)
            } else {
                positiveEntries.add(BarEntry(i.toFloat(), positiveCount.toFloat()))
                negativeEntries.add(BarEntry(i.toFloat(), negativeCount.toFloat()))
                positiveColors.add(android.graphics.Color.YELLOW)
                negativeColors.add(android.graphics.Color.YELLOW)
            }

            dates.add(graphDataList[i].createdDate as String)
        }

        val positiveDataSet = BarDataSet(positiveEntries, "Positive Count")
        val negativeDataSet = BarDataSet(negativeEntries, "Negative Count")

        positiveDataSet.colors = positiveColors
        negativeDataSet.colors = negativeColors

        val barData = BarData(positiveDataSet, negativeDataSet)
        barData.setDrawValues(true)

        barData.setValueTextColor(android.graphics.Color.WHITE)

        barChart.data = barData
        barChart.invalidate()

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(dates)
        xAxis.granularity = 1f

        barChart.setDrawValueAboveBar(true)
        barChart.setDrawMarkers(false)
        barChart.legend.textColor = android.graphics.Color.WHITE
        xAxis.textColor = android.graphics.Color.WHITE
        barChart.axisLeft.textColor = android.graphics.Color.WHITE
        barChart.axisRight.textColor = android.graphics.Color.WHITE
    }

    private fun navigateToReviewActivity(activityClass: Class<*>) {
        val productId = intent.getIntExtra("productId", -1)
        if (productId != -1) {
            val intent = Intent(this, activityClass)
            intent.putExtra("productId", productId)
            startActivity(intent)
        } else {
            showToast("Ошибка: ID продукта не найден")
        }
    }

    override fun onUnauthorized() {
        val intent = Intent(this@StatisticActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onGraphInfoReceived(graphDataList: List<GraphData>) {
        generateGraph(graphDataList)
    }

    override fun onStatisticInfoReceived(statisticResponse: StatisticResponse?) {
        statisticResponse?.let {
            val moodText = findViewById<TextView>(R.id.moodText)
            val moodImage = findViewById<ImageView>(R.id.moodImage)
            val answerText = findViewById<TextView>(R.id.textAnswer)
            when (it.problemMain) {
                "Позитивное" -> {
                    moodImage.setImageResource(R.drawable.positive)
                    moodText.text = "Позитивное"
                }

                "Негативное" -> {
                    moodImage.setImageResource(R.drawable.negative)
                    moodText.text = "Негативное"
                }

                "Сбалансированное" -> {
                    moodImage.setImageResource(R.drawable.neutral)
                    moodText.text = "Сбалансированное"
                }
            }
            val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
            val adapter = ProblemAdapter(it.topProblems)
            recyclerView.adapter = adapter
            answerText.text = it.answer
        }
    }
    override fun onError(message: String) {
        showToast(message)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
