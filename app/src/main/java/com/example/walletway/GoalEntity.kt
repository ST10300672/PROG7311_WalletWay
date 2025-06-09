package com.example.walletway

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    val minGoal: Double = 0.0,
    val maxGoal: Double = 0.0,
    @PrimaryKey val id: Int = 0 // Always only 1 row
)
