package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material3.Text
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
import com.example.data.model.WorldId
import com.example.data.model.WorldTheme
import com.example.ui.components.GamingButton
import com.example.ui.theme.*

@Composable
fun WorldUnlockDialog(
    unlockedWorld: WorldId,
    onEnterWorld: () -> Unit
) {
    val theme = remember(unlockedWorld) { WorldTheme.getTheme(unlockedWorld) }

    val infiniteTransition = rememberInfiniteTransition(label = "unlock_glow")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Dialog(
        onDismissRequest = onEnterWorld,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(28.dp, RoundedCornerShape(28.dp), ambientColor = theme.ambientGlow)
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
                    text = "CHAPTER COMPLETE!",
                    color = GoldenSun,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "NEW REALM UNLOCKED",
                    color = TextDeepNavy,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                // Large World Emoji / Badge
                Box(
                    modifier = Modifier
                        .scale(pulse)
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(theme.skyGradientTop, Color(0xFFE0F7FA))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = unlockedWorld.emoji,
                        fontSize = 46.sp
                    )
                }

                Text(
                    text = unlockedWorld.title,
                    color = theme.accentColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = unlockedWorld.subtitle,
                    color = TextSecondaryNavy,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                GamingButton(
                    text = "ENTER WORLD",
                    icon = Icons.Rounded.Explore,
                    onClick = onEnterWorld,
                    gradientColors = listOf(theme.accentColor, ElectricCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    testTag = "enter_world_dialog_btn"
                )
            }
        }
    }
}
