package com.everscripts.dolphy_soduko.util

import android.app.Activity
import com.google.android.gms.games.GamesSignInClient
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk

class SocialManager(private val activity: Activity) {

    init {
        PlayGamesSdk.initialize(activity)
    }

    fun signIn() {
        val gamesSignInClient = PlayGames.getGamesSignInClient(activity)
        gamesSignInClient.isAuthenticated.addOnCompleteListener { task ->
            val isAuthenticated = task.isSuccessful && task.result.isAuthenticated
            if (!isAuthenticated) {
                gamesSignInClient.signIn()
            }
        }
    }

    fun submitScore(leaderboardId: String, score: Long) {
        PlayGames.getLeaderboardsClient(activity)
            .submitScore(leaderboardId, score)
    }

    fun showLeaderboards() {
        PlayGames.getLeaderboardsClient(activity)
            .allLeaderboardsIntent
            .addOnSuccessListener { intent ->
                activity.startActivityForResult(intent, 9004)
            }
    }
}
