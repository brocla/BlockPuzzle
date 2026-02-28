package com.blockpuzzle.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.blockpuzzle.logic.GameEngine
import com.blockpuzzle.model.Shape
import kotlinx.coroutines.delay
import com.blockpuzzle.ui.components.DraggableShapePreview
import com.blockpuzzle.ui.components.GameGrid
import com.blockpuzzle.ui.components.ScoreBar
import com.blockpuzzle.ui.components.ScorePopOverlay
import com.blockpuzzle.ui.components.ShapePreview
import com.blockpuzzle.ui.theme.toComposeColor
import com.blockpuzzle.viewmodel.DragState
import com.blockpuzzle.viewmodel.GameViewModel
import com.blockpuzzle.viewmodel.HapticEvent

@Composable
fun GameScreen(
    viewModel: GameViewModel,
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
                highScore = gameState.highScore
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

            // Shape tray
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val density = LocalDensity.current
                gameState.currentShapes.forEachIndexed { index, shape ->
                    val canFit = shape != null && GameEngine.canFitAnywhere(gameState.grid, shape)
                    DraggableShapePreview(
                        shape = shape,
                        modifier = Modifier.weight(1f),
                        onDragStart = { s -> viewModel.onDragStart(index, s) },
                        onDrag = { rootOffset ->
                            val gridRelative = rootOffset - gridPositionInRoot
                            // The floating shape is drawn above the finger:
                            // its center is at finger.y - shapeHeight/2 - 32dp
                            // So the offset from finger to floating center is:
                            val s = dragState.shape
                            val floatingCenterYOffset = if (s != null) {
                                val gridCellPx = viewModel.cellSizePx
                                val liftPx = with(density) { 96.dp.toPx() } + gridCellPx
                                val floatingHeightPx = s.height * gridCellPx
                                -floatingHeightPx / 2f - liftPx
                            } else 0f
                            viewModel.onDrag(rootOffset, gridRelative, floatingCenterYOffset)
                        },
                        onDragEnd = { viewModel.onDragEnd() },
                        onDragCancel = { viewModel.onDragCancel() },
                        isDragging = dragState.shapeIndex == index,
                        dimmed = !canFit
                    )
                }
            }
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
                parentRootOffset = boxPositionInRoot
            )
        }

        // Game over overlay
        if (gameState.isGameOver) {
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
 * Drawn at a larger scale so it's visible past the finger.
 */
@Composable
private fun FloatingDragShape(dragState: DragState, gridCellSizePx: Float, parentRootOffset: Offset) {
    val shape = dragState.shape ?: return
    val density = LocalDensity.current
    val cellSizeDp = with(density) { gridCellSizePx.toDp() }

    val heightDp = cellSizeDp * shape.height
    // ShapePreview uses a square canvas of maxDim × maxDim and centers the cells inside.
    // Match that size so the cells align with our offset calculation.
    val maxDim = maxOf(shape.width, shape.height, 1)
    val canvasSizeDp = cellSizeDp * maxDim

    // Lift = 96dp + one grid cell height (matches ghost offset calculation)
    val liftDp = 96.dp + cellSizeDp

    // Convert finger from root (window) coords to parent Box coords.
    // The parent Box is offset from root by innerPadding (status bar, etc.).
    val fingerInParent = dragState.fingerRootOffset - parentRootOffset

    // Shape center: horizontally on finger, lifted above finger vertically.
    // ShapePreview centers cells in the square canvas, so canvas center = shape center.
    val centerX = with(density) { fingerInParent.x.toDp() }
    val centerY = with(density) { fingerInParent.y.toDp() } - heightDp / 2 - liftDp
    val offsetX = centerX - canvasSizeDp / 2
    val offsetY = centerY - canvasSizeDp / 2

    Box(
        modifier = Modifier
            .offset(x = offsetX, y = offsetY)
            .size(canvasSizeDp)
            .graphicsLayer { alpha = 0.8f }
    ) {
        ShapePreview(
            shape = shape,
            cellSize = cellSizeDp
        )
    }
}
