package com.blockpuzzle.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ShapeTest {

    private fun shape(vararg offsets: Pair<Int, Int>, color: BlockColor = BlockColor.RED): Shape =
        Shape(offsets.map { (r, c) -> CellOffset(r, c) }, color)

    // ──────────────────────────── width / height ────────────────────────────

    @Test
    fun `single cell is 1x1`() {
        val dot = shape(0 to 0)
        assertEquals(1, dot.width)
        assertEquals(1, dot.height)
    }

    @Test
    fun `horizontal line width equals cell count`() {
        val line = shape(0 to 0, 0 to 1, 0 to 2, 0 to 3)
        assertEquals(4, line.width)
        assertEquals(1, line.height)
    }

    @Test
    fun `vertical line height equals cell count`() {
        val line = shape(0 to 0, 1 to 0, 2 to 0)
        assertEquals(1, line.width)
        assertEquals(3, line.height)
    }

    @Test
    fun `L-shape has correct bounding box`() {
        // ┘ shape: (0,0), (1,0), (2,0), (2,1), (2,2)
        val bigL = shape(0 to 0, 1 to 0, 2 to 0, 2 to 1, 2 to 2)
        assertEquals(3, bigL.width)
        assertEquals(3, bigL.height)
    }

    @Test
    fun `2x2 square is 2x2`() {
        val sq = shape(0 to 0, 0 to 1, 1 to 0, 1 to 1)
        assertEquals(2, sq.width)
        assertEquals(2, sq.height)
    }

    @Test
    fun `non-square rectangle has correct dimensions`() {
        // 2 rows, 3 cols
        val rect = shape(0 to 0, 0 to 1, 0 to 2, 1 to 0, 1 to 1, 1 to 2)
        assertEquals(3, rect.width)
        assertEquals(2, rect.height)
    }
}
