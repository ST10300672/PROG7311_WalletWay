package com.example.walletway

import androidx.room.*

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insertExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses")
    suspend fun getAllExpenses(): List<ExpenseEntity>

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("""
    SELECT category, SUM(amount) as total 
    FROM expenses 
    WHERE date BETWEEN :startDate AND :endDate 
    GROUP BY category
""")
    suspend fun getCategoryTotalsBetweenDates(startDate: String, endDate: String): List<CategorySummary>

}
