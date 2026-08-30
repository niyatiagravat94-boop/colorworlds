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
    val levelProgressRepository = LevelProgressRepository(levelDao)
    val preferences = GamePreferences(context)

    val allProgressFlow: Flow<List<LevelProgressEntity>> = levelProgressRepository.allProgressFlow

    val totalStarsFlow: Flow<Int> = levelProgressRepository.totalStarsFlow

    val highestUnlockedLevelFlow: Flow<Int> = levelProgressRepository.highestUnlockedLevelFlow

    suspend fun getProgressForLevel(levelNumber: Int): LevelProgressEntity {
        return levelProgressRepository.getLevelProgress(levelNumber)
    }

    suspend fun saveLevelCompletion(
        levelNumber: Int,
        score: Int,
        starsEarned: Int
    ): Boolean {
        val levelData = LevelRepository.getLevel(levelNumber)
        val result = levelProgressRepository.saveLevelCompletion(levelData, score, starsEarned)
        preferences.totalScoreEver += score
        return result.unlockedNewWorld
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
