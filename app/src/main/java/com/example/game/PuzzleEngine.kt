package com.example.game

import com.example.data.model.*

data class ClearResult(
    val clearedRows: List<Int>,
    val clearedCols: List<Int>,
    val clearedCells: List<Coordinate>,
    val clearedColors: List<BlockColor>,
    val clearedObstaclesCount: Int,
    val pointsEarned: Int,
    val comboCount: Int,
    val newBatchGenerated: Boolean = false
)

data class BoardSnapshot(
    val grid: Array<Array<GridCell>>,
    val trayBlocks: List<BlockShape?>,
    val score: Int,
    val movesUsed: Int,
    val linesCleared: Int,
    val colorCollected: Map<BlockColor, Int>,
    val obstaclesCleared: Int,
    val comboStreak: Int,
    val batchId: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BoardSnapshot
        return score == other.score && movesUsed == other.movesUsed && linesCleared == other.linesCleared
    }

    override fun hashCode(): Int {
        var result = score
        result = 31 * result + movesUsed
        result = 31 * result + linesCleared
        return result
    }
}

class PuzzleEngine(
    val levelData: LevelData,
    failureStreak: Int = 0
) {
    val rows = levelData.gridRows
    val cols = levelData.gridCols

    var grid: Array<Array<GridCell>> = Array(rows) { Array(cols) { GridCell() } }
        private set

    var trayBlocks = mutableListOf<BlockShape?>()
        private set

    var batchId: Int = 0
        private set

    var score: Int = 0
        private set

    var movesUsed: Int = 0
        private set

    var linesClearedTotal: Int = 0
        private set

    val colorCollected = mutableMapOf<BlockColor, Int>()
    var obstaclesClearedTotal: Int = 0
        private set

    var comboStreak: Int = 0
        private set

    var consecutiveClears: Int = 0
        private set

    private val undoStack = mutableListOf<BoardSnapshot>()
    private val shapeGenerator = ShapeGenerator(levelData, failureStreak)

    init {
        reset()
    }

    fun reset() {
        grid = Array(rows) { Array(cols) { GridCell() } }
        levelData.initialSetup.initialCells.forEach { (coord, cell) ->
            if (coord.r in 0 until rows && coord.c in 0 until cols) {
                grid[coord.r][coord.c] = cell
            }
        }
        score = 0
        movesUsed = 0
        linesClearedTotal = 0
        colorCollected.clear()
        obstaclesClearedTotal = 0
        comboStreak = 0
        consecutiveClears = 0
        undoStack.clear()
        replenishTray()
    }

    fun getBoardDensity(): Float {
        var occupied = 0
        val total = rows * cols
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (grid[r][c].type != CellType.EMPTY) {
                    occupied++
                }
            }
        }
        return if (total > 0) occupied.toFloat() / total.toFloat() else 0f
    }

    fun canPlace(shape: BlockShape, startR: Int, startC: Int): Boolean {
        for (coord in shape.coordinates) {
            val r = startR + coord.r
            val c = startC + coord.c
            if (r !in 0 until rows || c !in 0 until cols) {
                return false
            }
            if (grid[r][c].type != CellType.EMPTY) {
                return false
            }
        }
        return true
    }

    fun getValidPlacementCells(shape: BlockShape, startR: Int, startC: Int): List<Coordinate>? {
        if (!canPlace(shape, startR, startC)) return null
        return shape.coordinates.map { Coordinate(startR + it.r, startC + it.c) }
    }

    private fun takeSnapshot(): BoardSnapshot {
        val gridCopy = Array(rows) { r ->
            Array(cols) { c ->
                grid[r][c]
            }
        }
        return BoardSnapshot(
            grid = gridCopy,
            trayBlocks = trayBlocks.toList(),
            score = score,
            movesUsed = movesUsed,
            linesCleared = linesClearedTotal,
            colorCollected = colorCollected.toMap(),
            obstaclesCleared = obstaclesClearedTotal,
            comboStreak = comboStreak,
            batchId = batchId
        )
    }

    /**
     * Places a block from the tray into the board.
     * Order of operations:
     * 1. Commit shape to grid
     * 2. Clear tray slot
     * 3. Check and clear completed rows & columns
     * 4. Update board occupancy, score, combos
     * 5. Check if entire batch is consumed (all slots null) -> replenish tray with next 3
     */
    fun placeBlock(trayIndex: Int, startR: Int, startC: Int): ClearResult? {
        val shape = trayBlocks.getOrNull(trayIndex) ?: return null
        if (!canPlace(shape, startR, startC)) return null

        // 1. Save undo snapshot
        undoStack.add(takeSnapshot())

        // 2. Place cells onto grid
        for (coord in shape.coordinates) {
            val r = startR + coord.r
            val c = startC + coord.c
            grid[r][c] = GridCell(type = CellType.FILLED, color = shape.color)
        }

        // 3. Mark tray slot as used (empty)
        trayBlocks[trayIndex] = null
        movesUsed++

        // Base score for placing block: 10 pts per cell
        val placementScore = shape.size * 10
        score += placementScore

        // 4. Check & clear completed lines & shatter obstacles
        val clearResult = checkAndClearLines()

        // 5. Check if all 3 shapes in current batch have been used
        var generatedNewBatch = false
        if (trayBlocks.all { it == null }) {
            replenishTray()
            generatedNewBatch = true
        }

        return clearResult.copy(newBatchGenerated = generatedNewBatch)
    }

    private fun checkAndClearLines(): ClearResult {
        val rowsToClear = mutableListOf<Int>()
        val colsToClear = mutableListOf<Int>()

        // Check rows
        for (r in 0 until rows) {
            var full = true
            for (c in 0 until cols) {
                if (grid[r][c].type == CellType.EMPTY) {
                    full = false
                    break
                }
            }
            if (full) rowsToClear.add(r)
        }

        // Check columns
        for (c in 0 until cols) {
            var full = true
            for (r in 0 until rows) {
                if (grid[r][c].type == CellType.EMPTY) {
                    full = false
                    break
                }
            }
            if (full) colsToClear.add(c)
        }

        val totalLines = rowsToClear.size + colsToClear.size
        val clearedCoords = mutableSetOf<Coordinate>()
        val clearedColors = mutableListOf<BlockColor>()
        var obstaclesShattered = 0

        if (totalLines > 0) {
            consecutiveClears++
            comboStreak = consecutiveClears

            for (r in rowsToClear) {
                for (c in 0 until cols) {
                    clearedCoords.add(Coordinate(r, c))
                }
            }
            for (c in colsToClear) {
                for (r in 0 until rows) {
                    clearedCoords.add(Coordinate(r, c))
                }
            }

            // Check if adjacent obstacles get shattered
            val affectedObstacles = mutableSetOf<Coordinate>()
            for (coord in clearedCoords) {
                val neighbors = listOf(
                    Coordinate(coord.r - 1, coord.c),
                    Coordinate(coord.r + 1, coord.c),
                    Coordinate(coord.r, coord.c - 1),
                    Coordinate(coord.r, coord.c + 1)
                )
                for (n in neighbors) {
                    if (n.r in 0 until rows && n.c in 0 until cols) {
                        if (grid[n.r][n.c].type == CellType.OBSTACLE_CRYSTAL) {
                            affectedObstacles.add(n)
                        }
                    }
                }
            }

            // Clear cells from grid & track collected colors
            for (coord in clearedCoords) {
                val cell = grid[coord.r][coord.c]
                if (cell.type == CellType.OBSTACLE_CRYSTAL) {
                    obstaclesShattered++
                }
                cell.color?.let { color ->
                    clearedColors.add(color)
                    colorCollected[color] = (colorCollected[color] ?: 0) + 1
                }
                grid[coord.r][coord.c] = GridCell(type = CellType.EMPTY, color = null)
            }

            for (obsCoord in affectedObstacles) {
                val cell = grid[obsCoord.r][obsCoord.c]
                if (cell.type == CellType.OBSTACLE_CRYSTAL) {
                    obstaclesShattered++
                    cell.color?.let { clearedColors.add(it) }
                    clearedCoords.add(obsCoord)
                    grid[obsCoord.r][obsCoord.c] = GridCell(type = CellType.EMPTY, color = null)
                }
            }

            linesClearedTotal += totalLines
            obstaclesClearedTotal += obstaclesShattered

            // Score calculation
            val baseLineScore = when (totalLines) {
                1 -> 100
                2 -> 300
                3 -> 600
                4 -> 1000
                5 -> 1500
                else -> totalLines * 350
            }
            val comboBonus = if (comboStreak > 1) (comboStreak - 1) * 150 else 0
            val obstacleBonus = obstaclesShattered * 250
            val linePoints = (baseLineScore + comboBonus + obstacleBonus)

            score += linePoints

            return ClearResult(
                clearedRows = rowsToClear,
                clearedCols = colsToClear,
                clearedCells = clearedCoords.toList(),
                clearedColors = clearedColors,
                clearedObstaclesCount = obstaclesShattered,
                pointsEarned = linePoints,
                comboCount = comboStreak
            )
        } else {
            consecutiveClears = 0
            return ClearResult(
                clearedRows = emptyList(),
                clearedCols = emptyList(),
                clearedCells = emptyList(),
                clearedColors = emptyList(),
                clearedObstaclesCount = 0,
                pointsEarned = 0,
                comboCount = 0
            )
        }
    }

    /**
     * Generates a new 3-shape batch using the smart ShapeGenerator.
     */
    fun replenishTray() {
        trayBlocks.clear()
        val newShapes = shapeGenerator.generateNextBatch(grid, rows, cols)
        trayBlocks.addAll(newShapes)
        batchId++
    }

    /**
     * Central valid move checker:
     * Checks all currently available (non-null) shapes in the tray.
     * Returns TRUE if at least ONE shape has a valid placement anywhere on the board.
     * Returns FALSE if NO available shape can fit.
     */
    fun hasAnyValidMove(): Boolean {
        val activeTrayShapes = trayBlocks.filterNotNull()
        if (activeTrayShapes.isEmpty()) return true

        for (shape in activeTrayShapes) {
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    if (canPlace(shape, r, c)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * Checks if a specific tray slot has any valid move on the board.
     */
    fun hasValidMoveForSlot(trayIndex: Int): Boolean {
        val shape = trayBlocks.getOrNull(trayIndex) ?: return false
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (canPlace(shape, r, c)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Counts how many available shapes in the tray have valid placements.
     */
    fun countPlayableShapes(): Int {
        var count = 0
        for (i in 0 until trayBlocks.size) {
            if (hasValidMoveForSlot(i)) count++
        }
        return count
    }

    /**
     * Returns all coordinates on the board where at least one tray shape can fit.
     */
    fun getAllValidPlacements(): List<Coordinate> {
        val validCoords = mutableSetOf<Coordinate>()
        val activeShapes = trayBlocks.filterNotNull()
        for (shape in activeShapes) {
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    if (canPlace(shape, r, c)) {
                        validCoords.add(Coordinate(r, c))
                    }
                }
            }
        }
        return validCoords.toList()
    }

    fun canUndo(): Boolean = undoStack.isNotEmpty()

    fun undo(): Boolean {
        val lastSnapshot = undoStack.removeLastOrNull() ?: return false
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                grid[r][c] = lastSnapshot.grid[r][c]
            }
        }
        trayBlocks = lastSnapshot.trayBlocks.toMutableList()
        score = lastSnapshot.score
        movesUsed = lastSnapshot.movesUsed
        linesClearedTotal = lastSnapshot.linesCleared
        colorCollected.clear()
        colorCollected.putAll(lastSnapshot.colorCollected)
        obstaclesClearedTotal = lastSnapshot.obstaclesCleared
        comboStreak = lastSnapshot.comboStreak
        consecutiveClears = lastSnapshot.comboStreak
        batchId = lastSnapshot.batchId
        return true
    }

    /**
     * Game Over happens ONLY when:
     * - Move limit is exceeded (and objective not met), OR
     * - None of the currently available shapes has a valid placement on the logical grid.
     */
    fun isGameOver(): Boolean {
        val activeTrayShapes = trayBlocks.filterNotNull()
        if (activeTrayShapes.isEmpty()) return false

        // Check if move limit exceeded
        levelData.maxMoves?.let { max ->
            if (movesUsed >= max && !isObjectiveCompleted()) {
                return true
            }
        }

        // Check if any shape can fit
        return !hasAnyValidMove()
    }

    fun isObjectiveCompleted(): Boolean {
        return when (val obj = levelData.objective) {
            is LevelObjective.ClearLines -> linesClearedTotal >= obj.targetLines
            is LevelObjective.ReachScore -> score >= obj.targetScore
            is LevelObjective.CollectColor -> (colorCollected[obj.color] ?: 0) >= obj.targetCount
            is LevelObjective.ClearObstacles -> obstaclesClearedTotal >= obj.targetCount
            is LevelObjective.LimitedMoves -> score >= obj.targetScore && movesUsed <= obj.maxMoves
            is LevelObjective.PerformCombos -> comboStreak >= obj.targetCombos || (linesClearedTotal >= 4)
        }
    }

    fun getObjectiveProgress(): Pair<Int, Int> {
        return when (val obj = levelData.objective) {
            is LevelObjective.ClearLines -> Pair(linesClearedTotal.coerceAtMost(obj.targetLines), obj.targetLines)
            is LevelObjective.ReachScore -> Pair(score.coerceAtMost(obj.targetScore), obj.targetScore)
            is LevelObjective.CollectColor -> Pair((colorCollected[obj.color] ?: 0).coerceAtMost(obj.targetCount), obj.targetCount)
            is LevelObjective.ClearObstacles -> Pair(obstaclesClearedTotal.coerceAtMost(obj.targetCount), obj.targetCount)
            is LevelObjective.LimitedMoves -> Pair(score.coerceAtMost(obj.targetScore), obj.targetScore)
            is LevelObjective.PerformCombos -> Pair(comboStreak.coerceAtMost(obj.targetCombos), obj.targetCombos)
        }
    }

    fun calculateStars(): Int {
        if (!isObjectiveCompleted()) return 0
        return when {
            score >= levelData.threeStarScore -> 3
            score >= levelData.twoStarScore -> 2
            else -> 1
        }
    }

    fun findHint(): Pair<Int, Coordinate>? {
        val activeTray = trayBlocks.mapIndexedNotNull { index, shape ->
            if (shape != null) Pair(index, shape) else null
        }
        for ((trayIdx, shape) in activeTray) {
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    if (canPlace(shape, r, c)) {
                        return Pair(trayIdx, Coordinate(r, c))
                    }
                }
            }
        }
        return null
    }

    // Dev / Debug Tools
    fun debugClearBoard() {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                grid[r][c] = GridCell(type = CellType.EMPTY)
            }
        }
    }

    fun debugForceReplenish() {
        replenishTray()
    }
}
