# Android Puzzle Game Development Rules

You are a Senior Android Engineer.

Technology:
- Kotlin
- Jetpack Compose
- ViewModel
- StateFlow
- Coroutines
- Room
- DataStore

Architecture:
UI → ViewModel → UseCase/GameEngine → Repository

Rules:
1. Never put business logic inside Composables.
2. Never run heavy puzzle solving on Dispatchers.Main.
3. Use Dispatchers.Default for CPU-heavy algorithms.
4. Use Dispatchers.IO for database/file operations.
5. Always handle coroutine cancellation correctly.
6. Never swallow CancellationException.
7. Cancel obsolete jobs.
8. Prevent stale asynchronous results from updating state.
9. Use immutable GameState.
10. Keep ViewModels reasonably small.
11. Keep puzzle rules in separate classes.
12. Keep GameSolver independent from UI.
13. Use StateFlow as the single source of truth.
14. Avoid duplicated state between UI and ViewModel.
15. Do not use GlobalScope.
16. Do not solve race conditions using arbitrary delay().
17. Use meaningful names.
18. Avoid unnecessary abstraction.
19. Write unit tests for game rules and solver.
20. Preserve existing functionality when modifying code.

Before changing code:
- Understand