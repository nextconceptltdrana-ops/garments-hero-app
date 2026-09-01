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
    primary = Color(0xFFFFD700),          // Radiant Gold Primary
    onPrimary = Color(0xFF1E1B2E),
    primaryContainer = Color(0xFF2E2405),
    onPrimaryContainer = Color(0xFFFFE57F),
    secondary = Color(0xFF00E676),        // Neon Emerald Secondary
    onSecondary = Color(0xFF003816),
    secondaryContainer = Color(0xFF004D25),
    onSecondaryContainer = Color(0xFFB9F6CA),
    tertiary = Color(0xFFFF5252),
    onTertiary = Color.White,
    background = Color(0xFF0B0F19),       // Deep Luxury Space Slate
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF161B2E),          // High-End Card Surface
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1E253D),
    onSurfaceVariant = Color(0xFFF1F5F9),
    outline = Color(0xFF3B4764)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF00695C),          // Deep Rich Emerald
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2F1),
    onPrimaryContainer = Color(0xFF00362F),
    secondary = Color(0xFFF59E0B),        // Warm Radiant Amber
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = Color(0xFFE53935),
    onTertiary = Color.White,
    background = Color(0xFFF1F5F9),       // Soft Luxury Slate White
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),          // Pure White
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF1E293B),
    outline = Color(0xFFCBD5E1)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
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

