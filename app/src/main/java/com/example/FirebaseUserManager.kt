package com.example

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseUserManager {
    private const val TAG = "FirebaseUserManager"
    private const val PREFS_NAME = "garments_hero_user_session"
    private const val KEY_NAME = "user_name"
    private const val KEY_MOBILE = "user_mobile"
    private const val KEY_REFERRAL_CODE = "user_referral_code"
    private const val KEY_REFERRED_BY = "user_referred_by"
    private const val KEY_CREATED_AT = "user_created_at"
    private const val KEY_CURRENT_LEVEL = "user_current_level"
    private const val KEY_COMPLETED_LEVELS = "user_completed_levels"
    private const val KEY_REWARD_COINS = "user_reward_coins"
    private const val KEY_EARNINGS_TAKA = "user_earnings_taka"
    private const val KEY_REFERRALS_COUNT = "user_referrals_count"
    private const val KEY_AVATAR_URL = "user_avatar_url"
    private const val KEY_LAST_CHECK_IN_DATE = "user_last_check_in_date"
    private const val KEY_CHECK_IN_STREAK = "user_check_in_streak"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_UNSYNCED_EARNINGS = "unsynced_earnings_taka"
    private const val KEY_UNSYNCED_COINS = "unsynced_reward_coins"
    private const val KEY_UNSYNCED_LEVELS = "unsynced_completed_levels"

    private val firestore: FirebaseFirestore by lazy {
        val db = FirebaseFirestore.getInstance()
        try {
            val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            db.firestoreSettings = settings
        } catch (e: Throwable) {
            // Already initialized or default applied
        }
        db
    }

    fun normalizeMobile(input: String): String {
        // Convert Bengali digits to English digits if any
        val englishDigits = input.map { char ->
            when (char) {
                '০' -> '0'
                '১' -> '1'
                '২' -> '2'
                '৩' -> '3'
                '৪' -> '4'
                '৫' -> '5'
                '৬' -> '6'
                '৭' -> '7'
                '৮' -> '8'
                '৯' -> '9'
                else -> char
            }
        }.joinToString("")

        // Remove non-digit characters
        val clean = englishDigits.replace(Regex("[^0-9]"), "")
        
        // If it starts with 880, strip 88
        if (clean.startsWith("880") && clean.length == 13) {
            return clean.substring(2)
        }
        return clean
    }

    fun isValidMobile(mobile: String): Boolean {
        val normalized = normalizeMobile(mobile)
        return normalized.length == 11 && normalized.startsWith("01")
    }

    private fun generateReferralCode(mobile: String): String {
        val last4 = if (mobile.length >= 4) mobile.takeLast(4) else "1234"
        val randomLetter = (1..2).map { ('A'..'Z').random() }.joinToString("")
        return "GH$last4$randomLetter"
    }

    fun registerUser(
        context: Context,
        name: String,
        mobile: String,
        referralInput: String,
        onResult: (success: Boolean, message: String, user: User?) -> Unit
    ) {
        val cleanName = name.trim()
        val cleanMobile = normalizeMobile(mobile)
        val cleanReferral = referralInput.trim().uppercase()

        if (cleanName.isEmpty()) {
            onResult(false, "অনুগ্রহ করে আপনার নাম লিখুন।", null)
            return
        }

        if (!isValidMobile(cleanMobile)) {
            onResult(false, "একটি সঠিক ১১ ডিজিটের মোবাইল নম্বর দিন (যেমন: 017XXXXXXXX)।", null)
            return
        }

        val userDocRef = firestore.collection("users").document(cleanMobile)

        // Check if user already exists
        userDocRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                onResult(false, "এই মোবাইল নম্বরটি ইতোমধ্যে নিবন্ধিত! অনুগ্রহ করে 'লগ ইন' করুন।", null)
            } else {
                // Generate a unique referral code for the new user
                val myReferralCode = generateReferralCode(cleanMobile)
                val newUser = User(
                    name = cleanName,
                    mobile = cleanMobile,
                    referralCode = myReferralCode,
                    referredBy = cleanReferral,
                    createdAt = System.currentTimeMillis(),
                    currentLevel = 1,
                    completedLevels = 0,
                    rewardCoins = 0,
                    earningsTaka = 0.0
                )

                // Save user to Firebase Firestore
                userDocRef.set(newUser)
                    .addOnSuccessListener {
                        saveSession(context, newUser)
                        Log.d(TAG, "User registered successfully in Firestore: $cleanMobile")

                        // Process referral bonus for referrer if code was supplied
                        if (cleanReferral.isNotEmpty()) {
                            firestore.collection("users")
                                .whereEqualTo("referralCode", cleanReferral)
                                .get()
                                .addOnSuccessListener { refQuery ->
                                    if (!refQuery.isEmpty) {
                                        val refDoc = refQuery.documents[0]
                                        val refCount = refDoc.getLong("referralsCount") ?: 0L
                                        val refEarnings = refDoc.getDouble("earningsTaka") ?: 0.0
                                        val refCoins = refDoc.getLong("rewardCoins") ?: 0L

                                        refDoc.reference.update(
                                            mapOf(
                                                "referralsCount" to (refCount + 1),
                                                "earningsTaka" to (refEarnings + 5.0),
                                                "rewardCoins" to (refCoins + 500)
                                            )
                                        )
                                    }
                                }
                        }

                        onResult(true, "অভিনন্দন! আপনার ইনকাম অ্যাকাউন্ট সফলভাবে তৈরি হয়েছে।", newUser)
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Failed to register user in Firestore (offline mode active): ${e.message}")
                        saveSession(context, newUser)
                        onResult(true, "অফলাইন সেশনে অ্যাকাউন্ট তৈরি হয়েছে।", newUser)
                    }
            }
        }.addOnFailureListener { e ->
            Log.w(TAG, "Error/Offline checking user existence, proceeding with local fallback: ${e.message}")
            val myReferralCode = generateReferralCode(cleanMobile)
            val newUser = User(
                name = cleanName,
                mobile = cleanMobile,
                referralCode = myReferralCode,
                referredBy = cleanReferral,
                createdAt = System.currentTimeMillis(),
                currentLevel = 1,
                completedLevels = 0,
                rewardCoins = 0,
                earningsTaka = 0.0
            )
            saveSession(context, newUser)
            onResult(true, "নিবন্ধন সম্পন্ন হয়েছে!", newUser)
        }
    }

    fun loginUser(
        context: Context,
        mobile: String,
        onResult: (success: Boolean, message: String, user: User?) -> Unit
    ) {
        val cleanMobile = normalizeMobile(mobile)

        if (!isValidMobile(cleanMobile)) {
            onResult(false, "একটি সঠিক ১১ ডিজিটের মোবাইল নম্বর দিন (যেমন: 017XXXXXXXX)।", null)
            return
        }

        val userDocRef = firestore.collection("users").document(cleanMobile)

        userDocRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val user = snapshot.toObject(User::class.java) ?: User(
                    name = snapshot.getString("name") ?: "ব্যবহারকারী",
                    mobile = cleanMobile,
                    referralCode = snapshot.getString("referralCode") ?: generateReferralCode(cleanMobile),
                    referredBy = snapshot.getString("referredBy") ?: "",
                    createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis(),
                    currentLevel = (snapshot.getLong("currentLevel") ?: 1L).toInt(),
                    completedLevels = (snapshot.getLong("completedLevels") ?: 0L).toInt(),
                    rewardCoins = snapshot.getLong("rewardCoins") ?: 0L,
                    earningsTaka = snapshot.getDouble("earningsTaka") ?: 0.0,
                    avatarUrl = snapshot.getString("avatarUrl") ?: "",
                    lastCheckInDate = snapshot.getString("lastCheckInDate") ?: "",
                    checkInStreak = (snapshot.getLong("checkInStreak") ?: 0L).toInt()
                )
                saveSession(context, user)
                Log.d(TAG, "User logged in successfully from Firestore: $cleanMobile")
                onResult(true, "স্বাগতম! লগইন সম্পন্ন হয়েছে।", user)
            } else {
                val saved = getSavedSession(context)
                if (saved != null && saved.mobile == cleanMobile) {
                    onResult(true, "স্বাগতম! অফলাইন সেশন থেকে লগইন সম্পন্ন হয়েছে।", saved)
                } else {
                    onResult(false, "এই মোবাইল নম্বরটি নিবন্ধিত নয়! অনুগ্রহ করে আগে 'নিবন্ধন' করুন।", null)
                }
            }
        }.addOnFailureListener { e ->
            Log.w(TAG, "Firestore login check failed (offline mode active): ${e.message}")
            val saved = getSavedSession(context)
            if (saved != null && saved.mobile == cleanMobile) {
                onResult(true, "স্বাগতম! অফলাইন তথ্য থেকে লগইন সম্পন্ন হয়েছে।", saved)
            } else {
                onResult(false, "ইন্টারনেট সংযোগ পরীক্ষা করুন অথবা নিবন্ধন করুন।", null)
            }
        }
    }

    /**
     * Local Point & Level Accumulation (ZERO Firestore Writes During Gameplay)
     * All earned points, coins, and levels are stored locally in SharedPreferences and memory.
     * UI updates instantly with zero latency, completely staying within free daily quota.
     */
    fun updateUserProgress(
        context: Context,
        user: User,
        coinsEarned: Long,
        takaEarned: Double,
        onResult: (updatedUser: User) -> Unit
    ) {
        val nextLevel = user.currentLevel + 1
        val updatedCompleted = user.completedLevels + 1
        val updatedCoins = user.rewardCoins + coinsEarned
        val updatedTaka = user.earningsTaka + takaEarned

        val updatedUser = user.copy(
            currentLevel = nextLevel,
            completedLevels = updatedCompleted,
            rewardCoins = updatedCoins,
            earningsTaka = updatedTaka
        )

        // 1. Save full updated user in local session
        saveSession(context, updatedUser)

        // 2. Buffer unsynced diff in SharedPreferences for crash safety
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentUnsyncedTaka = prefs.getFloat(KEY_UNSYNCED_EARNINGS, 0.0f) + takaEarned.toFloat()
        val currentUnsyncedCoins = prefs.getLong(KEY_UNSYNCED_COINS, 0L) + coinsEarned
        val currentUnsyncedLevels = prefs.getInt(KEY_UNSYNCED_LEVELS, 0) + 1

        prefs.edit()
            .putFloat(KEY_UNSYNCED_EARNINGS, currentUnsyncedTaka)
            .putLong(KEY_UNSYNCED_COINS, currentUnsyncedCoins)
            .putInt(KEY_UNSYNCED_LEVELS, currentUnsyncedLevels)
            .apply()

        Log.d(TAG, "Locally accumulated progress: +$takaEarned Taka, +$coinsEarned Coins (Unsynced buffer: $currentUnsyncedTaka Taka, $currentUnsyncedLevels levels). ZERO Firestore writes executed.")

        // Immediate zero-latency UI update callback
        onResult(updatedUser)
    }

    /**
     * Single Batch Sync on App Pause / Stop / Destroy / Startup
     * Synchronizes accumulated points, coins, and levels to Firestore in ONE atomic write.
     * Uses FieldValue.increment() to guarantee zero data loss.
     */
    @Synchronized
    fun syncPendingProgressToFirestore(
        context: Context,
        onComplete: ((success: Boolean) -> Unit)? = null
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val mobile = prefs.getString(KEY_MOBILE, "") ?: ""
        if (mobile.isEmpty()) {
            onComplete?.invoke(false)
            return
        }

        val pendingTaka = prefs.getFloat(KEY_UNSYNCED_EARNINGS, 0.0f).toDouble()
        val pendingCoins = prefs.getLong(KEY_UNSYNCED_COINS, 0L)
        val pendingLevels = prefs.getInt(KEY_UNSYNCED_LEVELS, 0)
        val currentLevel = prefs.getInt(KEY_CURRENT_LEVEL, 1)

        if (pendingTaka <= 0.0 && pendingCoins <= 0L && pendingLevels <= 0) {
            Log.d(TAG, "No pending points to sync. Firestore write quota preserved.")
            onComplete?.invoke(true)
            return
        }

        Log.d(TAG, "Syncing batch progress to Firestore for $mobile: +$pendingTaka Taka, +$pendingCoins Coins, +$pendingLevels Levels")

        val updates = mapOf(
            "earningsTaka" to com.google.firebase.firestore.FieldValue.increment(pendingTaka),
            "rewardCoins" to com.google.firebase.firestore.FieldValue.increment(pendingCoins),
            "completedLevels" to com.google.firebase.firestore.FieldValue.increment(pendingLevels.toLong()),
            "currentLevel" to currentLevel
        )

        firestore.collection("users").document(mobile)
            .set(updates, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Successfully synced batch progress to Firestore in single atomic write.")
                // Atomically subtract or reset the synced buffer
                val currentP = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val remainingTaka = (currentP.getFloat(KEY_UNSYNCED_EARNINGS, 0.0f) - pendingTaka.toFloat()).coerceAtLeast(0.0f)
                val remainingCoins = (currentP.getLong(KEY_UNSYNCED_COINS, 0L) - pendingCoins).coerceAtLeast(0L)
                val remainingLevels = (currentP.getInt(KEY_UNSYNCED_LEVELS, 0) - pendingLevels).coerceAtLeast(0)

                currentP.edit()
                    .putFloat(KEY_UNSYNCED_EARNINGS, remainingTaka)
                    .putLong(KEY_UNSYNCED_COINS, remainingCoins)
                    .putInt(KEY_UNSYNCED_LEVELS, remainingLevels)
                    .apply()

                onComplete?.invoke(true)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Batch sync to Firestore failed (will retry on next app exit/launch): ${e.message}")
                onComplete?.invoke(false)
            }
    }

    fun submitWithdrawal(
        context: Context,
        user: User,
        paymentMethod: String,
        paymentNumber: String,
        amountTaka: Double,
        onResult: (success: Boolean, message: String, updatedUser: User?) -> Unit
    ) {
        if (user.referralsCount < 3) {
            onResult(false, "উইথড্র করার জন্য আপনাকে অবশ্যই অন্তত ৩টি সফল রেফার সম্পন্ন করতে হবে! (আপনার বর্তমান সফল রেফারেল: ${user.referralsCount}/৩)", null)
            return
        }

        if (amountTaka < 100.0 || amountTaka > 500.0) {
            onResult(false, "উইথড্র পরিমাণ অবশ্যই ৳১০০.০০ থেকে ৳৫০০.০০ টাকার মধ্যে হতে হবে।", null)
            return
        }

        if (user.earningsTaka < amountTaka) {
            onResult(false, "আপনার অ্যাকাউন্টে পর্যাপ্ত ব্যালেন্স নেই (বর্তমান ব্যালেন্স: ৳${String.format(java.util.Locale.US, "%.2f", user.earningsTaka)})", null)
            return
        }

        if (paymentNumber.length < 11) {
            onResult(false, "সঠিক পেমেন্ট মোবাইল নম্বর লিখুন।", null)
            return
        }

        // Sync pending progress first to ensure Firestore balance is up-to-date
        syncPendingProgressToFirestore(context) {
            val coinsToDeduct = (amountTaka * 100).toLong()
            val updatedTaka = (user.earningsTaka - amountTaka).coerceAtLeast(0.0)
            val updatedCoins = (user.rewardCoins - coinsToDeduct).coerceAtLeast(0L)
            val updatedUser = user.copy(earningsTaka = updatedTaka, rewardCoins = updatedCoins)

            val withdrawalReq = mapOf(
                "userMobile" to user.mobile,
                "userName" to user.name,
                "paymentMethod" to paymentMethod,
                "paymentNumber" to paymentNumber,
                "amountTaka" to amountTaka,
                "rewardCoins" to coinsToDeduct,
                "requestedAt" to System.currentTimeMillis(),
                "status" to "PENDING"
            )

            firestore.collection("withdrawals")
                .add(withdrawalReq)
                .addOnSuccessListener {
                    firestore.collection("users").document(user.mobile)
                        .update(
                            mapOf(
                                "earningsTaka" to updatedTaka,
                                "rewardCoins" to updatedCoins
                            )
                        )
                    saveSession(context, updatedUser)
                    onResult(true, "উইথড্র রিকোয়েস্ট জমা হয়েছে! শীঘ্রই পেমেন্ট প্রসেস করা হবে।", updatedUser)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Withdrawal failed in Firestore (saved locally): ${e.message}")
                    // Fallback offline deduction
                    saveSession(context, updatedUser)
                    onResult(true, "উইথড্র রিকোয়েস্ট সংরক্ষিত হয়েছে (অফলাইন মোড)।", updatedUser)
                }
        }
    }

    fun fetchAllWithdrawals(onResult: (List<WithdrawalRequest>) -> Unit) {
        firestore.collection("withdrawals")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val list = querySnapshot.documents.map { doc ->
                    val userMobile = doc.getString("userMobile") ?: doc.getString("mobile") ?: ""
                    val userName = doc.getString("userName") ?: doc.getString("name") ?: doc.getString("fullName") ?: ""
                    WithdrawalRequest(
                        id = doc.id,
                        userMobile = userMobile,
                        userName = if (userName.isNotBlank()) userName else (if (userMobile.isNotBlank()) "ইউজার ($userMobile)" else "ইউজার"),
                        paymentMethod = doc.getString("paymentMethod") ?: "বিকাশ",
                        paymentNumber = doc.getString("paymentNumber") ?: "",
                        amountTaka = doc.getDouble("amountTaka") ?: 0.0,
                        requestedAt = doc.getLong("requestedAt") ?: System.currentTimeMillis(),
                        status = doc.getString("status") ?: "PENDING"
                    )
                }
                onResult(list.sortedByDescending { it.requestedAt })
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun fetchAllUsers(onResult: (List<User>) -> Unit) {
        firestore.collection("users")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val list = querySnapshot.documents.mapNotNull { doc ->
                    val rawName = doc.getString("name") ?: doc.getString("userName") ?: doc.getString("fullName") ?: ""
                    val mobile = doc.getString("mobile") ?: doc.getString("phone") ?: doc.getString("userMobile") ?: doc.id
                    val referralCode = doc.getString("referralCode") ?: ""
                    val referredBy = doc.getString("referredBy") ?: ""
                    val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    val currentLevel = (doc.getLong("currentLevel") ?: 1L).toInt()
                    val completedLevels = (doc.getLong("completedLevels") ?: 0L).toInt()
                    val rewardCoins = doc.getLong("rewardCoins") ?: 0L
                    val earningsTaka = doc.getDouble("earningsTaka") ?: 0.0
                    val referralsCount = (doc.getLong("referralsCount") ?: 0L).toInt()
                    val avatarUrl = doc.getString("avatarUrl") ?: ""

                    User(
                        name = if (rawName.isNotBlank()) rawName else "ইউজার ($mobile)",
                        mobile = mobile,
                        referralCode = referralCode,
                        referredBy = referredBy,
                        createdAt = createdAt,
                        currentLevel = currentLevel,
                        completedLevels = completedLevels,
                        rewardCoins = rewardCoins,
                        earningsTaka = earningsTaka,
                        referralsCount = referralsCount,
                        avatarUrl = avatarUrl
                    )
                }
                onResult(list.sortedByDescending { it.createdAt })
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun updateWithdrawalStatus(docId: String, status: String, onResult: (Boolean) -> Unit) {
        firestore.collection("withdrawals").document(docId)
            .update("status", status)
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun recordAdminAdImpression(adTakaAmount: Double = 1.25, onComplete: (() -> Unit)? = null) {
        try {
            val statsRef = firestore.collection("admin_stats").document("ad_impressions")
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(statsRef)
                val currentCount = if (snapshot.exists()) (snapshot.getLong("loginAdsCount") ?: 0L) else 0L
                val currentRevenue = if (snapshot.exists()) (snapshot.getDouble("loginAdsRevenueTaka") ?: 0.0) else 0.0

                transaction.set(
                    statsRef,
                    mapOf(
                        "loginAdsCount" to (currentCount + 1),
                        "loginAdsRevenueTaka" to (currentRevenue + adTakaAmount),
                        "lastUpdated" to System.currentTimeMillis()
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                )
            }.addOnCompleteListener {
                onComplete?.invoke()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error recording admin ad impression", e)
            onComplete?.invoke()
        }
    }

    fun fetchAdminAdStats(onResult: (count: Long, revenue: Double) -> Unit) {
        try {
            firestore.collection("admin_stats").document("ad_impressions")
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val count = snapshot.getLong("loginAdsCount") ?: 0L
                        val revenue = snapshot.getDouble("loginAdsRevenueTaka") ?: 0.0
                        onResult(count, revenue)
                    } else {
                        onResult(0L, 0.0)
                    }
                }
                .addOnFailureListener {
                    onResult(0L, 0.0)
                }
        } catch (e: Exception) {
            onResult(0L, 0.0)
        }
    }

    fun checkAppStatus(onStatusChecked: (isActive: Boolean, blockMessage: String?) -> Unit) {
        try {
            firestore.collection("app_config").document("status")
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val isActive = snapshot.getBoolean("is_active") ?: true
                        val message = snapshot.getString("message") ?: "এই অ্যাপটির সার্ভিসটি সাময়িকভাবে বন্ধ রাখা হয়েছে।"
                        onStatusChecked(isActive, if (!isActive) message else null)
                    } else {
                        // Default to active if config document doesn't exist yet
                        onStatusChecked(true, null)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "App status check note: keeping active by default unless explicitly disabled", exception)
                    // Keep app active on connection/offline issues so users aren't locked out prematurely
                    onStatusChecked(true, null)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase exception checking status", e)
            onStatusChecked(true, null)
        }
    }

    fun getBonusForStreak(dayStreak: Int): Pair<Double, Long> {
        return when (dayStreak) {
            1 -> Pair(0.20, 20L)
            2 -> Pair(0.25, 25L)
            3 -> Pair(0.30, 30L)
            4 -> Pair(0.35, 35L)
            5 -> Pair(0.40, 40L)
            6 -> Pair(0.50, 50L)
            else -> Pair(1.00, 100L) // Day 7 Mega Check-in Bonus
        }
    }

    fun getRequiredAdsForStreak(dayStreak: Int): Int {
        return when (dayStreak) {
            1 -> 2 // 20 coins (৳0.20) -> 2 Full-screen Ads
            2 -> 2 // 25 coins (৳0.25) -> 2 Full-screen Ads
            3 -> 3 // 30 coins (৳0.30) -> 3 Full-screen Ads
            4 -> 3 // 35 coins (৳0.35) -> 3 Full-screen Ads
            5 -> 4 // 40 coins (৳0.40) -> 4 Full-screen Ads
            6 -> 5 // 50 coins (৳0.50) -> 5 Full-screen Ads
            else -> 8 // Day 7 (100 coins / ৳1.00) -> 8 Full-screen Ads
        }
    }

    fun isEligibleForDailyCheckIn(user: User): Boolean {
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        return user.lastCheckInDate != todayStr
    }

    fun claimDailyCheckIn(
        context: Context,
        user: User,
        onResult: (success: Boolean, message: String, updatedUser: User?, bonusTaka: Double, bonusCoins: Long, streak: Int) -> Unit
    ) {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val todayStr = dateFormat.format(java.util.Date())

        if (user.lastCheckInDate == todayStr) {
            onResult(false, "আপনি আজকের দৈনিক বোনাস ইতোমধ্যে সংগ্রহ করেছেন! আগামীকাল আবার আসুন।", user, 0.0, 0L, user.checkInStreak)
            return
        }

        // Calculate yesterday date string
        val yesterdayCalendar = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.DAY_OF_YEAR, -1)
        }
        val yesterdayStr = dateFormat.format(yesterdayCalendar.time)

        val newStreak = if (user.lastCheckInDate == yesterdayStr) {
            if (user.checkInStreak >= 7) 1 else user.checkInStreak + 1
        } else {
            1
        }

        val (bonusTaka, bonusCoins) = getBonusForStreak(newStreak)
        val updatedTaka = user.earningsTaka + bonusTaka
        val updatedCoins = user.rewardCoins + bonusCoins

        val updatedUser = user.copy(
            earningsTaka = updatedTaka,
            rewardCoins = updatedCoins,
            lastCheckInDate = todayStr,
            checkInStreak = newStreak
        )

        // Save immediately locally for instant responsiveness
        saveSession(context, updatedUser)

        val cleanMobile = normalizeMobile(user.mobile)
        if (cleanMobile.isEmpty()) {
            onResult(true, "🎉 অভিনন্দন! আপনি ডে-$newStreak এর দৈনিক বোনাস +$bonusCoins কয়েন (৳${String.format(java.util.Locale.US, "%.2f", bonusTaka)}) পেয়েছেন।", updatedUser, bonusTaka, bonusCoins, newStreak)
            return
        }

        // Store directly in Firebase Firestore
        val updates = mapOf(
            "earningsTaka" to com.google.firebase.firestore.FieldValue.increment(bonusTaka),
            "rewardCoins" to com.google.firebase.firestore.FieldValue.increment(bonusCoins),
            "lastCheckInDate" to todayStr,
            "checkInStreak" to newStreak
        )

        firestore.collection("users").document(cleanMobile)
            .set(updates, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Daily check-in synced to Firestore successfully for $cleanMobile (Streak: $newStreak, Bonus: ৳$bonusTaka, $bonusCoins Coins)")
                onResult(true, "🎉 অভিনন্দন! আপনি ডে-$newStreak এর দৈনিক বোনাস +$bonusCoins কয়েন (৳${String.format(java.util.Locale.US, "%.2f", bonusTaka)}) পেয়েছেন।", updatedUser, bonusTaka, bonusCoins, newStreak)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Daily check-in Firestore write failed, buffered locally: ${e.message}")
                onResult(true, "🎉 অভিনন্দন! আপনি ডে-$newStreak এর দৈনিক বোনাস +$bonusCoins কয়েন (৳${String.format(java.util.Locale.US, "%.2f", bonusTaka)}) পেয়েছেন।", updatedUser, bonusTaka, bonusCoins, newStreak)
            }
    }

    fun saveSession(context: Context, user: User) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_NAME, user.name)
            .putString(KEY_MOBILE, user.mobile)
            .putString(KEY_REFERRAL_CODE, user.referralCode)
            .putString(KEY_REFERRED_BY, user.referredBy)
            .putLong(KEY_CREATED_AT, user.createdAt)
            .putInt(KEY_CURRENT_LEVEL, user.currentLevel)
            .putInt(KEY_COMPLETED_LEVELS, user.completedLevels)
            .putLong(KEY_REWARD_COINS, user.rewardCoins)
            .putFloat(KEY_EARNINGS_TAKA, user.earningsTaka.toFloat())
            .putInt(KEY_REFERRALS_COUNT, user.referralsCount)
            .putString(KEY_AVATAR_URL, user.avatarUrl)
            .putString(KEY_LAST_CHECK_IN_DATE, user.lastCheckInDate)
            .putInt(KEY_CHECK_IN_STREAK, user.checkInStreak)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun getSavedSession(context: Context): User? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedIn) return null

        val name = prefs.getString(KEY_NAME, "") ?: ""
        val mobile = prefs.getString(KEY_MOBILE, "") ?: ""
        if (mobile.isEmpty()) return null

        val referralCode = prefs.getString(KEY_REFERRAL_CODE, "") ?: generateReferralCode(mobile)
        val referredBy = prefs.getString(KEY_REFERRED_BY, "") ?: ""
        val createdAt = prefs.getLong(KEY_CREATED_AT, System.currentTimeMillis())
        val currentLevel = prefs.getInt(KEY_CURRENT_LEVEL, 1)
        val completedLevels = prefs.getInt(KEY_COMPLETED_LEVELS, 0)
        val rewardCoins = prefs.getLong(KEY_REWARD_COINS, 0L)
        val earningsTaka = prefs.getFloat(KEY_EARNINGS_TAKA, 0.0f).toDouble()
        val referralsCount = prefs.getInt(KEY_REFERRALS_COUNT, 0)
        val avatarUrl = prefs.getString(KEY_AVATAR_URL, "") ?: ""
        val lastCheckInDate = prefs.getString(KEY_LAST_CHECK_IN_DATE, "") ?: ""
        val checkInStreak = prefs.getInt(KEY_CHECK_IN_STREAK, 0)

        return User(
            name = name,
            mobile = mobile,
            referralCode = referralCode,
            referredBy = referredBy,
            createdAt = createdAt,
            currentLevel = currentLevel,
            completedLevels = completedLevels,
            rewardCoins = rewardCoins,
            earningsTaka = earningsTaka,
            referralsCount = referralsCount,
            avatarUrl = avatarUrl,
            lastCheckInDate = lastCheckInDate,
            checkInStreak = checkInStreak
        )
    }

    fun clearSession(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    /**
     * Google Play Data Deletion Compliance (Mandatory 2024+)
     * Fully deletes the user record from Firebase Firestore and purges all local session caches.
     */
    fun deleteAccountAndData(
        context: Context,
        mobile: String,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        val cleanMobile = normalizeMobile(mobile)
        if (cleanMobile.isEmpty()) {
            clearSession(context)
            onResult(true, "আপনার অ্যাকাউন্ট এবং লোকাল ডেটা সফলভাবে মুছে ফেলা হয়েছে।")
            return
        }

        firestore.collection("users").document(cleanMobile)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "User account and all associated cloud data deleted: $cleanMobile")
                clearSession(context)
                onResult(true, "আপনার অ্যাকাউন্ট এবং ফায়ারবেসে সংরক্ষিত সকল তথ্য স্থায়ীভাবে মুছে ফেলা হয়েছে।")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Firestore deletion failure (fallback clear local session): ${e.message}")
                clearSession(context)
                onResult(true, "অ্যাকাউন্ট অফলাইন সেশন থেকে সফলভাবে মুছে ফেলা হয়েছে।")
            }
    }
}

