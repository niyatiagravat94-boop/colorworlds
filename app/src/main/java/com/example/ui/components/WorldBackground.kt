package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.data.model.WorldId
import com.example.data.model.WorldTheme
import kotlin.math.*

@Composable
fun WorldBackground(
    worldId: WorldId,
    worldTheme: WorldTheme,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "world_anim")

    val animTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "anim_time"
    )

    val fastAnimTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fast_anim_time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Base Premium Light Sky Gradient (Warm & Inviting)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    worldTheme.skyGradientTop,
                    worldTheme.skyGradientBottom,
                    Color(0xFFFFFFFF)
                )
            ),
            size = size
        )

        when (worldId) {
            WorldId.COLOR_GARDEN -> drawGardenWorldLight(w, h, animTime, fastAnimTime)
            WorldId.OCEAN_WORLD -> drawOceanWorldLight(w, h, animTime, fastAnimTime)
            WorldId.MOUNTAIN_WORLD -> drawMountainWorldLight(w, h, animTime, fastAnimTime)
            WorldId.SPACE_WORLD -> drawSpaceWorldLight(w, h, animTime, fastAnimTime)
            WorldId.CRYSTAL_WORLD -> drawCrystalWorldLight(w, h, animTime, fastAnimTime)
        }
    }
}

private fun DrawScope.drawGardenWorldLight(w: Float, h: Float, t: Float, fastT: Float) {
    // 1. Warm Sunny Radial Glow in Top Right Corner
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x66FFE082), Color(0x22FFD54F), Color.Transparent),
            center = Offset(w * 0.85f, h * 0.12f),
            radius = w * 0.55f
        ),
        center = Offset(w * 0.85f, h * 0.12f),
        radius = w * 0.55f
    )

    // 2. Soft Floating White Fluffy Clouds
    val cloudOffset1 = (sin(t) * 45f) + (w * 0.2f)
    drawRoundRect(
        color = Color(0xAAFFFFFF),
        topLeft = Offset(cloudOffset1, h * 0.14f),
        size = Size(140f, 48f),
        cornerRadius = CornerRadius(24f, 24f)
    )
    drawCircle(
        color = Color(0xAAFFFFFF),
        center = Offset(cloudOffset1 + 50f, h * 0.13f),
        radius = 28f
    )
    drawCircle(
        color = Color(0xAAFFFFFF),
        center = Offset(cloudOffset1 + 90f, h * 0.14f),
        radius = 24f
    )

    val cloudOffset2 = (cos(t * 0.8f) * 55f) + (w * 0.62f)
    drawRoundRect(
        color = Color(0x99FFFFFF),
        topLeft = Offset(cloudOffset2, h * 0.26f),
        size = Size(160f, 50f),
        cornerRadius = CornerRadius(25f, 25f)
    )

    // 3. Gentle Rolling Pastel Green Hills in Bottom Background
    val hillPathBack = Path().apply {
        moveTo(0f, h * 0.78f)
        cubicTo(w * 0.25f, h * 0.72f, w * 0.65f, h * 0.84f, w, h * 0.76f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(hillPathBack, Color(0x2281C784))

    val hillPathFront = Path().apply {
        moveTo(0f, h * 0.86f)
        cubicTo(w * 0.4f, h * 0.80f, w * 0.75f, h * 0.90f, w, h * 0.84f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(hillPathFront, Color(0x33A5D6A7))

    // 4. Floating Flower Petals & Golden Sparkles
    for (i in 0 until 14) {
        val px = (w * ((i * 0.12f + sin(t + i)) % 1f))
        val py = h * (0.35f + 0.55f * ((cos(t * 0.5f + i * 2) + 1f) / 2f))
        val pRadius = 3.5f + (sin(fastT + i) + 1f) * 2f
        drawCircle(
            color = if (i % 2 == 0) Color(0xAAFFD54F) else Color(0x99FF80AB),
            center = Offset(px, py),
            radius = pRadius
        )
    }
}

private fun DrawScope.drawOceanWorldLight(w: Float, h: Float, t: Float, fastT: Float) {
    // 1. Bright Shimmering Aqua Sun Rays from Top Surface
    val rayPath = Path().apply {
        moveTo(w * 0.25f, 0f)
        lineTo(w * 0.48f + sin(t) * 30f, h * 0.85f)
        lineTo(w * 0.38f + sin(t) * 30f, h * 0.85f)
        lineTo(w * 0.15f, 0f)
        close()
    }
    drawPath(
        rayPath,
        Brush.verticalGradient(
            colors = listOf(Color(0x3300B0FF), Color(0x0000B0FF)),
            startY = 0f,
            endY = h * 0.85f
        )
    )

    // 2. Rising Translucent Bubbles with Highlights
    for (i in 0 until 16) {
        val bubbleY = ((h - (fastT * 110f + i * 65f)) % h + h) % h
        val bubbleX = w * (0.1f + (i * 0.08f) + sin(t + i) * 0.05f)
        val bubbleR = 5f + (i % 4) * 3f

        drawCircle(
            color = Color(0x4480D8FF),
            center = Offset(bubbleX, bubbleY),
            radius = bubbleR
        )
        drawCircle(
            color = Color(0xCCFFFFFF),
            center = Offset(bubbleX - bubbleR * 0.3f, bubbleY - bubbleR * 0.3f),
            radius = bubbleR * 0.35f
        )
    }

    // 3. Cute Tropical Fish Silhouettes
    val fishX = (w + (fastT * 75f) % (w + 120f)) - 60f
    val fishY = h * 0.28f + sin(fastT * 2f) * 18f
    val fishPath = Path().apply {
        moveTo(fishX, fishY)
        quadraticTo(fishX - 22f, fishY - 9f, fishX - 38f, fishY)
        lineTo(fishX - 48f, fishY - 11f)
        lineTo(fishX - 44f, fishY)
        lineTo(fishX - 48f, fishY + 11f)
        lineTo(fishX - 38f, fishY)
        quadraticTo(fishX - 22f, fishY + 9f, fishX, fishY)
        close()
    }
    drawPath(fishPath, Color(0x4400B0FF))
}

private fun DrawScope.drawMountainWorldLight(w: Float, h: Float, t: Float, fastT: Float) {
    // 1. Clean Layered Alpine Mountain Silhouettes with Snow Caps
    val backMountain = Path().apply {
        moveTo(0f, h * 0.72f)
        lineTo(w * 0.35f, h * 0.44f)
        lineTo(w * 0.7f, h * 0.66f)
        lineTo(w, h * 0.52f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(backMountain, Color(0x1F1E88E5))

    // Snow caps
    val snowCap = Path().apply {
        moveTo(w * 0.35f, h * 0.44f)
        lineTo(w * 0.42f, h * 0.50f)
        lineTo(w * 0.38f, h * 0.52f)
        lineTo(w * 0.35f, h * 0.49f)
        lineTo(w * 0.30f, h * 0.53f)
        lineTo(w * 0.28f, h * 0.49f)
        close()
    }
    drawPath(snowCap, Color(0xAAFFFFFF))

    // 2. Soft Falling Snow & Crystal Sparkles
    for (i in 0 until 18) {
        val sx = (w * (i * 0.055f + sin(t + i) * 0.04f))
        val sy = (fastT * 90f + i * 65f) % h
        drawCircle(
            color = Color(0x9990CAF9),
            center = Offset(sx, sy),
            radius = 3f + (i % 3) * 1.5f
        )
    }
}

private fun DrawScope.drawSpaceWorldLight(w: Float, h: Float, t: Float, fastT: Float) {
    // 1. Pastel Cosmic Nebula Cloud (Soft Lavender & Pink)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x33E040FB), Color(0x227C4DFF), Color.Transparent),
            center = Offset(w * 0.75f, h * 0.25f),
            radius = w * 0.55f
        ),
        center = Offset(w * 0.75f, h * 0.25f),
        radius = w * 0.55f
    )

    // 2. Cute Pastel Saturn-like Ringed Planet
    val planetCenter = Offset(w * 0.22f, h * 0.18f)
    val planetR = 26f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFF80AB), Color(0xFFE040FB)),
            center = planetCenter,
            radius = planetR
        ),
        center = planetCenter,
        radius = planetR
    )
    // Planet rings
    val ringPath = Path().apply {
        addOval(
            androidx.compose.ui.geometry.Rect(
                left = planetCenter.x - 48f,
                top = planetCenter.y - 14f,
                right = planetCenter.x + 48f,
                bottom = planetCenter.y + 14f
            )
        )
    }
    drawPath(
        path = ringPath,
        color = Color(0x99B388FF),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
    )

    // 3. Twinkling Pastel Golden & Cyan Stars
    for (i in 0 until 24) {
        val sx = (w * ((i * 0.075f + 0.05f) % 1f))
        val sy = (h * ((i * 0.115f + 0.08f) % 1f))
        val twinkle = (sin(fastT * 3f + i) + 1f) / 2f
        val starSize = 2.5f + twinkle * 3f
        drawCircle(
            color = if (i % 2 == 0) Color(0xAAFFD54F) else Color(0xAA00E5FF),
            center = Offset(sx, sy),
            radius = starSize
        )
    }
}

private fun DrawScope.drawCrystalWorldLight(w: Float, h: Float, t: Float, fastT: Float) {
    // 1. Shimmering Aurora Borealis Curtains
    val auroraPath = Path().apply {
        moveTo(0f, h * 0.16f + sin(t) * 28f)
        cubicTo(
            w * 0.35f, h * 0.06f + cos(t) * 35f,
            w * 0.65f, h * 0.24f + sin(t * 1.2f) * 30f,
            w, h * 0.14f + cos(t) * 25f
        )
        lineTo(w, 0f)
        lineTo(0f, 0f)
        close()
    }
    drawPath(
        auroraPath,
        Brush.verticalGradient(
            colors = listOf(Color(0x33FF4081), Color(0x33EA80FC), Color.Transparent),
            startY = 0f,
            endY = h * 0.4f
        )
    )

    // 2. Prismatic Magical Sparkles
    for (i in 0 until 18) {
        val cx = w * ((i * 0.11f + sin(t + i)) % 1f)
        val cy = h * (0.2f + 0.6f * ((cos(fastT + i * 2) + 1f) / 2f))
        val pulse = (sin(fastT * 4f + i) + 1f) / 2f
        drawCircle(
            color = if (i % 3 == 0) Color(0xCCFF4081) else Color(0xCC80D8FF),
            center = Offset(cx, cy),
            radius = 3.5f + pulse * 4f
        )
    }
}
