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
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    private val firestore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

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
                    rewardCoins = 100,
                    earningsTaka = 10.0
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
                                        val refEarnings = refDoc.getDouble("earningsTaka") ?: 10.0
                                        val refCoins = refDoc.getLong("rewardCoins") ?: 100L

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
                rewardCoins = 100,
                earningsTaka = 10.0
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
                    rewardCoins = snapshot.getLong("rewardCoins") ?: 100L,
                    earningsTaka = snapshot.getDouble("earningsTaka") ?: 10.0,
                    avatarUrl = snapshot.getString("avatarUrl") ?: ""
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

        saveSession(context, updatedUser)

        if (user.mobile.isNotEmpty()) {
            firestore.collection("users").document(user.mobile)
                .update(
                    mapOf(
                        "currentLevel" to nextLevel,
                        "completedLevels" to updatedCompleted,
                        "rewardCoins" to updatedCoins,
                        "earningsTaka" to updatedTaka
                    )
                ).addOnSuccessListener {
                    Log.d(TAG, "User progress updated in Firestore for level $nextLevel")
                }.addOnFailureListener { e ->
                    Log.w(TAG, "Failed to update progress in Firestore: ${e.message}")
                }
        }

        onResult(updatedUser)
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
            onResult(false, "আপনার অ্যাকাউন্টে পর্যাপ্ত ব্যালেন্স নেই (বর্তমান ব্যালেন্স: ৳${String.format("%.2f", user.earningsTaka)})", null)
            return
        }

        if (paymentNumber.length < 11) {
            onResult(false, "সঠিক পেমেন্ট মোবাইল নম্বর লিখুন।", null)
            return
        }

        val updatedTaka = user.earningsTaka - amountTaka
        val updatedUser = user.copy(earningsTaka = updatedTaka)

        val withdrawalReq = mapOf(
            "userMobile" to user.mobile,
            "userName" to user.name,
            "paymentMethod" to paymentMethod,
            "paymentNumber" to paymentNumber,
            "amountTaka" to amountTaka,
            "requestedAt" to System.currentTimeMillis(),
            "status" to "PENDING"
        )

        firestore.collection("withdrawals")
            .add(withdrawalReq)
            .addOnSuccessListener {
                firestore.collection("users").document(user.mobile)
                    .update("earningsTaka", updatedTaka)
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

    fun fetchAllWithdrawals(onResult: (List<WithdrawalRequest>) -> Unit) {
        firestore.collection("withdrawals")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val list = querySnapshot.documents.map { doc ->
                    WithdrawalRequest(
                        id = doc.id,
                        userMobile = doc.getString("userMobile") ?: "",
                        userName = doc.getString("userName") ?: "",
                        paymentMethod = doc.getString("paymentMethod") ?: "",
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
                    doc.toObject(User::class.java)
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
        val rewardCoins = prefs.getLong(KEY_REWARD_COINS, 100L)
        val earningsTaka = prefs.getFloat(KEY_EARNINGS_TAKA, 10.0f).toDouble()
        val referralsCount = prefs.getInt(KEY_REFERRALS_COUNT, 0)
        val avatarUrl = prefs.getString(KEY_AVATAR_URL, "") ?: ""

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
            avatarUrl = avatarUrl
        )
    }

    fun clearSession(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}

