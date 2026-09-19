package com.everscripts.dolphy_soduko.domain.repository

interface CloudSaveRepository {
    suspend fun saveProgress(level: Int, stars: Int)
    suspend fun loadProgress(): Pair<Int, Int>?
}
