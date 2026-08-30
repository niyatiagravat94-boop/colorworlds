package com.example.game

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.GamePreferences
import com.example.data.model.*
import com.example.data.repository.LevelRepository
import com.example.data.repository.RetentionManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PuzzleGameplayTest {

    private lateinit var context: Context
    private lateinit var preferences: GamePreferences
    private lateinit var retentionManager: RetentionManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        preferences = GamePreferences(context)
        preferences.lastLoginDate = ""
        preferences.lastRewardClaimDate = ""
        preferences.loginStreakDays = 1
        preferences.totalLinesClearedEver = 0
        retentionManager = RetentionManager(preferences)
    }

    @Test
    fun testPuzzleEngineBlockPlacementAndLineClears() {
        val level1 = LevelRepository.getLevel(1)
        val engine = PuzzleEngine(level1)

        // Board dimensions
        assertEquals(8, engine.rows)
        assertEquals(8, engine.cols)

        // Tray contains 3 blocks initially
        assertEquals(3, engine.trayBlocks.size)
        val block0 = engine.trayBlocks[0]
        assertNotNull(block0)

        // Can place block at top-left
        val canPlace = engine.canPlace(block0!!, 0, 0)
        assertTrue("Block should be placeable on empty grid", canPlace)

        // Place block
        val result = engine.placeBlock(0, 0, 0)
        assertNotNull("Placement result should not be null", result)
        assertEquals(1, engine.movesUsed)

        // Placed cell should be FILLED
        assertEquals(CellType.FILLED, engine.grid[0][0].type)

        // Tray block 0 should now be consumed
        assertNull(engine.trayBlocks[0])
        assertFalse("Tray should not be empty yet", engine.trayBlocks.all { it == null })
    }

    @Test
    fun test3BlockQueueAndBatchCycle() {
        val level1 = LevelRepository.getLevel(1)
        val engine = PuzzleEngine(level1)

        assertEquals(1, engine.batchId)
        assertEquals(3, engine.trayBlocks.size)
        assertTrue("All 3 slots filled in batch 1", engine.trayBlocks.all { it != null })

        // Consume Slot 0
        val shape0 = engine.trayBlocks[0]!!
        val res0 = engine.placeBlock(0, 0, 0)
        assertNotNull(res0)
        assertFalse("New batch not generated yet", res0!!.newBatchGenerated)
        assertNull("Slot 0 should be consumed", engine.trayBlocks[0])
        assertNotNull("Slot 1 still available", engine.trayBlocks[1])
        assertNotNull("Slot 2 still available", engine.trayBlocks[2])

        // Consume Slot 2 (can consume in any order)
        val shape2 = engine.trayBlocks[2]!!
        val res2 = engine.placeBlock(2, 4, 4)
        assertNotNull(res2)
        assertFalse("New batch not generated yet", res2!!.newBatchGenerated)
        assertNull("Slot 0 is null", engine.trayBlocks[0])
        assertNotNull("Slot 1 still available", engine.trayBlocks[1])
        assertNull("Slot 2 is null", engine.trayBlocks[2])

        // Consume final Slot 1 -> triggers Batch 2!
        val shape1 = engine.trayBlocks[1]!!
        val res1 = engine.placeBlock(1, 0, 4)
        assertNotNull(res1)
        assertTrue("Batch 2 should now be generated", res1!!.newBatchGenerated)
        assertEquals(2, engine.batchId)
        assertEquals(3, engine.trayBlocks.size)
        assertTrue("All 3 slots refilled for batch 2", engine.trayBlocks.all { it != null })
    }

    @Test
    fun testHasAnyValidMoveAndGameOverDetection() {
        val level1 = LevelRepository.getLevel(1)
        val engine = PuzzleEngine(level1)

        // On empty board, hasAnyValidMove is true
        assertTrue(engine.hasAnyValidMove())
        assertFalse(engine.isGameOver())

        // Fill all cells on the board
        for (r in 0 until engine.rows) {
            for (c in 0 until engine.cols) {
                engine.grid[r][c] = GridCell(type = CellType.FILLED, color = BlockColor.CYAN_AQUA)
            }
        }

        // Now board has 0 empty spaces -> no moves possible
        assertFalse("No moves should be valid on completely full grid", engine.hasAnyValidMove())
        assertTrue("Should detect Game Over", engine.isGameOver())
    }

    @Test
    fun testClearingRowSavesPlayerFromGameOver() {
        val level1 = LevelRepository.getLevel(1)
        val engine = PuzzleEngine(level1)

        // Setup row 0 with 7 filled cells out of 8
        for (c in 0 until 7) {
            engine.grid[0][c] = GridCell(type = CellType.FILLED, color = BlockColor.MAGENTA_PINK)
        }

        // Fill remaining board except (0, 7)
        for (r in 1 until engine.rows) {
            for (c in 0 until engine.cols) {
                engine.grid[r][c] = GridCell(type = CellType.FILLED, color = BlockColor.MAGENTA_PINK)
            }
        }

        // Current empty space: only (0, 7)
        assertEquals(CellType.EMPTY, engine.grid[0][7].type)

        // Put a 1x1 DOT into tray slot 0
        engine.trayBlocks[0] = BlockShape.create(ShapeType.DOT, BlockColor.BRIGHT_YELLOW)

        // Place the 1x1 DOT at (0, 7) -> completes row 0 and CLEARS row 0!
        val result = engine.placeBlock(0, 0, 7)
        assertNotNull(result)
        assertTrue("Row 0 should have been cleared", result!!.clearedRows.contains(0))

        // Row 0 is now completely empty (8 open cells)
        for (c in 0 until engine.cols) {
            assertEquals(CellType.EMPTY, engine.grid[0][c].type)
        }

        // Board is NOT game over if any tray block can fit into the freshly cleared row 0
        engine.trayBlocks[1] = BlockShape.create(ShapeType.LINE_3_H, BlockColor.CYAN_AQUA)
        assertTrue("LINE_3_H fits in cleared row 0", engine.hasAnyValidMove())
        assertFalse(engine.isGameOver())
    }

    @Test
    fun testShapeGeneratorPoolProgressionAndDensity() {
        val level1 = LevelRepository.getLevel(1)
        val genLevel1 = ShapeGenerator(level1)
        val emptyGrid = Array(8) { Array(8) { GridCell() } }

        // Level 1 generates valid batch
        val batch1 = genLevel1.generateNextBatch(emptyGrid, 8, 8)
        assertEquals(3, batch1.size)
        // Level 1 shapes belong to Easy pool or predefined
        assertTrue(batch1.all { ShapePools.EASY.contains(it.type) || it.type == ShapeType.LINE_4_H })

        // High density test
        val denseGrid = Array(8) { r ->
            Array(8) { c ->
                if (r < 7) GridCell(type = CellType.FILLED) else GridCell(type = CellType.EMPTY)
            }
        }
        val genDense = ShapeGenerator(LevelRepository.getLevel(30))
        val denseBatch = genDense.generateNextBatch(denseGrid, 8, 8)
        assertEquals(3, denseBatch.size)
    }

    @Test
    fun testRetentionDailyLoginAndStreak() {
        retentionManager.checkAndRecordDailyLogin()

        val today = retentionManager.getTodayDateString()
        assertEquals(today, preferences.lastLoginDate)
        assertEquals(1, preferences.loginStreakDays)
        assertTrue("Daily reward should be claimable on first login", retentionManager.isDailyRewardClaimable())

        val claimedReward = retentionManager.claimDailyReward()
        assertNotNull(claimedReward)
        assertEquals(1, claimedReward?.dayNumber)
        assertFalse("Reward should not be claimable twice in the same day", retentionManager.isDailyRewardClaimable())
    }

    @Test
    fun testRetentionMissionsAndProgress() {
        val initialMissions = retentionManager.getDailyMissions()
        assertEquals(3, initialMissions.size)

        // Clear 6 lines
        retentionManager.onGameEventProgress(linesCleared = 6, scoreEarned = 1600, comboAchieved = 2)

        val updatedMissions = retentionManager.getDailyMissions()
        val lineMission = updatedMissions.first { it.id == "mission_lines" }
        assertTrue("Line mission should be completed", lineMission.isCompleted)

        val scoreMission = updatedMissions.first { it.id == "mission_score" }
        assertTrue("Score mission should be completed", scoreMission.isCompleted)

        // Claim mission
        val claimed = retentionManager.claimDailyMission("mission_lines")
        assertNotNull(claimed)
        assertTrue(preferences.isDailyMissionClaimed("mission_lines", retentionManager.getTodayDateString()))
    }
}
