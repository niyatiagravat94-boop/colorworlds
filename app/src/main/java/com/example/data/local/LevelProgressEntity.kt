package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey
    val levelNumber: Int,
    val stars: Int = 0,
    val highScore: Int = 0,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false,
    val completedAtTimestamp: Long = 0L
)
