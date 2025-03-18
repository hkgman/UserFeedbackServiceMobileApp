package com.example.protypeapp.models.Review

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.protypeapp.R
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewNAdapter(private val context: Context, private val reviewList: MutableList<ReviewN>) :
    RecyclerView.Adapter<ReviewNAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName: TextView = view.findViewById(R.id.userName)
        val textReview: TextView = view.findViewById(R.id.textReview)
        val date: TextView = view.findViewById(R.id.date)
        val problem: TextView = view.findViewById(R.id.problem)
        val mark: TextView = view.findViewById(R.id.ratingNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.activity_item_review_negative, parent, false)
        return ReviewViewHolder(view)
    }
    fun formatDateString(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }
    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]
        holder.userName.text = review.user_name
        holder.textReview.text = review.text.toString()
        holder.date.text = formatDateString(review.created_date)
        holder.problem.text = review.problem
        holder.mark.text=review.mark.toString()
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }
    fun updateItems(newItems: List<ReviewN>) {
        reviewList.clear()
        reviewList.addAll(newItems)
        notifyDataSetChanged()
    }
}