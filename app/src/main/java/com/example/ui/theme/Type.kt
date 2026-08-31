package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Ultra-high legibility & high-contrast typography system designed specifically for garment workers in busy/low-light environments
val Typography =
  Typography(
    displayLarge = TextStyle(
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.Black,
      fontSize = 34.sp,
      lineHeight = 42.sp,
      letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.ExtraBold,
      fontSize = 30.sp,
      lineHeight = 38.sp,
      letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.ExtraBold,
      fontSize = 26.sp,
      lineHeight = 34.sp,
      letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.ExtraBold,
      fontSize = 26.sp,
      lineHeight = 34.sp,
      letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.ExtraBold,
      fontSize = 23.sp,
      lineHeight = 31.sp,
      letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.Bold,
      fontSize = 21.sp,
      lineHeight = 29.sp,
      letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.ExtraBold,
      fontSize = 21.sp,
      lineHeight = 29.sp,
      letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.ExtraBold,
      fontSize = 19.sp,
      lineHeight = 26.sp,
      letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.Bold,
      fontSize = 17.sp,
      lineHeight = 23.sp,
      letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.Bold,
      fontSize = 18.sp,
      lineHeight = 26.sp,
      letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.SemiBold,
      fontSize = 16.sp,
      lineHeight = 24.sp,
      letterSpacing = 0.2.sp
    ),
    bodySmall = TextStyle(
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.Bold,
      fontSize = 14.5.sp,
      lineHeight = 20.sp,
      letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.ExtraBold,
      fontSize = 17.sp,
      lineHeight = 22.sp,
      letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.ExtraBold,
      fontSize = 15.sp,
      lineHeight = 20.sp,
      letterSpacing = 0.25.sp
    ),
    labelSmall = TextStyle(
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.ExtraBold,
      fontSize = 13.5.sp,
      lineHeight = 18.sp,
      letterSpacing = 0.25.sp
    )
  )
