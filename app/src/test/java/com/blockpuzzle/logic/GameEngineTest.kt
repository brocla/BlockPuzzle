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
        assertTrue(lines.isEmpty())
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

    // ──────────────────────────── generateShapeTriple ────────────────────────────

    @Test
    fun `generateShapeTriple - returns 3 shapes`() {
        val shapes = GameEngine.generateShapeTriple(Random(42))
        assertEquals(3, shapes.size)
        shapes.forEach { shape ->
            assertTrue(shape.cells.isNotEmpty())
            assertTrue(shape.color != BlockColor.NONE)
        }
    }

    @Test
    fun `generateShapeTriple - deterministic with seeded random`() {
        val a = GameEngine.generateShapeTriple(Random(123))
        val b = GameEngine.generateShapeTriple(Random(123))
        assertEquals(a, b)
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
