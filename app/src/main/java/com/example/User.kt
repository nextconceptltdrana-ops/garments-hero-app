package com.example

import androidx.annotation.Keep

@Keep
data class User(
    val name: String = "",
    val mobile: String = "",
    val referralCode: String = "",
    val referredBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val currentLevel: Int = 1,
    val completedLevels: Int = 0,
    val rewardCoins: Long = 0,
    val earningsTaka: Double = 0.0,
    val referralsCount: Int = 0,
    val avatarUrl: String = "",
    val lastCheckInDate: String = "",
    val checkInStreak: Int = 0
) {
    companion object {
        const val TOTAL_LEVELS = 100000 // ১ লক্ষ লেভেল
    }

    val remainingLevels: Int
        get() = (TOTAL_LEVELS - completedLevels).coerceAtLeast(0)
}

@Keep
data class WithdrawalRequest(
    val id: String = "",
    val userMobile: String = "",
    val userName: String = "",
    val paymentMethod: String = "",
    val paymentNumber: String = "",
    val amountTaka: Double = 0.0,
    val requestedAt: Long = System.currentTimeMillis(),
    val status: String = "PENDING"
)

