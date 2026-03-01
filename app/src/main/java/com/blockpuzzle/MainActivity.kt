package com.blockpuzzle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blockpuzzle.ui.screens.GameScreen
import com.blockpuzzle.ui.screens.SplashOverlay
import com.blockpuzzle.ui.theme.BlockPuzzleTheme
import com.blockpuzzle.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlockPuzzleTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    val viewModel: GameViewModel = viewModel()
                    Box(modifier = Modifier.fillMaxSize()) {
                        GameScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                        var showSplash by rememberSaveable { mutableStateOf(true) }
                        if (showSplash) {
                            SplashOverlay(onFinished = { showSplash = false })
                        }
                    }
                }
            }
        }
    }
}
