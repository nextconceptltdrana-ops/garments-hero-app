package com.example

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class OtpFlowType {
    LOGIN,
    REGISTRATION
}

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

    // Google Play & AdMob Policy Compliance Dialog States
    private val _showPrivacyPolicyDialog = MutableStateFlow(false)
    val showPrivacyPolicyDialog: StateFlow<Boolean> = _showPrivacyPolicyDialog.asStateFlow()

    private val _showDeleteAccountDialog = MutableStateFlow(false)
    val showDeleteAccountDialog: StateFlow<Boolean> = _showDeleteAccountDialog.asStateFlow()

    private val _showFairPlayDialog = MutableStateFlow(false)
    val showFairPlayDialog: StateFlow<Boolean> = _showFairPlayDialog.asStateFlow()

    private val _isDeletingAccount = MutableStateFlow(false)
    val isDeletingAccount: StateFlow<Boolean> = _isDeletingAccount.asStateFlow()

    // Robust State Machine for Quiz Flow
    // Sequence: Start -> 6 Ads (Ad1..Ad6) -> Captcha -> 4 Ads (Ad7..Ad10) -> Spin Bonus -> 2 Ads (Ad11..Ad12) -> Level Update & Rewards
    sealed class QuizFlowState {
        object Idle : QuizFlowState()
        object Ad1 : QuizFlowState()
        object Ad2 : QuizFlowState()
        object Ad3 : QuizFlowState()
        object Ad4 : QuizFlowState()
        object Ad5 : QuizFlowState()
        object Ad6 : QuizFlowState()
        object Captcha : QuizFlowState()
        object Ad7 : QuizFlowState()
        object Ad8 : QuizFlowState()
        object Ad9 : QuizFlowState()
        object Ad10 : QuizFlowState()
        object Spin : QuizFlowState()
        object Ad11 : QuizFlowState()
        object Ad12 : QuizFlowState()
        object LevelUpdate : QuizFlowState()
        data class Completed(val message: String) : QuizFlowState()
    }

    data class LevelCompletionData(
        val completedLevel: Int,
        val nextLevel: Int,
        val coinsEarned: Long,
        val totalCoins: Long,
        val bonusCoins: Long,
        val takaEarned: Double,
        val totalBalance: Double,
        val bonusTaka: Double
    )

    private val _levelCompletionData = MutableStateFlow<LevelCompletionData?>(null)
    val levelCompletionData: StateFlow<LevelCompletionData?> = _levelCompletionData.asStateFlow()

    private val _quizFlowState = MutableStateFlow<QuizFlowState>(QuizFlowState.Idle)
    val quizFlowState: StateFlow<QuizFlowState> = _quizFlowState.asStateFlow()

    private val _isFlowBusy = MutableStateFlow(false)
    val isFlowBusy: StateFlow<Boolean> = _isFlowBusy.asStateFlow()

    private val _showCaptchaDialog = MutableStateFlow(false)
    val showCaptchaDialog: StateFlow<Boolean> = _showCaptchaDialog.asStateFlow()

    private val _showSpinBarDialog = MutableStateFlow(false)
    val showSpinBarDialog: StateFlow<Boolean> = _showSpinBarDialog.asStateFlow()

    private val _showAdOverlay = MutableStateFlow(false)
    val showAdOverlay: StateFlow<Boolean> = _showAdOverlay.asStateFlow()

    private val _currentAdTitle = MutableStateFlow("অ্যাড ১/১২")
    val currentAdTitle: StateFlow<String> = _currentAdTitle.asStateFlow()

    private val _currentAdIndex = MutableStateFlow(1)
    val currentAdIndex: StateFlow<Int> = _currentAdIndex.asStateFlow()

    private var onAdDismissedCallback: (() -> Unit)? = null
    private var pendingSpinBonusTaka: Double = 0.0

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

    private val _whatsappNumber = MutableStateFlow("01919085229")
    val whatsappNumber: StateFlow<String> = _whatsappNumber.asStateFlow()

    private val _whatsappGroupLink = MutableStateFlow("https://chat.whatsapp.com/CFweFxYB7Fk7X3sWJljF5b")
    val whatsappGroupLink: StateFlow<String> = _whatsappGroupLink.asStateFlow()

    private val _noticeText = MutableStateFlow("গার্মেন্টস হিরো অ্যাপে স্বাগতম! নিয়মিত কুইজ খেলে ও রেফার করে ইনকাম করুন।")
    val noticeText: StateFlow<String> = _noticeText.asStateFlow()

    private val _isNoticeActive = MutableStateFlow(true)
    val isNoticeActive: StateFlow<Boolean> = _isNoticeActive.asStateFlow()

    private val _isUpdatingConfig = MutableStateFlow(false)
    val isUpdatingConfig: StateFlow<Boolean> = _isUpdatingConfig.asStateFlow()

    private val _isAdminLoading = MutableStateFlow(false)
    val isAdminLoading: StateFlow<Boolean> = _isAdminLoading.asStateFlow()

    private val _showWelcomeMotivation = MutableStateFlow(false)
    val showWelcomeMotivation: StateFlow<Boolean> = _showWelcomeMotivation.asStateFlow()

    private val _registerPin = MutableStateFlow("")
    val registerPin: StateFlow<String> = _registerPin.asStateFlow()

    private val _loginPin = MutableStateFlow("")
    val loginPin: StateFlow<String> = _loginPin.asStateFlow()

    private var headerClickCount = 0
    private var hasPlayedSessionAd = false

    fun openWelcomeMotivation() {
        _showWelcomeMotivation.value = true
    }

    fun dismissWelcomeMotivation() {
        _showWelcomeMotivation.value = false
    }

    fun playStartupOpeningAds(activity: android.app.Activity) {
        if (hasPlayedSessionAd) return
        hasPlayedSessionAd = true
        _adStatusMessage.value = "স্পন্সরড বিজ্ঞাপন ১/২..."
        playAd(activity, "স্পন্সরড বিজ্ঞাপন ১/২", 1) {
            FirebaseUserManager.recordAdminAdImpression(adTakaAmount = 1.0) {}
            _adStatusMessage.value = "স্পন্সরড বিজ্ঞাপন ২/২..."
            playAd(activity, "স্পন্সরড বিজ্ঞাপন ২/২", 2) {
                FirebaseUserManager.recordAdminAdImpression(adTakaAmount = 1.0) {}
                _adStatusMessage.value = null
                _showWelcomeMotivation.value = true
            }
        }
    }

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
        AdManager.showFullScreenAd(
            activity = activity,
            onFallback = {
                _showAdOverlay.value = true
                _currentAdTitle.value = "লগইন স্পন্সরড এড"
                _currentAdIndex.value = 1
                onAdDismissedCallback = {
                    FirebaseUserManager.recordAdminAdImpression(adTakaAmount = 1.25) {
                        _userMessage.value = "লগইন এড সম্পন্ন হয়েছে (রাজস্ব সরাসরি এডমিন চ্যানেলে যুক্ত হয়েছে)।"
                        _isSuccessMsg.value = true
                        onFinished?.invoke()
                    }
                }
            },
            onAdDismissed = {
                FirebaseUserManager.recordAdminAdImpression(adTakaAmount = 1.25) {
                    _userMessage.value = "লগইন এড সম্পন্ন হয়েছে (রাজস্ব সরাসরি এডমিন চ্যানেলে যুক্ত হয়েছে)।"
                    _isSuccessMsg.value = true
                    onFinished?.invoke()
                }
            }
        )
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
        FirebaseUserManager.getAdminConfig { whatsapp, groupLink, notice, isNoticeActive ->
            _whatsappNumber.value = whatsapp
            _whatsappGroupLink.value = groupLink
            _noticeText.value = notice
            _isNoticeActive.value = isNoticeActive
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
    }

    fun updateAdminSettings(whatsapp: String, groupLink: String, notice: String, isNoticeActive: Boolean) {
        _isUpdatingConfig.value = true
        FirebaseUserManager.updateAdminConfig(whatsapp, groupLink, notice, isNoticeActive) { success ->
            _isUpdatingConfig.value = false
            if (success) {
                _whatsappNumber.value = whatsapp
                _whatsappGroupLink.value = groupLink
                _noticeText.value = notice
                _isNoticeActive.value = isNoticeActive
                _userMessage.value = "এডমিন সেটিংস (WhatsApp ও নোটিশ) সফলভাবে আপডেট হয়েছে!"
                _isSuccessMsg.value = true
            } else {
                _userMessage.value = "সেটিংস আপডেট ব্যর্থ হয়েছে! পুনরায় চেষ্টা করুন।"
                _isSuccessMsg.value = false
            }
        }
    }

    fun fetchPublicAdminConfig() {
        FirebaseUserManager.getAdminConfig { whatsapp, groupLink, notice, isNoticeActive ->
            _whatsappNumber.value = whatsapp
            _whatsappGroupLink.value = groupLink
            _noticeText.value = notice
            _isNoticeActive.value = isNoticeActive
        }
    }

    fun updateWithdrawalStatus(docId: String, newStatus: String) {
        FirebaseUserManager.updateWithdrawalStatus(docId, newStatus) { success ->
            if (success) {
                loadAdminData()
            }
        }
    }

    fun adminUpdateUser(
        userMobile: String,
        updatedName: String,
        updatedCurrentLevel: Int,
        updatedEarningsTaka: Double,
        updatedRewardCoins: Long,
        updatedReferralCode: String,
        updatedReferredBy: String
    ) {
        _isAdminLoading.value = true
        FirebaseUserManager.adminUpdateUser(
            userMobile = userMobile,
            updatedName = updatedName,
            updatedCurrentLevel = updatedCurrentLevel,
            updatedEarningsTaka = updatedEarningsTaka,
            updatedRewardCoins = updatedRewardCoins,
            updatedReferralCode = updatedReferralCode,
            updatedReferredBy = updatedReferredBy
        ) { success, message ->
            _isAdminLoading.value = false
            _userMessage.value = message
            _isSuccessMsg.value = success
            if (success) {
                loadAdminData()
            }
        }
    }

    fun adminSetUserBanStatus(userMobile: String, isBanned: Boolean, banReason: String) {
        _isAdminLoading.value = true
        FirebaseUserManager.adminSetUserBanStatus(userMobile, isBanned, banReason) { success, message ->
            _isAdminLoading.value = false
            _userMessage.value = message
            _isSuccessMsg.value = success
            if (success) {
                loadAdminData()
            }
        }
    }

    fun adminDeleteUser(userMobile: String) {
        _isAdminLoading.value = true
        FirebaseUserManager.adminDeleteUser(userMobile) { success, message ->
            _isAdminLoading.value = false
            _userMessage.value = message
            _isSuccessMsg.value = success
            if (success) {
                loadAdminData()
            }
        }
    }


    private var userSnapshotListener: com.google.firebase.firestore.ListenerRegistration? = null

    private val _referredUsersList = MutableStateFlow<List<User>>(emptyList())
    val referredUsersList: StateFlow<List<User>> = _referredUsersList.asStateFlow()

    private val _isSyncingReferrals = MutableStateFlow(false)
    val isSyncingReferrals: StateFlow<Boolean> = _isSyncingReferrals.asStateFlow()

    private val _isAppBlocked = MutableStateFlow(false)
    val isAppBlocked: StateFlow<Boolean> = _isAppBlocked.asStateFlow()

    private val _appBlockMessage = MutableStateFlow<String?>(null)
    val appBlockMessage: StateFlow<String?> = _appBlockMessage.asStateFlow()

    fun checkExistingSession(context: Context) {
        val saved = FirebaseUserManager.getSavedSession(context)
        _currentUser.value = saved

        if (saved != null && saved.mobile.isNotEmpty()) {
            startListeningToUser(context, saved.mobile)
            refreshReferralData(context)
        }

        // Sync any cached unsynced points from previous session or unexpected close
        FirebaseUserManager.syncPendingProgressToFirestore(context) {
            // Sync fresh level & user details directly from Firestore
            if (saved != null && saved.mobile.isNotEmpty()) {
                FirebaseUserManager.loginUser(context, saved.mobile, saved.pin) { success, _, updatedUser ->
                    if (success && updatedUser != null) {
                        _currentUser.value = updatedUser
                        refreshReferralData(context)
                    }
                }
            }
        }

        // Check Firebase active status / deletion kill-switch
        verifyAppStatus()
        fetchPublicAdminConfig()
    }

    private fun startListeningToUser(context: Context, mobile: String) {
        userSnapshotListener?.remove()
        userSnapshotListener = FirebaseUserManager.listenToUser(context, mobile) { updatedUser ->
            _currentUser.value = updatedUser
        }
    }

    fun refreshReferralData(context: Context) {
        val user = _currentUser.value ?: return
        _isSyncingReferrals.value = true
        FirebaseUserManager.syncAndFetchReferredUsers(context, user) { updatedUser, list ->
            _isSyncingReferrals.value = false
            _currentUser.value = updatedUser
            _referredUsersList.value = list
        }
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

    fun onRegisterPinChange(value: String) {
        _registerPin.value = value
    }

    fun onLoginPinChange(value: String) {
        _loginPin.value = value
    }

    fun register(context: Context) {
        val rawName = _registerName.value.trim()
        val rawMobile = _registerMobile.value.trim()
        val rawPin = _registerPin.value.trim()
        val cleanMobile = FirebaseUserManager.normalizeMobile(rawMobile)

        if (rawName.isEmpty()) {
            _userMessage.value = "অনুগ্রহ করে আপনার নাম লিখুন।"
            _isSuccessMsg.value = false
            return
        }

        if (!FirebaseUserManager.isValidMobile(cleanMobile)) {
            _userMessage.value = "একটি সঠিক ১১ ডিজিটের মোবাইল নম্বর দিন (যেমন: 017XXXXXXXX)।"
            _isSuccessMsg.value = false
            return
        }
        
        if (rawPin.length < 4) {
            _userMessage.value = "অন্তত ৪ ডিজিটের গোপন পিন দিন।"
            _isSuccessMsg.value = false
            return
        }

        _isLoading.value = true
        _userMessage.value = null

        FirebaseUserManager.registerUser(
            context = context,
            name = rawName,
            mobile = cleanMobile,
            pin = rawPin,
            referralInput = _registerReferral.value
        ) { success, msg, user ->
            _isLoading.value = false
            _isSuccessMsg.value = success
            _userMessage.value = msg
            if (success && user != null) {
                _currentUser.value = user
                startListeningToUser(context, user.mobile)
                refreshReferralData(context)
                val activity = findActivity(context)
                if (activity != null) {
                    playStartupOpeningAds(activity)
                } else {
                    _showWelcomeMotivation.value = true
                }
            }
        }
    }

    fun login(context: Context) {
        val rawMobile = _loginMobile.value
        val rawPin = _loginPin.value.trim()
        val cleanMobile = FirebaseUserManager.normalizeMobile(rawMobile)
        
        if (!FirebaseUserManager.isValidMobile(cleanMobile)) {
            _userMessage.value = "একটি সঠিক ১১ ডিজিটের মোবাইল নম্বর দিন (যেমন: 017XXXXXXXX)।"
            _isSuccessMsg.value = false
            return
        }
        
        if (rawPin.length < 4) {
            _userMessage.value = "অনুগ্রহ করে আপনার গোপন পিন দিন।"
            _isSuccessMsg.value = false
            return
        }

        _isLoading.value = true
        _userMessage.value = null

        FirebaseUserManager.loginUser(
            context = context,
            mobile = cleanMobile,
            pin = rawPin
        ) { success, msg, user ->
            _isLoading.value = false
            _isSuccessMsg.value = success
            _userMessage.value = msg
            if (success && user != null) {
                _currentUser.value = user
                startListeningToUser(context, user.mobile)
                refreshReferralData(context)
                val activity = findActivity(context)
                if (activity != null) {
                    playStartupOpeningAds(activity)
                } else {
                    _showWelcomeMotivation.value = true
                }
            }
        }
    }

    private val _adQueueProgress = MutableStateFlow(0)
    val adQueueProgress: StateFlow<Int> = _adQueueProgress.asStateFlow()

    private var verifiedAdsCountInCurrentLevel = 0

    fun playAd(
        activity: android.app.Activity,
        adTitle: String,
        adIndex: Int,
        onAdClosed: () -> Unit
    ) {
        _currentAdTitle.value = adTitle
        _currentAdIndex.value = adIndex
        _adStatusMessage.value = "বিজ্ঞাপন $adIndex/১২ প্লে হচ্ছে... (বিজ্ঞাপন শেষ হলে স্বয়ংক্রিয়ভাবে পরবর্তী ধাপে যাবে)"

        AdManager.showSequentialInterstitialAd(
            activity = activity,
            adIndex = adIndex,
            totalAds = AdManager.TOTAL_LEVEL_ADS,
            onFallback = {
                _showAdOverlay.value = true
                onAdDismissedCallback = onAdClosed
            },
            onAdDismissed = {
                onAdClosed()
            }
        )
    }

    fun dismissAdOverlay() {
        _showAdOverlay.value = false
        val callback = onAdDismissedCallback
        onAdDismissedCallback = null
        callback?.invoke()
    }

    /**
     * Start the sequential 12-Ad queue system for level completion.
     * Sequence: Ads 1-6 -> Captcha Verification -> Ads 7-10 -> Spin Bonus -> Ads 11-12 -> Atomic Reward Credit
     * Ensures all 12 ads are sequentially requested, viewed, and dismissed before point rewards are granted.
     */
    fun startQuizFlow(activity: android.app.Activity) {
        if (_isFlowBusy.value) return
        _isFlowBusy.value = true
        verifiedAdsCountInCurrentLevel = 0
        _adQueueProgress.value = 0
        pendingSpinBonusTaka = 0.0

        // Step 1: Ad 1 of 12
        _quizFlowState.value = QuizFlowState.Ad1
        playAd(activity, "অ্যাড ১/১২", 1) {
            verifiedAdsCountInCurrentLevel = 1
            _adQueueProgress.value = 1

            // Step 2: Ad 2 of 12
            _quizFlowState.value = QuizFlowState.Ad2
            playAd(activity, "অ্যাড ২/১২", 2) {
                verifiedAdsCountInCurrentLevel = 2
                _adQueueProgress.value = 2

                // Step 3: Ad 3 of 12
                _quizFlowState.value = QuizFlowState.Ad3
                playAd(activity, "অ্যাড ৩/১২", 3) {
                    verifiedAdsCountInCurrentLevel = 3
                    _adQueueProgress.value = 3

                    // Step 4: Ad 4 of 12
                    _quizFlowState.value = QuizFlowState.Ad4
                    playAd(activity, "অ্যাড ৪/১২", 4) {
                        verifiedAdsCountInCurrentLevel = 4
                        _adQueueProgress.value = 4

                        // Step 5: Ad 5 of 12
                        _quizFlowState.value = QuizFlowState.Ad5
                        playAd(activity, "অ্যাড ৫/১২", 5) {
                            verifiedAdsCountInCurrentLevel = 5
                            _adQueueProgress.value = 5

                            // Step 6: Ad 6 of 12
                            _quizFlowState.value = QuizFlowState.Ad6
                            playAd(activity, "অ্যাড ৬/১২", 6) {
                                verifiedAdsCountInCurrentLevel = 6
                                _adQueueProgress.value = 6

                                // Checkpoint 1: Google Captcha Security Verification (after 6 ads)
                                _quizFlowState.value = QuizFlowState.Captcha
                                _showCaptchaDialog.value = true
                                _adStatusMessage.value = "সিকিউরিটি ভেরিফিকেশন সম্পন্ন করুন (৬/১২ অ্যাড সম্পন্ন)..."
                            }
                        }
                    }
                }
            }
        }
    }

    // Backwards-compatible alias for startQuizFlow
    fun startEarningAdFlow(activity: android.app.Activity) {
        startQuizFlow(activity)
    }

    fun onCaptchaVerified(activity: android.app.Activity) {
        _showCaptchaDialog.value = false

        // Step 7: Ad 7 of 12
        _quizFlowState.value = QuizFlowState.Ad7
        playAd(activity, "অ্যাড ৭/১২", 7) {
            verifiedAdsCountInCurrentLevel = 7
            _adQueueProgress.value = 7

            // Step 8: Ad 8 of 12
            _quizFlowState.value = QuizFlowState.Ad8
            playAd(activity, "অ্যাড ৮/১২", 8) {
                verifiedAdsCountInCurrentLevel = 8
                _adQueueProgress.value = 8

                // Step 9: Ad 9 of 12
                _quizFlowState.value = QuizFlowState.Ad9
                playAd(activity, "অ্যাড ৯/১২", 9) {
                    verifiedAdsCountInCurrentLevel = 9
                    _adQueueProgress.value = 9

                    // Step 10: Ad 10 of 12 (4 ads completed after captcha)
                    _quizFlowState.value = QuizFlowState.Ad10
                    playAd(activity, "অ্যাড ১০/১২", 10) {
                        verifiedAdsCountInCurrentLevel = 10
                        _adQueueProgress.value = 10

                        // Checkpoint 2: Lucky Spin Bonus Wheel (after 10 ads total)
                        _quizFlowState.value = QuizFlowState.Spin
                        _showSpinBarDialog.value = true
                        _adStatusMessage.value = "লাকি স্পিন বার ঘুরিয়ে বোনাস জিতে নিন (১০/১২ অ্যাড সম্পন্ন)..."
                    }
                }
            }
        }
    }

    fun onSpinCompletedAndPlayFinalAds(activity: android.app.Activity, spinBonusTaka: Double) {
        _showSpinBarDialog.value = false
        pendingSpinBonusTaka = spinBonusTaka

        // Step 11: Ad 11 of 12
        _quizFlowState.value = QuizFlowState.Ad11
        playAd(activity, "অ্যাড ১১/১২", 11) {
            verifiedAdsCountInCurrentLevel = 11
            _adQueueProgress.value = 11

            // Step 12: Final Ad 12 of 12 (2 ads completed after spin)
            _quizFlowState.value = QuizFlowState.Ad12
            playAd(activity, "অ্যাড ১২/১২", 12) {
                verifiedAdsCountInCurrentLevel = 12
                _adQueueProgress.value = 12

                // Strict verification: Ensure all 12 ads are completed
                if (verifiedAdsCountInCurrentLevel >= 12) {
                    _quizFlowState.value = QuizFlowState.LevelUpdate
                    _adStatusMessage.value = "১২/১২ বিজ্ঞাপন সম্পন্ন হয়েছে! লেভেল ও পয়েন্ট যোগ হচ্ছে..."

                    val totalTakaEarned = 0.06 + pendingSpinBonusTaka
                    val bonusCoins = (pendingSpinBonusTaka * 100).toLong()
                    val totalCoinsEarned = 6L + bonusCoins
                    val previousLevel = maxOf(_currentUser.value?.currentLevel ?: 1, (_currentUser.value?.completedLevels ?: 0) + 1)

                    completeCurrentLevel(
                        activity.applicationContext,
                        takaEarned = totalTakaEarned,
                        coinsEarned = totalCoinsEarned
                    ) { updatedUser ->
                        val resultMsg = "🎉 অভিনন্দন! ১২টি বিজ্ঞাপন সফলভাবে দেখে লেভেল #$previousLevel সম্পন্ন করেছেন। +$totalCoinsEarned রিওয়ার্ড পয়েন্ট ($totalCoinsEarned কয়েন) একাউন্টে যোগ হয়েছে।"
                        _quizFlowState.value = QuizFlowState.Completed(resultMsg)
                        _adStatusMessage.value = resultMsg
                        _isFlowBusy.value = false

                        // Show Next Level prompt for continuous targeted earning flow
                        _levelCompletionData.value = LevelCompletionData(
                            completedLevel = previousLevel,
                            nextLevel = updatedUser.currentLevel,
                            coinsEarned = totalCoinsEarned,
                            totalCoins = updatedUser.rewardCoins,
                            bonusCoins = bonusCoins,
                            takaEarned = totalTakaEarned,
                            totalBalance = updatedUser.earningsTaka,
                            bonusTaka = pendingSpinBonusTaka
                        )
                    }
                } else {
                    _isFlowBusy.value = false
                    _quizFlowState.value = QuizFlowState.Idle
                    _adStatusMessage.value = "বিজ্ঞাপন সম্পূর্ণ দেখা হয়নি ($verifiedAdsCountInCurrentLevel/১২)। লেভেল পয়েন্ট পাওয়ার জন্য সবকটি বিজ্ঞাপন দেখতে হবে।"
                }
            }
        }
    }

    fun startNextLevel(activity: android.app.Activity) {
        _levelCompletionData.value = null
        _quizFlowState.value = QuizFlowState.Idle
        _adStatusMessage.value = null
        startQuizFlow(activity)
    }

    fun cancelNextLevel() {
        _levelCompletionData.value = null
        _quizFlowState.value = QuizFlowState.Idle
        _adStatusMessage.value = "লেভেল সম্পন্ন হয়েছে। আপনি পরবর্তীতে যেকোনো সময় আবার শুরু করতে পারবেন।"
    }

    fun dismissCaptcha() {
        _showCaptchaDialog.value = false
        _quizFlowState.value = QuizFlowState.Idle
        _isFlowBusy.value = false
        _adStatusMessage.value = null
    }

    fun dismissSpinBarDialog() {
        _showSpinBarDialog.value = false
        _quizFlowState.value = QuizFlowState.Idle
        _isFlowBusy.value = false
        _adStatusMessage.value = null
    }

    fun completeCurrentLevel(
        context: Context,
        takaEarned: Double = 0.06,
        coinsEarned: Long = 6L,
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
        amountTaka: Double,
        pin: String
    ) {
        val user = _currentUser.value ?: return
        
        if (pin.trim() != user.pin) {
            _isWithdrawSuccess.value = false
            _withdrawStatusMessage.value = "ভুল পিন! অনুগ্রহ করে সঠিক পিন দিন।"
            return
        }
        
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
        userSnapshotListener?.remove()
        userSnapshotListener = null
        hasPlayedSessionAd = false
        FirebaseUserManager.syncPendingProgressToFirestore(context)
        FirebaseUserManager.clearSession(context)
        _currentUser.value = null
        _referredUsersList.value = emptyList()
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

    // Google Play Policy Handlers
    fun openPrivacyPolicy() {
        _showPrivacyPolicyDialog.value = true
    }

    data class CheckInResultData(
        val bonusTaka: Double,
        val bonusCoins: Long,
        val streak: Int,
        val message: String
    )

    private val _checkInResultData = MutableStateFlow<CheckInResultData?>(null)
    val checkInResultData: StateFlow<CheckInResultData?> = _checkInResultData.asStateFlow()

    private val _isClaimingCheckIn = MutableStateFlow(false)
    val isClaimingCheckIn: StateFlow<Boolean> = _isClaimingCheckIn.asStateFlow()

    private val _checkInAdProgress = MutableStateFlow<Pair<Int, Int>?>(null) // (currentAd, totalAds)
    val checkInAdProgress: StateFlow<Pair<Int, Int>?> = _checkInAdProgress.asStateFlow()

    fun claimDailyCheckInWithAds(activity: android.app.Activity) {
        val user = _currentUser.value ?: return
        if (_isClaimingCheckIn.value || _isFlowBusy.value) return

        if (!FirebaseUserManager.isEligibleForDailyCheckIn(user)) {
            _userMessage.value = "আপনি আজকের দৈনিক বোনাস ইতোমধ্যে সংগ্রহ করেছেন! আগামীকাল আবার আসুন।"
            _isSuccessMsg.value = false
            return
        }

        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val yesterdayCalendar = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.DAY_OF_YEAR, -1)
        }
        val yesterdayStr = dateFormat.format(yesterdayCalendar.time)

        val nextStreak = if (user.lastCheckInDate == yesterdayStr) {
            if (user.checkInStreak >= 7) 1 else user.checkInStreak + 1
        } else {
            1
        }

        val (bonusTaka, bonusCoins) = FirebaseUserManager.getBonusForStreak(nextStreak)
        val totalRequiredAds = FirebaseUserManager.getRequiredAdsForStreak(nextStreak)

        _isClaimingCheckIn.value = true
        _isFlowBusy.value = true
        _checkInAdProgress.value = Pair(1, totalRequiredAds)
        _adStatusMessage.value = "দৈনিক বোনাস সংগ্রহ: ফুলস্ক্রিন অ্যাড ১/$totalRequiredAds..."

        playCheckInAdSequence(
            activity = activity,
            currentAd = 1,
            totalAds = totalRequiredAds,
            nextStreak = nextStreak,
            bonusTaka = bonusTaka,
            bonusCoins = bonusCoins
        )
    }

    private fun playCheckInAdSequence(
        activity: android.app.Activity,
        currentAd: Int,
        totalAds: Int,
        nextStreak: Int,
        bonusTaka: Double,
        bonusCoins: Long
    ) {
        _checkInAdProgress.value = Pair(currentAd, totalAds)
        val adTitle = "বোনাস অ্যাড $currentAd/$totalAds"

        playAd(activity, adTitle, currentAd) {
            if (currentAd < totalAds) {
                playCheckInAdSequence(
                    activity = activity,
                    currentAd = currentAd + 1,
                    totalAds = totalAds,
                    nextStreak = nextStreak,
                    bonusTaka = bonusTaka,
                    bonusCoins = bonusCoins
                )
            } else {
                // All proportional full-screen ads completed! Grant reward in Firestore
                _adStatusMessage.value = "বোনাস ও কয়েন একাউন্টে যোগ হচ্ছে..."
                _checkInAdProgress.value = null

                val user = _currentUser.value ?: run {
                    _isClaimingCheckIn.value = false
                    _isFlowBusy.value = false
                    return@playAd
                }

                FirebaseUserManager.claimDailyCheckIn(activity.applicationContext, user) { success, message, updatedUser, bTaka, bCoins, streak ->
                    _isClaimingCheckIn.value = false
                    _isFlowBusy.value = false
                    if (success && updatedUser != null) {
                        _currentUser.value = updatedUser
                        _checkInResultData.value = CheckInResultData(
                            bonusTaka = bTaka,
                            bonusCoins = bCoins,
                            streak = streak,
                            message = message
                        )
                    } else {
                        _userMessage.value = message
                        _isSuccessMsg.value = false
                    }
                }
            }
        }
    }

    // Backwards compatible method
    fun claimDailyCheckIn(context: Context) {
        val activity = findActivity(context)
        if (activity != null) {
            claimDailyCheckInWithAds(activity)
        } else {
            val user = _currentUser.value ?: return
            if (_isClaimingCheckIn.value) return
            _isClaimingCheckIn.value = true

            FirebaseUserManager.claimDailyCheckIn(context, user) { success, message, updatedUser, bonusTaka, bonusCoins, streak ->
                _isClaimingCheckIn.value = false
                if (success && updatedUser != null) {
                    _currentUser.value = updatedUser
                    _checkInResultData.value = CheckInResultData(
                        bonusTaka = bonusTaka,
                        bonusCoins = bonusCoins,
                        streak = streak,
                        message = message
                    )
                } else {
                    _userMessage.value = message
                    _isSuccessMsg.value = false
                }
            }
        }
    }

    fun dismissCheckInDialog() {
        _checkInResultData.value = null
    }

    fun dismissPrivacyPolicy() {
        _showPrivacyPolicyDialog.value = false
    }

    fun openDeleteAccountDialog() {
        _showDeleteAccountDialog.value = true
    }

    fun dismissDeleteAccountDialog() {
        _showDeleteAccountDialog.value = false
    }

    fun openFairPlayDialog() {
        _showFairPlayDialog.value = true
    }

    fun dismissFairPlayDialog() {
        _showFairPlayDialog.value = false
    }

    fun deleteAccount(context: Context) {
        val user = _currentUser.value ?: return
        _isDeletingAccount.value = true
        FirebaseUserManager.deleteAccountAndData(context, user.mobile) { success, message ->
            _isDeletingAccount.value = false
            _showDeleteAccountDialog.value = false
            userSnapshotListener?.remove()
            userSnapshotListener = null
            _currentUser.value = null
            _referredUsersList.value = emptyList()
            _loginMobile.value = ""
            _registerName.value = ""
            _registerMobile.value = ""
            _registerReferral.value = ""
            _selectedTabIndex.value = 0
            _userMessage.value = message
            _isSuccessMsg.value = success
        }
    }

    override fun onCleared() {
        super.onCleared()
        userSnapshotListener?.remove()
        userSnapshotListener = null
    }
}

