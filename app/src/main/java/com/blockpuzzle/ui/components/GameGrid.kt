package com.blockpuzzle.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import com.blockpuzzle.model.BlockColor
import com.blockpuzzle.model.Cell
import com.blockpuzzle.model.CellOffset
import com.blockpuzzle.model.GameState
import com.blockpuzzle.model.GameState.Companion.GRID_SIZE
import com.blockpuzzle.model.Shape
import com.blockpuzzle.ui.theme.BlockPuzzleTheme
import com.blockpuzzle.ui.theme.BoardLight
import com.blockpuzzle.ui.theme.BoardMedium
import com.blockpuzzle.ui.theme.CellInset
import com.blockpuzzle.ui.theme.GhostInvalid
import com.blockpuzzle.ui.theme.GhostValid
import com.blockpuzzle.ui.theme.GridLine
import com.blockpuzzle.ui.theme.toComposeColor

// Corner-radius fractions (relative to cell size) used by cell drawing helpers
private const val CORNER_BOARD = 0.20f
private const val CORNER_LARGE = 0.15f
private const val CORNER_MEDIUM = 0.12f
private const val CORNER_SMALL = 0.10f

/**
 * The 8x8 game grid drawn with Canvas for full control over the wooden look.
 *
 * @param grid The current grid state.
 * @param clearingCells Cells currently playing the line-clear animation.
 * @param ghostShape Optional shape being dragged — shown as a translucent preview.
 * @param ghostRow Grid row where the ghost's origin would land (-1 if not over grid).
 * @param ghostCol Grid column where the ghost's origin would land (-1 if not over grid).
 * @param ghostValid Whether the ghost position is a valid placement.
 * @param modifier Modifier for sizing/positioning.
 * @param onGridLayout Callback with grid's top-left offset and cell size in pixels.
 */
@Composable
fun GameGrid(
    grid: List<List<Cell>>,
    clearingCells: Set<CellOffset> = emptySet(),
    highlightCells: Set<CellOffset> = emptySet(),
    ghostShape: Shape? = null,
    ghostRow: Int = -1,
    ghostCol: Int = -1,
    ghostValid: Boolean = false,
    modifier: Modifier = Modifier,
    onGridLayout: ((gridOffset: Offset, cellSizePx: Float) -> Unit)? = null
) {
    // Animation: 0 → 1 over the clear duration.
    // 0..0.43 (0-150ms) = flash white, 0.43..1.0 (150-350ms) = fade out
    val clearAnim = remember { Animatable(0f) }

    LaunchedEffect(clearingCells) {
        if (clearingCells.isNotEmpty()) {
            clearAnim.snapTo(0f)
            clearAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 550, easing = LinearEasing)
            )
        }
        // When clearingCells becomes empty, don't snap — the cells are already
        // removed from the grid, so the stale anim value is never drawn.
    }
    val animProgress = clearAnim.value

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)  // square grid
    ) {
        val padding = size.width * 0.02f
        val boardSize = size.width - padding * 2
        val cellSize = boardSize / GRID_SIZE

        // Report layout to parent for drag coordinate mapping
        onGridLayout?.invoke(Offset(padding, padding), cellSize)

        // Board background
        drawRoundRect(
            color = BoardMedium,
            topLeft = Offset(padding, padding),
            size = Size(boardSize, boardSize),
            cornerRadius = CornerRadius(cellSize * CORNER_BOARD)
        )

        // Draw cells
        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                val x = padding + col * cellSize
                val y = padding + row * cellSize
                val cell = grid[row][col]
                val isClearing = CellOffset(row, col) in clearingCells

                if (isClearing && cell.filled) {
                    // Clearing animation: flash white then fade out
                    if (animProgress <= 150f / 550f) {
                        // Phase 1: flash — lerp block color toward white
                        val flashFraction = animProgress / (150f / 550f)
                        val baseColor = cell.color.toComposeColor()
                        val flashColor = lerp(baseColor, Color.White, flashFraction)
                        drawFilledCell(x, y, cellSize, flashColor)
                    } else {
                        // Phase 2: fade out from white to transparent
                        val fadeFraction = (animProgress - 150f / 550f) / (1f - 150f / 550f)
                        val alpha = 1f - fadeFraction
                        drawFilledCell(x, y, cellSize, Color.White.copy(alpha = alpha))
                    }
                } else if (cell.filled) {
                    val isHighlighted = CellOffset(row, col) in highlightCells
                    val color = if (isHighlighted) Color.White else cell.color.toComposeColor()
                    drawFilledCell(x, y, cellSize, color)
                } else if (CellOffset(row, col) in highlightCells) {
                    // Empty cell in a would-clear row/col — draw white to complete the line
                    drawFilledCell(x, y, cellSize, Color.White)
                } else {
                    drawEmptyCell(x, y, cellSize)
                }
            }
        }

        // Ghost preview — outline in the shape's color
        if (ghostShape != null && ghostRow >= 0 && ghostCol >= 0 && ghostValid) {
            val outlineColor = ghostShape.color.toComposeColor().copy(alpha = 0.50f)
            for (offset in ghostShape.cells) {
                val r = ghostRow + offset.row
                val c = ghostCol + offset.col
                if (r in 0 until GRID_SIZE && c in 0 until GRID_SIZE) {
                    val x = padding + c * cellSize
                    val y = padding + r * cellSize
                    drawGhostCell(x, y, cellSize, outlineColor)
                }
            }
        }

        // Grid lines
        for (i in 0..GRID_SIZE) {
            val pos = padding + i * cellSize
            // Horizontal
            drawLine(
                color = GridLine,
                start = Offset(padding, pos),
                end = Offset(padding + boardSize, pos),
                strokeWidth = if (i % GRID_SIZE == 0) 2f else 1f
            )
            // Vertical
            drawLine(
                color = GridLine,
                start = Offset(pos, padding),
                end = Offset(pos, padding + boardSize),
                strokeWidth = if (i % GRID_SIZE == 0) 2f else 1f
            )
        }
    }
}


private fun DrawScope.drawEmptyCell(x: Float, y: Float, cellSize: Float) {
    val inset = cellSize * 0.08f
    // Inset shadow
    drawRoundRect(
        color = CellInset,
        topLeft = Offset(x + inset, y + inset),
        size = Size(cellSize - inset * 2, cellSize - inset * 2),
        cornerRadius = CornerRadius(cellSize * CORNER_MEDIUM)
    )
    // Lighter fill
    drawRoundRect(
        color = BoardLight,
        topLeft = Offset(x + inset * 1.5f, y + inset * 1.5f),
        size = Size(cellSize - inset * 3, cellSize - inset * 3),
        cornerRadius = CornerRadius(cellSize * CORNER_SMALL)
    )
}

internal fun DrawScope.drawFilledCell(
    x: Float, y: Float, cellSize: Float, color: Color, insetFraction: Float = 0.06f
) {
    val inset = cellSize * insetFraction
    // Block shadow (slightly darker, tight behind the block)
    drawRoundRect(
        color = color.copy(alpha = color.alpha * 0.5f),
        topLeft = Offset(x + inset, y + inset),
        size = Size(cellSize - inset * 2, cellSize - inset * 2),
        cornerRadius = CornerRadius(cellSize * CORNER_LARGE)
    )
    // Block face
    drawRoundRect(
        color = color,
        topLeft = Offset(x + inset * 1.5f, y + inset * 1.5f),
        size = Size(cellSize - inset * 3, cellSize - inset * 3),
        cornerRadius = CornerRadius(cellSize * CORNER_MEDIUM)
    )
    // Highlight (top-left light reflection)
    drawRoundRect(
        color = Color.White.copy(alpha = color.alpha * 0.15f),
        topLeft = Offset(x + inset * 2f, y + inset * 2f),
        size = Size(cellSize * 0.4f, cellSize * 0.25f),
        cornerRadius = CornerRadius(cellSize * CORNER_SMALL)
    )
}

private fun DrawScope.drawGhostCell(x: Float, y: Float, cellSize: Float, color: Color) {
    val inset = cellSize * 0.08f
    val strokeWidth = cellSize * CORNER_MEDIUM
    drawRoundRect(
        color = color,
        topLeft = Offset(x + inset, y + inset),
        size = Size(cellSize - inset * 2, cellSize - inset * 2),
        cornerRadius = CornerRadius(cellSize * CORNER_MEDIUM),
        style = Stroke(width = strokeWidth)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF3E2723)
@Composable
private fun GameGridPreview() {
    val grid = GameState.emptyGrid().toMutableList().map { it.toMutableList() }.also { g ->
        // Place a few blocks for preview
        g[0][0] = Cell(true, BlockColor.RED)
        g[0][1] = Cell(true, BlockColor.RED)
        g[1][0] = Cell(true, BlockColor.RED)
        g[3][3] = Cell(true, BlockColor.BLUE)
        g[3][4] = Cell(true, BlockColor.BLUE)
        g[3][5] = Cell(true, BlockColor.BLUE)
        g[4][4] = Cell(true, BlockColor.BLUE)
        g[6][1] = Cell(true, BlockColor.ORANGE)
        g[6][2] = Cell(true, BlockColor.ORANGE)
        g[7][1] = Cell(true, BlockColor.ORANGE)
        g[7][2] = Cell(true, BlockColor.ORANGE)
    }
    val ghost = Shape(listOf(CellOffset(0, 0), CellOffset(0, 1), CellOffset(1, 0)), BlockColor.GREEN)

    BlockPuzzleTheme {
        GameGrid(
            grid = grid,
            ghostShape = ghost,
            ghostRow = 5,
            ghostCol = 5,
            ghostValid = true
        )
    }
}
