# Block Puzzle — Phases 8, 9, 10 Detailed Plans

## Current State
Phases 1-7 complete. The game is playable:
- 8x8 grid with wooden theme, 27 shape templates
- Drag and drop with floating shape above finger + ghost preview on grid
- Score tracking, line clearing with multiplier bonuses
- Game over detection and restart
- ViewModel architecture with StateFlow

---

## Phase 8 — Polish & Effects

### 8.1 Line Clear Animation
- When lines are cleared, don't remove them instantly
- Add a `clearingCells: Set<Pair<Int, Int>>` field to GameState (or a separate UI state)
- Animation sequence:
  1. Flash cleared cells white (150ms)
  2. Fade cleared cells to transparent (200ms)
  3. Remove cells from grid
- Use Compose `Animatable` or `LaunchedEffect` with delay
- During animation, block new shape placements

### 8.2 Score Pop Animation
- When points are scored, show floating "+N" text
- Position it near the center of the cleared area
- Animate: rise upward ~40dp while fading out over 800ms
- Use `AnimatedVisibility` or manual `Animatable` for offset + alpha
- Gold color for normal clears, brighter/larger for multi-line bonuses

### 8.3 Haptic Feedback
- Short vibration on successful shape placement (`HapticFeedbackType.LongPress`)
- Stronger double-tap vibration on line clear
- Use `LocalHapticFeedback.current.performHapticFeedback()`
- No vibration on invalid drop (shape snaps back silently)

### 8.4 Dim Unplayable Shapes
- After each placement, check which remaining tray shapes can still fit
- Pass `dimmed = true` to `ShapePreview` for shapes that can't fit anywhere
- Already supported — `ShapePreview` has a `dimmed` parameter that reduces alpha to 0.3
- Add `canFitAnywhere(grid, shape): Boolean` helper or reuse `isGameOver` logic per-shape

### 8.5 Smooth Shape Lift
- When drag starts, animate the tray shape scaling up slightly before it disappears
- Add a subtle drop shadow to the floating shape (`graphicsLayer { shadowElevation }`)
- Consider a slight scale-up (1.1x) on the floating shape to make it feel "lifted"

### 8.6 Game Over Screen Polish
- Fade-in the overlay (animate background alpha from 0 to 0.7)
- Scale-in the dialog card (0.8 → 1.0 with overshoot)
- Show final score with counting animation (0 → final over 1 second)
- If new high score, add a brief celebration effect (maybe pulsing gold text)

---

## Phase 9 — Persistence

### 9.1 Add DataStore Dependency
- Add to `libs.versions.toml`:
  ```
  datastorePreferences = "1.1.4"
  ```
  ```
  androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastorePreferences" }
  ```
- Add to `app/build.gradle.kts`:
  ```
  implementation(libs.androidx.datastore.preferences)
  ```

### 9.2 High Score Persistence (`data/HighScoreRepository.kt`)
- Create `data/` package
- Use `DataStore<Preferences>` with a single `intPreferencesKey("high_score")`
- Functions:
  - `suspend fun getHighScore(): Int`
  - `suspend fun saveHighScore(score: Int)`
  - `val highScoreFlow: Flow<Int>`
- Create the DataStore instance as a top-level singleton:
  ```kotlin
  val Context.dataStore by preferencesDataStore(name = "block_puzzle_prefs")
  ```

### 9.3 Wire to ViewModel
- Inject `DataStore` into `GameViewModel` (via `AndroidViewModel` or manual injection)
- On init: load high score from DataStore into GameState
- On score update: if new high score, save to DataStore
- High score survives app restarts

### 9.4 Save/Restore Game in Progress
- Serialize `GameState` to JSON using `kotlinx.serialization` or manual JSON
- Save on `onPause` / app backgrounding
- Restore on `onCreate` if saved state exists
- Clear saved state on game over (so restarting gives a fresh game)
- Fields to persist:
  - `grid` (8x8 array of color enum values)
  - `currentShapes` (3 nullable shape definitions)
  - `score`
  - `isGameOver`

### 9.5 Alternative: Skip Game State Persistence
- If complexity isn't worth it for a family game, skip 9.4
- Just persist high score (9.2-9.3)
- Starting a fresh game on app reopen is acceptable

---

## Phase 10 — Final Packaging & Distribution

### 10.1 App Icon
- Use Android Studio's **Image Asset Studio** (right-click `res` → New → Image Asset)
- Design: simple colored block arrangement on a dark wood background
- Generates all mipmap densities automatically
- Adaptive icon with foreground + background layers

### 10.2 App Name & Metadata
- Verify `strings.xml` has `app_name = "Block Puzzle"`
- Update `versionName` to "1.0.0" in `build.gradle.kts`
- Verify `AndroidManifest.xml` has no unnecessary permissions (should have none)

### 10.3 Splash Screen
- Android 12+ has built-in splash screen API
- Set `android:windowSplashScreenBackground` to dark wood color in `themes.xml`
- Set `android:windowSplashScreenAnimatedIcon` to the app icon
- Minimal code — just theme configuration

### 10.4 Build Signed APK
1. **Generate signing key**:
   - Android Studio → Build → Generate Signed Bundle / APK
   - Create new keystore (`.jks` file)
   - Store securely — needed for all future updates
2. **Build release APK**:
   - Select APK (not Bundle, since we're not using Play Store)
   - Sign with the new key
   - Enable minification (`isMinifyEnabled = true`) + R8 for smaller APK
   - Add ProGuard rules if needed (Compose usually needs none)
3. **Test the release APK**:
   - Install on a real device via `adb install`
   - Verify everything works (no debug-only behavior)

### 10.5 Distribution
- **Option A — Direct APK sharing** (simplest):
  - Email or chat the `.apk` file to family
  - Recipients enable "Install from unknown sources" in Settings
  - One-click install
- **Option B — Google Play internal testing** (more polished):
  - Create a Google Play Developer account ($25 one-time fee)
  - Upload as internal test track (up to 100 testers)
  - Testers get a link to install via Play Store
  - No public listing, no review process
  - Easier updates — just upload a new version

### 10.6 Pre-Ship Checklist
- [ ] All unit tests pass
- [ ] Game plays correctly on emulator
- [ ] Game plays correctly on a real device (if available)
- [ ] High score persists across restarts
- [ ] No crashes on rotation / backgrounding / foregrounding
- [ ] App icon looks good in launcher
- [ ] APK size is reasonable (should be under 10MB)
- [ ] No leftover debug logging or test data
