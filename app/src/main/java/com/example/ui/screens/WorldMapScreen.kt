package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorldId
import com.example.data.model.WorldTheme
import com.example.game.GameViewModel
import com.example.ads.BannerAdView
import com.example.ui.components.GamingIconButton
import com.example.ui.components.WorldBackground
import com.example.ui.theme.*

@Composable
fun WorldMapScreen(
    viewModel: GameViewModel,
    onWorldSelected: (worldId: WorldId) -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressList by viewModel.allProgress.collectAsState()
    val totalStars by viewModel.totalStars.collectAsState()

    val currentWorldTheme = remember { WorldTheme.getTheme(WorldId.SPACE_WORLD) }

    Box(modifier = modifier.fillMaxSize()) {
        WorldBackground(
            worldId = WorldId.SPACE_WORLD,
            worldTheme = currentWorldTheme
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
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
                    testTag = "world_map_back_btn"
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "WORLD ADVENTURE",
                        color = TextDeepNavy,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "5 Unique 3D Realms",
                        color = BrightBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Stars Badge
                Row(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = ShadowColorSoft)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = GoldenSun,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "$totalStars",
                        color = TextDeepNavy,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // World Cards List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp)
            ) {
                items(WorldId.entries) { worldId ->
                    val isUnlocked = viewModel.repository.isWorldUnlocked(worldId, progressList)
                    val worldTheme = remember(worldId) { WorldTheme.getTheme(worldId) }

                    val worldLevels = progressList.filter { it.levelNumber in worldId.startLevel..worldId.endLevel }
                    val completedCount = worldLevels.count { it.isCompleted }
                    val worldStars = worldLevels.sumOf { it.stars }
                    val maxStars = (worldId.endLevel - worldId.startLevel + 1) * 3

                    WorldCard(
                        worldId = worldId,
                        worldTheme = worldTheme,
                        isUnlocked = isUnlocked,
                        completedCount = completedCount,
                        totalLevels = (worldId.endLevel - worldId.startLevel + 1),
                        starsEarned = worldStars,
                        maxStars = maxStars,
                        onClick = {
                            if (isUnlocked) {
                                viewModel.soundManager.playButtonClick()
                                viewModel.hapticManager.tap()
                                onWorldSelected(worldId)
                            } else {
                                viewModel.soundManager.playInvalidPlacement()
                            }
                        }
                    )
                }
            }

            // AdMob Banner Ad
            BannerAdView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
fun WorldCard(
    worldId: WorldId,
    worldTheme: WorldTheme,
    isUnlocked: Boolean,
    completedCount: Int,
    totalLevels: Int,
    starsEarned: Int,
    maxStars: Int,
    onClick: () -> Unit
) {
    val progress = (completedCount.toFloat() / totalLevels.toFloat()).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isUnlocked) 8.dp else 2.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = ShadowColorSoft,
                spotColor = if (isUnlocked) worldTheme.ambientGlow.copy(alpha = 0.3f) else Color.Transparent
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (isUnlocked) Color.White else Color(0xFFECEFF1)
            )
            .clickable(onClick = onClick)
            .testTag("world_card_${worldId.name.lowercase()}")
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // World Icon / Emoji
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (isUnlocked) worldTheme.skyGradientTop
                        else Color(0xFFCFD8DC)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    Text(
                        text = worldId.emoji,
                        fontSize = 32.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "Locked",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = worldId.title,
                    color = if (isUnlocked) TextDeepNavy else TextMuted,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (isUnlocked) worldId.subtitle else "Complete previous world to unlock",
                    color = if (isUnlocked) worldTheme.accentColor else TextMuted,
                    fontSize = 12.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE2E8F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .clip(RoundedCornerShape(3.dp))
                            .background(worldTheme.accentColor)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$completedCount / $totalLevels Levels",
                        color = TextSecondaryNavy,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "$starsEarned / $maxStars ⭐",
                        color = GoldenSun,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = if (isUnlocked) Icons.Rounded.ChevronRight else Icons.Rounded.Lock,
                contentDescription = null,
                tint = if (isUnlocked) worldTheme.accentColor else TextMuted,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
