# Implementation Plan - Undo Feature & Smart Hint Coin Handling

## Problem Statement
1. **Coin Deduction on No Hint**: Currently, hint requests deduct coins (`spendStars`) or free hints *before* verifying if a hint actually exists. If no hint is available, coins are lost.
2. **Missing Undo Feature**: When players get stuck or have no hints available, there is no way to undo moves.

## Proposed Changes

### Domain & ViewModel Layer
#### [MODIFY] [GameViewModel.kt](file:///C:/Users/SHAIL/AndroidStudioProjects/dolphy_soduko/app/src/main/java/com/everscripts/dolphy_soduko/presentation/game/GameViewModel.kt)
- Add a move history stack (`private val moveHistory = mutableListOf<List<Bottle>>()`) to track previous bottle states.
- Push the current bottle state to `moveHistory` before making a pour in `onBottleClick`.
- Implement `undo()` method to restore the last bottle state from `moveHistory`.
- Clear `moveHistory` when loading or resetting levels.
- Refactor `getHint()` / `requestHint()` so that coins/free hints are only deducted *after* `GameSolver.solve()` successfully finds a valid hint. If no hint is available, coins are **not** reduced, and state is updated.

### UI Layer
#### [MODIFY] [GameScreen.kt](file:///C:/Users/SHAIL/AndroidStudioProjects/dolphy_soduko/app/src/main/java/com/everscripts/dolphy_soduko/presentation/game/GameScreen.kt)
- Add an **Undo** action button in Row 2 of the action icons (next to Home / Hint / Refresh / Settings), enabling players to easily undo their steps.

## Verification Plan

### Automated Tests
- Build project (`app:assembleDebug`) to verify compilation.

### Manual Verification
- Test making a move and tapping the Undo button to verify state reverts correctly.
- Test requesting a hint when no hint is available to verify coins/free hints are not deducted.
