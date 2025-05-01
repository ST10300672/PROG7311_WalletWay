package com.example.walletway

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun CategoryScreen(
    database: AppDatabase,
    onBack: () -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    val categories = remember { mutableStateListOf<CategoryEntity>() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val dbCategories = database.categoryDao().getAllCategories()
        categories.clear()
        categories.addAll(dbCategories)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Manage Categories", style = MaterialTheme.typography.h5)

        OutlinedTextField(
            value = categoryName,
            onValueChange = { categoryName = it },
            label = { Text("Category Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val newCategory = CategoryEntity(name = categoryName)
                scope.launch(Dispatchers.IO) {
                    database.categoryDao().insertCategory(newCategory)
                    val updatedCategories = database.categoryDao().getAllCategories()
                    categories.clear()
                    categories.addAll(updatedCategories)
                }
                categoryName = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Category")
        }

        Divider(modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(categories) { category ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.subtitle1
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    database.categoryDao().deleteCategory(category)
                                    val updated = database.categoryDao().getAllCategories()
                                    categories.clear()
                                    categories.addAll(updated)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}
