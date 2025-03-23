package com.example.protypeapp
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.protypeapp.controller.HomeController
import com.example.protypeapp.controller.Listeners.HomeListener
import com.example.protypeapp.models.Product.Product
import com.example.protypeapp.models.Product.ProductAdapter
import com.example.protypeapp.models.Product.ProductAdd
import com.example.protypeapp.models.User.UserInfo

class HomeActivity : AppCompatActivity(), HomeListener {
    private lateinit var homeController: HomeController
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productList: MutableList<Product>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        homeController = HomeController(this, this)
        homeController.checkAuthorization()

        setupViews()
        setupListeners()

        homeController.fetchProducts()
        homeController.fetchUserInfo()
    }

    override fun onResume() {
        super.onResume()
        homeController.fetchProducts()
        homeController.fetchUserInfo()
    }

    private fun setupViews() {
        productList = mutableListOf()
        productAdapter = ProductAdapter(this, productList)

        findViewById<RecyclerView>(R.id.productRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = productAdapter
        }
    }

    private fun setupListeners() {
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

        findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout).setOnRefreshListener {
            homeController.fetchProducts()
            findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout).isRefreshing = false
        }



        productAdapter.onDeleteClickListener = { product ->
            homeController.deleteProduct(product)
        }

        productAdapter.onItemClickListener = { product ->
            val intent = Intent(this, StatisticActivity::class.java).apply {
                putExtra("product_id", product.id)
                putExtra("product_name", product.productName)
                putExtra("supplier_name", product.supplierName)
            }
            startActivity(intent)
        }
    }

    override fun onUserInfoReceived(user: UserInfo) {
        findViewById<TextView>(R.id.user_info).text = "${user.surname} ${user.name} ${user.patronymic}"
    }

    override fun onUserImageReceived(bitmap: Bitmap?) {
        val imageView = findViewById<ImageView>(R.id.ivProfileImage)
        imageView.setImageBitmap(bitmap ?: BitmapFactory.decodeResource(resources, R.drawable.person))
    }

    override fun onProductsReceived(products: List<Product>) {
        productAdapter.updateItems(products.toMutableList())
    }

    override fun onProductAdded() {
        showToast("Продукт добавлен")
        homeController.fetchProducts()
    }

    override fun onProductDeleted() {
        showToast("Продукт удален")
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
