package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorldId
import com.example.data.model.WorldTheme
import com.example.data.repository.LevelRepository
import com.example.game.GameViewModel
import com.example.ads.BannerAdView
import com.example.ui.components.GamingIconButton
import com.example.ui.components.WorldBackground
import com.example.ui.theme.*

@Composable
fun LevelSelectScreen(
    worldId: WorldId,
    viewModel: GameViewModel,
    onLevelSelected: (levelNumber: Int) -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressList by viewModel.allProgress.collectAsState()
    val worldTheme = remember(worldId) { WorldTheme.getTheme(worldId) }
    val levels = remember(worldId) { LevelRepository.getLevelsForWorld(worldId) }

    Box(modifier = modifier.fillMaxSize()) {
        WorldBackground(
            worldId = worldId,
            worldTheme = worldTheme
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GamingIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    onClick = onBackClicked,
                    testTag = "level_select_back_btn"
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${worldId.emoji} ${worldId.title}",
                        color = TextDeepNavy,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Levels ${worldId.startLevel} – ${worldId.endLevel}",
                        color = worldTheme.accentColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.size(46.dp))
            }

            // Grid of Level Cards
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp)
            ) {
                items(levels) { levelData ->
                    val progress = progressList.firstOrNull { it.levelNumber == levelData.levelNumber }
                    val isUnlocked = progress?.isUnlocked == true || levelData.levelNumber == 1
                    val stars = progress?.stars ?: 0
                    val isCompleted = progress?.isCompleted == true

                    LevelCardItem(
                        levelNumber = levelData.levelNumber,
                        levelTitle = levelData.title,
                        isUnlocked = isUnlocked,
                        isCompleted = isCompleted,
                        stars = stars,
                        worldTheme = worldTheme,
                        onClick = {
                            if (isUnlocked) {
                                viewModel.soundManager.playButtonClick()
                                viewModel.hapticManager.tap()
                                onLevelSelected(levelData.levelNumber)
                            } else {
                                viewModel.soundManager.playInvalidPlacement()
                            }
                        }
                    )
                }
            }

            // AdMob Banner Ad safely positioned at the bottom of the Level Select menu
            BannerAdView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
fun LevelCardItem(
    levelNumber: Int,
    levelTitle: String,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    stars: Int,
    worldTheme: WorldTheme,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_active")
    val activePulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "active_pulse"
    )

    val isCurrentUncompleted = isUnlocked && !isCompleted

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (isCurrentUncompleted) activePulse else 1f)
            .shadow(
                elevation = if (isUnlocked) 8.dp else 2.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = ShadowColorSoft,
                spotColor = if (isUnlocked) worldTheme.ambientGlow.copy(alpha = 0.3f) else Color.Transparent
            )
            .clip(RoundedCornerShape(22.dp))
            .background(
                if (isUnlocked) Color.White else Color(0xFFECEFF1)
            )
            .clickable(onClick = onClick)
            .testTag("level_node_$levelNumber")
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Level Number Circle
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked) {
                            Brush.linearGradient(
                                colors = listOf(worldTheme.accentColor, ElectricCyan)
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFB0BEC5), Color(0xFF90A4AE))
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    Text(
                        text = "$levelNumber",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "Locked",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Text(
                text = levelTitle,
                color = if (isUnlocked) TextDeepNavy else TextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            // Star Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..3) {
                    Icon(
                        imageVector = if (i <= stars) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                        contentDescription = null,
                        tint = if (i <= stars) GoldenSun else Color(0xFFCFD8DC),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
