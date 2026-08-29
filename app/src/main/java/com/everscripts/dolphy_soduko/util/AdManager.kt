package com.everscripts.dolphy_soduko.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdManager(private val context: Context) {
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    
    private var isAdsRemoved = false
    private var levelCount = 0

    // Google Test IDs (Swap these for production IDs before publishing)
    private val INTERSTITIAL_ID = "ca-app-pub-6166817938980403/2536283517"
    private val REWARDED_ID = "ca-app-pub-6166817938980403/8910120170"

    init {
        MobileAds.initialize(context) {}
        loadInterstitial()
        loadRewarded()
    }

    fun setAdsRemoved(removed: Boolean) {
        isAdsRemoved = removed
    }

    private fun loadInterstitial() {
        if (isAdsRemoved) return
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, INTERSTITIAL_ID, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            })
    }

    private fun loadRewarded() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, REWARDED_ID, adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            })
    }

    fun showInterstitial(activity: Activity) {
        if (isAdsRemoved) return
        levelCount++
        if (levelCount % 3 == 0) {
            interstitialAd?.show(activity)
            loadInterstitial()
        }
    }

    fun showRewarded(activity: Activity, onRewardEarned: () -> Unit, onAdDismissed: () -> Unit, onAdFailed: () -> Unit) {
        Log.d("AdManager", "Attempting to show Rewarded Ad")
        rewardedAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("AdManager", "Rewarded Ad dismissed")
                    onAdDismissed()
                    loadRewarded()
                }
                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    Log.e("AdManager", "Rewarded Ad failed to show: ${error.message}")
                    onAdFailed()
                    loadRewarded()
                }
            }
            ad.show(activity) {
                Log.d("AdManager", "Rewarded Ad completed, granting reward")
                onRewardEarned()
            }
        } ?: run {
            Log.d("AdManager", "Rewarded Ad not loaded yet")
            onAdFailed()
            loadRewarded() // Try to reload for next time
        }
    }
}
