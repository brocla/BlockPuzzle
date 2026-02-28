package com.blockpuzzle.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Wooden/warm theme for Block Puzzle.
 * Always dark — the board is a dark wood background.
 * No dynamic color; the palette is fixed for a consistent look.
 */
private val WoodColorScheme = darkColorScheme(
    primary = TextGold,
    secondary = TextCream,
    tertiary = TextGold,
    background = BoardDark,
    surface = BoardMedium,
    onPrimary = BoardDark,
    onSecondary = TextCream,
    onTertiary = BoardDark,
    onBackground = TextCream,
    onSurface = TextCream
)

@Composable
fun BlockPuzzleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WoodColorScheme,
        typography = Typography,
        content = content
    )
}
