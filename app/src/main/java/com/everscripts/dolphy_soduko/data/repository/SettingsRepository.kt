package com.everscripts.dolphy_soduko.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val SFX_ENABLED = booleanPreferencesKey("sfx_enabled")
        val BGM_ENABLED = booleanPreferencesKey("bgm_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val ACTIVE_SKIN = stringPreferencesKey("active_skin")
        val CURRENT_LEVEL = intPreferencesKey("current_level")
        val ADS_REMOVED = booleanPreferencesKey("ads_removed")
        val WATER_COLOR = stringPreferencesKey("water_color")
        val FREE_HINTS_USED = intPreferencesKey("free_hints_used")
    }

    val sfxEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.SFX_ENABLED] ?: true }
    val bgmEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.BGM_ENABLED] ?: true }
    val hapticsEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.HAPTICS_ENABLED] ?: true }
    val activeSkin: Flow<String> = context.dataStore.data.map { it[Keys.ACTIVE_SKIN] ?: "DOLPHY" }
    val currentLevel: Flow<Int> = context.dataStore.data.map { it[Keys.CURRENT_LEVEL] ?: 1 }
    val adsRemoved: Flow<Boolean> = context.dataStore.data.map { it[Keys.ADS_REMOVED] ?: false }
    val waterColor: Flow<String> = context.dataStore.data.map { it[Keys.WATER_COLOR] ?: "FF2196F3" } // Default Blue
    val freeHintsUsed: Flow<Int> = context.dataStore.data.map { it[Keys.FREE_HINTS_USED] ?: 0 }

    suspend fun toggleSfx(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SFX_ENABLED] = enabled }
    }

    suspend fun toggleBgm(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BGM_ENABLED] = enabled }
    }

    suspend fun toggleHaptics(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun setActiveSkin(skin: String) {
        context.dataStore.edit { it[Keys.ACTIVE_SKIN] = skin }
    }

    suspend fun setLevel(level: Int) {
        context.dataStore.edit { it[Keys.CURRENT_LEVEL] = level }
    }

    suspend fun setAdsRemoved(removed: Boolean) {
        context.dataStore.edit { it[Keys.ADS_REMOVED] = removed }
    }

    suspend fun setWaterColor(colorHex: String) {
        context.dataStore.edit { it[Keys.WATER_COLOR] = colorHex }
    }

    suspend fun incrementFreeHints() {
        context.dataStore.edit { 
            val current = it[Keys.FREE_HINTS_USED] ?: 0
            it[Keys.FREE_HINTS_USED] = current + 1
        }
    }
}
