package com.example.data.repository

import com.example.data.local.LevelProgressDao
import com.example.data.local.LevelProgressEntity
import com.example.data.model.LevelData
import com.example.data.model.WorldId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository for managing per-level progress, star ratings, and best scores in Room database.
 * Ensures that whenever a player replays a level, the highest earned star rating and
 * high score are strictly preserved.
 */
class LevelProgressRepository(
    private val levelDao: LevelProgressDao
) {
    val allProgressFlow: Flow<List<LevelProgressEntity>> = levelDao.getAllProgress()

    val totalStarsFlow: Flow<Int> = levelDao.getTotalStars().map { it ?: 0 }

    val highestUnlockedLevelFlow: Flow<Int> = levelDao.getHighestUnlockedLevel().map { it ?: 1 }

    fun observeLevelProgress(levelNumber: Int): Flow<LevelProgressEntity?> {
        return levelDao.observeProgressForLevel(levelNumber)
    }

    suspend fun getLevelProgress(levelNumber: Int): LevelProgressEntity {
        return withContext(Dispatchers.IO) {
            levelDao.getProgressForLevel(levelNumber) ?: LevelProgressEntity(
                levelNumber = levelNumber,
                isUnlocked = levelNumber == 1
            )
        }
    }

    /**
     * Dynamically calculates stars earned based on level-specific score thresholds.
     * Guaranteed at least 1 star upon level completion.
     */
    fun calculateStarsForScore(levelData: LevelData, score: Int): Int {
        return when {
            score >= levelData.threeStarScore -> 3
            score >= levelData.twoStarScore -> 2
            else -> 1
        }
    }

    /**
     * Saves level completion, updating high score and stars while strictly preserving the best result.
     * Unlocks the subsequent level and determines if a new world was unlocked.
     */
    suspend fun saveLevelCompletion(
        levelData: LevelData,
        score: Int,
        starsEarned: Int
    ): SaveProgressResult {
        return withContext(Dispatchers.IO) {
            val levelNumber = levelData.levelNumber
            val currentProgress = levelDao.getProgressForLevel(levelNumber)

            // Monotonic preservation: Keep highest stars & score
            val previousStars = currentProgress?.stars ?: 0
            val previousBestScore = currentProgress?.highScore ?: 0
            val bestStars = maxOf(previousStars, starsEarned)
            val bestScore = maxOf(previousBestScore, score)

            levelDao.insertOrUpdate(
                LevelProgressEntity(
                    levelNumber = levelNumber,
                    stars = bestStars,
                    highScore = bestScore,
                    isUnlocked = true,
                    isCompleted = true,
                    completedAtTimestamp = System.currentTimeMillis()
                )
            )

            // Unlock next level if available
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

            SaveProgressResult(
                calculatedStars = starsEarned,
                persistedBestStars = bestStars,
                persistedBestScore = bestScore,
                isNewBestScore = score > previousBestScore,
                isNewBestStars = starsEarned > previousStars,
                unlockedNewWorld = unlockedNewWorld
            )
        }
    }

    suspend fun resetAll() {
        withContext(Dispatchers.IO) {
            levelDao.clearAll()
        }
    }
}

data class SaveProgressResult(
    val calculatedStars: Int,
    val persistedBestStars: Int,
    val persistedBestScore: Int,
    val isNewBestScore: Boolean,
    val isNewBestStars: Boolean,
    val unlockedNewWorld: Boolean
)
