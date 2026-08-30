package com.example.ads

/**
 * Centralized AdMob Configuration.
 * Holds the 3 AdMob Ad Units (Banner, Interstitial, Rewarded).
 *
 * For development & testing, standard Google AdMob Sample/Test Ad Unit IDs are used.
 * When releasing, update these constants with your production Ad Unit IDs.
 */
object AdMobConfig {
    // AdMob App ID
    const val APP_ID = "ca-app-pub-5873612031869970~7587840776"

    // AD UNIT 1: Banner Ad Unit ID
    const val BANNER_AD_UNIT_ID = "ca-app-pub-5873612031869970/8634645782"

    // AD UNIT 2: Interstitial Ad Unit ID
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-5873612031869970/7560297911"

    // AD UNIT 3: Native Advanced Ad Unit ID
    const val NATIVE_ADVANCED_AD_UNIT_ID = "ca-app-pub-5873612031869970/2199782726"

    // Frequency Caps & Safety Controls
    const val MIN_SECONDS_BETWEEN_INTERSTITIALS = 90L // Minimum cooldown between full-screen ads
    const val MIN_LEVELS_BETWEEN_INTERSTITIALS = 2    // Show interstitial at most every 2 completed levels
}
