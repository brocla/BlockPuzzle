package com.blockpuzzle.ui.theme

import androidx.compose.ui.graphics.Color
import com.blockpuzzle.model.BlockColor

// ── Board & background (dark walnut) ──
val BoardDark = Color(0xFF3E2723)       // Dark walnut — app background
val BoardMedium = Color(0xFF5D4037)     // Medium wood — grid board
val BoardLight = Color(0xFF795548)      // Lighter wood — empty cells
val GridLine = Color(0xFF4E342E)        // Subtle grid lines
val CellInset = Color(0xFF4A3228)       // Empty cell shadow/inset

// ── Text ──
val TextCream = Color(0xFFFFF8E1)       // Score, labels
val TextGold = Color(0xFFFFD54F)        // Highlighted numbers

// ── Ghost/preview overlay ──
val GhostValid = Color(0x4400C853)      // Green tint for valid placement
val GhostInvalid = Color(0x44FF1744)    // Red tint for invalid placement

// ── Block color palettes ──

enum class ColorPalette { JEWEL, VIVID, EARTHY }

/** Active palette — change this to switch all block colors. */
var activePalette: ColorPalette = ColorPalette.JEWEL

private data class PaletteColors(
    val red: Color,
    val blue: Color,
    val green: Color,
    val purple: Color,
    val orange: Color
)

private val JewelPalette = PaletteColors(
    red    = Color(0xFFE53935),  // Ruby
    blue   = Color(0xFF1E88E5),  // Sapphire
    green  = Color(0xFF43A047),  // Emerald
    purple = Color(0xFF8E24AA),  // Amethyst
    orange = Color(0xFFFB8C00)   // Tangerine
)

private val VividPalette = PaletteColors(
    red    = Color(0xFFFF5252),  // Coral
    blue   = Color(0xFF40C4FF),  // Sky
    green  = Color(0xFF76FF03),  // Lime
    purple = Color(0xFFD500F9),  // Violet
    orange = Color(0xFFFFAB40)   // Peach
)

private val EarthyPalette = PaletteColors(
    red    = Color(0xFFC62828),  // Brick
    blue   = Color(0xFF0277BD),  // Ocean
    green  = Color(0xFF558B2F),  // Moss
    purple = Color(0xFF6A1B9A),  // Plum
    orange = Color(0xFFFF8F00)   // Amber
)

private fun currentPalette(): PaletteColors = when (activePalette) {
    ColorPalette.JEWEL -> JewelPalette
    ColorPalette.VIVID -> VividPalette
    ColorPalette.EARTHY -> EarthyPalette
}

/** Map model BlockColor to Compose Color using the active palette. */
fun BlockColor.toComposeColor(): Color {
    val p = currentPalette()
    return when (this) {
        BlockColor.NONE   -> BoardLight
        BlockColor.RED    -> p.red
        BlockColor.BLUE   -> p.blue
        BlockColor.GREEN  -> p.green
        BlockColor.PURPLE -> p.purple
        BlockColor.ORANGE -> p.orange
    }
}
