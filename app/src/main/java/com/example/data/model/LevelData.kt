package com.example.data.model

sealed class LevelObjective {
    data class ClearLines(val targetLines: Int) : LevelObjective()
    data class ReachScore(val targetScore: Int) : LevelObjective()
    data class CollectColor(val color: BlockColor, val targetCount: Int) : LevelObjective()
    data class ClearObstacles(val targetCount: Int) : LevelObjective()
    data class LimitedMoves(val maxMoves: Int, val targetScore: Int) : LevelObjective()
    data class PerformCombos(val targetCombos: Int) : LevelObjective()

    fun getDisplayName(): String {
        return when (this) {
            is ClearLines -> "Clear $targetLines Lines"
            is ReachScore -> "Score $targetScore pts"
            is CollectColor -> "Collect $targetCount ${color.displayName} blocks"
            is ClearObstacles -> "Shatter $targetCount Crystal Gems"
            is LimitedMoves -> "Score $targetScore in $maxMoves moves"
            is PerformCombos -> "Execute $targetCombos Combos"
        }
    }
}

enum class CellType {
    EMPTY,
    FILLED,
    OBSTACLE_CRYSTAL, // Shatters when adjacent row/col is cleared
    PRESET_BLOCK
}

data class GridCell(
    val type: CellType = CellType.EMPTY,
    val color: BlockColor? = null,
    val obstacleHealth: Int = 1
)

data class InitialBoardSetup(
    val initialCells: Map<Coordinate, GridCell> = emptyMap()
)

data class LevelData(
    val levelNumber: Int,
    val worldId: WorldId,
    val title: String,
    val gridRows: Int = 8,
    val gridCols: Int = 8,
    val objective: LevelObjective,
    val targetScore: Int,
    val oneStarScore: Int,
    val twoStarScore: Int,
    val threeStarScore: Int,
    val allowedShapePool: List<ShapeType>,
    val allowedColorPool: List<BlockColor>,
    val initialSetup: InitialBoardSetup = InitialBoardSetup(),
    val predefinedBatches: List<List<ShapeType>>? = null,
    val maxMoves: Int? = null,
    val tutorialHint: String? = null
)
