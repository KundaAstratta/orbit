package com.example.orbit

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var name: String,
    val colorResId: Int
)