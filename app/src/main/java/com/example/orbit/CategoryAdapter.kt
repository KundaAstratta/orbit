package com.example.orbit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.orbit.databinding.ItemCategoryBinding

class CategoryAdapter(private val onLongClickListener: (Category) -> Unit) :
    ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Category) {
            binding.tvCategoryName.text = category.name
            val color = ContextCompat.getColor(binding.root.context, category.colorResId)
            binding.cardView.setCardBackgroundColor(color)

            itemView.setOnLongClickListener {
                onLongClickListener(category)
                true
            }
        }
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean = oldItem == newItem
    }
}