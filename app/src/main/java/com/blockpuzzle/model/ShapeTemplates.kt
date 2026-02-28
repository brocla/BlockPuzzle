package com.blockpuzzle.model

/**
 * Standard block puzzle shape templates (~20 shapes).
 * Each is defined as a list of (row, col) offsets with a placeholder color.
 * Color is assigned randomly when a shape triple is generated.
 */
object ShapeTemplates {

    private fun shape(vararg offsets: Pair<Int, Int>): List<CellOffset> =
        offsets.map { (r, c) -> CellOffset(r, c) }

    // --- Single ---
    val DOT = shape(0 to 0)

    // --- Lines (horizontal) ---
    val LINE_H2 = shape(0 to 0, 0 to 1)
    val LINE_H3 = shape(0 to 0, 0 to 1, 0 to 2)
    val LINE_H4 = shape(0 to 0, 0 to 1, 0 to 2, 0 to 3)

    // --- Lines (vertical) ---
    val LINE_V2 = shape(0 to 0, 1 to 0)
    val LINE_V3 = shape(0 to 0, 1 to 0, 2 to 0)
    val LINE_V4 = shape(0 to 0, 1 to 0, 2 to 0, 3 to 0)

    // --- Squares ---
    val SQUARE_2X2 = shape(0 to 0, 0 to 1, 1 to 0, 1 to 1)
    val SQUARE_3X3 = shape(
        0 to 0, 0 to 1, 0 to 2,
        1 to 0, 1 to 1, 1 to 2,
        2 to 0, 2 to 1, 2 to 2
    )

    // --- Small L-shapes (2x2 with one cell missing) ---
    val SMALL_L_BR = shape(0 to 0, 1 to 0, 1 to 1)           // ┘ bottom-right
    val SMALL_L_BL = shape(0 to 1, 1 to 0, 1 to 1)           // └ bottom-left
    val SMALL_L_TR = shape(0 to 0, 0 to 1, 1 to 0)           // ┐ top-right
    val SMALL_L_TL = shape(0 to 0, 0 to 1, 1 to 1)           // ┌ top-left

    // --- Large L-shapes (3x3 corner) ---
    val BIG_L_BR = shape(0 to 0, 1 to 0, 2 to 0, 2 to 1, 2 to 2)  // ┘
    val BIG_L_BL = shape(0 to 2, 1 to 2, 2 to 0, 2 to 1, 2 to 2)  // └
    val BIG_L_TR = shape(0 to 0, 0 to 1, 0 to 2, 1 to 0, 2 to 0)  // ┐
    val BIG_L_TL = shape(0 to 0, 0 to 1, 0 to 2, 1 to 2, 2 to 2)  // ┌

    // --- T-shapes ---
    val T_DOWN = shape(0 to 0, 0 to 1, 0 to 2, 1 to 1)       // T pointing down
    val T_UP = shape(0 to 1, 1 to 0, 1 to 1, 1 to 2)         // T pointing up
    val T_RIGHT = shape(0 to 0, 1 to 0, 1 to 1, 2 to 0)      // T pointing right
    val T_LEFT = shape(0 to 1, 1 to 0, 1 to 1, 2 to 1)       // T pointing left

    // --- S/Z shapes ---
    val S_HORIZONTAL = shape(0 to 1, 0 to 2, 1 to 0, 1 to 1) // S horizontal
    val S_VERTICAL = shape(0 to 0, 1 to 0, 1 to 1, 2 to 1)   // S vertical
    val Z_HORIZONTAL = shape(0 to 0, 0 to 1, 1 to 1, 1 to 2) // Z horizontal
    val Z_VERTICAL = shape(0 to 1, 1 to 0, 1 to 1, 2 to 0)   // Z vertical

    /** All available templates. */
    val ALL: List<List<CellOffset>> = listOf(
        DOT,
        LINE_H2, LINE_H3, LINE_H4,
        LINE_V2, LINE_V3, LINE_V4,
        SQUARE_2X2, SQUARE_3X3,
        SMALL_L_BR, SMALL_L_BL, SMALL_L_TR, SMALL_L_TL,
        BIG_L_BR, BIG_L_BL, BIG_L_TR, BIG_L_TL,
        T_DOWN, T_UP, T_RIGHT, T_LEFT,
        S_HORIZONTAL, S_VERTICAL, Z_HORIZONTAL, Z_VERTICAL
    )

    /** Colors available for random assignment (excludes NONE). */
    val COLORS: List<BlockColor> = BlockColor.entries.filter { it != BlockColor.NONE }
}
