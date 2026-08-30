package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.GraphicsQuality
import com.example.game.GameViewModel
import com.example.ui.components.GamingButton
import com.example.ui.theme.*

@Composable
fun SettingsDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val prefs = viewModel.preferences
    var musicOn by remember { mutableStateOf(prefs.isMusicEnabled) }
    var musicVolume by remember { mutableFloatStateOf(prefs.musicVolume) }
    var sfxOn by remember { mutableStateOf(prefs.isSfxEnabled) }
    var sfxVolume by remember { mutableFloatStateOf(prefs.sfxVolume) }
    var hapticsOn by remember { mutableStateOf(prefs.isHapticsEnabled) }
    var graphicsQuality by remember { mutableStateOf(prefs.graphicsQuality) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .shadow(24.dp, RoundedCornerShape(28.dp), ambientColor = ShadowColorSoft)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "SETTINGS & AUDIO",
                    color = TextDeepNavy,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                // 1. Music & Ambient Panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFF0F7FF))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (musicOn) Icons.Rounded.MusicNote else Icons.Rounded.MusicOff,
                                contentDescription = null,
                                tint = BrightBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Background Music",
                                color = TextDeepNavy,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Switch(
                            checked = musicOn,
                            onCheckedChange = {
                                viewModel.toggleMusic()
                                musicOn = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = VibrantGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFCFD8DC)
                            )
                        )
                    }

                    if (musicOn) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Vol: ${(musicVolume * 100).toInt()}%",
                                color = TextSecondaryNavy,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(60.dp)
                            )
                            Slider(
                                value = musicVolume,
                                onValueChange = {
                                    musicVolume = it
                                    viewModel.setMusicVolume(it)
                                },
                                valueRange = 0f..1f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = BrightBlue,
                                    activeTrackColor = BrightBlue,
                                    inactiveTrackColor = Color(0xFFD0E1FD)
                                )
                            )
                        }
                    }
                }

                // 2. SFX Panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFFFF9E6))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (sfxOn) Icons.AutoMirrored.Rounded.VolumeUp else Icons.AutoMirrored.Rounded.VolumeOff,
                                contentDescription = null,
                                tint = GoldenSun,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sound Effects (SFX)",
                                color = TextDeepNavy,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Switch(
                            checked = sfxOn,
                            onCheckedChange = {
                                viewModel.toggleSfx()
                                sfxOn = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = VibrantGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFCFD8DC)
                            )
                        )
                    }

                    if (sfxOn) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Vol: ${(sfxVolume * 100).toInt()}%",
                                color = TextSecondaryNavy,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(60.dp)
                            )
                            Slider(
                                value = sfxVolume,
                                onValueChange = {
                                    sfxVolume = it
                                    viewModel.setSfxVolume(it)
                                },
                                valueRange = 0f..1f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = GoldenSun,
                                    activeTrackColor = GoldenSun,
                                    inactiveTrackColor = Color(0xFFFFECC0)
                                )
                            )
                        }
                    }
                }

                // 3. Haptics Panel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFFFF0F5))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (hapticsOn) Icons.Rounded.Vibration else Icons.Rounded.MobileOff,
                            contentDescription = null,
                            tint = CandyPink,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Haptic Vibration",
                            color = TextDeepNavy,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Switch(
                        checked = hapticsOn,
                        onCheckedChange = {
                            viewModel.toggleHaptics()
                            hapticsOn = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = VibrantGreen,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFCFD8DC)
                        )
                    )
                }

                // 4. Graphics Quality Selector
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "GRAPHICS & 60 FPS RENDERING",
                        color = TextSecondaryNavy,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GraphicsQuality.entries.forEach { quality ->
                            val isSelected = graphicsQuality == quality
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) BrightBlue else Color(0xFFECEFF1)
                                    )
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = quality.name,
                                    color = if (isSelected) Color.White else TextSecondaryNavy,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                GamingButton(
                    text = "SAVE & CLOSE",
                    icon = Icons.Rounded.Check,
                    onClick = onDismiss,
                    gradientColors = listOf(Color(0xFF00C853), Color(0xFF00E676)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    testTag = "settings_close_btn"
                )
            }
        }
    }
}
