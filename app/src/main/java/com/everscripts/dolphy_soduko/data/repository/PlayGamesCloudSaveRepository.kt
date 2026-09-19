package com.everscripts.dolphy_soduko.data.repository

import com.everscripts.dolphy_soduko.domain.repository.CloudSaveRepository
import com.everscripts.dolphy_soduko.util.SocialManager

class PlayGamesCloudSaveRepository(
    private val socialManager: SocialManager
) : CloudSaveRepository {

    override suspend fun saveProgress(level: Int, stars: Int) {
        val data = "$level,$stars"
        socialManager.saveGame(data)
    }

    override suspend fun loadProgress(): Pair<Int, Int>? {
        val data = socialManager.loadGame() ?: return null
        return try {
            val parts = data.split(",")
            if (parts.size == 2) {
                parts[0].toInt() to parts[1].toInt()
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
