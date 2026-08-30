package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class WorldId(
    val id: String,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val startLevel: Int,
    val endLevel: Int
) {
    COLOR_GARDEN(
        id = "world_garden",
        title = "Color Garden",
        subtitle = "A sunny blooming paradise of vibrant colors",
        emoji = "🌸",
        startLevel = 1,
        endLevel = 10
    ),
    OCEAN_WORLD(
        id = "world_ocean",
        title = "Ocean World",
        subtitle = "Luminous aqua lagoons & tropical reef wonders",
        emoji = "🌊",
        startLevel = 11,
        endLevel = 20
    ),
    MOUNTAIN_WORLD(
        id = "world_mountain",
        title = "Mountain World",
        subtitle = "Majestic sunny summits & alpine snow crystals",
        emoji = "🏔️",
        startLevel = 21,
        endLevel = 30
    ),
    SPACE_WORLD(
        id = "world_space",
        title = "Space World",
        subtitle = "Pastel cosmic nebulae, cyan moons & orbiting stars",
        emoji = "🚀",
        startLevel = 31,
        endLevel = 40
    ),
    CRYSTAL_WORLD(
        id = "world_crystal",
        title = "Crystal World",
        subtitle = "Prismatic spires, aurora chimes & magical gems",
        emoji = "💎",
        startLevel = 41,
        endLevel = 50
    );

    companion object {
        fun forLevel(levelNumber: Int): WorldId {
            return entries.firstOrNull { levelNumber in it.startLevel..it.endLevel }
                ?: COLOR_GARDEN
        }
    }
}

data class WorldTheme(
    val worldId: WorldId,
    val skyGradientTop: Color,
    val skyGradientBottom: Color,
    val ambientGlow: Color,
    val boardSurfaceColor: Color,
    val boardBorderGlow: Color,
    val gridEmptyCellColor: Color,
    val gridEmptyCellBorder: Color,
    val hudCardBg: Color,
    val accentColor: Color,
    val primaryTextColor: Color,
    val secondaryTextColor: Color,
    val particlePalette: List<Color>,
    val musicMood: String
) {
    companion object {
        fun getTheme(worldId: WorldId): WorldTheme {
            return when (worldId) {
                WorldId.COLOR_GARDEN -> WorldTheme(
                    worldId = worldId,
                    skyGradientTop = Color(0xFFE0F7FA), // Soft Light Sky / Mint
                    skyGradientBottom = Color(0xFFE8F5E9), // Gentle Pastel Green
                    ambientGlow = Color(0xFF00C853),
                    boardSurfaceColor = Color(0xFFFFFFFF),
                    boardBorderGlow = Color(0xFF81C784),
                    gridEmptyCellColor = Color(0xFFF1F8E9),
                    gridEmptyCellBorder = Color(0xFFC8E6C9),
                    hudCardBg = Color(0xFFFFFFFF),
                    accentColor = Color(0xFF2E7D32),
                    primaryTextColor = Color(0xFF1B382B),
                    secondaryTextColor = Color(0xFF388E3C),
                    particlePalette = listOf(
                        Color(0xFF00E676),
                        Color(0xFFFFD600),
                        Color(0xFFFF4081),
                        Color(0xFF00B0FF),
                        Color(0xFFFF6D00)
                    ),
                    musicMood = "Playful & Sunny Meadow"
                )
                WorldId.OCEAN_WORLD -> WorldTheme(
                    worldId = worldId,
                    skyGradientTop = Color(0xFFE1F5FE), // Bright Light Sky Aqua
                    skyGradientBottom = Color(0xFFE0F2F1), // Gentle Turquoise
                    ambientGlow = Color(0xFF00B0FF),
                    boardSurfaceColor = Color(0xFFFFFFFF),
                    boardBorderGlow = Color(0xFF80D8FF),
                    gridEmptyCellColor = Color(0xFFE0F7FA),
                    gridEmptyCellBorder = Color(0xFFB2EBF2),
                    hudCardBg = Color(0xFFFFFFFF),
                    accentColor = Color(0xFF0288D1),
                    primaryTextColor = Color(0xFF01579B),
                    secondaryTextColor = Color(0xFF0288D1),
                    particlePalette = listOf(
                        Color(0xFF00E5FF),
                        Color(0xFF80D8FF),
                        Color(0xFF00B0FF),
                        Color(0xFF69F0AE),
                        Color(0xFFFF80AB)
                    ),
                    musicMood = "Bright Tropical Lagoon"
                )
                WorldId.MOUNTAIN_WORLD -> WorldTheme(
                    worldId = worldId,
                    skyGradientTop = Color(0xFFE3F2FD), // Crisp Soft Blue
                    skyGradientBottom = Color(0xFFEDE7F6), // Lavender Mist
                    ambientGlow = Color(0xFF448AFF),
                    boardSurfaceColor = Color(0xFFFFFFFF),
                    boardBorderGlow = Color(0xFF90CAF9),
                    gridEmptyCellColor = Color(0xFFF0F4F8),
                    gridEmptyCellBorder = Color(0xFFCFD8DC),
                    hudCardBg = Color(0xFFFFFFFF),
                    accentColor = Color(0xFF1976D2),
                    primaryTextColor = Color(0xFF0D47A1),
                    secondaryTextColor = Color(0xFF1976D2),
                    particlePalette = listOf(
                        Color(0xFFE3F2FD),
                        Color(0xFF90CAF9),
                        Color(0xFF80D8FF),
                        Color(0xFFFFD54F),
                        Color(0xFF80DEEA)
                    ),
                    musicMood = "Crisp Alpine Sun"
                )
                WorldId.SPACE_WORLD -> WorldTheme(
                    worldId = worldId,
                    skyGradientTop = Color(0xFFEDE7F6), // Pastel Cosmic Lavender
                    skyGradientBottom = Color(0xFFE1BEE7), // Soft Nebula Purple
                    ambientGlow = Color(0xFF7C4DFF),
                    boardSurfaceColor = Color(0xFFFFFFFF),
                    boardBorderGlow = Color(0xFFB388FF),
                    gridEmptyCellColor = Color(0xFFF3E5F5),
                    gridEmptyCellBorder = Color(0xFFE1BEE7),
                    hudCardBg = Color(0xFFFFFFFF),
                    accentColor = Color(0xFF6A1B9A),
                    primaryTextColor = Color(0xFF4A148C),
                    secondaryTextColor = Color(0xFF7B1FA2),
                    particlePalette = listOf(
                        Color(0xFFE040FB),
                        Color(0xFF7C4DFF),
                        Color(0xFF00E5FF),
                        Color(0xFFFF4081),
                        Color(0xFFFFEA00)
                    ),
                    musicMood = "Playful Cosmic Synthesizer"
                )
                WorldId.CRYSTAL_WORLD -> WorldTheme(
                    worldId = worldId,
                    skyGradientTop = Color(0xFFFCE4EC), // Soft Aurora Pink
                    skyGradientBottom = Color(0xFFF3E5F5), // Light Crystal Lavender
                    ambientGlow = Color(0xFFFF4081),
                    boardSurfaceColor = Color(0xFFFFFFFF),
                    boardBorderGlow = Color(0xFFFF80AB),
                    gridEmptyCellColor = Color(0xFFFCE4EC),
                    gridEmptyCellBorder = Color(0xFFF8BBD0),
                    hudCardBg = Color(0xFFFFFFFF),
                    accentColor = Color(0xFFC2185B),
                    primaryTextColor = Color(0xFF880E4F),
                    secondaryTextColor = Color(0xFFC2185B),
                    particlePalette = listOf(
                        Color(0xFFFF4081),
                        Color(0xFFFF80AB),
                        Color(0xFFFFD600),
                        Color(0xFFB388FF),
                        Color(0xFF69F0AE)
                    ),
                    musicMood = "Prismatic Aurora Chimes"
                )
            }
        }
    }
}
