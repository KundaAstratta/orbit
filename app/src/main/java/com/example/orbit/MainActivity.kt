package com.example.orbit

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.orbit.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import android.app.ActivityOptions // Importez cette classe
import android.view.View // Et celle-ci

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var categoryAdapter: CategoryAdapter
    // Initialisation de l'accès à la base de données
    private val db by lazy { AppDatabase.getDatabase(this).appDao() }

    private val categoryColors by lazy {
        listOf(
            R.color.cat_color_1, R.color.cat_color_2, R.color.cat_color_3,
            R.color.cat_color_4, R.color.cat_color_5
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSwipeActions()
        loadCategories() // Charger les données au démarrage

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.fabAddTask.setOnClickListener {
            addCategory()
        }
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter { category, view ->
            val intent = Intent(this, TaskActivity::class.java).apply {
                putExtra("EXTRA_CATEGORY_NAME", category.name)
                putExtra("EXTRA_CATEGORY_COLOR_ID", category.colorResId)
                putExtra("EXTRA_CATEGORY_ID", category.id)
            }

            val options = ActivityOptions.makeSceneTransitionAnimation(
                this,
                view,
                ViewCompat.getTransitionName(view)!! // On récupère le nom qu'on a défini
            )


            startActivity(intent, options.toBundle())
        }

        binding.rvCategories.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val categories = db.getAllCategories()
            categoryAdapter.submitList(categories)
        }
    }

    private fun addCategory() {
        lifecycleScope.launch {
            val categoryCount = db.getAllCategories().size
            val newName = "Nouvelle Catégorie ${categoryCount + 1}"
            val newColor = categoryColors[categoryCount % categoryColors.size]
            val newCategory = Category(name = newName, colorResId = newColor)
            db.insertCategory(newCategory)
            loadCategories() // Recharger la liste depuis la DB
        }
    }
// Dans MainActivity.kt

    private fun setupSwipeActions() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(r: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val category = categoryAdapter.currentList[position]
                when (direction) {
                    // On passe la position en plus de la catégorie
                    ItemTouchHelper.LEFT -> showDeleteConfirmationDialog(category, position)
                    ItemTouchHelper.RIGHT -> showEditCategoryDialog(category, position)
                }
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvCategories)
    }

    private fun showDeleteConfirmationDialog(category: Category, position: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirmation de Suppression")
            .setMessage("Voulez-vous vraiment supprimer la catégorie \"${category.name}\" ?")
            .setNegativeButton("Annuler") { _, _ ->
                // ✅ LA CORRECTION : On notifie l'adaptateur de redessiner l'élément à sa position
                categoryAdapter.notifyItemChanged(position)
            }
            .setPositiveButton("Supprimer") { _, _ ->
                lifecycleScope.launch {
                    db.deleteCategory(category)
                    loadCategories()
                }
            }
            .setCancelable(false) // Empêche la fermeture accidentelle
            .show()
    }

    private fun showEditCategoryDialog(category: Category, position: Int) {
        val editText = EditText(this)
        editText.setText(category.name)
        MaterialAlertDialogBuilder(this)
            .setTitle("Modifier le nom")
            .setView(editText)
            .setNegativeButton("Annuler") { _, _ ->
                // ✅ LA CORRECTION : On fait la même chose ici
                categoryAdapter.notifyItemChanged(position)
            }
            .setPositiveButton("Valider") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    lifecycleScope.launch {
                        val updatedCategory = category.copy(name = newName)
                        db.updateCategory(updatedCategory)
                        loadCategories()
                    }
                }
            }
            .setCancelable(false)
            .show()
    }
}