package com.example.protypeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.protypeapp.controller.ReviewListController
import com.example.protypeapp.controller.listeners.ReviewListListener
import com.example.protypeapp.models.review.Review
import com.example.protypeapp.models.review.ReviewListAdapter
import com.example.protypeapp.utils.PaginationScrollListener

class ReviewListActivity : AppCompatActivity(), ReviewListListener {
    private lateinit var reviewListAdapter: ReviewListAdapter
    private lateinit var reviewList: MutableList<Review>
    private lateinit var reviewRecyclerView: RecyclerView
    private lateinit var reviewListController: ReviewListController
    private lateinit var radioGroup: RadioGroup
    private lateinit var editText: EditText
    private lateinit var view: TextView
    private lateinit var buttonSearch: Button
    private var currentPage = 1
    private val perPage = 10
    private var isLoading = false
    private var totalPages = 1
    private var productId: Int = -1
    private var searchString = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_list)

        reviewListController = ReviewListController(this, this)
        reviewListController.checkAuthorization()

        setupViews()

        productId = intent.getIntExtra("productId", -1)
        if (productId != -1) {
            loadReviews()
        } else {
            showToast("Ошибка: ID продукта не передан")
        }
    }

    private fun setupViews() {
        reviewRecyclerView = findViewById(R.id.reviewAllRecyclerView)
        editText = findViewById(R.id.etTextField)
        radioGroup = findViewById(R.id.radio_group)
        view = findViewById(R.id.tv_no_items)
        reviewList = mutableListOf()
        reviewListAdapter = ReviewListAdapter(this, reviewList)

        reviewRecyclerView.adapter = reviewListAdapter
        val layoutManager = LinearLayoutManager(this)
        reviewRecyclerView.layoutManager = layoutManager
        buttonSearch = findViewById(R.id.btnSearch)
        setupListeners(layoutManager)
    }


    private fun setupListeners(layoutManager: LinearLayoutManager) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                editable?.let {
                    searchString = it.toString()
                    if (searchString.isEmpty()) {
                        currentPage = 1
                        loadReviews()
                    }
                }
            }
        })

        buttonSearch.setOnClickListener {
            currentPage = 1  // Сбрасываем текущую страницу
            loadReviews()
        }

        reviewRecyclerView.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun loadMoreItems() {
                if (currentPage < totalPages && !isLoading) {
                    isLoading = true
                    currentPage++
                    loadReviews()
                }
            }

            override fun isLastPage(): Boolean = currentPage >= totalPages

            override fun isLoading(): Boolean = isLoading
        })

        findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout).setOnRefreshListener {
            currentPage = 1  // Сбрасываем текущую страницу
            loadReviews()
            findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout).isRefreshing = false
        }

        radioGroup.setOnCheckedChangeListener { _, _ ->
            currentPage = 1  // Сбрасываем текущую страницу при смене фильтра
            loadReviews()
        }
    }

    private fun loadReviews() {
        // Сбрасываем список отзывов перед загрузкой новых
        if (currentPage == 1) {
            reviewList.clear()
            reviewListAdapter.notifyDataSetChanged()
        }

        when (radioGroup.checkedRadioButtonId) {
            R.id.radio_all -> reviewListController.fetchAllFromServer(productId, currentPage, perPage, searchString)
            R.id.radio_positive -> reviewListController.fetchPositiveFromServer(productId, currentPage, perPage, searchString)
            R.id.radio_ai -> reviewListController.fetchGenericFromServer(productId, currentPage, perPage, searchString)
            R.id.radio_human -> reviewListController.fetchNotGenericFromServer(productId, currentPage, perPage, searchString)
            R.id.radio_negative -> reviewListController.fetchNegativeFromServer(productId, currentPage, perPage, searchString)
            else -> reviewListController.fetchAllFromServer(productId, currentPage, perPage, searchString)
        }
    }


    override fun onReviewsReceived(reviews: List<Review>, total: Int) {
        if (currentPage == 1) {
            reviewList.clear()
        }
        reviewList.addAll(reviews)
        reviewListAdapter.notifyDataSetChanged()
        isLoading = false
        totalPages=total
        if (reviews.isEmpty()) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }

    override fun onUnauthorized() {
        val intent = Intent(this@ReviewListActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onNotFound() {
        val intent = Intent(this@ReviewListActivity, HomeActivity::class.java)
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