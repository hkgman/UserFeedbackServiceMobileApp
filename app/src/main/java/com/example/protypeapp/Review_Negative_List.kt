package com.example.protypeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.protypeapp.API.ApiClient
import com.example.protypeapp.API.ApiService
import com.example.protypeapp.controller.Listeners.ReviewNegativeListListener
import com.example.protypeapp.controller.ReviewNegativeListController
import com.example.protypeapp.models.Review.PaginationScrollListener
import com.example.protypeapp.models.Review.Review
import com.example.protypeapp.models.Review.ReviewN
import com.example.protypeapp.models.Review.ReviewNAdapter
import com.example.protypeapp.userStorage.UserPreferences
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Review_Negative_List : AppCompatActivity(),ReviewNegativeListListener {
    private lateinit var reviewAdapter: ReviewNAdapter
    private lateinit var productList: MutableList<ReviewN>
    private lateinit var reviewRecyclerView: RecyclerView
    private lateinit var reviewNegativeListController: ReviewNegativeListController

    private var currentPage = 1
    private val perPage = 10
    private var isLoading = false
    private var totalPages = 1
    private var productId: Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_negative_list)
        reviewRecyclerView = findViewById(R.id.reviewRecyclerView)
        reviewNegativeListController = ReviewNegativeListController(this,this)
        productList = mutableListOf()
        reviewAdapter = ReviewNAdapter(this, productList)
        reviewRecyclerView.adapter = reviewAdapter
        val layoutManager = LinearLayoutManager(this)
        reviewRecyclerView.layoutManager = layoutManager
        productId = intent.getIntExtra("product_id", -1)
        if (productId != -1) {
            loadReviews()
        } else {
            Toast.makeText(this, "Ошибка: ID продукта не передан", Toast.LENGTH_SHORT).show()
        }
        reviewRecyclerView.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun loadMoreItems() {
                if (currentPage < totalPages && !isLoading) {
                    isLoading = true
                    currentPage++
                    loadReviews()
                }
            }

            override fun isLastPage(): Boolean {
                Log.d("dddd","$currentPage > $totalPages")
                return currentPage >= totalPages
            }

            override fun isLoading(): Boolean {
                return isLoading
            }
        })
    }

    private fun loadReviews() {
        reviewNegativeListController.fetchNegativeFromServer(productId, currentPage, perPage)
    }

    override fun onReviewsReceived(reviews: List<ReviewN>, total: Int) {
        if (currentPage == 1) {
            productList.clear()
        }
        productList.addAll(reviews)
        reviewAdapter.notifyDataSetChanged()
        isLoading = false
        totalPages=total
    }

    override fun onUnauthorized() {
        val intent = Intent(this@Review_Negative_List, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}