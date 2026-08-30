package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DailyMission
import com.example.data.model.DailyRewardItem
import com.example.data.model.MilestoneAchievement
import com.example.game.GameViewModel
import com.example.ui.components.GamingButton
import com.example.ui.components.GamingIconButton
import com.example.ui.theme.*

enum class HubTab(val title: String, val icon: String) {
    DAILY_REWARDS("Daily Gift", "🎁"),
    MISSIONS("Missions", "🎯"),
    MILESTONES("Milestones", "🏆")
}

@Composable
fun DailyHubDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(HubTab.DAILY_REWARDS) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .shadow(24.dp, RoundedCornerShape(28.dp), ambientColor = ShadowColorSoft)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .border(2.dp, Color(0x334A90E2), RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔥 ${uiState.loginStreakDays} Day Streak",
                        color = GoldenSun,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = "PLAYER REWARDS",
                        color = TextDeepNavy,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    GamingIconButton(
                        icon = Icons.Rounded.Close,
                        onClick = onDismiss,
                        size = 36.dp,
                        testTag = "close_hub_btn"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Tab Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HubTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .then(
                                    if (isSelected) {
                                        Modifier.background(
                                            Brush.linearGradient(listOf(BrightBlue, ElectricCyan))
                                        )
                                    } else {
                                        Modifier.background(Color.Transparent)
                                    }
                                )
                                .clickable {
                                    viewModel.soundManager.playButtonClick()
                                    selectedTab = tab
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${tab.icon} ${tab.title}",
                                color = if (isSelected) Color.White else TextSecondaryNavy,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Content
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        HubTab.DAILY_REWARDS -> DailyRewardsTabContent(
                            rewards = uiState.dailyRewards,
                            streakDays = uiState.loginStreakDays,
                            isClaimable = uiState.isDailyRewardClaimable,
                            onClaim = { viewModel.claimDailyReward() }
                        )
                        HubTab.MISSIONS -> DailyMissionsTabContent(
                            missions = uiState.dailyMissions,
                            onClaim = { viewModel.claimDailyMission(it) }
                        )
                        HubTab.MILESTONES -> MilestonesTabContent(
                            milestones = uiState.milestones,
                            onClaim = { viewModel.claimMilestone(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailyRewardsTabContent(
    rewards: List<DailyRewardItem>,
    streakDays: Int,
    isClaimable: Boolean,
    onClaim: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_claim")
    val claimScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "claim_scale"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "7-Day Login Streak Calendar",
                color = BrightBlue,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Check in daily to build your streak and earn free Boosters!",
                color = TextSecondaryNavy,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 7 Days Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Days 1 to 4
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rewards.take(4).forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            DailyRewardCard(item = item)
                        }
                    }
                }

                // Days 5 to 7 (Day 7 is Grand Chest)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rewards.drop(4).take(2).forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            DailyRewardCard(item = item)
                        }
                    }
                    rewards.lastOrNull()?.let { grandItem ->
                        Box(modifier = Modifier.weight(2f)) {
                            DailyRewardCard(item = grandItem, isGrandChest = true)
                        }
                    }
                }
            }
        }

        // Claim CTA Button
        if (isClaimable) {
            GamingButton(
                text = "CLAIM TODAY'S REWARD",
                icon = Icons.Rounded.CardGiftcard,
                onClick = onClaim,
                gradientColors = listOf(GoldenSun, Color(0xFFFF8F00)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .scale(claimScale),
                testTag = "claim_daily_reward_btn"
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE8F8F0))
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = VibrantGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Today's reward claimed! Next reward unlocks tomorrow.",
                        color = VibrantGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DailyRewardCard(
    item: DailyRewardItem,
    isGrandChest: Boolean = false
) {
    val borderColor = when {
        item.isAvailableToday -> GoldenSun
        item.isClaimed -> VibrantGreen
        else -> Color(0xFFE2E8F0)
    }

    val bgGradient = when {
        item.isAvailableToday -> listOf(Color(0xFFFFF8E1), Color(0xFFFFECB3))
        item.isClaimed -> listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))
        else -> listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(bgGradient))
            .border(if (item.isAvailableToday) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Day ${item.dayNumber}",
                color = if (item.isAvailableToday) GoldenSun else TextSecondaryNavy,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = if (isGrandChest) "👑 Grand" else item.description,
                color = TextDeepNavy,
                fontSize = if (isGrandChest) 13.sp else 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            if (item.isClaimed) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Claimed",
                        tint = VibrantGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text("DONE", color = VibrantGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            } else if (item.isAvailableToday) {
                Text(
                    text = "READY",
                    color = GoldenSun,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            } else {
                Text(
                    text = "LOCKED",
                    color = TextMuted,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
fun DailyMissionsTabContent(
    missions: List<DailyMission>,
    onClaim: (missionId: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(missions) { mission ->
            MissionCard(mission = mission, onClaim = { onClaim(mission.id) })
        }
    }
}

@Composable
fun MissionCard(
    mission: DailyMission,
    onClaim: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, if (mission.isCompleted && !mission.isClaimed) BrightBlue else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mission.title,
                    color = TextDeepNavy,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE2E8F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(mission.progressFraction)
                            .clip(RoundedCornerShape(3.dp))
                            .background(BrightBlue)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${mission.currentProgress} / ${mission.targetProgress} • Reward: " +
                            listOfNotNull(
                                if (mission.hintsReward > 0) "+${mission.hintsReward} 💡" else null,
                                if (mission.undosReward > 0) "+${mission.undosReward} ↩️" else null
                            ).joinToString(", "),
                    color = TextSecondaryNavy,
                    fontSize = 11.sp
                )
            }

            if (mission.isClaimed) {
                Text("CLAIMED", color = VibrantGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            } else if (mission.isCompleted) {
                Button(
                    onClick = onClaim,
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantGreen),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("CLAIM", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            } else {
                Text("IN PROGRESS", color = TextMuted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun MilestonesTabContent(
    milestones: List<MilestoneAchievement>,
    onClaim: (milestoneId: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(milestones) { milestone ->
            MilestoneCard(milestone = milestone, onClaim = { onClaim(milestone.id) })
        }
    }
}

@Composable
fun MilestoneCard(
    milestone: MilestoneAchievement,
    onClaim: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, if (milestone.isCompleted && !milestone.isClaimed) GoldenSun else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = milestone.title,
                    color = TextDeepNavy,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = milestone.description,
                    color = TextSecondaryNavy,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE2E8F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(milestone.progressFraction)
                            .clip(RoundedCornerShape(3.dp))
                            .background(GoldenSun)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${milestone.currentProgress} / ${milestone.targetProgress} • Reward: " +
                            listOfNotNull(
                                if (milestone.hintsReward > 0) "+${milestone.hintsReward} 💡" else null,
                                if (milestone.undosReward > 0) "+${milestone.undosReward} ↩️" else null,
                                if (milestone.starsReward > 0) "+${milestone.starsReward} ⭐" else null
                            ).joinToString(", "),
                    color = GoldenSun,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (milestone.isClaimed) {
                Text("CLAIMED", color = VibrantGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            } else if (milestone.isCompleted) {
                Button(
                    onClick = onClaim,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldenSun),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("CLAIM", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            } else {
                Text("LOCKED", color = TextMuted, fontSize = 11.sp)
            }
        }
    }
}
