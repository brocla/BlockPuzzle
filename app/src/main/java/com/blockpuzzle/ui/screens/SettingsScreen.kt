package com.blockpuzzle.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.blockpuzzle.ui.theme.BoardDark
import com.blockpuzzle.ui.theme.ColorPalette
import com.blockpuzzle.ui.theme.TextCream
import com.blockpuzzle.ui.theme.TextGold
import com.blockpuzzle.ui.theme.paletteOf

@Composable
fun SettingsScreen(
    hapticEnabled: Boolean,
    selectedPalette: ColorPalette,
    onHapticToggle: (Boolean) -> Unit,
    onPaletteSelect: (ColorPalette) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BoardDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextCream
                    )
                }
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextCream
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Haptic toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Haptic Feedback",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextCream
                )
                Switch(
                    checked = hapticEnabled,
                    onCheckedChange = onHapticToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TextGold,
                        checkedTrackColor = TextGold.copy(alpha = 0.4f),
                        uncheckedThumbColor = TextCream.copy(alpha = 0.6f),
                        uncheckedTrackColor = TextCream.copy(alpha = 0.2f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Palette section
            Text(
                text = "Color Palette",
                style = MaterialTheme.typography.titleMedium,
                color = TextCream,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            ColorPalette.entries.forEach { palette ->
                PaletteRow(
                    palette = palette,
                    selected = palette == selectedPalette,
                    onClick = { onPaletteSelect(palette) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PaletteRow(
    palette: ColorPalette,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = paletteOf(palette).toList()
    val label = when (palette) {
        ColorPalette.JEWEL -> "Jewel"
        ColorPalette.VIVID -> "Vivid"
        ColorPalette.EARTHY -> "Earthy"
        ColorPalette.PASTEL -> "Pastel"
        ColorPalette.NEON -> "Neon"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = TextGold,
                unselectedColor = TextCream.copy(alpha = 0.6f)
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = TextCream,
            modifier = Modifier.width(72.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        // Color swatches
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            colors.forEach { color ->
                ColorSwatch(color = color, selected = selected)
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Color, selected: Boolean) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .then(
                if (selected) {
                    Modifier.border(2.dp, TextCream, RoundedCornerShape(4.dp))
                } else {
                    Modifier
                }
            )
    )
}
