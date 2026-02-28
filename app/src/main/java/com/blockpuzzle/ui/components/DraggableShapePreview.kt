package com.blockpuzzle.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import com.blockpuzzle.model.Shape

/**
 * A shape in the tray that can be dragged.
 * When dragging starts, it becomes invisible (the floating overlay takes over).
 * Reports drag positions in root (window) coordinates.
 */
@Composable
fun DraggableShapePreview(
    shape: Shape?,
    onDragStart: (Shape) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    isDragging: Boolean,
    dimmed: Boolean = false,
    modifier: Modifier = Modifier
) {
    var positionInRoot by remember { mutableStateOf(Offset.Zero) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
            .onGloballyPositioned { coords ->
                positionInRoot = coords.positionInRoot()
            }
            .pointerInput(shape) {
                if (shape == null) return@pointerInput
                detectDragGestures(
                    onDragStart = { startOffset ->
                        onDragStart(shape)
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
                // Hide the tray shape while it's being dragged
                alpha = if (isDragging) 0f else 1f
            }
    ) {
        ShapePreview(shape = shape, dimmed = dimmed)
    }
}
