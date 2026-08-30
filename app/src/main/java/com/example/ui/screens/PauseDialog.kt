package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.WorldTheme
import com.example.game.GameViewModel
import com.example.ui.components.GamingButton
import com.example.ui.components.GamingIconButton
import com.example.ui.theme.*

@Composable
fun PauseDialog(
    viewModel: GameViewModel,
    worldTheme: WorldTheme,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onQuit: () -> Unit
) {
    val prefs = viewModel.preferences

    Dialog(onDismissRequest = onResume) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(24.dp, RoundedCornerShape(28.dp), ambientColor = ShadowColorSoft)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "PAUSED",
                    color = TextDeepNavy,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                // Audio / Haptic Quick Toggles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GamingIconButton(
                        icon = if (prefs.isMusicEnabled) Icons.Rounded.MusicNote else Icons.Rounded.MusicOff,
                        onClick = { viewModel.toggleMusic() },
                        backgroundColor = if (prefs.isMusicEnabled) BrightBlue else Color(0xFFECEFF1),
                        iconTint = if (prefs.isMusicEnabled) Color.White else TextMuted,
                        size = 46.dp,
                        testTag = "pause_toggle_music"
                    )

                    GamingIconButton(
                        icon = if (prefs.isSfxEnabled) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
                        onClick = { viewModel.toggleSfx() },
                        backgroundColor = if (prefs.isSfxEnabled) GoldenSun else Color(0xFFECEFF1),
                        iconTint = if (prefs.isSfxEnabled) Color.White else TextMuted,
                        size = 46.dp,
                        testTag = "pause_toggle_sfx"
                    )

                    GamingIconButton(
                        icon = if (prefs.isHapticsEnabled) Icons.Rounded.Vibration else Icons.Rounded.MobileOff,
                        onClick = { viewModel.toggleHaptics() },
                        backgroundColor = if (prefs.isHapticsEnabled) CandyPink else Color(0xFFECEFF1),
                        iconTint = if (prefs.isHapticsEnabled) Color.White else TextMuted,
                        size = 46.dp,
                        testTag = "pause_toggle_haptics"
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                GamingButton(
                    text = "RESUME",
                    icon = Icons.Rounded.PlayArrow,
                    onClick = onResume,
                    gradientColors = listOf(Color(0xFF00C853), Color(0xFF00E676)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    testTag = "pause_resume_btn"
                )

                GamingButton(
                    text = "RESTART LEVEL",
                    icon = Icons.Rounded.Replay,
                    onClick = onRestart,
                    gradientColors = listOf(Color(0xFF0288D1), Color(0xFF00B0FF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    testTag = "pause_restart_btn"
                )

                GamingButton(
                    text = "QUIT TO MAP",
                    icon = Icons.Rounded.ExitToApp,
                    onClick = onQuit,
                    gradientColors = listOf(Color(0xFF78909C), Color(0xFF607D8B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    testTag = "pause_quit_btn"
                )
            }
        }
    }
}
