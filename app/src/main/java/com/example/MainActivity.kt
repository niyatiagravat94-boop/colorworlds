package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.data.model.WorldId
import com.example.game.GameViewModel
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
                var isSettingsOpen by remember { mutableStateOf(false) }

                BackHandler(enabled = currentScreen !is Screen.Home) {
                    when (val screen = currentScreen) {
                        is Screen.GamePlay -> {
                            currentScreen = Screen.LevelSelect(screen.levelNumber.let { WorldId.forLevel(it) })
                        }
                        is Screen.LevelSelect -> {
                            currentScreen = Screen.WorldMap
                        }
                        is Screen.WorldMap -> {
                            currentScreen = Screen.Home
                        }
                        Screen.Home -> {}
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF8FAFC))
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith
                                    fadeOut(animationSpec = tween(300))
                        },
                        label = "screen_transition"
                    ) { targetScreen ->
                        when (targetScreen) {
                            is Screen.Home -> {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onPlayClicked = { levelNumber ->
                                        viewModel.loadLevel(levelNumber)
                                        currentScreen = Screen.GamePlay(levelNumber)
                                    },
                                    onWorldMapClicked = {
                                        viewModel.soundManager.playButtonClick()
                                        currentScreen = Screen.WorldMap
                                    },
                                    onSettingsClicked = {
                                        viewModel.soundManager.playButtonClick()
                                        isSettingsOpen = true
                                    }
                                )
                            }
                            is Screen.WorldMap -> {
                                WorldMapScreen(
                                    viewModel = viewModel,
                                    onWorldSelected = { worldId ->
                                        currentScreen = Screen.LevelSelect(worldId)
                                    },
                                    onBackClicked = {
                                        viewModel.soundManager.playButtonClick()
                                        currentScreen = Screen.Home
                                    }
                                )
                            }
                            is Screen.LevelSelect -> {
                                LevelSelectScreen(
                                    worldId = targetScreen.worldId,
                                    viewModel = viewModel,
                                    onLevelSelected = { levelNumber ->
                                        viewModel.loadLevel(levelNumber)
                                        currentScreen = Screen.GamePlay(levelNumber)
                                    },
                                    onBackClicked = {
                                        viewModel.soundManager.playButtonClick()
                                        currentScreen = Screen.WorldMap
                                    }
                                )
                            }
                            is Screen.GamePlay -> {
                                GamePlayScreen(
                                    viewModel = viewModel,
                                    onBackToLevelSelect = {
                                        currentScreen = Screen.LevelSelect(
                                            WorldId.forLevel(targetScreen.levelNumber)
                                        )
                                    },
                                    onWorldCompleteContinue = {
                                        currentScreen = Screen.WorldMap
                                    }
                                )
                            }
                        }
                    }

                    if (isSettingsOpen) {
                        SettingsDialog(
                            viewModel = viewModel,
                            onDismiss = { isSettingsOpen = false }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.preferences.isMusicEnabled) {
            viewModel.soundManager.resumeMusic()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.soundManager.pauseMusic()
    }
}
