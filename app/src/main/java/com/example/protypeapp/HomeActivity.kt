package com.example.protypeapp
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.protypeapp.controller.HomeController
import com.example.protypeapp.controller.listeners.HomeListener
import com.example.protypeapp.models.product.Product
import com.example.protypeapp.models.product.ProductAdapter
import com.example.protypeapp.models.product.ProductAdd
import com.example.protypeapp.models.user.UserInfo
import com.example.protypeapp.utils.PaginationScrollListener

class HomeActivity : AppCompatActivity(), HomeListener {
    private lateinit var homeController: HomeController
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productList: MutableList<Product>
    private lateinit var productRecyclerView: RecyclerView
    private var currentPage = 1
    private val perPage = 4
    private var isLoading = false
    private var totalPages = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        homeController = HomeController(this, this)
        homeController.checkAuthorization()

        setupViews()

        loadProducts()
        homeController.fetchUserInfo()
    }

    override fun onResume() {
        super.onResume()
        currentPage = 1
        homeController.fetchProducts()
        homeController.fetchUserInfo()
    }

    private fun setupViews() {
        productRecyclerView = findViewById(R.id.productRecyclerView)
        productList = mutableListOf()
        productAdapter = ProductAdapter(this, productList)
        productRecyclerView.adapter = productAdapter
        val layoutManager = LinearLayoutManager(this)
        productRecyclerView.layoutManager = layoutManager
        setupListeners(layoutManager)
    }

    private fun setupListeners(layoutManager:LinearLayoutManager) {
        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        findViewById<Button>(R.id.btnAdd).setOnClickListener {
            val productName = findViewById<EditText>(R.id.etTextField).text.toString()
            if (productName.isNotEmpty()) {
                val newProduct = ProductAdd("productName", "New Supplier", "New Supplier", "Skibidi", productName)
                Log.d("dff",productName)
                homeController.addProduct(newProduct)
                findViewById<EditText>(R.id.etTextField).text.clear()
            }
        }

        productRecyclerView.addOnScrollListener(
            object : PaginationScrollListener(layoutManager) {
                override fun loadMoreItems() {
                    if (currentPage < totalPages && !isLoading) {
                        isLoading = true
                        currentPage++
                        loadProducts()
                    }
                }

                override fun isLastPage(): Boolean = currentPage >= totalPages

                override fun isLoading(): Boolean = isLoading
            })

        findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout).setOnRefreshListener {
            currentPage = 1
            homeController.fetchProducts()
            findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout).isRefreshing = false
        }



        productAdapter.onDeleteClickListener = { product ->
            homeController.deleteProduct(product)
        }

        productAdapter.onItemClickListener = { product ->
            val intent = Intent(this, StatisticActivity::class.java).apply {
                putExtra("productId", product.id)
                putExtra("productName", product.productName)
                putExtra("supplierName", product.supplierName)
            }
            startActivity(intent)
        }
    }

    private fun loadProducts() {
        homeController.fetchProducts(currentPage, perPage)
    }
    override fun onUserInfoReceived(user: UserInfo) {
        findViewById<TextView>(R.id.user_info).text = "${user.surname} ${user.name} ${user.patronymic}"
    }

    override fun onUserImageReceived(bitmap: Bitmap?) {
        val imageView = findViewById<ImageView>(R.id.ivProfileImage)
        imageView.setImageBitmap(bitmap ?: BitmapFactory.decodeResource(resources, R.drawable.person))
    }

    override fun onProductsReceived(products: List<Product>,total:Int) {
        if (currentPage == 1) {
            productList.clear()
        }
        productList.addAll(products)
        productAdapter.notifyDataSetChanged()
        isLoading = false
        totalPages=total
    }

    override fun onProductAdded() {
        showToast("Продукт добавлен")
        currentPage = 1
        homeController.fetchProducts()
    }

    override fun onProductDeleted() {
        showToast("Продукт удален")
        currentPage = 1
        homeController.fetchProducts()
    }

    override fun onUserNotAuthorized() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onError(message: String) {
        showToast(message)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
