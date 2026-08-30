package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class BlockColor(
    val id: String,
    val displayName: String,
    val primaryColor: Color,
    val lightFacetColor: Color,
    val darkFacetColor: Color,
    val highlightColor: Color,
    val shadowColor: Color,
    val glowColor: Color
) {
    ELECTRIC_BLUE(
        id = "electric_blue",
        displayName = "Electric Blue",
        primaryColor = Color(0xFF00B0FF),
        lightFacetColor = Color(0xFF80D8FF),
        darkFacetColor = Color(0xFF0081CB),
        highlightColor = Color(0xFFE1F5FE),
        shadowColor = Color(0xFF004D80),
        glowColor = Color(0x6600B0FF)
    ),
    CYAN_AQUA(
        id = "cyan_aqua",
        displayName = "Cyan Aqua",
        primaryColor = Color(0xFF00E5FF),
        lightFacetColor = Color(0xFF84FFFF),
        darkFacetColor = Color(0xFF00B2CC),
        highlightColor = Color(0xFFE0F7FA),
        shadowColor = Color(0xFF006B7B),
        glowColor = Color(0x6600E5FF)
    ),
    EMERALD_GREEN(
        id = "emerald_green",
        displayName = "Emerald Green",
        primaryColor = Color(0xFF00E676),
        lightFacetColor = Color(0xFF69F0AE),
        darkFacetColor = Color(0xFF00B056),
        highlightColor = Color(0xFFE8F5E9),
        shadowColor = Color(0xFF006830),
        glowColor = Color(0x6600E676)
    ),
    LIME_GREEN(
        id = "lime_green",
        displayName = "Lime Green",
        primaryColor = Color(0xFF76FF03),
        lightFacetColor = Color(0xFFB2FF59),
        darkFacetColor = Color(0xFF55C700),
        highlightColor = Color(0xFFF1F8E9),
        shadowColor = Color(0xFF337700),
        glowColor = Color(0x6676FF03)
    ),
    BRIGHT_YELLOW(
        id = "bright_yellow",
        displayName = "Bright Yellow",
        primaryColor = Color(0xFFFFD600),
        lightFacetColor = Color(0xFFFFFF52),
        darkFacetColor = Color(0xFFC7A500),
        highlightColor = Color(0xFFFFFDE7),
        shadowColor = Color(0xFF7A6500),
        glowColor = Color(0x66FFD600)
    ),
    CORAL_ORANGE(
        id = "coral_orange",
        displayName = "Coral Orange",
        primaryColor = Color(0xFFFF6D00),
        lightFacetColor = Color(0xFFFF9E40),
        darkFacetColor = Color(0xFFC43E00),
        highlightColor = Color(0xFFFFF3E0),
        shadowColor = Color(0xFF7A2700),
        glowColor = Color(0x66FF6D00)
    ),
    VIBRANT_RED(
        id = "vibrant_red",
        displayName = "Ruby Red",
        primaryColor = Color(0xFFFF1744),
        lightFacetColor = Color(0xFFFF616F),
        darkFacetColor = Color(0xFFC4001D),
        highlightColor = Color(0xFFFFEBEE),
        shadowColor = Color(0xFF750010),
        glowColor = Color(0x66FF1744)
    ),
    MAGENTA_PINK(
        id = "magenta_pink",
        displayName = "Magenta Pink",
        primaryColor = Color(0xFFFF4081),
        lightFacetColor = Color(0xFFFF79B0),
        darkFacetColor = Color(0xFFC60055),
        highlightColor = Color(0xFFFCE4EC),
        shadowColor = Color(0xFF7A0033),
        glowColor = Color(0x66FF4081)
    ),
    ROYAL_PURPLE(
        id = "royal_purple",
        displayName = "Royal Purple",
        primaryColor = Color(0xFF7C4DFF),
        lightFacetColor = Color(0xFFB47CFF),
        darkFacetColor = Color(0xFF3F1DCB),
        highlightColor = Color(0xFFEDE7F6),
        shadowColor = Color(0xFF220A80),
        glowColor = Color(0x667C4DFF)
    ),
    GOLDEN_AMBER(
        id = "golden_amber",
        displayName = "Golden Amber",
        primaryColor = Color(0xFFFFAB00),
        lightFacetColor = Color(0xFFFFDD4B),
        darkFacetColor = Color(0xFFC67C00),
        highlightColor = Color(0xFFFFF8E1),
        shadowColor = Color(0xFF754900),
        glowColor = Color(0x66FFAB00)
    );

    companion object {
        fun fromIndex(index: Int): BlockColor {
            val values = entries
            return values[index.mod(values.size)]
        }
    }
}
