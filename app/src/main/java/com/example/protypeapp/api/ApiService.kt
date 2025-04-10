package com.example.protypeapp.api

import com.example.protypeapp.models.product.Product
import com.example.protypeapp.models.product.ProductAdd
import com.example.protypeapp.models.product.ProductResponse
import com.example.protypeapp.models.review.ReviewResponse
import com.example.protypeapp.models.statistic.GraphData
import com.example.protypeapp.models.statistic.StatisticResponse
import com.example.protypeapp.models.user.UpdateUserRequest
import com.example.protypeapp.models.user.UpdateUserResponse
import com.example.protypeapp.models.user.UserInfo
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {

    //products
    @GET("/products/byUser/")
    fun getProducts(@Query("page") page: Int,
                    @Query("per_page") perPage: Int): Call<ProductResponse>
    @POST("/products/")
    fun addProduct(@Body product: ProductAdd): Call<Void>

    @Multipart
    @POST("/products/csv")
    fun addProductCsv(@Part file: MultipartBody.Part?): Call<Void>
    @DELETE("/products/{id}")
    fun deleteProduct(@Path("id") productId: Int): Call<Unit>
    //reviews
    @GET("/reviews/{product_id}/generic")
    fun getGenericReviews(@Path("product_id") productId: Int,
                          @Query("page") page: Int,
                          @Query("per_page") perPage: Int,
                          @Query("search_string") searchString: String
    ): Call<ReviewResponse>
    @GET("/reviews/{product_id}/not-generic")
    fun getNotGenericReviews(@Path("product_id") productId: Int,
                             @Query("page") page: Int,
                             @Query("per_page") perPage: Int,
                             @Query("search_string") searchString: String
    ): Call<ReviewResponse>
    @GET("reviews/{product_id}/positive")
    fun getPositiveReviews(
        @Path("product_id") productId: Int,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("search_string") searchString: String
    ): Call<ReviewResponse>
    @GET("/reviews/{product_id}/negative")
    fun getNegativeReviews(@Path("product_id") productId: Int,
                           @Query("page") page: Int,
                           @Query("per_page") perPage: Int,
                           @Query("search_string") searchString: String
    ): Call<ReviewResponse>

    @GET("/reviews/{product_id}")
    fun getAllReviews(@Path("product_id") productId: Int,
                           @Query("page") page: Int,
                           @Query("per_page") perPage: Int,
                      @Query("search_string") searchString: String
    ): Call<ReviewResponse>

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