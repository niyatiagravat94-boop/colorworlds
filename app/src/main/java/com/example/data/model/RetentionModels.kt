package com.example.data.model

data class DailyRewardItem(
    val dayNumber: Int, // 1..7
    val title: String,
    val description: String,
    val hints: Int = 0,
    val undos: Int = 0,
    val bonusStars: Int = 0,
    val isClaimed: Boolean = false,
    val isAvailableToday: Boolean = false
)

enum class MissionType {
    PLAY_LEVELS,
    CLEAR_LINES,
    SCORE_POINTS,
    TRIGGER_COMBO,
    USE_BOOSTER
}

data class DailyMission(
    val id: String,
    val title: String,
    val type: MissionType,
    val currentProgress: Int,
    val targetProgress: Int,
    val hintsReward: Int,
    val undosReward: Int,
    val isClaimed: Boolean
) {
    val isCompleted: Boolean get() = currentProgress >= targetProgress
    val progressFraction: Float get() = (currentProgress.toFloat() / targetProgress.toFloat()).coerceIn(0f, 1f)
}

data class MilestoneAchievement(
    val id: String,
    val title: String,
    val description: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val hintsReward: Int = 0,
    val undosReward: Int = 0,
    val starsReward: Int = 0,
    val isClaimed: Boolean = false
) {
    val isCompleted: Boolean get() = currentProgress >= targetProgress
    val progressFraction: Float get() = (currentProgress.toFloat() / targetProgress.toFloat()).coerceIn(0f, 1f)
}
