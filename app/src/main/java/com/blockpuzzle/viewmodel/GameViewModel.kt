package com.blockpuzzle.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blockpuzzle.logic.GameEngine
import com.blockpuzzle.model.GameState
import com.blockpuzzle.model.GameState.Companion.GRID_SIZE
import com.blockpuzzle.model.Shape
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** One-shot haptic feedback events emitted by the ViewModel. */
enum class HapticEvent { PLACE, LINE_CLEAR }

/** Ephemeral score pop info for the "+N" floating animation. */
data class ScorePop(
    val points: Int,
    /** Center row on the grid (fractional). */
    val centerRow: Float,
    /** Center col on the grid (fractional). */
    val centerCol: Float,
    /** True for line-clear bonuses (shown larger/brighter). */
    val isBonus: Boolean,
    /** Unique id so Compose can key animations. */
    val id: Long
)

/**
 * Drag state tracked separately from game state for performance —
 * drag offsets change every frame, game state changes only on drop.
 */
data class DragState(
    val shapeIndex: Int = -1,
    val shape: Shape? = null,
    /** Finger position in root (window) coordinates — used to position the floating shape. */
    val fingerRootOffset: Offset = Offset.Zero,
    val ghostRow: Int = -1,
    val ghostCol: Int = -1,
    val ghostValid: Boolean = false,
    /** Cells in rows/columns that would be cleared if the shape is dropped here. */
    val highlightCells: Set<Pair<Int, Int>> = emptySet()
)

class GameViewModel : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _dragState = MutableStateFlow(DragState())
    val dragState: StateFlow<DragState> = _dragState.asStateFlow()

    private val _hapticEvents = MutableSharedFlow<HapticEvent>(extraBufferCapacity = 4)
    val hapticEvents: SharedFlow<HapticEvent> = _hapticEvents.asSharedFlow()

    private val _scorePops = MutableStateFlow<List<ScorePop>>(emptyList())
    val scorePops: StateFlow<List<ScorePop>> = _scorePops.asStateFlow()
    private var nextPopId = 0L

    /** Called by the UI after a score pop animation finishes. */
    fun dismissScorePop(id: Long) {
        _scorePops.update { pops -> pops.filter { it.id != id } }
    }

    private fun emitScorePop(points: Int, centerRow: Float, centerCol: Float, isBonus: Boolean) {
        val pop = ScorePop(points, centerRow, centerCol, isBonus, nextPopId++)
        _scorePops.update { it + pop }
    }

    // Grid layout info from the composable — set by onGridLayout callback
    var gridScreenOffset: Offset = Offset.Zero
    var cellSizePx: Float = 0f

    init {
        startNewGame()
    }

    fun startNewGame() {
        _gameState.update {
            GameState(
                grid = GameState.emptyGrid(),
                currentShapes = GameEngine.generateShapeTriple(),
                score = 0,
                highScore = it.highScore
            )
        }
        _dragState.value = DragState()
    }

    /** Called when the player starts dragging a shape from the tray. */
    fun onDragStart(shapeIndex: Int, shape: Shape) {
        // Block drags while a clear animation is running
        if (_gameState.value.clearingCells.isNotEmpty()) return

        _dragState.value = DragState(
            shapeIndex = shapeIndex,
            shape = shape
        )
    }

    /**
     * Called every frame during drag.
     * @param fingerInRoot Finger position in root (window) coordinates.
     * @param fingerInGrid Finger position relative to the grid composable's top-left.
     * @param floatingShapeCenterYOffset Vertical offset (in px) from the finger to the
     *        center of the floating shape visual (negative = above finger).
     */
    fun onDrag(fingerInRoot: Offset, fingerInGrid: Offset, floatingShapeCenterYOffset: Float) {
        val drag = _dragState.value
        val shape = drag.shape ?: return

        // The offset points to the floating shape's top-left in grid-relative coords.
        // Horizontal: center on finger. Vertical: use the offset (shape top position).
        val visualX = fingerInGrid.x
        val visualY = fingerInGrid.y + floatingShapeCenterYOffset

        // Floating shape center in fractional grid coordinates
        val centerRow = (visualY - gridScreenOffset.y) / cellSizePx
        val centerCol = (visualX - gridScreenOffset.x) / cellSizePx

        // Place ghost so its center aligns with the floating shape center.
        // round() snaps at half-cell boundaries so the ghost follows closely.
        val adjRow = kotlin.math.round(centerRow - shape.height / 2f).toInt()
        val adjCol = kotlin.math.round(centerCol - shape.width / 2f).toInt()

        val grid = _gameState.value.grid
        val valid = adjRow >= 0 && adjCol >= 0 &&
            GameEngine.canPlace(grid, shape, adjRow, adjCol)

        // Compute which rows/columns would clear if shape is dropped here
        val highlight = if (valid) {
            val simGrid = GameEngine.placeShape(grid, shape, adjRow, adjCol)
            val lines = GameEngine.findCompleteLines(simGrid)
            if (!lines.isEmpty()) {
                val cells = mutableSetOf<Pair<Int, Int>>()
                for (r in lines.rows) {
                    for (c in 0 until GRID_SIZE) cells.add(r to c)
                }
                for (c in lines.cols) {
                    for (r in 0 until GRID_SIZE) cells.add(r to c)
                }
                cells
            } else emptySet()
        } else emptySet()

        _dragState.value = drag.copy(
            fingerRootOffset = fingerInRoot,
            ghostRow = adjRow,
            ghostCol = adjCol,
            ghostValid = valid,
            highlightCells = highlight
        )
    }

    /** Called when the player lifts their finger. */
    fun onDragEnd() {
        val drag = _dragState.value
        val shape = drag.shape ?: return

        // Block placement while a clear animation is running
        if (_gameState.value.clearingCells.isNotEmpty()) {
            _dragState.value = DragState()
            return
        }

        if (drag.ghostValid && drag.ghostRow >= 0 && drag.ghostCol >= 0) {
            // Place the shape
            val grid = _gameState.value.grid
            val newGrid = GameEngine.placeShape(grid, shape, drag.ghostRow, drag.ghostCol)

            // Score: placement points always awarded immediately
            val placementPts = GameEngine.placementPoints(shape)

            // Remove the placed shape from the tray
            val newShapes = _gameState.value.currentShapes.toMutableList()
            newShapes[drag.shapeIndex] = null

            // If all 3 shapes placed, generate a new triple
            val finalShapes = if (newShapes.all { it == null }) {
                GameEngine.generateShapeTriple()
            } else {
                newShapes
            }

            // Check for line clears
            val lines = GameEngine.findCompleteLines(newGrid)

            if (!lines.isEmpty()) {
                // Collect the set of cells that will be cleared
                val clearing = mutableSetOf<Pair<Int, Int>>()
                for (r in lines.rows) {
                    for (c in 0 until GRID_SIZE) clearing.add(r to c)
                }
                for (c in lines.cols) {
                    for (r in 0 until GRID_SIZE) clearing.add(r to c)
                }

                // Compute center of cleared area for score pop positioning
                val clearCenterRow = clearing.map { it.first }.average().toFloat()
                val clearCenterCol = clearing.map { it.second }.average().toFloat()

                // Haptic: placement tap
                if (HAPTIC_ENABLED) _hapticEvents.tryEmit(HapticEvent.PLACE)

                // Phase 1: show placed shape + mark clearing cells (triggers animation)
                _gameState.update {
                    it.copy(
                        grid = newGrid,
                        currentShapes = finalShapes,
                        score = it.score + placementPts,
                        clearingCells = clearing
                    )
                }

                // Phase 2: after animation delay, actually clear and award bonus
                viewModelScope.launch {
                    delay(CLEAR_ANIMATION_MS)

                    val clearResult = GameEngine.clearLines(newGrid, lines)
                    val newScore = _gameState.value.score + clearResult.points
                    val newHighScore = maxOf(newScore, _gameState.value.highScore)
                    val gameOver = GameEngine.isGameOver(clearResult.grid, finalShapes)

                    _gameState.update {
                        it.copy(
                            grid = clearResult.grid,
                            score = newScore,
                            highScore = newHighScore,
                            isGameOver = gameOver,
                            clearingCells = emptySet()
                        )
                    }

                    // Haptic: stronger buzz for line clear
                    if (HAPTIC_ENABLED) _hapticEvents.tryEmit(HapticEvent.LINE_CLEAR)

                    // Show score pop for line clear bonus
                    emitScorePop(clearResult.points, clearCenterRow, clearCenterCol, isBonus = true)
                }
            } else {
                // Haptic: placement tap
                if (HAPTIC_ENABLED) _hapticEvents.tryEmit(HapticEvent.PLACE)

                // No lines to clear — update immediately
                val newScore = _gameState.value.score + placementPts
                val newHighScore = maxOf(newScore, _gameState.value.highScore)
                val gameOver = GameEngine.isGameOver(newGrid, finalShapes)

                _gameState.update {
                    it.copy(
                        grid = newGrid,
                        currentShapes = finalShapes,
                        score = newScore,
                        highScore = newHighScore,
                        isGameOver = gameOver
                    )
                }

                // Show score pop for placement points
                val shapeCenterRow = drag.ghostRow + shape.height / 2f
                val shapeCenterCol = drag.ghostCol + shape.width / 2f
                emitScorePop(placementPts, shapeCenterRow, shapeCenterCol, isBonus = false)
            }
        }

        // Clear drag state
        _dragState.value = DragState()
    }

    companion object {
        const val CLEAR_ANIMATION_MS = 750L
        /** Set to true to enable haptic feedback on placement and line clear. */
        const val HAPTIC_ENABLED = false
    }

    /** Cancel drag without placing. */
    fun onDragCancel() {
        _dragState.value = DragState()
    }
}
