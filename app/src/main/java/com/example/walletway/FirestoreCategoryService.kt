package com.example.walletway

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreCategoryService {
    private val db = FirebaseFirestore.getInstance()
    private val categoriesCollection = db.collection("categories")

    suspend fun getCategoriesForUser(userEmail: String): List<CategoryEntity> {
        return categoriesCollection
            .whereEqualTo("userEmail", userEmail)
            .get()
            .await()
            .toObjects(CategoryEntity::class.java)
    }

    suspend fun addCategory(category: CategoryEntity) {
        if (category.id.isNotBlank()) {
            categoriesCollection.document(category.id).set(category).await()
        }
    }

    suspend fun deleteCategory(categoryId: String) {
        if (categoryId.isNotBlank()) {
            categoriesCollection.document(categoryId).delete().await()
        }
    }
}
