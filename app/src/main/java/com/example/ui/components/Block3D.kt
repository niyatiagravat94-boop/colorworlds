package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.BlockColor
import com.example.data.model.BlockShape
import com.example.data.model.CellType
import com.example.data.model.Coordinate
import com.example.data.model.GridCell
import com.example.data.model.WorldTheme

/**
 * Premium 3D Beveled Cell with Specular Highlights, Top-Glow and Crisp Bevel Edges.
 */
@Composable
fun BlockCell3D(
    cell: GridCell,
    cellSize: Dp,
    worldTheme: WorldTheme,
    modifier: Modifier = Modifier,
    isGhostPreview: Boolean = false,
    ghostColor: BlockColor? = null,
    isHintHighlight: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hint_pulse")
    val hintScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hint_scale"
    )

    Canvas(
        modifier = modifier.size(cellSize)
    ) {
        val w = size.width
        val h = size.height

        val isOccupied = cell.type != CellType.EMPTY

        if (!isOccupied && !isGhostPreview) {
            // Draw Crisp Light Empty Grid Cell with Inset Shadow Depth
            drawEmptyCellLight(w, h, worldTheme)
        } else {
            // Draw Glossy Raised 3D Block (Solid or Ghost)
            val effectiveColor = if (isGhostPreview) {
                (ghostColor?.primaryColor ?: worldTheme.accentColor).copy(alpha = 0.55f)
            } else {
                cell.color?.primaryColor ?: worldTheme.accentColor
            }

            draw3DBlockBevel(
                w = w,
                h = h,
                baseColor = effectiveColor,
                isGhost = isGhostPreview,
                isHint = isHintHighlight,
                scaleFactor = if (isHintHighlight) hintScale else 1f
            )
        }
    }
}

/**
 * Draws a clean, modern recessed cell tile in the grid on a light background.
 */
private fun DrawScope.drawEmptyCellLight(w: Float, h: Float, theme: WorldTheme) {
    val corner = w * 0.22f
    val pad = 1.5f

    // 1. Recessed Floor
    drawRoundRect(
        color = theme.gridEmptyCellColor,
        topLeft = Offset(pad, pad),
        size = Size(w - pad * 2, h - pad * 2),
        cornerRadius = CornerRadius(corner, corner)
    )

    // 2. Subtle Inset Inner Border
    drawRoundRect(
        color = theme.gridEmptyCellBorder,
        topLeft = Offset(pad, pad),
        size = Size(w - pad * 2, h - pad * 2),
        cornerRadius = CornerRadius(corner, corner),
        style = Stroke(width = 1.2f)
    )
}

/**
 * Renders a tactile, candy-gloss 3D block with top specular sheen and bottom depth bevel.
 */
private fun DrawScope.draw3DBlockBevel(
    w: Float,
    h: Float,
    baseColor: Color,
    isGhost: Boolean,
    isHint: Boolean,
    scaleFactor: Float = 1f
) {
    val corner = w * 0.24f
    val inset = 1.5f

    // Bottom depth shadow (Cast down)
    if (!isGhost) {
        drawRoundRect(
            color = Color(0x33000000),
            topLeft = Offset(inset, inset + 3f),
            size = Size(w - inset * 2, h - inset * 2),
            cornerRadius = CornerRadius(corner, corner)
        )
    }

    // 1. Base Dark Bevel (Bottom/Right shading)
    val darkShade = Color(
        red = (baseColor.red * 0.72f).coerceIn(0f, 1f),
        green = (baseColor.green * 0.72f).coerceIn(0f, 1f),
        blue = (baseColor.blue * 0.72f).coerceIn(0f, 1f),
        alpha = baseColor.alpha
    )

    drawRoundRect(
        color = darkShade,
        topLeft = Offset(inset, inset),
        size = Size(w - inset * 2, h - inset * 2),
        cornerRadius = CornerRadius(corner, corner)
    )

    // 2. Raised Front Cap (Offset 2.5px up to create 3D height)
    val raisedHeight = if (isGhost) 1.5f else 3f
    val lightShade = Color(
        red = (baseColor.red * 1.12f).coerceIn(0f, 1f),
        green = (baseColor.green * 1.12f).coerceIn(0f, 1f),
        blue = (baseColor.blue * 1.12f).coerceIn(0f, 1f),
        alpha = baseColor.alpha
    )

    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(lightShade, baseColor),
            startY = inset,
            endY = h - inset - raisedHeight
        ),
        topLeft = Offset(inset, inset),
        size = Size(w - inset * 2, h - inset * 2 - raisedHeight),
        cornerRadius = CornerRadius(corner, corner)
    )

    // 3. Top-Left Gloss Specular Highlight Sheen
    if (!isGhost) {
        val highlightPath = Path().apply {
            moveTo(inset + corner * 0.5f, inset + 2f)
            lineTo(w - inset - corner * 0.5f, inset + 2f)
            cubicTo(
                w - inset - corner * 0.2f, inset + 2f,
                w - inset - 2f, inset + corner * 0.2f,
                w - inset - 2f, inset + corner * 0.5f
            )
            lineTo(inset + corner * 0.5f, inset + (h * 0.35f))
            close()
        }
        drawPath(
            path = highlightPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0x99FFFFFF), Color(0x11FFFFFF)),
                startY = inset,
                endY = inset + h * 0.35f
            )
        )

        // Corner Pin-Glow (Top-Left point light)
        drawCircle(
            color = Color(0xCCFFFFFF),
            center = Offset(inset + corner * 0.7f, inset + corner * 0.7f),
            radius = w * 0.08f
        )
    }

    // 4. Subtle Clean Border Outline
    drawRoundRect(
        color = if (isHint) Color(0xFFFFD600) else Color(0x44FFFFFF),
        topLeft = Offset(inset, inset),
        size = Size(w - inset * 2, h - inset * 2),
        cornerRadius = CornerRadius(corner, corner),
        style = Stroke(width = if (isHint) 2.5f else 1f)
    )
}

/**
 * Preview Composable for Shapes inside the Tray or Dialogs
 */
@Composable
fun ShapePreview3D(
    shape: BlockShape,
    cellSize: Dp = 22.dp,
    isHint: Boolean = false,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.size(
            width = cellSize * shape.cols,
            height = cellSize * shape.rows
        )
    ) {
        val cellPx = cellSize.toPx()
        val coordsSet = shape.coordinates.toSet()

        for (r in 0 until shape.rows) {
            for (c in 0 until shape.cols) {
                if (Coordinate(r, c) in coordsSet) {
                    val ox = c * cellPx
                    val oy = r * cellPx
                    val cellW = cellPx
                    val cellH = cellPx
                    val corner = cellW * 0.24f
                    val inset = 1.5f

                    // Shadow
                    drawRoundRect(
                        color = Color(0x22000000),
                        topLeft = Offset(ox + inset, oy + inset + 2.5f),
                        size = Size(cellW - inset * 2, cellH - inset * 2),
                        cornerRadius = CornerRadius(corner, corner)
                    )

                    // 3D Dark Bevel Base
                    val baseCol = shape.color.primaryColor
                    val darkShade = Color(
                        red = (baseCol.red * 0.75f).coerceIn(0f, 1f),
                        green = (baseCol.green * 0.75f).coerceIn(0f, 1f),
                        blue = (baseCol.blue * 0.75f).coerceIn(0f, 1f),
                        alpha = 1f
                    )
                    drawRoundRect(
                        color = darkShade,
                        topLeft = Offset(ox + inset, oy + inset),
                        size = Size(cellW - inset * 2, cellH - inset * 2),
                        cornerRadius = CornerRadius(corner, corner)
                    )

                    // Raised Front Face
                    val lightShade = Color(
                        red = (baseCol.red * 1.15f).coerceIn(0f, 1f),
                        green = (baseCol.green * 1.15f).coerceIn(0f, 1f),
                        blue = (baseCol.blue * 1.15f).coerceIn(0f, 1f),
                        alpha = 1f
                    )
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(lightShade, baseCol),
                            startY = oy + inset,
                            endY = oy + cellH - inset - 2.5f
                        ),
                        topLeft = Offset(ox + inset, oy + inset),
                        size = Size(cellW - inset * 2, cellH - inset * 2 - 2.5f),
                        cornerRadius = CornerRadius(corner, corner)
                    )

                    // Gloss sheen
                    drawCircle(
                        color = Color(0xCCFFFFFF),
                        center = Offset(ox + inset + corner * 0.7f, oy + inset + corner * 0.7f),
                        radius = cellW * 0.08f
                    )

                    // Hint Border if active
                    if (isHint) {
                        drawRoundRect(
                            color = Color(0xFFFFD600),
                            topLeft = Offset(ox + inset, oy + inset),
                            size = Size(cellW - inset * 2, cellH - inset * 2),
                            cornerRadius = CornerRadius(corner, corner),
                            style = Stroke(width = 2.5f)
                        )
                    }
                }
            }
        }
    }
}
