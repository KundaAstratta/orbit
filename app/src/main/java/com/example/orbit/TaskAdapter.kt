package com.example.orbit

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.orbit.databinding.ItemTaskBinding

class TaskAdapter : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.cbTask.text = task.name
            binding.cbTask.isChecked = task.isCompleted
            updateStrikeThrough(task.isCompleted)

            binding.cbTask.setOnCheckedChangeListener { _, isChecked ->
                task.isCompleted = isChecked
                updateStrikeThrough(isChecked)
            }
        }

        private fun updateStrikeThrough(isCompleted: Boolean) {
            if (isCompleted) {
                binding.cbTask.paintFlags = binding.cbTask.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.cbTask.paintFlags = binding.cbTask.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean = oldItem == newItem
    }
}