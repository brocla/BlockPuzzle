package com.blockpuzzle.model

/**
 * A puzzle piece defined by a list of cell offsets relative to an origin (0,0).
 * For example, an L-shape might have cells [(0,0), (1,0), (2,0), (2,1)].
 */
data class Shape(
    val cells: List<CellOffset>,
    val color: BlockColor
) {
    /** Bounding box width (columns). */
    val width: Int get() = cells.maxOf { it.col } + 1

    /** Bounding box height (rows). */
    val height: Int get() = cells.maxOf { it.row } + 1
}

/**
 * A (row, col) offset within a shape, relative to the shape's top-left origin.
 */
data class CellOffset(val row: Int, val col: Int)
