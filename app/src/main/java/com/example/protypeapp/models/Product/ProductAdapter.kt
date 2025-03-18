package com.example.protypeapp.models.Product

import android.content.Context
import android.media.Image
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
import com.squareup.picasso.Picasso

class ProductAdapter(private val context: Context, private val productList: MutableList<Product>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    var onDeleteClickListener: ((Product) -> Unit)? = null
    var onItemClickListener: ((Product) -> Unit)? = null

    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productNameTextView: TextView = view.findViewById(R.id.tvProductName)
        val supplierNameTextView: TextView = view.findViewById(R.id.tvSupplierName)
        val idTextView: TextView = view.findViewById(R.id.hiddenInput)
        val deleteButton: Button = view.findViewById(R.id.buttonAction)
        val itemContainer: View = view.findViewById(R.id.itemContainer) // Корневой контейнер карточки
        val ivProductImage: ImageView = view.findViewById(R.id.ivProductImage)
        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val product = productList[position]
                    if (product.status == "READY") {
                        onItemClickListener?.invoke(product)
                    } else {
                        Toast.makeText(context, "Происходит парсинг товаров...", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.activity_item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.productNameTextView.text = product.product_name
        holder.idTextView.text = product.id.toString()
        holder.supplierNameTextView.text = product.brand_name
        Picasso.get().load(product.image_url).resize(48,48).centerCrop().placeholder(R.drawable.photo).into(holder.ivProductImage);
        if (product.status == "LOADING") {
            holder.itemView.alpha = 0.6f
            holder.itemView.isEnabled = true
            holder.deleteButton.visibility = View.GONE
            holder.productNameTextView.text = "🔄 ${product.product_name}"
            holder.itemContainer.setBackgroundResource(R.drawable.custom_card_background_loading)
        } else {
            holder.itemView.alpha = 1.0f
            holder.itemView.isEnabled = true
            holder.deleteButton.visibility = View.VISIBLE
            holder.itemContainer.setBackgroundResource(R.drawable.custom_card_background)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClickListener?.invoke(product)
        }
    }


    override fun getItemCount(): Int {
        return productList.size
    }

    fun addProduct(product: Product) {
        productList.add(product)
        notifyItemInserted(productList.size - 1)
    }

    fun updateItems(newItems: List<Product>) {
        productList.clear()
        productList.addAll(newItems)
        notifyDataSetChanged()  // Обновляем весь список
    }
}
