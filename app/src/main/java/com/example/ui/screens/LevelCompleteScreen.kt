package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.LevelProgressEntity
import com.example.data.model.LevelData
import com.example.data.model.WorldTheme
import com.example.data.repository.LevelProgressRepository
import com.example.ui.components.GamingButton
import com.example.ui.theme.*

/**
 * LevelCompleteScreen calculates star ratings dynamically based on level-specific score thresholds,
 * displays animated star reveals with visual threshold indicators, and persists the results
 * to Room database via LevelProgressRepository ensuring the highest achieved result is retained.
 */
@Composable
fun LevelCompleteScreen(
    levelData: LevelData,
    score: Int,
    movesUsed: Int,
    worldTheme: WorldTheme,
    levelProgressRepository: LevelProgressRepository? = null,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onBackToLevelSelect: () -> Unit = {}
) {
    // Dynamically calculate earned stars based on level score thresholds
    val calculatedStars = remember(levelData, score) {
        levelProgressRepository?.calculateStarsForScore(levelData, score) ?: when {
            score >= levelData.threeStarScore -> 3
            score >= levelData.twoStarScore -> 2
            else -> 1
        }
    }

    var bestScoreState by remember { mutableIntStateOf(score) }
    var bestStarsState by remember { mutableIntStateOf(calculatedStars) }
    var isNewRecord by remember { mutableStateOf(false) }

    // Persist via LevelProgressRepository asynchronously and fetch saved best
    LaunchedEffect(levelData.levelNumber, score, calculatedStars) {
        if (levelProgressRepository != null) {
            val result = levelProgressRepository.saveLevelCompletion(
                levelData = levelData,
                score = score,
                starsEarned = calculatedStars
            )
            bestScoreState = result.persistedBestScore
            bestStarsState = result.persistedBestStars
            isNewRecord = result.isNewBestScore
        } else {
            bestScoreState = score
            bestStarsState = calculatedStars
        }
    }

    var starCountVisible by remember { mutableIntStateOf(0) }

    LaunchedEffect(calculatedStars) {
        starCountVisible = 0
        for (i in 1..calculatedStars) {
            kotlinx.coroutines.delay(260)
            starCountVisible = i
        }
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .shadow(28.dp, RoundedCornerShape(32.dp), ambientColor = GoldenSun)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Tag
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = worldTheme.accentColor.copy(alpha = 0.12f),
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Text(
                        text = "${levelData.worldId.title.uppercase()} • LEVEL ${levelData.levelNumber}",
                        color = worldTheme.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = "LEVEL COMPLETE!",
                    color = TextDeepNavy,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )

                // 3 Star Visual Display with dynamic thresholds
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    for (i in 1..3) {
                        val isVisible = i <= starCountVisible
                        val scale by animateFloatAsState(
                            targetValue = if (isVisible) 1.22f else 0.82f,
                            animationSpec = spring(dampingRatio = 0.45f, stiffness = 420f),
                            label = "star_pop_$i"
                        )

                        val thresholdScore = when (i) {
                            1 -> levelData.oneStarScore
                            2 -> levelData.twoStarScore
                            else -> levelData.threeStarScore
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (isVisible) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                                contentDescription = "Star $i",
                                tint = if (isVisible) GoldenSun else Color(0xFFCFD8DC),
                                modifier = Modifier
                                    .size(46.dp)
                                    .scale(scale)
                            )
                            Text(
                                text = "$thresholdScore+",
                                color = if (isVisible) GoldenSun else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Stats & Threshold Breakdown Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SCORE", color = TextSecondaryNavy, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (isNewRecord || score >= bestScoreState) {
                                Icon(
                                    imageVector = Icons.Rounded.EmojiEvents,
                                    contentDescription = "New Record",
                                    tint = GoldenSun,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text("$score pts", color = GoldenSun, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("BEST RECORD", color = TextSecondaryNavy, fontSize = 12.sp, fontWeight = FontWeight.Normal)
                        Text("${maxOf(bestScoreState, score)} pts", color = TextDeepNavy, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("MOVES PLAYED", color = TextSecondaryNavy, fontSize = 12.sp, fontWeight = FontWeight.Normal)
                        Text("$movesUsed", color = TextDeepNavy, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Action Buttons
                GamingButton(
                    text = "NEXT LEVEL",
                    icon = Icons.AutoMirrored.Rounded.ArrowForward,
                    onClick = onNextLevel,
                    gradientColors = listOf(Color(0xFF00C853), Color(0xFF00E676)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    testTag = "screen_next_level_btn"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GamingButton(
                        text = "REPLAY",
                        icon = Icons.Rounded.Replay,
                        onClick = onReplay,
                        gradientColors = listOf(Color(0xFF78909C), Color(0xFF607D8B)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        testTag = "screen_replay_btn"
                    )

                    GamingButton(
                        text = "LEVELS",
                        icon = Icons.AutoMirrored.Rounded.List,
                        onClick = onBackToLevelSelect,
                        gradientColors = listOf(Color(0xFF455A64), Color(0xFF37474F)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        testTag = "screen_levels_btn"
                    )
                }
            }
        }
    }
}
