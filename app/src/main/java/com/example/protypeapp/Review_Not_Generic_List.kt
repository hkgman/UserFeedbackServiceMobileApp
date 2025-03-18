package com.example.protypeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.protypeapp.API.ApiClient
import com.example.protypeapp.API.ApiService
import com.example.protypeapp.controller.Listeners.ReviewListListener
import com.example.protypeapp.controller.ReviewListController
import com.example.protypeapp.models.Review.Review
import com.example.protypeapp.models.Review.ReviewAdapter
import com.example.protypeapp.userStorage.UserPreferences
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Review_Not_Generic_List : AppCompatActivity(),ReviewListListener {
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var productList: MutableList<Review>
    private lateinit var reviewRecyclerView: RecyclerView
    private lateinit var reviewListController: ReviewListController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_not_generic_list)
        reviewRecyclerView = findViewById(R.id.reviewRecyclerView)
        reviewListController = ReviewListController(this,this)
        productList = mutableListOf()
        reviewAdapter = ReviewAdapter(this, productList)
        reviewRecyclerView.adapter = reviewAdapter
        reviewRecyclerView.layoutManager = LinearLayoutManager(this)
        val productId = intent.getIntExtra("product_id", -1)
        if (productId != -1) {
            reviewListController.fetchNotGenericFromServer(productId)
        } else {
            Toast.makeText(this, "Ошибка: ID продукта не передан", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onReviewsReceived(reviews: List<Review>) {
        reviewAdapter.updateItems(reviews)
    }

    override fun onUnauthorized() {
        val intent = Intent(this@Review_Not_Generic_List, MainActivity::class.java)
        startActivity(intent)
        finish()
    }


}