package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.BlockShape
import com.example.data.model.WorldTheme
import com.example.ui.theme.ShadowColorSoft
import kotlinx.coroutines.delay

@Composable
fun TrayView(
    trayBlocks: List<BlockShape?>,
    worldTheme: WorldTheme,
    cellSize: Dp,
    batchId: Int = 0,
    slotValidMoves: List<Boolean> = listOf(true, true, true),
    hintTrayIndex: Int?,
    onDragStart: (trayIndex: Int, touchPos: Offset) -> Unit,
    onDragMove: (touchPos: Offset) -> Unit,
    onDragEnd: (touchPos: Offset, boardCenter: Offset) -> Unit,
    onDragCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (index in 0 until 3) {
            val shape = trayBlocks.getOrNull(index)
            val isHint = hintTrayIndex == index
            val isValid = slotValidMoves.getOrElse(index) { true }

            TraySlot(
                index = index,
                shape = shape,
                worldTheme = worldTheme,
                cellSize = cellSize,
                batchId = batchId,
                isPlayable = isValid,
                isHint = isHint,
                onDragStart = onDragStart,
                onDragMove = onDragMove,
                onDragEnd = onDragEnd,
                onDragCancel = onDragCancel
            )
        }
    }
}

@Composable
fun TraySlot(
    index: Int,
    shape: BlockShape?,
    worldTheme: WorldTheme,
    cellSize: Dp,
    batchId: Int,
    isPlayable: Boolean,
    isHint: Boolean,
    onDragStart: (trayIndex: Int, touchPos: Offset) -> Unit,
    onDragMove: (touchPos: Offset) -> Unit,
    onDragEnd: (touchPos: Offset, boardCenter: Offset) -> Unit,
    onDragCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isTouching by remember { mutableStateOf(false) }
    var slotRootPos by remember { mutableStateOf(Offset.Zero) }
    var currentTouchPos by remember { mutableStateOf(Offset.Zero) }

    // Staggered pop-in animation on new batch arrival
    val entryAnim = remember(batchId, shape != null) { Animatable(if (shape != null) 0f else 1f) }

    LaunchedEffect(batchId, shape != null) {
        if (shape != null) {
            entryAnim.snapTo(0f)
            delay(index * 90L) // Stagger 0ms, 90ms, 180ms
            entryAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.65f,
                    stiffness = 380f
                )
            )
        }
    }

    val touchScale by animateFloatAsState(
        targetValue = if (isTouching) 1.08f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "tray_touch_scale"
    )

    val combinedScale = entryAnim.value * touchScale

    Box(
        modifier = modifier
            .size(width = 104.dp, height = 104.dp)
            .shadow(
                elevation = if (shape != null) 6.dp else 1.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = ShadowColorSoft,
                spotColor = if (shape != null && isPlayable) worldTheme.ambientGlow.copy(alpha = 0.3f) else Color.Transparent
            )
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .border(
                width = 1.5.dp,
                color = if (isHint) Color(0xFFFFD600) else Color(0x334A90E2),
                shape = RoundedCornerShape(22.dp)
            )
            .onGloballyPositioned { coordinates ->
                slotRootPos = coordinates.positionInRoot()
            }
            .pointerInput(shape) {
                if (shape == null) return@pointerInput
                detectDragGestures(
                    onDragStart = { localOffset ->
                        isTouching = true
                        val globalTouch = Offset(slotRootPos.x + localOffset.x, slotRootPos.y + localOffset.y)
                        currentTouchPos = globalTouch
                        onDragStart(index, globalTouch)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val globalTouch = Offset(
                            slotRootPos.x + change.position.x,
                            slotRootPos.y + change.position.y
                        )
                        currentTouchPos = globalTouch
                        onDragMove(globalTouch)
                    },
                    onDragEnd = {
                        isTouching = false
                        onDragEnd(currentTouchPos, Offset.Zero)
                    },
                    onDragCancel = {
                        isTouching = false
                        onDragCancel()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (shape != null) {
            val previewCellSize = 22.dp
            Box(
                modifier = Modifier
                    .scale(combinedScale)
                    .alpha(if (isPlayable) 1f else 0.45f),
                contentAlignment = Alignment.Center
            ) {
                ShapePreview3D(
                    shape = shape,
                    cellSize = previewCellSize,
                    isHint = isHint
                )
            }
        } else {
            // Empty cradle slot on crisp white background
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFECEFF1))
            )
        }
    }
}
