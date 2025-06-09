package com.example.walletway

import android.app.DatePickerDialog
import android.graphics.Color
import android.widget.LinearLayout
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CategorySummaryScreen(
    userEmail: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    val summaries = remember { mutableStateListOf<CategorySummary>() }
    val allSummaries = remember { mutableStateListOf<CategorySummary>() }
    val categories = remember { mutableStateListOf<String>() }
    val selectedCategory = remember { mutableStateOf<String?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }
    val goalService = remember { FirestoreGoalService() }
    val scope = rememberCoroutineScope()
    val calendar = Calendar.getInstance()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val firestoreService = remember { FirestoreService() }

    val startDatePickerDialog = remember {
        DatePickerDialog(context, { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            startDate = sdf.format(calendar.time)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
    }

    val endDatePickerDialog = remember {
        DatePickerDialog(context, { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            endDate = sdf.format(calendar.time)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
    }

    var goal by remember { mutableStateOf<GoalEntity?>(null) }
    LaunchedEffect(Unit) {
        goal = goalService.getGoalForUser(userEmail)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Spending Summary by Category", style = MaterialTheme.typography.h5)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { startDatePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
            Text(if (startDate.isBlank()) "Select Start Date" else "Start Date: $startDate")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { endDatePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
            Text(if (endDate.isBlank()) "Select End Date" else "End Date: $endDate")
        }

        Spacer(modifier = Modifier.height(8.dp))

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
                        summaries.addAll(allSummaries.filter { it.category == category })
                        categoryExpanded = false
                    }) {
                        Text(category)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            startDate = ""
            endDate = ""
            selectedCategory.value = null
            summaries.clear()
            allSummaries.clear()
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Reset Filters")
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Button(onClick = {
            scope.launch(Dispatchers.IO) {
                val allExpenses = firestoreService.getAllExpensesForUser(userEmail)

                val filteredExpenses = allExpenses.filter { expense ->
                    val expenseDate = try { sdf.parse(expense.date) } catch (_: Exception) { null }
                    val from = if (startDate.isNotBlank()) sdf.parse(startDate) else null
                    val to = if (endDate.isNotBlank()) sdf.parse(endDate) else null

                    (from == null || (expenseDate != null && expenseDate >= from)) &&
                            (to == null || (expenseDate != null && expenseDate <= to))
                }

                val grouped = filteredExpenses.groupBy { it.category }
                val summaryList = grouped.map { (category, group) ->
                    CategorySummary(category, group.sumOf { it.amount })
                }

                allSummaries.clear()
                allSummaries.addAll(summaryList)
                summaries.clear()
                summaries.addAll(
                    if (selectedCategory.value != null)
                        summaryList.filter { it.category == selectedCategory.value }
                    else summaryList
                )

                categories.clear()
                categories.addAll(summaryList.map { it.category }.distinct())
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Show Summary")
        }

        Spacer(modifier = Modifier.height(24.dp))

        summaries.forEach { summary ->
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

        Spacer(modifier = Modifier.height(24.dp))

        if (summaries.isNotEmpty()) {
            AndroidView(
                factory = { context ->
                    BarChart(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            600
                        )
                    }
                },
                update = { chart ->
                    val entries = ArrayList<BarEntry>()
                    val labels = ArrayList<String>()

                    summaries.forEachIndexed { index, summary ->
                        entries.add(BarEntry(index.toFloat(), summary.total.toFloat()))
                        labels.add(summary.category)
                    }

                    val dataSet = BarDataSet(entries, "Spending").apply {
                        colors = ColorTemplate.MATERIAL_COLORS.toList()
                        valueTextSize = 14f
                    }

                    chart.data = BarData(dataSet)
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                    chart.xAxis.granularity = 1f
                    chart.xAxis.labelRotationAngle = -45f
                    chart.axisLeft.axisMinimum = 0f
                    chart.axisRight.isEnabled = false
                    chart.description = Description().apply { text = "" }

                    goal?.let {
                        chart.axisLeft.removeAllLimitLines()
                        chart.axisLeft.addLimitLine(
                            LimitLine(it.minGoal.toFloat(), "Min Goal").apply {
                                lineColor = Color.GREEN
                                lineWidth = 2f
                                textColor = Color.BLACK
                            }
                        )
                        chart.axisLeft.addLimitLine(
                            LimitLine(it.maxGoal.toFloat(), "Max Goal").apply {
                                lineColor = Color.RED
                                lineWidth = 2f
                                textColor = Color.BLACK
                            }
                        )
                    }

                    chart.invalidate()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}


