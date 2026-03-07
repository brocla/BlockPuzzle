package com.blockpuzzle.logic

import com.blockpuzzle.model.BlockColor
import com.blockpuzzle.model.Cell
import com.blockpuzzle.model.CellOffset
import com.blockpuzzle.model.GameState
import com.blockpuzzle.model.GameState.Companion.GRID_SIZE
import com.blockpuzzle.model.Shape
import com.blockpuzzle.model.ShapeTemplates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameEngineTest {

    private val empty = GameState.emptyGrid()

    private fun shape(vararg offsets: Pair<Int, Int>, color: BlockColor = BlockColor.RED): Shape =
        Shape(offsets.map { (r, c) -> CellOffset(r, c) }, color)

    /** Fill an entire row on the grid. */
    private fun fillRow(grid: List<List<Cell>>, row: Int): List<List<Cell>> {
        val mutable = grid.map { it.toMutableList() }
        for (c in 0 until GRID_SIZE) {
            mutable[row][c] = Cell(filled = true, color = BlockColor.BLUE)
        }
        return mutable.map { it.toList() }
    }

    /** Fill an entire column on the grid. */
    private fun fillCol(grid: List<List<Cell>>, col: Int): List<List<Cell>> {
        val mutable = grid.map { it.toMutableList() }
        for (r in 0 until GRID_SIZE) {
            mutable[r][col] = Cell(filled = true, color = BlockColor.BLUE)
        }
        return mutable.map { it.toList() }
    }

    // ──────────────────────────── canPlace ────────────────────────────

    @Test
    fun `canPlace - dot on empty grid`() {
        val dot = shape(0 to 0)
        assertTrue(GameEngine.canPlace(empty, dot, 0, 0))
        assertTrue(GameEngine.canPlace(empty, dot, 7, 7))
    }

    @Test
    fun `canPlace - shape within bounds`() {
        val line = shape(0 to 0, 0 to 1, 0 to 2)
        assertTrue(GameEngine.canPlace(empty, line, 0, 0))
        assertTrue(GameEngine.canPlace(empty, line, 0, 5))  // cols 5,6,7
    }

    @Test
    fun `canPlace - shape out of bounds`() {
        val line = shape(0 to 0, 0 to 1, 0 to 2)
        assertFalse(GameEngine.canPlace(empty, line, 0, 6))  // col 8 out
        assertFalse(GameEngine.canPlace(empty, line, -1, 0))
    }

    @Test
    fun `canPlace - overlap with filled cell`() {
        val dot = shape(0 to 0)
        val grid = GameEngine.placeShape(empty, dot, 3, 3)
        assertFalse(GameEngine.canPlace(grid, dot, 3, 3))
        assertTrue(GameEngine.canPlace(grid, dot, 3, 4))
    }

    @Test
    fun `canPlace - L-shape at corner`() {
        val bigL = shape(0 to 0, 1 to 0, 2 to 0, 2 to 1, 2 to 2)
        assertTrue(GameEngine.canPlace(empty, bigL, 5, 5))   // fits: rows 5-7, cols 5-7
        assertFalse(GameEngine.canPlace(empty, bigL, 6, 5))  // row 8 out
        assertFalse(GameEngine.canPlace(empty, bigL, 5, 6))  // col 8 out
    }

    // ──────────────────────────── placeShape ────────────────────────────

    @Test
    fun `placeShape - fills correct cells`() {
        val line = shape(0 to 0, 0 to 1, 0 to 2, color = BlockColor.ORANGE)
        val grid = GameEngine.placeShape(empty, line, 2, 3)

        assertTrue(grid[2][3].filled)
        assertTrue(grid[2][4].filled)
        assertTrue(grid[2][5].filled)
        assertEquals(BlockColor.ORANGE, grid[2][3].color)
        assertFalse(grid[2][2].filled)
        assertFalse(grid[2][6].filled)
    }

    @Test
    fun `placeShape - does not mutate original grid`() {
        val dot = shape(0 to 0)
        val after = GameEngine.placeShape(empty, dot, 0, 0)
        assertFalse(empty[0][0].filled)
        assertTrue(after[0][0].filled)
    }

    // ──────────────────────────── findCompleteLines ────────────────────────────

    @Test
    fun `findCompleteLines - no lines on empty grid`() {
        val lines = GameEngine.findCompleteLines(empty)
        assertTrue(lines.isEmpty)
    }

    @Test
    fun `findCompleteLines - detects full row`() {
        val grid = fillRow(empty, 3)
        val lines = GameEngine.findCompleteLines(grid)
        assertEquals(setOf(3), lines.rows)
        assertTrue(lines.cols.isEmpty())
    }

    @Test
    fun `findCompleteLines - detects full column`() {
        val grid = fillCol(empty, 5)
        val lines = GameEngine.findCompleteLines(grid)
        assertTrue(lines.rows.isEmpty())
        assertEquals(setOf(5), lines.cols)
    }

    @Test
    fun `findCompleteLines - detects row and column simultaneously`() {
        var grid = fillRow(empty, 2)
        grid = fillCol(grid, 4)
        val lines = GameEngine.findCompleteLines(grid)
        assertEquals(setOf(2), lines.rows)
        assertEquals(setOf(4), lines.cols)
    }

    @Test
    fun `findCompleteLines - multiple rows`() {
        var grid = fillRow(empty, 0)
        grid = fillRow(grid, 7)
        val lines = GameEngine.findCompleteLines(grid)
        assertEquals(setOf(0, 7), lines.rows)
    }

    // ──────────────────────────── clearLines ────────────────────────────

    @Test
    fun `clearLines - clears row cells`() {
        val grid = fillRow(empty, 3)
        val lines = CompleteLines(rows = setOf(3))
        val result = GameEngine.clearLines(grid, lines)

        for (c in 0 until GRID_SIZE) {
            assertFalse(result.grid[3][c].filled)
        }
    }

    @Test
    fun `clearLines - single row scores 80 points`() {
        // 8 cells * 10 points * 1x multiplier = 80
        val grid = fillRow(empty, 0)
        val lines = CompleteLines(rows = setOf(0))
        val result = GameEngine.clearLines(grid, lines)
        assertEquals(80, result.points)
    }

    @Test
    fun `clearLines - two lines get 3x multiplier`() {
        // 2 rows = 16 cells, but unique cells = 16
        // multiplier = 2*(2+1)/2 = 3
        // 16 * 10 * 3 = 480
        var grid = fillRow(empty, 0)
        grid = fillRow(grid, 1)
        val lines = CompleteLines(rows = setOf(0, 1))
        val result = GameEngine.clearLines(grid, lines)
        assertEquals(480, result.points)
    }

    @Test
    fun `clearLines - row plus column with intersection`() {
        // 1 row (8 cells) + 1 col (8 cells) - 1 intersection = 15 unique cells
        // multiplier = 2*(2+1)/2 = 3
        // 15 * 10 * 3 = 450
        var grid = fillRow(empty, 2)
        grid = fillCol(grid, 4)
        val lines = CompleteLines(rows = setOf(2), cols = setOf(4))
        val result = GameEngine.clearLines(grid, lines)
        assertEquals(450, result.points)
    }

    @Test
    fun `clearLines - no lines returns zero points`() {
        val result = GameEngine.clearLines(empty, CompleteLines())
        assertEquals(0, result.points)
    }

    // ──────────────────────────── isGameOver ────────────────────────────

    @Test
    fun `isGameOver - empty grid is never game over`() {
        val shapes = listOf(shape(0 to 0))
        assertFalse(GameEngine.isGameOver(empty, shapes))
    }

    @Test
    fun `isGameOver - all shapes null means game over`() {
        assertTrue(GameEngine.isGameOver(empty, listOf(null, null, null)))
    }

    @Test
    fun `isGameOver - full grid with dot is game over`() {
        var grid = empty
        val dot = shape(0 to 0)
        for (r in 0 until GRID_SIZE) {
            for (c in 0 until GRID_SIZE) {
                grid = GameEngine.placeShape(grid, dot, r, c)
            }
        }
        assertTrue(GameEngine.isGameOver(grid, listOf(dot)))
    }

    @Test
    fun `isGameOver - nearly full grid with one empty cell`() {
        var grid = empty
        val dot = shape(0 to 0)
        for (r in 0 until GRID_SIZE) {
            for (c in 0 until GRID_SIZE) {
                if (r == 7 && c == 7) continue  // leave one cell open
                grid = GameEngine.placeShape(grid, dot, r, c)
            }
        }
        // Dot can still fit at (7,7)
        assertFalse(GameEngine.isGameOver(grid, listOf(dot)))
        // 2-cell line cannot fit
        val line = shape(0 to 0, 0 to 1)
        assertTrue(GameEngine.isGameOver(grid, listOf(line)))
    }

    // ──────────────────────────── canFitAnywhere ────────────────────────────

    @Test
    fun `canFitAnywhere - any shape fits on empty grid`() {
        val bigL = shape(0 to 0, 1 to 0, 2 to 0, 2 to 1, 2 to 2)
        assertTrue(GameEngine.canFitAnywhere(empty, bigL))
    }

    @Test
    fun `canFitAnywhere - dot fits on nearly full grid`() {
        var grid = empty
        val dot = shape(0 to 0)
        for (r in 0 until GRID_SIZE) {
            for (c in 0 until GRID_SIZE) {
                if (r == 4 && c == 4) continue
                grid = GameEngine.placeShape(grid, dot, r, c)
            }
        }
        assertTrue(GameEngine.canFitAnywhere(grid, dot))
    }

    @Test
    fun `canFitAnywhere - line does not fit on nearly full grid`() {
        var grid = empty
        val dot = shape(0 to 0)
        for (r in 0 until GRID_SIZE) {
            for (c in 0 until GRID_SIZE) {
                if (r == 4 && c == 4) continue
                grid = GameEngine.placeShape(grid, dot, r, c)
            }
        }
        val line = shape(0 to 0, 0 to 1)
        assertFalse(GameEngine.canFitAnywhere(grid, line))
    }

    @Test
    fun `canFitAnywhere - false on completely full grid`() {
        var grid = empty
        val dot = shape(0 to 0)
        for (r in 0 until GRID_SIZE) {
            for (c in 0 until GRID_SIZE) {
                grid = GameEngine.placeShape(grid, dot, r, c)
            }
        }
        assertFalse(GameEngine.canFitAnywhere(grid, dot))
    }

    // ──────────────────────────── CompleteLines ────────────────────────────

    @Test
    fun `CompleteLines isEmpty - empty is true`() {
        assertTrue(CompleteLines().isEmpty)
    }

    @Test
    fun `CompleteLines isEmpty - rows only is false`() {
        assertFalse(CompleteLines(rows = setOf(0)).isEmpty)
    }

    @Test
    fun `CompleteLines isEmpty - cols only is false`() {
        assertFalse(CompleteLines(cols = setOf(3)).isEmpty)
    }

    // ──────────────────────────── clearLines - higher multipliers ────────────────

    @Test
    fun `clearLines - three rows get 6x multiplier`() {
        // 3 rows = 24 cells, multiplier = 3*(3+1)/2 = 6
        // 24 * 10 * 6 = 1440
        var grid = fillRow(empty, 0)
        grid = fillRow(grid, 1)
        grid = fillRow(grid, 2)
        val lines = CompleteLines(rows = setOf(0, 1, 2))
        val result = GameEngine.clearLines(grid, lines)
        assertEquals(1440, result.points)
    }

    @Test
    fun `clearLines - four lines get 10x multiplier`() {
        // 2 rows + 2 cols: lineCount = 4, multiplier = 4*(4+1)/2 = 10
        // Unique cells: 2*8 + 2*8 - 4 intersections = 28
        // 28 * 10 * 10 = 2800
        var grid = fillRow(empty, 0)
        grid = fillRow(grid, 1)
        grid = fillCol(grid, 0)
        grid = fillCol(grid, 1)
        val lines = CompleteLines(rows = setOf(0, 1), cols = setOf(0, 1))
        val result = GameEngine.clearLines(grid, lines)
        assertEquals(2800, result.points)
    }

    @Test
    fun `clearLines - clears intersecting row and column cells`() {
        var grid = fillRow(empty, 3)
        grid = fillCol(grid, 5)
        val lines = CompleteLines(rows = setOf(3), cols = setOf(5))
        val result = GameEngine.clearLines(grid, lines)
        // All cells in row 3 and column 5 should be cleared
        for (c in 0 until GRID_SIZE) {
            assertFalse("Row 3, col $c should be cleared", result.grid[3][c].filled)
        }
        for (r in 0 until GRID_SIZE) {
            assertFalse("Row $r, col 5 should be cleared", result.grid[r][5].filled)
        }
    }

    // ──────────────────────────── canPlace - edge cases ────────────────────────────

    @Test
    fun `canPlace - shape at grid bottom-right corner`() {
        val square = shape(0 to 0, 0 to 1, 1 to 0, 1 to 1)
        assertTrue(GameEngine.canPlace(empty, square, 6, 6))   // fits in rows 6-7, cols 6-7
        assertFalse(GameEngine.canPlace(empty, square, 7, 7))  // row 8 and col 8 out
    }

    @Test
    fun `canPlace - negative row is rejected`() {
        val dot = shape(0 to 0)
        assertFalse(GameEngine.canPlace(empty, dot, -1, 0))
    }

    @Test
    fun `canPlace - negative col is rejected`() {
        val dot = shape(0 to 0)
        assertFalse(GameEngine.canPlace(empty, dot, 0, -1))
    }

    // ──────────────────────────── isGameOver - edge cases ────────────────────────────

    @Test
    fun `isGameOver - empty shapes list is game over`() {
        assertTrue(GameEngine.isGameOver(empty, emptyList()))
    }

    @Test
    fun `isGameOver - mix of null and fitting shape is not game over`() {
        val dot = shape(0 to 0)
        assertFalse(GameEngine.isGameOver(empty, listOf(null, dot, null)))
    }

    // ──────────────────────────── generateShapeTriple ────────────────────────────

    @Test
    fun `generateShapeTriple - returns 3 shapes`() {
        val shapes = GameEngine.generateShapeTriple(empty, Random(42))
        assertEquals(3, shapes.size)
        shapes.forEach { shape ->
            assertTrue(shape.cells.isNotEmpty())
            assertTrue(shape.color != BlockColor.NONE)
        }
    }

    @Test
    fun `generateShapeTriple - deterministic with seeded random`() {
        val a = GameEngine.generateShapeTriple(empty, Random(123))
        val b = GameEngine.generateShapeTriple(empty, Random(123))
        assertEquals(a, b)
    }

    @Test
    fun `generateShapeTriple - never assigns NONE color`() {
        // Test across multiple seeds to increase confidence
        for (seed in 0..99) {
            val shapes = GameEngine.generateShapeTriple(empty, Random(seed))
            shapes.forEach { shape ->
                assertTrue(
                    "Shape should not have NONE color (seed=$seed)",
                    shape.color != BlockColor.NONE
                )
            }
        }
    }

    @Test
    fun `generateShapeTriple - all shapes fit on a crowded grid`() {
        // Fill all but one cell to create a very crowded board
        val crowded = List(GRID_SIZE) { r ->
            List(GRID_SIZE) { c ->
                if (r == 0 && c == 0) Cell() else Cell(filled = true, color = BlockColor.RED)
            }
        }
        for (seed in 0..49) {
            val shapes = GameEngine.generateShapeTriple(crowded, Random(seed))
            shapes.forEach { shape ->
                assertTrue(
                    "Shape should be placeable on crowded grid (seed=$seed)",
                    GameEngine.canFitAnywhere(crowded, shape)
                )
            }
        }
    }

    // ──────────────────────────── placementPoints ────────────────────────────

    @Test
    fun `placementPoints - equals cell count`() {
        val dot = shape(0 to 0)
        assertEquals(1, GameEngine.placementPoints(dot))

        val square = shape(0 to 0, 0 to 1, 1 to 0, 1 to 1)
        assertEquals(4, GameEngine.placementPoints(square))
    }
}
