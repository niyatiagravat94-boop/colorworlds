package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.nativeCanvas
import com.example.vfx.ParticleManager
import com.example.vfx.ParticleType

@Composable
fun VfxOverlay(
    particleManager: ParticleManager,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        // 1. Draw all active particles
        val particles = particleManager.particles.toList()
        for (p in particles) {
            val color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f))
            when (p.type) {
                ParticleType.BLOCK_SHARD -> {
                    rotate(p.rotation, pivot = Offset(p.x, p.y)) {
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(p.x - p.size / 2f, p.y - p.size / 2f),
                            size = Size(p.size, p.size),
                            cornerRadius = CornerRadius(p.size * 0.25f, p.size * 0.25f)
                        )
                    }
                }
                ParticleType.SPARKLE -> {
                    rotate(p.rotation, pivot = Offset(p.x, p.y)) {
                        scale(p.scale, pivot = Offset(p.x, p.y)) {
                            val half = p.size / 2f
                            val starPath = Path().apply {
                                moveTo(p.x, p.y - half)
                                quadraticTo(p.x, p.y, p.x + half, p.y)
                                quadraticTo(p.x, p.y, p.x, p.y + half)
                                quadraticTo(p.x, p.y, p.x - half, p.y)
                                quadraticTo(p.x, p.y, p.x, p.y - half)
                                close()
                            }
                            drawPath(starPath, color)
                        }
                    }
                }
                ParticleType.CONFETTI -> {
                    rotate(p.rotation, pivot = Offset(p.x, p.y)) {
                        drawRect(
                            color = color,
                            topLeft = Offset(p.x - p.size / 2f, p.y - p.size / 3f),
                            size = Size(p.size, p.size * 0.6f)
                        )
                    }
                }
                ParticleType.BUBBLE -> {
                    drawCircle(
                        color = color,
                        center = Offset(p.x, p.y),
                        radius = p.size / 2f
                    )
                }
                ParticleType.SNOW, ParticleType.COSMIC_STAR -> {
                    drawCircle(
                        color = color,
                        center = Offset(p.x, p.y),
                        radius = p.size / 2f * p.scale
                    )
                }
                ParticleType.SHOCKWAVE -> {
                    drawCircle(
                        color = color,
                        center = Offset(p.x, p.y),
                        radius = p.size,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f)
                    )
                }
            }
        }

        // 2. Draw Floating Scores (High Contrast with Warm Slate Shadow)
        val scores = particleManager.floatingScores.toList()
        for (s in scores) {
            val paint = android.graphics.Paint().apply {
                this.color = android.graphics.Color.argb(
                    (s.alpha * 255).toInt().coerceIn(0, 255),
                    (s.color.red * 255).toInt(),
                    (s.color.green * 255).toInt(),
                    (s.color.blue * 255).toInt()
                )
                textSize = 54f * s.scale
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                textAlign = android.graphics.Paint.Align.CENTER
                setShadowLayer(10f, 0f, 4f, android.graphics.Color.argb(120, 30, 41, 59))
            }
            drawContext.canvas.nativeCanvas.drawText(
                s.text,
                s.x,
                s.y,
                paint
            )
        }

        // 3. Draw Combo Banner (Vivid Colors with Clean Dark Slate Shadow)
        particleManager.activeComboBanner?.let { banner ->
            val bannerAlpha = banner.alpha.coerceIn(0f, 1f)
            val bannerScale = banner.scale
            val cx = size.width / 2f
            val cy = size.height * 0.38f

            val bannerPaint = android.graphics.Paint().apply {
                this.color = android.graphics.Color.argb(
                    (bannerAlpha * 255).toInt().coerceIn(0, 255),
                    (banner.color.red * 255).toInt(),
                    (banner.color.green * 255).toInt(),
                    (banner.color.blue * 255).toInt()
                )
                textSize = 70f * bannerScale
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                textAlign = android.graphics.Paint.Align.CENTER
                setShadowLayer(16f * bannerScale, 0f, 6f, android.graphics.Color.argb(140, 30, 41, 59))
            }

            val subPaint = android.graphics.Paint().apply {
                this.color = android.graphics.Color.argb(
                    (bannerAlpha * 240).toInt().coerceIn(0, 255),
                    30, 41, 59 // Slate Navy text for high contrast on light backgrounds
                )
                textSize = 34f * bannerScale
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                textAlign = android.graphics.Paint.Align.CENTER
                setShadowLayer(8f * bannerScale, 0f, 4f, android.graphics.Color.argb(80, 255, 255, 255))
            }

            drawContext.canvas.nativeCanvas.drawText(
                banner.title,
                cx,
                cy,
                bannerPaint
            )
            drawContext.canvas.nativeCanvas.drawText(
                banner.subtitle,
                cx,
                cy + 42f * bannerScale,
                subPaint
            )
        }
    }
}
