package com.krilatokolo.wingeddriver.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
   primary = Color(0xffaac7ff),
   secondary = Color(0xffbec6dc),
   tertiary = Color(0xffddbce0)
)

private val LightColorScheme = lightColorScheme(
   primary = Color(0xff415f91),
   secondary = Color(0xff565f71),
   tertiary = Color(0xFF705575)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WingedDriverTheme(
   darkTheme: Boolean = isSystemInDarkTheme(),
   // Dynamic color is available on Android 12+
   dynamicColor: Boolean = true,
   content: @Composable () -> Unit,
) {
   val colorScheme = when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
         val context = LocalContext.current
         if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
   }

   MaterialTheme(
      colorScheme = colorScheme,
      typography = MyTypography,
   ) {
      if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
         // Workaround for https://issuetracker.google.com/issues/274471576 - Ripple effects do not work on Android 13
         val defaultRippleConfig = LocalRippleConfiguration.current ?: RippleConfiguration()
         val transparencyFixRippleConfiguration = RippleConfiguration(
            color = defaultRippleConfig.color,
            rippleAlpha = defaultRippleConfig.rippleAlpha?.run {
               RippleAlpha(
                  draggedAlpha,
                  focusedAlpha,
                  hoveredAlpha,
                  pressedAlpha = pressedAlpha.coerceAtLeast(MIN_RIPPLE_ALPHA_ON_TIRAMISU)
               )
            }

         )
         CompositionLocalProvider(LocalRippleConfiguration provides transparencyFixRippleConfiguration) {
            content()
         }
      } else {
         content()
      }
   }
}

private const val MIN_RIPPLE_ALPHA_ON_TIRAMISU = 0.5f
