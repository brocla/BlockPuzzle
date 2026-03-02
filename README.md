# Block Puzzle

A block puzzle game for Android built with Kotlin and Jetpack Compose.

## Features

- Drag and drop block shapes onto an 8x8 grid
- Clear rows and columns to score points
- Score popups and confetti effects
- High score tracking with DataStore persistence
- Settings screen with customizable options
- Splash screen overlay

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (no XML layouts)
- **State Management:** ViewModel + Compose state
- **Persistence:** DataStore Preferences
- **Min SDK:** See `app/build.gradle.kts`
- **Build System:** Gradle with version catalog

## Project Structure

```
app/src/main/java/com/blockpuzzle/
├── data/           # Repositories (high scores, settings)
├── logic/          # Game engine
├── model/          # Data classes (shapes, cells, game state)
├── ui/
│   ├── components/ # Reusable UI components (grid, score bar, shape previews)
│   ├── screens/    # Full screens and overlays
│   └── theme/      # Colors, typography, theming
└── viewmodel/      # Game ViewModel
```

## Building

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle and run on an emulator or device

Or from the command line:

```bash
./gradlew assembleDebug
```

## Testing

```bash
./gradlew test
```

## Tools

- `icon_gen.py` — Python script to generate the app icon vector drawable
