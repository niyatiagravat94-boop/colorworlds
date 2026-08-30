package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BlockColor
import com.example.data.model.BlockShape
import com.example.data.model.ShapeType
import com.example.data.model.WorldId
import com.example.data.model.WorldTheme
import com.example.game.GameViewModel
import com.example.ui.components.GameLogo3D
import com.example.ui.components.GamingButton
import com.example.ui.components.GamingIconButton
import com.example.ui.components.ShapePreview3D
import com.example.ui.components.WorldBackground
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    onPlayClicked: (levelNumber: Int) -> Unit,
    onWorldMapClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val totalStars by viewModel.totalStars.collectAsState()
    val highestUnlockedLevel by viewModel.highestUnlockedLevel.collectAsState()
    val preferences = viewModel.preferences
    var showDailyHubDialog by remember { mutableStateOf(false) }

    var logoEntered by remember { mutableStateOf(false) }
    val logoScale by animateFloatAsState(
        targetValue = if (logoEntered) 1.0f else 0.85f,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "logo_scale_entrance"
    )

    LaunchedEffect(Unit) {
        logoEntered = true
        viewModel.soundManager.startAmbientMusic(WorldId.COLOR_GARDEN)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "home_bob")
    val playPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "play_pulse"
    )

    val gardenTheme = remember { WorldTheme.getTheme(WorldId.COLOR_GARDEN) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Dynamic Animated World Background (Bright & Sunny)
        WorldBackground(
            worldId = WorldId.COLOR_GARDEN,
            worldTheme = gardenTheme
        )

        // Top Status Bar (Stars, Daily Rewards & Settings)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stars Chip
            Row(
                modifier = Modifier
                    .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = ShadowColorSoft)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.5.dp, Color(0x334A90E2), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = GoldenSun,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "$totalStars ⭐",
                    color = TextDeepNavy,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Right Action Buttons (Daily Rewards Hub & Settings)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Daily Rewards & Streak Chip with Badge
                Box {
                    GamingIconButton(
                        icon = Icons.Rounded.CardGiftcard,
                        onClick = { showDailyHubDialog = true },
                        testTag = "home_daily_rewards_btn"
                    )
                    if (uiState.hasUnclaimedRewards) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                                .clip(CircleShape)
                                .background(CandyPink)
                                .border(2.dp, Color.White, CircleShape)
                        )
                    }
                }

                // Settings Button
                GamingIconButton(
                    icon = Icons.Rounded.Settings,
                    onClick = onSettingsClicked,
                    testTag = "home_settings_btn"
                )
            }
        }

        // Center Hero Branding & 3D Blocks Display
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // New Animated 3D Game Logo
            GameLogo3D(
                modifier = Modifier.scale(logoScale)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Main Play CTA Button
            GamingButton(
                text = if (highestUnlockedLevel > 1) "CONTINUE (LVL $highestUnlockedLevel)" else "PLAY ADVENTURE",
                icon = Icons.Rounded.PlayArrow,
                onClick = { onPlayClicked(highestUnlockedLevel) },
                gradientColors = listOf(Color(0xFF00C853), Color(0xFF00E676)),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(64.dp)
                    .scale(playPulse),
                testTag = "main_play_button"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // World Map Button
            GamingButton(
                text = "EXPLORE WORLDS",
                icon = Icons.Rounded.Public,
                onClick = onWorldMapClicked,
                gradientColors = listOf(Color(0xFF0288D1), Color(0xFF00B0FF)),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(56.dp),
                testTag = "world_map_button"
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Player Stats & Streak Pill
            Row(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = ShadowColorSoft)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0x334A90E2), RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔥 ${uiState.loginStreakDays}d Streak",
                    color = GoldenSun,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "5 Worlds • 50 Levels",
                    color = TextSecondaryNavy,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                if (preferences.highestComboEver > 0) {
                    Text(
                        text = "x${preferences.highestComboEver} Combo",
                        color = BrightBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Daily Rewards & Missions Modal Dialog
        if (showDailyHubDialog) {
            DailyHubDialog(
                viewModel = viewModel,
                onDismiss = { showDailyHubDialog = false }
            )
        }
    }
}
