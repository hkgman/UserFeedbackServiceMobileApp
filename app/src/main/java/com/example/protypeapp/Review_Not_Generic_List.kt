package com.example.protypeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.protypeapp.API.ApiClient
import com.example.protypeapp.API.ApiService
import com.example.protypeapp.models.Review.Review
import com.example.protypeapp.models.Review.ReviewAdapter
import com.example.protypeapp.userStorage.UserPreferences
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Review_Not_Generic_List : AppCompatActivity() {
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var productList: MutableList<Review>
    private lateinit var reviewRecyclerView: RecyclerView
    private lateinit var userPreferences: UserPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_not_generic_list)
        reviewRecyclerView = findViewById(R.id.reviewRecyclerView)
        productList = mutableListOf()
        reviewAdapter = ReviewAdapter(this, productList)
        reviewRecyclerView.adapter = reviewAdapter
        reviewRecyclerView.layoutManager = LinearLayoutManager(this)
        val productId = intent.getIntExtra("product_id", -1)
        if (productId != -1) {
            fetchItemsFromServer(productId)
        } else {
            Toast.makeText(this, "Ошибка: ID продукта не передан", Toast.LENGTH_SHORT).show()
        }
    }
    private fun fetchItemsFromServer(productId:Int) {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        val call = apiService.getNotGenericReviews(productId)

        call.enqueue(object : Callback<List<Review>> {
            override fun onResponse(call: Call<List<Review>>, response: Response<List<Review>>) {
                if (response.isSuccessful) {
                    val itemsFromServer = response.body()?.toMutableList() ?: mutableListOf()

                    reviewAdapter.updateItems(itemsFromServer)
                } else {
                    if (response.code() == 401) {
                        handleUnauthorizedError()
                    } else {
                        val errorResponse = response.errorBody()?.string()
                        val jsonObject = JSONObject(errorResponse!!)
                        val errorMessage = jsonObject.optString("message", "Неизвестная ошибка")
                        Toast.makeText(this@Review_Not_Generic_List, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<List<Review>>, t: Throwable) {
                Toast.makeText(this@Review_Not_Generic_List, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun handleUnauthorizedError() {
        userPreferences.logout()

        val intent = Intent(this@Review_Not_Generic_List, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}