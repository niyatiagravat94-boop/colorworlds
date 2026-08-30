package com.example.game

import com.example.data.model.*
import kotlin.random.Random

object ShapePools {
    val EASY: List<ShapeType> = listOf(
        ShapeType.DOT,
        ShapeType.LINE_2_H,
        ShapeType.LINE_2_V,
        ShapeType.LINE_3_H,
        ShapeType.LINE_3_V,
        ShapeType.SQUARE_2X2,
        ShapeType.CORNER_3_TL,
        ShapeType.CORNER_3_TR,
        ShapeType.CORNER_3_BL,
        ShapeType.CORNER_3_BR
    )

    val MEDIUM: List<ShapeType> = listOf(
        ShapeType.LINE_4_H,
        ShapeType.LINE_4_V,
        ShapeType.L_SMALL_TL,
        ShapeType.L_SMALL_TR,
        ShapeType.L_SMALL_BL,
        ShapeType.L_SMALL_BR,
        ShapeType.T_DOWN,
        ShapeType.T_UP,
        ShapeType.T_LEFT,
        ShapeType.T_RIGHT,
        ShapeType.Z_H,
        ShapeType.Z_V,
        ShapeType.S_H,
        ShapeType.S_V
    )

    val HARD: List<ShapeType> = listOf(
        ShapeType.LINE_5_H,
        ShapeType.LINE_5_V,
        ShapeType.SQUARE_3X3,
        ShapeType.L_LARGE_TL,
        ShapeType.L_LARGE_TR,
        ShapeType.L_LARGE_BL,
        ShapeType.L_LARGE_BR,
        ShapeType.PLUS,
        ShapeType.U_SHAPE
    )

    fun getDifficultyCategory(type: ShapeType): String {
        return when {
            EASY.contains(type) -> "EASY"
            MEDIUM.contains(type) -> "MEDIUM"
            else -> "HARD"
        }
    }
}

class ShapeGenerator(
    private val levelData: LevelData,
    private val failureStreak: Int = 0
) {
    private val recentShapes = ArrayDeque<ShapeType>(6)
    private var shapeBag = mutableListOf<ShapeType>()
    private var batchCount = 0

    fun getBatchCount(): Int = batchCount

    /**
     * Gets the allowed shape pool based on level progression constraints.
     */
    private fun getUnlockedShapePool(): List<ShapeType> {
        val level = levelData.levelNumber
        val explicitPool = levelData.allowedShapePool

        // If level explicitly defines a restricted pool, respect it
        if (explicitPool.isNotEmpty()) {
            return explicitPool
        }

        return when {
            level <= 5 -> ShapePools.EASY
            level <= 10 -> ShapePools.EASY + listOf(
                ShapeType.LINE_4_H, ShapeType.LINE_4_V,
                ShapeType.L_SMALL_TL, ShapeType.L_SMALL_TR, ShapeType.L_SMALL_BL, ShapeType.L_SMALL_BR,
                ShapeType.T_DOWN, ShapeType.T_UP
            )
            level <= 20 -> ShapePools.EASY + ShapePools.MEDIUM
            level <= 30 -> ShapePools.EASY + ShapePools.MEDIUM + listOf(
                ShapeType.L_LARGE_TL, ShapeType.L_LARGE_TR, ShapeType.L_LARGE_BL, ShapeType.L_LARGE_BR,
                ShapeType.PLUS
            )
            else -> ShapePools.EASY + ShapePools.MEDIUM + ShapePools.HARD
        }
    }

    /**
     * Returns the color palette to sample from.
     */
    private fun getColorPool(): List<BlockColor> {
        return levelData.allowedColorPool.ifEmpty {
            listOf(
                BlockColor.CYAN_AQUA,
                BlockColor.BRIGHT_YELLOW,
                BlockColor.MAGENTA_PINK,
                BlockColor.EMERALD_GREEN,
                BlockColor.ELECTRIC_BLUE
            )
        }
    }

    /**
     * Generates a batch of 3 validated BlockShapes for the active board.
     */
    fun generateNextBatch(
        grid: Array<Array<GridCell>>,
        rows: Int,
        cols: Int
    ): List<BlockShape> {
        val currentBatchIndex = batchCount
        batchCount++

        // 1. Check for Predefined Level Batches
        val predefinedList = levelData.predefinedBatches
        if (predefinedList != null && currentBatchIndex < predefinedList.size) {
            val batchTypes = predefinedList[currentBatchIndex]
            val colors = getColorPool()
            return batchTypes.mapIndexed { idx, type ->
                val color = colors[idx % colors.size]
                BlockShape.create(type, color)
            }
        }

        // 2. Controlled Procedural Generation
        val density = calculateBoardDensity(grid, rows, cols)
        val unlockedPool = getUnlockedShapePool()
        val colors = getColorPool()

        val requiredValidCount = when {
            levelData.levelNumber <= 5 -> 2
            density > 0.85f -> 1
            else -> 1
        }

        var candidateBatch: List<BlockShape> = emptyList()
        var attempts = 0
        val maxAttempts = 30

        while (attempts < maxAttempts) {
            attempts++
            candidateBatch = createCandidateBatch(unlockedPool, colors, density)

            val validCount = candidateBatch.count { shape ->
                hasAnyValidPlacement(shape, grid, rows, cols)
            }

            // If we satisfy the minimum playable requirement, accept!
            if (validCount >= requiredValidCount) {
                // Record into recent shapes memory
                candidateBatch.forEach { shape ->
                    if (recentShapes.size >= 6) recentShapes.removeFirst()
                    recentShapes.addLast(shape.type)
                }
                return candidateBatch
            }
        }

        // Fallback: If after 30 attempts high-density board couldn't fit normal picks,
        // specifically pick shapes that CAN fit, favoring smallest shapes.
        val fittingShapes = findPlayableShapesFromPool(unlockedPool, grid, rows, cols, colors)
        if (fittingShapes.isNotEmpty()) {
            val result = mutableListOf<BlockShape>()
            // Add 1 or 2 playable shapes
            result.add(fittingShapes.random())
            while (result.size < 3) {
                val candidate = sampleSingleShape(unlockedPool, density)
                val color = colors.random()
                result.add(BlockShape.create(candidate, color))
            }
            return result
        }

        // Ultimate fallback: Return candidate batch (even if impossible, triggering natural Game Over)
        return candidateBatch.ifEmpty {
            listOf(
                BlockShape.create(ShapeType.DOT, colors.random()),
                BlockShape.create(ShapeType.LINE_2_H, colors.random()),
                BlockShape.create(ShapeType.LINE_2_V, colors.random())
            )
        }
    }

    private fun createCandidateBatch(
        pool: List<ShapeType>,
        colors: List<BlockColor>,
        density: Float
    ): List<BlockShape> {
        val selectedTypes = mutableListOf<ShapeType>()
        var tries = 0

        while (selectedTypes.size < 3 && tries < 20) {
            tries++
            val sampled = sampleSingleShape(pool, density)

            // Anti-identical rule: Avoid 3 identical shapes in the same batch
            val sameCount = selectedTypes.count { it == sampled }
            if (sameCount >= 2) {
                continue
            }

            // Anti-repetition rule for large/hard shapes (avoid repeating 3x3, 1x5 immediately)
            if (ShapePools.HARD.contains(sampled) && recentShapes.count { it == sampled } >= 2) {
                continue
            }

            selectedTypes.add(sampled)
        }

        // Ensure exactly 3
        while (selectedTypes.size < 3) {
            selectedTypes.add(pool.random())
        }

        // Assign distinct vibrant colors if possible
        val shuffledColors = colors.shuffled()
        return selectedTypes.mapIndexed { idx, type ->
            val color = shuffledColors.getOrElse(idx) { colors.random() }
            BlockShape.create(type, color)
        }
    }

    private fun sampleSingleShape(pool: List<ShapeType>, density: Float): ShapeType {
        val easyInPool = pool.filter { ShapePools.EASY.contains(it) }
        val mediumInPool = pool.filter { ShapePools.MEDIUM.contains(it) }
        val hardInPool = pool.filter { ShapePools.HARD.contains(it) }

        // Calculate weights based on Level + Board Density + Failure Streak
        val (easyWeight, mediumWeight, hardWeight) = getTierWeights(density)

        val totalWeight = (if (easyInPool.isNotEmpty()) easyWeight else 0f) +
                (if (mediumInPool.isNotEmpty()) mediumWeight else 0f) +
                (if (hardInPool.isNotEmpty()) hardWeight else 0f)

        val rand = Random.nextFloat() * totalWeight
        var accum = 0f

        if (easyInPool.isNotEmpty()) {
            accum += easyWeight
            if (rand <= accum) return easyInPool.random()
        }

        if (mediumInPool.isNotEmpty()) {
            accum += mediumWeight
            if (rand <= accum) return mediumInPool.random()
        }

        if (hardInPool.isNotEmpty()) {
            return hardInPool.random()
        }

        return pool.random()
    }

    private fun getTierWeights(density: Float): Triple<Float, Float, Float> {
        val level = levelData.levelNumber

        // Base weights by level
        var (easy, med, hard) = when {
            level <= 5 -> Triple(1.0f, 0.0f, 0.0f)
            level <= 10 -> Triple(0.65f, 0.35f, 0.0f)
            level <= 20 -> Triple(0.50f, 0.40f, 0.10f)
            level <= 30 -> Triple(0.35f, 0.45f, 0.20f)
            level <= 40 -> Triple(0.25f, 0.45f, 0.30f)
            else -> Triple(0.20f, 0.40f, 0.40f)
        }

        // Density modulation
        when {
            density > 0.80f -> {
                // High density emergency: boost small flexible shapes
                easy = (easy + 0.50f).coerceAtMost(0.85f)
                med = (med * 0.5f).coerceAtLeast(0.15f)
                hard = 0.0f
            }
            density > 0.60f -> {
                easy = (easy + 0.25f).coerceAtMost(0.70f)
                med = med.coerceAtMost(0.35f)
                hard = (hard * 0.4f)
            }
            density < 0.30f -> {
                // Low density: encourage medium/large shapes for strategic combos
                easy = (easy * 0.7f).coerceAtLeast(0.20f)
                med = (med + 0.15f)
                hard = (hard + 0.15f)
            }
        }

        // Subtle assistance for repeated level retries
        if (failureStreak > 0) {
            val boost = (failureStreak * 0.08f).coerceAtMost(0.25f)
            easy += boost
            hard = (hard - boost).coerceAtLeast(0f)
        }

        return Triple(easy, med, hard)
    }

    private fun calculateBoardDensity(grid: Array<Array<GridCell>>, rows: Int, cols: Int): Float {
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

    private fun hasAnyValidPlacement(
        shape: BlockShape,
        grid: Array<Array<GridCell>>,
        rows: Int,
        cols: Int
    ): Boolean {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (canPlaceOnGrid(shape, r, c, grid, rows, cols)) {
                    return true
                }
            }
        }
        return false
    }

    private fun canPlaceOnGrid(
        shape: BlockShape,
        startR: Int,
        startC: Int,
        grid: Array<Array<GridCell>>,
        rows: Int,
        cols: Int
    ): Boolean {
        for (coord in shape.coordinates) {
            val r = startR + coord.r
            val c = startC + coord.c
            if (r !in 0 until rows || c !in 0 until cols) return false
            if (grid[r][c].type != CellType.EMPTY) return false
        }
        return true
    }

    private fun findPlayableShapesFromPool(
        pool: List<ShapeType>,
        grid: Array<Array<GridCell>>,
        rows: Int,
        cols: Int,
        colors: List<BlockColor>
    ): List<BlockShape> {
        val playable = mutableListOf<BlockShape>()
        for (type in pool) {
            val sample = BlockShape.create(type, colors.random())
            if (hasAnyValidPlacement(sample, grid, rows, cols)) {
                playable.add(sample)
            }
        }
        return playable
    }
}
