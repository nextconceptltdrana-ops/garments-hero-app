package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFF00E676),
    onPrimary = Color(0xFF00220F),
    primaryContainer = Color(0xFF00522B),
    onPrimaryContainer = Color(0xFFB9F6CA),
    secondary = Color(0xFFFFAB40),
    onSecondary = Color(0xFF3E1F00),
    secondaryContainer = Color(0xFF5C3300),
    onSecondaryContainer = Color(0xFFFFD180),
    tertiary = Color(0xFFFF5252),
    onTertiary = Color.White,
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFF1F5F9),
    outline = Color(0xFF94A3B8)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF005A3E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF00281A),
    secondary = Color(0xFFC25E00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFECB3),
    onSecondaryContainer = Color(0xFF3E2723),
    tertiary = Color(0xFFC62828),
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF1E293B),
    outline = Color(0xFF475569)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Set dynamicColor to false by default for crisp, consistent high-contrast colors across all devices
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
