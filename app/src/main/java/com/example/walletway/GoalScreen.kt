package com.example.walletway

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun GoalScreen(
    userEmail: String,
    onBack: () -> Unit,
    onReload: () -> Unit
) {
    var minGoal by remember { mutableStateOf("") }
    var maxGoal by remember { mutableStateOf("") }

    val firestoreGoalService = remember { FirestoreGoalService() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val goal = firestoreGoalService.getGoalForUser(userEmail)
            goal?.let {
                minGoal = it.minGoal.toString()
                maxGoal = it.maxGoal.toString()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Set Monthly Goals", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = minGoal,
            onValueChange = { minGoal = it },
            label = { Text("Minimum Spending Goal") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = maxGoal,
            onValueChange = { maxGoal = it },
            label = { Text("Maximum Spending Goal") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val min = minGoal.toDoubleOrNull() ?: 0.0
                val max = maxGoal.toDoubleOrNull() ?: 0.0
                val newGoal = GoalEntity(min, max)

                scope.launch(Dispatchers.IO) {
                    firestoreGoalService.setGoalForUser(userEmail, newGoal)
                }

                onReload()
                onBack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Goals")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Back")
        }
    }
}
