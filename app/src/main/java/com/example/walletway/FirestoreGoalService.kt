// FirestoreGoalService.kt
package com.example.walletway

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreGoalService {
    private val db = FirebaseFirestore.getInstance()
    private val goalsCollection = db.collection("goals")

    suspend fun getGoalForUser(userEmail: String): GoalEntity? {
        val snapshot = goalsCollection.document(userEmail).get().await()
        return snapshot.toObject(GoalEntity::class.java)
    }

    suspend fun setGoalForUser(userEmail: String, goal: GoalEntity) {
        goalsCollection.document(userEmail).set(goal).await()
    }
}
