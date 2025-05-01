package com.example.walletway

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CategorySummaryScreen(
    database: AppDatabase,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    val summaries = remember { mutableStateListOf<CategorySummary>() }
    val allSummaries = remember { mutableStateListOf<CategorySummary>() }
    val categories = remember { mutableStateListOf<CategoryEntity>() }
    val selectedCategory = remember { mutableStateOf<CategoryEntity?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }


    val scope = rememberCoroutineScope()
    val calendar = Calendar.getInstance()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val startDatePickerDialog = remember {
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

    val endDatePickerDialog = remember {
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

    // Load categories once
    LaunchedEffect(Unit) {
        val dbCategories = database.categoryDao().getAllCategories()
        categories.clear()
        categories.addAll(dbCategories)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Spending Summary by Category", style = MaterialTheme.typography.h5)

        Spacer(modifier = Modifier.height(16.dp))

        // Date pickers
        Button(
            onClick = { startDatePickerDialog.show() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (startDate.isBlank()) "Select Start Date" else "Start Date: $startDate")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { endDatePickerDialog.show() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (endDate.isBlank()) "Select End Date" else "End Date: $endDate")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category dropdown filter
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
                    summaries.clear()
                    summaries.addAll(allSummaries)
                    categoryExpanded = false
                }) {
                    Text("All Categories")
                }

                categories.forEach { category ->
                    DropdownMenuItem(onClick = {
                        selectedCategory.value = category
                        summaries.clear()
                        summaries.addAll(allSummaries.filter { it.category == category.name })
                        categoryExpanded = false
                    }) {
                        Text(category.name)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Reset filters
        Button(
            onClick = {
                startDate = ""
                endDate = ""
                selectedCategory.value = null
                summaries.clear()
                allSummaries.clear()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset Filters")
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Show summary
        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    val result = database.expenseDao()
                        .getCategoryTotalsBetweenDates(startDate, endDate)
                    allSummaries.clear()
                    allSummaries.addAll(result)
                    summaries.clear()
                    summaries.addAll(
                        if (selectedCategory.value != null)
                            result.filter { it.category == selectedCategory.value?.name }
                        else result
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Show Summary")
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn {
            items(summaries) { summary ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Category: ${summary.category}")
                        Text("Total Spent: R${summary.total}")
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
    }
}
