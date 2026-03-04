package com.blockpuzzle.model

/** A read-only 2-D grid of cells. */
typealias Grid = List<List<Cell>>

/**
 * Complete game state — everything needed to render the UI and resume a game.
 */
data class GameState(
    val grid: Grid = emptyGrid(),
    val currentShapes: List<Shape?> = listOf(null, null, null),
    val score: Int = 0,
    val highScore: Int = 0,
    val isGameOver: Boolean = false,
    /** Cells currently playing the clear animation. Non-empty blocks new placements. */
    val clearingCells: Set<CellOffset> = emptySet()
) {
    companion object {
        const val GRID_SIZE = 8

        fun emptyGrid(): Grid =
            List(GRID_SIZE) { List(GRID_SIZE) { Cell() } }
    }
}
