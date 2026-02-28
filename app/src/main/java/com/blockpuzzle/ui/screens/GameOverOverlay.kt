package com.blockpuzzle.ui.screens

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.blockpuzzle.ui.theme.TextGold
import com.blockpuzzle.ui.theme.BlockPuzzleTheme
import com.blockpuzzle.ui.theme.BoardDark
import com.blockpuzzle.ui.theme.TextCream
import com.blockpuzzle.ui.theme.TextGold

@Composable
fun GameOverOverlay(
    score: Int,
    highScore: Int,
    onNewGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {} // Consume clicks so they don't pass through
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
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
                text = "Score: $score",
                style = MaterialTheme.typography.titleMedium,
                color = TextCream
            )

            if (score >= highScore && score > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "New High Score!",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextGold
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
                    style = MaterialTheme.typography.titleMedium
                )
            }
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
