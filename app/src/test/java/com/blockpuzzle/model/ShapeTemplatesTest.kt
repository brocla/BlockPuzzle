package com.blockpuzzle.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShapeTemplatesTest {

    @Test
    fun `ALL contains 15 canonical templates`() {
        assertEquals(15, ShapeTemplates.ALL.size)
    }

    @Test
    fun `every template has at least one cell`() {
        for (template in ShapeTemplates.ALL) {
            assertTrue("Template ${template.name} should not be empty", template.cells.isNotEmpty())
        }
    }

    @Test
    fun `every template starts at row 0 and col 0 or higher`() {
        for (template in ShapeTemplates.ALL) {
            assertTrue(
                "All cell rows should be >= 0 in ${template.name}",
                template.cells.all { it.row >= 0 }
            )
            assertTrue(
                "All cell cols should be >= 0 in ${template.name}",
                template.cells.all { it.col >= 0 }
            )
        }
    }

    @Test
    fun `every template fits within 8x8 grid`() {
        for (template in ShapeTemplates.ALL) {
            val maxRow = template.cells.maxOf { it.row }
            val maxCol = template.cells.maxOf { it.col }
            assertTrue("${template.name} too tall: maxRow=$maxRow", maxRow < 8)
            assertTrue("${template.name} too wide: maxCol=$maxCol", maxCol < 8)
        }
    }

    @Test
    fun `no template has duplicate cells`() {
        for (template in ShapeTemplates.ALL) {
            val unique = template.cells.toSet()
            assertEquals(
                "${template.name} has duplicate cells",
                template.cells.size, unique.size
            )
        }
    }

    @Test
    fun `every template has valid rotation count`() {
        for (template in ShapeTemplates.ALL) {
            assertTrue(
                "${template.name} has invalid rotations: ${template.rotations}",
                template.rotations in listOf(1, 2, 4)
            )
        }
    }

    @Test
    fun `every template has positive weight`() {
        for (template in ShapeTemplates.ALL) {
            assertTrue(
                "${template.name} has non-positive weight: ${template.weight}",
                template.weight > 0f
            )
        }
    }

    @Test
    fun `COLORS excludes NONE`() {
        assertFalse(ShapeTemplates.COLORS.contains(BlockColor.NONE))
    }

    @Test
    fun `COLORS contains all non-NONE block colors`() {
        val expected = BlockColor.entries.filter { it != BlockColor.NONE }
        assertEquals(expected, ShapeTemplates.COLORS)
    }

    @Test
    fun `COLORS has 5 entries`() {
        assertEquals(5, ShapeTemplates.COLORS.size)
    }
}
