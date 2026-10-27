package com.bhaskar.anytranslate.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdMobHelper(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null
    private var lookupCount = 0

    companion object {
        private const val TAG = "AdMobHelper"
        // Google official test Interstitial Ad Unit ID
        const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        private const val AD_FREQUENCY_INTERVAL = 4 // Display an interstitial every 4 lookups
    }

    init {
        loadInterstitial()
    }

    fun loadInterstitial() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            TEST_INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(TAG, "AdMob Interstitial Ad Loaded successfully")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    Log.w(TAG, "AdMob Interstitial Ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    /**
     * Checks if it's time to show an interstitial ad and displays it if ready.
     */
    fun onTranslationPerformed(activity: Activity) {
        lookupCount++
        if (lookupCount % AD_FREQUENCY_INTERVAL == 0) {
            interstitialAd?.show(activity)
            // Reload the next ad
            loadInterstitial()
        }
    }
}
