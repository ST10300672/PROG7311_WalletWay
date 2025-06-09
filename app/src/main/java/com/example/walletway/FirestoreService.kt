package com.example.walletway

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreService {
    private val db = FirebaseFirestore.getInstance()
    private val expenseCollection = db.collection("expenses")

    suspend fun addExpense(expense: Expense) {
        val docRef = expenseCollection.document(expense.id)
        docRef.set(expense).await()
    }

    suspend fun getAllExpensesForUser(userEmail: String): List<Expense> {
        return expenseCollection
            .whereEqualTo("userEmail", userEmail)
            .get()
            .await()
            .toObjects(Expense::class.java)
    }

    suspend fun updateExpense(expense: Expense) {
        if (expense.id.isNotBlank()) {
            expenseCollection.document(expense.id).set(expense).await()
        }
    }

    suspend fun deleteExpense(expenseId: String) {
        if (expenseId.isNotBlank()) {
            expenseCollection.document(expenseId).delete().await()
        }
    }

    suspend fun getExpensesByDateRange(
        userEmail: String,
        startDate: String,
        endDate: String
    ): List<Expense> {
        return expenseCollection
            .whereEqualTo("userEmail", userEmail)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .get()
            .await()
            .toObjects(Expense::class.java)
    }
}
