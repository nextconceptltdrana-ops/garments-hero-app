package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.NumberFormat
import java.util.*

fun formatBanglaNumber(number: Any?): String {
    val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val str = number?.toString() ?: "০"
    val sb = StringBuilder()
    for (ch in str) {
        if (ch in '0'..'9') {
            sb.append(banglaDigits[ch - '0'])
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAuthApp(viewModel: AuthViewModel) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val selectedTab by viewModel.selectedTabIndex.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val userMsg by viewModel.userMessage.collectAsState()
    val isSuccessMsg by viewModel.isSuccessMsg.collectAsState()

    val isAppBlocked by viewModel.isAppBlocked.collectAsState()
    val appBlockMessage by viewModel.appBlockMessage.collectAsState()

    val isAdminMode by viewModel.isAdminMode.collectAsState()
    val showAdminPasswordDialog by viewModel.showAdminPasswordDialog.collectAsState()

    val showPrivacyPolicyDialog by viewModel.showPrivacyPolicyDialog.collectAsState()
    val showDeleteAccountDialog by viewModel.showDeleteAccountDialog.collectAsState()
    val showFairPlayDialog by viewModel.showFairPlayDialog.collectAsState()
    val isDeletingAccount by viewModel.isDeletingAccount.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkExistingSession(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) {
                                viewModel.onHeaderClicked()
                            }
                    ) {
                        Text(
                            text = if (isAdminMode) "গার্মেন্টস হিরো এডমিন প্যানেল" else "গার্মেন্টস হিরো - গেম ও রিওয়ার্ডস",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (isAdminMode) "এডমিন এক্সেস মোড - সকল ব্যবহারকারী ও উইথড্র" else "ফায়ারবেস রিয়েলটাইম লেভেল ও রিওয়ার্ড সিস্টেম",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isAdminMode) Color(0xFF311B92) else MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = if (isAdminMode) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    if (isAdminMode) {
                        IconButton(onClick = { viewModel.exitAdminMode() }) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Exit Admin",
                                tint = Color.White
                            )
                        }
                    } else {
                        Surface(
                            color = Color(0xFF4CAF50),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = "Firebase Connected",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Firebase Active",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            AdMobBannerView()
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            if (showAdminPasswordDialog) {
                AdminPasswordDialog(
                    viewModel = viewModel,
                    onDismiss = { viewModel.dismissAdminPasswordDialog() }
                )
            }

            if (showPrivacyPolicyDialog) {
                PrivacyPolicyDialog(
                    onDismiss = { viewModel.dismissPrivacyPolicy() }
                )
            }

            if (showFairPlayDialog) {
                FairPlayDialog(
                    onDismiss = { viewModel.dismissFairPlayDialog() }
                )
            }

            if (showDeleteAccountDialog) {
                DeleteAccountDialog(
                    isDeleting = isDeletingAccount,
                    onConfirm = { viewModel.deleteAccount(context) },
                    onDismiss = { viewModel.dismissDeleteAccountDialog() }
                )
            }

            if (isAdminMode) {
                AdminDashboardScreen(viewModel = viewModel)
            } else if (isAppBlocked) {
                AppBlockedScreen(
                    message = appBlockMessage,
                    onRetry = { viewModel.verifyAppStatus() },
                    onAdminClick = { viewModel.onHeaderClicked() }
                )
            } else if (currentUser != null) {
                UserIncomeDashboard(
                    user = currentUser!!,
                    viewModel = viewModel,
                    context = context
                )
            } else {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Tab Selector for Registration and Login
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { viewModel.selectTab(0) },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "নিবন্ধন (Register)",
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { viewModel.selectTab(1) },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Login,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "লগ ইন (Login)",
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Message banner
                    AnimatedVisibility(
                        visible = userMsg != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        userMsg?.let { msg ->
                            Surface(
                                color = if (isSuccessMsg) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSuccessMsg) Color(0xFF4CAF50) else Color(0xFFEF5350)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isSuccessMsg) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (isSuccessMsg) Color(0xFF2E7D32) else Color(0xFFC62828),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = msg,
                                        color = if (isSuccessMsg) Color(0xFF1B5E20) else Color(0xFFB71C1C),
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.clearMessage() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close",
                                            tint = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (selectedTab == 0) {
                        RegistrationCard(
                            viewModel = viewModel,
                            context = context,
                            isLoading = isLoading
                        )
                    } else {
                        LoginCard(
                            viewModel = viewModel,
                            context = context,
                            isLoading = isLoading
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Google Play Compliance Links
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.openPrivacyPolicy() }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Privacy Policy",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "প্রাইভেসি পলিসি",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = "•",
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.openFairPlayDialog() }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = "Fair Play Rules",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ফেয়ার প্লে নিয়মাবলী",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RegistrationCard(
    viewModel: AuthViewModel,
    context: Context,
    isLoading: Boolean
) {
    val name by viewModel.registerName.collectAsState()
    val mobile by viewModel.registerMobile.collectAsState()
    val referral by viewModel.registerReferral.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.HowToReg,
                contentDescription = "Registration",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "নতুন প্লেয়ার অ্যাকাউন্ট তৈরি করুন",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "১ লক্ষ লেভেল খেলে রিওয়ার্ড পয়েন্ট ও মিশন বোনাস অর্জন করুন!",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { viewModel.onRegisterNameChange(it) },
                label = { Text("আপনার নাম *") },
                placeholder = { Text("যেমন: মোহাম্মদ রহিম") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = mobile,
                onValueChange = { viewModel.onRegisterMobileChange(it) },
                label = { Text("মোবাইল নম্বর *") },
                placeholder = { Text("017XXXXXXXX") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = referral,
                onValueChange = { viewModel.onRegisterReferralChange(it) },
                label = { Text("রেফারেল কোড (ঐচ্ছিক)") },
                placeholder = { Text("যদি থাকে (যেমন: GH1234AB)") },
                leadingIcon = { Icon(Icons.Default.CardGiftcard, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.register(context) },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ফায়ারবেসে সেভ হচ্ছে...")
                } else {
                    Icon(Icons.Default.AppRegistration, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("নিবন্ধন করুন", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = { viewModel.selectTab(1) }
            ) {
                Text("ইতোমধ্যে অ্যাকাউন্ট আছে? ")
                Text(
                    text = "লগ ইন করুন",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun LoginCard(
    viewModel: AuthViewModel,
    context: Context,
    isLoading: Boolean
) {
    val mobile by viewModel.loginMobile.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LockOpen,
                contentDescription = "Login",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "প্লেয়ার অ্যাকাউন্টে প্রবেশ করুন",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "আপনার নিবন্ধিত মোবাইল নম্বর দিয়ে লগইন করুন",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = mobile,
                onValueChange = { viewModel.onLoginMobileChange(it) },
                label = { Text("মোবাইল নম্বর *") },
                placeholder = { Text("017XXXXXXXX") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.login(context) },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ফায়ারবেসে ভেরিফাই হচ্ছে...")
                } else {
                    Icon(Icons.Default.Login, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("লগ ইন করুন", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = { viewModel.selectTab(0) }
            ) {
                Text("নতুন ব্যবহারকারী? ")
                Text(
                    text = "নিবন্ধন করুন",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun UserIncomeDashboard(
    user: User,
    viewModel: AuthViewModel,
    context: Context
) {
    val activity = context as? android.app.Activity
    val showWithdrawDialog by viewModel.showWithdrawDialog.collectAsState()
    val showCaptchaDialog by viewModel.showCaptchaDialog.collectAsState()
    val showSpinBarDialog by viewModel.showSpinBarDialog.collectAsState()
    val showAdOverlay by viewModel.showAdOverlay.collectAsState()
    val currentAdTitle by viewModel.currentAdTitle.collectAsState()
    val currentAdIndex by viewModel.currentAdIndex.collectAsState()
    val adStatusMessage by viewModel.adStatusMessage.collectAsState()
    val isFlowBusy by viewModel.isFlowBusy.collectAsState()
    val quizFlowState by viewModel.quizFlowState.collectAsState()
    val levelCompletionData by viewModel.levelCompletionData.collectAsState()
    val checkInResultData by viewModel.checkInResultData.collectAsState()
    val isClaimingCheckIn by viewModel.isClaimingCheckIn.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (adStatusMessage != null) {
            Surface(
                color = if (quizFlowState is AuthViewModel.QuizFlowState.Completed) Color(0xFF2E7D32) else Color(0xFF1E88E5),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isFlowBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Text(
                        text = adStatusMessage!!,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // TOP ROUNDED GOLDEN BORDER HEADER FRAME
        GoldenHeaderFrame(
            user = user,
            onWithdrawClick = {
                if (!isFlowBusy) {
                    viewModel.setWithdrawDialogVisible(true)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // DAILY CHECK-IN REWARD BONUS CARD
        val checkInAdProgress by viewModel.checkInAdProgress.collectAsState()
        DailyCheckInCard(
            user = user,
            isClaiming = isClaimingCheckIn,
            adProgress = checkInAdProgress,
            onClaimCheckIn = {
                if (!isFlowBusy) {
                    val activity = context as? android.app.Activity
                    if (activity != null) {
                        viewModel.claimDailyCheckInWithAds(activity)
                    } else {
                        viewModel.claimDailyCheckIn(context)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Gameplay Level Card
        LevelPlayCard(
            user = user,
            isFlowBusy = isFlowBusy,
            onPlayLevel = {
                if (!isFlowBusy) {
                    if (activity != null) {
                        viewModel.startQuizFlow(activity)
                    } else {
                        viewModel.completeCurrentLevel(context)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Profile & Referral Details
        UserAccountDetailsCard(
            user = user,
            context = context,
            onPrivacyPolicyClick = {
                viewModel.openPrivacyPolicy()
            },
            onFairPlayClick = {
                viewModel.openFairPlayDialog()
            },
            onDeleteAccountClick = {
                viewModel.openDeleteAccountDialog()
            },
            onLogout = {
                if (!isFlowBusy) {
                    viewModel.logout(context)
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showCaptchaDialog && activity != null) {
        GoogleCaptchaDialog(
            userLevel = user.currentLevel,
            onCaptchaVerified = {
                viewModel.onCaptchaVerified(activity)
            },
            onDismiss = {
                viewModel.dismissCaptcha()
            }
        )
    }

    if (showSpinBarDialog && activity != null) {
        SpinBarDialog(
            userLevel = user.currentLevel,
            onSpinCompleted = { spinBonusTaka ->
                viewModel.onSpinCompletedAndPlayFinalAds(activity, spinBonusTaka)
            },
            onDismiss = {
                viewModel.dismissSpinBarDialog()
            }
        )
    }

    if (showWithdrawDialog) {
        WithdrawDialog(
            user = user,
            viewModel = viewModel,
            context = context,
            onDismiss = { viewModel.setWithdrawDialogVisible(false) }
        )
    }

    if (levelCompletionData != null && activity != null) {
        LevelCompletionNextDialog(
            data = levelCompletionData!!,
            onNextLevel = {
                viewModel.startNextLevel(activity)
            },
            onCancel = {
                viewModel.cancelNextLevel()
            }
        )
    }

    if (showAdOverlay) {
        FullScreenInteractiveAdDialog(
            adTitle = currentAdTitle ?: "স্পন্সরড ভিডিও বিজ্ঞাপন",
            adIndex = currentAdIndex,
            onFinished = {
                viewModel.dismissAdOverlay()
            }
        )
    }

    if (checkInResultData != null) {
        DailyCheckInCelebrationDialog(
            data = checkInResultData!!,
            onDismiss = {
                viewModel.dismissCheckInDialog()
            }
        )
    }
}

@Composable
fun FullScreenInteractiveAdDialog(
    adTitle: String,
    adIndex: Int,
    onFinished: () -> Unit
) {
    var secondsRemaining by remember { mutableIntStateOf(5) }
    var isReadyToProceed by remember { mutableStateOf(false) }

    val sponsorList = remember {
        listOf(
            SponsoredBannerData(
                brand = "Daraz Online Shopping",
                tagline = "মেগা ডিসকাউন্ট অফার! ৫০% পর্যন্ত ছাড় + ফ্রি ডেলিভারি",
                actionText = "শপ করুন",
                badge = "OFFICIAL SPONSOR",
                iconEmoji = "🛍️",
                bgGradient = listOf(Color(0xFF881337), Color(0xFF4C0519)),
                btnColor = Color(0xFFF43F5E)
            ),
            SponsoredBannerData(
                brand = "bKash Digital Payment",
                tagline = "অ্যাপ দিয়ে পেমেন্টে পাবেন ইনস্ট্যান্ট ২০% ক্যাশব্যাক ও সেন্ড মানি বোনাস!",
                actionText = "ক্যাশব্যাক নিন",
                badge = "FEATURED PARTNER",
                iconEmoji = "💳",
                bgGradient = listOf(Color(0xFF831843), Color(0xFF1E1B4B)),
                btnColor = Color(0xFFE11D48)
            ),
            SponsoredBannerData(
                brand = "Binance Crypto Exchange",
                tagline = "ট্রেড করুন বিশ্বের সেরা প্ল্যাটফর্মে, জিরো ফিতে আজই একাউন্ট খুলুন",
                actionText = "ট্রেড করুন",
                badge = "PROMO AD",
                iconEmoji = "📈",
                bgGradient = listOf(Color(0xFF451A03), Color(0xFF1C1917)),
                btnColor = Color(0xFFF59E0B)
            ),
            SponsoredBannerData(
                brand = "Samsung Galaxy S24 Ultra",
                tagline = "Galaxy AI এর সাথে স্মার্টফোনের ভবিষ্যৎ আজই উপভোগ করুন",
                actionText = "অর্ডার করুন",
                badge = "PREMIUM BRAND",
                iconEmoji = "📱",
                bgGradient = listOf(Color(0xFF0C4A6E), Color(0xFF082F49)),
                btnColor = Color(0xFF0284C7)
            ),
            SponsoredBannerData(
                brand = "Foodpanda Delivery",
                tagline = "প্রথম অর্ডারে পান ৫০% ছাড় ও ৩০ মিনিটে এক্সপ্রেস ডেলিভারি!",
                actionText = "অর্ডার দিন",
                badge = "HOT DEAL",
                iconEmoji = "🍕",
                bgGradient = listOf(Color(0xFF881337), Color(0xFF4C0519)),
                btnColor = Color(0xFFEC4899)
            )
        )
    }

    val currentSponsor = remember(adIndex) {
        val safeIdx = if (adIndex > 0) (adIndex - 1) % sponsorList.size else 0
        sponsorList[safeIdx]
    }

    LaunchedEffect(Unit) {
        while (secondsRemaining > 0) {
            kotlinx.coroutines.delay(1000)
            secondsRemaining -= 1
        }
        isReadyToProceed = true
    }

    Dialog(
        onDismissRequest = {
            if (isReadyToProceed) onFinished()
        },
        properties = DialogProperties(
            dismissOnBackPress = isReadyToProceed,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0F1D))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131E35)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Ad Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFFFD700).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFD700))
                        ) {
                            Text(
                                text = "🏆 $adTitle",
                                color = Color(0xFFFFD700),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            color = if (isReadyToProceed) Color(0xFF10B981) else Color(0xFF3B82F6),
                            shape = CircleShape
                        ) {
                            Text(
                                text = if (isReadyToProceed) "✓ সম্পন্ন" else "${secondsRemaining}s",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Sponsor Card Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.verticalGradient(currentSponsor.bgGradient))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.4f),
                                shape = CircleShape,
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = currentSponsor.iconEmoji, fontSize = 28.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = currentSponsor.brand,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = currentSponsor.tagline,
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.5.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (!isReadyToProceed) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color(0xFFFFD700),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "বিজ্ঞাপন লোড ও ভেরিফাই হচ্ছে ($secondsRemaining সেকেন্ড)...",
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Button(
                            onClick = onFinished,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "পুরস্কার গ্রহণ করুন এবং এগিয়ে যান ➔",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LevelCompletionNextDialog(
    data: AuthViewModel.LevelCompletionData,
    onNextLevel: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color(0xFF1E1B2E),
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Celebration Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFFFD700), Color(0xFFFFA000))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎉",
                        fontSize = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "লেভেল #${data.completedLevel} সফলভাবে সম্পন্ন!",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "টার্গেট: ১,০০,০০০ লেভেল মিশন 🎯",
                    fontSize = 12.sp,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Stats breakdown card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B263F))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⭐ এই লেভেলে অর্জিত রিওয়ার্ড:", color = Color.LightGray, fontSize = 13.sp)
                            Text(
                                text = "+ ${data.coinsEarned} কয়েন",
                                color = Color(0xFF4ADE80),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        }

                        if (data.bonusCoins > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🎁 লাকি স্পিন বোনাস:", color = Color(0xFFE0E7FF), fontSize = 12.sp)
                                Text(
                                    text = "+ ${data.bonusCoins} কয়েন",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💼 আপনার মোট রিওয়ার্ড কয়েন:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${data.totalCoins} কয়েন",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "(সমমূল্য ৳${String.format(Locale.US, "%.2f", data.totalBalance)})",
                                    color = Color(0xFF81C784),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "আপনি কি পরের লেভেল #${data.nextLevel} শুরু করবেন?",
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onNextLevel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00C853)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "নেক্সট লেভেল #${data.nextLevel} শুরু করুন ➔",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFEF4444)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ক্যানসেল / বিরতি নিন (Cancel)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    )
}

@Composable
fun GoldenHeaderFrame(
    user: User,
    onWithdrawClick: () -> Unit
) {
    // Golden gradient for borders
    val goldGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFFD700),
            Color(0xFFFFA500),
            Color(0xFFFFF8DC),
            Color(0xFFFFD700)
        )
    )

    val formatBanglaNumber: (Number) -> String = { num ->
        val formatted = NumberFormat.getNumberInstance(Locale.US).format(num)
        formatted.map { char ->
            when (char) {
                '0' -> '০'
                '1' -> '১'
                '2' -> '২'
                '3' -> '৩'
                '4' -> '৪'
                '5' -> '৫'
                '6' -> '৬'
                '7' -> '৭'
                '8' -> '৮'
                '9' -> '৯'
                else -> char
            }
        }.joinToString("")
    }

    // Rounded Frame Container with Golden Border
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(
                border = BorderStroke(3.dp, goldGradient),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF1E1E2C) // Royal dark background for vibrant gold highlights
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Top Row: Avatar + Name + Embedded Withdraw Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Round Avatar with Golden Border
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2D2B42))
                        .border(BorderStroke(2.dp, goldGradient), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (user.name.isNotEmpty()) user.name.take(1).uppercase() else "U",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Phone
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = user.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = user.mobile,
                        fontSize = 13.sp,
                        color = Color(0xFFFFD700)
                    )
                }

                // EMBEDDED WITHDRAW BUTTON inside Golden Header Frame
                Button(
                    onClick = onWithdrawClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32), // Green Cash accent
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFFFD700))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Withdraw",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "উইথড্র",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Middle Row: Reward Coins & Level Milestone Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Total Reward Coins Box
                Surface(
                    color = Color(0xFF28253A),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "রিওয়ার্ড পয়েন্ট",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFEEEEEE)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${formatBanglaNumber(user.rewardCoins)} কয়েন",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD54F)
                        )
                    }
                }

                // Completed Levels Box
                Surface(
                    color = Color(0xFF28253A),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = null,
                                tint = Color(0xFF69F0AE),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "লেভেল অর্জন",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFEEEEEE)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${formatBanglaNumber(user.completedLevels)} লেভেল",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF69F0AE)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Section: Levels Stats (Current Level, Played, Remaining out of 100,000)
            Surface(
                color = Color(0xFF141322),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "বর্তমান লেভেল: #${formatBanglaNumber(user.currentLevel)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "মোট: ১,০০,০০০ লেভেল",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFFD700)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Bar
                    val progressFraction = (user.completedLevels.toFloat() / User.TOTAL_LEVELS.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = progressFraction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = Color(0xFFFFD700),
                        trackColor = Color(0xFF33304A)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Played & Remaining Level counters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "▶️ খেলেছেন: ${formatBanglaNumber(user.completedLevels)} লেভেল",
                            fontSize = 12.sp,
                            color = Color(0xFF81C784)
                        )
                        Text(
                            text = "⏳ বাকি আছে: ${formatBanglaNumber(user.remainingLevels)} লেভেল",
                            fontSize = 12.sp,
                            color = Color(0xFFFF8A80)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LevelPlayCard(
    user: User,
    isFlowBusy: Boolean = false,
    onPlayLevel: () -> Unit
) {
    val goldGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFFD700),
            Color(0xFFFFA500),
            Color(0xFFFFF8DC),
            Color(0xFFFFD700)
        )
    )

    val busyGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF37474F),
            Color(0xFF455A64),
            Color(0xFF37474F)
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.5.dp, if (isFlowBusy) busyGradient else goldGradient, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1C2A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ONLY THE LARGE EYE-CATCHING ROUND GOLDEN "START EARNING" BUTTON BOX
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .clip(RoundedCornerShape(34.dp))
                    .clickable(enabled = !isFlowBusy) { onPlayLevel() },
                shape = RoundedCornerShape(34.dp),
                color = Color.Unspecified
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (isFlowBusy) busyGradient else goldGradient)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isFlowBusy) Color(0xFF263238) else Color(0xFF3E2723)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isFlowBusy) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color(0xFFFFD700),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Start Earning",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = if (isFlowBusy) "বিজ্ঞাপন ও টাস্ক চলমান..." else "START LEVEL MISSION 🎯",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isFlowBusy) Color.White else Color(0xFF211300)
                            )
                            Text(
                                text = if (isFlowBusy) "অনুগ্রহ করে বিজ্ঞাপন বা টাস্ক সম্পন্ন করুন" else "লেভেল #${user.currentLevel} শুরু করুন (২৫ রিওয়ার্ড কয়েন + লাকি স্পিন)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFlowBusy) Color.LightGray else Color(0xFF4E342E)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserAccountDetailsCard(
    user: User,
    context: Context,
    onPrivacyPolicyClick: () -> Unit,
    onFairPlayClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "অ্যাফিলিয়েট ও রেফারেল তথ্য",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Referral Code Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "আপনার রেফারেল কোড", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = user.referralCode,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Referral Code", user.referralCode)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "রেফারেল কোড কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

            ProfileItemRow(
                icon = Icons.Default.GroupAdd,
                label = "মোট সফল রেফারেল",
                value = "${user.referralsCount} জন (উইথড্র এর জন্য ৩টি প্রয়োজন)"
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

            ProfileItemRow(
                icon = Icons.Default.PersonSearch,
                label = "রেফার করেছেন কে?",
                value = if (user.referredBy.isNotEmpty()) user.referredBy else "কেউ নেই (সরাসরি)"
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

            ProfileItemRow(
                icon = Icons.Default.CloudSync,
                label = "ফায়ারবেস স্টেটাস",
                value = "রিয়েলটাইম সিঙ্ক সক্রিয়"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Google Play & Policy Navigation Buttons
            Text(
                text = "আইনি ও পলিসি সংক্রান্ত অপশন",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPrivacyPolicyClick,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "গোপনীয়তা নীতি", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onFairPlayClick,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "ফেয়ার প্লে নিয়ম", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "লগ আউট করুন (Logout)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Google Play Mandatory: Account Deletion Button
            OutlinedButton(
                onClick = onDeleteAccountClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
            ) {
                Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "Delete Account", tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "অ্যাকাউন্ট ও ডেটা মুছুন (Delete Account)", fontSize = 13.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun WithdrawDialog(
    user: User,
    viewModel: AuthViewModel,
    context: Context,
    onDismiss: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf("bKash") }
    var accountNo by remember { mutableStateOf(user.mobile) }
    var amountText by remember { mutableStateOf("500") }

    val withdrawStatus by viewModel.withdrawStatusMessage.collectAsState()
    val isWithdrawSuccess by viewModel.isWithdrawSuccess.collectAsState()
    val isWithdrawLoading by viewModel.isWithdrawLoading.collectAsState()

    val enteredTaka = amountText.toDoubleOrNull() ?: 0.0
    val requiredCoins = (enteredTaka * 100).toLong()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "রিওয়ার্ড পয়েন্ট উইথড্র / রিডিম",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // High-contrast Points and Taka conversion breakdown card
                Surface(
                    color = Color(0xFF1E1B2E),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "আপনার মোট পয়েন্ট",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFE0E0E0)
                                )
                                Text(
                                    text = "${formatBanglaNumber(user.rewardCoins)} কয়েন",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFFD54F)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "টাকায় সমমূল্য মান",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFE0E0E0)
                                )
                                Text(
                                    text = "৳${String.format(Locale.US, "%.2f", user.earningsTaka)}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF69F0AE)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = Color.White.copy(alpha = 0.25f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // High contrast Rate Calculation Badge
                        Surface(
                            color = Color(0xFF2E294A),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "হিসাব নিয়ম: ১০০ পয়েন্ট = ৳১.০০ (১ কয়েন = ০.০১ টাকা)",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFF176)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // High contrast limit badge
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFE53935)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📌 উইথড্র লিমিট: ১০,০০০ পয়েন্ট (৳১০০) থেকে ৫০,০০০ পয়েন্ট (৳৫০০)",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Referral Requirement Status Box
                Surface(
                    color = if (user.referralsCount >= 3) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, if (user.referralsCount >= 3) Color(0xFF2E7D32) else Color(0xFFEF6C00)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (user.referralsCount >= 3) Icons.Default.CheckCircle else Icons.Default.GroupAdd,
                                contentDescription = null,
                                tint = if (user.referralsCount >= 3) Color(0xFF2E7D32) else Color(0xFFE65100),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "রেফারেল শর্ত: ${user.referralsCount}/৩ টি সম্পন্ন",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = if (user.referralsCount >= 3) Color(0xFF1B5E20) else Color(0xFFBF360C)
                            )
                        }
                        if (user.referralsCount < 3) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "উইথড্র চালু করতে আপনাকে অবশ্যই ৩টি সফল রেফার সম্পন্ন করতে হবে (বাকি: ${3 - user.referralsCount}টি)। আপনার রেফার কোড: ${user.referralCode}",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFBF360C),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status Message
                withdrawStatus?.let { msg ->
                    Surface(
                        color = if (isWithdrawSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (isWithdrawSuccess) Color(0xFF4CAF50) else Color(0xFFE53935)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = msg,
                            color = if (isWithdrawSuccess) Color(0xFF1B5E20) else Color(0xFFB71C1C),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Text(
                    text = "পেমেন্ট মেথড নির্বাচন করুন:",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                val paymentMethods = listOf(
                    Triple("bKash", R.drawable.img_bkash, Color(0xFFE2136E)),
                    Triple("Nagad", R.drawable.img_nagad, Color(0xFFF7931E)),
                    Triple("Rocket", R.drawable.img_rocket, Color(0xFF8C3494))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    paymentMethods.forEach { (method, logoRes, brandColor) ->
                        val isSelected = selectedMethod == method
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedMethod = method }
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) brandColor else Color.LightGray.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) brandColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 10.dp, horizontal = 4.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = logoRes),
                                    contentDescription = method,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = method,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                    color = if (isSelected) brandColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = accountNo,
                    onValueChange = { accountNo = it },
                    label = { Text("$selectedMethod নম্বর", fontWeight = FontWeight.SemiBold) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Fast select presets with bold high-contrast text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("100", "200", "500").forEach { preset ->
                        val pTaka = preset.toDoubleOrNull() ?: 0.0
                        val pCoins = (pTaka * 100).toLong()
                        val isPresetSelected = amountText == preset
                        OutlinedButton(
                            onClick = { amountText = preset },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            border = BorderStroke(
                                width = if (isPresetSelected) 2.dp else 1.dp,
                                color = if (isPresetSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f)
                            ),
                            colors = if (isPresetSelected) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(
                                text = "৳$preset\n(${formatBanglaNumber(pCoins)} কয়েন)",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 15.sp,
                                fontWeight = if (isPresetSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                color = if (isPresetSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("উইথড্র টাকার পরিমাণ (৳)", fontWeight = FontWeight.SemiBold) },
                    supportingText = {
                        Text(
                            text = "প্রয়োজনীয় পয়েন্ট: ${formatBanglaNumber(requiredCoins)} কয়েন (১০০ কয়েন = ৳১)",
                            color = Color(0xFF2E7D32),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    viewModel.requestWithdrawal(
                        context = context,
                        paymentMethod = selectedMethod,
                        paymentNumber = accountNo,
                        amountTaka = amt
                    )
                },
                enabled = !isWithdrawLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                if (isWithdrawLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("সাবমিট হচ্ছে...")
                } else {
                    Text("রিকোয়েস্ট পাঠান", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন")
            }
        }
    )
}

@Composable
fun ProfileItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AdminPasswordDialog(
    viewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    var passwordText by remember { mutableStateOf("") }
    val errorMsg by viewModel.adminPasswordError.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "এডমিন প্যানেল ভেরিফিকেশন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "এডমিন মোডে প্রবেশ করতে গোপন ৪ ডিজিটের পাসওয়ার্ড দিন:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = passwordText,
                    onValueChange = { passwordText = it },
                    label = { Text("এডমিন পাসওয়ার্ড (যেমন: 5229)") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    isError = errorMsg != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                errorMsg?.let { err ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.submitAdminPassword(passwordText) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("প্রবেশ করুন", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

@Composable
fun AdminDashboardScreen(viewModel: AuthViewModel) {
    val context = LocalContext.current
    val withdrawals by viewModel.adminWithdrawals.collectAsState()
    val users by viewModel.adminUsers.collectAsState()
    val adminLoginAdsCount by viewModel.adminLoginAdsCount.collectAsState()
    val adminLoginAdsRevenue by viewModel.adminLoginAdsRevenue.collectAsState()
    val isLoading by viewModel.isAdminLoading.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Withdrawals, 1: Users

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Admin Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF311B92))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "👑 এডমিন কন্ট্রোল প্যানেল",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "মোট ব্যবহারকারী: ${users.size} জন | উইথড্র রিকোয়েস্ট: ${withdrawals.size} টি",
                        fontSize = 12.sp,
                        color = Color(0xFFFFD700)
                    )
                }

                IconButton(
                    onClick = { viewModel.loadAdminData() },
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // eCPM & AdMob Revenue Split Summary Card
        val totalLevelsCompleted = users.sumOf { it.completedLevels }
        val levelRevenueTaka = totalLevelsCompleted * 5.00
        val grossRevenueTaka = levelRevenueTaka + adminLoginAdsRevenue
        val userPayoutTaka = totalLevelsCompleted * 2.50
        val adminProfitTaka = (totalLevelsCompleted * 2.50) + adminLoginAdsRevenue
        val totalAdImpressions = (totalLevelsCompleted * 4) + adminLoginAdsCount.toInt()

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "📊 গুগল এডমোব ও ক্যাপচার ইনকাম অ্যানালিটিক্স (eCPM Rate 50% Split)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("💵 মোট বিজ্ঞাপন রাজস্ব (100%):", fontSize = 11.sp, color = Color.White)
                        Text(
                            "৳${String.format(Locale.US, "%.2f", grossRevenueTaka)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column {
                        Text("👥 ইউজারদের শেয়ার (50%):", fontSize = 11.sp, color = Color(0xFF81C784))
                        Text(
                            "৳${String.format(Locale.US, "%.2f", userPayoutTaka)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF81C784)
                        )
                    }

                    Column {
                        Text("👑 এডমিন নিট লাভ:", fontSize = 11.sp, color = Color(0xFFFFD54F))
                        Text(
                            "৳${String.format(Locale.US, "%.2f", adminProfitTaka)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "👑 এডমিন এক্সক্লুসিভ লগইন এড: ${adminLoginAdsCount}টি (১০০% এডমিন চ্যানেলে: ৳${String.format(Locale.US, "%.2f", adminLoginAdsRevenue)})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )

                Text(
                    text = "📈 মোট দেখানো বিজ্ঞাপন ইমপ্রেশন: $totalAdImpressions টি | প্রতি ১০০০ বিজ্ঞাপনে গড়ে $45.00 eCPM",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs: Withdrawal Requests / All Users
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Color(0xFF311B92),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "উইথড্র রিকোয়েস্ট (${withdrawals.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "সকল ইউজার (${users.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF311B92))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("ফায়ারবেস থেকে তথ্য লোড হচ্ছে...")
                }
            }
        } else if (selectedTab == 0) {
            // Withdrawal Requests List
            if (withdrawals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("কোনো উইথড্র রিকোয়েস্ট পাওয়া যায়নি।")
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    withdrawals.forEach { item ->
                        AdminWithdrawalItemCard(
                            item = item,
                            onApprove = { viewModel.updateWithdrawalStatus(item.id, "APPROVED") },
                            onReject = { viewModel.updateWithdrawalStatus(item.id, "REJECTED") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        } else {
            // All Users List
            if (users.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("কোনো নিবন্ধিত ইউজার পাওয়া যায়নি।")
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    users.forEach { u ->
                        AdminUserItemCard(user = u)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminWithdrawalItemCard(
    item: WithdrawalRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val statusColor = when (item.status) {
        "APPROVED" -> Color(0xFF2E7D32)
        "REJECTED" -> Color(0xFFC62828)
        else -> Color(0xFFE65100)
    }

    val statusText = when (item.status) {
        "APPROVED" -> "অনুমোদিত (APPROVED)"
        "REJECTED" -> "বাতিল (REJECTED)"
        else -> "অপেক্ষমাণ (PENDING)"
    }

    val dateFormatted = try {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        sdf.format(java.util.Date(item.requestedAt))
    } catch (e: Exception) {
        "আজকের রিকোয়েস্ট"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00695C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.userName.take(1).uppercase().ifEmpty { "U" },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.userName.ifEmpty { "ইউজার (${item.userMobile})" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "📅 $dateFormatted",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "📱 পেমেন্ট নম্বর (${item.paymentMethod}): ${item.paymentNumber}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "💰 উইথড্র পরিমাণ: ৳${String.format(Locale.US, "%.2f", item.amountTaka)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF2E7D32)
            )
            Text(
                text = "👤 রেজিস্ট্রেশন মোবাইল: ${item.userMobile}",
                fontSize = 12.sp,
                color = Color.DarkGray
            )

            if (item.status == "PENDING") {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                        border = BorderStroke(1.dp, Color(0xFFC62828)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("বাতিল করুন", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("অনুমোদন করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUserItemCard(user: User) {
    val dateFormatted = try {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        sdf.format(java.util.Date(user.createdAt))
    } catch (e: Exception) {
        "নতুন জয়েন"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF311B92)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.take(1).uppercase().ifEmpty { "U" },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name.ifEmpty { "ইউজার (${user.mobile})" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "📱 মোবাইল: ${user.mobile}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "📅 যোগদানের সময়: $dateFormatted",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Text(
                    text = "🎁 নিজের কোড: ${user.referralCode} | 🤝 যার রেফারে: ${if (user.referredBy.isNotBlank()) user.referredBy else "সরাসরি"}",
                    fontSize = 12.sp,
                    color = Color(0xFFE65100)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF311B92).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "⭐ লেভেল #${user.currentLevel}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF311B92),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = Color(0xFF2E7D32).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "💰 ব্যালেন্স: ৳${String.format(Locale.US, "%.2f", user.earningsTaka)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppBlockedScreen(
    message: String?,
    onRetry: () -> Unit,
    onAdminClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0D1B))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C192E)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, Color(0xFFE53935)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = Color(0xFFD32F2F).copy(alpha = 0.2f),
                    shape = CircleShape,
                    border = BorderStroke(2.dp, Color(0xFFD32F2F)),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = "Blocked",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier
                            .padding(16.dp)
                            .size(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "🚫 ইউজার সার্ভিস স্বয়ংক্রিয়ভাবে বন্ধ করা হয়েছে",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = message ?: "ফায়ারবেস সংযোগ বিচ্ছিন্ন বা অ্যাপটির সার্ভিস সার্ভার থেকে ব্লক/ডিলিট করা হয়েছে। ডাটাবেস পুনঃসংযুক্ত না হওয়া পর্যন্ত ইউজার সার্ভিস বন্ধ থাকবে। (এডমিন প্যানেল সচল আছে)",
                    fontSize = 13.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("সার্ভার স্ট্যাটাস রিফ্রেশ করুন", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onAdminClick,
                    border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFFFFD700))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("👑 এডমিন প্যানেল এক্সেস করুন", color = Color(0xFFFFD700), fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

/**
 * Google Play Store Compliant Privacy Policy Dialog
 */
@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val privacyPolicyUrl = "https://docs.google.com/document/d/1Z-2AmNEi6WOQ7jeLUzRNWKs-fymoyru4WbQpQIvK4pg/edit?usp=sharing"

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "গোপনীয়তা নীতি (Privacy Policy)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "সর্বশেষ আপডেট: আগস্ট ২০২৬",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Online Google Doc Direct Access Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "লিংক ওপেন করতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show()
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open Web Policy",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "অফিশিয়াল অনলাইন পলিসি ডকুমেন্ট",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "গুগল ডকসে বিস্তারিত দেখতে ট্যাপ করুন",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "১. তথ্যের সংগ্রহ ও ব্যবহার (Information Collection):",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "আমরা আপনার নাম এবং মোবাইল নম্বর শুধুমাত্র অ্যাকাউন্ট সনাক্তকরণ, গেমের অগ্রগতি/লেভেল পয়েন্ট ক্লাউডে সংরক্ষণ এবং রিওয়ার্ড পে-আউট ভেরিফিকেশনের জন্য সংগ্রহ করি। আপনার সম্মতি ব্যতীত কোনো ব্যক্তিগত তথ্য তৃতীয় পক্ষের সাথে বিক্রি বা শেয়ার করা হয় না।",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "২. থার্ড-পার্টি সার্ভিস (Third-Party Services):",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "• Google Firebase Firestore: সুরক্ষিত ক্লাউড ডাটাবেজ হিসেবে ব্যবহৃত হয়।\n• Google AdMob: বিজ্ঞাপন প্রদর্শনের জন্য Google AdMob SDK ব্যবহৃত হয়। AdMob তাদের নিজস্ব গোপনীয়তা নীতি অনুযায়ী বিজ্ঞাপন পরিবেশন করে।",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "৩. অ্যাকাউন্ট ও ডাটা মুছে ফেলা (Data Deletion):",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "গুগল প্লে স্টোরের পলিসি অনুযায়ী, যেকোনো ব্যবহারকারী প্রোফাইল অপশনে গিয়ে 'অ্যাকাউন্ট ও ডেটা মুছুন' বাটনে ক্লিক করে সাথে সাথে তার সকল ক্লাউড ও লোকাল ডেটা স্থায়ীভাবে মুছে ফেলতে পারেন।",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "৪. যোগাযোগ ও অভিযোগ:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "যেকোনো জিজ্ঞাসা বা অভিযোগের জন্য আমাদের সাপোর্ট ইমেইলে যোগাযোগ করতে পারেন: support@garmentshero.app",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("সম্মত ও বন্ধ করুন (Agree & Close)")
            }
        }
    )
}

/**
 * Fair Play & Anti-Fraud / AdMob Traffic Protection Dialog
 */
@Composable
fun FairPlayDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = null,
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ফেয়ার প্লে ও অ্যান্টি-বট নিয়ম",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Google AdMob এর ইনভ্যালিড ট্রাফিক প্রতিরোধ ও স্বচ্ছতা নিশ্চিত করতে নিম্নের নিয়মগুলো মেনে চলুন:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "🚫 ১. ভিপিএন (VPN/Proxy) নিষিদ্ধ:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC62828)
                )
                Text(
                    text = "ভিপিএন সংযোগ ব্যবহার করে বিজ্ঞাপন লোড করা সম্পূর্ণ নিষিদ্ধ। ভিপিএন শনাক্ত হলে অ্যাকাউন্ট সাময়িকভাবে স্থগিত হতে পারে।",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "🚫 ২. অটো-ক্লিকার বা বট স্ক্রিপ্ট নিষিদ্ধ:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC62828)
                )
                Text(
                    text = "অটোমেটেড স্ক্রিপ্ট বা থার্ড-পার্টি ক্লিকার টুল ব্যবহার করে বিজ্ঞাপন বা লেভেল পূরণ করার চেষ্টা করলে স্বয়ংক্রিয়ভাবে অ্যাকাউন্ট ব্যান হবে।",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "✅ ৩. একটি ডিভাইসে একটি অ্যাকাউন্ট:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = "ন্যায্য নিয়ম মেনে গেম খেলুন, লেভেল মিশন শেষ করে রিওয়ার্ড পয়েন্ট অর্জন করুন এবং উইথড্র অনুরোধ পাঠান।",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
            ) {
                Text("আমি বুঝতে পেরেছি (Understood)", color = Color.White)
            }
        }
    )
}

/**
 * Google Play 2024+ Mandatory: Account & Data Deletion Confirmation Dialog
 */
@Composable
fun DeleteAccountDialog(
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!isDeleting) onDismiss()
        },
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "অ্যাকাউন্ট ডিলিট নিশ্চিতকরণ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "আপনি কি নিশ্চিত যে আপনার অ্যাকাউন্ট এবং ফায়ারবেসে থাকা সকল ডাটা (পয়েন্ট, লেভেল অগ্রগতি, রেফারেল রেকর্ড) স্থায়ীভাবে মুছে ফেলতে চান?",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ সতর্কতা: একবার ডিলিট করার পর এই ডেটা আর কোনোভাবেই ফিরিয়ে আনা সম্ভব হবে না।",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isDeleting,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("মুছে ফেলা হচ্ছে...", color = Color.White)
                } else {
                    Text("হ্যাঁ, স্থায়ীভাবে মুছুন (Delete)", color = Color.White)
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isDeleting,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("বাতিল (Cancel)")
            }
        }
    )
}

@Composable
fun DailyCheckInCard(
    user: User,
    isClaiming: Boolean,
    adProgress: Pair<Int, Int>? = null,
    onClaimCheckIn: () -> Unit
) {
    val isEligible = FirebaseUserManager.isEligibleForDailyCheckIn(user)
    val streak = user.checkInStreak
    val activeDay = if (isEligible) {
        val yesterdayCalendar = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.DAY_OF_YEAR, -1)
        }
        val yesterdayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(yesterdayCalendar.time)
        if (user.lastCheckInDate == yesterdayStr) {
            if (streak >= 7) 1 else streak + 1
        } else {
            1
        }
    } else {
        if (streak in 1..7) streak else 1
    }

    val daysRewards = listOf(
        Triple(1, 0.03, 3L),
        Triple(2, 0.03, 3L),
        Triple(3, 0.04, 4L),
        Triple(4, 0.04, 4L),
        Triple(5, 0.05, 5L),
        Triple(6, 0.06, 6L),
        Triple(7, 0.08, 8L)
    )

    val goldGradient = Brush.horizontalGradient(
        listOf(Color(0xFFFFD700), Color(0xFFFFA000), Color(0xFFFFD700))
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(2.dp, goldGradient),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1C30)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD700).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📅", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "দৈনিক চেক-ইন রিওয়ার্ড",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "রিওয়ার্ড অনুপাতে ফুলস্ক্রিন অ্যাড দেখুন",
                            fontSize = 11.5.sp,
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Streak Badge
                Surface(
                    color = Color(0xFF2E2A48),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔥", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "স্ট্রিক: ${if (isEligible && activeDay == 1 && streak > 0) 0 else streak} দিন",
                            color = Color(0xFFFFD700),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7-day horizontal scroll of day cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                daysRewards.forEach { (day, taka, coins) ->
                    val isClaimed = if (isEligible) {
                        day < activeDay
                    } else {
                        day <= streak
                    }
                    val isCurrentDay = (day == activeDay) && isEligible
                    val requiredAds = FirebaseUserManager.getRequiredAdsForStreak(day)

                    val itemBorder = when {
                        isCurrentDay -> BorderStroke(2.dp, Color(0xFFFFD700))
                        isClaimed -> BorderStroke(1.dp, Color(0xFF4ADE80))
                        else -> BorderStroke(1.dp, Color(0xFF3B3856))
                    }

                    val itemBg = when {
                        isCurrentDay -> Color(0xFF332B52)
                        isClaimed -> Color(0xFF1E3A2B)
                        else -> Color(0xFF26233B)
                    }

                    Surface(
                        modifier = Modifier
                            .width(80.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        color = itemBg,
                        border = itemBorder
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "দিন $day",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrentDay) Color(0xFFFFD700) else Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (day == 7) "🎁" else if (isClaimed) "✅" else "🪙",
                                fontSize = 20.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "+$coins",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isClaimed) Color(0xFF4ADE80) else Color(0xFFFFD700)
                            )
                            Text(
                                text = "৳${String.format(Locale.US, "%.2f", taka)}",
                                fontSize = 10.sp,
                                color = Color.LightGray
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            // Proportional Full-screen Ad indicator
                            Surface(
                                color = if (isClaimed) Color(0xFF2E7D32).copy(alpha = 0.6f) else Color(0xFF1F2937),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.5.dp, if (isCurrentDay) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = if (isClaimed) "দেখা শেষ" else "📺 $requiredAds অ্যাড",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isClaimed) Color(0xFF81C784) else if (isCurrentDay) Color(0xFFFFD700) else Color.LightGray,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }

                            if (isCurrentDay) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Surface(
                                    color = Color(0xFFFFD700),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "আজকের",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF1E1C30),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            if (isEligible) {
                val (_, nextTaka, nextCoins) = daysRewards.find { it.first == activeDay } ?: Triple(1, 0.03, 3L)
                val totalReqAds = FirebaseUserManager.getRequiredAdsForStreak(activeDay)

                Button(
                    onClick = onClaimCheckIn,
                    enabled = !isClaiming,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800),
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.5.dp, Color(0xFFFFD700))
                ) {
                    if (isClaiming) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        val currentAd = adProgress?.first ?: 1
                        val totalAds = adProgress?.second ?: totalReqAds
                        Text(
                            text = "📺 ফুলস্ক্রিন অ্যাড $currentAd/$totalAds চলছে...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🎁 আজকের বোনাস সংগ্রহ করুন ($totalReqAds টি ফুলস্ক্রিন অ্যাড)",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.5.sp
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E3A2B),
                    border = BorderStroke(1.dp, Color(0xFF4ADE80).copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4ADE80),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "✅ আজকের বোনাস সংগ্রহ সম্পন্ন (আগামীকাল আবার আসুন)",
                            color = Color(0xFF4ADE80),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailyCheckInCelebrationDialog(
    data: AuthViewModel.CheckInResultData,
    onDismiss: () -> Unit
) {
    val goldGradient = Brush.horizontalGradient(
        listOf(Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFD700))
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color(0xFF1E1C30),
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Celebration Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFFFD700), Color(0xFFFFA000))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎁", fontSize = 36.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "দৈনিক চেক-ইন সম্পন্ন!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Text(
                    text = "দিন #${data.streak} স্ট্রিক বোনাস সরাসরি আপনার ফায়ারবেস অ্যাকাউন্টে যোগ হয়েছে",
                    fontSize = 12.5.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Reward Details Container
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            BorderStroke(1.5.dp, goldGradient),
                            RoundedCornerShape(16.dp)
                        ),
                    color = Color(0xFF2A2744),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "+${data.bonusCoins} কয়েন",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            text = "(সমমূল্য ৳${String.format(Locale.US, "%.2f", data.bonusTaka)})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4ADE80)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "🔥 পরের বোনাস পেতে আগামীকাল আবার অ্যাপে প্রবেশ করুন!",
                    fontSize = 12.sp,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00C853)
                )
            ) {
                Text(
                    text = "ধন্যবাদ / সম্পন্ন করুন ➔",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    )
}


