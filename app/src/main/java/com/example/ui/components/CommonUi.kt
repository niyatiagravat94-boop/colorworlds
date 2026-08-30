package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorldTheme
import com.example.ui.theme.*

/**
 * Premium 3D Beveled Game Action Button
 * Features light highlights, deep bottom bevel, and spring bounce press physics.
 */
@Composable
fun GameButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    gradientColors: List<Color> = listOf(Color(0xFF00C853), Color(0xFF00E676)),
    bevelDarkColor: Color = Color(0xFF009624),
    testTag: String = "game_button",
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 600f),
        label = "btn_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 2.dp else 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = gradientColors.first().copy(alpha = 0.4f),
                spotColor = bevelDarkColor.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(bevelDarkColor) // 3D Bevel Base underneath
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple(color = Color.White.copy(alpha = 0.3f)),
                        onClick = onClick
                    )
                } else Modifier
            )
            .testTag(testTag)
            .padding(bottom = if (isPressed) 1.dp else 4.dp) // 3D Press Down Effect
    ) {
        // Raised Front Surface
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (enabled) gradientColors else listOf(Color(0xFFCFD8DC), Color(0xFFB0BEC5))
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color(0x99FFFFFF), Color(0x22FFFFFF))
                    ),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
                )
                .padding(horizontal = 20.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * Backward compatibility alias for GameButton
 */
@Composable
fun GamingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    gradientColors: List<Color> = listOf(Color(0xFF00B0FF), Color(0xFF0288D1)),
    borderColor: Color = Color.Transparent,
    testTag: String = "gaming_button"
) {
    val bevelDark = gradientColors.last().copy(alpha = 0.9f)
    GameButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        icon = icon,
        gradientColors = gradientColors,
        bevelDarkColor = bevelDark,
        testTag = testTag
    )
}

/**
 * Light 3D Circular Action / Navigation Button
 */
@Composable
fun GamingIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
    backgroundColor: Color = Color.White,
    iconTint: Color = TextDeepNavy,
    size: Dp = 46.dp,
    testTag: String = "icon_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 600f),
        label = "icon_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(size)
            .shadow(6.dp, CircleShape, ambientColor = ShadowColorSoft, spotColor = ShadowColorDeep)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.5.dp, Color(0x334A90E2), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color(0x3300B0FF)),
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(size * 0.55f)
        )

        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(CandyPink)
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

/**
 * Clean & Rewarding Star Rating Indicator with Pop Animations
 */
@Composable
fun StarRatingRow(
    stars: Int,
    maxStars: Int = 3,
    starSize: Dp = 34.dp,
    modifier: Modifier = Modifier,
    isAnimated: Boolean = false
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxStars) {
            val isEarned = i <= stars

            val scale by animateFloatAsState(
                targetValue = if (isEarned) 1.15f else 0.9f,
                animationSpec = spring(dampingRatio = 0.4f, stiffness = 300f),
                label = "star_scale_$i"
            )

            Icon(
                imageVector = if (isEarned) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                contentDescription = "Star $i",
                tint = if (isEarned) GoldenSun else Color(0xFFCFD8DC),
                modifier = Modifier
                    .size(starSize)
                    .scale(if (isAnimated) scale else 1f)
            )
        }
    }
}

/**
 * Light 3D Objective Header Card
 */
@Composable
fun ObjectiveBar(
    title: String,
    current: Int,
    target: Int,
    worldTheme: WorldTheme,
    modifier: Modifier = Modifier
) {
    val progress = if (target > 0) (current.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 1f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "obj_progress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = ShadowColorSoft)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.5.dp, Color(0x220288D1), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TARGET OBJECTIVE",
                    color = worldTheme.accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$current / $target",
                    color = TextDeepNavy,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = title,
                color = TextDeepNavy,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            // Progress bar track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFECEFF1))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    worldTheme.accentColor,
                                    GoldenSun
                                )
                            )
                        )
                )
            }
        }
    }
}
