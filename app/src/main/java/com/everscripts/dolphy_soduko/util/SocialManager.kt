package com.everscripts.dolphy_soduko.util

import android.app.Activity
import android.util.Log
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.android.gms.games.snapshot.Snapshot
import com.google.android.gms.games.snapshot.SnapshotMetadataChange
import kotlinx.coroutines.tasks.await

class SocialManager(private val activity: Activity) {
    private val TAG = "SocialManager"

    init {
        PlayGamesSdk.initialize(activity)
    }

    fun signIn() {
        val gamesSignInClient = PlayGames.getGamesSignInClient(activity)
        gamesSignInClient.isAuthenticated.addOnCompleteListener { task ->
            val isAuthenticated = task.isSuccessful && task.result.isAuthenticated
            if (!isAuthenticated) {
                gamesSignInClient.signIn()
            } else {
                Log.d(TAG, "Already authenticated with Play Games")
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

    /**
     * Saves the game data to the cloud using Snapshots.
     * @param data JSON string or simple formatted string containing level and stars.
     */
    suspend fun saveGame(data: String) {
        try {
            val snapshotsClient = PlayGames.getSnapshotsClient(activity)
            
            // 1. Open the snapshot (create if it doesn't exist)
            val conflictPolicy = com.google.android.gms.games.SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED
            val task = snapshotsClient.open("dolphy_save", true, conflictPolicy)
            val result = task.await()
            
            val snapshot = result.data ?: return
            
            // 2. Write data
            snapshot.snapshotContents.writeBytes(data.toByteArray())
            
            // 3. Commit changes
            val metadataChange = SnapshotMetadataChange.Builder()
                .setDescription("Game progress at ${System.currentTimeMillis()}")
                .build()
            
            snapshotsClient.commitAndClose(snapshot, metadataChange).await()
            Log.d(TAG, "Cloud save successful: $data")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save game to cloud", e)
        }
    }

    /**
     * Loads the game data from the cloud.
     */
    suspend fun loadGame(): String? {
        try {
            val snapshotsClient = PlayGames.getSnapshotsClient(activity)
            val conflictPolicy = com.google.android.gms.games.SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED
            val result = snapshotsClient.open("dolphy_save", true, conflictPolicy).await()
            
            val snapshot = result.data ?: return null
            val data = snapshot.snapshotContents.readFully().decodeToString()
            snapshotsClient.discardAndClose(snapshot).await()
            
            Log.d(TAG, "Cloud data loaded: $data")
            return data
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load game from cloud", e)
            return null
        }
    }
}
