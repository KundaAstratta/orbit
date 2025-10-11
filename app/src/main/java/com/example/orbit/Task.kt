package com.example.orbit

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE // Supprime les tâches si la catégorie est supprimée
        )
    ]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var categoryId: Long, // Lien vers la catégorie parente
    var name: String,
    var isCompleted: Boolean = false
)