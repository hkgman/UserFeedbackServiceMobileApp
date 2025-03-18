package com.example.protypeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.protypeapp.API.ApiClient
import com.example.protypeapp.API.ApiService
import com.example.protypeapp.controller.Listeners.ReviewNegativeListListener
import com.example.protypeapp.controller.ReviewNegativeListController
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_negative_list)
        reviewRecyclerView = findViewById(R.id.reviewRecyclerView)
        reviewNegativeListController = ReviewNegativeListController(this,this)
        productList = mutableListOf()
        reviewAdapter = ReviewNAdapter(this, productList)
        reviewRecyclerView.adapter = reviewAdapter
        reviewRecyclerView.layoutManager = LinearLayoutManager(this)
        val productId = intent.getIntExtra("product_id", -1)
        if (productId != -1) {
            reviewNegativeListController.fetchNegativeFromServer(productId)
        } else {
            Toast.makeText(this, "Ошибка: ID продукта не передан", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onReviewsReceived(reviews: List<ReviewN>) {
        reviewAdapter.updateItems(reviews)
    }

    override fun onUnauthorized() {
        val intent = Intent(this@Review_Negative_List, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}