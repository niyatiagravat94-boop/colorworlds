package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.audio.SoundManager
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdsManager(private val context: Context) {

    private val tag = "AdsManager"
    private var isInitialized = false

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private var lastInterstitialShownTime = 0L
    private var levelsCompletedSinceLastAd = 0

    var soundManager: SoundManager? = null

    /**
     * Initializes the Google Mobile Ads SDK once per app lifecycle.
     */
    fun initialize() {
        if (isInitialized) return
        try {
            MobileAds.initialize(context) { initializationStatus ->
                Log.d(tag, "AdMob Initialized: $initializationStatus")
                isInitialized = true
                loadInterstitialAd()
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize AdMob SDK", e)
        }
    }

    /**
     * Preloads an Interstitial Ad.
     */
    fun loadInterstitialAd() {
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            AdMobConfig.INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(tag, "Interstitial Ad loaded successfully")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.w(tag, "Interstitial Ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    /**
     * Checks if an interstitial ad should be shown based on frequency controls.
     */
    fun registerLevelCompletion() {
        levelsCompletedSinceLastAd++
    }

    private fun canShowInterstitial(): Boolean {
        val now = System.currentTimeMillis()
        val cooldownPassed = (now - lastInterstitialShownTime) >= (AdMobConfig.MIN_SECONDS_BETWEEN_INTERSTITIALS * 1000L)
        val levelThresholdReached = levelsCompletedSinceLastAd >= AdMobConfig.MIN_LEVELS_BETWEEN_INTERSTITIALS
        return cooldownPassed && levelThresholdReached
    }

    /**
     * Shows an Interstitial Ad if loaded and frequency constraints are met.
     */
    fun showInterstitialIfAllowed(activity: Activity, onDismissed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad != null && canShowInterstitial()) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    soundManager?.setDucked(true)
                }

                override fun onAdDismissedFullScreenContent() {
                    soundManager?.setDucked(false)
                    interstitialAd = null
                    lastInterstitialShownTime = System.currentTimeMillis()
                    levelsCompletedSinceLastAd = 0
                    loadInterstitialAd()
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    soundManager?.setDucked(false)
                    interstitialAd = null
                    loadInterstitialAd()
                    onDismissed()
                }
            }
            ad.show(activity)
        } else {
            // If ad is not ready or cooldown not met, proceed smoothly without blocking
            onDismissed()
            if (interstitialAd == null) {
                loadInterstitialAd()
            }
        }
    }
}
