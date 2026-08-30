package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
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
import com.example.data.model.WorldTheme
import com.example.ui.components.GamingButton
import com.example.ui.theme.*

@Composable
fun LevelCompleteDialog(
    levelNumber: Int,
    stars: Int,
    score: Int,
    movesUsed: Int,
    worldTheme: WorldTheme,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit
) {
    var starCountVisible by remember { mutableIntStateOf(0) }

    LaunchedEffect(stars) {
        starCountVisible = 0
        for (i in 1..stars) {
            kotlinx.coroutines.delay(280)
            starCountVisible = i
        }
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(24.dp, RoundedCornerShape(28.dp), ambientColor = GoldenSun)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "LEVEL $levelNumber",
                    color = worldTheme.accentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "VICTORY!",
                    color = TextDeepNavy,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                // 3 Star Animation Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    for (i in 1..3) {
                        val isVisible = i <= starCountVisible
                        val scale by animateFloatAsState(
                            targetValue = if (isVisible) 1.25f else 0.8f,
                            animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
                            label = "complete_star_$i"
                        )

                        Icon(
                            imageVector = if (isVisible) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                            contentDescription = "Star $i",
                            tint = if (isVisible) GoldenSun else Color(0xFFCFD8DC),
                            modifier = Modifier
                                .size(48.dp)
                                .scale(scale)
                        )
                    }
                }

                // Stats Pill
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("FINAL SCORE", color = TextSecondaryNavy, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("$score pts", color = GoldenSun, fontSize = 17.sp, fontWeight = FontWeight.Black)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("MOVES USED", color = TextSecondaryNavy, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("$movesUsed", color = TextDeepNavy, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action Buttons
                GamingButton(
                    text = "NEXT LEVEL",
                    icon = Icons.AutoMirrored.Rounded.ArrowForward,
                    onClick = onNextLevel,
                    gradientColors = listOf(Color(0xFF00C853), Color(0xFF00E676)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    testTag = "dialog_next_level_btn"
                )

                GamingButton(
                    text = "REPLAY",
                    icon = Icons.Rounded.Replay,
                    onClick = onReplay,
                    gradientColors = listOf(Color(0xFF78909C), Color(0xFF607D8B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    testTag = "dialog_replay_btn"
                )
            }
        }
    }
}
