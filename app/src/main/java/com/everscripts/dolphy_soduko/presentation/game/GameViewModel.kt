package com.everscripts.dolphy_soduko.presentation.game

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.everscripts.dolphy_soduko.data.repository.SettingsRepository
import com.everscripts.dolphy_soduko.domain.logic.GameSolver
import com.everscripts.dolphy_soduko.domain.logic.LevelGenerator
import com.everscripts.dolphy_soduko.domain.logic.PourRuleEngine
import com.everscripts.dolphy_soduko.model.Bottle
import com.everscripts.dolphy_soduko.presentation.theme.DolphySkin
import com.everscripts.dolphy_soduko.presentation.theme.GameSkin
import com.everscripts.dolphy_soduko.presentation.theme.SkinManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class Screen {
    HOME, GAME
}

data class PourAnimation(
    val sourceId: Int,
    val targetId: Int,
    val segmentsCount: Int
)

data class GameState(
    val level: Int = 1,
    val bottles: List<Bottle> = emptyList(),
    val selectedBottleId: Int? = null,
    val isWon: Boolean = false,
    val skin: GameSkin = DolphySkin,
    val sfxEnabled: Boolean = true,
    val bgmEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val isAdsRemoved: Boolean = false,
    val hint: GameSolver.Move? = null,
    val showAd: Boolean = false,
    val requestHintAd: Boolean = false,
    val pourAnimation: PourAnimation? = null,
    val isAnimating: Boolean = false,
    val waterColorHex: String = "FF2196F3",
    val isHintLoading: Boolean = false,
    val freeHintsUsed: Int = 0,
    val currentScreen: Screen = Screen.HOME,
    val exitEvent: Boolean = false,
    val isDailyChallenge: Boolean = false
)

class GameViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private lateinit var levelGenerator: LevelGenerator
    private var hintJob: Job? = null

    init {
        viewModelScope.launch {
            combine(
                settingsRepository.currentLevel,
                settingsRepository.activeSkin,
                settingsRepository.sfxEnabled,
                settingsRepository.bgmEnabled,
                settingsRepository.hapticsEnabled,
                settingsRepository.adsRemoved,
                settingsRepository.waterColor,
                settingsRepository.freeHintsUsed
            ) { args ->
                val level = args[0] as Int
                val skinName = args[1] as String
                val sfx = args[2] as Boolean
                val bgm = args[3] as Boolean
                val haptics = args[4] as Boolean
                val adsRemoved = args[5] as Boolean
                val waterColorHex = args[6] as String
                val hintsUsed = args[7] as Int
                
                _state.update { 
                    it.copy(
                        level = level,
                        skin = SkinManager.getSkin(skinName, Color(android.graphics.Color.parseColor("#$waterColorHex"))),
                        sfxEnabled = sfx,
                        bgmEnabled = bgm,
                        hapticsEnabled = haptics,
                        isAdsRemoved = adsRemoved,
                        waterColorHex = waterColorHex,
                        freeHintsUsed = hintsUsed
                    )
                }
                if (_state.value.bottles.isEmpty()) {
                    loadLevel(level)
                }
            }.collect()
        }
    }

    fun loadLevel(level: Int) {
        hintJob?.cancel()
        levelGenerator = LevelGenerator(seed = level.toLong())
        val bottles = levelGenerator.generate(level)
        _state.update { it.copy(
            level = level, 
            bottles = bottles, 
            isWon = false, 
            selectedBottleId = null, 
            hint = null, 
            isHintLoading = false,
            isDailyChallenge = false
        ) }
        viewModelScope.launch {
            settingsRepository.setLevel(level)
        }
    }

    fun loadDailyChallenge() {
        hintJob?.cancel()
        _state.update { it.copy(isAnimating = true) } // Show loading or prevent clicks
        
        viewModelScope.launch {
            // Use current day as seed for a new challenge every day
            val daySeed = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
            val bottles = withContext(Dispatchers.Default) {
                LevelGenerator(seed = daySeed).generateHardLevel()
            }
            
            _state.update { it.copy(
                bottles = bottles,
                isWon = false,
                selectedBottleId = null,
                hint = null,
                isHintLoading = false,
                isDailyChallenge = true,
                currentScreen = Screen.GAME,
                isAnimating = false
            ) }
        }
    }

    fun onBottleClick(bottleId: Int) {
        val currentState = _state.value
        if (currentState.isWon || currentState.isAnimating) return

        // Clear active hint and stop searching immediately on any interaction
        hintJob?.cancel()
        _state.update { it.copy(hint = null, isHintLoading = false) }

        if (currentState.selectedBottleId == null) {
            // Select bottle if not empty
            if (currentState.bottles.find { it.id == bottleId }?.isEmpty == false) {
                _state.update { it.copy(selectedBottleId = bottleId) }
            }
        } else if (currentState.selectedBottleId == bottleId) {
            // Deselect
            _state.update { it.copy(selectedBottleId = null) }
        } else {
            // Try pour
            val sourceId = currentState.selectedBottleId!!
            val sourceBottle = currentState.bottles.find { it.id == sourceId }!!
            val targetBottle = currentState.bottles.find { it.id == bottleId }!!

            if (PourRuleEngine.canPour(sourceBottle, targetBottle)) {
                val totalPourCount = PourRuleEngine.calculatePourCount(sourceBottle, targetBottle)
                
                viewModelScope.launch {
                    _state.update { it.copy(
                        isAnimating = true,
                        selectedBottleId = null
                    )}

                    var currentBottles = currentState.bottles
                    
                    repeat(totalPourCount) {
                        // Trigger animation for 1 fish
                        _state.update { it.copy(
                            pourAnimation = PourAnimation(sourceId, bottleId, 1)
                        )}
                        
                        delay(600) // Fish leap duration
                        
                        // Execute logic for 1 fish
                        currentBottles = PourRuleEngine.moveOne(currentBottles, sourceId, bottleId)
                        
                        _state.update { it.copy(
                            bottles = currentBottles,
                            pourAnimation = null
                        )}
                        
                        delay(50) // Small gap between fish
                    }
                    
                    val isWon = PourRuleEngine.isGameWon(currentBottles)
                    _state.update { it.copy(
                        isWon = isWon,
                        isAnimating = false,
                        hint = null
                    )}
                }
            } else {
                // Invalid move, just deselect or select new source if not empty
                if (currentState.bottles.find { it.id == bottleId }?.isEmpty == false) {
                    _state.update { it.copy(selectedBottleId = bottleId) }
                } else {
                    _state.update { it.copy(selectedBottleId = null) }
                }
            }
        }
    }

    fun nextLevel() {
        if (_state.value.isDailyChallenge) {
            exitGame()
            return
        }
        val nextLevel = _state.value.level + 1
        loadLevel(nextLevel)
        // Ads temporarily disabled
        /*
        if (!_state.value.isAdsRemoved && nextLevel % 3 == 0) {
            _state.update { it.copy(showAd = true) }
        }
        */
    }

    fun onAdShown() {
        _state.update { it.copy(showAd = false) }
    }

    fun requestHint() {
        val s = _state.value
        
        // Toggle Logic: If a hint is currently visible, click to hide it
        if (s.hint != null) {
            _state.update { it.copy(hint = null) }
            return
        }

        // Ads temporarily disabled: Always give hint
        getHint()
        
        /* 
        if (s.isAdsRemoved || s.freeHintsUsed < 5) {
            getHint()
            if (!s.isAdsRemoved) {
                viewModelScope.launch {
                    settingsRepository.incrementFreeHints()
                }
            }
        } else {
            _state.update { it.copy(requestHintAd = true) }
        }
        */
    }

    fun enterGame() {
        _state.update { it.copy(currentScreen = Screen.GAME) }
    }

    fun exitGame() {
        _state.update { it.copy(currentScreen = Screen.HOME) }
    }

    fun quitApp() {
        _state.update { it.copy(exitEvent = true) }
    }

    fun onHintAdShown() {
        _state.update { it.copy(requestHintAd = false) }
        getHint()
    }

    fun onHintAdDismissed() {
        _state.update { it.copy(requestHintAd = false) }
    }

    fun onHintAdFailed() {
        _state.update { it.copy(requestHintAd = false) }
    }

    fun resetLevel() {
        hintJob?.cancel()
        loadLevel(_state.value.level)
    }

    fun toggleSkin() {
        viewModelScope.launch {
            val nextSkin = if (_state.value.skin.name == "DOLPHY") "JELLYFISH" else "DOLPHY"
            settingsRepository.setActiveSkin(nextSkin)
        }
    }

    fun updateWaterColor(colorHex: String) {
        viewModelScope.launch {
            settingsRepository.setWaterColor(colorHex)
        }
    }

    fun toggleSfx(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.toggleSfx(enabled)
        }
    }

    fun toggleBgm(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.toggleBgm(enabled)
        }
    }

    fun toggleHaptics(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.toggleHaptics(enabled)
        }
    }

    fun getHint() {
        val currentBottles = _state.value.bottles
        if (currentBottles.isEmpty()) return

        Log.d("GameViewModel", "Getting hint...")
        hintJob?.cancel()
        _state.update { it.copy(isHintLoading = true, hint = null) }
        
        hintJob = viewModelScope.launch {
            try {
                val solution = withContext(Dispatchers.Default) {
                    GameSolver.solve(currentBottles)
                }
                
                if (solution != null && solution.isNotEmpty()) {
                    val hintMove = solution.first()
                    Log.d("GameViewModel", "Hint found: ${hintMove.fromId} -> ${hintMove.toId}")
                    _state.update { it.copy(hint = hintMove) }
                } else {
                    Log.d("GameViewModel", "No hint found or search limit reached")
                }
            } catch (e: CancellationException) {
                Log.d("GameViewModel", "Hint search cancelled")
                throw e // Propagate cancellation
            } catch (e: Exception) {
                Log.e("GameViewModel", "Error during hint search", e)
            } finally {
                // Guaranteed to run even if cancelled, fixing the spinner leak
                _state.update { it.copy(isHintLoading = false) }
            }
        }
    }
}
