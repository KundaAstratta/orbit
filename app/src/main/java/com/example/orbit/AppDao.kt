package com.example.orbit

import androidx.room.*

@Dao
interface AppDao {
    // --- Opérations sur les Catégories ---
    @Insert
    suspend fun insertCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM Category ORDER BY id ASC")
    suspend fun getAllCategories(): List<Category>

    // --- Opérations sur les Tâches ---
    @Insert
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM Task WHERE categoryId = :categoryId ORDER BY id ASC")
    suspend fun getTasksForCategory(categoryId: Long): List<Task>
}