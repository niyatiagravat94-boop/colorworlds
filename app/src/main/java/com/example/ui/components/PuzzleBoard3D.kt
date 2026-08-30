package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.BlockColor
import com.example.data.model.CellType
import com.example.data.model.Coordinate
import com.example.data.model.GridCell
import com.example.data.model.WorldTheme
import com.example.ui.theme.ShadowColorSoft

@Composable
fun PuzzleBoard3D(
    grid: List<List<GridCell>>,
    worldTheme: WorldTheme,
    cellSize: Dp,
    previewPlacement: List<Coordinate>? = null,
    previewColor: BlockColor? = null,
    isValidPreview: Boolean = false,
    activeHintCoord: Coordinate? = null,
    onBoardPositioned: (topLeft: Offset, cellSizePx: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = grid.size
    val cols = if (grid.isNotEmpty()) grid[0].size else 0
    val boardPadding = 12.dp
    val gridSpacing = 4.dp
    val boardWidth = (cellSize * cols) + (gridSpacing * (cols - 1)) + (boardPadding * 2)

    val previewCoordsSet = previewPlacement?.map { it.r to it.c }?.toSet() ?: emptySet()

    Box(
        modifier = modifier
            .width(boardWidth)
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = ShadowColorSoft,
                spotColor = worldTheme.boardBorderGlow.copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(worldTheme.boardSurfaceColor) // Pristine Crisp White
            .border(
                width = 2.dp,
                color = worldTheme.boardBorderGlow,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(boardPadding)
            .onGloballyPositioned { coordinates ->
                val rootPos = coordinates.positionInRoot()
                val cellPx = coordinates.size.width.toFloat() / cols.coerceAtLeast(1)
                onBoardPositioned(rootPos, cellPx)
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(gridSpacing)
        ) {
            for (r in 0 until rows) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(gridSpacing)
                ) {
                    for (c in 0 until cols) {
                        val gridCell = grid[r][c]
                        val isPreview = (r to c) in previewCoordsSet
                        val isHint = activeHintCoord?.let { it.r == r && it.c == c } ?: false

                        BlockCell3D(
                            cell = gridCell,
                            cellSize = cellSize,
                            worldTheme = worldTheme,
                            isGhostPreview = isPreview && gridCell.type == CellType.EMPTY,
                            ghostColor = if (isPreview) previewColor else null,
                            isHintHighlight = isHint
                        )
                    }
                }
            }
        }
    }
}
