package com.example

import androidx.annotation.Keep
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
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
    val checkInStreak: Int = 0,
    val hasEarnedReferralBonus: Boolean = false,
    val earnedReferralWithdrawBonus: Double = 0.0,
    val isBanned: Boolean = false,
    val banReason: String = ""
) {
    companion object {
        const val TOTAL_LEVELS = 100000 // ১ লক্ষ লেভেল
    }

    @get:Exclude
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
    val status: String = "PENDING",
    val referredBy: String = "",
    val hasEarnedReferralBonus: Boolean = false,
    val referralBonusAmount: Double = 0.0
)

@Keep
data class AppNotification(
    val id: String = "",
    val userMobile: String = "",
    val title: String = "",
    val message: String = "",
    val amountTaka: Double = 0.0,
    val type: String = "REFERRAL_BONUS",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

