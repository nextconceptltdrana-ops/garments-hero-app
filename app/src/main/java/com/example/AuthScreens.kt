package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import java.text.NumberFormat
import java.util.*

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
                            text = if (isAdminMode) "গার্মেন্টস হিরো এডমিন প্যানেল" else "গার্মেন্টস হিরো ইনকাম অ্যাপ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (isAdminMode) "এডমিন এক্সেস মোড - সকল ব্যবহারকারী ও উইথড্র" else "ফায়ারবেস রিয়েলটাইম আর্নিং ও উইথড্র সিস্টেম",
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
                text = "নতুন ইনকাম অ্যাকাউন্ট তৈরি করুন",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "১ লক্ষ লেভেল খেলে প্রতিদিন টাকা আয় করার সুযোগ!",
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
                text = "ইনকাম অ্যাকাউন্টে প্রবেশ করুন",
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
                            Text("💰 এই লেভেলে আয়:", color = Color.LightGray, fontSize = 13.sp)
                            Text(
                                text = "+ ৳${String.format(Locale.US, "%.2f", data.takaEarned)}",
                                color = Color(0xFF4ADE80),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        }

                        if (data.bonusTaka > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🎁 স্পিন বোনাস:", color = Color(0xFFE0E7FF), fontSize = 12.sp)
                                Text(
                                    text = "+ ৳${String.format(Locale.US, "%.2f", data.bonusTaka)}",
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
                            Text("💼 আপনার মোট ব্যালেন্স:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "৳${String.format(Locale.US, "%.2f", data.totalBalance)}",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
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

            // Middle Row: Total Earnings & Reward Points
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Total Earnings Box
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
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "মোট ইনকাম",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "৳${String.format(Locale.US, "%.2f", user.earningsTaka)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF81C784)
                        )
                    }
                }

                // Reward Points Box
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
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "রেওয়ার্ড পয়েন্ট",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${formatBanglaNumber(user.rewardCoins)} কয়েন",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
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
                                text = if (isFlowBusy) "বিজ্ঞাপন ও টাস্ক চলমান..." else "START EARNING MONEY 💰",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isFlowBusy) Color.White else Color(0xFF211300)
                            )
                            Text(
                                text = if (isFlowBusy) "অনুগ্রহ করে বিজ্ঞাপন বা টাস্ক সম্পন্ন করুন" else "লেভেল #${user.currentLevel} শুরু করুন (আয়: ২৫প + স্পিন বোনাস)",
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

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "লগ আউট করুন (Logout)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "টাকা উইথড্র করুন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "বর্তমান ব্যালেন্স: ৳${String.format(Locale.US, "%.2f", user.earningsTaka)}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    fontSize = 15.sp
                )
                Text(
                    text = "উইথড্র লিমিট: ৳১০০.০০ থেকে ৳৫০০.০০ (৩টি সফল রেফার আবশ্যক)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Referral Requirement Status Box
                Surface(
                    color = if (user.referralsCount >= 3) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (user.referralsCount >= 3) Color(0xFF2E7D32) else Color(0xFFEF6C00)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (user.referralsCount >= 3) Icons.Default.CheckCircle else Icons.Default.GroupAdd,
                                contentDescription = null,
                                tint = if (user.referralsCount >= 3) Color(0xFF2E7D32) else Color(0xFFE65100),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "রেফারেল শর্ত: ${user.referralsCount}/৩ টি সম্পন্ন",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = if (user.referralsCount >= 3) Color(0xFF2E7D32) else Color(0xFFE65100)
                            )
                        }
                        if (user.referralsCount < 3) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "উইথড্র চালু করতে আপনাকে অবশ্যই ৩টি সফল রেফার সম্পন্ন করতে হবে (বাকি: ${3 - user.referralsCount}টি)। আপনার রেফার কোড: ${user.referralCode}",
                                fontSize = 11.sp,
                                color = Color(0xFFBF360C)
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = msg,
                            color = if (isWithdrawSuccess) Color(0xFF2E7D32) else Color(0xFFC62828),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Text(
                    text = "পেমেন্ট মেথড নির্বাচন করুন:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
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
                                    color = if (isSelected) brandColor else Color.LightGray.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) brandColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 12.dp, horizontal = 4.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = logoRes),
                                    contentDescription = method,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = method,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
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
                    label = { Text("$selectedMethod নম্বর") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("উইথড্র টাকার পরিমাণ (৳)") },
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


