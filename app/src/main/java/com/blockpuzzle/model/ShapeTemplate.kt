package com.blockpuzzle.model

/**
 * A shape definition with metadata for generation.
 * Templates define the canonical orientation; random rotations
 * are applied when placing shapes in the tray.
 */
data class ShapeTemplate(
    val name: String,
    val cells: List<CellOffset>,
    val rotations: Int,
    val weight: Float = 1f
) {
    fun toShape(color: BlockColor): Shape = Shape(cells = cells, color = color)
}
