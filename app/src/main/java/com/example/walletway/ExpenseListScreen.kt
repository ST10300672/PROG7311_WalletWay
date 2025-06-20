package com.example.walletway

import android.app.DatePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.map

@Composable
fun ExpenseListScreen(
    userEmail: String,
    onBack: () -> Unit,
    reloadFlag: Boolean
) {
    val context = LocalContext.current
    val expenses = remember { mutableStateListOf<Expense>() }
    val allExpenses = remember { mutableStateListOf<Expense>() }
    val categories = remember { mutableStateListOf<String>() }
    val selectedCategory = remember { mutableStateOf<String?>(null) }

    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedExpense by remember { mutableStateOf<Expense?>(null) }
    var newDescription by remember { mutableStateOf("") }
    var newAmount by remember { mutableStateOf("") }
    var fullImagePath by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    val scope = rememberCoroutineScope()
    val firestoreService = remember { FirestoreService() }

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()

    val startDatePicker = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                startDate = sdf.format(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val endDatePicker = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                endDate = sdf.format(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    LaunchedEffect(reloadFlag) {
        val all: List<Expense> = firestoreService.getAllExpensesForUser(userEmail)
        allExpenses.clear()
        allExpenses.addAll(all)
        expenses.clear()
        expenses.addAll(all)
        categories.clear()
        categories.addAll(all.map { it.category }.distinct())

    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Expense List", style = MaterialTheme.typography.h5)
            Spacer(modifier = Modifier.height(12.dp))

            // Category dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedCategory.value ?: "",
                    onValueChange = {},
                    label = { Text("Filter by Category") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { categoryExpanded = true },
                    readOnly = true,
                    enabled = false
                )
                DropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    DropdownMenuItem(onClick = {
                        selectedCategory.value = null
                        expenses.clear()
                        expenses.addAll(allExpenses)
                        categoryExpanded = false
                    }) {
                        Text("All Categories")
                    }
                    categories.forEach { category ->
                        DropdownMenuItem(onClick = {
                            selectedCategory.value = category
                            expenses.clear()
                            expenses.addAll(allExpenses.filter { it.category == category })
                            categoryExpanded = false
                        }) {
                            Text(category)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = { startDatePicker.show() }, modifier = Modifier.fillMaxWidth()) {
                Text(if (startDate.isBlank()) "Select Start Date" else "Start Date: $startDate")
            }

            Button(onClick = { endDatePicker.show() }, modifier = Modifier.fillMaxWidth()) {
                Text(if (endDate.isBlank()) "Select End Date" else "End Date: $endDate")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        val filtered = allExpenses.filter { expense ->
                            val matchCategory = selectedCategory.value == null || expense.category == selectedCategory.value
                            val matchDate = try {
                                val expenseDate = sdf.parse(expense.date)
                                val from = if (startDate.isNotBlank()) sdf.parse(startDate) else null
                                val to = if (endDate.isNotBlank()) sdf.parse(endDate) else null

                                when {
                                    from != null && to != null -> expenseDate != null && (expenseDate >= from && expenseDate <= to)
                                    from != null -> expenseDate != null && expenseDate >= from
                                    to != null -> expenseDate != null && expenseDate <= to
                                    else -> true
                                }
                            } catch (e: Exception) {
                                false
                            }
                            matchCategory && matchDate
                        }
                        expenses.clear()
                        expenses.addAll(filtered)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Date Filter")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    startDate = ""
                    endDate = ""
                    selectedCategory.value = null
                    scope.launch(Dispatchers.IO) {
                        val refreshed = firestoreService.getAllExpensesForUser(userEmail)
                        allExpenses.clear()
                        allExpenses.addAll(refreshed)
                        expenses.clear()
                        expenses.addAll(refreshed)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset Filters")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Add New Expense")
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        items(expenses) { expense ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedExpense = expense
                        newDescription = expense.description
                        newAmount = expense.amount.toString()
                        showEditDialog = true
                    },
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Amount: R${expense.amount}")
                    Text("Description: ${expense.description}")
                    Text("Category: ${expense.category}")
                    Text("Date: ${expense.date}")

                    expense.photoPath?.let { path ->
                        Image(
                            painter = rememberAsyncImagePainter(model = path),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .padding(top = 8.dp)
                                .clickable { fullImagePath = path }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            expenseToDelete = expense
                            showDeleteConfirm = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete Expense")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Back")
            }
        }
    }

    if (showEditDialog && selectedExpense != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Expense") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newDescription,
                        onValueChange = { newDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newAmount,
                        onValueChange = { newAmount = it },
                        label = { Text("Amount") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    selectedExpense?.let { expense ->
                        val updated = expense.copy(
                            description = newDescription,
                            amount = newAmount.toDoubleOrNull() ?: expense.amount
                        )
                        scope.launch(Dispatchers.IO) {
                            firestoreService.updateExpense(updated)
                            val refreshed = firestoreService.getAllExpensesForUser(userEmail)
                            allExpenses.clear()
                            allExpenses.addAll(refreshed)
                            expenses.clear()
                            expenses.addAll(refreshed)
                        }
                        showEditDialog = false
                    }
                }) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                Button(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteConfirm && expenseToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this expense?") },
            confirmButton = {
                Button(onClick = {
                    scope.launch(Dispatchers.IO) {
                        firestoreService.deleteExpense(expenseToDelete!!.id)
                        val refreshed = firestoreService.getAllExpensesForUser(userEmail)
                        allExpenses.clear()
                        allExpenses.addAll(refreshed)
                        expenses.clear()
                        expenses.addAll(refreshed)
                    }
                    showDeleteConfirm = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (fullImagePath != null) {
        AlertDialog(
            onDismissRequest = { fullImagePath = null },
            buttons = {},
            text = {
                Image(
                    painter = rememberAsyncImagePainter(model = fullImagePath!!),
                    contentDescription = "Full Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                )
            }
        )
    }
}

