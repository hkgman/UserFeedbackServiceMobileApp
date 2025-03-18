package com.example.protypeapp.API

import com.example.protypeapp.models.Statistic.GraphData
import com.example.protypeapp.models.Product.Product
import com.example.protypeapp.models.Product.ProductAdd
import com.example.protypeapp.models.Review.Review
import com.example.protypeapp.models.Review.ReviewN
import com.example.protypeapp.models.Statistic.StatisticResponse
import com.example.protypeapp.models.User.UpdateUserRequest
import com.example.protypeapp.models.User.UpdateUserResponse
import com.example.protypeapp.models.User.UserInfo
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    //products
    @GET("/products/byUser/")
    fun getProducts(): Call<List<Product>>
    @POST("/products/")
    fun addProduct(@Body product: ProductAdd): Call<Product>
    @DELETE("/products/{id}")
    fun deleteProduct(@Path("id") productId: Int): Call<Unit>
    //reviews
    @GET("/reviews/product/{id}/generic")
    fun getGenericReviews(@Path("id") productId: Int): Call<List<Review>>
    @GET("/reviews/product/{id}/not-generic")
    fun getNotGenericReviews(@Path("id") productId: Int): Call<List<Review>>
    @GET("/reviews/product/{id}/positive")
    fun getPositiveReviews(@Path("id") productId: Int): Call<List<Review>>
    @GET("/reviews/product/{id}/negative")
    fun getNegativeReviews(@Path("id") productId: Int): Call<List<ReviewN>>
    @GET("/reviews/statistic/{id}")
    fun getStatistic(@Path("id") productId: Int): Call<StatisticResponse>
    //graph
    @GET("/reviews/graph_info/{product_id}")
    fun getGraphInfo(@Path("product_id") productId: Int): Call<List<GraphData>>
    //user
    @GET("/user")
    fun getUser():Call<UserInfo>
    @PUT("user/update")
    fun updateUser(@Body user: UpdateUserRequest): Call<UpdateUserResponse>
}