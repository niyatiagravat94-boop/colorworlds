package com.example.data.repository

import android.content.Context
import com.example.data.local.*
import com.example.data.model.LevelData
import com.example.data.model.WorldId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class GameRepository(context: Context, scope: CoroutineScope) {
    private val database = GameDatabase.getDatabase(context, scope)
    private val levelDao = database.levelProgressDao()
    val preferences = GamePreferences(context)

    val allProgressFlow: Flow<List<LevelProgressEntity>> = levelDao.getAllProgress()

    val totalStarsFlow: Flow<Int> = levelDao.getTotalStars().map { it ?: 0 }

    val highestUnlockedLevelFlow: Flow<Int> = levelDao.getHighestUnlockedLevel().map { it ?: 1 }

    suspend fun getProgressForLevel(levelNumber: Int): LevelProgressEntity {
        return withContext(Dispatchers.IO) {
            levelDao.getProgressForLevel(levelNumber) ?: LevelProgressEntity(
                levelNumber = levelNumber,
                isUnlocked = levelNumber == 1
            )
        }
    }

    suspend fun saveLevelCompletion(
        levelNumber: Int,
        score: Int,
        starsEarned: Int
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val currentProgress = levelDao.getProgressForLevel(levelNumber)
            val newHighScore = maxOf(currentProgress?.highScore ?: 0, score)
            val newStars = maxOf(currentProgress?.stars ?: 0, starsEarned)

            levelDao.insertOrUpdate(
                LevelProgressEntity(
                    levelNumber = levelNumber,
                    stars = newStars,
                    highScore = newHighScore,
                    isUnlocked = true,
                    isCompleted = true,
                    completedAtTimestamp = System.currentTimeMillis()
                )
            )

            // Unlock next level if exists
            val nextLevelNum = levelNumber + 1
            var unlockedNewWorld = false
            if (nextLevelNum <= LevelRepository.getTotalLevelCount()) {
                val nextProgress = levelDao.getProgressForLevel(nextLevelNum)
                if (nextProgress == null || !nextProgress.isUnlocked) {
                    levelDao.insertOrUpdate(
                        LevelProgressEntity(
                            levelNumber = nextLevelNum,
                            stars = nextProgress?.stars ?: 0,
                            highScore = nextProgress?.highScore ?: 0,
                            isUnlocked = true,
                            isCompleted = nextProgress?.isCompleted ?: false
                        )
                    )
                }

                val currentWorld = WorldId.forLevel(levelNumber)
                val nextWorld = WorldId.forLevel(nextLevelNum)
                if (currentWorld != nextWorld) {
                    unlockedNewWorld = true
                }
            }

            preferences.totalScoreEver += score
            unlockedNewWorld
        }
    }

    fun isWorldUnlocked(worldId: WorldId, progressList: List<LevelProgressEntity>): Boolean {
        if (worldId == WorldId.COLOR_GARDEN) return true
        val prevWorldEndLevel = when (worldId) {
            WorldId.COLOR_GARDEN -> 0
            WorldId.OCEAN_WORLD -> 10
            WorldId.MOUNTAIN_WORLD -> 20
            WorldId.SPACE_WORLD -> 30
            WorldId.CRYSTAL_WORLD -> 40
        }
        val prevLevel = progressList.firstOrNull { it.levelNumber == prevWorldEndLevel }
        return prevLevel?.isCompleted == true || progressList.any { it.levelNumber >= worldId.startLevel && it.isUnlocked }
    }

    suspend fun resetAllProgress() {
        withContext(Dispatchers.IO) {
            levelDao.clearAll()
            GameDatabase.prepopulateDatabase(levelDao)
            preferences.hasCompletedTutorial = false
            preferences.highestComboEver = 0
            preferences.hintsCount = 5
            preferences.undosCount = 5
        }
    }
}
