package com.example.protypeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.protypeapp.controller.Listeners.ReviewListListener
import com.example.protypeapp.controller.ReviewListController
import com.example.protypeapp.utils.PaginationScrollListener
import com.example.protypeapp.models.Review.Review
import com.example.protypeapp.models.Review.ReviewAdapter

class ReviewGenericList : AppCompatActivity(),ReviewListListener {
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var productList: MutableList<Review>
    private lateinit var reviewRecyclerView: RecyclerView
    private lateinit var reviewListController: ReviewListController
    private var currentPage = 1
    private val perPage = 10
    private var isLoading = false
    private var totalPages = 1
    private var productId: Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_generic_list)
        reviewRecyclerView = findViewById(R.id.reviewRecyclerView)
        reviewListController = ReviewListController(this, this)
        productList = mutableListOf()
        reviewAdapter = ReviewAdapter(this, productList)
        reviewRecyclerView.adapter = reviewAdapter
        val layoutManager = LinearLayoutManager(this)
        reviewRecyclerView.layoutManager = layoutManager
        productId = intent.getIntExtra("product_id", -1)
        if (productId != -1) {
            loadReviews()
        } else {
            showToast("Ошибка: ID продукта не передан")
        }


        reviewRecyclerView.addOnScrollListener(
        object : PaginationScrollListener(layoutManager) {
            override fun loadMoreItems() {
                if (currentPage < totalPages && !isLoading) {
                    isLoading = true
                    currentPage++
                    loadReviews()
                }
            }

            override fun isLastPage(): Boolean {
                return currentPage >= totalPages
            }

            override fun isLoading(): Boolean {
                return isLoading
            }
        })
    }


    private fun loadReviews() {
        reviewListController.fetchGenericFromServer(productId, currentPage, perPage)
    }

    override fun onReviewsReceived(reviews: List<Review>,total: Int) {
        if (currentPage == 1) {
            productList.clear()
        }
        productList.addAll(reviews)
        reviewAdapter.notifyDataSetChanged()
        isLoading = false
        totalPages=total
    }

    override fun onUnauthorized() {
        val intent = Intent(this@ReviewGenericList, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onError(message: String) {
        showToast(message)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}