package com.blockpuzzle.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.blockpuzzle.model.BlockColor
import com.blockpuzzle.model.ColorPalette

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

/** Active palette — change this to switch all block colors. */
var activePalette by mutableStateOf(ColorPalette.JEWEL)

data class PaletteColors(
    val red: Color,
    val blue: Color,
    val green: Color,
    val purple: Color,
    val orange: Color
) {
    fun toList(): List<Color> = listOf(red, blue, green, purple, orange)
}

val JewelPalette = PaletteColors(
    red    = Color(0xFFE53935),  // Ruby
    blue   = Color(0xFF1E88E5),  // Sapphire
    green  = Color(0xFF43A047),  // Emerald
    purple = Color(0xFF8E24AA),  // Amethyst
    orange = Color(0xFFFB8C00)   // Tangerine
)

val CoolMinimalPalette = PaletteColors(
    red    = Color(0xFFFF2E63),  // Neon Pink
    blue   = Color(0xFF08D9D6),  // Aqua Cyan
    green  = Color(0xFF252A34),  // Deep Space Gray
    purple = Color(0xFFFCE38A),  // Soft Neon Yellow
    orange = Color(0xFFFF9F1C)   // Tangerine Orange
)

val EarthyPalette = PaletteColors(
    red    = Color(0xFFCB997E),  // Clay Brown
    blue   = Color(0xFFB7B7A4),  // Weathered Stone
    green  = Color(0xFF6B705C),  // Moss Green
    purple = Color(0xFFA5A58D),  // Sage
    orange = Color(0xFFFFE8D6)   // Sand Cream
)

val PastelPalette = PaletteColors(
    red    = Color(0xFFF48FB1),  // Rose
    blue   = Color(0xFF81D4FA),  // Baby blue
    green  = Color(0xFFA5D6A7),  // Mint
    purple = Color(0xFFCE93D8),  // Lavender
    orange = Color(0xFFFFCC80)   // Peach
)

val NeonPalette = PaletteColors(
    red    = Color(0xFFFF2E63),  // Neon Pink
    blue   = Color(0xFF08D9D6),  // Aqua Cyan
    green  = Color(0xFF252A34),  // Deep Space Gray
    purple = Color(0xFFFCE38A),  // Soft Neon Yellow
    orange = Color(0xFFFF9F1C)   // Tangerine Orange
)

val WoodPalette = PaletteColors(
    red    = Color(0xFFEED9C4),  // Pale Birch
    blue   = Color(0xFFD2A679),  // Honey Oak
    green  = Color(0xFFA97458),  // Chestnut Brown
    purple = Color(0xFF6B4F3A),  // Walnut Grain
    orange = Color(0xFF3E2C1C)   // Burnt Umber
)

fun paletteOf(palette: ColorPalette): PaletteColors = when (palette) {
    ColorPalette.JEWEL -> JewelPalette
    ColorPalette.COOL_MINIMAL -> CoolMinimalPalette
    ColorPalette.EARTHY -> EarthyPalette
    ColorPalette.PASTEL -> PastelPalette
    ColorPalette.NEON -> NeonPalette
    ColorPalette.WOOD -> WoodPalette
}

private fun currentPalette(): PaletteColors = paletteOf(activePalette)

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
