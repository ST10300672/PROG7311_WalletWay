package com.example.walletway

import com.google.firebase.auth.FirebaseUser
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.material.MaterialTheme
import com.google.firebase.auth.FirebaseAuth

enum class ScreenState {
    BUDGET,
    CATEGORY,
    GOAL,
    EXPENSE_LIST,
    CATEGORY_SUMMARY
}

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        database = androidx.room.Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "walletway-db"
        ).fallbackToDestructiveMigration()
            .build()

        setContent {
            MaterialTheme {
                var user by remember { mutableStateOf(auth.currentUser) }
                var currentScreen by remember { mutableStateOf(ScreenState.BUDGET) }
                var reloadFlag by remember { mutableStateOf(false) }

                if (user != null) {
                    when (currentScreen) {
                        ScreenState.BUDGET -> BudgetScreen(
                            userEmail = user!!.email ?: "User",
                            onLogout = { auth.signOut(); user = null },
                            database = database,
                            onManageCategories = { currentScreen = ScreenState.CATEGORY },
                            onManageGoals = { currentScreen = ScreenState.GOAL },
                            onManageExpenses = { currentScreen = ScreenState.EXPENSE_LIST },
                            onViewCategorySummary = { currentScreen = ScreenState.CATEGORY_SUMMARY },
                            reloadFlag = reloadFlag
                        )

                        ScreenState.CATEGORY -> CategoryScreen(
                            database = database,
                            onBack = { currentScreen = ScreenState.BUDGET }
                        )

                        ScreenState.GOAL -> GoalScreen(
                            database = database,
                            onBack = { currentScreen = ScreenState.BUDGET },
                            onReload = { reloadFlag = !reloadFlag }
                        )

                        ScreenState.EXPENSE_LIST -> ExpenseListScreen(
                            database = database,
                            onBack = { currentScreen = ScreenState.BUDGET },
                            reloadFlag = reloadFlag
                        )

                        ScreenState.CATEGORY_SUMMARY -> CategorySummaryScreen(
                            database = database,
                            onBack = { currentScreen = ScreenState.BUDGET }
                        )
                    }
                } else {
                    AuthScreen(
                        onLogin = { email, password ->
                            loginUser(email, password) { loggedInUser ->
                                user = loggedInUser
                            }
                        },
                        onRegister = { email, password ->
                            registerUser(email, password) { loggedInUser ->
                                user = loggedInUser
                            }
                        }
                    )
                }
            }
        }
    }

    private fun loginUser(email: String, password: String, onResult: (user: FirebaseUser?) -> Unit) {
        if (email.isNotBlank() && password.isNotBlank()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        onResult(auth.currentUser)
                    } else {
                        Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun registerUser(email: String, password: String, onResult: (user: FirebaseUser?) -> Unit) {
        if (email.isNotBlank() && password.isNotBlank()) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                        onResult(auth.currentUser)
                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
