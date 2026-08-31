package com.example

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

object AdManager {
    private const val TAG = "AdManager"

    // Production AdMob Ad Unit IDs provided by Admin
    const val INTERSTITIAL_AD_ID = "ca-app-pub-3015642322344048/9496062662"
    const val REWARDED_INTERSTITIAL_AD_ID = "ca-app-pub-3015642322344048/9496062662"
    const val REWARDED_AD_ID = "ca-app-pub-3015642322344048/8886754239"
    const val BANNER_AD_ID = "ca-app-pub-3015642322344048/5139080914"

    const val TOTAL_LEVEL_ADS = 12

    private var isInitialized = false
    private var isGmsAvailable = false

    private var preloadedInterstitial: InterstitialAd? = null
    private var preloadedRewardedInterstitial: RewardedInterstitialAd? = null
    private var preloadedRewarded: RewardedAd? = null
    private var isLoadingInterstitial = false
    private var isLoadingRewardedInterstitial = false
    private var isLoadingRewarded = false

    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true

        try {
            val requestConfig = com.google.android.gms.ads.RequestConfiguration.Builder()
                .setTestDeviceIds(emptyList())
                .build()
            MobileAds.setRequestConfiguration(requestConfig)

            MobileAds.initialize(context.applicationContext) { initializationStatus ->
                Log.d(TAG, "Official AdMob SDK Initialized: $initializationStatus")
                isGmsAvailable = true
                preloadAds(context.applicationContext)
            }
            isGmsAvailable = true
            preloadAds(context.applicationContext)
        } catch (e: Throwable) {
            Log.w(TAG, "MobileAds initialize warning: ${e.message}")
        }
    }

    fun isGmsActive(): Boolean = isGmsAvailable

    fun isEmulatorCheck(): Boolean = false

    fun createHighEcpmAdRequest(): AdRequest {
        val builder = AdRequest.Builder()
        // Premium High-eCPM targeting keywords for Google AdMob auction
        val highEcpmKeywords = listOf(
            "finance", "investment", "crypto", "banking", "insurance",
            "stock trading", "software", "ecommerce", "technology",
            "online shopping", "credit", "business loan", "premium gaming",
            "cloud services", "web hosting", "high cpm video", "rewards"
        )
        for (keyword in highEcpmKeywords) {
            builder.addKeyword(keyword)
        }
        return builder.build()
    }

    fun preloadAds(context: Context) {
        preloadInterstitial(context)
        preloadRewardedInterstitial(context)
        preloadRewarded(context)
    }

    private fun preloadInterstitial(context: Context) {
        if (preloadedInterstitial != null || isLoadingInterstitial) return
        isLoadingInterstitial = true

        try {
            val adRequest = createHighEcpmAdRequest()
            InterstitialAd.load(
                context,
                INTERSTITIAL_AD_ID,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        Log.d(TAG, "AdMob InterstitialAd preloaded successfully")
                        preloadedInterstitial = ad
                        isLoadingInterstitial = false
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.w(TAG, "InterstitialAd preload failed: ${loadAdError.message} (code: ${loadAdError.code})")
                        preloadedInterstitial = null
                        isLoadingInterstitial = false
                    }
                }
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Exception during InterstitialAd preload: ${e.message}", e)
            isLoadingInterstitial = false
        }
    }

    private fun preloadRewardedInterstitial(context: Context) {
        if (preloadedRewardedInterstitial != null || isLoadingRewardedInterstitial) return
        isLoadingRewardedInterstitial = true

        try {
            val adRequest = createHighEcpmAdRequest()
            RewardedInterstitialAd.load(
                context,
                REWARDED_INTERSTITIAL_AD_ID,
                adRequest,
                object : RewardedInterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedInterstitialAd) {
                        Log.d(TAG, "RewardedInterstitialAd preloaded successfully into memory")
                        preloadedRewardedInterstitial = ad
                        isLoadingRewardedInterstitial = false
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.w(TAG, "RewardedInterstitialAd preload failed: ${loadAdError.message} (code: ${loadAdError.code})")
                        preloadedRewardedInterstitial = null
                        isLoadingRewardedInterstitial = false
                    }
                }
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Exception during RewardedInterstitialAd preload: ${e.message}", e)
            isLoadingRewardedInterstitial = false
        }
    }

    private fun preloadRewarded(context: Context) {
        if (preloadedRewarded != null || isLoadingRewarded) return
        isLoadingRewarded = true

        try {
            val adRequest = createHighEcpmAdRequest()
            RewardedAd.load(
                context,
                REWARDED_AD_ID,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        Log.d(TAG, "RewardedAd preloaded successfully into memory")
                        preloadedRewarded = ad
                        isLoadingRewarded = false
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.w(TAG, "RewardedAd preload failed: ${loadAdError.message} (code: ${loadAdError.code})")
                        preloadedRewarded = null
                        isLoadingRewarded = false
                    }
                }
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Exception during RewardedAd preload: ${e.message}", e)
            isLoadingRewarded = false
        }
    }

    /**
     * Show standard AdMob Full Screen Ad (Rewarded Interstitial / Rewarded / Interstitial)
     * If already preloaded in memory, it presents instantly.
     * If not available (e.g. fresh install / no fill on test phone), calls onFallback to display rich interactive sponsor ad.
     */
    fun showFullScreenAd(
        activity: Activity,
        onFallback: () -> Unit,
        onAdDismissed: () -> Unit
    ) {
        showSequentialInterstitialAd(
            activity = activity,
            adIndex = 1,
            totalAds = TOTAL_LEVEL_ADS,
            onFallback = onFallback,
            onAdDismissed = onAdDismissed
        )
    }

    /**
     * Requests and shows sequential full-screen interstitial ad with complete AdMob listeners.
     * AdMob Listener Flow:
     * - onAdShowedFullScreenContent: Full screen is displayed to the user
     * - onAdImpression: AdMob server registers official impression
     * - onAdDismissedFullScreenContent: User finished watching and dismissed the ad -> triggers pipeline preload and proceeds to next queue step
     * - onAdFailedToShowFullScreenContent: Fallback to high-converting interactive sponsor ad dialog with mandatory countdown
     */
    fun showSequentialInterstitialAd(
        activity: Activity,
        adIndex: Int,
        totalAds: Int = TOTAL_LEVEL_ADS,
        onFallback: () -> Unit,
        onAdDismissed: () -> Unit
    ) {
        initialize(activity)
        try {
            activity.runOnUiThread {
                activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        } catch (e: Throwable) {
            // Ignore if already set
        }

        // 1. Check Preloaded Standard Interstitial
        val cachedInterstitial = preloadedInterstitial
        if (cachedInterstitial != null) {
            preloadedInterstitial = null
            Log.d(TAG, "[Ad $adIndex/$totalAds] Presenting preloaded AdMob InterstitialAd")
            cachedInterstitial.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "[Ad $adIndex/$totalAds] InterstitialAd showed full screen")
                }

                override fun onAdImpression() {
                    Log.d(TAG, "[Ad $adIndex/$totalAds] InterstitialAd recorded impression on AdMob")
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "[Ad $adIndex/$totalAds] InterstitialAd dismissed by user")
                    preloadAds(activity.applicationContext)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "[Ad $adIndex/$totalAds] InterstitialAd failed to show: ${error.message} (code: ${error.code})")
                    preloadAds(activity.applicationContext)
                    onFallback()
                }
            }
            try {
                cachedInterstitial.show(activity)
                return
            } catch (e: Throwable) {
                Log.e(TAG, "Exception displaying InterstitialAd: ${e.message}", e)
            }
        }

        // 2. Check Preloaded Rewarded Interstitial
        val cachedRewardedInterstitial = preloadedRewardedInterstitial
        if (cachedRewardedInterstitial != null) {
            preloadedRewardedInterstitial = null
            Log.d(TAG, "[Ad $adIndex/$totalAds] Presenting preloaded RewardedInterstitialAd")
            cachedRewardedInterstitial.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "[Ad $adIndex/$totalAds] RewardedInterstitialAd showed full screen")
                }

                override fun onAdImpression() {
                    Log.d(TAG, "[Ad $adIndex/$totalAds] RewardedInterstitialAd impression counted")
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "[Ad $adIndex/$totalAds] RewardedInterstitialAd dismissed by user")
                    preloadAds(activity.applicationContext)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "[Ad $adIndex/$totalAds] RewardedInterstitialAd failed to show: ${error.message}")
                    preloadAds(activity.applicationContext)
                    onFallback()
                }
            }
            try {
                cachedRewardedInterstitial.show(activity) { rewardItem ->
                    Log.d(TAG, "[Ad $adIndex/$totalAds] RewardedInterstitialAd reward earned: ${rewardItem.amount}")
                }
                return
            } catch (e: Throwable) {
                Log.e(TAG, "Exception showing cached RewardedInterstitialAd: ${e.message}", e)
            }
        }

        // 3. Check Preloaded Rewarded Ad
        val cachedRewarded = preloadedRewarded
        if (cachedRewarded != null) {
            preloadedRewarded = null
            Log.d(TAG, "[Ad $adIndex/$totalAds] Presenting preloaded RewardedAd")
            cachedRewarded.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "[Ad $adIndex/$totalAds] RewardedAd presented full screen")
                }

                override fun onAdImpression() {
                    Log.d(TAG, "[Ad $adIndex/$totalAds] RewardedAd impression logged")
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "[Ad $adIndex/$totalAds] RewardedAd dismissed")
                    preloadAds(activity.applicationContext)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "[Ad $adIndex/$totalAds] RewardedAd failed to show: ${error.message}")
                    preloadAds(activity.applicationContext)
                    onFallback()
                }
            }
            try {
                cachedRewarded.show(activity) { rewardItem ->
                    Log.d(TAG, "[Ad $adIndex/$totalAds] RewardedAd reward verified: ${rewardItem.amount}")
                }
                return
            } catch (e: Throwable) {
                Log.e(TAG, "Exception showing cached RewardedAd: ${e.message}", e)
            }
        }

        // 4. If nothing in cache, attempt live on-demand fetch with listener before falling back
        Log.d(TAG, "[Ad $adIndex/$totalAds] Cache empty. Requesting on-demand InterstitialAd from AdMob network...")
        try {
            val adRequest = createHighEcpmAdRequest()
            InterstitialAd.load(
                activity,
                INTERSTITIAL_AD_ID,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(liveAd: InterstitialAd) {
                        Log.d(TAG, "[Ad $adIndex/$totalAds] Live on-demand InterstitialAd loaded successfully!")
                        liveAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdShowedFullScreenContent() {
                                Log.d(TAG, "[Ad $adIndex/$totalAds] Live InterstitialAd showed full screen")
                            }

                            override fun onAdImpression() {
                                Log.d(TAG, "[Ad $adIndex/$totalAds] Live InterstitialAd impression confirmed")
                            }

                            override fun onAdDismissedFullScreenContent() {
                                Log.d(TAG, "[Ad $adIndex/$totalAds] Live InterstitialAd dismissed")
                                preloadAds(activity.applicationContext)
                                onAdDismissed()
                            }

                            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                                Log.e(TAG, "[Ad $adIndex/$totalAds] Live InterstitialAd failed to show: ${error.message}")
                                preloadAds(activity.applicationContext)
                                onFallback()
                            }
                        }
                        try {
                            liveAd.show(activity)
                        } catch (e: Throwable) {
                            Log.e(TAG, "Exception showing live InterstitialAd: ${e.message}", e)
                            preloadAds(activity.applicationContext)
                            onFallback()
                        }
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.w(TAG, "[Ad $adIndex/$totalAds] On-demand load error: ${error.message} (code: ${error.code}). Using fallback interactive ad.")
                        preloadAds(activity.applicationContext)
                        onFallback()
                    }
                }
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Exception requesting on-demand ad: ${e.message}", e)
            preloadAds(activity.applicationContext)
            onFallback()
        }
    }
}

@Composable
fun AdMobBannerView(modifier: Modifier = Modifier) {
    var currentAdIndex by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var isAdMobLoaded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    val sampleAds = androidx.compose.runtime.remember {
        listOf(
            SponsoredBannerData(
                brand = "Daraz Online Shopping",
                tagline = "মেগা ডিসকাউন্ট অফার! ৫০% পর্যন্ত ছাড় + ফ্রি ডেলিভারি",
                actionText = "শপ করুন",
                badge = "SPONSORED",
                iconEmoji = "🛍️",
                bgGradient = listOf(Color(0xFF831843), Color(0xFF500724)),
                btnColor = Color(0xFFF43F5E)
            ),
            SponsoredBannerData(
                brand = "bKash Digital Payment",
                tagline = "অ্যাপ দিয়ে পেমেন্টে পাবেন ইনস্ট্যান্ট ২০% ক্যাশব্যাক!",
                actionText = "ক্যাশব্যাক নিন",
                badge = "OFFER",
                iconEmoji = "💳",
                bgGradient = listOf(Color(0xFF831843), Color(0xFF1E1B4B)),
                btnColor = Color(0xFFE11D48)
            ),
            SponsoredBannerData(
                brand = "Binance Crypto Exchange",
                tagline = "ট্রেড করুন বিশ্বের সেরা প্ল্যাটফর্মে, জিরো ফিতে শুরু করুন",
                actionText = "ট্রেড করুন",
                badge = "PROMO",
                iconEmoji = "📈",
                bgGradient = listOf(Color(0xFF451A03), Color(0xFF1C1917)),
                btnColor = Color(0xFFF59E0B)
            ),
            SponsoredBannerData(
                brand = "Samsung Galaxy S24 Ultra",
                tagline = "Galaxy AI এর সাথে স্মার্টফোনের ভবিষ্যৎ উপভোগ করুন",
                actionText = "অর্ডার করুন",
                badge = "FEATURED",
                iconEmoji = "📱",
                bgGradient = listOf(Color(0xFF0C4A6E), Color(0xFF082F49)),
                btnColor = Color(0xFF0284C7)
            ),
            SponsoredBannerData(
                brand = "Foodpanda Delivery",
                tagline = "প্রথম অর্ডারে পান ৫০% ছাড়! কোড: PANDA50",
                actionText = "অর্ডার দিন",
                badge = "DEAL",
                iconEmoji = "🍕",
                bgGradient = listOf(Color(0xFF881337), Color(0xFF4C0519)),
                btnColor = Color(0xFFEC4899)
            )
        )
    }

    // Auto rotate banner every 5 seconds if not showing custom live AdMob view
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(5000)
            currentAdIndex = (currentAdIndex + 1) % sampleAds.size
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            contentAlignment = Alignment.Center
        ) {
            // Live AdMob Banner View
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                factory = { context ->
                    val adView = AdView(context)
                    adView.setAdSize(AdSize.BANNER)
                    adView.adUnitId = AdManager.BANNER_AD_ID
                    adView.adListener = object : com.google.android.gms.ads.AdListener() {
                        override fun onAdLoaded() {
                            isAdMobLoaded = true
                            Log.d("AdManager", "AdMob Banner loaded successfully")
                        }
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.w("AdManager", "AdMob Banner load error: ${error.message} code: ${error.code}")
                            isAdMobLoaded = false
                        }
                    }
                    try {
                        adView.loadAd(AdManager.createHighEcpmAdRequest())
                    } catch (e: Throwable) {
                        Log.w("AdManager", "AdView banner load exception: ${e.message}")
                        isAdMobLoaded = false
                    }
                    adView
                }
            )

            // Dynamic High-converting Sponsored Ad if AdMob is still loading, test-mode or no-fill
            if (!isAdMobLoaded) {
                val ad = sampleAds[currentAdIndex]
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(ad.bgGradient)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Ad Icon + Brand & Tagline
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Emoji / Brand Icon Container
                        Surface(
                            color = Color.Black.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = ad.iconEmoji, fontSize = 22.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFFFFD700),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = ad.badge,
                                        color = Color(0xFF0F172A),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = ad.brand,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = ad.tagline,
                                color = Color(0xFFF1F5F9),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Call To Action Button
                    Surface(
                        color = ad.btnColor,
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = ad.actionText,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }
    }
}

data class SponsoredBannerData(
    val brand: String,
    val tagline: String,
    val actionText: String,
    val badge: String,
    val iconEmoji: String,
    val bgGradient: List<Color>,
    val btnColor: Color
)

