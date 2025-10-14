package com.example.orbit

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.orbit.databinding.ActivityTaskBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class TaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskBinding
    private lateinit var taskAdapter: TaskAdapter
    private val db by lazy { AppDatabase.getDatabase(this).appDao() }
    private var categoryId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryId = intent.getLongExtra("EXTRA_CATEGORY_ID", -1)
        if (categoryId == -1L) {
            finish() // Sécurité : quitter si l'ID n'est pas valide
            return
        }

        val categoryName = intent.getStringExtra("EXTRA_CATEGORY_NAME")
        val categoryColorId = intent.getIntExtra("EXTRA_CATEGORY_COLOR_ID", R.color.eldritch_grey)
        binding.tvCategoryNameHeader.text = categoryName
        binding.categoryCardHeader.setCardBackgroundColor(ContextCompat.getColor(this, categoryColorId))

        setupTaskRecyclerView()
        setupTaskSwipeActions()
        loadTasks()

        binding.fabBack.setOnClickListener {
            finish()
        }

        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }
    }

    private fun setupTaskRecyclerView() {
        taskAdapter = TaskAdapter()
        binding.rvTasks.apply {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(this@TaskActivity)
        }
    }

    private fun loadTasks() {
        lifecycleScope.launch {
            val tasks = db.getTasksForCategory(categoryId)
            taskAdapter.submitList(tasks)
        }
    }

    private fun showAddTaskDialog() {
        val editText = EditText(this)
        editText.hint = "Nom de la nouvelle tâche..."

        MaterialAlertDialogBuilder(this)
            .setTitle("Ajouter une tâche")
            .setView(editText)
            .setNegativeButton("Annuler", null)
            .setPositiveButton("Ajouter") { _, _ ->
                val taskName = editText.text.toString().trim()
                if (taskName.isNotEmpty()) {
                    lifecycleScope.launch {
                        val newTask = Task(name = taskName, categoryId = categoryId)
                        db.insertTask(newTask)
                        loadTasks()
                    }
                }
            }
            .show()
    }
// Dans TaskActivity.kt

    private fun setupTaskSwipeActions() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(r: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val task = taskAdapter.currentList[position]
                when (direction) {
                    // On passe la position
                    ItemTouchHelper.LEFT -> showDeleteTaskDialog(task, position)
                    ItemTouchHelper.RIGHT -> showEditTaskDialog(task, position)
                }
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvTasks)
    }

    private fun showDeleteTaskDialog(task: Task, position: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirmation de Suppression")
            .setMessage("Voulez-vous vraiment supprimer la tâche \"${task.name}\" ?")
            .setNegativeButton("Annuler") { _, _ ->
                // ✅ LA CORRECTION
                taskAdapter.notifyItemChanged(position)
            }
            .setPositiveButton("Supprimer") { _, _ ->
                lifecycleScope.launch {
                    db.deleteTask(task)
                    loadTasks()
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun showEditTaskDialog(task: Task, position: Int) {
        val editText = EditText(this)
        editText.setText(task.name)
        MaterialAlertDialogBuilder(this)
            .setTitle("Modifier la tâche")
            .setView(editText)
            .setNegativeButton("Annuler") { _, _ ->
                // ✅ LA CORRECTION
                taskAdapter.notifyItemChanged(position)
            }
            .setPositiveButton("Valider") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    lifecycleScope.launch {
                        val updatedTask = task.copy(name = newName)
                        db.updateTask(updatedTask)
                        loadTasks()
                    }
                }
            }
            .setCancelable(false)
            .show()
    }
}