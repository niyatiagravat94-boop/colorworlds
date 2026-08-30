package com.example.data.repository

import com.example.data.local.GamePreferences
import com.example.data.model.DailyMission
import com.example.data.model.DailyRewardItem
import com.example.data.model.MilestoneAchievement
import com.example.data.model.MissionType
import java.text.SimpleDateFormat
import java.util.*

class RetentionManager(private val preferences: GamePreferences) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun getTodayDateString(): String = dateFormat.format(Date())

    private fun getYesterdayDateString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormat.format(cal.time)
    }

    fun checkAndRecordDailyLogin() {
        val today = getTodayDateString()
        val lastLogin = preferences.lastLoginDate
        val yesterday = getYesterdayDateString()

        if (lastLogin.isEmpty()) {
            // First time ever
            preferences.lastLoginDate = today
            preferences.loginStreakDays = 1
            preferences.currentStreakCycleDay = 1
        } else if (lastLogin == today) {
            // Same day, nothing to update
        } else if (lastLogin == yesterday) {
            // Consecutive login!
            preferences.lastLoginDate = today
            val newStreak = preferences.loginStreakDays + 1
            preferences.loginStreakDays = newStreak

            val nextCycleDay = preferences.currentStreakCycleDay + 1
            preferences.currentStreakCycleDay = if (nextCycleDay > 7) 1 else nextCycleDay
        } else {
            // Skipped a day or more -> Reset streak
            preferences.lastLoginDate = today
            preferences.loginStreakDays = 1
            preferences.currentStreakCycleDay = 1
        }
    }

    fun isDailyRewardClaimable(): Boolean {
        val today = getTodayDateString()
        return preferences.lastRewardClaimDate != today
    }

    fun getDailyRewards(): List<DailyRewardItem> {
        val today = getTodayDateString()
        val isAlreadyClaimedToday = preferences.lastRewardClaimDate == today
        val currentCycleDay = preferences.currentStreakCycleDay

        val rewardConfigs = listOf(
            DailyRewardItem(1, "Day 1 Gift", "2 Free Hints", hints = 2),
            DailyRewardItem(2, "Day 2 Gift", "2 Free Undos", undos = 2),
            DailyRewardItem(3, "Day 3 Gift", "5 Bonus Stars", bonusStars = 5),
            DailyRewardItem(4, "Day 4 Gift", "3 Free Hints", hints = 3),
            DailyRewardItem(5, "Day 5 Gift", "3 Free Undos", undos = 3),
            DailyRewardItem(6, "Day 6 Gift", "3 Hints + 3 Undos", hints = 3, undos = 3),
            DailyRewardItem(7, "Grand Chest", "5 Hints, 5 Undos, 15 Stars", hints = 5, undos = 5, bonusStars = 15)
        )

        return rewardConfigs.map { item ->
            val isClaimed = if (isAlreadyClaimedToday) {
                item.dayNumber <= currentCycleDay
            } else {
                item.dayNumber < currentCycleDay
            }
            val isAvailable = !isAlreadyClaimedToday && (item.dayNumber == currentCycleDay)
            item.copy(
                isClaimed = isClaimed,
                isAvailableToday = isAvailable
            )
        }
    }

    fun claimDailyReward(): DailyRewardItem? {
        if (!isDailyRewardClaimable()) return null
        val today = getTodayDateString()
        val currentCycleDay = preferences.currentStreakCycleDay
        val rewards = getDailyRewards()
        val reward = rewards.firstOrNull { it.dayNumber == currentCycleDay } ?: return null

        preferences.lastRewardClaimDate = today
        if (reward.hints > 0) preferences.hintsCount += reward.hints
        if (reward.undos > 0) preferences.undosCount += reward.undos

        return reward
    }

    // Daily Missions
    fun getDailyMissions(): List<DailyMission> {
        val today = getTodayDateString()

        val definitions = listOf(
            DailyMission(
                id = "mission_lines",
                title = "Clear 6 Lines",
                type = MissionType.CLEAR_LINES,
                currentProgress = preferences.getDailyMissionProgress("mission_lines", today),
                targetProgress = 6,
                hintsReward = 1,
                undosReward = 1,
                isClaimed = preferences.isDailyMissionClaimed("mission_lines", today)
            ),
            DailyMission(
                id = "mission_score",
                title = "Earn 1,500 Score",
                type = MissionType.SCORE_POINTS,
                currentProgress = preferences.getDailyMissionProgress("mission_score", today),
                targetProgress = 1500,
                hintsReward = 2,
                undosReward = 0,
                isClaimed = preferences.isDailyMissionClaimed("mission_score", today)
            ),
            DailyMission(
                id = "mission_combo",
                title = "Perform a x2 Combo",
                type = MissionType.TRIGGER_COMBO,
                currentProgress = preferences.getDailyMissionProgress("mission_combo", today),
                targetProgress = 2,
                hintsReward = 1,
                undosReward = 2,
                isClaimed = preferences.isDailyMissionClaimed("mission_combo", today)
            )
        )
        return definitions
    }

    fun onGameEventProgress(
        linesCleared: Int = 0,
        scoreEarned: Int = 0,
        comboAchieved: Int = 0,
        levelCompleted: Boolean = false
    ) {
        val today = getTodayDateString()
        if (linesCleared > 0) {
            val cur = preferences.getDailyMissionProgress("mission_lines", today)
            preferences.setDailyMissionProgress("mission_lines", today, cur + linesCleared)
            preferences.totalLinesClearedEver += linesCleared
        }
        if (scoreEarned > 0) {
            val cur = preferences.getDailyMissionProgress("mission_score", today)
            preferences.setDailyMissionProgress("mission_score", today, cur + scoreEarned)
        }
        if (comboAchieved >= 2) {
            val cur = preferences.getDailyMissionProgress("mission_combo", today)
            preferences.setDailyMissionProgress("mission_combo", today, maxOf(cur, comboAchieved))
        }
        if (levelCompleted) {
            preferences.totalLevelsCompletedEver += 1
        }
    }

    fun claimDailyMission(missionId: String): DailyMission? {
        val today = getTodayDateString()
        val missions = getDailyMissions()
        val mission = missions.firstOrNull { it.id == missionId } ?: return null
        if (!mission.isCompleted || mission.isClaimed) return null

        preferences.setDailyMissionClaimed(missionId, today, true)
        if (mission.hintsReward > 0) preferences.hintsCount += mission.hintsReward
        if (mission.undosReward > 0) preferences.undosCount += mission.undosReward

        return mission
    }

    // Milestones
    fun getMilestones(totalStars: Int): List<MilestoneAchievement> {
        val list = listOf(
            MilestoneAchievement(
                id = "milestone_stars_10",
                title = "Rising Star",
                description = "Earn 10 total stars",
                currentProgress = totalStars,
                targetProgress = 10,
                hintsReward = 2,
                undosReward = 2,
                isClaimed = preferences.isMilestoneClaimed("milestone_stars_10")
            ),
            MilestoneAchievement(
                id = "milestone_stars_30",
                title = "Star Collector",
                description = "Earn 30 total stars",
                currentProgress = totalStars,
                targetProgress = 30,
                hintsReward = 3,
                undosReward = 3,
                isClaimed = preferences.isMilestoneClaimed("milestone_stars_30")
            ),
            MilestoneAchievement(
                id = "milestone_lines_25",
                title = "Line Smasher",
                description = "Clear 25 total lines across games",
                currentProgress = preferences.totalLinesClearedEver,
                targetProgress = 25,
                hintsReward = 3,
                undosReward = 2,
                isClaimed = preferences.isMilestoneClaimed("milestone_lines_25")
            ),
            MilestoneAchievement(
                id = "milestone_combo_3",
                title = "Combo Master",
                description = "Reach a x3 Combo Streak",
                currentProgress = preferences.highestComboEver,
                targetProgress = 3,
                hintsReward = 4,
                undosReward = 4,
                isClaimed = preferences.isMilestoneClaimed("milestone_combo_3")
            )
        )
        return list
    }

    fun claimMilestone(milestoneId: String, totalStars: Int): MilestoneAchievement? {
        val milestones = getMilestones(totalStars)
        val milestone = milestones.firstOrNull { it.id == milestoneId } ?: return null
        if (!milestone.isCompleted || milestone.isClaimed) return null

        preferences.setMilestoneClaimed(milestoneId, true)
        if (milestone.hintsReward > 0) preferences.hintsCount += milestone.hintsReward
        if (milestone.undosReward > 0) preferences.undosCount += milestone.undosReward

        return milestone
    }

    fun hasAnyUnclaimedRewards(totalStars: Int): Boolean {
        if (isDailyRewardClaimable()) return true
        val missions = getDailyMissions()
        if (missions.any { it.isCompleted && !it.isClaimed }) return true
        val milestones = getMilestones(totalStars)
        if (milestones.any { it.isCompleted && !it.isClaimed }) return true
        return false
    }
}
