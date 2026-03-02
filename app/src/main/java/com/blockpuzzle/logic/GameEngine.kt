package com.blockpuzzle.logic

import com.blockpuzzle.model.BlockColor
import com.blockpuzzle.model.Cell
import com.blockpuzzle.model.CellOffset
import com.blockpuzzle.model.GameState
import com.blockpuzzle.model.GameState.Companion.GRID_SIZE
import com.blockpuzzle.model.Shape
import com.blockpuzzle.model.ShapeTemplates
import kotlin.random.Random

/**
 * Pure game logic — no Android dependencies.
 * All functions take state in and return state out; no mutation.
 */
object GameEngine {

    /** Check whether [shape] can be placed at ([row], [col]) on the [grid]. */
    fun canPlace(grid: List<List<Cell>>, shape: Shape, row: Int, col: Int): Boolean =
        shape.cells.all { offset ->
            val r = row + offset.row
            val c = col + offset.col
            r in 0 until GRID_SIZE && c in 0 until GRID_SIZE && !grid[r][c].filled
        }

    /** Place [shape] at ([row], [col]) and return the updated grid. */
    fun placeShape(grid: List<List<Cell>>, shape: Shape, row: Int, col: Int): List<List<Cell>> {
        val mutable = grid.map { it.toMutableList() }
        for (offset in shape.cells) {
            mutable[row + offset.row][col + offset.col] = Cell(filled = true, color = shape.color)
        }
        return mutable.map { it.toList() }
    }

    /** Find all fully-filled rows and columns. */
    fun findCompleteLines(grid: List<List<Cell>>): CompleteLines {
        val rows = (0 until GRID_SIZE).filter { r ->
            (0 until GRID_SIZE).all { c -> grid[r][c].filled }
        }.toSet()
        val cols = (0 until GRID_SIZE).filter { c ->
            (0 until GRID_SIZE).all { r -> grid[r][c].filled }
        }.toSet()
        return CompleteLines(rows, cols)
    }

    /**
     * Clear the given rows and columns from the grid.
     * Returns the new grid and the points scored.
     *
     * Scoring:
     * - 10 points per cell cleared
     * - Bonus multiplier for multiple simultaneous lines:
     *   1 line = 1x, 2 lines = 3x, 3 lines = 6x, 4+ lines = triangular(n)x
     */
    fun clearLines(grid: List<List<Cell>>, lines: CompleteLines): ClearResult {
        if (lines.isEmpty()) return ClearResult(grid, 0)

        val mutable = grid.map { it.toMutableList() }
        val clearedCells = lines.toCellSet(GRID_SIZE)

        for ((r, c) in clearedCells) {
            mutable[r][c] = Cell()
        }

        val lineCount = lines.rows.size + lines.cols.size
        val multiplier = lineCount * (lineCount + 1) / 2  // triangular number
        val points = clearedCells.size * 10 * multiplier

        return ClearResult(mutable.map { it.toList() }, points)
    }

    /** Check whether [shape] can fit anywhere on the [grid]. */
    fun canFitAnywhere(grid: List<List<Cell>>, shape: Shape): Boolean =
        (0 until GRID_SIZE).any { r ->
            (0 until GRID_SIZE).any { c ->
                canPlace(grid, shape, r, c)
            }
        }

    /** Check if any of the remaining shapes can be placed anywhere on the grid. */
    fun isGameOver(grid: List<List<Cell>>, shapes: List<Shape?>): Boolean =
        shapes.filterNotNull().none { canFitAnywhere(grid, it) }

    /** Generate 3 random shapes with random colors and random orientations. */
    fun generateShapeTriple(random: Random = Random): List<Shape> =
        List(3) {
            val template = ShapeTemplates.ALL.random(random)
            val color = ShapeTemplates.COLORS.random(random)
            var shape = template.toShape(color)
            repeat(random.nextInt(template.rotations)) { shape = shape.rotateCW() }
            shape
        }

    /** Points awarded for placing a shape (before any line-clear bonus). */
    fun placementPoints(shape: Shape): Int = shape.cells.size
}

/** Indices of complete rows and columns. */
data class CompleteLines(
    val rows: Set<Int> = emptySet(),
    val cols: Set<Int> = emptySet()
) {
    fun isEmpty(): Boolean = rows.isEmpty() && cols.isEmpty()
    fun isNotEmpty(): Boolean = !isEmpty()

    /** All individual cells covered by the complete rows and columns. */
    fun toCellSet(gridSize: Int): Set<CellOffset> = buildSet {
        for (r in rows) {
            for (c in 0 until gridSize) add(CellOffset(r, c))
        }
        for (c in cols) {
            for (r in 0 until gridSize) add(CellOffset(r, c))
        }
    }
}

/** Result of clearing lines: updated grid + points scored. */
data class ClearResult(
    val grid: List<List<Cell>>,
    val points: Int
)
