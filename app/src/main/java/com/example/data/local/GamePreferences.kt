package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

enum class GraphicsQuality {
    LOW,
    MEDIUM,
    HIGH
}

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("color_worlds_prefs", Context.MODE_PRIVATE)

    var isMusicEnabled: Boolean
        get() = prefs.getBoolean(KEY_MUSIC, true)
        set(value) = prefs.edit().putBoolean(KEY_MUSIC, value).apply()

    var musicVolume: Float
        get() = prefs.getFloat(KEY_MUSIC_VOLUME, 0.65f)
        set(value) = prefs.edit().putFloat(KEY_MUSIC_VOLUME, value.coerceIn(0f, 1f)).apply()

    var isSfxEnabled: Boolean
        get() = prefs.getBoolean(KEY_SFX, true)
        set(value) = prefs.edit().putBoolean(KEY_SFX, value).apply()

    var sfxVolume: Float
        get() = prefs.getFloat(KEY_SFX_VOLUME, 0.85f)
        set(value) = prefs.edit().putFloat(KEY_SFX_VOLUME, value.coerceIn(0f, 1f)).apply()

    var isHapticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTICS, value).apply()

    var graphicsQuality: GraphicsQuality
        get() {
            val name = prefs.getString(KEY_GRAPHICS, GraphicsQuality.HIGH.name) ?: GraphicsQuality.HIGH.name
            return try {
                GraphicsQuality.valueOf(name)
            } catch (e: Exception) {
                GraphicsQuality.HIGH
            }
        }
        set(value) = prefs.edit().putString(KEY_GRAPHICS, value.name).apply()

    var hasCompletedTutorial: Boolean
        get() = prefs.getBoolean(KEY_TUTORIAL, false)
        set(value) = prefs.edit().putBoolean(KEY_TUTORIAL, value).apply()

    var hintsCount: Int
        get() = prefs.getInt(KEY_HINTS, 5)
        set(value) = prefs.edit().putInt(KEY_HINTS, value).apply()

    var undosCount: Int
        get() = prefs.getInt(KEY_UNDOS, 5)
        set(value) = prefs.edit().putInt(KEY_UNDOS, value).apply()

    var highestComboEver: Int
        get() = prefs.getInt(KEY_HIGHEST_COMBO, 0)
        set(value) = prefs.edit().putInt(KEY_HIGHEST_COMBO, value).apply()

    var totalScoreEver: Long
        get() = prefs.getLong(KEY_TOTAL_SCORE, 0L)
        set(value) = prefs.edit().putLong(KEY_TOTAL_SCORE, value).apply()

    var lastLoginDate: String
        get() = prefs.getString(KEY_LAST_LOGIN_DATE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_LOGIN_DATE, value).apply()

    var loginStreakDays: Int
        get() = prefs.getInt(KEY_LOGIN_STREAK, 1)
        set(value) = prefs.edit().putInt(KEY_LOGIN_STREAK, value).apply()

    var lastRewardClaimDate: String
        get() = prefs.getString(KEY_LAST_REWARD_CLAIM_DATE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_REWARD_CLAIM_DATE, value).apply()

    var currentStreakCycleDay: Int
        get() = prefs.getInt(KEY_STREAK_CYCLE_DAY, 1)
        set(value) = prefs.edit().putInt(KEY_STREAK_CYCLE_DAY, value).apply()

    var totalLinesClearedEver: Int
        get() = prefs.getInt(KEY_TOTAL_LINES_EVER, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_LINES_EVER, value).apply()

    var totalLevelsCompletedEver: Int
        get() = prefs.getInt(KEY_TOTAL_LEVELS_EVER, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_LEVELS_EVER, value).apply()

    // Daily missions progress for current date
    fun getDailyMissionProgress(missionId: String, date: String): Int {
        return prefs.getInt("daily_mission_${date}_$missionId", 0)
    }

    fun setDailyMissionProgress(missionId: String, date: String, progress: Int) {
        prefs.edit().putInt("daily_mission_${date}_$missionId", progress).apply()
    }

    fun isDailyMissionClaimed(missionId: String, date: String): Boolean {
        return prefs.getBoolean("daily_mission_claimed_${date}_$missionId", false)
    }

    fun setDailyMissionClaimed(missionId: String, date: String, claimed: Boolean) {
        prefs.edit().putBoolean("daily_mission_claimed_${date}_$missionId", claimed).apply()
    }

    // Milestones claimed
    fun isMilestoneClaimed(milestoneId: String): Boolean {
        return prefs.getBoolean("milestone_claimed_$milestoneId", false)
    }

    fun setMilestoneClaimed(milestoneId: String, claimed: Boolean) {
        prefs.edit().putBoolean("milestone_claimed_$milestoneId", claimed).apply()
    }

    companion object {
        private const val KEY_MUSIC = "key_music"
        private const val KEY_MUSIC_VOLUME = "key_music_volume"
        private const val KEY_SFX = "key_sfx"
        private const val KEY_SFX_VOLUME = "key_sfx_volume"
        private const val KEY_HAPTICS = "key_haptics"
        private const val KEY_GRAPHICS = "key_graphics"
        private const val KEY_TUTORIAL = "key_tutorial"
        private const val KEY_HINTS = "key_hints"
        private const val KEY_UNDOS = "key_undos"
        private const val KEY_HIGHEST_COMBO = "key_highest_combo"
        private const val KEY_TOTAL_SCORE = "key_total_score"
        private const val KEY_LAST_LOGIN_DATE = "key_last_login_date"
        private const val KEY_LOGIN_STREAK = "key_login_streak"
        private const val KEY_LAST_REWARD_CLAIM_DATE = "key_last_reward_claim_date"
        private const val KEY_STREAK_CYCLE_DAY = "key_streak_cycle_day"
        private const val KEY_TOTAL_LINES_EVER = "key_total_lines_ever"
        private const val KEY_TOTAL_LEVELS_EVER = "key_total_levels_ever"
    }
}
