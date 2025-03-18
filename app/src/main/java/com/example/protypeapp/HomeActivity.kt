package com.example.protypeapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.protypeapp.API.ApiClient
import com.example.protypeapp.API.ApiService
import com.example.protypeapp.models.Product.Product
import com.example.protypeapp.models.Product.ProductAdapter
import com.example.protypeapp.models.Product.ProductAdd
import com.example.protypeapp.models.User.UserInfo
import com.example.protypeapp.userStorage.UserPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Base64
import android.widget.TextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import org.json.JSONObject


class HomeActivity : AppCompatActivity() {

    private lateinit var productAdapter: ProductAdapter
    private lateinit var productList: MutableList<Product>
    private lateinit var productRecyclerView: RecyclerView
    private lateinit var addButton: Button
    private lateinit var profileButton: Button
    private lateinit var textField: EditText
    private lateinit var userInfo: TextView
    private lateinit var image:ImageView
    private lateinit var swipeRefreshLayout:SwipeRefreshLayout
    private lateinit var userPreferences: UserPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        userPreferences = UserPreferences(this)
        if (!userPreferences.isLoggedIn()) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        productRecyclerView = findViewById(R.id.productRecyclerView)
        addButton = findViewById(R.id.btnAdd)
        profileButton = findViewById(R.id.btnProfile)
        textField = findViewById(R.id.etTextField)
        userInfo = findViewById(R.id.user_info)
        image = findViewById(R.id.ivProfileImage)
        productList = mutableListOf()
        productAdapter = ProductAdapter(this, productList)
        productRecyclerView.layoutManager = LinearLayoutManager(this)
        productRecyclerView.adapter = productAdapter
        swipeRefreshLayout.setOnRefreshListener {
            refreshProductList()
        }
        profileButton.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }
        addButton.setOnClickListener {
            val productName = textField.text.toString()
            if (productName.isNotEmpty()) {
                val newProduct = ProductAdd("productName", "New Supplier", "New Supplier", "Skibidi",productName)
                addProductToServer(newProduct)
                textField.text.clear()
            }
        }
        productAdapter.onDeleteClickListener = { product ->
            deleteProductFromServer(product)
        }
        productAdapter.onItemClickListener = { product ->
            val intent = Intent(this, StatisticActivity::class.java)
            intent.putExtra("product_id", product.id)
            intent.putExtra("product_name", product.product_name)
            intent.putExtra("supplier_name", product.supplier_name)
            startActivity(intent)
        }
    }

    private fun refreshProductList() {
        val updatedProducts = fetchItemsFromServer()
        swipeRefreshLayout.isRefreshing = false // Останавливаем анимацию
    }
    override fun onResume() {
        super.onResume()
        fetchItemsFromServer()
        fetchUserInfo()
    }
    private fun fetchUserInfo() {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        val call = apiService.getUser()

        call.enqueue(object : Callback<UserInfo> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<UserInfo>, response: Response<UserInfo>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        userInfo.setText("${user.surname} ${user.name} ${user.patronymic}")

                        if (user.image != null && user.image != "none") {
                            val imageBytes = decodeBase64(user.image)
                            if (imageBytes != null) {
                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                image.setImageBitmap(bitmap)
                            } else {
                                image.setImageResource(R.drawable.person)
                            }
                        } else {
                            image.setImageResource(R.drawable.person)
                        }
                    } else {
                        Toast.makeText(this@HomeActivity, "Полученные данные пусты", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (response.code() == 401) {
                        handleUnauthorizedError()
                    } else {
                        val errorResponse = response.errorBody()?.string()
                        val jsonObject = JSONObject(errorResponse!!)
                        val errorMessage = jsonObject.optString("message", "Неизвестная ошибка")
                        Toast.makeText(this@HomeActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<UserInfo>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun decodeBase64(base64String: String?): ByteArray? {
        return try {
            if (base64String != null) {
                Base64.decode(base64String, Base64.DEFAULT)
            } else {
                null
            }
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun fetchItemsFromServer() {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        val call = apiService.getProducts()

        call.enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful) {
                    val itemsFromServer = response.body()?.toMutableList() ?: mutableListOf()

                    productAdapter.updateItems(itemsFromServer)
                } else {
                    if (response.code() == 401) {
                        handleUnauthorizedError()
                    } else {
                        val errorResponse = response.errorBody()?.string()
                        val jsonObject = JSONObject(errorResponse!!)
                        val errorMessage = jsonObject.optString("message", "Неизвестная ошибка")
                        Toast.makeText(this@HomeActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun addProductToServer(newProduct: ProductAdd) {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        val call = apiService.addProduct(newProduct)

        call.enqueue(object : Callback<Product> {
            override fun onResponse(call: Call<Product>, response: Response<Product>) {
                if (response.isSuccessful) {
                    fetchItemsFromServer()
                    Toast.makeText(this@HomeActivity, "Продукт добавлен", Toast.LENGTH_SHORT).show()
                } else {
                    if (response.code() == 401) {
                        handleUnauthorizedError()
                    } else {
                        val errorResponse = response.errorBody()?.string()
                        val jsonObject = JSONObject(errorResponse!!)
                        val errorMessage = jsonObject.optString("message", "Неизвестная ошибка")
                        Toast.makeText(this@HomeActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<Product>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun deleteProductFromServer(product: Product) {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        val call = apiService.deleteProduct(product.id)

        call.enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    fetchItemsFromServer()
                    Toast.makeText(this@HomeActivity, "Продукт удален", Toast.LENGTH_SHORT).show()
                } else {
                    if (response.code() == 401) {
                        handleUnauthorizedError()
                    } else {
                        val errorResponse = response.errorBody()?.string()
                        val jsonObject = JSONObject(errorResponse!!)
                        val errorMessage = jsonObject.optString("message", "Неизвестная ошибка")
                        Toast.makeText(this@HomeActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun handleUnauthorizedError() {
        userPreferences.logout()

        val intent = Intent(this@HomeActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
