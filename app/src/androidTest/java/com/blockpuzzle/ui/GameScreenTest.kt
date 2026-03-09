package com.blockpuzzle.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.blockpuzzle.MainActivity
import com.blockpuzzle.model.BlockColor
import com.blockpuzzle.model.Cell
import com.blockpuzzle.model.GameState
import com.blockpuzzle.viewmodel.GameViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    // ── Test 1: Game screen renders with score labels ──

    @Test
    fun gameScreenShowsScoreAndBestLabels() {
        rule.waitForIdle()
        rule.onNodeWithText("Score").assertIsDisplayed()
        rule.onNodeWithText("Best").assertIsDisplayed()
        rule.onNodeWithText("0").assertIsDisplayed()
    }

    // ── Test 2: Settings screen opens ──

    @Test
    fun settingsIconOpensSettingsScreen() {
        rule.onNodeWithContentDescription("Settings").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Settings").assertIsDisplayed()
        rule.onNodeWithText("Haptic Feedback").assertIsDisplayed()
        rule.onNodeWithText("Fair Shapes").assertIsDisplayed()
        rule.onNodeWithText("Jewel").assertIsDisplayed()
        rule.onNodeWithText("Neon").assertIsDisplayed()
    }

    // ── Test 3: Settings back button returns to game ──

    @Test
    fun settingsBackButtonReturnsToGame() {
        rule.onNodeWithContentDescription("Settings").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Settings").assertIsDisplayed()

        rule.onNodeWithContentDescription("Back").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Score").assertIsDisplayed()
    }

    // ── Test 4: Settings toggles are interactive ──

    @Test
    fun settingsTogglesCanBeSwitched() {
        rule.onNodeWithContentDescription("Settings").performClick()
        rule.waitForIdle()

        val hapticToggle = rule.onNodeWithText("Haptic Feedback")
            .assertIsDisplayed()

        val fairToggle = rule.onNodeWithText("Fair Shapes")
            .assertIsDisplayed()

        hapticToggle.performClick()
        fairToggle.performClick()
        rule.waitForIdle()
    }

    // ── Test 5: Tray has three shape slots and hold box exists ──

    @Test
    fun trayShowsThreeShapeSlotsAndHoldBox() {
        rule.waitForIdle()
        rule.onNodeWithTag("tray_shape_0").assertIsDisplayed()
        rule.onNodeWithTag("tray_shape_1").assertIsDisplayed()
        rule.onNodeWithTag("tray_shape_2").assertIsDisplayed()
        rule.onNodeWithTag("hold_box").assertIsDisplayed()
    }

    // ── Test 6: Game Over overlay appears when no moves remain ──

    @Test
    fun gameOverOverlayAppearsWhenGameIsOver() {
        // Force game over state via ViewModel
        rule.runOnUiThread {
            val viewModel = ViewModelProvider(rule.activity)[GameViewModel::class.java]
            viewModel.setGameStateForTest(
                GameState(
                    grid = fullGrid(),
                    currentShapes = listOf(null, null, null),
                    score = 42,
                    isGameOver = true
                )
            )
        }

        // Wait for the 1500ms delay + overlay animation
        rule.waitUntil(timeoutMillis = 5000) {
            rule.onAllNodes(hasText("Game Over")).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithText("Game Over").assertIsDisplayed()
        rule.onNodeWithText("Play Again").assertIsDisplayed()
    }

    // ── Test 7: Play Again resets the game ──

    @Test
    fun playAgainResetsGameAfterGameOver() {
        // Force game over state
        rule.runOnUiThread {
            val viewModel = ViewModelProvider(rule.activity)[GameViewModel::class.java]
            viewModel.setGameStateForTest(
                GameState(
                    grid = fullGrid(),
                    currentShapes = listOf(null, null, null),
                    score = 99,
                    isGameOver = true
                )
            )
        }

        // Wait for Game Over overlay
        rule.waitUntil(timeoutMillis = 5000) {
            rule.onAllNodes(hasText("Play Again")).fetchSemanticsNodes().isNotEmpty()
        }

        // Tap Play Again
        rule.onNodeWithText("Play Again").performClick()
        rule.waitForIdle()

        // Game Over overlay should be gone, game screen should be back
        rule.onNodeWithText("Game Over").assertDoesNotExist()
        rule.onNodeWithText("Score").assertIsDisplayed()
        // Tray should be repopulated with three new shapes
        rule.onNodeWithTag("tray_shape_0").assertIsDisplayed()
        rule.onNodeWithTag("tray_shape_1").assertIsDisplayed()
        rule.onNodeWithTag("tray_shape_2").assertIsDisplayed()
    }

    // ── Test 8: Color palette selection persists across settings visits ──

    @Test
    fun paletteSelectionPersistsAcrossSettingsVisits() {
        // Open settings and select "Earthy"
        rule.onNodeWithContentDescription("Settings").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Earthy").performClick()
        rule.waitForIdle()

        // Go back to game
        rule.onNodeWithContentDescription("Back").performClick()
        rule.waitForIdle()

        // Reopen settings and verify Earthy is selected, Jewel is not
        rule.onNodeWithContentDescription("Settings").performClick()
        rule.waitForIdle()

        // The clickable Row merges semantics: text + RadioButton selected state
        rule.onNode(hasText("Earthy") and hasClickAction())
            .assertIsSelected()
        rule.onNode(hasText("Jewel") and hasClickAction())
            .assertIsNotSelected()

        // Restore default palette (Jewel) to not affect other tests
        rule.onNodeWithText("Jewel").performClick()
        rule.waitForIdle()
        rule.onNodeWithContentDescription("Back").performClick()
    }


    // ── Helpers ──

    /** Creates an 8×8 grid completely filled with colored cells. */
    private fun fullGrid() = List(8) {
        List(8) { Cell(filled = true, color = BlockColor.RED) }
    }
}
