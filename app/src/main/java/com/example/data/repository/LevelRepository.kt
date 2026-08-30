package com.example.data.repository

import com.example.data.model.*

object LevelRepository {

    private val allLevels: List<LevelData> = buildList {
        // WORLD 1: COLOR GARDEN (Levels 1–10)
        // Easy introductory levels, teaching dragging, row/col clears, basic colors.
        val gardenColors = listOf(
            BlockColor.EMERALD_GREEN,
            BlockColor.LIME_GREEN,
            BlockColor.BRIGHT_YELLOW,
            BlockColor.CYAN_AQUA,
            BlockColor.MAGENTA_PINK
        )

        // Level 1 - Drag & Drop Tutorial
        add(
            LevelData(
                levelNumber = 1,
                worldId = WorldId.COLOR_GARDEN,
                title = "First Bloom",
                gridRows = 8,
                gridCols = 8,
                objective = LevelObjective.ClearLines(targetLines = 1),
                targetScore = 300,
                oneStarScore = 150,
                twoStarScore = 300,
                threeStarScore = 450,
                allowedShapePool = listOf(ShapeType.LINE_4_H, ShapeType.LINE_3_H, ShapeType.SQUARE_2X2, ShapeType.DOT),
                allowedColorPool = gardenColors,
                predefinedBatches = listOf(
                    listOf(ShapeType.LINE_4_H, ShapeType.LINE_4_H, ShapeType.SQUARE_2X2),
                    listOf(ShapeType.LINE_3_H, ShapeType.LINE_3_V, ShapeType.DOT)
                ),
                tutorialHint = "Drag shapes from the tray onto the board to fill and clear rows or columns!"
            )
        )

        // Level 2 - Complete Rows
        add(
            LevelData(
                levelNumber = 2,
                worldId = WorldId.COLOR_GARDEN,
                title = "Garden Pathway",
                gridRows = 8,
                gridCols = 8,
                objective = LevelObjective.ClearLines(targetLines = 2),
                targetScore = 600,
                oneStarScore = 300,
                twoStarScore = 600,
                threeStarScore = 850,
                allowedShapePool = listOf(ShapeType.LINE_3_H, ShapeType.LINE_3_V, ShapeType.LINE_2_H, ShapeType.SQUARE_2X2),
                allowedColorPool = gardenColors,
                predefinedBatches = listOf(
                    listOf(ShapeType.LINE_3_H, ShapeType.LINE_3_V, ShapeType.SQUARE_2X2),
                    listOf(ShapeType.LINE_2_H, ShapeType.LINE_2_V, ShapeType.LINE_3_H)
                ),
                tutorialHint = "Clear full horizontal rows or vertical columns to score points."
            )
        )

        // Level 3 - Target Score
        add(
            LevelData(
                levelNumber = 3,
                worldId = WorldId.COLOR_GARDEN,
                title = "Sunshine Meadow",
                gridRows = 8,
                gridCols = 8,
                objective = LevelObjective.ReachScore(targetScore = 1000),
                targetScore = 1000,
                oneStarScore = 1000,
                twoStarScore = 1350,
                threeStarScore = 1750,
                allowedShapePool = listOf(ShapeType.LINE_3_H, ShapeType.LINE_3_V, ShapeType.L_SMALL_TL, ShapeType.L_SMALL_BR, ShapeType.SQUARE_2X2),
                allowedColorPool = gardenColors,
                predefinedBatches = listOf(
                    listOf(ShapeType.LINE_3_H, ShapeType.L_SMALL_TL, ShapeType.SQUARE_2X2),
                    listOf(ShapeType.LINE_3_V, ShapeType.L_SMALL_BR, ShapeType.LINE_2_H)
                )
            )
        )

        // Level 4 - Introducing L-shapes
        add(
            LevelData(
                levelNumber = 4,
                worldId = WorldId.COLOR_GARDEN,
                title = "Daisy Patch",
                gridRows = 8,
                gridCols = 8,
                objective = LevelObjective.ClearLines(targetLines = 3),
                targetScore = 1200,
                oneStarScore = 900,
                twoStarScore = 1300,
                threeStarScore = 1800,
                allowedShapePool = listOf(ShapeType.L_SMALL_TL, ShapeType.L_SMALL_TR, ShapeType.L_SMALL_BL, ShapeType.L_SMALL_BR, ShapeType.LINE_2_H, ShapeType.LINE_2_V),
                allowedColorPool = gardenColors,
                predefinedBatches = listOf(
                    listOf(ShapeType.L_SMALL_TL, ShapeType.L_SMALL_TR, ShapeType.LINE_2_H),
                    listOf(ShapeType.L_SMALL_BL, ShapeType.L_SMALL_BR, ShapeType.LINE_2_V)
                )
            )
        )

        // Level 5 - First Milestone Challenge
        add(
            LevelData(
                levelNumber = 5,
                worldId = WorldId.COLOR_GARDEN,
                title = "Butterfly Grove",
                gridRows = 8,
                gridCols = 8,
                objective = LevelObjective.PerformCombos(targetCombos = 1),
                targetScore = 1500,
                oneStarScore = 1000,
                twoStarScore = 1500,
                threeStarScore = 2200,
                allowedShapePool = listOf(ShapeType.SQUARE_2X2, ShapeType.LINE_3_H, ShapeType.LINE_3_V, ShapeType.T_DOWN, ShapeType.T_UP),
                allowedColorPool = gardenColors,
                predefinedBatches = listOf(
                    listOf(ShapeType.SQUARE_2X2, ShapeType.LINE_3_H, ShapeType.T_DOWN),
                    listOf(ShapeType.LINE_3_V, ShapeType.T_UP, ShapeType.SQUARE_2X2)
                ),
                tutorialHint = "Clear multiple lines at the same time or in consecutive turns for huge Combo multipliers!"
            )
        )

        // Levels 6 to 10 (Garden Continuation)
        for (i in 6..10) {
            val lines = 3 + (i - 6)
            val score = 1500 + (i - 5) * 400
            add(
                LevelData(
                    levelNumber = i,
                    worldId = WorldId.COLOR_GARDEN,
                    title = listOf("Rose Petal Walk", "Honeybee Hollow", "Emerald Canopy", "Blossom Arch", "Garden Finale")[i - 6],
                    gridRows = 8,
                    gridCols = 8,
                    objective = when (i % 3) {
                        0 -> LevelObjective.CollectColor(BlockColor.EMERALD_GREEN, 12 + (i - 6) * 3)
                        1 -> LevelObjective.ClearLines(lines)
                        else -> LevelObjective.ReachScore(score)
                    },
                    targetScore = score,
                    oneStarScore = score - 300,
                    twoStarScore = score + 200,
                    threeStarScore = score + 800,
                    allowedShapePool = listOf(
                        ShapeType.LINE_3_H, ShapeType.LINE_3_V, ShapeType.LINE_4_H,
                        ShapeType.SQUARE_2X2, ShapeType.L_SMALL_TR, ShapeType.L_SMALL_BL,
                        ShapeType.T_DOWN, ShapeType.CORNER_3_TL, ShapeType.Z_H
                    ),
                    allowedColorPool = gardenColors,
                    initialSetup = if (i == 10) InitialBoardSetup(
                        mapOf(
                            Coordinate(3, 3) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.BRIGHT_YELLOW),
                            Coordinate(4, 4) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.BRIGHT_YELLOW)
                        )
                    ) else InitialBoardSetup()
                )
            )
        }

        // WORLD 2: OCEAN WORLD (Levels 11–20)
        // Deep ocean, aquatic hues, tighter challenges, coral obstacles, combo objectives
        val oceanColors = listOf(
            BlockColor.ELECTRIC_BLUE,
            BlockColor.CYAN_AQUA,
            BlockColor.EMERALD_GREEN,
            BlockColor.ROYAL_PURPLE,
            BlockColor.CORAL_ORANGE
        )

        for (i in 11..20) {
            val idx = i - 11
            val score = 2500 + idx * 450
            val initialObstacles = when {
                i == 11 -> mapOf(
                    Coordinate(3, 3) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.CYAN_AQUA),
                    Coordinate(3, 4) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.CYAN_AQUA)
                )
                i == 15 -> mapOf(
                    Coordinate(2, 2) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.CORAL_ORANGE),
                    Coordinate(2, 5) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.CORAL_ORANGE),
                    Coordinate(5, 2) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.CORAL_ORANGE),
                    Coordinate(5, 5) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.CORAL_ORANGE)
                )
                i == 20 -> mapOf(
                    Coordinate(1, 1) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.ROYAL_PURPLE),
                    Coordinate(1, 6) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.ROYAL_PURPLE),
                    Coordinate(6, 1) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.ROYAL_PURPLE),
                    Coordinate(6, 6) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.ROYAL_PURPLE),
                    Coordinate(3, 3) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.CYAN_AQUA),
                    Coordinate(4, 4) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.CYAN_AQUA)
                )
                else -> emptyMap()
            }

            add(
                LevelData(
                    levelNumber = i,
                    worldId = WorldId.OCEAN_WORLD,
                    title = listOf(
                        "Coral Lagoon", "Tidal Surge", "Sunken Atoll", "Bioluminescent Bay",
                        "Pearl Sanctuary", "Deep Trench", "Whale Song", "Abyssal Shelf",
                        "Manta Reef", "Ocean Master"
                    )[idx],
                    gridRows = 8,
                    gridCols = 8,
                    objective = when (idx % 4) {
                        0 -> LevelObjective.ClearObstacles(initialObstacles.size.coerceAtLeast(2))
                        1 -> LevelObjective.CollectColor(BlockColor.CYAN_AQUA, 16 + idx * 2)
                        2 -> LevelObjective.ClearLines(5 + idx)
                        else -> LevelObjective.PerformCombos(2 + idx / 3)
                    },
                    targetScore = score,
                    oneStarScore = score - 400,
                    twoStarScore = score + 300,
                    threeStarScore = score + 1000,
                    allowedShapePool = listOf(
                        ShapeType.LINE_3_H, ShapeType.LINE_3_V, ShapeType.LINE_4_H, ShapeType.LINE_4_V,
                        ShapeType.SQUARE_2X2, ShapeType.L_LARGE_TL, ShapeType.L_LARGE_BR,
                        ShapeType.T_DOWN, ShapeType.T_UP, ShapeType.T_LEFT, ShapeType.T_RIGHT,
                        ShapeType.Z_H, ShapeType.S_H, ShapeType.CORNER_3_TR
                    ),
                    allowedColorPool = oceanColors,
                    initialSetup = InitialBoardSetup(initialObstacles),
                    maxMoves = if (idx % 3 == 0) 25 + idx else null
                )
            )
        }

        // WORLD 3: MOUNTAIN WORLD (Levels 21–30)
        // Snow peaks, frost crystals, complex corner shapes, strategic planning
        val mountainColors = listOf(
            BlockColor.ELECTRIC_BLUE,
            BlockColor.GOLDEN_AMBER,
            BlockColor.BRIGHT_YELLOW,
            BlockColor.VIBRANT_RED,
            BlockColor.CYAN_AQUA
        )

        for (i in 21..30) {
            val idx = i - 21
            val score = 4000 + idx * 500
            val obstacles = when {
                i == 21 -> mapOf(
                    Coordinate(0, 0) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.CYAN_AQUA),
                    Coordinate(7, 7) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.CYAN_AQUA),
                    Coordinate(0, 7) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.CYAN_AQUA),
                    Coordinate(7, 0) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.CYAN_AQUA)
                )
                i == 25 -> mapOf(
                    Coordinate(3, 2) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.GOLDEN_AMBER),
                    Coordinate(3, 5) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.GOLDEN_AMBER),
                    Coordinate(4, 2) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.GOLDEN_AMBER),
                    Coordinate(4, 5) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.GOLDEN_AMBER)
                )
                i == 30 -> mapOf(
                    Coordinate(2, 3) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.VIBRANT_RED),
                    Coordinate(2, 4) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.VIBRANT_RED),
                    Coordinate(5, 3) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.VIBRANT_RED),
                    Coordinate(5, 4) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.VIBRANT_RED),
                    Coordinate(3, 2) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.CYAN_AQUA),
                    Coordinate(4, 5) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.CYAN_AQUA)
                )
                else -> emptyMap()
            }

            add(
                LevelData(
                    levelNumber = i,
                    worldId = WorldId.MOUNTAIN_WORLD,
                    title = listOf(
                        "Alpine Ridge", "Glacier Gorge", "Frost Citadel", "Eagle Crest",
                        "Avalanche Pass", "Crystal Cavern", "Mist Summit", "Summit Aurora",
                        "Thunder Peak", "Apex of the World"
                    )[idx],
                    gridRows = 8,
                    gridCols = 8,
                    objective = when (idx % 4) {
                        0 -> LevelObjective.ReachScore(score)
                        1 -> LevelObjective.ClearLines(6 + idx)
                        2 -> LevelObjective.CollectColor(BlockColor.GOLDEN_AMBER, 20 + idx * 2)
                        else -> LevelObjective.LimitedMoves(maxMoves = 22 + idx, targetScore = score - 500)
                    },
                    targetScore = score,
                    oneStarScore = score - 500,
                    twoStarScore = score + 400,
                    threeStarScore = score + 1200,
                    allowedShapePool = listOf(
                        ShapeType.LINE_2_H, ShapeType.LINE_2_V, ShapeType.LINE_3_H, ShapeType.LINE_3_V,
                        ShapeType.LINE_4_H, ShapeType.LINE_4_V, ShapeType.LINE_5_H,
                        ShapeType.SQUARE_2X2, ShapeType.SQUARE_3X3,
                        ShapeType.L_LARGE_TL, ShapeType.L_LARGE_TR, ShapeType.L_LARGE_BL, ShapeType.L_LARGE_BR,
                        ShapeType.T_UP, ShapeType.T_DOWN, ShapeType.Z_H, ShapeType.Z_V, ShapeType.PLUS
                    ),
                    allowedColorPool = mountainColors,
                    initialSetup = InitialBoardSetup(obstacles)
                )
            )
        }

        // WORLD 4: SPACE WORLD (Levels 31–40)
        // Cosmic nebulae, neon glowing platforms, complex multi-step combos, tight spaces
        val spaceColors = listOf(
            BlockColor.ROYAL_PURPLE,
            BlockColor.MAGENTA_PINK,
            BlockColor.CYAN_AQUA,
            BlockColor.BRIGHT_YELLOW,
            BlockColor.CORAL_ORANGE
        )

        for (i in 31..40) {
            val idx = i - 31
            val score = 6000 + idx * 600
            val obstacles = when {
                i == 31 -> mapOf(
                    Coordinate(2, 2) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.ROYAL_PURPLE),
                    Coordinate(2, 5) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.ROYAL_PURPLE),
                    Coordinate(5, 2) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.ROYAL_PURPLE),
                    Coordinate(5, 5) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.ROYAL_PURPLE)
                )
                i == 35 -> mapOf(
                    Coordinate(3, 3) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.MAGENTA_PINK),
                    Coordinate(3, 4) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.MAGENTA_PINK),
                    Coordinate(4, 3) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.MAGENTA_PINK),
                    Coordinate(4, 4) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.MAGENTA_PINK)
                )
                i == 40 -> mapOf(
                    Coordinate(1, 3) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.CYAN_AQUA),
                    Coordinate(1, 4) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.CYAN_AQUA),
                    Coordinate(6, 3) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.CYAN_AQUA),
                    Coordinate(6, 4) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.CYAN_AQUA),
                    Coordinate(3, 1) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.MAGENTA_PINK),
                    Coordinate(4, 1) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.MAGENTA_PINK),
                    Coordinate(3, 6) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.MAGENTA_PINK),
                    Coordinate(4, 6) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.MAGENTA_PINK)
                )
                else -> emptyMap()
            }

            add(
                LevelData(
                    levelNumber = i,
                    worldId = WorldId.SPACE_WORLD,
                    title = listOf(
                        "Starlight Belt", "Nebula Core", "Supernova Gate", "Pulsar Beacon",
                        "Orbital Station", "Gravity Well", "Comet Trail", "Solar Flare",
                        "Event Horizon", "Cosmic Voyager"
                    )[idx],
                    gridRows = 8,
                    gridCols = 8,
                    objective = when (idx % 4) {
                        0 -> LevelObjective.ClearLines(8 + idx)
                        1 -> LevelObjective.CollectColor(BlockColor.MAGENTA_PINK, 22 + idx * 2)
                        2 -> LevelObjective.PerformCombos(3 + idx / 3)
                        else -> LevelObjective.LimitedMoves(maxMoves = 24 + idx, targetScore = score)
                    },
                    targetScore = score,
                    oneStarScore = score - 600,
                    twoStarScore = score + 500,
                    threeStarScore = score + 1500,
                    allowedShapePool = listOf(
                        ShapeType.LINE_3_H, ShapeType.LINE_3_V, ShapeType.LINE_4_H, ShapeType.LINE_4_V,
                        ShapeType.SQUARE_2X2, ShapeType.SQUARE_3X3,
                        ShapeType.L_LARGE_TL, ShapeType.L_LARGE_TR, ShapeType.L_LARGE_BL, ShapeType.L_LARGE_BR,
                        ShapeType.T_UP, ShapeType.T_DOWN, ShapeType.T_LEFT, ShapeType.T_RIGHT,
                        ShapeType.Z_H, ShapeType.Z_V, ShapeType.S_H, ShapeType.S_V,
                        ShapeType.PLUS, ShapeType.U_SHAPE
                    ),
                    allowedColorPool = spaceColors,
                    initialSetup = InitialBoardSetup(obstacles)
                )
            )
        }

        // WORLD 5: CRYSTAL WORLD (Levels 41–50)
        // Grandmaster levels, prismatic colors, supreme satisfaction, ultimate combos
        val crystalColors = listOf(
            BlockColor.MAGENTA_PINK,
            BlockColor.ROYAL_PURPLE,
            BlockColor.CYAN_AQUA,
            BlockColor.BRIGHT_YELLOW,
            BlockColor.EMERALD_GREEN,
            BlockColor.CORAL_ORANGE
        )

        for (i in 41..50) {
            val idx = i - 41
            val score = 8500 + idx * 800
            val obstacles = when {
                i == 41 -> mapOf(
                    Coordinate(2, 2) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.MAGENTA_PINK),
                    Coordinate(2, 5) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.CYAN_AQUA),
                    Coordinate(5, 2) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.CYAN_AQUA),
                    Coordinate(5, 5) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.MAGENTA_PINK)
                )
                i == 45 -> mapOf(
                    Coordinate(0, 3) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.BRIGHT_YELLOW),
                    Coordinate(0, 4) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.BRIGHT_YELLOW),
                    Coordinate(7, 3) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.BRIGHT_YELLOW),
                    Coordinate(7, 4) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.BRIGHT_YELLOW),
                    Coordinate(3, 0) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.MAGENTA_PINK),
                    Coordinate(4, 0) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.MAGENTA_PINK),
                    Coordinate(3, 7) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.MAGENTA_PINK),
                    Coordinate(4, 7) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.MAGENTA_PINK)
                )
                i == 50 -> mapOf(
                    Coordinate(2, 2) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.MAGENTA_PINK),
                    Coordinate(2, 5) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.CYAN_AQUA),
                    Coordinate(5, 2) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.CYAN_AQUA),
                    Coordinate(5, 5) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.MAGENTA_PINK),
                    Coordinate(3, 3) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.ROYAL_PURPLE),
                    Coordinate(3, 4) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.ROYAL_PURPLE),
                    Coordinate(4, 3) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.ROYAL_PURPLE),
                    Coordinate(4, 4) to GridCell(CellType.OBSTACLE_CRYSTAL, BlockColor.ROYAL_PURPLE)
                )
                else -> emptyMap()
            }

            add(
                LevelData(
                    levelNumber = i,
                    worldId = WorldId.CRYSTAL_WORLD,
                    title = listOf(
                        "Prism Threshold", "Amethyst Spire", "Diamond Fracture", "Aurora Cascade",
                        "Lapis Scepter", "Emerald Geode", "Obsidian Heart", "Iridescent Void",
                        "Chrono Crystal", "Grand Masterpiece"
                    )[idx],
                    gridRows = 8,
                    gridCols = 8,
                    objective = when (idx % 4) {
                        0 -> LevelObjective.ClearObstacles(obstacles.size.coerceAtLeast(4))
                        1 -> LevelObjective.CollectColor(BlockColor.ROYAL_PURPLE, 25 + idx * 2)
                        2 -> LevelObjective.PerformCombos(4 + idx / 2)
                        else -> LevelObjective.ReachScore(score)
                    },
                    targetScore = score,
                    oneStarScore = score - 800,
                    twoStarScore = score + 600,
                    threeStarScore = score + 2000,
                    allowedShapePool = listOf(
                        ShapeType.LINE_3_H, ShapeType.LINE_3_V, ShapeType.LINE_4_H, ShapeType.LINE_4_V, ShapeType.LINE_5_H,
                        ShapeType.SQUARE_2X2, ShapeType.SQUARE_3X3,
                        ShapeType.L_LARGE_TL, ShapeType.L_LARGE_TR, ShapeType.L_LARGE_BL, ShapeType.L_LARGE_BR,
                        ShapeType.T_UP, ShapeType.T_DOWN, ShapeType.T_LEFT, ShapeType.T_RIGHT,
                        ShapeType.Z_H, ShapeType.Z_V, ShapeType.S_H, ShapeType.S_V,
                        ShapeType.PLUS, ShapeType.U_SHAPE
                    ),
                    allowedColorPool = crystalColors,
                    initialSetup = InitialBoardSetup(obstacles),
                    maxMoves = if (idx == 9) 35 else null
                )
            )
        }
    }

    fun getLevel(levelNumber: Int): LevelData {
        val clamped = levelNumber.coerceIn(1, allLevels.size)
        return allLevels[clamped - 1]
    }

    fun getAllLevels(): List<LevelData> = allLevels

    fun getLevelsForWorld(worldId: WorldId): List<LevelData> {
        return allLevels.filter { it.worldId == worldId }
    }

    fun getTotalLevelCount(): Int = allLevels.size
}
