package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorldTheme
import com.example.game.GameViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun GamePlayScreen(
    viewModel: GameViewModel,
    onBackToLevelSelect: () -> Unit,
    onWorldCompleteContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val worldTheme = remember(uiState.currentWorldId) { WorldTheme.getTheme(uiState.currentWorldId) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val context = LocalContext.current
    val activity = context as? Activity

    var showRetryDialog by remember { mutableStateOf(false) }

    // Responsive cell size calculation for 8x8 grid
    val gridCols = if (uiState.grid.isNotEmpty()) uiState.grid[0].size else 8
    val boardAvailableWidth = screenWidth - 48.dp
    val dynamicCellSize = (boardAvailableWidth / gridCols).coerceIn(36.dp, 44.dp)

    val animatedScore by animateIntAsState(
        targetValue = uiState.score,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "score_ticker"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .offset(
                x = (kotlin.random.Random.nextFloat() - 0.5f).dp * uiState.screenShakeIntensity,
                y = (kotlin.random.Random.nextFloat() - 0.5f).dp * uiState.screenShakeIntensity
            )
    ) {
        // 1. Dynamic Animated World Environment
        WorldBackground(
            worldId = uiState.currentWorldId,
            worldTheme = worldTheme
        )

        // 2. Main Gameplay Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top HUD
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GamingIconButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        onClick = {
                            viewModel.soundManager.playButtonClick()
                            onBackToLevelSelect()
                        },
                        size = 44.dp,
                        testTag = "gameplay_back_btn"
                    )

                    // Pristine Level & World Header (No broken glyphs, clean hierarchy)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "LVL ${uiState.currentLevelNumber}",
                                color = BrightBlue,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(TextSecondaryNavy.copy(alpha = 0.6f))
                            )
                            Text(
                                text = uiState.levelData.title,
                                color = TextDeepNavy,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = uiState.currentWorldId.emoji,
                                fontSize = 12.sp
                            )
                            Text(
                                text = uiState.currentWorldId.title,
                                color = worldTheme.accentColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Retry Confirmation Button (Replaces debug ladybug)
                        GamingIconButton(
                            icon = Icons.Rounded.Refresh,
                            onClick = {
                                viewModel.soundManager.playButtonClick()
                                showRetryDialog = true
                            },
                            size = 44.dp,
                            testTag = "gameplay_retry_btn"
                        )

                        // Pause Button
                        GamingIconButton(
                            icon = Icons.Rounded.Pause,
                            onClick = { viewModel.togglePause() },
                            size = 44.dp,
                            testTag = "gameplay_pause_btn"
                        )
                    }
                }

                // Score and Moves Counter Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Score Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = ShadowColorSoft)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White)
                            .border(1.5.dp, Color(0x220288D1), RoundedCornerShape(18.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SCORE",
                                color = TextSecondaryNavy,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$animatedScore",
                                color = GoldenSun,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Moves / Batch Indicator Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = ShadowColorSoft)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White)
                            .border(1.5.dp, Color(0x220288D1), RoundedCornerShape(18.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (uiState.maxMoves != null) "MOVES" else "BATCH",
                                color = TextSecondaryNavy,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (uiState.maxMoves != null) {
                                    val left = (uiState.maxMoves!! - uiState.movesUsed).coerceAtLeast(0)
                                    "$left"
                                } else {
                                    "#${uiState.batchId}"
                                },
                                color = BrightBlue,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Objective Progress Bar
                ObjectiveBar(
                    title = uiState.objectiveTitle,
                    current = uiState.objectiveProgress.first,
                    target = uiState.objectiveProgress.second,
                    worldTheme = worldTheme
                )
            }

            // Tutorial Hint (if present for early levels)
            uiState.levelData.tutorialHint?.let { hintText ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .shadow(4.dp, RoundedCornerShape(14.dp), ambientColor = ShadowColorSoft)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE0F7FA))
                        .border(1.dp, Color(0xFF80DEEA), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💡 $hintText",
                        color = Color(0xFF006064),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Central 3D Puzzle Board
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                PuzzleBoard3D(
                    grid = uiState.grid,
                    worldTheme = worldTheme,
                    cellSize = dynamicCellSize,
                    previewPlacement = uiState.previewPlacement,
                    previewColor = uiState.previewColor,
                    isValidPreview = uiState.isValidPreview,
                    activeHintCoord = uiState.activeHint?.second,
                    onBoardPositioned = { offset, cellSize ->
                        viewModel.setBoardMetrics(offset, cellSize)
                    }
                )
            }

            // Bottom Tray & Booster Actions (Hint & Undo)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Boosters Row (Hint & Undo)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Undo Button with Clean Badge
                    GamingIconButton(
                        icon = Icons.Rounded.Undo,
                        onClick = { viewModel.requestUndo() },
                        badgeText = "${viewModel.preferences.undosCount}",
                        backgroundColor = if (uiState.canUndo) Color.White else Color(0xFFF1F5F9),
                        iconTint = if (uiState.canUndo) BrightBlue else TextMuted,
                        size = 46.dp,
                        testTag = "gameplay_undo_btn"
                    )

                    // Combo Streak Pill (if active)
                    if (uiState.comboStreak > 1) {
                        Box(
                            modifier = Modifier
                                .shadow(6.dp, RoundedCornerShape(16.dp), ambientColor = Color(0xFFFF5252))
                                .clip(RoundedCornerShape(16.dp))
                                .background(BrightCoral)
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "COMBO x${uiState.comboStreak} 🔥",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Hint Button with Clean Badge
                    GamingIconButton(
                        icon = Icons.Rounded.Lightbulb,
                        onClick = { viewModel.requestHint() },
                        badgeText = "${viewModel.preferences.hintsCount}",
                        backgroundColor = Color.White,
                        iconTint = GoldenSun,
                        size = 46.dp,
                        testTag = "gameplay_hint_btn"
                    )
                }

                // 3-Slot Block Tray with animated staggered entry and validity awareness
                TrayView(
                    trayBlocks = uiState.trayBlocks,
                    worldTheme = worldTheme,
                    cellSize = dynamicCellSize,
                    batchId = uiState.batchId,
                    slotValidMoves = uiState.slotValidMoves,
                    hintTrayIndex = uiState.activeHint?.first,
                    onDragStart = { trayIdx, touchPos ->
                        viewModel.onDragStart(trayIdx, touchPos)
                    },
                    onDragMove = { touchPos ->
                        viewModel.onDragMove(touchPos)
                    },
                    onDragEnd = { touchPos, center ->
                        viewModel.onDragEnd(touchPos, center)
                    },
                    onDragCancel = {
                        viewModel.cancelDrag()
                    }
                )
            }
        }

        // 3. Floating Dragged Block under finger
        if (uiState.isDragging && uiState.draggedTrayIndex != null) {
            val shape = uiState.trayBlocks.getOrNull(uiState.draggedTrayIndex!!)
            if (shape != null) {
                val density = LocalDensity.current
                val touchOffset = uiState.dragTouchPosition

                val elevatedY = touchOffset.y - with(density) { (dynamicCellSize * 1.5f).toPx() }
                val elevatedX = touchOffset.x - with(density) { (dynamicCellSize * ((shape.cols - 1).toFloat() * 0.5f)).toPx() }

                Box(
                    modifier = Modifier
                        .offset(
                            x = with(density) { elevatedX.toDp() },
                            y = with(density) { elevatedY.toDp() }
                        )
                        .scale(1.1f)
                        .shadow(16.dp, ambientColor = shape.color.primaryColor)
                ) {
                    ShapePreview3D(
                        shape = shape,
                        cellSize = dynamicCellSize
                    )
                }
            }
        }

        // 4. 60 FPS Canvas VFX Overlay
        VfxOverlay(particleManager = viewModel.particleManager)

        // 5. Retry Confirmation Modal
        if (showRetryDialog) {
            RetryConfirmationDialog(
                levelNumber = uiState.currentLevelNumber,
                onConfirmRetry = {
                    showRetryDialog = false
                    viewModel.restartCurrentLevel()
                },
                onDismiss = {
                    viewModel.soundManager.playButtonClick()
                    showRetryDialog = false
                }
            )
        }

        // 6. Level Complete Modal
        if (uiState.isLevelCompleted) {
            LevelCompleteDialog(
                levelNumber = uiState.currentLevelNumber,
                stars = uiState.starsEarned,
                score = uiState.score,
                movesUsed = uiState.movesUsed,
                worldTheme = worldTheme,
                onNextLevel = {
                    viewModel.adsManager.registerLevelCompletion()
                    if (activity != null) {
                        viewModel.adsManager.showInterstitialIfAllowed(activity) {
                            if (uiState.isWorldCompleteModalVisible) {
                                viewModel.dismissWorldUnlockModal()
                                onWorldCompleteContinue()
                            } else {
                                viewModel.nextLevel()
                            }
                        }
                    } else {
                        if (uiState.isWorldCompleteModalVisible) {
                            viewModel.dismissWorldUnlockModal()
                            onWorldCompleteContinue()
                        } else {
                            viewModel.nextLevel()
                        }
                    }
                },
                onReplay = { viewModel.restartCurrentLevel() }
            )
        }

        // 7. World Unlock Celebration Modal
        if (uiState.isWorldCompleteModalVisible && uiState.unlockedNextWorld != null) {
            WorldUnlockDialog(
                unlockedWorld = uiState.unlockedNextWorld!!,
                onEnterWorld = {
                    viewModel.dismissWorldUnlockModal()
                    viewModel.loadLevel(uiState.unlockedNextWorld!!.startLevel)
                }
            )
        }

        // 8. Pause Dialog
        if (uiState.isPaused) {
            PauseDialog(
                viewModel = viewModel,
                worldTheme = worldTheme,
                onResume = { viewModel.togglePause() },
                onRestart = {
                    viewModel.togglePause()
                    viewModel.restartCurrentLevel()
                },
                onQuit = {
                    viewModel.togglePause()
                    onBackToLevelSelect()
                }
            )
        }

        // 9. Game Over / Out of Moves Dialog
        if (uiState.isGameOver && !uiState.isLevelCompleted) {
            GameOverDialog(
                score = uiState.score,
                onRetry = { viewModel.restartCurrentLevel() },
                onBackToMap = onBackToLevelSelect
            )
        }
    }
}

