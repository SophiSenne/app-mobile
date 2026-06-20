package com.example.hanger.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ===== Esquema de cores claro (padrão do app) =====
private val LightColors = lightColorScheme(
    primary = HangerBlack,
    onPrimary = HangerCream,
    secondary = HangerPink,
    onSecondary = Color.White,
    tertiary = HangerGold,
    onTertiary = HangerBlack,
    background = HangerCream,
    onBackground = HangerBlack,
    surface = HangerCream,
    onSurface = HangerBlack,
    surfaceVariant = HangerInputBg,
    onSurfaceVariant = HangerGray,
    outline = HangerBorder,
    error = HangerPink,
    onError = Color.White
)

// ===== Esquema de cores escuro =====
private val DarkColors = darkColorScheme(
    primary = HangerCream,
    onPrimary = HangerBlack,
    secondary = HangerPink,
    onSecondary = Color.White,
    tertiary = HangerGold,
    onTertiary = HangerBlack,
    background = HangerBlack,
    onBackground = HangerCream,
    surface = Color(0xFF1A1A1A),
    onSurface = HangerCream,
    surfaceVariant = HangerPlum,
    onSurfaceVariant = HangerGrayLight,
    outline = HangerGray,
    error = HangerPink,
    onError = Color.White
)

val HangerTypography = Typography(
    headlineSmall = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 20.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, letterSpacing = 4.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 0.8.sp)
)

/**
 * Tema do app Hanger.
 *
 * @param darkTheme se deve usar o esquema escuro (padrão: segue o sistema)
 * @param dynamicColor se deve usar Material You (Android 12+). Como o Hanger tem
 * identidade visual própria, o padrão é false para preservar a paleta da marca.
 */
@Composable
fun HangerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HangerTypography,
        content = content
    )
}