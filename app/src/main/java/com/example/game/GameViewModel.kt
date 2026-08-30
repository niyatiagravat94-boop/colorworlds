package com.example.game

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.HapticManager
import com.example.audio.SoundManager
import com.example.ads.AdsManager
import com.example.data.local.GraphicsQuality
import com.example.data.local.LevelProgressEntity
import com.example.data.model.*
import com.example.data.repository.GameRepository
import com.example.data.repository.LevelRepository
import com.example.data.repository.RetentionManager
import com.example.vfx.ParticleManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class GameUiState(
    val currentLevelNumber: Int = 1,
    val currentWorldId: WorldId = WorldId.COLOR_GARDEN,
    val levelData: LevelData = LevelRepository.getLevel(1),
    val grid: List<List<GridCell>> = emptyList(),
    val trayBlocks: List<BlockShape?> = emptyList(),
    val batchId: Int = 0,
    val score: Int = 0,
    val movesUsed: Int = 0,
    val maxMoves: Int? = null,
    val objectiveProgress: Pair<Int, Int> = Pair(0, 1),
    val objectiveTitle: String = "",
    val comboStreak: Int = 0,
    val isLevelCompleted: Boolean = false,
    val isGameOver: Boolean = false,
    val starsEarned: Int = 0,
    val isPaused: Boolean = false,
    val isWorldCompleteModalVisible: Boolean = false,
    val unlockedNextWorld: WorldId? = null,
    val activeHint: Pair<Int, Coordinate>? = null,
    val canUndo: Boolean = false,
    val isDragging: Boolean = false,
    val draggedTrayIndex: Int? = null,
    val dragTouchPosition: Offset = Offset.Zero,
    val previewPlacement: List<Coordinate>? = null,
    val previewColor: BlockColor? = null,
    val candidateGridCoord: Coordinate? = null,
    val isValidPreview: Boolean = false,
    val boardBoundsOffset: Offset = Offset.Zero,
    val cellSizePx: Float = 0f,
    val screenShakeIntensity: Float = 0f,
    // Debug & Validation telemetry
    val boardDensity: Float = 0f,
    val slotValidMoves: List<Boolean> = listOf(true, true, true),
    val playableShapesCount: Int = 3,
    val isDebugPanelVisible: Boolean = false,
    val highlightedValidCoords: List<Coordinate> = emptyList(),
    // Retention State
    val dailyRewards: List<DailyRewardItem> = emptyList(),
    val dailyMissions: List<DailyMission> = emptyList(),
    val milestones: List<MilestoneAchievement> = emptyList(),
    val isDailyRewardClaimable: Boolean = false,
    val loginStreakDays: Int = 1,
    val hasUnclaimedRewards: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    val repository = GameRepository(application, viewModelScope)
    val preferences = repository.preferences
    val retentionManager = RetentionManager(preferences)
    val soundManager = SoundManager(preferences)
    val hapticManager = HapticManager(application, preferences)
    val particleManager = ParticleManager()
    val adsManager = AdsManager(application)

    private val failureStreakPerLevel = mutableMapOf<Int, Int>()

    private var engine = PuzzleEngine(LevelRepository.getLevel(1))

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val allProgress: StateFlow<List<LevelProgressEntity>> = repository.allProgressFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalStars: StateFlow<Int> = repository.totalStarsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val highestUnlockedLevel: StateFlow<Int> = repository.highestUnlockedLevelFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    private var gameLoopJob: Job? = null

    init {
        adsManager.soundManager = soundManager
        adsManager.initialize()
        retentionManager.checkAndRecordDailyLogin()
        refreshRetentionState()
        startGameLoop()
        loadLevel(1)

        viewModelScope.launch {
            totalStars.collect {
                refreshRetentionState()
            }
        }
    }

    fun refreshRetentionState() {
        val stars = totalStars.value
        val rewards = retentionManager.getDailyRewards()
        val missions = retentionManager.getDailyMissions()
        val milestones = retentionManager.getMilestones(stars)
        val isClaimable = retentionManager.isDailyRewardClaimable()
        val hasUnclaimed = retentionManager.hasAnyUnclaimedRewards(stars)

        _uiState.update {
            it.copy(
                dailyRewards = rewards,
                dailyMissions = missions,
                milestones = milestones,
                isDailyRewardClaimable = isClaimable,
                loginStreakDays = preferences.loginStreakDays,
                hasUnclaimedRewards = hasUnclaimed
            )
        }
    }

    fun claimDailyReward() {
        val reward = retentionManager.claimDailyReward()
        if (reward != null) {
            soundManager.playWorldUnlock()
            hapticManager.celebrate()
            refreshRetentionState()
        }
    }

    fun claimDailyMission(missionId: String) {
        val mission = retentionManager.claimDailyMission(missionId)
        if (mission != null) {
            soundManager.playStarEarned(3)
            hapticManager.celebrate()
            refreshRetentionState()
        }
    }

    fun claimMilestone(milestoneId: String) {
        val milestone = retentionManager.claimMilestone(milestoneId, totalStars.value)
        if (milestone != null) {
            soundManager.playWorldUnlock()
            hapticManager.celebrate()
            refreshRetentionState()
        }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastTime = System.nanoTime()
            while (true) {
                val currentTime = System.nanoTime()
                val dt = ((currentTime - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastTime = currentTime

                particleManager.update(dt)

                // Screen shake decay
                if (_uiState.value.screenShakeIntensity > 0f) {
                    val newShake = (_uiState.value.screenShakeIntensity - dt * 25f).coerceAtLeast(0f)
                    _uiState.update { it.copy(screenShakeIntensity = newShake) }
                }

                delay(16)
            }
        }
    }

    fun loadLevel(levelNumber: Int) {
        val level = LevelRepository.getLevel(levelNumber)
        val failureCount = failureStreakPerLevel[levelNumber] ?: 0
        engine = PuzzleEngine(level, failureStreak = failureCount)
        val world = level.worldId
        soundManager.startAmbientMusic(world)

        _uiState.update {
            it.copy(
                currentLevelNumber = levelNumber,
                currentWorldId = world,
                levelData = level,
                grid = engine.grid.map { row -> row.toList() },
                trayBlocks = engine.trayBlocks.toList(),
                batchId = engine.batchId,
                score = 0,
                movesUsed = 0,
                maxMoves = level.maxMoves,
                objectiveProgress = engine.getObjectiveProgress(),
                objectiveTitle = level.objective.getDisplayName(),
                comboStreak = 0,
                isLevelCompleted = false,
                isGameOver = false,
                starsEarned = 0,
                isPaused = false,
                isWorldCompleteModalVisible = false,
                unlockedNextWorld = null,
                activeHint = null,
                canUndo = false,
                isDragging = false,
                draggedTrayIndex = null,
                previewPlacement = null,
                previewColor = null,
                candidateGridCoord = null,
                boardDensity = engine.getBoardDensity(),
                slotValidMoves = listOf(
                    engine.hasValidMoveForSlot(0),
                    engine.hasValidMoveForSlot(1),
                    engine.hasValidMoveForSlot(2)
                ),
                playableShapesCount = engine.countPlayableShapes(),
                highlightedValidCoords = emptyList()
            )
        }
        particleManager.clear()
    }

    fun restartCurrentLevel() {
        soundManager.playButtonClick()
        hapticManager.tap()
        val lvl = _uiState.value.currentLevelNumber
        failureStreakPerLevel[lvl] = (failureStreakPerLevel[lvl] ?: 0) + 1
        loadLevel(lvl)
    }

    fun nextLevel() {
        val next = _uiState.value.currentLevelNumber + 1
        if (next <= LevelRepository.getTotalLevelCount()) {
            loadLevel(next)
        }
    }

    fun setBoardMetrics(offset: Offset, cellSize: Float) {
        _uiState.update {
            it.copy(boardBoundsOffset = offset, cellSizePx = cellSize)
        }
    }

    fun onDragStart(trayIndex: Int, touchPos: Offset) {
        val shape = engine.trayBlocks.getOrNull(trayIndex) ?: return
        soundManager.playBlockPickup()
        hapticManager.tap()

        _uiState.update {
            it.copy(
                isDragging = true,
                draggedTrayIndex = trayIndex,
                dragTouchPosition = touchPos,
                previewColor = shape.color,
                activeHint = null
            )
        }
        updateDragPreview(shape, touchPos)
    }

    fun onDragMove(touchPos: Offset) {
        val trayIndex = _uiState.value.draggedTrayIndex ?: return
        val shape = engine.trayBlocks.getOrNull(trayIndex) ?: return
        _uiState.update { it.copy(dragTouchPosition = touchPos) }
        updateDragPreview(shape, touchPos)
    }

    private fun updateDragPreview(shape: BlockShape, touchPos: Offset) {
        val state = _uiState.value
        val boardOffset = state.boardBoundsOffset
        val cellSize = state.cellSizePx

        if (cellSize <= 0f) return

        val elevatedY = touchPos.y - (cellSize * 1.5f)
        val elevatedX = touchPos.x - ((shape.cols - 1) * cellSize * 0.5f)

        val localX = elevatedX - boardOffset.x
        val localY = elevatedY - boardOffset.y

        val gridC = kotlin.math.round(localX / cellSize).toInt()
        val gridR = kotlin.math.round(localY / cellSize).toInt()

        if (gridR in 0 until engine.rows && gridC in 0 until engine.cols) {
            val coords = engine.getValidPlacementCells(shape, gridR, gridC)
            if (coords != null) {
                _uiState.update {
                    it.copy(
                        previewPlacement = coords,
                        previewColor = shape.color,
                        isValidPreview = true,
                        candidateGridCoord = Coordinate(gridR, gridC)
                    )
                }
                return
            }
        }

        _uiState.update {
            it.copy(
                previewPlacement = null,
                previewColor = null,
                isValidPreview = false,
                candidateGridCoord = null
            )
        }
    }

    fun onDragEnd(touchPos: Offset, boardCenter: Offset) {
        val trayIndex = _uiState.value.draggedTrayIndex
        if (trayIndex == null) {
            cancelDrag()
            return
        }

        val shape = engine.trayBlocks.getOrNull(trayIndex)
        if (shape == null) {
            cancelDrag()
            return
        }

        val state = _uiState.value
        val boardOffset = state.boardBoundsOffset
        val cellSize = state.cellSizePx

        val candidate = state.candidateGridCoord
        val effectiveTouch = if (touchPos != Offset.Zero) touchPos else state.dragTouchPosition
        val elevatedY = effectiveTouch.y - (cellSize * 1.5f)
        val elevatedX = effectiveTouch.x - ((shape.cols - 1) * cellSize * 0.5f)

        val localX = elevatedX - boardOffset.x
        val localY = elevatedY - boardOffset.y

        val calcC = kotlin.math.round(localX / cellSize).toInt()
        val calcR = kotlin.math.round(localY / cellSize).toInt()

        val finalR = if (candidate != null && engine.canPlace(shape, candidate.r, candidate.c)) {
            candidate.r
        } else if (calcR in 0 until engine.rows && calcC in 0 until engine.cols && engine.canPlace(shape, calcR, calcC)) {
            calcR
        } else null

        val finalC = if (candidate != null && engine.canPlace(shape, candidate.r, candidate.c)) {
            candidate.c
        } else if (calcR in 0 until engine.rows && calcC in 0 until engine.cols && engine.canPlace(shape, calcR, calcC)) {
            calcC
        } else null

        if (finalR != null && finalC != null) {
            // 1. Valid placement executed in engine (clears lines, updates scores, replenishes if batch empty)
            val clearResult = engine.placeBlock(trayIndex, finalR, finalC)
            soundManager.playBlockSnap()
            hapticManager.snap()

            val placeCenter = Offset(
                boardOffset.x + (finalC + shape.cols * 0.5f) * cellSize,
                boardOffset.y + (finalR + shape.rows * 0.5f) * cellSize
            )
            particleManager.spawnBlockPlacementBurst(placeCenter, shape.color.primaryColor)
            particleManager.addFloatingScore("+${shape.size * 10}", placeCenter, shape.color.lightFacetColor)

            val linesClearedCount = if (clearResult != null) clearResult.clearedRows.size + clearResult.clearedCols.size else 0
            val pts = (shape.size * 10) + (clearResult?.pointsEarned ?: 0)

            retentionManager.onGameEventProgress(
                linesCleared = linesClearedCount,
                scoreEarned = pts,
                comboAchieved = clearResult?.comboCount ?: 0,
                levelCompleted = false
            )
            refreshRetentionState()

            if (clearResult != null && clearResult.clearedCells.isNotEmpty()) {
                soundManager.playLineClear(clearResult.comboCount)
                hapticManager.clear()

                val clearOffsets = clearResult.clearedCells.map { coord ->
                    Offset(
                        boardOffset.x + (coord.c + 0.5f) * cellSize,
                        boardOffset.y + (coord.r + 0.5f) * cellSize
                    )
                }
                val colors = clearResult.clearedColors.map { it.primaryColor }
                particleManager.spawnLineClearExplosion(clearOffsets, colors)

                val clearCenter = clearOffsets.firstOrNull() ?: placeCenter
                particleManager.addFloatingScore("+${clearResult.pointsEarned}", clearCenter, Color(0xFFFFD600))

                if (clearResult.comboCount > 1) {
                    particleManager.triggerComboBanner(clearResult.comboCount)
                    soundManager.playComboCheer(clearResult.comboCount)
                    if (clearResult.comboCount > preferences.highestComboEver) {
                        preferences.highestComboEver = clearResult.comboCount
                    }
                }

                _uiState.update { it.copy(screenShakeIntensity = (clearResult.comboCount * 4f).coerceAtMost(16f)) }
            }

            // 2. Check Level Completion
            val completed = engine.isObjectiveCompleted()
            val stars = if (completed) engine.calculateStars() else 0

            // 3. Check Game Over strictly AFTER line clearing and tray update
            val isOver = if (completed) false else engine.isGameOver()

            if (completed) {
                failureStreakPerLevel.remove(state.currentLevelNumber)
                soundManager.playLevelComplete()
                hapticManager.celebrate()
                particleManager.spawnConfettiCelebration(800f, 1200f)

                retentionManager.onGameEventProgress(levelCompleted = true)
                refreshRetentionState()

                viewModelScope.launch {
                    val unlockedNewWorld = repository.saveLevelCompletion(
                        levelNumber = state.currentLevelNumber,
                        score = engine.score,
                        starsEarned = stars
                    )
                    if (unlockedNewWorld) {
                        val nextWorld = WorldId.forLevel(state.currentLevelNumber + 1)
                        soundManager.playWorldUnlock()
                        _uiState.update {
                            it.copy(
                                isWorldCompleteModalVisible = true,
                                unlockedNextWorld = nextWorld
                            )
                        }
                    }
                }
            } else if (isOver) {
                soundManager.playInvalidPlacement()
                hapticManager.clear()
            }

            _uiState.update {
                it.copy(
                    isDragging = false,
                    draggedTrayIndex = null,
                    previewPlacement = null,
                    previewColor = null,
                    candidateGridCoord = null,
                    grid = engine.grid.map { r -> r.toList() },
                    trayBlocks = engine.trayBlocks.toList(),
                    batchId = engine.batchId,
                    score = engine.score,
                    movesUsed = engine.movesUsed,
                    objectiveProgress = engine.getObjectiveProgress(),
                    comboStreak = engine.comboStreak,
                    isLevelCompleted = completed,
                    isGameOver = isOver,
                    starsEarned = stars,
                    canUndo = engine.canUndo(),
                    boardDensity = engine.getBoardDensity(),
                    slotValidMoves = listOf(
                        engine.hasValidMoveForSlot(0),
                        engine.hasValidMoveForSlot(1),
                        engine.hasValidMoveForSlot(2)
                    ),
                    playableShapesCount = engine.countPlayableShapes(),
                    highlightedValidCoords = emptyList()
                )
            }
        } else {
            // Invalid placement
            soundManager.playInvalidPlacement()
            cancelDrag()
        }
    }

    fun cancelDrag() {
        _uiState.update {
            it.copy(
                isDragging = false,
                draggedTrayIndex = null,
                previewPlacement = null,
                previewColor = null,
                candidateGridCoord = null,
                isValidPreview = false
            )
        }
    }

    fun requestHint() {
        if (preferences.hintsCount <= 0) return
        val hint = engine.findHint()
        if (hint != null) {
            preferences.hintsCount--
            soundManager.playStarEarned(2)
            hapticManager.tap()
            _uiState.update { it.copy(activeHint = hint) }
        }
    }

    fun requestUndo() {
        if (!engine.canUndo() || preferences.undosCount <= 0) return
        if (engine.undo()) {
            preferences.undosCount--
            soundManager.playButtonClick()
            hapticManager.tap()
            _uiState.update {
                it.copy(
                    grid = engine.grid.map { r -> r.toList() },
                    trayBlocks = engine.trayBlocks.toList(),
                    batchId = engine.batchId,
                    score = engine.score,
                    movesUsed = engine.movesUsed,
                    objectiveProgress = engine.getObjectiveProgress(),
                    comboStreak = engine.comboStreak,
                    isLevelCompleted = false,
                    isGameOver = false,
                    canUndo = engine.canUndo(),
                    boardDensity = engine.getBoardDensity(),
                    slotValidMoves = listOf(
                        engine.hasValidMoveForSlot(0),
                        engine.hasValidMoveForSlot(1),
                        engine.hasValidMoveForSlot(2)
                    ),
                    playableShapesCount = engine.countPlayableShapes(),
                    highlightedValidCoords = emptyList()
                )
            }
        }
    }

    fun togglePause() {
        soundManager.playButtonClick()
        val newPaused = !_uiState.value.isPaused
        soundManager.setDucked(newPaused)
        _uiState.update { it.copy(isPaused = newPaused) }
    }

    fun setMusicVolume(volume: Float) {
        preferences.musicVolume = volume
        soundManager.updateMusicVolume()
    }

    fun setSfxVolume(volume: Float) {
        preferences.sfxVolume = volume
    }

    fun toggleMusic() {
        preferences.isMusicEnabled = !preferences.isMusicEnabled
        soundManager.updateMusicVolume()
    }

    fun toggleSfx() {
        preferences.isSfxEnabled = !preferences.isSfxEnabled
        if (preferences.isSfxEnabled) {
            soundManager.playButtonClick()
        }
    }

    fun toggleHaptics() {
        preferences.isHapticsEnabled = !preferences.isHapticsEnabled
        hapticManager.tap()
    }

    fun setGraphicsQuality(quality: GraphicsQuality) {
        preferences.graphicsQuality = quality
    }

    fun dismissWorldUnlockModal() {
        _uiState.update { it.copy(isWorldCompleteModalVisible = false) }
    }

    fun continueWithReward() {
        engine.debugForceReplenish()
        _uiState.update {
            it.copy(
                isGameOver = false,
                trayBlocks = engine.trayBlocks.toList(),
                batchId = engine.batchId,
                movesUsed = (it.movesUsed - 5).coerceAtLeast(0),
                slotValidMoves = listOf(
                    engine.hasValidMoveForSlot(0),
                    engine.hasValidMoveForSlot(1),
                    engine.hasValidMoveForSlot(2)
                ),
                playableShapesCount = engine.countPlayableShapes()
            )
        }
        soundManager.playLevelComplete()
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllProgress()
            refreshRetentionState()
            loadLevel(1)
        }
    }

    // --- Dev / Debug Support ---
    fun toggleDebugPanel() {
        _uiState.update { it.copy(isDebugPanelVisible = !it.isDebugPanelVisible) }
    }

    fun debugForceNewBatch() {
        engine.debugForceReplenish()
        _uiState.update {
            it.copy(
                trayBlocks = engine.trayBlocks.toList(),
                batchId = engine.batchId,
                slotValidMoves = listOf(
                    engine.hasValidMoveForSlot(0),
                    engine.hasValidMoveForSlot(1),
                    engine.hasValidMoveForSlot(2)
                ),
                playableShapesCount = engine.countPlayableShapes()
            )
        }
    }

    fun debugForceGameOver() {
        _uiState.update { it.copy(isGameOver = true) }
    }

    fun debugClearBoard() {
        engine.debugClearBoard()
        _uiState.update {
            it.copy(
                grid = engine.grid.map { r -> r.toList() },
                boardDensity = 0f,
                slotValidMoves = listOf(
                    engine.hasValidMoveForSlot(0),
                    engine.hasValidMoveForSlot(1),
                    engine.hasValidMoveForSlot(2)
                ),
                playableShapesCount = engine.countPlayableShapes()
            )
        }
    }

    fun debugToggleHighlightValidMoves() {
        if (_uiState.value.highlightedValidCoords.isNotEmpty()) {
            _uiState.update { it.copy(highlightedValidCoords = emptyList()) }
        } else {
            val allValid = engine.getAllValidPlacements()
            _uiState.update { it.copy(highlightedValidCoords = allValid) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
