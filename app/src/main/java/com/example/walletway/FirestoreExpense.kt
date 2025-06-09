package com.example.walletway

data class FirestoreExpense(
    val id: String = "",
    val userEmail: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val date: String = "",
    val photoPath: String? = null
)
