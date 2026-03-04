package com.blockpuzzle.logic

import com.blockpuzzle.model.Cell
import com.blockpuzzle.model.CellOffset
import com.blockpuzzle.model.GameState.Companion.GRID_SIZE
import com.blockpuzzle.model.Grid
import com.blockpuzzle.model.Shape
import com.blockpuzzle.model.ShapeTemplates
import kotlin.random.Random

/**
 * Pure game logic — no Android dependencies.
 * All functions take state in and return state out; no mutation.
 */
object GameEngine {

    /** Deep-copy [grid], apply [mutate], and freeze back to immutable lists. */
    private inline fun Grid.mutated(
        mutate: (MutableList<MutableList<Cell>>) -> Unit
    ): Grid {
        val copy = map { it.toMutableList() }.toMutableList()
        mutate(copy)
        return copy.map { it.toList() }
    }

    /** Check whether [shape] can be placed at ([row], [col]) on the [grid]. */
    fun canPlace(grid: Grid, shape: Shape, row: Int, col: Int): Boolean =
        shape.cells.all { offset ->
            val r = row + offset.row
            val c = col + offset.col
            r in 0 until GRID_SIZE && c in 0 until GRID_SIZE && !grid[r][c].filled
        }

    /** Place [shape] at ([row], [col]) and return the updated grid. */
    fun placeShape(grid: Grid, shape: Shape, row: Int, col: Int): Grid =
        grid.mutated { mutable ->
            for (offset in shape.cells) {
                mutable[row + offset.row][col + offset.col] = Cell(filled = true, color = shape.color)
            }
        }

    /** Find all fully-filled rows and columns. */
    fun findCompleteLines(grid: Grid): CompleteLines {
        val rows = grid.indices.filter { r ->
            grid[r].all { it.filled }
        }.toSet()
        val cols = grid[0].indices.filter { c ->
            grid.all { row -> row[c].filled }
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
    fun clearLines(grid: Grid, lines: CompleteLines): ClearResult {
        if (lines.isEmpty) return ClearResult(grid, 0)

        val clearedCells = lines.toCellSet(GRID_SIZE)

        val newGrid = grid.mutated { mutable ->
            for ((r, c) in clearedCells) {
                mutable[r][c] = Cell()
            }
        }

        val lineCount = lines.rows.size + lines.cols.size
        val multiplier = lineCount * (lineCount + 1) / 2  // triangular number
        val points = clearedCells.size * 10 * multiplier

        return ClearResult(newGrid, points)
    }

    /** Check whether [shape] can fit anywhere on the [grid]. */
    fun canFitAnywhere(grid: Grid, shape: Shape): Boolean =
        grid.indices.any { r ->
            grid[0].indices.any { c ->
                canPlace(grid, shape, r, c)
            }
        }

    /** Check if any of the remaining shapes can be placed anywhere on the grid. */
    fun isGameOver(grid: Grid, shapes: List<Shape?>): Boolean =
        shapes.none { it != null && canFitAnywhere(grid, it) }

    /** Generate 3 random shapes with random colors and random orientations. */
    fun generateShapeTriple(random: Random = Random): List<Shape> =
        List(3) {
            val template = ShapeTemplates.ALL.random(random)
            val color = ShapeTemplates.COLORS.random(random)
            (0 until random.nextInt(template.rotations))
                .fold(template.toShape(color)) { shape, _ -> shape.rotateCW() }
        }

    /** Points awarded for placing a shape (before any line-clear bonus). */
    fun placementPoints(shape: Shape): Int = shape.cells.size
}

/** Indices of complete rows and columns. */
data class CompleteLines(
    val rows: Set<Int> = emptySet(),
    val cols: Set<Int> = emptySet()
) {
    val isEmpty: Boolean get() = rows.isEmpty() && cols.isEmpty()
    val isNotEmpty: Boolean get() = !isEmpty

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
    val grid: Grid,
    val points: Int
)
