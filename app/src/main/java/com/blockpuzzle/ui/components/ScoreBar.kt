package com.blockpuzzle.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.blockpuzzle.ui.theme.BlockPuzzleTheme
import com.blockpuzzle.ui.theme.TextCream
import com.blockpuzzle.ui.theme.TextGold

@Composable
fun ScoreBar(
    score: Int,
    highScore: Int,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScoreColumn(label = "Score", value = score)
        ScoreColumn(label = "Best", value = highScore)
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = TextCream
            )
        }
    }
}

@Composable
private fun ScoreColumn(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = TextCream
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineLarge,
            color = TextGold
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF3E2723)
@Composable
private fun ScoreBarPreview() {
    BlockPuzzleTheme {
        ScoreBar(score = 1250, highScore = 3400, onSettingsClick = {})
    }
}
