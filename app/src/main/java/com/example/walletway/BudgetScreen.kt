package com.example.walletway

import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun BudgetScreen(
    userEmail: String,
    onLogout: () -> Unit,
    database: AppDatabase,
    onManageCategories: () -> Unit,
    onManageGoals: () -> Unit,
    onManageExpenses: () -> Unit,
    reloadFlag: Boolean,
    onViewCategorySummary: () -> Unit,
    onNavigateToCategoryChart: () -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    val expenses = remember { mutableStateListOf<ExpenseEntity>() }
    val categories = remember { mutableStateListOf<CategoryEntity>() }
    var minGoal by remember { mutableStateOf<Double?>(null) }
    var maxGoal by remember { mutableStateOf<Double?>(null) }
    val selectedCategory = remember { mutableStateOf<CategoryEntity?>(null) }
    val selectedPhotoPath = remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(reloadFlag) {
        val firestoreService = FirestoreService()
        val dbExpenses = firestoreService.getAllExpensesForUser(userEmail)
        expenses.clear()
        expenses.addAll(dbExpenses.map {
            ExpenseEntity(
                id = it.id,
                description = it.description,
                amount = it.amount,
                category = it.category,
                date = it.date,
                photoPath = it.photoPath
            )
        })


        val firestoreCategoryService = FirestoreCategoryService()
        val dbCategories = firestoreCategoryService.getCategoriesForUser(userEmail)
        categories.clear()
        categories.addAll(dbCategories)

        val goalService = FirestoreGoalService()
        val goal = goalService.getGoalForUser(userEmail)
        minGoal = goal?.minGoal
        maxGoal = goal?.maxGoal
    }

    val total = expenses.sumOf { it.amount }
    val overspent = maxGoal != null && total > maxGoal!!

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val copiedPath = copyImageToInternalStorage(context, it)
            selectedPhotoPath.value = copiedPath
        }
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                Text("Welcome, $userEmail", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Total Spent: R$total",
                    style = MaterialTheme.typography.subtitle1.copy(
                        color = when {
                            maxGoal != null && total > maxGoal!! -> MaterialTheme.colors.error
                            minGoal != null && total < minGoal!! -> Color.Gray
                            else -> MaterialTheme.colors.onBackground
                        }
                    )
                )

                if (minGoal != null || maxGoal != null) {
                    Spacer(modifier = Modifier.height(4.dp))

                    val goalInfo = buildString {
                        if (minGoal != null) append("Min: R${minGoal!!.toInt()} ")
                        if (maxGoal != null) append("Max: R${maxGoal!!.toInt()}")
                    }
                    Text(goalInfo, style = MaterialTheme.typography.caption)

                    Spacer(modifier = Modifier.height(4.dp))

                    // Progress relative to maxGoal if available, else fallback to dummy 1f
                    val progress = when {
                        maxGoal != null -> (total / maxGoal!!).toFloat().coerceIn(0f, 1f)
                        else -> 1f
                    }

                    val barColor = when {
                        maxGoal != null && total > maxGoal!! -> Color.Red
                        minGoal != null && total < minGoal!! -> Color.Gray
                        else -> MaterialTheme.colors.primary
                    }

                    LinearProgressIndicator(
                        progress = progress,
                        color = barColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )
                }
                

                Spacer(modifier = Modifier.height(16.dp))


                Card(elevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("Amount") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        var expanded by remember { mutableStateOf(false) }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true }
                        ) {
                            OutlinedTextField(
                                value = selectedCategory.value?.name ?: "",
                                onValueChange = {},
                                label = { Text("Select Category") },
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                enabled = false
                            )

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                categories.forEach { category ->
                                    DropdownMenuItem(onClick = {
                                        selectedCategory.value = category
                                        expanded = false
                                    }) {
                                        Text(text = category.name)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = date,
                            onValueChange = {},
                            label = { Text("Select Date") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { datePickerDialog.show() },
                            readOnly = true,
                            enabled = false
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Pick Photo (Optional)")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (description.isBlank() || amount.isBlank() || date.isBlank()) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Please fill in all fields before adding an expense.")
                                    }
                                    return@Button
                                }

                                val expenseAmount = amount.toDoubleOrNull()
                                if (expenseAmount == null) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Invalid amount format.")
                                    }
                                    return@Button
                                }

                                val newExpense = ExpenseEntity(
                                    id = UUID.randomUUID().toString(),
                                    description = description,
                                    amount = expenseAmount,
                                    category = selectedCategory.value?.name ?: "Uncategorized",
                                    date = date,
                                    photoPath = selectedPhotoPath.value
                                )

                                val expenseMap = hashMapOf(
                                    "id" to newExpense.id,
                                    "userEmail" to userEmail,
                                    "description" to newExpense.description,
                                    "amount" to newExpense.amount,
                                    "category" to newExpense.category,
                                    "date" to newExpense.date,
                                    "photoPath" to newExpense.photoPath
                                )

                                Firebase.firestore.collection("expenses")
                                    .document(newExpense.id)
                                    .set(expenseMap)
                                    .addOnSuccessListener {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Expense added to Firestore.")
                                            expenses.add(newExpense)
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Error adding expense: ${e.localizedMessage}")
                                        }
                                    }

                                description = ""
                                amount = ""
                                date = ""
                                selectedCategory.value = null
                                selectedPhotoPath.value = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add Expense")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = onManageCategories, modifier = Modifier.fillMaxWidth()) {
                        Text("Manage Categories")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onManageGoals, modifier = Modifier.fillMaxWidth()) {
                        Text("Manage Goals")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onManageExpenses, modifier = Modifier.fillMaxWidth()) {
                        Text("Manage Expenses")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onViewCategorySummary, modifier = Modifier.fillMaxWidth()) {
                        Text("View Category Spending Summary")
                    }
                    Button(onClick = onNavigateToCategoryChart, modifier = Modifier.fillMaxWidth()) {
                        Text("View Category Spending Graph")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Recent Expenses", style = MaterialTheme.typography.subtitle1)
            }

            items(expenses) { expense ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("R${expense.amount} - ${expense.description}")
                        Text("Category: ${expense.category}", style = MaterialTheme.typography.caption)
                        Text("Date: ${expense.date}", style = MaterialTheme.typography.caption)
                        expense.photoPath?.let { uri ->
                            Image(
                                painter = rememberAsyncImagePainter(model = uri),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Button(onClick = onLogout) {
                        Text("Logout")
                    }
                }
            }
        }
    }
}

