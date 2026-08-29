package com.everscripts.dolphy_soduko

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.everscripts.dolphy_soduko.data.repository.SettingsRepository
import com.everscripts.dolphy_soduko.presentation.game.GameScreen
import com.everscripts.dolphy_soduko.presentation.game.GameViewModel
import com.everscripts.dolphy_soduko.ui.theme.Dolphy_sodukoTheme
import com.everscripts.dolphy_soduko.util.AdManager
import com.everscripts.dolphy_soduko.util.SocialManager
import com.everscripts.dolphy_soduko.util.AudioManager
import com.everscripts.dolphy_soduko.util.HapticManager
import com.everscripts.dolphy_soduko.data.repository.BillingRepository
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    
    private lateinit var adManager: AdManager
    private lateinit var socialManager: SocialManager
    private lateinit var billingRepository: BillingRepository
    private lateinit var audioManager: AudioManager
    private lateinit var hapticManager: HapticManager

    private val viewModel: GameViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val settingsRepository = SettingsRepository(applicationContext)
                return GameViewModel(settingsRepository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val settingsRepository = SettingsRepository(applicationContext)
        adManager = AdManager(this)
        socialManager = SocialManager(this)
        billingRepository = BillingRepository(this, settingsRepository)
        audioManager = AudioManager(this)
        hapticManager = HapticManager(this)
        
        socialManager.signIn()

        enableEdgeToEdge()
        setContent {
            val state by viewModel.state.collectAsState()
            
            // Sync hardware toggles
            LaunchedEffect(state.bgmEnabled, state.sfxEnabled, state.hapticsEnabled) {
                audioManager.setBgmEnabled(state.bgmEnabled)
                audioManager.setSfxEnabled(state.sfxEnabled)
                hapticManager.setEnabled(state.hapticsEnabled)
            }

            // Trigger SFX/Haptics on interactions
            LaunchedEffect(state.pourAnimation) {
                if (state.pourAnimation != null) {
                    audioManager.playPourSfx()
                    hapticManager.vibrateSelection()
                }
            }

            LaunchedEffect(state.isWon) {
                if (state.isWon) {
                    audioManager.playWinSfx()
                    hapticManager.vibrateSuccess()
                }
            }

            LaunchedEffect(state.selectedBottleId) {
                if (state.selectedBottleId != null) {
                    hapticManager.vibrateSelection()
                }
            }

            LaunchedEffect(state.showAd) {
                if (state.showAd) {
                    adManager.showInterstitial(this@MainActivity)
                    viewModel.onAdShown()
                }
            }

            LaunchedEffect(state.requestHintAd) {
                if (state.requestHintAd) {
                    adManager.showRewarded(
                        activity = this@MainActivity,
                        onRewardEarned = { viewModel.onHintAdShown() },
                        onAdDismissed = { viewModel.onHintAdDismissed() }
                    )
                }
            }

            LaunchedEffect(state.isAdsRemoved) {
                adManager.setAdsRemoved(state.isAdsRemoved)
            }

            Dolphy_sodukoTheme {
                GameScreen(viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::audioManager.isInitialized) {
            audioManager.startBgm()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::audioManager.isInitialized) {
            audioManager.pauseBgm()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::audioManager.isInitialized) {
            audioManager.release()
        }
    }
}
