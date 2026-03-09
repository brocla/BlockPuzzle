package com.blockpuzzle.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.blockpuzzle.logic.GameEngine
import com.blockpuzzle.model.Shape
import kotlinx.coroutines.delay
import com.blockpuzzle.ui.components.DraggableShapePreview
import com.blockpuzzle.ui.components.GameGrid
import com.blockpuzzle.ui.components.HoldBox
import com.blockpuzzle.ui.components.ScoreBar
import com.blockpuzzle.ui.components.ScorePopOverlay
import com.blockpuzzle.ui.components.ShapePreview
import com.blockpuzzle.ui.theme.toComposeColor
import com.blockpuzzle.viewmodel.DragSource
import com.blockpuzzle.viewmodel.DragState
import com.blockpuzzle.viewmodel.GameViewModel
import com.blockpuzzle.viewmodel.HapticEvent

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()
    val dragState by viewModel.dragState.collectAsState()
    val scorePops by viewModel.scorePops.collectAsState()

    // Haptic feedback
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(Unit) {
        viewModel.hapticEvents.collect { event ->
            when (event) {
                HapticEvent.PLACE -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                HapticEvent.LINE_CLEAR -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    delay(80)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
        }
    }

    // Track positions in root (window) coordinates
    var gridPositionInRoot by remember { mutableStateOf(Offset.Zero) }
    var boxPositionInRoot by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                boxPositionInRoot = coords.positionInRoot()
            }
    ) {
        // Main layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            ScoreBar(
                score = gameState.score,
                highScore = gameState.highScore,
                onSettingsClick = onSettingsClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            GameGrid(
                grid = gameState.grid,
                clearingCells = gameState.clearingCells,
                highlightCells = dragState.highlightCells,
                ghostShape = dragState.shape,
                ghostRow = dragState.ghostRow,
                ghostCol = dragState.ghostCol,
                ghostValid = dragState.ghostValid,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .onGloballyPositioned { coords ->
                        gridPositionInRoot = coords.positionInRoot()
                    },
                onGridLayout = { gridOffset, cellSizePx ->
                    viewModel.gridScreenOffset = gridOffset
                    viewModel.cellSizePx = cellSizePx
                }
            )

            val density = LocalDensity.current
            val liftBasePx = with(density) { 96.dp.toPx() }
            viewModel.liftBasePx = liftBasePx

            // Shape tray
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                gameState.currentShapes.forEachIndexed { index, shape ->
                    val canFit = shape != null && GameEngine.canFitAnywhere(gameState.grid, shape)
                    DraggableShapePreview(
                        shape = shape,
                        modifier = Modifier.weight(1f).testTag("tray_shape_$index"),
                        onDragStart = { s -> viewModel.onDragStart(index, s) },
                        onDrag = { rootOffset ->
                            val gridRelative = rootOffset - gridPositionInRoot
                            val s = dragState.shape
                            val floatingCenterYOffset = if (s != null) {
                                val gridCellPx = viewModel.cellSizePx
                                val liftPx = liftBasePx + gridCellPx
                                val floatingHeightPx = s.height * gridCellPx
                                -floatingHeightPx / 2f - liftPx
                            } else 0f
                            viewModel.onDrag(rootOffset, gridRelative, floatingCenterYOffset)
                        },
                        onDragEnd = { viewModel.onDragEnd() },
                        onDragCancel = { viewModel.onDragCancel() },
                        onDoubleTap = { viewModel.rotateShape(index) },
                        isDragging = dragState.source == DragSource.TRAY && dragState.shapeIndex == index,
                        dimmed = !canFit
                    )
                }
            }

            // Hold box
            Spacer(modifier = Modifier.height(4.dp))
            val holdCanFit = gameState.holdShape != null &&
                GameEngine.canFitAnywhere(gameState.grid, gameState.holdShape!!)
            HoldBox(
                modifier = Modifier.testTag("hold_box"),
                holdShape = gameState.holdShape,
                isDragging = dragState.source == DragSource.HOLD && dragState.shape != null,
                dimmed = !holdCanFit && gameState.holdShape != null,
                onDragStart = { s -> viewModel.onHoldDragStart(s) },
                onDrag = { rootOffset ->
                    val gridRelative = rootOffset - gridPositionInRoot
                    val s = dragState.shape
                    val floatingCenterYOffset = if (s != null) {
                        val gridCellPx = viewModel.cellSizePx
                        val liftPx = liftBasePx + gridCellPx
                        val floatingHeightPx = s.height * gridCellPx
                        -floatingHeightPx / 2f - liftPx
                    } else 0f
                    viewModel.onDrag(rootOffset, gridRelative, floatingCenterYOffset)
                },
                onDragEnd = { viewModel.onDragEnd() },
                onDragCancel = { viewModel.onDragCancel() },
                onDoubleTap = { viewModel.rotateHoldShape() },
                onGloballyPositioned = { coords ->
                    val pos = coords.positionInRoot()
                    viewModel.holdBoxScreenRect = Rect(
                        offset = pos,
                        size = Size(
                            coords.size.width.toFloat(),
                            coords.size.height.toFloat()
                        )
                    )
                }
            )
        }

        // Score pop overlay — positioned relative to the grid
        if (scorePops.isNotEmpty()) {
            val gridInBox = gridPositionInRoot - boxPositionInRoot
            val density = LocalDensity.current
            val gridOffsetDp = with(density) {
                androidx.compose.ui.unit.DpOffset(gridInBox.x.toDp(), gridInBox.y.toDp())
            }
            Box(modifier = Modifier.offset(x = gridOffsetDp.x, y = gridOffsetDp.y)) {
                ScorePopOverlay(
                    pops = scorePops,
                    gridOffset = viewModel.gridScreenOffset,
                    cellSizePx = viewModel.cellSizePx,
                    onDismiss = { id -> viewModel.dismissScorePop(id) }
                )
            }
        }

        // Floating drag overlay — the shape following the finger
        if (dragState.shape != null) {
            FloatingDragShape(
                dragState = dragState,
                gridCellSizePx = viewModel.cellSizePx,
                parentRootOffset = boxPositionInRoot,
                gridRootOffset = gridPositionInRoot,
                gridPaddingOffset = viewModel.gridScreenOffset,
                onDropAnimationDone = { viewModel.onDropAnimationDone() }
            )
        }

        // Mid-game confetti — triggered once when the player beats their best score
        // Stays on screen for the rest of the game; cleared on new game
        val showMidGameConfetti by viewModel.showMidGameConfetti.collectAsState()
        if (showMidGameConfetti) {
            ConfettiEffect()
        }

        // Game over overlay — delayed so the player can see the board first
        var showGameOver by remember { mutableStateOf(false) }
        LaunchedEffect(gameState.isGameOver) {
            if (gameState.isGameOver) {
                delay(1500)
                showGameOver = true
            } else {
                showGameOver = false
            }
        }
        if (showGameOver) {
            GameOverOverlay(
                score = gameState.score,
                highScore = gameState.highScore,
                onNewGame = { viewModel.startNewGame() }
            )
        }
    }
}

/**
 * The shape that floats under the player's finger during drag.
 * Animates scale up on lift and moves to the grid target on drop.
 */
@Composable
private fun FloatingDragShape(
    dragState: DragState,
    gridCellSizePx: Float,
    parentRootOffset: Offset,
    gridRootOffset: Offset,
    gridPaddingOffset: Offset,
    onDropAnimationDone: () -> Unit
) {
    val shape = dragState.shape ?: return
    val density = LocalDensity.current
    val cellSizeDp = with(density) { gridCellSizePx.toDp() }

    val heightDp = cellSizeDp * shape.height
    // Use maxDim=5 to match ShapePreview's fixed canvas size
    val maxDim = 5
    val canvasSizeDp = cellSizeDp * maxDim

    val liftDp = 96.dp + cellSizeDp

    // --- Drag position (shape center, in parent dp) ---
    val fingerInParent = dragState.fingerRootOffset - parentRootOffset
    val dragCenterX = with(density) { fingerInParent.x.toDp() }
    val dragCenterY = with(density) { fingerInParent.y.toDp() } - heightDp / 2 - liftDp

    // --- Target grid position (shape center, in parent dp) ---
    val gridInParent = gridRootOffset - parentRootOffset
    val targetCenterX = with(density) { (gridInParent.x + gridPaddingOffset.x).toDp() } +
        cellSizeDp * (dragState.ghostCol + shape.width / 2f)
    val targetCenterY = with(density) { (gridInParent.y + gridPaddingOffset.y).toDp() } +
        cellSizeDp * (dragState.ghostRow + shape.height / 2f)

    // Lift animation: scale from 0.5 to 1.0
    val liftScale = remember(shape) { Animatable(0.5f) }
    LaunchedEffect(shape) {
        liftScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
        )
    }

    // Drop animation: move to grid target
    val dropProgress = remember { Animatable(0f) }
    LaunchedEffect(dragState.isDropAnimating) {
        if (dragState.isDropAnimating) {
            dropProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
            )
            onDropAnimationDone()
        } else {
            dropProgress.snapTo(0f)
        }
    }

    // Interpolate position during drop
    val t = dropProgress.value
    val centerX = if (dragState.isDropAnimating) {
        dragCenterX + (targetCenterX - dragCenterX) * t
    } else {
        dragCenterX
    }
    val centerY = if (dragState.isDropAnimating) {
        dragCenterY + (targetCenterY - dragCenterY) * t
    } else {
        dragCenterY
    }

    val offsetX = centerX - canvasSizeDp / 2
    val offsetY = centerY - canvasSizeDp / 2

    val scale = if (dragState.isDropAnimating) 1f else liftScale.value
    val alpha = if (dragState.isDropAnimating) {
        0.8f + 0.2f * t  // 0.8 → 1.0 (become fully opaque as it lands)
    } else {
        0.8f
    }

    Box(
        modifier = Modifier
            .offset(x = offsetX, y = offsetY)
            .size(canvasSizeDp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
    ) {
        ShapePreview(
            shape = shape,
            cellSize = cellSizeDp
        )
    }
}
