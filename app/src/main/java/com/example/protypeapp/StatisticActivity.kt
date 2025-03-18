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
import com.example.protypeapp.API.ApiClient
import com.example.protypeapp.API.ApiService
import com.example.protypeapp.controller.Listeners.StatisticListener
import com.example.protypeapp.controller.StatisticController
import com.example.protypeapp.models.Statistic.GraphData
import com.example.protypeapp.models.Statistic.ProblemAdapter
import com.example.protypeapp.models.Statistic.StatisticResponse
import com.example.protypeapp.userStorage.UserPreferences
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StatisticActivity : AppCompatActivity(),StatisticListener {
    private lateinit var statisticController: StatisticController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic)
        statisticController = StatisticController(this,this)
        val buttonHuman = findViewById<Button>(R.id.buttonHuman)
        val buttonRobot = findViewById<Button>(R.id.buttonRobot)
        val buttonGood = findViewById<Button>(R.id.buttonGood)
        val buttonBad = findViewById<Button>(R.id.buttonBad)

        buttonHuman.setOnClickListener {
            navigateToReviewActivity(Review_Not_Generic_List::class.java)
        }

        buttonRobot.setOnClickListener {
            navigateToReviewActivity(Review_Generic_List::class.java)
        }

        buttonGood.setOnClickListener {
            navigateToReviewActivity(Review_Positive_List::class.java)
        }

        buttonBad.setOnClickListener {
            navigateToReviewActivity(Review_Negative_List::class.java)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)


    }
    override fun onResume() {
        super.onResume()
        val productId = intent.getIntExtra("product_id", -1)
        if (productId != -1) {
            statisticController.fetchStatisticData(productId)
        } else {
            Toast.makeText(this, "Ошибка: ID продукта не передан", Toast.LENGTH_SHORT).show()
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
            val positiveCount = graphDataList[i].positive_count as Int
            val negativeCount = graphDataList[i].negative_count as Int

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

            dates.add(graphDataList[i].created_date as String)
        }

        val positiveDataSet = BarDataSet(positiveEntries, "Positive Count")
        val negativeDataSet = BarDataSet(negativeEntries, "Negative Count")

        positiveDataSet.setColors(positiveColors)
        negativeDataSet.setColors(negativeColors)

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
        val productId = intent.getIntExtra("product_id", -1)
        if (productId != -1) {
            val intent = Intent(this, activityClass)
            intent.putExtra("product_id", productId)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Ошибка: ID продукта не найден", Toast.LENGTH_SHORT).show()
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
}
