package com.blockpuzzle.model

/**
 * Canonical shape templates — 15 unique shapes.
 * Rotational variants are generated at runtime via [Shape.rotateCW].
 * Each template specifies how many unique orientations it has.
 */
object ShapeTemplates {

    private fun cells(vararg offsets: Pair<Int, Int>): List<CellOffset> =
        offsets.map { (r, c) -> CellOffset(r, c) }

    val DOT = ShapeTemplate(
        name = "Dot",
        cells = cells(0 to 0),
        rotations = 1
    )

    val LINE_2 = ShapeTemplate(
        name = "Line 2",
        cells = cells(0 to 0, 0 to 1),
        rotations = 2
    )

    val LINE_3 = ShapeTemplate(
        name = "Line 3",
        cells = cells(0 to 0, 0 to 1, 0 to 2),
        rotations = 2
    )

    val LINE_4 = ShapeTemplate(
        name = "Line 4",
        cells = cells(0 to 0, 0 to 1, 0 to 2, 0 to 3),
        rotations = 2
    )

    val SQUARE_2X2 = ShapeTemplate(
        name = "Square 2x2",
        cells = cells(0 to 0, 0 to 1, 1 to 0, 1 to 1),
        rotations = 1
    )

    val SQUARE_3X3 = ShapeTemplate(
        name = "Square 3x3",
        cells = cells(
            0 to 0, 0 to 1, 0 to 2,
            1 to 0, 1 to 1, 1 to 2,
            2 to 0, 2 to 1, 2 to 2
        ),
        rotations = 1
    )

    val SMALL_DIAG = ShapeTemplate(
        name = "Small Diagonal",
        cells = cells(0 to 0, 1 to 1),
        rotations = 2
    )

    val SMALL_L = ShapeTemplate(
        name = "Small L",
        cells = cells(0 to 0, 1 to 0, 1 to 1),
        rotations = 4
    )

    val BIG_L = ShapeTemplate(
        name = "Big L",
        cells = cells(0 to 0, 1 to 0, 2 to 0, 2 to 1, 2 to 2),
        rotations = 4
    )

    val T_SHAPE = ShapeTemplate(
        name = "T-Shape",
        cells = cells(0 to 0, 0 to 1, 0 to 2, 1 to 1),
        rotations = 4
    )

    val S_SHAPE = ShapeTemplate(
        name = "S-Shape",
        cells = cells(0 to 1, 0 to 2, 1 to 0, 1 to 1),
        rotations = 2
    )

    val Z_SHAPE = ShapeTemplate(
        name = "Z-Shape",
        cells = cells(0 to 0, 0 to 1, 1 to 1, 1 to 2),
        rotations = 2
    )

    val TOMAHAWK_RIGHT = ShapeTemplate(
        name = "Tomahawk Right",
        cells = cells(0 to 0, 0 to 1, 0 to 2, 1 to 2),
        rotations = 4
    )

    val TOMAHAWK_LEFT = ShapeTemplate(
        name = "Tomahawk Left",
        cells = cells(0 to 0, 0 to 1, 0 to 2, 1 to 0),
        rotations = 4
    )

    val RECT_2X3 = ShapeTemplate(
        name = "Rectangle 2x3",
        cells = cells(
            0 to 0, 0 to 1, 0 to 2,
            1 to 0, 1 to 1, 1 to 2
        ),
        rotations = 4
    )

    /** All available templates (15 canonical shapes). */
    val ALL: List<ShapeTemplate> = listOf(
        DOT,
        LINE_2, LINE_3, LINE_4,
        SQUARE_2X2, SQUARE_3X3,
        SMALL_DIAG,
        SMALL_L, BIG_L,
        T_SHAPE,
        S_SHAPE, Z_SHAPE,
        TOMAHAWK_RIGHT, TOMAHAWK_LEFT,
        RECT_2X3
    )

    /** Colors available for random assignment (excludes NONE). */
    val COLORS: List<BlockColor> = BlockColor.entries.filter { it != BlockColor.NONE }
}
