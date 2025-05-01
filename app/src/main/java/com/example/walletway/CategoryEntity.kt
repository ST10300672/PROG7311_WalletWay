package com.example.walletway

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories") // ✅ Add this!
data class CategoryEntity(
    val name: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)
