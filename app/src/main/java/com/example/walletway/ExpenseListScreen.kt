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
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpenseListScreen(
    database: AppDatabase,
    onBack: () -> Unit,
    reloadFlag: Boolean
) {
    val context = LocalContext.current
    val expenses = remember { mutableStateListOf<ExpenseEntity>() }
    val allExpenses = remember { mutableStateListOf<ExpenseEntity>() }
    val categories = remember { mutableStateListOf<CategoryEntity>() }
    val selectedCategory = remember { mutableStateOf<CategoryEntity?>(null) }

    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    var newDescription by remember { mutableStateOf("") }
    var newAmount by remember { mutableStateOf("") }
    var fullImagePath by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }

    val scope = rememberCoroutineScope()

    // Load expenses and categories
    LaunchedEffect(reloadFlag) {
        val dbExpenses = database.expenseDao().getAllExpenses()
        allExpenses.clear()
        allExpenses.addAll(dbExpenses)
        expenses.clear()
        expenses.addAll(dbExpenses)

        val dbCategories = database.categoryDao().getAllCategories()
        categories.clear()
        categories.addAll(dbCategories)
    }

    val calendar = Calendar.getInstance()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Expense List", style = MaterialTheme.typography.h5)
            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedCategory.value?.name ?: "",
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
                            expenses.addAll(allExpenses.filter { it.category == category.name })
                            categoryExpanded = false
                        }) {
                            Text(category.name)
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

            Divider(modifier = Modifier.padding(vertical = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        val filtered = allExpenses.filter { expense ->
                            val matchCategory = selectedCategory.value?.name == null || expense.category == selectedCategory.value?.name
                            val matchDate = try {
                                val expenseDate = sdf.parse(expense.date)
                                val fromDate = if (startDate.isNotBlank()) sdf.parse(startDate) else null
                                val toDate = if (endDate.isNotBlank()) sdf.parse(endDate) else null

                                when {
                                    fromDate != null && toDate != null -> expenseDate != null &&
                                            (expenseDate.after(fromDate) || expenseDate == fromDate) &&
                                            (expenseDate.before(toDate) || expenseDate == toDate)
                                    fromDate != null -> expenseDate != null && (expenseDate.after(fromDate) || expenseDate == fromDate)
                                    toDate != null -> expenseDate != null && (expenseDate.before(toDate) || expenseDate == toDate)
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
                        val all = database.expenseDao().getAllExpenses()
                        expenses.clear()
                        expenses.addAll(all)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset Filters")
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Add New Expense")
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))
        }

        item { Text("Filtered Expenses", style = MaterialTheme.typography.subtitle1) }

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
                        val file = File(path)
                        if (file.exists()) {
                            Image(
                                painter = rememberAsyncImagePainter(model = file),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .padding(top = 8.dp)
                                    .clickable { fullImagePath = path }
                            )
                        }
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

    if (fullImagePath != null) {
        AlertDialog(
            onDismissRequest = { fullImagePath = null },
            buttons = {},
            text = {
                Image(
                    painter = rememberAsyncImagePainter(model = File(fullImagePath!!)),
                    contentDescription = "Full Image",
                    modifier = Modifier.fillMaxWidth().height(400.dp)
                )
            }
        )
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
                            database.expenseDao().updateExpense(updated)
                            val updatedExpenses = database.expenseDao().getAllExpenses()
                            allExpenses.clear()
                            allExpenses.addAll(updatedExpenses)
                            expenses.clear()
                            expenses.addAll(updatedExpenses)
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
                        database.expenseDao().deleteExpense(expenseToDelete!!)
                        val updated = database.expenseDao().getAllExpenses()
                        allExpenses.clear()
                        allExpenses.addAll(updated)
                        expenses.clear()
                        expenses.addAll(updated)
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
}
