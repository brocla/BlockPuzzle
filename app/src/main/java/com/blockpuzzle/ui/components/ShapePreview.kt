package com.blockpuzzle.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.blockpuzzle.model.BlockColor
import com.blockpuzzle.model.CellOffset
import com.blockpuzzle.model.Shape
import com.blockpuzzle.ui.theme.BlockPuzzleTheme
import com.blockpuzzle.ui.theme.toComposeColor

/**
 * Renders a shape at small scale for the tray below the grid.
 *
 * @param shape The shape to render, or null if already placed.
 * @param cellSize Size of each mini-cell in dp.
 * @param dimmed Whether to gray out the shape (can't be placed anywhere).
 */
@Composable
fun ShapePreview(
    shape: Shape?,
    modifier: Modifier = Modifier,
    cellSize: Dp = 28.dp,
    dimmed: Boolean = false
) {
    // Fixed canvas size so the Row height doesn't change when a shape is placed
    val maxDim = 5
    val canvasSize = cellSize * maxDim

    if (shape == null) {
        Canvas(modifier = modifier.size(canvasSize)) {}
        return
    }

    val width = shape.width
    val height = shape.height

    Canvas(modifier = modifier.size(canvasSize)) {
        val cellPx = canvasSize.toPx() / maxDim
        // Center the shape within the canvas
        val offsetX = (size.width - width * cellPx) / 2
        val offsetY = (size.height - height * cellPx) / 2
        val color = if (dimmed) {
            shape.color.toComposeColor().copy(alpha = 0.15f)
        } else {
            shape.color.toComposeColor()
        }

        for (cell in shape.cells) {
            val x = offsetX + cell.col * cellPx
            val y = offsetY + cell.row * cellPx
            drawPreviewCell(x, y, cellPx, color)
        }
    }
}

private fun DrawScope.drawPreviewCell(x: Float, y: Float, cellPx: Float, color: Color) {
    val inset = cellPx * 0.08f
    // Shadow — scale alpha instead of overwriting so dimmed shapes stay dim
    drawRoundRect(
        color = color.copy(alpha = color.alpha * 0.5f),
        topLeft = Offset(x + inset, y + inset),
        size = Size(cellPx - inset * 2, cellPx - inset * 2),
        cornerRadius = CornerRadius(cellPx * 0.15f)
    )
    // Face
    drawRoundRect(
        color = color,
        topLeft = Offset(x + inset * 1.5f, y + inset * 1.5f),
        size = Size(cellPx - inset * 3, cellPx - inset * 3),
        cornerRadius = CornerRadius(cellPx * 0.12f)
    )
    // Highlight — scale alpha to respect dimming
    drawRoundRect(
        color = Color.White.copy(alpha = color.alpha * 0.15f),
        topLeft = Offset(x + inset * 2f, y + inset * 2f),
        size = Size(cellPx * 0.4f, cellPx * 0.25f),
        cornerRadius = CornerRadius(cellPx * 0.1f)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF3E2723)
@Composable
private fun ShapePreviewDemo() {
    val lShape = Shape(
        cells = listOf(CellOffset(0, 0), CellOffset(1, 0), CellOffset(2, 0), CellOffset(2, 1)),
        color = BlockColor.RED
    )
    BlockPuzzleTheme {
        ShapePreview(shape = lShape)
    }
}
