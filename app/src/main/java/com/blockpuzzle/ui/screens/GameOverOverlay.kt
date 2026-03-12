package com.blockpuzzle.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.blockpuzzle.ui.theme.BlockPuzzleTheme
import com.blockpuzzle.ui.theme.BoardDark
import com.blockpuzzle.ui.theme.TextCream
import com.blockpuzzle.ui.theme.TextGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun GameOverOverlay(
    score: Int,
    highScore: Int,
    onNewGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isNewHighScore = score >= highScore && score > 0

    // Scrim fade-in: alpha 0 → 0.7 over 400ms
    val scrimAlpha = remember { Animatable(0f) }
    // Card entrance: alpha 0 → 1, translationY +60dp → 0dp over 400ms (100ms delay)
    val cardAlpha = remember { Animatable(0f) }
    val cardOffsetY = remember { Animatable(60f) }
    // Score count-up: 0 → 1 over 1s (starts after card is visible)
    val scoreProgress = remember { Animatable(0f) }
    // High score pulse: scale oscillates 1.0 → 1.15 → 1.0
    val highScorePulse = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        suspend fun Animatable<Float, *>.slideTo(
            target: Float, duration: Int = 400
        ) = animateTo(target, tween(duration, easing = FastOutSlowInEasing))

        launch { scrimAlpha.slideTo(0.7f) }
        launch {
            delay(100)
            launch { cardAlpha.slideTo(1f) }
            launch { cardOffsetY.slideTo(0f) }
        }
        // Score count-up starts after the card has slid in
        launch {
            delay(500)
            scoreProgress.slideTo(1f, duration = 1000)
            // After count-up finishes, start high score pulse
            if (isNewHighScore) {
                highScorePulse.animateTo(
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
        }
    }

    val displayedScore = (score * scoreProgress.value).roundToInt()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = scrimAlpha.value))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {} // Consume clicks so they don't pass through
            ),
        contentAlignment = Alignment.Center
    ) {
        // Confetti layer behind the card
        if (isNewHighScore && scoreProgress.value >= 1f) {
            ConfettiEffect()
        }

        Column(
            modifier = Modifier
                .graphicsLayer {
                    alpha = cardAlpha.value
                    translationY = cardOffsetY.value * density
                }
                .background(BoardDark, RoundedCornerShape(16.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Game Over",
                style = MaterialTheme.typography.headlineLarge,
                color = TextGold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Score: $displayedScore",
                style = MaterialTheme.typography.titleMedium,
                color = TextCream
            )

            if (isNewHighScore) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "New High Score!",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextGold,
                    modifier = Modifier.graphicsLayer {
                        scaleX = highScorePulse.value
                        scaleY = highScorePulse.value
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onNewGame,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TextGold,
                    contentColor = BoardDark
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Play Again",
                    style = MaterialTheme.typography.titleMedium,
                    color = BoardDark
                )
            }
        }
    }
}

// ── Confetti ──

internal data class ConfettiParticle(
    val x: Float,         // 0..1 horizontal position
    val speed: Float,     // fall speed multiplier
    val size: Float,      // rect size in dp
    val color: Color,
    val wobbleSpeed: Float,
    val wobbleAmp: Float, // horizontal wobble amplitude
    val rotation: Float,  // initial rotation
    val landingY: Float = 1f // 0..1 y-coordinate where particle comes to rest
)

internal val confettiColors = listOf(
    Color(0xFFFFD54F), // Gold
    Color(0xFFE53935), // Ruby
    Color(0xFF1E88E5), // Sapphire
    Color(0xFF43A047), // Emerald
    Color(0xFF8E24AA), // Amethyst
    Color(0xFFFB8C00), // Tangerine
    Color(0xFFFFFFFF), // White
)

@Composable
internal fun ConfettiEffect() {
    AccumulatingConfetti(trigger = 1, reset = 0)
}

private const val NUM_COLUMNS = 25
private const val AVG_SIZE_DP = 7f // midpoint of 4..10 range
private const val SLOT_HEIGHT = (AVG_SIZE_DP * 1.4f) / 800f // screen fraction per stacked piece

/** Generate a batch of 50 confetti particles with landing positions offset by existing column heights. */
private fun generateBatch(columnCounts: IntArray): List<ConfettiParticle> {
    val rng = Random(System.nanoTime())
    val raw = List(50) {
        ConfettiParticle(
            x = rng.nextFloat(),
            speed = 0.5f + rng.nextFloat() * 0.8f,
            size = 4f + rng.nextFloat() * 6f,
            color = confettiColors[rng.nextInt(confettiColors.size)],
            wobbleSpeed = 1.5f + rng.nextFloat() * 2f,
            wobbleAmp = 0.01f + rng.nextFloat() * 0.03f,
            rotation = rng.nextFloat() * 360f
        )
    }
    val sorted = raw.sortedByDescending { it.speed }
    return sorted.map { p ->
        val col = (p.x * NUM_COLUMNS).toInt().coerceIn(0, NUM_COLUMNS - 1)
        val slot = columnCounts[col]
        columnCounts[col]++
        p.copy(landingY = 1f - slot * SLOT_HEIGHT)
    }
}

/**
 * Confetti that accumulates across multiple triggers. Each trigger adds a new falling batch;
 * landed particles persist until [reset] changes (new game).
 */
@Composable
internal fun AccumulatingConfetti(trigger: Int, reset: Int) {
    val density = LocalDensity.current

    // Global column heights — tracks how high the pile is in each column
    val columnCounts = remember { IntArray(NUM_COLUMNS) }

    // Landed particles from all previous batches (persistent pile)
    val landedParticles = remember { mutableStateListOf<ConfettiParticle>() }

    // Current falling batch + its animation progress
    var fallingBatch by remember { mutableStateOf<List<ConfettiParticle>>(emptyList()) }
    val fallProgress = remember { Animatable(0f) }

    // Reset: clear everything on new game
    LaunchedEffect(reset) {
        columnCounts.fill(0)
        landedParticles.clear()
        fallingBatch = emptyList()
        fallProgress.snapTo(0f)
    }

    // New trigger: generate a new batch and animate it
    LaunchedEffect(trigger) {
        if (trigger == 0) return@LaunchedEffect
        val batch = generateBatch(columnCounts)
        fallingBatch = batch
        fallProgress.snapTo(0f)
        fallProgress.animateTo(
            targetValue = 2f,
            animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
        )
        // Animation done — move all to landed pile
        landedParticles.addAll(batch)
        fallingBatch = emptyList()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Draw landed pile (static)
        for (p in landedParticles) {
            val px = p.x * size.width
            val py = p.landingY * size.height
            val sizePx = with(density) { p.size.dp.toPx() }
            drawRect(
                color = p.color,
                topLeft = Offset(px - sizePx / 2, py - sizePx / 2),
                size = Size(sizePx, sizePx * 1.4f)
            )
        }

        // Draw falling batch (animated)
        val t = fallProgress.value
        for (p in fallingBatch) {
            val particleT = t * p.speed
            val fallingY = -0.1f + particleT * 1.5f
            val landed = fallingY >= p.landingY
            val y = fallingY.coerceAtMost(p.landingY)
            if (y < -0.1f) continue
            val wobble = if (landed) 0f
                else kotlin.math.sin(particleT * p.wobbleSpeed * Math.PI.toFloat() * 2f) * p.wobbleAmp
            val x = p.x + wobble

            val px = x * size.width
            val py = y * size.height
            val sizePx = with(density) { p.size.dp.toPx() }

            drawRect(
                color = p.color,
                topLeft = Offset(px - sizePx / 2, py - sizePx / 2),
                size = Size(sizePx, sizePx * 1.4f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GameOverPreview() {
    BlockPuzzleTheme {
        GameOverOverlay(score = 1250, highScore = 3400, onNewGame = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun GameOverHighScorePreview() {
    BlockPuzzleTheme {
        GameOverOverlay(score = 3500, highScore = 3400, onNewGame = {})
    }
}
