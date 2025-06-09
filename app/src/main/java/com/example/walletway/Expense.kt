package com.example.walletway

data class Expense(
    var id: String = "",
    var description: String = "",
    var amount: Double = 0.0,
    var category: String = "",
    var date: String = "",
    var photoPath: String? = null,
    var userEmail: String = ""
)
