package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CourierColorScheme = darkColorScheme(
  primary = PrimaryBlue,
  onPrimary = Color.Black,
  primaryContainer = PrimaryBlueVariant,
  onPrimaryContainer = TextPrimary,
  secondary = SecondaryTeal,
  onSecondary = Color.Black,
  secondaryContainer = SurfaceVariantDark,
  onSecondaryContainer = TextPrimary,
  tertiary = CashGold,
  onTertiary = Color.Black,
  background = BackgroundDark,
  onBackground = TextPrimary,
  surface = SurfaceDark,
  onSurface = TextPrimary,
  surfaceVariant = SurfaceVariantDark,
  onSurfaceVariant = TextSecondary,
  outline = CardBorderDark,
  error = StatusError,
  onError = Color.White
)

@Composable
fun SenKuryeTheme(
  darkTheme: Boolean = true,
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = CourierColorScheme,
    typography = Typography,
    content = content
  )
}

