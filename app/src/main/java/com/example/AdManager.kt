package com.example

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

object AdManager {
    private const val TAG = "AdManager"

    // Production AdMob Ad Unit IDs provided by Admin
    const val REWARDED_INTERSTITIAL_AD_ID = "ca-app-pub-3015642322344048/9496062662"
    const val REWARDED_AD_ID = "ca-app-pub-3015642322344048/8886754239"
    const val BANNER_AD_ID = "ca-app-pub-3015642322344048/5139080914"

    private var isInitialized = false
    private var isGmsAvailable = false

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            val isTestEnv = isEmulator()
            if (isTestEnv) {
                Log.i(TAG, "Running in emulator/test environment. Bypassing live AdMob GMS initialization.")
                isGmsAvailable = false
                isInitialized = true
                return
            }

            val hasPlayStore = try {
                context.packageManager.getPackageInfo("com.android.vending", 0) != null
            } catch (e: Throwable) {
                false
            }

            if (!hasPlayStore) {
                Log.i(TAG, "Google Play Store not present. Bypassing live AdMob calls.")
                isGmsAvailable = false
                isInitialized = true
                return
            }

            val availability = try {
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
            } catch (e: Throwable) {
                Log.w(TAG, "Error checking play services availability: ${e.message}")
                ConnectionResult.SERVICE_MISSING
            }

            if (availability != ConnectionResult.SUCCESS) {
                Log.w(TAG, "Google Play Services unavailable ($availability). Disabling live AdMob calls.")
                isGmsAvailable = false
                isInitialized = true
                return
            }

            try {
                MobileAds.initialize(context) { initializationStatus ->
                    Log.d(TAG, "AdMob SDK Initialized: $initializationStatus")
                    isGmsAvailable = true
                    isInitialized = true
                }
            } catch (e: Throwable) {
                Log.w(TAG, "MobileAds initialize call failed or unsupported: ${e.message}")
                isGmsAvailable = false
            }
            isInitialized = true
        } catch (e: Throwable) {
            Log.e(TAG, "AdMob initialization error: ${e.message}")
            isGmsAvailable = false
            isInitialized = true
        }
    }

    private fun isEmulator(): Boolean {
        val fingerPrint = android.os.Build.FINGERPRINT.lowercase()
        val model = android.os.Build.MODEL.lowercase()
        val manufacturer = android.os.Build.MANUFACTURER.lowercase()
        val brand = android.os.Build.BRAND.lowercase()
        val device = android.os.Build.DEVICE.lowercase()
        val product = android.os.Build.PRODUCT.lowercase()
        val hardware = android.os.Build.HARDWARE.lowercase()
        val board = android.os.Build.BOARD.lowercase()
        val tags = android.os.Build.TAGS.lowercase()
        val host = android.os.Build.HOST.lowercase()
        val user = android.os.Build.USER.lowercase()

        return BuildConfig.DEBUG
                || tags.contains("test-keys")
                || fingerPrint.startsWith("generic")
                || fingerPrint.startsWith("unknown")
                || fingerPrint.contains("sdk")
                || fingerPrint.contains("emulator")
                || fingerPrint.contains("gphone")
                || fingerPrint.contains("aosp")
                || model.contains("google_sdk")
                || model.contains("emulator")
                || model.contains("android sdk")
                || model.contains("sdk")
                || model.contains("gphone")
                || model.contains("vbox")
                || model.contains("pixel")
                || model.contains("aosp")
                || manufacturer.contains("genymotion")
                || manufacturer.contains("google")
                || manufacturer.contains("android")
                || brand.contains("android")
                || brand.contains("generic")
                || device.contains("generic")
                || device.contains("aosp")
                || hardware.contains("goldfish")
                || hardware.contains("ranchu")
                || hardware.contains("cutf")
                || hardware.contains("cuttlefish")
                || hardware.contains("vbox")
                || hardware.contains("x86")
                || hardware.contains("arm")
                || hardware.contains("ttvm")
                || board.contains("goldfish")
                || board.contains("cutf")
                || board.contains("vbox")
                || product.contains("sdk")
                || product.contains("emulator")
                || product.contains("gphone")
                || product.contains("cuttlefish")
                || product.contains("vbox")
                || product.contains("aosp")
                || host.contains("google")
                || user.contains("android")
    }

    /**
     * Show a full screen ad (attempts Rewarded Interstitial first, fallback to Rewarded, fallback on error)
     */
    fun showFullScreenAd(
        activity: Activity,
        onAdDismissed: () -> Unit
    ) {
        initialize(activity)
        
        if (!isGmsAvailable) {
            Log.w(TAG, "Play Services not functional on this device/emulator. Proceeding without ad.")
            onAdDismissed()
            return
        }

        Log.d(TAG, "Loading Full Screen Ad for user flow...")

        try {
            val adRequest = AdRequest.Builder().build()

            RewardedInterstitialAd.load(
                activity,
                REWARDED_INTERSTITIAL_AD_ID,
                adRequest,
                object : RewardedInterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedInterstitialAd) {
                        Log.d(TAG, "RewardedInterstitialAd loaded successfully")
                        try {
                            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    Log.d(TAG, "Ad 1 dismissed")
                                    onAdDismissed()
                                }

                                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                                    Log.e(TAG, "Ad failed to show: ${error.message}")
                                    onAdDismissed()
                                }
                            }
                            ad.show(activity) { rewardItem ->
                                Log.d(TAG, "User earned ad reward: ${rewardItem.amount} ${rewardItem.type}")
                            }
                        } catch (e: Throwable) {
                            Log.e(TAG, "Error showing RewardedInterstitialAd: ${e.message}", e)
                            onAdDismissed()
                        }
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.w(TAG, "RewardedInterstitialAd load failed: ${loadAdError.message}. Trying RewardedAd...")
                        loadRewardedAd(activity, onAdDismissed)
                    }
                }
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Error initiating RewardedInterstitialAd load: ${e.message}", e)
            loadRewardedAd(activity, onAdDismissed)
        }
    }

    private fun loadRewardedAd(
        activity: Activity,
        onAdDismissed: () -> Unit
    ) {
        try {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                activity,
                REWARDED_AD_ID,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        Log.d(TAG, "RewardedAd loaded successfully")
                        try {
                            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    Log.d(TAG, "Rewarded Ad dismissed")
                                    onAdDismissed()
                                }

                                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                                    Log.e(TAG, "Rewarded Ad failed to show: ${error.message}")
                                    onAdDismissed()
                                }
                            }
                            ad.show(activity) { rewardItem ->
                                Log.d(TAG, "RewardedAd reward: ${rewardItem.amount}")
                            }
                        } catch (e: Throwable) {
                            Log.e(TAG, "Error showing RewardedAd: ${e.message}", e)
                            onAdDismissed()
                        }
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.e(TAG, "RewardedAd load failed: ${loadAdError.message}")
                        // Proceed gracefully even if network block or no fill occurs so user flow is not stuck
                        onAdDismissed()
                    }
                }
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Error initiating RewardedAd load: ${e.message}", e)
            onAdDismissed()
        }
    }
}
