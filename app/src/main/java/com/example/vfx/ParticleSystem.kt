package com.example.vfx

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.*
import kotlin.random.Random

enum class ParticleType {
    BLOCK_SHARD,
    SPARKLE,
    CONFETTI,
    BUBBLE,
    SNOW,
    COSMIC_STAR,
    SHOCKWAVE
}

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var color: Color,
    var alpha: Float,
    var rotation: Float,
    var rotSpeed: Float,
    var lifespan: Float,
    var age: Float = 0f,
    val type: ParticleType = ParticleType.BLOCK_SHARD,
    var scale: Float = 1f
) {
    val isDead: Boolean get() = age >= lifespan

    fun update(dt: Float, gravity: Float = 600f) {
        age += dt
        val progress = age / lifespan

        when (type) {
            ParticleType.BLOCK_SHARD -> {
                x += vx * dt
                y += vy * dt
                vy += gravity * dt
                vx *= 0.98f
                rotation += rotSpeed * dt
                alpha = (1f - progress).coerceIn(0f, 1f)
            }
            ParticleType.SPARKLE -> {
                x += vx * dt
                y += vy * dt
                vx *= 0.94f
                vy *= 0.94f
                rotation += rotSpeed * dt
                scale = sin(progress * PI.toFloat()).coerceIn(0f, 1.5f)
                alpha = (1f - progress).coerceIn(0f, 1f)
            }
            ParticleType.CONFETTI -> {
                x += vx * dt + sin((age * 6f) + rotation) * 20f * dt
                y += vy * dt
                vy = (vy + 200f * dt).coerceAtMost(300f)
                rotation += rotSpeed * dt
                alpha = if (progress > 0.8f) (1f - (progress - 0.8f) / 0.2f) else 1f
            }
            ParticleType.BUBBLE -> {
                x += sin(age * 3f + rotation) * 15f * dt
                y -= (vy.absoluteValue + 40f) * dt
                alpha = (sin(progress * PI.toFloat()) * 0.8f).coerceIn(0f, 0.8f)
            }
            ParticleType.SNOW -> {
                x += sin(age * 2f) * 10f * dt
                y += vy.absoluteValue * dt
                alpha = (0.7f - progress * 0.4f).coerceIn(0f, 1f)
            }
            ParticleType.COSMIC_STAR -> {
                x += vx * dt
                y += vy * dt
                scale = 0.5f + 0.5f * sin(age * 5f)
                alpha = (sin(progress * PI.toFloat())).coerceIn(0f, 1f)
            }
            ParticleType.SHOCKWAVE -> {
                size += vx * dt
                alpha = (1f - progress).coerceIn(0f, 1f)
            }
        }
    }
}

data class FloatingScore(
    val id: Long,
    val text: String,
    var x: Float,
    var y: Float,
    val color: Color,
    var alpha: Float = 1f,
    var scale: Float = 0.5f,
    var age: Float = 0f,
    val lifespan: Float = 0.9f
) {
    val isDead: Boolean get() = age >= lifespan

    fun update(dt: Float) {
        age += dt
        val progress = (age / lifespan).coerceIn(0f, 1f)
        y -= 90f * dt
        scale = if (progress < 0.2f) {
            0.5f + (progress / 0.2f) * 0.8f // Pop up to 1.3
        } else {
            1.3f - ((progress - 0.2f) / 0.8f) * 0.3f // Settle to 1.0
        }
        alpha = (1f - progress * progress).coerceIn(0f, 1f)
    }
}

data class ComboBanner(
    val title: String,
    val subtitle: String,
    val color: Color,
    var alpha: Float = 1f,
    var scale: Float = 0.2f,
    var rotation: Float = -5f,
    var age: Float = 0f,
    val lifespan: Float = 1.3f
) {
    val isDead: Boolean get() = age >= lifespan

    fun update(dt: Float) {
        age += dt
        val progress = (age / lifespan).coerceIn(0f, 1f)
        if (progress < 0.25f) {
            val t = progress / 0.25f
            scale = 0.2f + t * 1.0f
            rotation = -5f + t * 5f
            alpha = 1f
        } else if (progress > 0.75f) {
            val t = (progress - 0.75f) / 0.25f
            scale = 1.2f + t * 0.3f
            alpha = 1f - t
        } else {
            scale = 1.2f
            rotation = 0f
            alpha = 1f
        }
    }
}

class ParticleManager {
    val particles = mutableListOf<Particle>()
    val floatingScores = mutableListOf<FloatingScore>()
    var activeComboBanner: ComboBanner? = null

    private var nextScoreId = 1L

    fun spawnBlockPlacementBurst(center: Offset, color: Color) {
        repeat(14) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 250f + 60f
            particles.add(
                Particle(
                    x = center.x,
                    y = center.y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    size = Random.nextFloat() * 8f + 5f,
                    color = color,
                    alpha = 1f,
                    rotation = Random.nextFloat() * 360f,
                    rotSpeed = (Random.nextFloat() - 0.5f) * 400f,
                    lifespan = Random.nextFloat() * 0.35f + 0.25f,
                    type = ParticleType.BLOCK_SHARD
                )
            )
        }
        // Sparkle
        repeat(6) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 120f + 30f
            particles.add(
                Particle(
                    x = center.x,
                    y = center.y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    size = Random.nextFloat() * 12f + 8f,
                    color = Color.White,
                    alpha = 1f,
                    rotation = Random.nextFloat() * 360f,
                    rotSpeed = Random.nextFloat() * 200f,
                    lifespan = 0.4f,
                    type = ParticleType.SPARKLE
                )
            )
        }
    }

    fun spawnLineClearExplosion(cells: List<Offset>, colors: List<Color>) {
        cells.forEachIndexed { i, offset ->
            val cellColor = colors.getOrNull(i % colors.size) ?: Color.Cyan
            repeat(10) {
                val angle = Random.nextFloat() * 2f * PI.toFloat()
                val speed = Random.nextFloat() * 350f + 100f
                particles.add(
                    Particle(
                        x = offset.x,
                        y = offset.y,
                        vx = cos(angle) * speed,
                        vy = sin(angle) * speed - 120f,
                        size = Random.nextFloat() * 14f + 6f,
                        color = cellColor,
                        alpha = 1f,
                        rotation = Random.nextFloat() * 360f,
                        rotSpeed = (Random.nextFloat() - 0.5f) * 600f,
                        lifespan = Random.nextFloat() * 0.5f + 0.4f,
                        type = ParticleType.BLOCK_SHARD
                    )
                )
            }
            // Sparkles
            repeat(4) {
                particles.add(
                    Particle(
                        x = offset.x + (Random.nextFloat() - 0.5f) * 20f,
                        y = offset.y + (Random.nextFloat() - 0.5f) * 20f,
                        vx = (Random.nextFloat() - 0.5f) * 100f,
                        vy = (Random.nextFloat() - 0.5f) * 100f,
                        size = Random.nextFloat() * 16f + 10f,
                        color = Color.White,
                        alpha = 1f,
                        rotation = 0f,
                        rotSpeed = 180f,
                        lifespan = 0.5f,
                        type = ParticleType.SPARKLE
                    )
                )
            }
        }
    }

    fun spawnConfettiCelebration(width: Float, height: Float) {
        val palette = listOf(
            Color(0xFFFF1744), Color(0xFFFFD600), Color(0xFF00E676),
            Color(0xFF00E5FF), Color(0xFFFF4081), Color(0xFF7C4DFF)
        )
        repeat(70) {
            val x = Random.nextFloat() * width
            val y = Random.nextFloat() * (height * 0.4f)
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = (Random.nextFloat() - 0.5f) * 200f,
                    vy = Random.nextFloat() * 100f + 80f,
                    size = Random.nextFloat() * 12f + 8f,
                    color = palette.random(),
                    alpha = 1f,
                    rotation = Random.nextFloat() * 360f,
                    rotSpeed = (Random.nextFloat() - 0.5f) * 500f,
                    lifespan = Random.nextFloat() * 1.5f + 1.2f,
                    type = ParticleType.CONFETTI
                )
            )
        }
    }

    fun addFloatingScore(text: String, position: Offset, color: Color = Color(0xFFFFD600)) {
        floatingScores.add(
            FloatingScore(
                id = nextScoreId++,
                text = text,
                x = position.x,
                y = position.y,
                color = color
            )
        )
    }

    fun triggerComboBanner(comboCount: Int) {
        val (title, subtitle, color) = when {
            comboCount >= 5 -> Triple("UNSTOPPABLE!", "x$comboCount MEGA MULTIPLIER!", Color(0xFFFF1744))
            comboCount == 4 -> Triple("AMAZING!", "x4 COMBO BLAST!", Color(0xFFFF4081))
            comboCount == 3 -> Triple("GREAT!", "x3 COMBO CHAIN!", Color(0xFFFFD600))
            comboCount == 2 -> Triple("GOOD!", "x2 COMBO!", Color(0xFF00E5FF))
            else -> Triple("NICE CLEAR!", "+100 PTS", Color(0xFF00E676))
        }
        activeComboBanner = ComboBanner(title, subtitle, color)
    }

    fun update(dt: Float) {
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.update(dt)
            if (p.isDead) pIter.remove()
        }

        val sIter = floatingScores.iterator()
        while (sIter.hasNext()) {
            val s = sIter.next()
            s.update(dt)
            if (s.isDead) sIter.remove()
        }

        activeComboBanner?.let { banner ->
            banner.update(dt)
            if (banner.isDead) activeComboBanner = null
        }
    }

    fun clear() {
        particles.clear()
        floatingScores.clear()
        activeComboBanner = null
    }
}
