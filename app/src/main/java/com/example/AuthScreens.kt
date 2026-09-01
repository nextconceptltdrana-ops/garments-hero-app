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

fun launchWhatsAppGroup(context: Context, groupUrl: String) {
    val trimmedUrl = if (groupUrl.isNotBlank()) groupUrl.trim() else "https://chat.whatsapp.com/CFweFxYB7Fk7X3sWJljF5b"
    val inviteCode = if (trimmedUrl.contains("chat.whatsapp.com/")) {
        trimmedUrl.substringAfter("chat.whatsapp.com/").substringBefore("?").substringBefore("/").trim()
    } else {
        trimmedUrl.substringAfterLast("/").trim()
    }

    val waDeepLinkUri = if (inviteCode.isNotEmpty()) Uri.parse("whatsapp://chat?code=$inviteCode") else Uri.parse(trimmedUrl)
    val webUri = Uri.parse(trimmedUrl)

    val intentsToTry = listOf(
        Intent(Intent.ACTION_VIEW, waDeepLinkUri).apply {
            setPackage("com.whatsapp")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        },
        Intent(Intent.ACTION_VIEW, waDeepLinkUri).apply {
            setPackage("com.whatsapp.w4b")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        },
        Intent(Intent.ACTION_VIEW, waDeepLinkUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        },
        Intent(Intent.ACTION_VIEW, webUri).apply {
            setPackage("com.whatsapp")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        },
        Intent(Intent.ACTION_VIEW, webUri).apply {
            setPackage("com.whatsapp.w4b")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    )

    var success = false
    for (intent in intentsToTry) {
        try {
            context.startActivity(intent)
            success = true
            break
        } catch (ignored: Exception) {
        }
    }

    if (!success) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("WhatsApp Group Link", trimmedUrl)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "আপনার ফোনে WhatsApp অ্যাপ পাওয়া যায়নি! গ্রুপ লিংক কপি করা হয়েছে।", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp গ্রুপ লিংক: $trimmedUrl", Toast.LENGTH_LONG).show()
        }
    }
}

fun launchWhatsAppHelpline(context: Context, whatsappNumber: String, userMobile: String) {
    val cleanNumber = whatsappNumber.replace(Regex("[^0-9]"), "")
    val formattedNumber = if (cleanNumber.startsWith("0")) "88$cleanNumber" else if (!cleanNumber.startsWith("88")) "88$cleanNumber" else cleanNumber
    val msg = "আসসালামু আলাইকুম, গার্মেন্টস হিরো অ্যাপ সংক্রান্ত তথ্যের জন্য যোগাযোগ করছি। আমার মোবাইল নম্বর: $userMobile"
    val encodedMsg = Uri.encode(msg)

    val waDeepLinkUri = Uri.parse("whatsapp://send?phone=$formattedNumber&text=$encodedMsg")
    val webUri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedNumber&text=$encodedMsg")

    val intentsToTry = listOf(
        Intent(Intent.ACTION_VIEW, waDeepLinkUri).apply {
            setPackage("com.whatsapp")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        },
        Intent(Intent.ACTION_VIEW, waDeepLinkUri).apply {
            setPackage("com.whatsapp.w4b")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        },
        Intent(Intent.ACTION_VIEW, waDeepLinkUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        },
        Intent(Intent.ACTION_VIEW, webUri).apply {
            setPackage("com.whatsapp")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        },
        Intent(Intent.ACTION_VIEW, webUri).apply {
            setPackage("com.whatsapp.w4b")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    )

    var success = false
    for (intent in intentsToTry) {
        try {
            context.startActivity(intent)
            success = true
            break
        } catch (ignored: Exception) {
        }
    }

    if (!success) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("WhatsApp Helpline", whatsappNumber)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "আপনার ফোনে WhatsApp অ্যাপ পাওয়া যায়নি! হেল্পলাইন নম্বর কপি করা হয়েছে: $whatsappNumber", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp হেল্পলাইন নম্বর: $whatsappNumber", Toast.LENGTH_LONG).show()
        }
    }
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
    val showWelcomeMotivation by viewModel.showWelcomeMotivation.collectAsState()
    // val showOtpDialog by viewModel.showOtpDialog.collectAsState()
    val isDeletingAccount by viewModel.isDeletingAccount.collectAsState()
    val whatsappNumber by viewModel.whatsappNumber.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkExistingSession(context)
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null && !currentUser!!.isBanned && !isAdminMode) {
            val act = context as? android.app.Activity
            if (act != null) {
                viewModel.playStartupOpeningAds(act)
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                if (isAdminMode) {
                                    listOf(Color(0xFF1E1B4B), Color(0xFF311B92), Color(0xFF1E1B4B))
                                } else {
                                    listOf(Color(0xFF00382B), Color(0xFF005A44), Color(0xFF00382B))
                                }
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ) {
                                    viewModel.onHeaderClicked()
                                }
                        ) {
                            Text(
                                text = if (isAdminMode) "👑 গার্মেন্টস হিরো এডমিন প্যানেল" else "✨ গার্মেন্টস হিরো - মিশন ও রিওয়ার্ডস",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.5.sp,
                                color = if (isAdminMode) Color(0xFFFFD700) else Color(0xFFFFE082),
                                letterSpacing = 0.15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isAdminMode) "এডমিন এক্সেস মোড - ইউজার ডাটা ও পেমেন্ট কন্ট্রোল" else "১,০০,০০০ লেভেল মিশন • প্রতিদিন আনলিমিটেড ইনকাম",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.88f)
                            )
                        }

                        if (isAdminMode) {
                            IconButton(onClick = { viewModel.exitAdminMode() }) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Exit Admin",
                                    tint = Color(0xFFFFD700)
                                )
                            }
                        } else {
                            Surface(
                                color = Color(0xFF00C853).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.2.dp, Color(0xFFFFD700).copy(alpha = 0.8f)),
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = "Firebase Connected",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "LIVE",
                                        color = Color(0xFFFFD700),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            AdMobBannerView()
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0F1423),
                            Color(0xFF14192B),
                            Color(0xFF0B0F19)
                        )
                    )
                )
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

            if (showWelcomeMotivation) {
                WelcomeMotivationDialog(
                    onDismiss = { viewModel.dismissWelcomeMotivation() }
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
                if (currentUser!!.isBanned) {
                    UserBannedScreen(
                        user = currentUser!!,
                        whatsappNumber = whatsappNumber,
                        onLogout = { viewModel.logout(context) },
                        onContactAdmin = {
                            launchWhatsAppHelpline(
                                context = context,
                                whatsappNumber = whatsappNumber,
                                userMobile = currentUser!!.mobile
                            )
                        }
                    )
                } else {
                    UserIncomeDashboard(
                        user = currentUser!!,
                        viewModel = viewModel,
                        context = context
                    )
                }
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
                    Surface(
                        color = Color(0xFF161B2E),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFFFFD700),
                            divider = {},
                            indicator = {}
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { viewModel.selectTab(0) },
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (selectedTab == 0) Brush.horizontalGradient(
                                            listOf(Color(0xFF00695C), Color(0xFF00C853))
                                        ) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                    ),
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PersonAdd,
                                            contentDescription = null,
                                            tint = if (selectedTab == 0) Color.White else Color(0xFFFFE082),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "নিবন্ধন (Register)",
                                            fontWeight = if (selectedTab == 0) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = if (selectedTab == 0) Color.White else Color(0xFFFFE082),
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { viewModel.selectTab(1) },
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (selectedTab == 1) Brush.horizontalGradient(
                                            listOf(Color(0xFF00695C), Color(0xFF00C853))
                                        ) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                    ),
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Login,
                                            contentDescription = null,
                                            tint = if (selectedTab == 1) Color.White else Color(0xFFFFE082),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "লগ ইন (Login)",
                                            fontWeight = if (selectedTab == 1) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = if (selectedTab == 1) Color.White else Color(0xFFFFE082),
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            )
                        }
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B2E)),
        border = BorderStroke(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF00C853).copy(alpha = 0.25f), Color(0xFF004D40))
                        )
                    )
                    .border(1.2.dp, Color(0xFFFFD700), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HowToReg,
                    contentDescription = "Registration",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "নতুন প্লেয়ার অ্যাকাউন্ট তৈরি করুন",
                fontSize = 19.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 0.2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "১ লক্ষ লেভেল খেলে রিওয়ার্ড পয়েন্ট ও মিশন বোনাস অর্জন করুন!",
                fontSize = 13.sp,
                color = Color(0xFFFFE082),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFFFFD700),
                unfocusedLabelColor = Color(0xFF94A3B8),
                focusedBorderColor = Color(0xFFFFD700),
                unfocusedBorderColor = Color(0xFF334155),
                cursorColor = Color(0xFFFFD700),
                focusedLeadingIconColor = Color(0xFFFFD700),
                unfocusedLeadingIconColor = Color(0xFF94A3B8)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { viewModel.onRegisterNameChange(it) },
                label = { Text("আপনার নাম *") },
                placeholder = { Text("যেমন: মোহাম্মদ রহিম", color = Color(0xFF64748B)) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            val registerPin by viewModel.registerPin.collectAsState()
            
            OutlinedTextField(
                value = mobile,
                onValueChange = { viewModel.onRegisterMobileChange(it) },
                label = { Text("মোবাইল নম্বর *") },
                placeholder = { Text("017XXXXXXXX", color = Color(0xFF64748B)) },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = registerPin,
                onValueChange = { viewModel.onRegisterPinChange(it) },
                label = { Text("গোপন পিন *") },
                placeholder = { Text("অন্তত ৪ ডিজিটের পিন", color = Color(0xFF64748B)) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = referral,
                onValueChange = { viewModel.onRegisterReferralChange(it) },
                label = { Text("রেফারেল কোড (ঐচ্ছিক)") },
                placeholder = { Text("যেমন: GH1234AB", color = Color(0xFF64748B)) },
                leadingIcon = { Icon(Icons.Default.CardGiftcard, contentDescription = null) },
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(22.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(enabled = !isLoading) { viewModel.register(context) },
                shape = RoundedCornerShape(14.dp),
                color = Color.Unspecified
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00695C), Color(0xFF00C853))
                            )
                        )
                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.7f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFFFFD700),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ফায়ারবেসে সেভ হচ্ছে...", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AppRegistration, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("নিবন্ধন সম্পন্ন করুন", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            TextButton(
                onClick = { viewModel.selectTab(1) }
            ) {
                Text("ইতোমধ্যে অ্যাকাউন্ট আছে? ", color = Color(0xFFCBD5E1), fontSize = 13.5.sp)
                Text(
                    text = "লগ ইন করুন",
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD700),
                    fontSize = 14.sp
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
    val loginPin by viewModel.loginPin.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B2E)),
        border = BorderStroke(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF00C853).copy(alpha = 0.25f), Color(0xFF004D40))
                        )
                    )
                    .border(1.2.dp, Color(0xFFFFD700), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = "Login",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "প্লেয়ার অ্যাকাউন্টে প্রবেশ করুন",
                fontSize = 19.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 0.2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "আপনার নিবন্ধিত মোবাইল নম্বর দিয়ে লগইন করুন",
                fontSize = 13.sp,
                color = Color(0xFFFFE082),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFFFFD700),
                unfocusedLabelColor = Color(0xFF94A3B8),
                focusedBorderColor = Color(0xFFFFD700),
                unfocusedBorderColor = Color(0xFF334155),
                cursorColor = Color(0xFFFFD700),
                focusedLeadingIconColor = Color(0xFFFFD700),
                unfocusedLeadingIconColor = Color(0xFF94A3B8)
            )

            OutlinedTextField(
                value = mobile,
                onValueChange = { viewModel.onLoginMobileChange(it) },
                label = { Text("মোবাইল নম্বর *") },
                placeholder = { Text("017XXXXXXXX", color = Color(0xFF64748B)) },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = loginPin,
                onValueChange = { viewModel.onLoginPinChange(it) },
                label = { Text("গোপন পিন *") },
                placeholder = { Text("আপনার পিন দিন", color = Color(0xFF64748B)) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(22.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(enabled = !isLoading) { viewModel.login(context) },
                shape = RoundedCornerShape(14.dp),
                color = Color.Unspecified
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00695C), Color(0xFF00C853))
                            )
                        )
                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.7f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFFFFD700),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("লগইন যাচাই হচ্ছে...", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Login, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("লগ ইন করুন", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            TextButton(
                onClick = { viewModel.selectTab(0) }
            ) {
                Text("নতুন ব্যবহারকারী? ", color = Color(0xFFCBD5E1), fontSize = 13.5.sp)
                Text(
                    text = "নিবন্ধন করুন",
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD700),
                    fontSize = 14.sp
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
    val adQueueProgress by viewModel.adQueueProgress.collectAsState()
    val referredUsers by viewModel.referredUsersList.collectAsState()
    val isSyncingReferrals by viewModel.isSyncingReferrals.collectAsState()

    val currentWhatsapp by viewModel.whatsappNumber.collectAsState()
    val currentWhatsappGroup by viewModel.whatsappGroupLink.collectAsState()
    val currentNotice by viewModel.noticeText.collectAsState()
    val isNoticeActive by viewModel.isNoticeActive.collectAsState()

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
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
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
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (isFlowBusy && adQueueProgress > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "বিজ্ঞাপন কিউ অগ্রগতি: $adQueueProgress/১২",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${((adQueueProgress / 12f) * 100).toInt()}%",
                                color = Color(0xFFFFD700),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { adQueueProgress / 12f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFFFFD700),
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }

        // LIVE NOTICE BOARD
        if (isNoticeActive && currentNotice.isNotBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                border = BorderStroke(1.dp, Color(0xFFFFB300))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Notice",
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "📢 লাইভ নোটিশ বোর্ড",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = currentNotice,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1F2937),
                            lineHeight = 18.sp
                        )
                    }
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

        Spacer(modifier = Modifier.height(10.dp))

        // INSPIRATION & 5% LIFETIME EARNING GUIDE CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.openWelcomeMotivation() },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B38)),
            border = BorderStroke(1.2.dp, Color(0xFFFFD700).copy(alpha = 0.8f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "👑", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ইনকাম গাইড ও ৫% আজীবন কমিশন অফার",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.5.sp,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            text = "কীভাবে শুধু অ্যাড দেখে ও টিম বানিয়ে আনলিমিটেড আয় করবেন",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Surface(
                    color = Color(0xFFFFD700).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFD700))
                ) {
                    Text(
                        text = "পড়ুন ➔",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // WHATSAPP 24/7 HELPLINE SUPPORT BANNER
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    launchWhatsAppHelpline(context, currentWhatsapp, user.mobile)
                },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            border = BorderStroke(1.5.dp, Color(0xFF25D366))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = Color(0xFF25D366),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "WhatsApp",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "💬 ২৪/৭ WhatsApp হেল্পলাইন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF14532D)
                        )
                        Text(
                            text = "সরাসরি এডমিনের সাথে চ্যাট করুন ($currentWhatsapp)",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF166534)
                        )
                    }
                }

                Surface(
                    color = Color(0xFF25D366),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "চ্যাট",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // WHATSAPP OFFICIAL COMMUNITY GROUP JOIN CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    launchWhatsAppGroup(context, currentWhatsappGroup)
                },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
            border = BorderStroke(1.5.dp, Color(0xFF00897B))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = Color(0xFF00897B),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = "WhatsApp Group",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "👥 অফিসিয়াল WhatsApp গ্রুপ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF115E59)
                        )
                        Text(
                            text = "সকল মেম্বারদের সাথে যুক্ত হতে গ্রুপে জয়েন করুন",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F766E)
                        )
                    }
                }

                Surface(
                    color = Color(0xFF00897B),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "জয়েন",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

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
            referredUsers = referredUsers,
            isSyncing = isSyncingReferrals,
            onRefreshReferrals = {
                viewModel.refreshReferralData(context)
            },
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
                tagline = "মেগা ডিসকাউন্ট অফার! ৫০% পর্যন্ত ছাড় + ফ্রি হোম ডেলিভারি। আজই সেরা ডিল উপভোগ করুন!",
                actionText = "অফারটি দেখুন",
                badge = "OFFICIAL SPONSOR",
                iconEmoji = "🛍️",
                bgGradient = listOf(Color(0xFF991B1B), Color(0xFF4C0519), Color(0xFF1E1B4B)),
                btnColor = Color(0xFFF43F5E)
            ),
            SponsoredBannerData(
                brand = "bKash Digital Payment",
                tagline = "বিকাশ অ্যাপ দিয়ে পেমেন্টে পাবেন ইনস্ট্যান্ট ২০% পর্যন্ত ক্যাশব্যাক ও আকর্ষণীয় রিওয়ার্ড পয়েন্ট!",
                actionText = "ক্যাশব্যাক নিন",
                badge = "FEATURED PARTNER",
                iconEmoji = "💳",
                bgGradient = listOf(Color(0xFF831843), Color(0xFF3B0764), Color(0xFF0F172A)),
                btnColor = Color(0xFFE11D48)
            ),
            SponsoredBannerData(
                brand = "Binance Crypto Exchange",
                tagline = "বিশ্বের এক নম্বর সিকিউর এক্সচেঞ্জে ট্রেড করুন। জিরো ডিপোজিট ফিতে আজই যুক্ত হোন!",
                actionText = "ট্রেড শুরু করুন",
                badge = "GLOBAL PROMO",
                iconEmoji = "📈",
                bgGradient = listOf(Color(0xFF78350F), Color(0xFF451A03), Color(0xFF18181B)),
                btnColor = Color(0xFFF59E0B)
            ),
            SponsoredBannerData(
                brand = "Samsung Galaxy S24 Ultra",
                tagline = "Galaxy AI এর সাথে স্মার্টফোনের ভবিষ্যৎ ও অবিশ্বাস্য ক্যামেরা কোয়ালিটি উপভোগ করুন!",
                actionText = "ডিভাইস দেখুন",
                badge = "PREMIUM BRAND",
                iconEmoji = "📱",
                bgGradient = listOf(Color(0xFF075985), Color(0xFF0C4A6E), Color(0xFF0F172A)),
                btnColor = Color(0xFF0284C7)
            ),
            SponsoredBannerData(
                brand = "Foodpanda Express Delivery",
                tagline = "প্রথম অর্ডারে পান ৫০% পর্যন্ত স্পেশাল ডিসকাউন্ট ও ৩০ মিনিটে গরম খাবার হোম ডেলিভারি!",
                actionText = "অর্ডার দিন",
                badge = "SUPER DEAL",
                iconEmoji = "🍕",
                bgGradient = listOf(Color(0xFF9F1239), Color(0xFF701A75), Color(0xFF1E1B4B)),
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
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0B0F19),
                            Color(0xFF111827),
                            Color(0xFF0A0E17)
                        )
                    )
                )
                .systemBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Navigation / Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFFFD700).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🎬 $adTitle",
                                    color = Color(0xFFFFD700),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Countdown Timer / Close Button
                    if (isReadyToProceed) {
                        Surface(
                            onClick = onFinished,
                            color = Color(0xFF10B981),
                            shape = RoundedCornerShape(20.dp),
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "স্কিপ করুন ✕",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    progress = { (5 - secondsRemaining) / 5f },
                                    modifier = Modifier.size(14.dp),
                                    color = Color(0xFF38BDF8),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "রিওয়ার্ড: ${secondsRemaining}s",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Smooth Progress Bar
                LinearProgressIndicator(
                    progress = { if (isReadyToProceed) 1f else (5 - secondsRemaining) / 5f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (isReadyToProceed) Color(0xFF10B981) else Color(0xFFFFD700),
                    trackColor = Color(0xFF334155)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Hero Interactive Sponsor Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2235)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Visual Brand Hero Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.verticalGradient(currentSponsor.bgGradient))
                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    color = Color.Black.copy(alpha = 0.4f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = currentSponsor.iconEmoji, fontSize = 30.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                    color = Color(0xFFFFD700),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = currentSponsor.badge,
                                        color = Color(0xFF0F172A),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "⭐⭐⭐⭐⭐  ৪.৯ (১২০K+ রিভিউ)",
                                    color = Color(0xFFFFE082),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Brand Title & Description
                        Text(
                            text = currentSponsor.brand,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = currentSponsor.tagline,
                            color = Color(0xFFE2E8F0),
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 21.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Features Highlights Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf("⚡ ইনস্ট্যান্ট রিওয়ার্ড", "🔒 ১০০% সুরক্ষিত", "🎁 স্পেশাল অফার").forEach { badgeText ->
                                Surface(
                                    color = Color(0xFF0F172A),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFF475569))
                                ) {
                                    Text(
                                        text = badgeText,
                                        color = Color(0xFFF1F5F9),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Embedded AdMob Live Banner container
                        AdMobBannerView()
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action & Reward Bar
                if (!isReadyToProceed) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFF475569))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFFFFD700),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "বিজ্ঞাপন দেখা হচ্ছে (${formatBanglaNumber(secondsRemaining)} সেকেন্ড বাকি)...",
                                color = Color(0xFFF1F5F9),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onFinished,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🎉 পুরস্কার সংগ্রহ করুন এবং এগিয়ে যান ➔",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
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
    referredUsers: List<User> = emptyList(),
    isSyncing: Boolean = false,
    onRefreshReferrals: () -> Unit = {},
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "অ্যাফিলিয়েট ও রেফারেল তথ্য",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(
                    onClick = onRefreshReferrals,
                    enabled = !isSyncing
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("সিঙ্ক হচ্ছে...", fontSize = 12.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("সিঙ্ক রিফ্রেশ", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Referral Code Row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "আপনার রেফারেল কোড",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = user.referralCode,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
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
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "গার্মেন্টস হিরো অ্যাপে যোগ দিন এবং প্রতিদিন ইনকাম করুন!\nআমার রেফারেল কোড: ${user.referralCode}\nনিবন্ধনের সময় রেফার কোড ব্যবহার করুন।"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "রেফার কোড শেয়ার করুন"))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            ProfileItemRow(
                icon = Icons.Default.GroupAdd,
                label = "মোট সফল রেফারেল",
                value = "${user.referralsCount} জন (উইথড্র এর জন্য ৩টি প্রয়োজন)"
            )

            // High-visibility Referral Bonus Info Box
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFFF8E1),
                border = BorderStroke(1.dp, Color(0xFFFFB300))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "🎁 আজীবন রেফারেল কমিশন অফার:",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color(0xFFC25E00)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "১. রেফারেল কোড দিয়ে সাইনআপ করলেই তাৎক্ষণিক ৳৫.০০ বোনাস।\n২. আপনার রেফার করা বন্ধু যতবার উইথড্র করবে, প্রতিবার আপনি আজীবন পাবেন শতকরা ৫% কমিশন (কোনো লিমিট নেই, আজীবন চালু থাকবে)!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF3E2723),
                        lineHeight = 18.sp
                    )
                }
            }

            // Show Referral Progress Bar towards 3 referrals
            val refCount = user.referralsCount
            val progress = (refCount / 3f).coerceIn(0f, 1f)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp),
                    color = if (refCount >= 3) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = if (refCount >= 3) "✅ ৩টি রেফারেল শর্ত পূরণ হয়েছে! আপনি উইথড্র করতে পারবেন।"
                    else "উইথড্র করার যোগ্যতা অর্জন করতে আরও ${3 - refCount} টি রেফারেল প্রয়োজন।",
                    fontSize = 13.sp,
                    color = if (refCount >= 3) Color(0xFF1B5E20) else Color(0xFF475569),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (referredUsers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "আপনার সফল রেফারেল তালিকা (${referredUsers.size} জন):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        referredUsers.forEach { refUser ->
                            val maskedMobile = if (refUser.mobile.length >= 7) {
                                refUser.mobile.take(5) + "****" + refUser.mobile.takeLast(2)
                            } else refUser.mobile
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${refUser.name} ($maskedMobile)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                val withdrawBonus = refUser.earnedReferralWithdrawBonus
                                val totalBonusFromRef = 5.0 + withdrawBonus
                                Text(
                                    text = if (withdrawBonus > 0) "+৳${String.format(Locale.US, "%.2f", totalBonusFromRef)} (৫৳ + ৳${String.format(Locale.US, "%.2f", withdrawBonus)} উইথড্র বোনাস)" else "+৳৫.০০",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }
            }

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
    var pinText by remember { mutableStateOf("") }

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
                
                Spacer(modifier = Modifier.height(10.dp))
                
                OutlinedTextField(
                    value = pinText,
                    onValueChange = { pinText = it },
                    label = { Text("আপনার গোপন পিন দিন", fontWeight = FontWeight.SemiBold) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
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
                        amountTaka = amt,
                        pin = pinText
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
    val currentWhatsapp by viewModel.whatsappNumber.collectAsState()
    val currentWhatsappGroup by viewModel.whatsappGroupLink.collectAsState()
    val currentNotice by viewModel.noticeText.collectAsState()
    val isNoticeActive by viewModel.isNoticeActive.collectAsState()
    val isUpdatingConfig by viewModel.isUpdatingConfig.collectAsState()
    val isLoading by viewModel.isAdminLoading.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Withdrawals, 1: Users, 2: App Settings

    var editWhatsapp by remember(currentWhatsapp) { mutableStateOf(currentWhatsapp) }
    var editWhatsappGroup by remember(currentWhatsappGroup) { mutableStateOf(currentWhatsappGroup) }
    var editNotice by remember(currentNotice) { mutableStateOf(currentNotice) }
    var editNoticeActive by remember(isNoticeActive) { mutableStateOf(isNoticeActive) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
                        text = "মোট ইউজার: ${users.size} জন | উইথড্র: ${withdrawals.size} টি | হেল্প: $currentWhatsapp",
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
                        Text("💵 মোট রাজস্ব (100%):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            "৳${String.format(Locale.US, "%.2f", grossRevenueTaka)}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Column {
                        Text("👥 ইউজার শেয়ার (50%):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
                        Text(
                            "৳${String.format(Locale.US, "%.2f", userPayoutTaka)}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF81C784)
                        )
                    }

                    Column {
                        Text("👑 এডমিন লাভ:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                        Text(
                            "৳${String.format(Locale.US, "%.2f", adminProfitTaka)}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD54F)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.25f))
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "👑 এডমিন এক্সক্লুসিভ লগইন এড: ${adminLoginAdsCount}টি (১০০% এডমিন চ্যানেলে: ৳${String.format(Locale.US, "%.2f", adminLoginAdsRevenue)})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )

                Text(
                    text = "📈 মোট দেখানো বিজ্ঞাপন ইমপ্রেশন: $totalAdImpressions টি | প্রতি ১০০০ বিজ্ঞাপনে গড়ে $45.00 eCPM",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE2E8F0)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs: Withdrawal Requests / All Users / Settings
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
                        text = "উইথড্র (${withdrawals.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "ইউজার (${users.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Text(
                        text = "⚙️ সেটিংস ও নোটিশ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
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
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("কোনো উইথড্র রিকোয়েস্ট পাওয়া যায়নি।")
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
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
        } else if (selectedTab == 1) {
            // All Users List
            if (users.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("কোনো নিবন্ধিত ইউজার পাওয়া যায়নি।")
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    users.forEach { u ->
                        AdminUserItemCard(
                            user = u,
                            onSaveEdit = { name, level, taka, coins, refCode, refBy ->
                                viewModel.adminUpdateUser(
                                    userMobile = u.mobile,
                                    updatedName = name,
                                    updatedCurrentLevel = level,
                                    updatedEarningsTaka = taka,
                                    updatedRewardCoins = coins,
                                    updatedReferralCode = refCode,
                                    updatedReferredBy = refBy
                                )
                            },
                            onToggleBan = { isBanned, reason ->
                                viewModel.adminSetUserBanStatus(
                                    userMobile = u.mobile,
                                    isBanned = isBanned,
                                    banReason = reason
                                )
                            },
                            onDeleteUser = {
                                viewModel.adminDeleteUser(userMobile = u.mobile)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        } else {
            // App Settings & Notice Controller Tab
            Column(modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = null,
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "💬 WhatsApp হেল্পলাইন নম্বর সেটআপ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "এখানে যে মোবাইল নম্বর সেট করবেন, ইউজাররা অ্যাপের WhatsApp বাটনে চাপলে সরাসরি সেই নম্বরে চ্যাট চালু হবে।",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = editWhatsapp,
                            onValueChange = { editWhatsapp = it },
                            label = { Text("WhatsApp হেল্প নম্বর") },
                            placeholder = { Text("01919085229") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF25D366)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = editWhatsappGroup,
                            onValueChange = { editWhatsappGroup = it },
                            label = { Text("WhatsApp গ্রুপ লিংক") },
                            placeholder = { Text("https://chat.whatsapp.com/...") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null, tint = Color(0xFF00897B)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "📢 লাইভ নোটিশ বোর্ড কন্ট্রোল",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "হোমে নোটিশ প্রদর্শন চালু রাখুন:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Switch(
                                checked = editNoticeActive,
                                onCheckedChange = { editNoticeActive = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = editNotice,
                            onValueChange = { editNotice = it },
                            label = { Text("নোটিশ বার্তা (Notice Text)") },
                            placeholder = { Text("জরুরি নোটিশ লিখুন...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 3,
                            maxLines = 5
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.updateAdminSettings(
                                    whatsapp = editWhatsapp.trim(),
                                    groupLink = editWhatsappGroup.trim(),
                                    notice = editNotice.trim(),
                                    isNoticeActive = editNoticeActive
                                )
                            },
                            enabled = !isUpdatingConfig && editWhatsapp.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF311B92)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isUpdatingConfig) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ফায়ারবেসে সংরক্ষণ হচ্ছে...")
                            } else {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("💾 সেটিংস সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(60.dp))
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
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569)
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
                        fontSize = 12.sp,
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
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "💰 উইথড্র পরিমাণ: ৳${String.format(Locale.US, "%.2f", item.amountTaka)}",
                fontSize = 16.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF2E7D32)
            )
            Text(
                text = "👤 রেজিস্ট্রেশন মোবাইল: ${item.userMobile}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF334155)
            )

            if (item.referredBy.isNotBlank()) {
                val potentialBonus = item.amountTaka * 0.05
                val bonusStatusText = if (item.status == "APPROVED" && item.referralBonusAmount > 0.0) {
                    "✅ ৫% আজীবন রেফারেল কমিশন ৳${String.format(Locale.US, "%.2f", item.referralBonusAmount)} রেফারকারী (${item.referredBy}) এর অ্যাকাউন্টে জমা হয়েছে।"
                } else if (item.status == "PENDING") {
                    "🎉 এপ্রুভ করলে রেফারকারী (${item.referredBy}) ৫% হারে ৳${String.format(Locale.US, "%.2f", potentialBonus)} আজীবন লাইফটাইম কমিশন পাবেন!"
                } else {
                    "ℹ️ রেফারকারী: ${item.referredBy}"
                }
                val bonusColor = if (item.status == "APPROVED" && item.referralBonusAmount > 0.0 || item.status == "PENDING") Color(0xFF1B5E20) else Color(0xFFE65100)
                val bonusBg = if (item.status == "APPROVED" && item.referralBonusAmount > 0.0 || item.status == "PENDING") Color(0xFFE8F5E9) else Color(0xFFFFF3E0)

                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = bonusBg,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = bonusStatusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = bonusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }

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
                        Text("বাতিল করুন", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("অনুমোদন করুন", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


@Composable
fun AdminUserItemCard(
    user: User,
    onSaveEdit: (name: String, level: Int, earningsTaka: Double, rewardCoins: Long, refCode: String, refBy: String) -> Unit,
    onToggleBan: (isBanned: Boolean, banReason: String) -> Unit,
    onDeleteUser: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showBanDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val dateFormatted = try {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        sdf.format(java.util.Date(user.createdAt))
    } catch (e: Exception) {
        "নতুন জয়েন"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (user.isBanned) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surface
        ),
        border = if (user.isBanned) BorderStroke(1.5.dp, Color(0xFFD32F2F)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (user.isBanned) Color(0xFFD32F2F) else Color(0xFF311B92)),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = user.name.ifEmpty { "ইউজার (${user.mobile})" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (user.isBanned) {
                            Surface(
                                color = Color(0xFFD32F2F),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "🚫 BANNED",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "📱 মোবাইল: ${user.mobile}",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "📅 যোগদানের সময়: $dateFormatted",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = "🎁 নিজের কোড: ${user.referralCode} | 🤝 যার রেফারে: ${if (user.referredBy.isNotBlank()) user.referredBy else "সরাসরি"}",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD97706)
                    )

                    if (user.isBanned && user.banReason.isNotBlank()) {
                        Text(
                            text = "⚠️ ব্যান কারণ: ${user.banReason}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFC62828)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Color(0xFF311B92).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "⭐ লেভেল #${user.currentLevel}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF311B92),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            color = Color(0xFF2E7D32).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "💰 ৳${String.format(Locale.US, "%.2f", user.earningsTaka)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            color = Color(0xFFE65100).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "🪙 ${user.rewardCoins} কয়েন",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Row: Edit (লেভেল/টাকা/নাম), Ban/Unban, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Edit Button
                OutlinedButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF311B92)
                    ),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("এডিট / পরিবর্তন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Ban / Unban Button
                Button(
                    onClick = {
                        if (user.isBanned) {
                            onToggleBan(false, "")
                        } else {
                            showBanDialog = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isBanned) Color(0xFF2E7D32) else Color(0xFFD97706)
                    ),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (user.isBanned) Icons.Default.CheckCircle else Icons.Default.Block,
                        contentDescription = "Ban Status",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (user.isBanned) "আনব্যান করুন" else "ব্যান করুন",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Delete Button
                IconButton(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Delete User",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // 1. Edit User Details Dialog (Change Level, Money, Coins, Name, Referrer)
    if (showEditDialog) {
        var editName by remember { mutableStateOf(user.name) }
        var editLevel by remember { mutableStateOf(user.currentLevel.toString()) }
        var editTaka by remember { mutableStateOf(String.format(Locale.US, "%.2f", user.earningsTaka)) }
        var editCoins by remember { mutableStateOf(user.rewardCoins.toString()) }
        var editReferralCode by remember { mutableStateOf(user.referralCode) }
        var editReferredBy by remember { mutableStateOf(user.referredBy) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text("🛠️ ইউজার তথ্য পরিবর্তন (এডমিন)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "মোবাইল: ${user.mobile}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF311B92)
                    )

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("ইউজারের নাম") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editLevel,
                        onValueChange = { editLevel = it.filter { char -> char.isDigit() } },
                        label = { Text("⭐ বর্তমান লেভেল (Level)") },
                        supportingText = { Text("লেভেল রিসেট বা যেকোনো লেভেলে সেট করতে পারবেন") },
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editTaka,
                        onValueChange = { editTaka = it },
                        label = { Text("💰 ব্যালেন্স টাকা (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editCoins,
                        onValueChange = { editCoins = it.filter { char -> char.isDigit() } },
                        label = { Text("🪙 রিওয়ার্ড কয়েন (Coins)") },
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editReferralCode,
                        onValueChange = { editReferralCode = it.uppercase() },
                        label = { Text("ইউজারের রেফারেল কোড") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editReferredBy,
                        onValueChange = { editReferredBy = it.uppercase() },
                        label = { Text("যার রেফার কোডে যুক্ত হয়েছে") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val levelInt = editLevel.toIntOrNull() ?: user.currentLevel
                        val takaDouble = editTaka.toDoubleOrNull() ?: user.earningsTaka
                        val coinsLong = editCoins.toLongOrNull() ?: user.rewardCoins
                        onSaveEdit(
                            editName.trim(),
                            levelInt,
                            takaDouble,
                            coinsLong,
                            editReferralCode.trim(),
                            editReferredBy.trim()
                        )
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF311B92))
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // 2. Ban Reason Dialog
    if (showBanDialog) {
        var banReasonText by remember { mutableStateOf("অস্বাভাবিক কার্যকলাপ বা নিয়ম লঙ্ঘনের কারণে ব্যান করা হয়েছে।") }

        AlertDialog(
            onDismissRequest = { showBanDialog = false },
            title = {
                Text("🚫 ইউজার ব্যান করুন", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("এই ইউজার (${user.name} - ${user.mobile}) কে ব্যান করতে চান? ব্যান করলে ইউজার অ্যাপে ঢুকতে পারবে না।")
                    OutlinedTextField(
                        value = banReasonText,
                        onValueChange = { banReasonText = it },
                        label = { Text("ব্যানের কারণ") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onToggleBan(true, banReasonText)
                        showBanDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("হ্যাঁ, ব্যান করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBanDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // 3. Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text("⚠️ স্থায়ীভাবে মুছে ফেলুন (Delete)", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
            },
            text = {
                Text("আপনি কি নিশ্চিত যে ইউজার '${user.name}' (${user.mobile}) এর সকল তথ্য ডাটাবেজ থেকে সম্পূর্ণ মুছে ফেলতে চান? এই অ্যাকশন আর ফেরানো যাবে না।")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteUser()
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("ডিলিট করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
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
                            fontSize = 13.sp,
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Streak Badge
                Surface(
                    color = Color(0xFF2E2A48),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFFFD700))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔥", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "স্ট্রিক: ${if (isEligible && activeDay == 1 && streak > 0) 0 else streak} দিন",
                            color = Color(0xFFFFD700),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
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
                            .width(86.dp)
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
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrentDay) Color(0xFFFFD700) else Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (day == 7) "🎁" else if (isClaimed) "✅" else "🪙",
                                fontSize = 22.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "+$coins",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isClaimed) Color(0xFF4ADE80) else Color(0xFFFFD700)
                            )
                            Text(
                                text = "৳${String.format(Locale.US, "%.2f", taka)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF1F5F9)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Proportional Full-screen Ad indicator
                            Surface(
                                color = if (isClaimed) Color(0xFF2E7D32) else Color(0xFF1F2937),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.8.dp, if (isCurrentDay) Color(0xFFFFD700) else Color(0xFF475569))
                            ) {
                                Text(
                                    text = if (isClaimed) "দেখা শেষ" else "📺 $requiredAds অ্যাড",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isClaimed) Color(0xFFB9F6CA) else if (isCurrentDay) Color(0xFFFFD700) else Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF1E1C30),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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


@Composable
fun UserBannedScreen(
    user: User,
    whatsappNumber: String,
    onLogout: () -> Unit,
    onContactAdmin: () -> Unit
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
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
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
                        contentDescription = "Banned",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier
                            .padding(18.dp)
                            .size(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "আপনার একাউন্ট ব্যান করা হয়েছে!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF5252),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "ইউজার: ${user.name} (${user.mobile})",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = Color(0xFF2D1616),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFD32F2F).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "ব্যানের কারণ:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF8A80)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.banReason.ifBlank { "অস্বাভাবিক কার্যকলাপ বা অ্যাপের নিয়ম লঙ্ঘনের কারণে এডমিন কর্তৃক একাউন্টটি স্থগিত করা হয়েছে।" },
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Contact Admin WhatsApp Button
                Button(
                    onClick = onContactAdmin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "WhatsApp",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "এডমিনের সাথে WhatsApp এ যোগাযোগ করুন",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Logout Button
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.8f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("লগ আউট করুন")
                }
            }
        }
    }
}


@Composable
fun WelcomeMotivationDialog(
    onDismiss: () -> Unit
) {
    val steps = listOf(
        MotivationStep(
            stepNumber = 1,
            badge = "ধাপ ১/৫ • সহজ ইনকাম",
            icon = "📱",
            headline = "স্মার্টফোনে ঘরে বসেই নিশ্চিত ইনকাম!",
            title = "প্রতিটি অ্যাড দেখার বিনিময়ে ১০০% রিয়েল টাকা",
            desc = "কোনো কঠিন কাজ বা জটিল নিয়ম নেই! প্রতিদিন ঘরে বসে শুধুমাত্র ছোট ছোট ভিডিও বিজ্ঞাপন ও ব্যানার দেখে নিশ্চিত টাকা ও রিওয়ার্ড কয়েন অর্জন করুন। আপনার অবসর সময়কে রূপান্তর করুন আসল উপার্জনে!",
            accentColor = Color(0xFF00E676),
            bgColor = Color(0xFF063826)
        ),
        MotivationStep(
            stepNumber = 2,
            badge = "ধাপ ২/৫ • আজীবন কমিশন",
            icon = "🚀",
            headline = "জীবন বদলে দেওয়ার মতো আজীবন ইনকাম!",
            title = "৫% আজীবন লাইফটাইম রেফারেল কমিশন (প্যাসিভ আয়)",
            desc = "আপনি নিজে অ্যাড দেখে ইনকাম করবেন এবং আপনার বন্ধুবান্ধবদের রেফারেল কোড শেয়ার করবেন। আপনার রেফার করা মেম্বার যতবারই টাকা উইথড্র করবে, প্রতিবারই আপনি আজীবন পাবেন শতকরা ৫% সরাসরি নগদ কমিশন! কোনো লিমিট নেই—লাইফটাইম কমিশন উপভোগ করুন!",
            accentColor = Color(0xFFFFD700),
            bgColor = Color(0xFF3D2C04)
        ),
        MotivationStep(
            stepNumber = 3,
            badge = "ধাপ ৩/৫ • দ্রুত উইথড্রল",
            icon = "⚡",
            headline = "১০০% বিশ্বস্ত ও দ্রুত পেমেন্ট সুবিধা!",
            title = "বিকাশ, নগদ ও রকেটে সরাসরি ক্যাশআউট",
            desc = "কোনো প্রকার ঝামেলা বা বিলম্ব ছাড়াই সরাসরি আপনার বিকাশ, নগদ অথবা রকেট নম্বরে উইথড্র দিয়ে টাকা বুঝে নিন। প্রতি সপ্তাহে শত শত ইউজার নিয়মিত পেমেন্ট পাচ্ছেন!",
            accentColor = Color(0xFF00B0FF),
            bgColor = Color(0xFF0A2E4C)
        ),
        MotivationStep(
            stepNumber = 4,
            badge = "ধাপ ৪/৫ • নিরাপত্তা সতর্কতা",
            icon = "🔒",
            headline = "আপনার একাউন্টের নিরাপত্তা আপনার হাতে!",
            title = "গোপন পিন কোড কাউকে শেয়ার করবেন না",
            desc = "আপনার লগইন এবং উইথড্র করার গোপন পিন (PIN) সম্পূর্ণ ব্যক্তিগত। এই পিন কোডটি কখনোই অন্য কাউকে শেয়ার করবেন না। অন্য কেউ পিন জেনে গেলে আপনার একাউন্টের টাকা চুরি হয়ে যেতে পারে!",
            accentColor = Color(0xFFFF5252),
            bgColor = Color(0xFF3E0000)
        ),
        MotivationStep(
            stepNumber = 5,
            badge = "ধাপ ৫/৫ • শুরু করুন আজই",
            icon = "🏆",
            headline = "আপনার সাফল্য আপনার নিজের হাতে!",
            title = "টিম গড়ে তুলুন এবং শুরু করুন আসল ইনকাম",
            desc = "দেরি না করে এখনই অ্যাড দেখা শুরু করুন এবং আপনার রেফারেল কোড বেশি বেশি বন্ধুদের মাঝে ছড়িয়ে দিয়ে আজই আপনার আজীবন আয়ের টিম গড়ে তুলুন!",
            accentColor = Color(0xFFFF9100),
            bgColor = Color(0xFF3E1F03)
        )
    )

    var currentStepIndex by remember { mutableStateOf(0) }
    val currentStep = steps[currentStepIndex]
    val isLastStep = currentStepIndex == steps.lastIndex

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.90f))
                .padding(horizontal = 14.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0F172A)
                ),
                border = BorderStroke(
                    2.dp,
                    Brush.linearGradient(
                        listOf(
                            currentStep.accentColor,
                            Color(0xFFFF9800),
                            currentStep.accentColor
                        )
                    )
                ),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar with Step Badge, Step Indicator & Skip Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = currentStep.accentColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, currentStep.accentColor)
                        ) {
                            Text(
                                text = currentStep.badge,
                                color = currentStep.accentColor,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Progress Indicator Dots
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                steps.indices.forEach { index ->
                                    val isActive = index == currentStepIndex
                                    Box(
                                        modifier = Modifier
                                            .size(if (isActive) 16.dp else 6.dp, 6.dp)
                                            .background(
                                                color = if (isActive) currentStep.accentColor else Color.White.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(3.dp)
                                            )
                                    )
                                }
                            }

                            // Skip Button
                            Surface(
                                modifier = Modifier.clickable { onDismiss() },
                                color = Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "স্কিপ ✕",
                                        color = Color.White,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Step Big Icon
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = currentStep.accentColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.5.dp, currentStep.accentColor.copy(alpha = 0.8f)),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = currentStep.icon, fontSize = 36.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Headline
                    Text(
                        text = currentStep.headline,
                        fontSize = 17.5.sp,
                        fontWeight = FontWeight.Black,
                        color = currentStep.accentColor,
                        textAlign = TextAlign.Center,
                        lineHeight = 23.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Main Reading Content Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = currentStep.bgColor,
                        border = BorderStroke(1.2.dp, currentStep.accentColor.copy(alpha = 0.7f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Text(
                                text = currentStep.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = Color.White,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentStep.desc,
                                fontSize = 13.sp,
                                color = Color(0xFFF1F5F9),
                                lineHeight = 19.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Navigation Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentStepIndex > 0) {
                            OutlinedButton(
                                onClick = { currentStepIndex-- },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "⬅ পূর্ববর্তী",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (isLastStep) {
                                    onDismiss()
                                } else {
                                    currentStepIndex++
                                }
                            },
                            modifier = Modifier
                                .weight(if (currentStepIndex > 0) 1.5f else 1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLastStep) Color(0xFF00C853) else Color(0xFFFFD700)
                            ),
                            elevation = ButtonDefaults.buttonElevation(6.dp)
                        ) {
                            Text(
                                text = if (isLastStep) "🚀 মেইন পেজে প্রবেশ করুন" else "পরের নিয়ম পড়ুন ➔",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isLastStep) Color.White else Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

data class MotivationStep(
    val stepNumber: Int,
    val badge: String,
    val icon: String,
    val headline: String,
    val title: String,
    val desc: String,
    val accentColor: Color,
    val bgColor: Color
)



