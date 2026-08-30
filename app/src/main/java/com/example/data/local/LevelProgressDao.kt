package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelProgressDao {
    @Query("SELECT * FROM level_progress ORDER BY levelNumber ASC")
    fun getAllProgress(): Flow<List<LevelProgressEntity>>

    @Query("SELECT * FROM level_progress WHERE levelNumber = :levelNumber LIMIT 1")
    suspend fun getProgressForLevel(levelNumber: Int): LevelProgressEntity?

    @Query("SELECT * FROM level_progress WHERE levelNumber = :levelNumber LIMIT 1")
    fun observeProgressForLevel(levelNumber: Int): Flow<LevelProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: LevelProgressEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(progressList: List<LevelProgressEntity>)

    @Query("SELECT SUM(stars) FROM level_progress")
    fun getTotalStars(): Flow<Int?>

    @Query("SELECT MAX(levelNumber) FROM level_progress WHERE isUnlocked = 1")
    fun getHighestUnlockedLevel(): Flow<Int?>

    @Query("DELETE FROM level_progress")
    suspend fun clearAll()
}
