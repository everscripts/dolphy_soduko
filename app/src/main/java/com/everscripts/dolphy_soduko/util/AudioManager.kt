package com.everscripts.dolphy_soduko.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log

class AudioManager(private val context: Context) {
    private val TAG = "AudioManager"

    private var sfxEnabled = true
    private var bgmEnabled = true

    private var soundPool: SoundPool? = null
    private var leapSoundId = 0
    private var winSoundId = 0

    private var mediaPlayer: MediaPlayer? = null

    init {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(attributes)
            .build()

        loadSounds()
        loadBgm()
    }

    private fun loadSounds() {
        val leapResId = context.resources.getIdentifier("sfx_leap", "raw", context.packageName)
        val winResId = context.resources.getIdentifier("sfx_win", "raw", context.packageName)

        if (leapResId != 0) {
            leapSoundId = soundPool?.load(context, leapResId, 1) ?: 0
        } else {
            Log.w(TAG, "sfx_leap.mp3 missing in res/raw")
        }

        if (winResId != 0) {
            winSoundId = soundPool?.load(context, winResId, 1) ?: 0
        } else {
            Log.w(TAG, "sfx_win.mp3 missing in res/raw")
        }
    }

    private fun loadBgm() {
        val bgmResId = context.resources.getIdentifier("bgm_main", "raw", context.packageName)
        if (bgmResId != 0) {
            try {
                mediaPlayer = MediaPlayer.create(context, bgmResId)
                mediaPlayer?.isLooping = true
                mediaPlayer?.setVolume(0.4f, 0.4f)
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing BGM: ${e.message}")
            }
        } else {
            Log.w(TAG, "bgm_main.mp3 missing in res/raw")
        }
    }

    fun setSfxEnabled(enabled: Boolean) {
        sfxEnabled = enabled
    }

    fun setBgmEnabled(enabled: Boolean) {
        bgmEnabled = enabled
        if (enabled) startBgm() else pauseBgm()
    }

    fun playPourSfx() {
        if (!sfxEnabled || leapSoundId == 0) return
        soundPool?.play(leapSoundId, 1f, 1f, 0, 0, 1f)
    }

    fun playWinSfx() {
        if (!sfxEnabled || winSoundId == 0) return
        soundPool?.play(winSoundId, 1f, 1f, 0, 0, 1f)
    }

    fun startBgm() {
        if (!bgmEnabled) return
        try {
            if (mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting BGM: ${e.message}")
        }
    }

    fun pauseBgm() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing BGM: ${e.message}")
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
