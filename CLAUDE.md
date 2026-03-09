# Nana's Block Puzzle — Claude Code Guide

## Build & Test Commands

```bash
# Build (JAVA_HOME is set as a system env var)
./gradlew assembleDebug

# Unit tests (pure JVM, no device needed)
./gradlew testDebugUnitTest

# UI tests (requires connected device or emulator)
./gradlew connectedAndroidTest

# Build test APK only (fast compile check)
./gradlew assembleDebugAndroidTest
```

- DEX format restriction: instrumented test method names must be camelCase (no backtick names with spaces)
- App stays on device after tests via `android.injected.androidTest.leaveApksInstalledAfterRun=true` in gradle.properties

## Architecture

MVVM with Jetpack Compose. No navigation library — screen switching is a boolean flag in MainActivity.

- **Model layer** (`model/`): `GameState` (data class with grid, shapes, score, holdShape), `Shape`, `ShapeTemplate`, `Cell`, `BlockColor`, `ColorPalette`
- **Logic** (`logic/GameEngine.kt`): Pure functions — `canPlace`, `placeShape`, `findCompleteLines`, `clearLines`, `isGameOver`, `generateShapeTriple`
- **ViewModel** (`viewmodel/GameViewModel.kt`): Owns `_gameState: MutableStateFlow<GameState>` and `_dragState: MutableStateFlow<DragState>`. DragState is separate for per-frame performance (drag updates don't trigger full game state recomposition)
- **Data** (`data/`): DataStore Preferences for settings and JSON serialization for game state persistence
- **UI** (`ui/`): Compose screens and components. Grid is Canvas-based, shapes use Canvas in ShapePreview

## Compose Stability

Compose stability configured via `app/compose-stability.conf` — add new model/viewmodel data classes there to keep them free of Compose imports while still getting recomposition skipping.

## Key Patterns

- `GameState` is immutable; always use `_gameState.update { it.copy(...) }` — never mutate in place
- Side effects (coroutine launches, repo calls) must happen OUTSIDE `StateFlow.update{}` lambdas
- `DragSource` enum (TRAY or HOLD) tracks where a drag originated
- Hold box drop detection uses bounding-box overlap (`Rect.overlaps()`), not point-in-rect, because the floating shape is lifted above the finger
- Shape lift offset: 96dp + 1 cell above finger position
- Tray replenishes when all 3 shapes are placed (or moved to hold). Game over checks both tray AND hold shapes
- `generateShapeTriple` has an `ensureFit` parameter (Fair Shapes setting) that retries until all shapes fit on the grid

## Grid & Scoring

- 8×8 grid of `Cell(filled, color: BlockColor)`
- Placement points via `placementPoints(shape)` based on cell count
- Line clear bonus: 10 × cells cleared × triangular multiplier (1 line = 1×, 2 = 3×, 3 = 6×)
- Clear animation: 550ms (flash white then fade). Game over overlay appears after 1500ms delay

## Color Palettes

Five palettes: JEWEL, EARTHY, PASTEL, NEON, WOOD. Defined in `ui/theme/Color.kt` with `paletteOf()` mapping. `activePalette` is a top-level observable var synced from DataStore.

## Settings Persistence

DataStore Preferences in `SettingsRepository`: haptic feedback, fair shapes (easy shapes), color palette. Game state persisted as JSON in `GameStateRepository` with backwards-compatible deserialization (`optJSONObject` for nullable fields).

## Testing

- Unit tests in `app/src/test/` — GameEngine logic tests with seeded `Random`
- UI tests in `app/src/androidTest/java/com/blockpuzzle/ui/GameScreenTest.kt`
- UI tests use `createAndroidComposeRule<MainActivity>()`
- `@VisibleForTesting setGameStateForTest()` on GameViewModel allows forcing state in UI tests
- TestTags: `tray_shape_0/1/2`, `hold_box`, `hold_box_shape`
- Splash screen (2.6s animation) auto-dismisses; existing tests handle it via `waitForIdle()`

## File Conventions

- One composable per file for screens and complex components
- Theme colors are top-level vals (e.g., `TextGold`, `BoardDark`, `BoardMedium`)
- Shape templates defined in `ShapeTemplates.kt` as `List<CellOffset>` patterns
