package com.blockpuzzle.model

/**
 * A single cell on the 8x8 grid.
 */
data class Cell(
    val filled: Boolean = false,
    val color: BlockColor = BlockColor.NONE
)
