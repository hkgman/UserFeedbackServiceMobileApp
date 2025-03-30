package com.example.protypeapp.models.review

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.protypeapp.R
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewListAdapter (private val context: Context, private val reviewList: MutableList<Review>) :
    RecyclerView.Adapter<ReviewListAdapter.ReviewViewHolder>(){
    inner class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName: TextView = view.findViewById(R.id.userName)
        val textReview: TextView = view.findViewById(R.id.textReview)
        val date: TextView = view.findViewById(R.id.date)
        val mark: TextView = view.findViewById(R.id.ratingNumber)
        val problem: TextView=view.findViewById(R.id.problem)
        val imageView: ImageView = view.findViewById(R.id.ivReviewImage)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewListAdapter.ReviewViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.activity_item_review, parent, false)
        return ReviewViewHolder(view)
    }
    private fun formatDateString(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }
    override fun onBindViewHolder(holder: ReviewListAdapter.ReviewViewHolder, position: Int) {
        val review = reviewList[position]
        holder.userName.text = review.userName
        holder.textReview.text = review.text.toString()
        holder.date.text = formatDateString(review.createdDate)
        holder.mark.text=review.mark.toString()
        holder.problem.text = review.problem
        when {
            review.isPositive == null -> {
                holder.itemView.setBackgroundResource(R.drawable.custom_card_background)
            }
            review.isPositive -> {
                holder.itemView.setBackgroundResource(R.drawable.neon_border_positive)
            }
            else -> {
                holder.itemView.setBackgroundResource(R.drawable.neon_border_negative)
            }
        }
        if (review.isGeneric) {
            holder.imageView.setImageResource(R.drawable.robot)
        } else {
            holder.imageView.setImageResource(R.drawable.person)
        }

    }

    override fun getItemCount(): Int {
        return reviewList.size
    }
    fun updateItems(newItems: List<Review>) {
        reviewList.clear()
        reviewList.addAll(newItems)
        notifyDataSetChanged()
    }
}