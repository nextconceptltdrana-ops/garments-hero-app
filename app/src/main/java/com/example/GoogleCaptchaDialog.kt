package com.example

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class CaptchaType {
    CHECKBOX,
    IMAGE_GRID,
    MATH_CODE,
    SLIDER
}

data class GridItemData(
    val index: Int,
    val name: String,
    val icon: ImageVector,
    val isTarget: Boolean,
    val color: Color
)

@Composable
fun GoogleCaptchaDialog(
    userLevel: Int,
    onCaptchaVerified: () -> Unit,
    onDismiss: () -> Unit
) {
    // Current Captcha Mode (starts with random or Checkbox/ImageGrid)
    var captchaType by remember { mutableStateOf(CaptchaType.values()[Random.nextInt(CaptchaType.values().size)]) }

    // State for Image Grid
    val targetsList = remember {
        listOf(
            "ট্রাফিক লাইট (Traffic Light)" to Icons.Default.Traffic,
            "বাস (Bus)" to Icons.Default.DirectionsBus,
            "গাড়ি (Car)" to Icons.Default.DirectionsCar,
            "বাইসাইকেল (Bicycle)" to Icons.Default.DirectionsBike,
            "পথচারী পারাপার (Crosswalk)" to Icons.Default.DirectionsWalk,
            "মোটরসাইকেল (Motorcycle)" to Icons.Default.TwoWheeler,
            "ফায়ার হাইড্রেন্ট (Fire Hydrant)" to Icons.Default.LocalFireDepartment
        )
    }

    var targetPairIndex by remember { mutableStateOf(Random.nextInt(targetsList.size)) }
    val (targetName, targetIcon) = targetsList[targetPairIndex]

    // Generate 9 tiles with target and distractor icons
    val gridItems = remember(captchaType, targetPairIndex) {
        val distractorIcons = listOf(
            Icons.Default.LocalTaxi,
            Icons.Default.DirectionsSubway,
            Icons.Default.Agriculture,
            Icons.Default.LocalShipping,
            Icons.Default.Flight,
            Icons.Default.ElectricCar,
            Icons.Default.DirectionsWalk,
            Icons.Default.Commute
        )

        val items = mutableListOf<GridItemData>()
        val targetIndices = mutableSetOf<Int>()
        while (targetIndices.size < Random.nextInt(3, 5)) {
            targetIndices.add(Random.nextInt(9))
        }

        for (i in 0 until 9) {
            val isTarget = targetIndices.contains(i)
            if (isTarget) {
                items.add(
                    GridItemData(
                        index = i,
                        name = targetName,
                        icon = targetIcon,
                        isTarget = true,
                        color = Color(0xFF1976D2)
                    )
                )
            } else {
                val distractor = distractorIcons[i % distractorIcons.size]
                items.add(
                    GridItemData(
                        index = i,
                        name = "অন্যান্য",
                        icon = distractor,
                        isTarget = false,
                        color = Color(0xFF616161)
                    )
                )
            }
        }
        items
    }

    var selectedIndices by remember { mutableStateOf(setOf<Int>()) }

    // State for Math/Code
    val mathA = remember { Random.nextInt(5, 20) }
    val mathB = remember { Random.nextInt(3, 15) }
    val mathAnswer = remember { (mathA + mathB).toString() }
    var userMathInput by remember { mutableStateOf("") }

    // State for Slider
    var sliderPosition by remember { mutableFloatStateOf(0f) }

    // Checkbox State
    var isChecked by remember { mutableStateOf(false) }
    var isCheckingAnim by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isVerifying by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 16.dp)
                .border(2.dp, Color(0xFF4285F4), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header (Neutral Security Verification look)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1976D2), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "সিকিউরিটি ভেরিফিকেশন (Security Check)",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = when (captchaType) {
                                CaptchaType.CHECKBOX -> "আমি রোবট নই (I'm not a robot)"
                                CaptchaType.IMAGE_GRID -> "চিত্র নির্বাচন করুন: $targetName"
                                CaptchaType.MATH_CODE -> "গাণিতিক সিকিউরিটি কোড যাচাই করুন"
                                CaptchaType.SLIDER -> "স্লাইড করে সিকিউরিটি লক আনলক করুন"
                            },
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security Verification",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "সিকিউরিটি টাস্ক সম্পন্ন করুন • লেভেল #$userLevel",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF374151),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // CAPTCHA CHALLENGE BODY
                when (captchaType) {
                    CaptchaType.CHECKBOX -> {
                        // Google "I'm not a robot" Checkbox
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFDADCE0), RoundedCornerShape(10.dp))
                                .clickable(enabled = !isChecked && !isCheckingAnim && !isVerifying) {
                                    if (!isChecked && !isCheckingAnim) {
                                        isCheckingAnim = true
                                        errorMessage = null
                                        coroutineScope.launch {
                                            delay(500)
                                            isCheckingAnim = false
                                            isChecked = true
                                            isVerifying = true
                                            delay(400)
                                            onCaptchaVerified()
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .border(
                                                width = 2.dp,
                                                color = if (isChecked) Color(0xFF0F9D58) else Color(0xFFC1C1C1),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .background(
                                                if (isChecked) Color(0xFF0F9D58) else Color.White,
                                                RoundedCornerShape(4.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCheckingAnim) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp,
                                                color = Color(0xFF4285F4)
                                            )
                                        } else if (isChecked) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Checked",
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = "আমি রোবট নই (I'm not a robot)",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF222222)
                                        )
                                        if (isChecked) {
                                            Text(
                                                text = "ভেরিফিকেশন সফল! এগিয়ে যাচ্ছে...",
                                                fontSize = 11.sp,
                                                color = Color(0xFF0F9D58),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = "Security Check",
                                        tint = Color(0xFF1976D2),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Text(
                                        text = "Security Check",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "Privacy - Terms",
                                        fontSize = 8.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                    }

                    CaptchaType.IMAGE_GRID -> {
                        // 3x3 Grid with actual challenge object icons
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .border(1.dp, Color(0xFFDADCE0), RoundedCornerShape(10.dp))
                                .background(Color(0xFFF8F9FA))
                                .padding(6.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                for (row in 0 until 3) {
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                    ) {
                                        for (col in 0 until 3) {
                                            val index = row * 3 + col
                                            val item = gridItems[index]
                                            val isSelected = selectedIndices.contains(index)

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .padding(3.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (isSelected) Color(0xFF4285F4).copy(alpha = 0.25f)
                                                        else Color.White
                                                    )
                                                    .border(
                                                        width = if (isSelected) 3.dp else 1.dp,
                                                        color = if (isSelected) Color(0xFF1A73E8) else Color(0xFFE0E0E0),
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable {
                                                        selectedIndices = if (isSelected) {
                                                            selectedIndices - index
                                                        } else {
                                                            selectedIndices + index
                                                        }
                                                        errorMessage = null
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        imageVector = item.icon,
                                                        contentDescription = item.name,
                                                        tint = if (isSelected) Color(0xFF1A73E8) else item.color,
                                                        modifier = Modifier.size(38.dp)
                                                    )
                                                }

                                                if (isSelected) {
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .padding(4.dp)
                                                            .size(20.dp)
                                                            .background(Color(0xFF1A73E8), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "Selected",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    CaptchaType.MATH_CODE -> {
                        // Math/Security Code Box
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F4))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "নিরাপত্তা সমীকরণটি সমাধান করুন:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Color(0xFF202124),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = " Security Challenge: $mathA + $mathB = ? ",
                                        color = Color(0xFFFFD700),
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = userMathInput,
                                    onValueChange = {
                                        userMathInput = it
                                        errorMessage = null
                                    },
                                    label = { Text("উত্তর লিখুন (যেমন: ${mathA + mathB})", color = Color(0xFF374151), fontWeight = FontWeight.Medium) },
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = Color(0xFF111827),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF1A73E8),
                                        unfocusedBorderColor = Color(0xFF4B5563),
                                        focusedTextColor = Color(0xFF111827),
                                        unfocusedTextColor = Color(0xFF111827),
                                        focusedLabelColor = Color(0xFF1A73E8),
                                        unfocusedLabelColor = Color(0xFF374151),
                                        cursorColor = Color(0xFF1A73E8),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    CaptchaType.SLIDER -> {
                        // Drag Slider to verify
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F4))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "স্লাইডারটি টেনে ডানে নিয়ে ছেড়ে দিন:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Slider(
                                    value = sliderPosition,
                                    onValueChange = {
                                        sliderPosition = it
                                        errorMessage = null
                                        if (it >= 90f && !isVerifying) {
                                            isVerifying = true
                                            coroutineScope.launch {
                                                delay(400)
                                                onCaptchaVerified()
                                            }
                                        }
                                    },
                                    valueRange = 0f..100f,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF1A73E8),
                                        activeTrackColor = Color(0xFF34A853)
                                    )
                                )

                                Text(
                                    text = if (sliderPosition >= 90f) "✅ সিকিউরিটি লক উন্মুক্ত!" else "স্লাইড করুন: ${sliderPosition.toInt()}%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sliderPosition >= 90f) Color(0xFF2E7D32) else Color(0xFF374151)
                                )
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFD32F2F),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Refresh & Verify Control Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Refresh button switches challenge type!
                    IconButton(
                        onClick = {
                            errorMessage = null
                            selectedIndices = emptySet()
                            userMathInput = ""
                            sliderPosition = 0f
                            isChecked = false
                            isCheckingAnim = false
                            val allTypes = CaptchaType.values()
                            val nextTypeIndex = (captchaType.ordinal + 1) % allTypes.size
                            captchaType = allTypes[nextTypeIndex]
                            targetPairIndex = Random.nextInt(targetsList.size)
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Captcha",
                                tint = Color(0xFF1A73E8)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD32F2F))
                        ) {
                            Text(
                                text = "বাতিল",
                                color = Color(0xFFD32F2F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                isVerifying = true
                                var isValid = false

                                when (captchaType) {
                                    CaptchaType.CHECKBOX -> {
                                        isChecked = true
                                        isValid = true
                                    }

                                    CaptchaType.IMAGE_GRID -> {
                                        val correctIndices = gridItems.filter { it.isTarget }.map { it.index }.toSet()
                                        if (selectedIndices.isEmpty()) {
                                            errorMessage = "অনুগ্রহ করে অন্তত ১টি $targetName ছবি নির্বাচন করুন।"
                                        } else if (selectedIndices == correctIndices || selectedIndices.size >= 2) {
                                            isValid = true
                                        } else {
                                            errorMessage = "ভুল উত্তর! সঠিক ছবিগুলো নির্বাচন করে চেষ্টা করুন।"
                                        }
                                    }

                                    CaptchaType.MATH_CODE -> {
                                        if (userMathInput.trim() == mathAnswer) {
                                            isValid = true
                                        } else {
                                            errorMessage = "ভুল গণিত উত্তর! সঠিক সংখ্যাটি লিখুন।"
                                        }
                                    }

                                    CaptchaType.SLIDER -> {
                                        if (sliderPosition >= 85f) {
                                            isValid = true
                                        } else {
                                            errorMessage = "স্লাইডারটি পুরোপুরি ডানে টানুন।"
                                        }
                                    }
                                }

                                if (isValid) {
                                    onCaptchaVerified()
                                } else {
                                    isVerifying = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isVerifying
                        ) {
                            if (isVerifying) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = "Verify",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ভেরিফাই (VERIFY)", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpinBarDialog(
    userLevel: Int,
    onSpinCompleted: (bonusTaka: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var isSpinning by remember { mutableStateOf(false) }
    var isSpinCompleted by remember { mutableStateOf(false) }
    var wonBonusTaka by remember { mutableDoubleStateOf(0.0) }
    var isJackpotWinner by remember { mutableStateOf(false) }
    var spinMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    var currentRotationAngle by remember { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .border(2.5.dp, Color(0xFFFFD700), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141221)),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Banner
                Surface(
                    color = Color(0xFF2E7D32),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = "Spin Wheel",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ভেরিফিকেশন সফল! ✅",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "🎰 লাকি স্পিন বোনাস ড্র (লেভেল #$userLevel)",
                                color = Color(0xFFFFD700),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Spin Bar Options Preview Chips
                Text(
                    text = "স্পিন হুইল বোনাস ড্র (+১ কয়েন / +২ কয়েন বোনাস):",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val prizes = listOf("+১ কয়েন (১প)" to Color(0xFF42A5F5), "+২ কয়েন (২প) 🔥" to Color(0xFFFFD700))
                    for ((label, color) in prizes) {
                        Surface(
                            color = color.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, color),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = color,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SPIN WHEEL / BAR VISUAL REPRESENTATION
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF252139))
                        .border(4.dp, Color(0xFFFFD700), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Wheel Canvas with 6 colored sectors
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val colors = listOf(
                            Color(0xFF1E88E5), // +1p
                            Color(0xFFFFB300), // +2p
                            Color(0xFF43A047), // +1p
                            Color(0xFFFF8F00), // +2p
                            Color(0xFF8E24AA), // +1p
                            Color(0xFFFFD700)  // +2p
                        )
                        val sweep = 360f / 6f

                        for (i in 0 until 6) {
                            drawArc(
                                color = colors[i],
                                startAngle = currentRotationAngle + (i * sweep),
                                sweepAngle = sweep,
                                useCenter = true
                            )
                        }
                    }

                    // Center Hub Indicator
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color(0xFF141221), CircleShape)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Pointer",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SPIN ACTION & RESULT
                if (!isSpinCompleted) {
                    Button(
                        onClick = {
                            if (!isSpinning) {
                                isSpinning = true
                                coroutineScope.launch {
                                    // Rotate wheel animation simulation
                                    for (step in 1..24) {
                                        currentRotationAngle += 22.5f
                                        delay(70)
                                    }

                                    // Determine Outcome based on balanced profitable probabilities:
                                    val rand = Random.nextInt(100) // 0..99
                                    if (rand < 40) { // 40% chance -> 2 Paisa (0.02 Taka / 2 Coins bonus)
                                        wonBonusTaka = 0.02
                                        isJackpotWinner = true
                                        spinMessage = "🎉 মেগা স্পিন! আপনি +২ কয়েন (৳০.০২) এক্সট্রা বোনাস জিতেছেন!"
                                    } else { // 60% chance -> 1 Paisa (0.01 Taka / 1 Coin bonus)
                                        wonBonusTaka = 0.01
                                        isJackpotWinner = false
                                        spinMessage = "⭐ অভিনন্দন! আপনি +১ কয়েন (৳০.০১) স্পিন বোনাস পেয়েছেন!"
                                    }

                                    isSpinning = false
                                    isSpinCompleted = true
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !isSpinning
                    ) {
                        if (isSpinning) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("স্পিন ঘুরছে...", color = Color.Black, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Casino, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("স্পিন বার ঘুরান (SPIN NOW)", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        }
                    }
                } else {
                    // Spin Completed Winner Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isJackpotWinner) Color(0xFF880E4F) else Color(0xFF1B5E20))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = spinMessage,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "+৳${String.format(Locale.US, "%.2f", wonBonusTaka)} স্পিন বোনাস",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFD700)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Final Action Button: Watch last 2 Ads & Complete Level
                    Button(
                        onClick = {
                            onSpinCompleted(wonBonusTaka)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "Play Final Ads",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val totalEarned = 0.20 + wonBonusTaka
                            Text(
                                text = "শেষ ২টি ভিডিও অ্যাড দেখুন ও ৳${String.format(Locale.US, "%.2f", totalEarned)} ইনকাম নিন",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss) {
                    Text("বাতিল (Cancel)", color = Color.Gray)
                }
            }
        }
    }
}
