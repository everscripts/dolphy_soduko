package com.everscripts.dolphy_soduko.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class HapticManager(private val context: Context) {
    private var hapticsEnabled = true
    private val TAG = "HapticManager"

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun setEnabled(enabled: Boolean) {
        hapticsEnabled = enabled
        Log.d(TAG, "Haptics Enabled: $enabled")
    }

    fun vibrateSelection() {
        if (!hapticsEnabled) return
        Log.d(TAG, "Vibrating: Selection")
        vibrate(50)
    }

    fun vibrateSuccess() {
        if (!hapticsEnabled) return
        Log.d(TAG, "Vibrating: Success")
        vibrate(100)
    }

    fun vibrateError() {
        if (!hapticsEnabled) return
        Log.d(TAG, "Vibrating: Error")
        vibrate(longArrayOf(0, 50, 50, 50), -1)
    }

    private fun vibrate(duration: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    private fun vibrate(pattern: LongArray, repeat: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeat))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, repeat)
        }
    }
}
