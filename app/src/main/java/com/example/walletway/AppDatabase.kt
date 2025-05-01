package com.example.walletway

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ExpenseEntity::class, CategoryEntity::class, GoalEntity::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun goalDao(): GoalDao
}

