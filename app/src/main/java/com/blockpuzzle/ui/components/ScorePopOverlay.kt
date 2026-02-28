package com.blockpuzzle.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockpuzzle.viewmodel.ScorePop

private val GoldColor = Color(0xFFFFD700)
private val BrightGoldColor = Color(0xFFFFEA00)

/**
 * Displays animated "+N" score pops over the grid.
 * Each pop rises ~40dp and fades out over 800ms.
 *
 * @param pops Active score pops to animate.
 * @param gridOffset The grid's top-left pixel offset within its Canvas (padding).
 * @param cellSizePx Size of one grid cell in pixels.
 * @param onDismiss Called when a pop's animation completes.
 */
@Composable
fun ScorePopOverlay(
    pops: List<ScorePop>,
    gridOffset: Offset,
    cellSizePx: Float,
    onDismiss: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    for (pop in pops) {
        val progress = remember(pop.id) { Animatable(0f) }

        LaunchedEffect(pop.id) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800, easing = LinearEasing)
            )
            onDismiss(pop.id)
        }

        val riseDp = 40.dp * progress.value
        val alpha = 1f - progress.value

        // Convert grid row/col to dp offset within the grid composable
        val xPx = gridOffset.x + pop.centerCol * cellSizePx + cellSizePx / 2f
        val yPx = gridOffset.y + pop.centerRow * cellSizePx
        val xDp = with(density) { xPx.toDp() }
        val yDp = with(density) { yPx.toDp() } - riseDp

        val fontSize = if (pop.isBonus) 28.sp else 20.sp
        val color = if (pop.isBonus) BrightGoldColor else GoldColor
        val fontWeight = if (pop.isBonus) FontWeight.ExtraBold else FontWeight.Bold

        Box(
            modifier = modifier
                .offset(x = xDp, y = yDp)
                .graphicsLayer { this.alpha = alpha },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+${pop.points}",
                style = TextStyle(
                    color = color,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(1.5f, 1.5f),
                        blurRadius = 3f
                    )
                )
            )
        }
    }
}
