package com.example.protypeapp.models.Statistic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProblemAdapter(private val problems: List<String>) : RecyclerView.Adapter<ProblemAdapter.ProblemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProblemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return ProblemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProblemViewHolder, position: Int) {
        holder.bind(problems[position])
    }

    override fun getItemCount(): Int = problems.size

    class ProblemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(problem: String) {
            textView.text = problem
        }
    }
}
