package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.audio.SoundManager
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Manages Google AdMob lifecycle for Banner, Interstitial, and Rewarded Ads.
 * Handles SDK initialization, preloading, interstitial frequency control,
 * audio ducking during full-screen playback, and safe rewarded ad callbacks.
 */
class AdsManager(private val context: Context) {

    private val tag = "AdsManager"
    private var isInitialized = false

    // Interstitial Ad state
    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private var lastInterstitialShownTime = 0L
    private var levelsCompletedSinceLastAd = 0

    // Rewarded Ad state
    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

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
                loadRewardedAd()
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize AdMob SDK", e)
        }
    }

    /**
     * Creates a standard AdRequest for Banner and Fullscreen ads.
     */
    fun createAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    // ==========================================
    // INTERSTITIAL AD LIFECYCLE
    // ==========================================

    /**
     * Preloads an Interstitial Ad.
     */
    fun loadInterstitialAd() {
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        val adRequest = createAdRequest()
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
     * Increments the completed level counter for frequency capping.
     */
    fun registerLevelCompletion() {
        levelsCompletedSinceLastAd++
    }

    /**
     * Checks if interstitial cooldown and frequency thresholds have passed.
     */
    fun canShowInterstitial(): Boolean {
        val now = System.currentTimeMillis()
        val cooldownPassed = (now - lastInterstitialShownTime) >= (AdMobConfig.MIN_SECONDS_BETWEEN_INTERSTITIALS * 1000L)
        val levelThresholdReached = levelsCompletedSinceLastAd >= AdMobConfig.MIN_LEVELS_BETWEEN_INTERSTITIALS
        return cooldownPassed && levelThresholdReached
    }

    /**
     * Shows an Interstitial Ad if available and frequency rules are satisfied.
     * Ducks background music during playback and automatically preloads the next ad.
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
            // If ad is not ready or cooldown not met, proceed smoothly without blocking gameplay
            onDismissed()
            if (interstitialAd == null) {
                loadInterstitialAd()
            }
        }
    }

    // ==========================================
    // REWARDED AD LIFECYCLE
    // ==========================================

    /**
     * Preloads a Rewarded Ad.
     */
    fun loadRewardedAd() {
        if (rewardedAd != null || isRewardedLoading) return
        isRewardedLoading = true

        val adRequest = createAdRequest()
        RewardedAd.load(
            context,
            AdMobConfig.REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    Log.d(tag, "Rewarded Ad loaded successfully")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    Log.w(tag, "Rewarded Ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    /**
     * Returns true if a rewarded ad is currently ready to display.
     */
    fun isRewardedAdReady(): Boolean = rewardedAd != null

    /**
     * Displays a Rewarded Ad with safe user reward callbacks.
     * The onRewardEarned callback is invoked ONLY when Google AdMob confirms reward completion.
     */
    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: (RewardItem) -> Unit,
        onAdClosed: () -> Unit = {}
    ) {
        val ad = rewardedAd
        if (ad != null) {
            var earnedReward: RewardItem? = null

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    soundManager?.setDucked(true)
                }

                override fun onAdDismissedFullScreenContent() {
                    soundManager?.setDucked(false)
                    rewardedAd = null
                    loadRewardedAd()

                    earnedReward?.let { reward ->
                        onRewardEarned(reward)
                    }
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    soundManager?.setDucked(false)
                    rewardedAd = null
                    loadRewardedAd()
                    Toast.makeText(activity, "Ad isn't available right now. Please try again.", Toast.LENGTH_SHORT).show()
                    onAdClosed()
                }
            }

            ad.show(activity) { rewardItem ->
                earnedReward = rewardItem
                Log.d(tag, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
            }
        } else {
            Toast.makeText(activity, "Ad is loading. Please try again in a moment.", Toast.LENGTH_SHORT).show()
            loadRewardedAd()
            onAdClosed()
        }
    }
}
