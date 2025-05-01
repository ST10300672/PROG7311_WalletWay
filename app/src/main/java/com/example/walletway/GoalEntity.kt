package com.example.walletway

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    val minGoal: Double,
    val maxGoal: Double,
    @PrimaryKey val id: Int = 0 // Always only 1 row
)
