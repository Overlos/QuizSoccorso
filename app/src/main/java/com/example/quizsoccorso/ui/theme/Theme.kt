package com.example.quizsoccorso.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFE3E2E6),
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFC7C6CA),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F)
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

private val LivelyLightColorScheme = lightColorScheme(
    primary = LivelyCoral,
    secondary = LivelyAmber,
    tertiary = LivelySage,
    background = LivelyCream,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = LivelyWarmGray,
    onSurface = LivelyWarmGray,
    surfaceVariant = Color(0xFFFFF2DF)
)

private val LivelyDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF8A80), // Lighter Coral for dark mode
    secondary = Color(0xFFFFD54F), // Lighter Amber for dark mode
    tertiary = Color(0xFF81C784),  // Lighter Sage for dark mode
    background = LivelyCharcoal,
    surface = Color(0xFF373E47),
    onPrimary = Color(0xFF373E47),
    onBackground = Color(0xFFECEFF1),
    onSurface = Color(0xFFECEFF1),
    surfaceVariant = Color(0xFF454D55)
)

private val AccessibleColorScheme = lightColorScheme(
    primary = AccessibleBlue,
    secondary = AccessibleOrange,
    tertiary = AccessibleYellow,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    surfaceVariant = Color(0xFFE5F1F8)
)

private val OledColorScheme = darkColorScheme(
    primary = Color(0xFFF0F0F0),
    secondary = PurpleGrey80,
    background = Color.Black,
    surface = Color.Black,
    onBackground = Color(0xFFF0F0F0),
    onSurface = Color(0xFFF0F0F0),
    onPrimary = Color.Black,
    surfaceVariant = Color(0xFF121212)
)

private val ReadingColorScheme = lightColorScheme(
    primary = SepiaText,
    secondary = SepiaSecondary,
    background = SepiaBackground,
    surface = SepiaSurface,
    onBackground = SepiaText,
    onSurface = SepiaText,
    onSecondary = Color.White,
    surfaceVariant = Color(0xFFE9DEC4)
)

@Composable
fun QuizSoccorsoTheme(
    appTheme: com.example.quizsoccorso.AppTheme = com.example.quizsoccorso.AppTheme.SYSTEM,
    fontSizeMultiplier: Float = 1.0f,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appTheme) {
        com.example.quizsoccorso.AppTheme.LIGHT -> false
        com.example.quizsoccorso.AppTheme.DARK -> true
        com.example.quizsoccorso.AppTheme.LIGHT_LIVELY -> false
        com.example.quizsoccorso.AppTheme.DARK_LIVELY -> true
        com.example.quizsoccorso.AppTheme.ACCESSIBLE -> false
        com.example.quizsoccorso.AppTheme.OLED -> true
        com.example.quizsoccorso.AppTheme.READING -> false
        com.example.quizsoccorso.AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when (appTheme) {
        com.example.quizsoccorso.AppTheme.LIGHT -> LightColorScheme
        com.example.quizsoccorso.AppTheme.DARK -> DarkColorScheme
        com.example.quizsoccorso.AppTheme.LIGHT_LIVELY -> LivelyLightColorScheme
        com.example.quizsoccorso.AppTheme.DARK_LIVELY -> LivelyDarkColorScheme
        com.example.quizsoccorso.AppTheme.ACCESSIBLE -> AccessibleColorScheme
        com.example.quizsoccorso.AppTheme.OLED -> OledColorScheme
        com.example.quizsoccorso.AppTheme.READING -> ReadingColorScheme
        com.example.quizsoccorso.AppTheme.SYSTEM -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) DarkColorScheme else LightColorScheme
            }
        }
    }

    // Applica il moltiplicatore alla tipografia, inclusa l'altezza della riga (lineHeight)
    // Usiamo un moltiplicatore leggermente superiore per il lineHeight (1.2x) per evitare sovrapposizioni verticali
    val lineMultiplier = 1.25f
    val scaledTypography = Typography.let {
        it.copy(
            headlineLarge = it.headlineLarge.copy(
                fontSize = it.headlineLarge.fontSize * fontSizeMultiplier,
                lineHeight = it.headlineLarge.lineHeight * fontSizeMultiplier * lineMultiplier
            ),
            headlineMedium = it.headlineMedium.copy(
                fontSize = it.headlineMedium.fontSize * fontSizeMultiplier,
                lineHeight = it.headlineMedium.lineHeight * fontSizeMultiplier * lineMultiplier
            ),
            headlineSmall = it.headlineSmall.copy(
                fontSize = it.headlineSmall.fontSize * fontSizeMultiplier,
                lineHeight = it.headlineSmall.lineHeight * fontSizeMultiplier * lineMultiplier
            ),
            titleLarge = it.titleLarge.copy(
                fontSize = it.titleLarge.fontSize * fontSizeMultiplier,
                lineHeight = it.titleLarge.lineHeight * fontSizeMultiplier * lineMultiplier
            ),
            titleMedium = it.titleMedium.copy(
                fontSize = it.titleMedium.fontSize * fontSizeMultiplier,
                lineHeight = it.titleMedium.lineHeight * fontSizeMultiplier * lineMultiplier
            ),
            titleSmall = it.titleSmall.copy(
                fontSize = it.titleSmall.fontSize * fontSizeMultiplier,
                lineHeight = it.titleSmall.lineHeight * fontSizeMultiplier * lineMultiplier
            ),
            bodyLarge = it.bodyLarge.copy(
                fontSize = it.bodyLarge.fontSize * fontSizeMultiplier,
                lineHeight = it.bodyLarge.lineHeight * fontSizeMultiplier * lineMultiplier
            ),
            bodyMedium = it.bodyMedium.copy(
                fontSize = it.bodyMedium.fontSize * fontSizeMultiplier,
                lineHeight = it.bodyMedium.lineHeight * fontSizeMultiplier * lineMultiplier
            ),
            bodySmall = it.bodySmall.copy(
                fontSize = it.bodySmall.fontSize * fontSizeMultiplier,
                lineHeight = it.bodySmall.lineHeight * fontSizeMultiplier * lineMultiplier
            ),
            labelLarge = it.labelLarge.copy(
                fontSize = it.labelLarge.fontSize * fontSizeMultiplier,
                lineHeight = it.labelLarge.lineHeight * fontSizeMultiplier * lineMultiplier
            ),
            labelMedium = it.labelMedium.copy(
                fontSize = it.labelMedium.fontSize * fontSizeMultiplier,
                lineHeight = it.labelMedium.lineHeight * fontSizeMultiplier * lineMultiplier
            ),
            labelSmall = it.labelSmall.copy(
                fontSize = it.labelSmall.fontSize * fontSizeMultiplier,
                lineHeight = it.labelSmall.lineHeight * fontSizeMultiplier * lineMultiplier
            )
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = content
    )
}
