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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
            // Header Bar with true centering
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                GamingIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    onClick = onBackClicked,
                    modifier = Modifier.align(Alignment.CenterStart),
                    testTag = "world_map_back_btn"
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "WORLD ADVENTURE",
                        color = TextDeepNavy,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "5 Unique 3D Realms",
                        color = BrightBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center
                    )
                }

                // Stars Badge
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = ShadowColorSoft)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0x334A90E2), RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = GoldenSun,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "$totalStars",
                        color = TextDeepNavy,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // World Cards List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
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
                elevation = if (isUnlocked) 6.dp else 2.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = ShadowColorSoft,
                spotColor = if (isUnlocked) worldTheme.ambientGlow.copy(alpha = 0.25f) else Color.Transparent
            )
            .clip(RoundedCornerShape(22.dp))
            .background(
                if (isUnlocked) Color.White else Color(0xFFECEFF1)
            )
            .clickable(onClick = onClick)
            .testTag("world_card_${worldId.name.lowercase()}")
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // World Icon / Emoji
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isUnlocked) worldTheme.skyGradientTop
                        else Color(0xFFCFD8DC)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    Text(
                        text = worldId.emoji,
                        fontSize = 28.sp
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

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = worldId.title,
                    color = if (isUnlocked) TextDeepNavy else TextMuted,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (isUnlocked) worldId.subtitle else "Complete previous world to unlock",
                    color = if (isUnlocked) worldTheme.accentColor else TextMuted,
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

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

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$completedCount / $totalLevels Levels",
                        color = TextSecondaryNavy,
                        fontSize = 11.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "$starsEarned / $maxStars ⭐",
                        color = GoldenSun,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = if (isUnlocked) Icons.Rounded.ChevronRight else Icons.Rounded.Lock,
                contentDescription = null,
                tint = if (isUnlocked) worldTheme.accentColor else TextMuted,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
