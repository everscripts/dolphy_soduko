# Copilot instructions for dolphy_soduko

## Project at a glance

This repository is a Kotlin Android puzzle game built with Jetpack Compose. The app centers on a color-pouring gameplay loop where the state is managed by `GameViewModel`, while the actual rules and search logic live in pure domain classes under `domain/logic`. User preferences and persisted progress are stored through DataStore (`SettingsRepository`).

The repo-specific architecture rules are documented in `AI_ANDROID_GAME_RULES.md`; treat those as the primary behavioral guardrails for code changes:

- UI code must not contain business logic.
- Heavy puzzle solving must not run on `Dispatchers.Main`; use `Dispatchers.Default` for CPU-heavy search/solver work.
- Database and file I/O belong on `Dispatchers.IO`.
- Cancel stale coroutine jobs and do not swallow `CancellationException`.
- Keep state immutable and keep `StateFlow` as the single source of truth.
- Preserve the current puzzle semantics; moves are driven by bottle color matching and source-target constraints.

## Build, test, and lint commands

Run commands from the repository root.

- Build debug APK: `./gradlew :app:assembleDebug`
- Run the full unit test suite: `./gradlew :app:testDebugUnitTest`
- Run a single test class: `./gradlew :app:testDebugUnitTest --tests com.everscripts.dolphy_soduko.GameLogicTest`
- Run lint checks: `./gradlew :app:lintDebug`
- Broad project validation: `./gradlew :app:build`

On Windows, use `gradlew.bat` instead of `./gradlew`.

Important test note: `GameSolver.solve` is a suspend function. Any new or updated tests that call it must run inside a coroutine context (for example, with `runTest`) instead of invoking it directly from a plain JUnit method.

## High-level architecture

- `MainActivity.kt` is the app entry point. It wires the game view model, enables edge-to-edge behavior, starts platform services, and keeps audio/haptics/ad behavior synchronized with current app state.
- `presentation/game/GameViewModel.kt` is the central coordinator for evolving game state, navigation, level loading, hint generation, and reward actions.
- `presentation/game/` contains the Compose UI and screen-level state rendering.
- `domain/logic/` is the gameplay engine layer:
  - `PourRuleEngine.kt` validates legal pours and applies single-step state transitions.
  - `LevelGenerator.kt` builds level layouts and seeded daily challenge configurations.
  - `GameSolver.kt` searches for valid moves or hint paths without depending on Android UI classes.
- `model/` defines the domain objects used by the game, including bottles and color segments; keep them independent from UI composition.
- `data/repository/SettingsRepository.kt` persists settings and progression via DataStore (`Preferences`), including sound, skin, current level, stars, and ad-removal state.
- `util/` contains Android service wrappers for ads, audio, haptics, billing, and social sign-in.

## Key conventions specific to this repo

- Keep the package layout consistent: Compose screens under `presentation/*`, business logic under `domain/logic/*`, and persistent config under `data/repository/*`.
- Keep pure puzzle rules in `domain/logic` and do not push game logic into composables or UI callbacks.
- Treat `GameState` as immutable and prefer a single authoritative `StateFlow` rather than duplicating state across the UI and the ViewModel.
- Preserve existing puzzle semantics: moves are based on bottle color matching and source-target rules. Changing the bottle representation or pour behavior usually requires updates to both `PourRuleEngine` and related tests.
- Extend the targeted game logic tests in `app/src/test/java/com/everscripts/dolphy_soduko/GameLogicTest.kt` when changing puzzle rules or solver behavior instead of creating unrelated test coverage.
- Use the default Android app module `:app`; do not add extra app modules without a clear reason.
- Keep repository changes focused on the existing app structure and avoid introducing new architecture layers unless the current shape clearly cannot support the task.
