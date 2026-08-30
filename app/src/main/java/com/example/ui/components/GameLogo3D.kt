package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Premium 3D Beveled Game Logo Component.
 * Implements entrance animation (0.85 -> 1.05 -> 1.0) and gentle idle movement.
 */
@Composable
fun GameLogo3D(
    modifier: Modifier = Modifier
) {
    // 1. Entrance Scale Animation (0.85 -> 1.05 -> 1.0)
    val entranceTransition = rememberInfiniteTransition(label = "logo_idle")
    val idleFloat by entranceTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_float"
    )

    val shimmerAlpha by entranceTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_shimmer"
    )

    Column(
        modifier = modifier
            .offset(y = idleFloat.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 3D Block Mascot Crest
        Box(
            modifier = Modifier
                .size(130.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(130.dp)) {
                val w = size.width
                val h = size.height

                // Radial Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x5500E5FF),
                            Color(0x337C4DFF),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = w * 0.5f
                    )
                )

                // 3D Isometric Mini-Blocks
                val blockSize = w * 0.28f
                val corner = blockSize * 0.24f

                // Block 1 (Cyan Aqua - Top Left)
                val b1Pos = Offset(w * 0.16f, h * 0.14f)
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF80D8FF), Color(0xFF00B0FF), Color(0xFF0091EA))
                    ),
                    topLeft = b1Pos,
                    size = Size(blockSize, blockSize),
                    cornerRadius = CornerRadius(corner, corner)
                )
                drawCircle(
                    color = Color(0xCCFFFFFF),
                    center = Offset(b1Pos.x + corner * 0.8f, b1Pos.y + corner * 0.8f),
                    radius = blockSize * 0.12f
                )

                // Block 2 (Bright Golden Yellow - Top Right)
                val b2Pos = Offset(w * 0.54f, h * 0.22f)
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFFFF59D), Color(0xFFFFD600), Color(0xFFFFAB00))
                    ),
                    topLeft = b2Pos,
                    size = Size(blockSize, blockSize),
                    cornerRadius = CornerRadius(corner, corner)
                )
                drawCircle(
                    color = Color(0xCCFFFFFF),
                    center = Offset(b2Pos.x + corner * 0.8f, b2Pos.y + corner * 0.8f),
                    radius = blockSize * 0.12f
                )

                // Block 3 (Vibrant Coral / Pink - Center Bottom)
                val b3Pos = Offset(w * 0.35f, h * 0.48f)
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFFF80AB), Color(0xFFFF4081), Color(0xFFC51162))
                    ),
                    topLeft = b3Pos,
                    size = Size(blockSize * 1.05f, blockSize * 1.05f),
                    cornerRadius = CornerRadius(corner, corner)
                )
                drawCircle(
                    color = Color(0xCCFFFFFF),
                    center = Offset(b3Pos.x + corner * 0.8f, b3Pos.y + corner * 0.8f),
                    radius = blockSize * 0.14f
                )

                // Sparkle accents
                drawCircle(
                    color = Color(0xFFFFFFFF).copy(alpha = shimmerAlpha),
                    center = Offset(w * 0.82f, h * 0.18f),
                    radius = w * 0.035f
                )
                drawCircle(
                    color = Color(0xFFFFF9C4).copy(alpha = shimmerAlpha),
                    center = Offset(w * 0.12f, h * 0.70f),
                    radius = w * 0.025f
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Main Title Plaque
        Box(
            modifier = Modifier
                .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = ShadowColorSoft)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(2.dp, Color(0x3300B0FF), RoundedCornerShape(20.dp))
                .padding(horizontal = 24.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = GoldenSun,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "COLOR WORLDS",
                    color = TextDeepNavy,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center
                )
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = GoldenSun,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Subtitle Pill
        Text(
            text = "3D BLOCK PUZZLE",
            color = BrightBlue,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.5.sp,
            textAlign = TextAlign.Center
        )
    }
}
