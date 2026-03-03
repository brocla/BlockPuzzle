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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blockpuzzle.data.SettingsRepository
import com.blockpuzzle.ui.screens.GameScreen
import com.blockpuzzle.ui.screens.SettingsScreen
import com.blockpuzzle.ui.screens.SplashOverlay
import com.blockpuzzle.ui.theme.BlockPuzzleTheme
import com.blockpuzzle.ui.theme.ColorPalette
import com.blockpuzzle.ui.theme.activePalette
import com.blockpuzzle.viewmodel.GameViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsRepo = remember { SettingsRepository(this@MainActivity) }
            val hapticEnabled by settingsRepo.hapticEnabledFlow.collectAsState(initial = false)
            val palette by settingsRepo.paletteFlow.collectAsState(initial = ColorPalette.JEWEL)
            val scope = rememberCoroutineScope()

            // Sync persisted palette to observable global state
            activePalette = palette

            BlockPuzzleTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    val viewModel: GameViewModel = viewModel()

                    // Sync haptic setting to ViewModel
                    LaunchedEffect(hapticEnabled) {
                        viewModel.hapticEnabled = hapticEnabled
                    }

                    var showSettings by rememberSaveable { mutableStateOf(false) }

                    Box(modifier = Modifier.fillMaxSize()) {
                        GameScreen(
                            viewModel = viewModel,
                            onSettingsClick = { showSettings = true },
                            modifier = Modifier.padding(innerPadding)
                        )

                        if (showSettings) {
                            SettingsScreen(
                                hapticEnabled = hapticEnabled,
                                selectedPalette = palette,
                                onHapticToggle = { enabled ->
                                    scope.launch { settingsRepo.saveHapticEnabled(enabled) }
                                },
                                onPaletteSelect = { selected ->
                                    activePalette = selected
                                    scope.launch { settingsRepo.savePalette(selected) }
                                },
                                onBack = { showSettings = false }
                            )
                        }

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
