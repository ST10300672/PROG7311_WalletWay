package com.example.walletway

import android.app.DatePickerDialog
import android.graphics.Color
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CategorySpendingChartScreen(
    userEmail: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val chartView = remember { mutableStateOf<BarChart?>(null) }

    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val startPicker = DatePickerDialog(context, { _, y, m, d ->
        startDate = String.format("%04d-%02d-%02d", y, m + 1, d)
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    val endPicker = DatePickerDialog(context, { _, y, m, d ->
        endDate = String.format("%04d-%02d-%02d", y, m + 1, d)
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Category Spending Chart", style = MaterialTheme.typography.h6)
        Spacer(Modifier.height(12.dp))

        Button(onClick = { startPicker.show() }, modifier = Modifier.fillMaxWidth()) {
            Text(if (startDate.isEmpty()) "Select Start Date" else "Start: $startDate")
        }
        Spacer(Modifier.height(8.dp))

        Button(onClick = { endPicker.show() }, modifier = Modifier.fillMaxWidth()) {
            Text(if (endDate.isEmpty()) "Select End Date" else "End: $endDate")
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    val firestore = FirestoreService()
                    val goalService = FirestoreGoalService()

                    val expenses = firestore.getExpensesByDateRange(userEmail, startDate, endDate)
                    val goal = goalService.getGoalForUser(userEmail)

                    val grouped = expenses.groupBy { it.date }
                    val sortedDates = grouped.keys.sorted()
                    val categories = expenses.map { it.category }.distinct().sorted()

                    val dataSets = categories.mapIndexedNotNull { catIndex, category ->
                        val entries = sortedDates.mapIndexedNotNull { dateIdx, date ->
                            val total = grouped[date]
                                ?.filter { it.category == category }
                                ?.sumOf { it.amount } ?: 0.0
                            if (total > 0) BarEntry(dateIdx.toFloat(), total.toFloat()) else null
                        }
                        if (entries.isNotEmpty()) {
                            BarDataSet(entries, category).apply {
                                color = ColorTemplate.MATERIAL_COLORS[catIndex % ColorTemplate.MATERIAL_COLORS.size]
                                valueTextSize = 10f
                                setDrawValues(true)
                            }
                        } else null
                    }

                    chartView.value?.apply {
                        data = BarData(dataSets).apply {
                            barWidth = 0.25f
                        }

                        xAxis.apply {
                            valueFormatter = IndexAxisValueFormatter(sortedDates)
                            position = XAxis.XAxisPosition.BOTTOM
                            granularity = 1f
                            setDrawGridLines(false)
                            labelRotationAngle = -35f
                            textSize = 11f
                        }

                        axisLeft.apply {
                            axisMinimum = 0f
                            textSize = 11f
                            removeAllLimitLines()
                            goal?.minGoal?.let {
                                addLimitLine(LimitLine(it.toFloat(), "Min Goal").apply {
                                    lineColor = Color.BLUE
                                    textColor = Color.BLUE
                                    lineWidth = 1.5f
                                    textSize = 10f
                                    labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                                })
                            }
                            goal?.maxGoal?.let {
                                addLimitLine(LimitLine(it.toFloat(), "Max Goal").apply {
                                    lineColor = Color.RED
                                    textColor = Color.RED
                                    lineWidth = 1.5f
                                    textSize = 10f
                                    labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                                })
                            }
                        }

                        axisRight.isEnabled = false

                        legend.apply {
                            isEnabled = true
                            textSize = 12f
                            form = Legend.LegendForm.SQUARE
                            orientation = Legend.LegendOrientation.VERTICAL
                            verticalAlignment = Legend.LegendVerticalAlignment.TOP
                            horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                            setDrawInside(false)
                            yEntrySpace = 10f
                        }

                        description.isEnabled = false
                        invalidate()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate Chart")
        }

        Spacer(Modifier.height(16.dp))

        AndroidView(
            factory = { ctx ->
                BarChart(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        600
                    )
                    chartView.value = this
                    description.isEnabled = false
                    setFitBars(true)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
        )

        Spacer(Modifier.height(16.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}



