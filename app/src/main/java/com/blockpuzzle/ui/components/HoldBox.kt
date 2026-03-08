package com.blockpuzzle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import com.blockpuzzle.model.Shape
import com.blockpuzzle.ui.theme.BoardMedium
import com.blockpuzzle.ui.theme.BoardLight

/**
 * Hold box — a single-shape container below the tray.
 * The player can drag a tray shape here for safekeeping,
 * then drag it back onto the grid whenever they like.
 */
@Composable
fun HoldBox(
    holdShape: Shape?,
    isDragging: Boolean,
    dimmed: Boolean,
    onDragStart: (Shape) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onDoubleTap: () -> Unit,
    onGloballyPositioned: (LayoutCoordinates) -> Unit,
    modifier: Modifier = Modifier
) {
    var positionInRoot by remember { mutableStateOf(Offset.Zero) }
    val shape = RoundedCornerShape(12.dp)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(BoardMedium, shape)
            .border(1.dp, BoardLight, shape)
            .padding(12.dp)
            .onGloballyPositioned { coords ->
                positionInRoot = coords.positionInRoot()
                onGloballyPositioned(coords)
            }
            .pointerInput(holdShape) {
                if (holdShape == null) return@pointerInput
                detectTapGestures(onDoubleTap = { onDoubleTap() })
            }
            .pointerInput(holdShape) {
                if (holdShape == null) return@pointerInput
                detectDragGestures(
                    onDragStart = { startOffset ->
                        onDragStart(holdShape)
                        onDrag(positionInRoot + startOffset)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        onDrag(positionInRoot + change.position)
                    },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragCancel
                )
            }
            .graphicsLayer {
                alpha = if (isDragging) 0f else 1f
            }
    ) {
        ShapePreview(shape = holdShape, dimmed = dimmed)
    }
}
