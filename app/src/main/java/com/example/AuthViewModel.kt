package com.example

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {

    private val _selectedTabIndex = MutableStateFlow(0) // 0: Register, 1: Login
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    private val _registerName = MutableStateFlow("")
    val registerName: StateFlow<String> = _registerName.asStateFlow()

    private val _registerMobile = MutableStateFlow("")
    val registerMobile: StateFlow<String> = _registerMobile.asStateFlow()

    private val _registerReferral = MutableStateFlow("")
    val registerReferral: StateFlow<String> = _registerReferral.asStateFlow()

    private val _loginMobile = MutableStateFlow("")
    val loginMobile: StateFlow<String> = _loginMobile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _isSuccessMsg = MutableStateFlow(false)
    val isSuccessMsg: StateFlow<Boolean> = _isSuccessMsg.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Withdrawal State
    private val _showWithdrawDialog = MutableStateFlow(false)
    val showWithdrawDialog: StateFlow<Boolean> = _showWithdrawDialog.asStateFlow()

    private val _withdrawStatusMessage = MutableStateFlow<String?>(null)
    val withdrawStatusMessage: StateFlow<String?> = _withdrawStatusMessage.asStateFlow()

    private val _isWithdrawSuccess = MutableStateFlow(false)
    val isWithdrawSuccess: StateFlow<Boolean> = _isWithdrawSuccess.asStateFlow()

    private val _isWithdrawLoading = MutableStateFlow(false)
    val isWithdrawLoading: StateFlow<Boolean> = _isWithdrawLoading.asStateFlow()

    // Admin Mode State & Password Dialog State
    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    // Ad Flow & Captcha States
    enum class AdFlowStep {
        IDLE, AD1, AD2, SHOW_CAPTCHA, AD3, AD4, SHOW_SPIN_BAR, AD5, AD6, COMPLETED
    }

    private val _adFlowStep = MutableStateFlow(AdFlowStep.IDLE)
    val adFlowStep: StateFlow<AdFlowStep> = _adFlowStep.asStateFlow()

    private val _showCaptchaDialog = MutableStateFlow(false)
    val showCaptchaDialog: StateFlow<Boolean> = _showCaptchaDialog.asStateFlow()

    private val _showSpinBarDialog = MutableStateFlow(false)
    val showSpinBarDialog: StateFlow<Boolean> = _showSpinBarDialog.asStateFlow()

    private val _adStatusMessage = MutableStateFlow<String?>(null)
    val adStatusMessage: StateFlow<String?> = _adStatusMessage.asStateFlow()

    private val _showAdminPasswordDialog = MutableStateFlow(false)
    val showAdminPasswordDialog: StateFlow<Boolean> = _showAdminPasswordDialog.asStateFlow()

    private val _adminPasswordError = MutableStateFlow<String?>(null)
    val adminPasswordError: StateFlow<String?> = _adminPasswordError.asStateFlow()

    private val _adminWithdrawals = MutableStateFlow<List<WithdrawalRequest>>(emptyList())
    val adminWithdrawals: StateFlow<List<WithdrawalRequest>> = _adminWithdrawals.asStateFlow()

    private val _adminUsers = MutableStateFlow<List<User>>(emptyList())
    val adminUsers: StateFlow<List<User>> = _adminUsers.asStateFlow()

    private val _adminLoginAdsCount = MutableStateFlow(0L)
    val adminLoginAdsCount: StateFlow<Long> = _adminLoginAdsCount.asStateFlow()

    private val _adminLoginAdsRevenue = MutableStateFlow(0.0)
    val adminLoginAdsRevenue: StateFlow<Double> = _adminLoginAdsRevenue.asStateFlow()

    private val _isAdminLoading = MutableStateFlow(false)
    val isAdminLoading: StateFlow<Boolean> = _isAdminLoading.asStateFlow()

    private var headerClickCount = 0
    private var hasPlayedSessionAd = false

    private fun findActivity(context: Context): android.app.Activity? {
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is android.app.Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    fun playLoginAdForAdmin(activity: android.app.Activity, onFinished: (() -> Unit)? = null) {
        _userMessage.value = "লগইন সফল! এডমিন প্যানেলের জন্য বিজ্ঞাপন প্লে হচ্ছে..."
        AdManager.showFullScreenAd(activity) {
            FirebaseUserManager.recordAdminAdImpression(adTakaAmount = 1.25) {
                _userMessage.value = "লগইন এড সম্পন্ন হয়েছে (রাজস্ব সরাসরি এডমিন চ্যানেলে যুক্ত হয়েছে)।"
                _isSuccessMsg.value = true
                onFinished?.invoke()
            }
        }
    }

    fun onHeaderClicked() {
        headerClickCount++
        if (headerClickCount >= 5) {
            headerClickCount = 0
            _adminPasswordError.value = null
            _showAdminPasswordDialog.value = true
        }
    }

    fun dismissAdminPasswordDialog() {
        _showAdminPasswordDialog.value = false
        _adminPasswordError.value = null
        headerClickCount = 0
    }

    fun submitAdminPassword(password: String) {
        if (password.trim() == "5229") {
            _showAdminPasswordDialog.value = false
            _adminPasswordError.value = null
            _isAdminMode.value = true
            headerClickCount = 0
            loadAdminData()
        } else {
            _adminPasswordError.value = "ভুল পাসওয়ার্ড! সঠিক পাসওয়ার্ড দিন।"
        }
    }

    fun exitAdminMode() {
        _isAdminMode.value = false
    }

    fun loadAdminData() {
        _isAdminLoading.value = true
        FirebaseUserManager.fetchAllWithdrawals { withdrawals ->
            _adminWithdrawals.value = withdrawals
            FirebaseUserManager.fetchAllUsers { users ->
                _adminUsers.value = users
                FirebaseUserManager.fetchAdminAdStats { count, revenue ->
                    _adminLoginAdsCount.value = count
                    _adminLoginAdsRevenue.value = revenue
                    _isAdminLoading.value = false
                }
            }
        }
    }

    fun updateWithdrawalStatus(docId: String, newStatus: String) {
        FirebaseUserManager.updateWithdrawalStatus(docId, newStatus) { success ->
            if (success) {
                loadAdminData()
            }
        }
    }


    private val _isAppBlocked = MutableStateFlow(false)
    val isAppBlocked: StateFlow<Boolean> = _isAppBlocked.asStateFlow()

    private val _appBlockMessage = MutableStateFlow<String?>(null)
    val appBlockMessage: StateFlow<String?> = _appBlockMessage.asStateFlow()

    fun checkExistingSession(context: Context) {
        val saved = FirebaseUserManager.getSavedSession(context)
        _currentUser.value = saved

        // Sync fresh level & user details directly from Firestore
        if (saved != null && saved.mobile.isNotEmpty()) {
            FirebaseUserManager.loginUser(context, saved.mobile) { success, _, updatedUser ->
                if (success && updatedUser != null) {
                    _currentUser.value = updatedUser
                    if (!hasPlayedSessionAd) {
                        hasPlayedSessionAd = true
                        val activity = findActivity(context)
                        if (activity != null) {
                            playLoginAdForAdmin(activity)
                        }
                    }
                }
            }
        }

        // Check Firebase active status / deletion kill-switch
        verifyAppStatus()
    }

    fun verifyAppStatus() {
        FirebaseUserManager.checkAppStatus { isActive, blockMsg ->
            if (!isActive) {
                _isAppBlocked.value = true
                _appBlockMessage.value = blockMsg ?: "এই অ্যাপটির সার্ভার রেসপন্স বন্ধ আছে বা অ্যাপ প্রজেক্ট ডিলিট করা হয়েছে!"
            } else {
                _isAppBlocked.value = false
                _appBlockMessage.value = null
            }
        }
    }

    fun selectTab(index: Int) {
        _selectedTabIndex.value = index
        _userMessage.value = null
    }

    fun onRegisterNameChange(value: String) {
        _registerName.value = value
    }

    fun onRegisterMobileChange(value: String) {
        _registerMobile.value = value
    }

    fun onRegisterReferralChange(value: String) {
        _registerReferral.value = value
    }

    fun onLoginMobileChange(value: String) {
        _loginMobile.value = value
    }

    fun register(context: Context) {
        _isLoading.value = true
        _userMessage.value = null

        FirebaseUserManager.registerUser(
            context = context,
            name = _registerName.value,
            mobile = _registerMobile.value,
            referralInput = _registerReferral.value
        ) { success, msg, user ->
            _isLoading.value = false
            _isSuccessMsg.value = success
            _userMessage.value = msg
            if (success && user != null) {
                _currentUser.value = user
            }
        }
    }

    fun login(context: Context) {
        _isLoading.value = true
        _userMessage.value = null

        FirebaseUserManager.loginUser(
            context = context,
            mobile = _loginMobile.value
        ) { success, msg, user ->
            _isLoading.value = false
            _isSuccessMsg.value = success
            _userMessage.value = msg
            if (success && user != null) {
                _currentUser.value = user
                hasPlayedSessionAd = true
                val activity = findActivity(context)
                if (activity != null) {
                    playLoginAdForAdmin(activity)
                }
            }
        }
    }

    fun startEarningAdFlow(activity: android.app.Activity) {
        _adFlowStep.value = AdFlowStep.AD1
        _adStatusMessage.value = "অ্যাড ১/৬ চালু হচ্ছে..."

        AdManager.showFullScreenAd(activity) {
            _adFlowStep.value = AdFlowStep.AD2
            _adStatusMessage.value = "অ্যাড ২/৬ চালু হচ্ছে..."
            AdManager.showFullScreenAd(activity) {
                _adFlowStep.value = AdFlowStep.SHOW_CAPTCHA
                _showCaptchaDialog.value = true
                _adStatusMessage.value = "গুগল ক্যাপচার সিকিউরিটি ভেরিফিকেশন করুন..."
            }
        }
    }

    fun onCaptchaVerified(activity: android.app.Activity) {
        _showCaptchaDialog.value = false
        _adFlowStep.value = AdFlowStep.AD3
        _adStatusMessage.value = "ক্যাপচার সফল! অ্যাড ৩/৬ চালু হচ্ছে..."

        AdManager.showFullScreenAd(activity) {
            _adFlowStep.value = AdFlowStep.AD4
            _adStatusMessage.value = "অ্যাড ৪/৬ চালু হচ্ছে..."
            AdManager.showFullScreenAd(activity) {
                _adFlowStep.value = AdFlowStep.SHOW_SPIN_BAR
                _showSpinBarDialog.value = true
                _adStatusMessage.value = "স্পিন বার ঘুরিয়ে বোনাস জিতে নিন..."
            }
        }
    }

    fun onSpinCompletedAndPlayFinalAds(activity: android.app.Activity, spinBonusTaka: Double) {
        _showSpinBarDialog.value = false
        _adFlowStep.value = AdFlowStep.AD5
        _adStatusMessage.value = "স্পিন বোনাস সংরক্ষিত! অ্যাড ৫/৬ চালু হচ্ছে..."

        AdManager.showFullScreenAd(activity) {
            _adFlowStep.value = AdFlowStep.AD6
            _adStatusMessage.value = "অ্যাড ৬/৬ চালু হচ্ছে..."
            AdManager.showFullScreenAd(activity) {
                val totalTakaEarned = 0.20 + spinBonusTaka
                val totalCoinsEarned = 20L + (spinBonusTaka * 100).toLong()
                val previousLevel = _currentUser.value?.currentLevel ?: 1

                completeCurrentLevel(activity.applicationContext, takaEarned = totalTakaEarned, coinsEarned = totalCoinsEarned) { updatedUser ->
                    _adFlowStep.value = AdFlowStep.COMPLETED
                    _adStatusMessage.value = "🎉 অভিনন্দন! লেভেল #$previousLevel সফলভাবে সম্পন্ন হয়েছে। ৳${String.format(java.util.Locale.US, "%.2f", totalTakaEarned)} (২০প বেসিক + ৳${String.format(java.util.Locale.US, "%.2f", spinBonusTaka)} বোনাস) একাউন্টে যুক্ত হয়েছে।"
                }
            }
        }
    }

    fun dismissCaptcha() {
        _showCaptchaDialog.value = false
        _adFlowStep.value = AdFlowStep.IDLE
        _adStatusMessage.value = null
    }

    fun dismissSpinBarDialog() {
        _showSpinBarDialog.value = false
        _adFlowStep.value = AdFlowStep.IDLE
        _adStatusMessage.value = null
    }

    fun completeCurrentLevel(
        context: Context,
        takaEarned: Double = 0.20,
        coinsEarned: Long = 20L,
        onResult: ((User) -> Unit)? = null
    ) {
        val user = _currentUser.value ?: return

        FirebaseUserManager.updateUserProgress(
            context = context,
            user = user,
            coinsEarned = coinsEarned,
            takaEarned = takaEarned
        ) { updatedUser ->
            _currentUser.value = updatedUser
            onResult?.invoke(updatedUser)
        }
    }

    fun setWithdrawDialogVisible(visible: Boolean) {
        _showWithdrawDialog.value = visible
        if (!visible) {
            _withdrawStatusMessage.value = null
        }
    }

    fun requestWithdrawal(
        context: Context,
        paymentMethod: String,
        paymentNumber: String,
        amountTaka: Double
    ) {
        val user = _currentUser.value ?: return
        _isWithdrawLoading.value = true
        _withdrawStatusMessage.value = null

        FirebaseUserManager.submitWithdrawal(
            context = context,
            user = user,
            paymentMethod = paymentMethod,
            paymentNumber = paymentNumber,
            amountTaka = amountTaka
        ) { success, message, updatedUser ->
            _isWithdrawLoading.value = false
            _isWithdrawSuccess.value = success
            _withdrawStatusMessage.value = message
            if (success && updatedUser != null) {
                _currentUser.value = updatedUser
            }
        }
    }

    fun logout(context: Context) {
        hasPlayedSessionAd = false
        FirebaseUserManager.clearSession(context)
        _currentUser.value = null
        _loginMobile.value = ""
        _registerName.value = ""
        _registerMobile.value = ""
        _registerReferral.value = ""
        _selectedTabIndex.value = 1 // Switch to login tab
        _userMessage.value = "লগ আউট সম্পন্ন হয়েছে।"
        _isSuccessMsg.value = true
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    fun clearWithdrawMessage() {
        _withdrawStatusMessage.value = null
    }
}

