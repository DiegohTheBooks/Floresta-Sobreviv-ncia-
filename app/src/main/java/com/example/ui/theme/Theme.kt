package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SurvivalColorScheme = darkColorScheme(
  primary = ForestGreen80,
  onPrimary = Color(0xFF0A250D),
  primaryContainer = ForestGreen40,
  onPrimaryContainer = Color(0xFFA5F4AA),
  secondary = AmberEmber80,
  onSecondary = Color(0xFF451900),
  secondaryContainer = AmberEmber40,
  onSecondaryContainer = Color(0xFFFFDBCF),
  tertiary = RiverCyan80,
  onTertiary = Color(0xFF003730),
  background = SurvivalDarkBackground,
  onBackground = Color(0xFFE2E6E2),
  surface = SurvivalDarkSurface,
  onSurface = Color(0xFFE2E6E2),
  surfaceVariant = SurvivalDarkSurfaceVariant,
  onSurfaceVariant = Color(0xFFC3C9C2),
  outline = SurvivalCardBorder,
  error = VitalHealth,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      window.statusBarColor = Color(0xFF0F1410).toArgb()
      window.navigationBarColor = Color(0xFF0F1410).toArgb()
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
      WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
    }
  }

  MaterialTheme(
    colorScheme = SurvivalColorScheme,
    typography = Typography,
    content = content
  )
}
