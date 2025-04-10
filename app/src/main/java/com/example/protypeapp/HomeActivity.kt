package com.example.protypeapp
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.FileUtils
import android.provider.OpenableColumns
import android.view.View
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
import com.example.protypeapp.controller.listeners.HomeListener
import com.example.protypeapp.models.product.Product
import com.example.protypeapp.models.product.ProductAdapter
import com.example.protypeapp.models.product.ProductAdd
import com.example.protypeapp.models.user.UserInfo
import com.example.protypeapp.utils.PaginationScrollListener
import java.io.File
import java.io.FileOutputStream

class HomeActivity : AppCompatActivity(), HomeListener {
    private lateinit var homeController: HomeController
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productList: MutableList<Product>
    private lateinit var productRecyclerView: RecyclerView
    private lateinit var view: TextView
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
    }

    override fun onResume() {
        super.onResume()
        currentPage = 1
        loadProducts()
        homeController.fetchUserInfo()
    }

    private fun setupViews() {
        view = findViewById(R.id.tv_no_items)
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
            if(productName.isEmpty()){
                showToast("Введите ссылку.")
            }
            if (productName.isNotEmpty()) {
                val newProduct = ProductAdd("productName", "New Supplier", "New Supplier", "Skibidi", productName)
                homeController.addProduct(newProduct)
                findViewById<EditText>(R.id.etTextField).text.clear()
            }
        }

        findViewById<Button>(R.id.btnAddCsv).setOnClickListener {
            val intent = Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)

            startActivityForResult(Intent.createChooser(intent, "Select a file"), 111)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Companion.REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK) {
            val selectedFileUri = data?.data

            if (selectedFileUri != null) {
                try {
                    val contentResolver = contentResolver
                    val inputStream = contentResolver.openInputStream(selectedFileUri)
                    if (inputStream != null) {
                        val fileName = getFileNameFromUri(selectedFileUri) ?: "temp.csv"

                        val extension = fileName.substringAfterLast(".", "csv")

                        val tempFile = File(cacheDir, "temp.$extension")

                        inputStream.copyTo(tempFile.outputStream())
                        homeController.addProductCsv(tempFile)
                        inputStream.close()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        name = it.getString(nameIndex)
                    }
                }
            }
        }
        if (name == null) {
            name = uri.path
            val cut = name?.lastIndexOf('/')
            if (cut != -1 && cut != null) {
                name = name?.substring(cut + 1)
            }
        }
        return name
    }

    companion object {
        private const val REQUEST_CODE_PICK_FILE = 111
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

    override fun onProductsReceived(products: List<Product>, total: Int, page: Int) {
        if (page == 1) {
            productAdapter.updateItems(products.toMutableList())
        } else {
            val currentItems = productAdapter.getItems().toMutableList()
            currentItems.addAll(products)
            productAdapter.updateItems(currentItems)
        }

        isLoading = false
        totalPages = total

        view.visibility = if (productAdapter.itemCount == 0) View.VISIBLE else View.GONE
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
