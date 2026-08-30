package com.example.ui.navigation

import com.example.data.model.WorldId

sealed class Screen {
    data object Home : Screen()
    data object WorldMap : Screen()
    data class LevelSelect(val worldId: WorldId) : Screen()
    data class GamePlay(val levelNumber: Int) : Screen()
}
