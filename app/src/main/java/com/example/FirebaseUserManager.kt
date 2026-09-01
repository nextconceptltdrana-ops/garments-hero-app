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
    private const val KEY_HAS_EARNED_REFERRAL_BONUS = "user_has_earned_referral_bonus"
    private const val KEY_EARNED_REFERRAL_WITHDRAW_BONUS = "user_earned_referral_withdraw_bonus"
    private const val KEY_IS_BANNED = "user_is_banned"
    private const val KEY_BAN_REASON = "user_ban_reason"
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
                    earningsTaka = 0.0,
                    hasEarnedReferralBonus = false
                )

                // Save user to Firebase Firestore
                userDocRef.set(newUser)
                    .addOnSuccessListener {
                        saveSession(context, newUser)
                        Log.d(TAG, "User registered successfully in Firestore: $cleanMobile")

                        // Process initial 5 Taka referral bonus for referrer if code was supplied
                        if (cleanReferral.isNotEmpty()) {
                            firestore.collection("users")
                                .whereEqualTo("referralCode", cleanReferral)
                                .get()
                                .addOnSuccessListener { refQuery ->
                                    if (!refQuery.isEmpty) {
                                        val refDoc = refQuery.documents[0]
                                        refDoc.reference.update(
                                            mapOf(
                                                "referralsCount" to com.google.firebase.firestore.FieldValue.increment(1),
                                                "earningsTaka" to com.google.firebase.firestore.FieldValue.increment(5.0),
                                                "rewardCoins" to com.google.firebase.firestore.FieldValue.increment(500)
                                            )
                                        )
                                    } else {
                                        // Also try matching by mobile number in case they inputted phone
                                        firestore.collection("users").document(cleanReferral).get()
                                            .addOnSuccessListener { phoneDoc ->
                                                if (phoneDoc.exists()) {
                                                    phoneDoc.reference.update(
                                                        mapOf(
                                                            "referralsCount" to com.google.firebase.firestore.FieldValue.increment(1),
                                                            "earningsTaka" to com.google.firebase.firestore.FieldValue.increment(5.0),
                                                            "rewardCoins" to com.google.firebase.firestore.FieldValue.increment(500)
                                                        )
                                                    )
                                                }
                                            }
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
                earningsTaka = 0.0,
                hasEarnedReferralBonus = false
            )
            saveSession(context, newUser)
            onResult(true, "নিবন্ধন সম্পন্ন হয়েছে!", newUser)
        }
    }

    fun parseUserFromSnapshot(snapshot: com.google.firebase.firestore.DocumentSnapshot, fallbackMobile: String = ""): User {
        val mobile = snapshot.getString("mobile") ?: snapshot.id.ifEmpty { fallbackMobile }
        val refCount = (snapshot.getLong("referralsCount") ?: 0L).toInt()
        val hasEarnedReferralBonus = snapshot.getBoolean("hasEarnedReferralBonus") ?: false
        val earnedReferralWithdrawBonus = snapshot.getDouble("earnedReferralWithdrawBonus") ?: (if (hasEarnedReferralBonus) 50.0 else 0.0)
        val isBanned = snapshot.getBoolean("isBanned") ?: false
        val banReason = snapshot.getString("banReason") ?: ""
        
        val rawCurrentLevel = (snapshot.getLong("currentLevel") ?: 1L).toInt().coerceAtLeast(1)
        val rawCompletedLevels = (snapshot.getLong("completedLevels") ?: 0L).toInt().coerceAtLeast(0)
        // Guaranteed synchronization: active currentLevel is always completedLevels + 1 (or max with stored currentLevel)
        val effectiveCurrentLevel = maxOf(rawCurrentLevel, rawCompletedLevels + 1)
        val effectiveCompletedLevels = effectiveCurrentLevel - 1

        return User(
            name = snapshot.getString("name") ?: "ব্যবহারকারী",
            mobile = mobile,
            referralCode = snapshot.getString("referralCode") ?: generateReferralCode(mobile),
            referredBy = snapshot.getString("referredBy") ?: "",
            createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis(),
            currentLevel = effectiveCurrentLevel,
            completedLevels = effectiveCompletedLevels,
            rewardCoins = snapshot.getLong("rewardCoins") ?: 0L,
            earningsTaka = snapshot.getDouble("earningsTaka") ?: 0.0,
            referralsCount = refCount,
            avatarUrl = snapshot.getString("avatarUrl") ?: "",
            lastCheckInDate = snapshot.getString("lastCheckInDate") ?: "",
            checkInStreak = (snapshot.getLong("checkInStreak") ?: 0L).toInt(),
            hasEarnedReferralBonus = hasEarnedReferralBonus,
            earnedReferralWithdrawBonus = earnedReferralWithdrawBonus,
            isBanned = isBanned,
            banReason = banReason
        )
    }

    fun listenToUser(
        context: Context,
        mobile: String,
        onUserUpdated: (User) -> Unit
    ): com.google.firebase.firestore.ListenerRegistration? {
        if (mobile.isEmpty()) return null
        val cleanMobile = normalizeMobile(mobile)
        return firestore.collection("users").document(cleanMobile)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    return@addSnapshotListener
                }
                val user = parseUserFromSnapshot(snapshot, cleanMobile)
                saveSession(context, user)
                onUserUpdated(user)
            }
    }

    fun syncAndFetchReferredUsers(
        context: Context,
        user: User,
        onResult: (updatedUser: User, referredList: List<User>) -> Unit
    ) {
        if (user.mobile.isEmpty()) {
            onResult(user, emptyList())
            return
        }

        val myCode = user.referralCode.trim().uppercase()
        val myMobile = user.mobile.trim()

        firestore.collection("users")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val referredList = mutableListOf<User>()
                for (doc in querySnapshot.documents) {
                    if (doc.id == user.mobile) continue
                    val docReferredBy = (doc.getString("referredBy") ?: "").trim().uppercase()
                    if (docReferredBy.isNotEmpty() && (docReferredBy == myCode || docReferredBy == myMobile)) {
                        referredList.add(parseUserFromSnapshot(doc))
                    }
                }

                val actualCount = referredList.size
                if (actualCount > user.referralsCount) {
                    val missingBonusCount = actualCount - user.referralsCount
                    val extraTaka = missingBonusCount * 5.0
                    val extraCoins = (missingBonusCount * 500).toLong()

                    val newTotalTaka = user.earningsTaka + extraTaka
                    val newTotalCoins = user.rewardCoins + extraCoins

                    firestore.collection("users").document(user.mobile)
                        .update(
                            mapOf(
                                "referralsCount" to actualCount,
                                "earningsTaka" to newTotalTaka,
                                "rewardCoins" to newTotalCoins
                            )
                        )

                    val correctedUser = user.copy(
                        referralsCount = actualCount,
                        earningsTaka = newTotalTaka,
                        rewardCoins = newTotalCoins
                    )
                    saveSession(context, correctedUser)
                    onResult(correctedUser, referredList.sortedByDescending { it.createdAt })
                } else {
                    onResult(user, referredList.sortedByDescending { it.createdAt })
                }
            }
            .addOnFailureListener {
                onResult(user, emptyList())
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
                val user = parseUserFromSnapshot(snapshot, cleanMobile)
                saveSession(context, user)
                Log.d(TAG, "User logged in successfully from Firestore: $cleanMobile (referrals: ${user.referralsCount})")
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
        val currentActiveLevel = maxOf(user.currentLevel, user.completedLevels + 1)
        val nextLevel = currentActiveLevel + 1
        val updatedCompleted = currentActiveLevel
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
        val storedCurrentLevel = prefs.getInt(KEY_CURRENT_LEVEL, 1)
        val storedCompletedLevels = prefs.getInt(KEY_COMPLETED_LEVELS, 0)
        val effectiveCurrentLevel = maxOf(storedCurrentLevel, storedCompletedLevels + 1)
        val effectiveCompletedLevels = effectiveCurrentLevel - 1

        if (pendingTaka <= 0.0 && pendingCoins <= 0L && pendingLevels <= 0) {
            Log.d(TAG, "No pending points to sync. Firestore write quota preserved.")
            onComplete?.invoke(true)
            return
        }

        Log.d(TAG, "Syncing batch progress to Firestore for $mobile: +$pendingTaka Taka, +$pendingCoins Coins, +$pendingLevels Levels (Current Level: $effectiveCurrentLevel, Completed: $effectiveCompletedLevels)")

        val updates = mapOf(
            "earningsTaka" to com.google.firebase.firestore.FieldValue.increment(pendingTaka),
            "rewardCoins" to com.google.firebase.firestore.FieldValue.increment(pendingCoins),
            "completedLevels" to effectiveCompletedLevels.toLong(),
            "currentLevel" to effectiveCurrentLevel.toLong()
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
                "status" to "PENDING",
                "referredBy" to user.referredBy,
                "hasEarnedReferralBonus" to user.hasEarnedReferralBonus,
                "earnedReferralWithdrawBonus" to user.earnedReferralWithdrawBonus,
                "referralBonusAmount" to 0.0
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
                        status = doc.getString("status") ?: "PENDING",
                        referredBy = doc.getString("referredBy") ?: "",
                        hasEarnedReferralBonus = doc.getBoolean("hasEarnedReferralBonus") ?: false,
                        referralBonusAmount = doc.getDouble("referralBonusAmount") ?: 0.0
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
                    val hasEarnedReferralBonus = doc.getBoolean("hasEarnedReferralBonus") ?: false
                    val earnedReferralWithdrawBonus = doc.getDouble("earnedReferralWithdrawBonus") ?: (if (hasEarnedReferralBonus) 50.0 else 0.0)
                    val isBanned = doc.getBoolean("isBanned") ?: false
                    val banReason = doc.getString("banReason") ?: ""

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
                        avatarUrl = avatarUrl,
                        hasEarnedReferralBonus = hasEarnedReferralBonus,
                        earnedReferralWithdrawBonus = earnedReferralWithdrawBonus,
                        isBanned = isBanned,
                        banReason = banReason
                    )
                }
                onResult(list.sortedByDescending { it.createdAt })
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    private fun findReferrerRef(referrerCodeOrMobile: String, onFound: (com.google.firebase.firestore.DocumentReference?) -> Unit) {
        val clean = referrerCodeOrMobile.trim()
        if (clean.isEmpty()) {
            onFound(null)
            return
        }

        firestore.collection("users").whereEqualTo("referralCode", clean.uppercase())
            .get()
            .addOnSuccessListener { query ->
                if (!query.isEmpty) {
                    onFound(query.documents[0].reference)
                } else {
                    // Try direct mobile match
                    val normalized = normalizeMobile(clean)
                    val directRef = firestore.collection("users").document(if (normalized.isNotEmpty()) normalized else clean)
                    directRef.get().addOnSuccessListener { phoneSnap ->
                        if (phoneSnap.exists()) {
                            onFound(directRef)
                        } else {
                            onFound(null)
                        }
                    }.addOnFailureListener {
                        onFound(null)
                    }
                }
            }
            .addOnFailureListener {
                onFound(null)
            }
    }

    fun updateWithdrawalStatus(docId: String, status: String, onResult: (Boolean) -> Unit) {
        val withdrawDocRef = firestore.collection("withdrawals").document(docId)

        withdrawDocRef.get().addOnSuccessListener { withdrawSnap ->
            if (!withdrawSnap.exists()) {
                onResult(false)
                return@addOnSuccessListener
            }

            val userMobile = withdrawSnap.getString("userMobile") ?: withdrawSnap.getString("mobile") ?: ""
            val userName = withdrawSnap.getString("userName") ?: "ইউজার"
            val amountTaka = withdrawSnap.getDouble("amountTaka") ?: 0.0
            val paymentMethod = withdrawSnap.getString("paymentMethod") ?: "বিকাশ"
            val paymentNumber = withdrawSnap.getString("paymentNumber") ?: ""
            val currentStatus = withdrawSnap.getString("status") ?: "PENDING"

            if (status == "APPROVED" && currentStatus != "APPROVED") {
                // Read User Document to verify if user has a 'referredBy' ID
                val userDocRef = firestore.collection("users").document(userMobile)
                userDocRef.get().addOnSuccessListener { userSnap ->
                    val referredBy = (userSnap.getString("referredBy") ?: withdrawSnap.getString("referredBy") ?: "").trim()

                    // Verify if user has a valid 'referredBy' ID for 5% lifetime referral commission
                    if (referredBy.isNotEmpty()) {
                        findReferrerRef(referredBy) { referrerRef ->
                            if (referrerRef != null) {
                                // Firestore Transaction mechanism to ensure atomic 5% lifetime commission crediting
                                firestore.runTransaction { transaction ->
                                    // 1. Transactional Reads first (mandatory in Firestore transactions)
                                    val freshUserSnap = transaction.get(userDocRef)
                                    val freshReferrerSnap = transaction.get(referrerRef)
                                    val freshWithdrawSnap = transaction.get(withdrawDocRef)

                                    val currentEarnedBonus = freshUserSnap.getDouble("earnedReferralWithdrawBonus") ?: 0.0

                                    // Calculate 5% lifetime referral commission without any maximum cap
                                    val calculatedCommission = amountTaka * 0.05
                                    val coinsToGrant = (calculatedCommission * 100).toLong() // 1 Taka = 100 Coins
                                    val newTotalEarnedBonus = currentEarnedBonus + calculatedCommission

                                    // 2. Transactional Writes
                                    // Grant 5% lifetime commission balance to Referrer
                                    val currentRefTaka = freshReferrerSnap.getDouble("earningsTaka") ?: 0.0
                                    val currentRefCoins = freshReferrerSnap.getLong("rewardCoins") ?: 0L
                                    transaction.update(
                                        referrerRef,
                                        mapOf(
                                            "earningsTaka" to (currentRefTaka + calculatedCommission),
                                            "rewardCoins" to (currentRefCoins + coinsToGrant)
                                        )
                                    )

                                    // Update total referral withdraw commission earned on User document
                                    transaction.update(
                                        userDocRef,
                                        mapOf(
                                            "earnedReferralWithdrawBonus" to newTotalEarnedBonus,
                                            "hasEarnedReferralBonus" to true
                                        )
                                    )

                                    // Update Withdrawal Document with approval and 5% lifetime commission details
                                    transaction.update(
                                        withdrawDocRef,
                                        mapOf(
                                            "status" to "APPROVED",
                                            "hasEarnedReferralBonus" to true,
                                            "referralBonusGranted" to true,
                                            "referralBonusAmount" to calculatedCommission,
                                            "approvedAt" to System.currentTimeMillis()
                                        )
                                    )
                                }.addOnSuccessListener {
                                    Log.d(TAG, "5% Lifetime Referral Commission transaction completed for user $userMobile (referred by $referredBy)!")

                                    val commissionAwarded = amountTaka * 0.05
                                    val coinsAwarded = (commissionAwarded * 100).toLong()

                                    // Add Notification & Transaction Record for Referrer
                                    val referrerNotification = mapOf(
                                        "userMobile" to referrerRef.id,
                                        "title" to "🎉 ৫% আজীবন রেফারেল কমিশন!",
                                        "message" to "আপনার রেফারকৃত মেম্বার $userName ($userMobile) এর ৳${String.format(java.util.Locale.US, "%.2f", amountTaka)} উইথড্র সফল হয়েছে। ৫% আজীবন কমিশন হিসেবে আপনার অ্যাকাউন্টে ৳${String.format(java.util.Locale.US, "%.2f", commissionAwarded)} ($coinsAwarded কয়েন) জমা হয়েছে!",
                                        "amountTaka" to commissionAwarded,
                                        "type" to "REFERRAL_BONUS",
                                        "timestamp" to System.currentTimeMillis(),
                                        "isRead" to false
                                    )
                                    firestore.collection("notifications").add(referrerNotification)

                                    // Add Notification for Withdrawing User
                                    val userNotification = mapOf(
                                        "userMobile" to userMobile,
                                        "title" to "✅ আপনার ৳${String.format(java.util.Locale.US, "%.2f", amountTaka)} উইথড্র সফল হয়েছে!",
                                        "message" to "আপনার $paymentMethod নম্বর ($paymentNumber)-এ ৳${String.format(java.util.Locale.US, "%.2f", amountTaka)} পেমেন্ট পাঠানো হয়েছে।",
                                        "amountTaka" to amountTaka,
                                        "type" to "WITHDRAWAL_SUCCESS",
                                        "timestamp" to System.currentTimeMillis(),
                                        "isRead" to false
                                    )
                                    firestore.collection("notifications").add(userNotification)

                                    onResult(true)
                                }.addOnFailureListener { e ->
                                    Log.e(TAG, "Transaction error in 5% referral commission: ${e.message}")
                                    // Fallback approve without failing entire withdrawal
                                    withdrawDocRef.update(
                                        mapOf(
                                            "status" to "APPROVED",
                                            "approvedAt" to System.currentTimeMillis()
                                        )
                                    ).addOnSuccessListener { onResult(true) }
                                     .addOnFailureListener { onResult(false) }
                                }
                            } else {
                                // Referrer profile not found, standard approval
                                withdrawDocRef.update(
                                    mapOf(
                                        "status" to "APPROVED",
                                        "approvedAt" to System.currentTimeMillis()
                                    )
                                ).addOnSuccessListener { onResult(true) }
                                 .addOnFailureListener { onResult(false) }
                            }
                        }
                    } else {
                        // Standard Approval without Referral Commission (no referrer ID)
                        withdrawDocRef.update(
                            mapOf(
                                "status" to "APPROVED",
                                "hasEarnedReferralBonus" to false,
                                "referralBonusGranted" to false,
                                "referralBonusAmount" to 0.0,
                                "approvedAt" to System.currentTimeMillis()
                            )
                        ).addOnSuccessListener {
                            // Add Notification for Withdrawing User
                            val userNotification = mapOf(
                                "userMobile" to userMobile,
                                "title" to "✅ আপনার ৳${String.format(java.util.Locale.US, "%.2f", amountTaka)} উইথড্র সফল হয়েছে!",
                                "message" to "আপনার $paymentMethod নম্বর ($paymentNumber)-এ ৳${String.format(java.util.Locale.US, "%.2f", amountTaka)} পেমেন্ট পাঠানো হয়েছে।",
                                "amountTaka" to amountTaka,
                                "type" to "WITHDRAWAL_SUCCESS",
                                "timestamp" to System.currentTimeMillis(),
                                "isRead" to false
                            )
                            firestore.collection("notifications").add(userNotification)
                            onResult(true)
                        }.addOnFailureListener {
                            onResult(false)
                        }
                    }
                }.addOnFailureListener {
                    withdrawDocRef.update("status", "APPROVED")
                        .addOnSuccessListener { onResult(true) }
                        .addOnFailureListener { onResult(false) }
                }
            } else if (status == "REJECTED" && currentStatus != "REJECTED") {
                // Reject and refund user balance
                val coinsToRefund = (amountTaka * 100).toLong()
                firestore.runTransaction { transaction ->
                    val userDocRef = firestore.collection("users").document(userMobile)
                    transaction.update(
                        userDocRef,
                        mapOf(
                            "earningsTaka" to com.google.firebase.firestore.FieldValue.increment(amountTaka),
                            "rewardCoins" to com.google.firebase.firestore.FieldValue.increment(coinsToRefund)
                        )
                    )
                    transaction.update(
                        withdrawDocRef,
                        mapOf(
                            "status" to "REJECTED",
                            "rejectedAt" to System.currentTimeMillis()
                        )
                    )
                }.addOnSuccessListener {
                    val userNotification = mapOf(
                        "userMobile" to userMobile,
                        "title" to "❌ উইথড্র রিকোয়েস্ট বাতিল করা হয়েছে",
                        "message" to "আপনার ৳${String.format(java.util.Locale.US, "%.2f", amountTaka)} উইথড্র রিকোয়েস্টটি বাতিল হয়েছে এবং টাকা আপনার মূল অ্যাকাউন্টে ফেরত দেওয়া হয়েছে।",
                        "amountTaka" to amountTaka,
                        "type" to "WITHDRAWAL_REJECTED",
                        "timestamp" to System.currentTimeMillis(),
                        "isRead" to false
                    )
                    firestore.collection("notifications").add(userNotification)
                    onResult(true)
                }.addOnFailureListener {
                    withdrawDocRef.update("status", "REJECTED")
                        .addOnSuccessListener { onResult(true) }
                        .addOnFailureListener { onResult(false) }
                }
            } else {
                withdrawDocRef.update("status", status)
                    .addOnSuccessListener { onResult(true) }
                    .addOnFailureListener { onResult(false) }
            }
        }.addOnFailureListener {
            onResult(false)
        }
    }

    fun fetchUserNotifications(userMobile: String, onResult: (List<AppNotification>) -> Unit) {
        val cleanMobile = normalizeMobile(userMobile)
        if (cleanMobile.isEmpty()) {
            onResult(emptyList())
            return
        }

        firestore.collection("notifications")
            .whereEqualTo("userMobile", cleanMobile)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val list = querySnapshot.documents.map { doc ->
                    AppNotification(
                        id = doc.id,
                        userMobile = doc.getString("userMobile") ?: "",
                        title = doc.getString("title") ?: "",
                        message = doc.getString("message") ?: "",
                        amountTaka = doc.getDouble("amountTaka") ?: 0.0,
                        type = doc.getString("type") ?: "REFERRAL_BONUS",
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        isRead = doc.getBoolean("isRead") ?: false
                    )
                }
                onResult(list.sortedByDescending { it.timestamp })
            }
            .addOnFailureListener {
                onResult(emptyList())
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

    fun getAdminConfig(onResult: (whatsappNumber: String, whatsappGroupLink: String, noticeText: String, isNoticeActive: Boolean) -> Unit) {
        try {
            firestore.collection("app_config").document("general")
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val whatsapp = snapshot.getString("whatsapp_number") ?: "01919085229"
                        val groupLink = snapshot.getString("whatsapp_group_link") ?: "https://chat.whatsapp.com/CFweFxYB7Fk7X3sWJljF5b"
                        val notice = snapshot.getString("notice_text") ?: "গার্মেন্টস হিরো অ্যাপে স্বাগতম! নিয়মিত কুইজ খেলে ও রেফার করে ইনকাম করুন।"
                        val isNoticeActive = snapshot.getBoolean("is_notice_active") ?: true
                        onResult(whatsapp, groupLink, notice, isNoticeActive)
                    } else {
                        onResult("01919085229", "https://chat.whatsapp.com/CFweFxYB7Fk7X3sWJljF5b", "গার্মেন্টস হিরো অ্যাপে স্বাগতম! নিয়মিত কুইজ খেলে ও রেফার করে ইনকাম করুন।", true)
                    }
                }
                .addOnFailureListener {
                    onResult("01919085229", "https://chat.whatsapp.com/CFweFxYB7Fk7X3sWJljF5b", "গার্মেন্টস হিরো অ্যাপে স্বাগতম! নিয়মিত কুইজ খেলে ও রেফার করে ইনকাম করুন।", true)
                }
        } catch (e: Exception) {
            onResult("01919085229", "https://chat.whatsapp.com/CFweFxYB7Fk7X3sWJljF5b", "গার্মেন্টস হিরো অ্যাপে স্বাগতম! নিয়মিত কুইজ খেলে ও রেফার করে ইনকাম করুন।", true)
        }
    }

    fun updateAdminConfig(
        whatsappNumber: String,
        whatsappGroupLink: String,
        noticeText: String,
        isNoticeActive: Boolean,
        onComplete: (Boolean) -> Unit
    ) {
        try {
            val data = mapOf(
                "whatsapp_number" to whatsappNumber,
                "whatsapp_group_link" to whatsappGroupLink,
                "notice_text" to noticeText,
                "is_notice_active" to isNoticeActive,
                "updated_at" to System.currentTimeMillis()
            )
            firestore.collection("app_config").document("general")
                .set(data, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener {
                    onComplete(true)
                }
                .addOnFailureListener {
                    onComplete(false)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating admin config", e)
            onComplete(false)
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
            1 -> Pair(0.03, 3L)
            2 -> Pair(0.03, 3L)
            3 -> Pair(0.04, 4L)
            4 -> Pair(0.04, 4L)
            5 -> Pair(0.05, 5L)
            6 -> Pair(0.06, 6L)
            else -> Pair(0.08, 8L) // Day 7 Mega Check-in Bonus (8 Coins = ৳0.08)
        }
    }

    fun getRequiredAdsForStreak(dayStreak: Int): Int {
        return when (dayStreak) {
            1 -> 4 // 3 coins (৳0.03) -> 4 Full-screen Ads
            2 -> 4 // 3 coins (৳0.03) -> 4 Full-screen Ads
            3 -> 5 // 4 coins (৳0.04) -> 5 Full-screen Ads
            4 -> 5 // 4 coins (৳0.04) -> 5 Full-screen Ads
            5 -> 6 // 5 coins (৳0.05) -> 6 Full-screen Ads
            6 -> 7 // 6 coins (৳0.06) -> 7 Full-screen Ads
            else -> 10 // Day 7 (8 coins / ৳0.08) -> 10 Full-screen Ads
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
            .putBoolean(KEY_HAS_EARNED_REFERRAL_BONUS, user.hasEarnedReferralBonus)
            .putFloat(KEY_EARNED_REFERRAL_WITHDRAW_BONUS, user.earnedReferralWithdrawBonus.toFloat())
            .putBoolean(KEY_IS_BANNED, user.isBanned)
            .putString(KEY_BAN_REASON, user.banReason)
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
        val storedCurrentLevel = prefs.getInt(KEY_CURRENT_LEVEL, 1).coerceAtLeast(1)
        val storedCompletedLevels = prefs.getInt(KEY_COMPLETED_LEVELS, 0).coerceAtLeast(0)
        val effectiveCurrentLevel = maxOf(storedCurrentLevel, storedCompletedLevels + 1)
        val effectiveCompletedLevels = effectiveCurrentLevel - 1

        val rewardCoins = prefs.getLong(KEY_REWARD_COINS, 0L)
        val earningsTaka = prefs.getFloat(KEY_EARNINGS_TAKA, 0.0f).toDouble()
        val referralsCount = prefs.getInt(KEY_REFERRALS_COUNT, 0)
        val avatarUrl = prefs.getString(KEY_AVATAR_URL, "") ?: ""
        val lastCheckInDate = prefs.getString(KEY_LAST_CHECK_IN_DATE, "") ?: ""
        val checkInStreak = prefs.getInt(KEY_CHECK_IN_STREAK, 0)
        val hasEarnedReferralBonus = prefs.getBoolean(KEY_HAS_EARNED_REFERRAL_BONUS, false)
        val earnedReferralWithdrawBonus = prefs.getFloat(KEY_EARNED_REFERRAL_WITHDRAW_BONUS, if (hasEarnedReferralBonus) 50.0f else 0.0f).toDouble()
        val isBanned = prefs.getBoolean(KEY_IS_BANNED, false)
        val banReason = prefs.getString(KEY_BAN_REASON, "") ?: ""

        return User(
            name = name,
            mobile = mobile,
            referralCode = referralCode,
            referredBy = referredBy,
            createdAt = createdAt,
            currentLevel = effectiveCurrentLevel,
            completedLevels = effectiveCompletedLevels,
            rewardCoins = rewardCoins,
            earningsTaka = earningsTaka,
            referralsCount = referralsCount,
            avatarUrl = avatarUrl,
            lastCheckInDate = lastCheckInDate,
            checkInStreak = checkInStreak,
            hasEarnedReferralBonus = hasEarnedReferralBonus,
            earnedReferralWithdrawBonus = earnedReferralWithdrawBonus,
            isBanned = isBanned,
            banReason = banReason
        )
    }

    /**
     * Admin Functionality: Directly update a user's balance, coins, level, name, or referral details
     */
    fun adminUpdateUser(
        userMobile: String,
        updatedName: String,
        updatedCurrentLevel: Int,
        updatedEarningsTaka: Double,
        updatedRewardCoins: Long,
        updatedReferralCode: String,
        updatedReferredBy: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val cleanMobile = normalizeMobile(userMobile)
        if (cleanMobile.isEmpty()) {
            onResult(false, "ইউজারের মোবাইল নম্বর পাওয়া যায়নি।")
            return
        }

        val completedLevels = (updatedCurrentLevel - 1).coerceAtLeast(0)
        val updates = mapOf(
            "name" to updatedName.trim(),
            "currentLevel" to updatedCurrentLevel.coerceAtLeast(1),
            "completedLevels" to completedLevels,
            "earningsTaka" to updatedEarningsTaka.coerceAtLeast(0.0),
            "rewardCoins" to updatedRewardCoins.coerceAtLeast(0L),
            "referralCode" to updatedReferralCode.trim().uppercase(),
            "referredBy" to updatedReferredBy.trim().uppercase()
        )

        firestore.collection("users").document(cleanMobile)
            .set(updates, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                onResult(true, "ইউজারের তথ্য সফলভাবে আপডেট করা হয়েছে!")
            }
            .addOnFailureListener { e ->
                onResult(false, "তথ্য আপডেট করতে সমস্যা: ${e.message}")
            }
    }

    /**
     * Admin Functionality: Ban or Unban a user
     */
    fun adminSetUserBanStatus(
        userMobile: String,
        isBanned: Boolean,
        banReason: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val cleanMobile = normalizeMobile(userMobile)
        if (cleanMobile.isEmpty()) {
            onResult(false, "ইউজারের মোবাইল নম্বর পাওয়া যায়নি।")
            return
        }

        val updates = mapOf(
            "isBanned" to isBanned,
            "banReason" to if (isBanned) banReason.trim().ifEmpty { "নিয়ম লঙ্ঘনের কারণে অ্যাকাউন্ট সাময়িকভাবে স্থগিত করা হয়েছে।" } else ""
        )

        firestore.collection("users").document(cleanMobile)
            .set(updates, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                val msg = if (isBanned) "ইউজারকে সফলভাবে ব্যান (Ban) করা হয়েছে।" else "ইউজারের ব্যান তুলে নেওয়া হয়েছে (Unbanned)।"
                onResult(true, msg)
            }
            .addOnFailureListener { e ->
                onResult(false, "ব্যান স্ট্যাটাস পরিবর্তনে সমস্যা: ${e.message}")
            }
    }

    /**
     * Admin Functionality: Delete a user and associated records completely
     */
    fun adminDeleteUser(
        userMobile: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val cleanMobile = normalizeMobile(userMobile)
        if (cleanMobile.isEmpty()) {
            onResult(false, "ইউজারের মোবাইল নম্বর পাওয়া যায়নি।")
            return
        }

        firestore.collection("users").document(cleanMobile)
            .delete()
            .addOnSuccessListener {
                onResult(true, "ইউজারকে সফলভাবে ডাটাবেজ থেকে সম্পূর্ণ মুছে ফেলা হয়েছে।")
            }
            .addOnFailureListener { e ->
                onResult(false, "ইউজার মুছে ফেলতে সমস্যা: ${e.message}")
            }
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

