package com.blockpuzzle.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
        ScoreBar(score = 1250, highScore = 3400)
    }
}
