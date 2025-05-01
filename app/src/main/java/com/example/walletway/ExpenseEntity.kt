package com.example.walletway

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    val description: String,
    val amount: Double,
    val category: String,
    val date: String,
    val photoPath: String? = null,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)
