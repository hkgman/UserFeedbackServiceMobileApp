package com.example.protypeapp.models.review

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.protypeapp.R
import com.example.protypeapp.models.product.Product

class FilterAdapter(
    private val filters: List<String>,
    private val onFilterSelected: (Int) -> Unit
) : RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {

    private var selectedPosition = 0

    inner class FilterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.filterText)

        init {
            view.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = bindingAdapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onFilterSelected(selectedPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_item_filter, parent, false)
        return FilterViewHolder(view)
    }

    override fun getItemCount(): Int = filters.size

    override fun onBindViewHolder(holder: FilterAdapter.FilterViewHolder, position: Int) {
        val text = filters[position]
        holder.textView.text=text
        if (position == selectedPosition) {
            holder.textView.setBackgroundResource(R.drawable.background_selected)
        } else {
            holder.textView.setBackgroundResource(R.drawable.background_unselected)
        }
    }
}