package com.pixelwarrior.monsters.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Pixel art inspired colors
val PixelBlack = Color(0xFF000000)
val PixelWhite = Color(0xFFFFFFFF)
val PixelDarkGray = Color(0xFF404040)
val PixelGray = Color(0xFF808080)
val PixelLightGray = Color(0xFFC0C0C0)

val PixelBlue = Color(0xFF0066CC)
val PixelDarkBlue = Color(0xFF003366)
val PixelLightBlue = Color(0xFF66AAFF)

val PixelGreen = Color(0xFF00AA00)
val PixelDarkGreen = Color(0xFF005500)
val PixelLightGreen = Color(0xFF66FF66)

val PixelRed = Color(0xFFCC0000)

// Game specific colors
val HpRed = Color(0xFFCC0000)
val MpBlue = Color(0xFF0000CC)
val ExpYellow = Color(0xFFFFCC00)
val GoldOrange = Color(0xFFFF8800)

private val DarkColorScheme = darkColorScheme(
    primary = PixelBlue,
    secondary = PixelGreen,
    tertiary = ExpYellow,
    background = PixelBlack,
    surface = PixelDarkGray,
    onPrimary = PixelWhite,
    onSecondary = PixelWhite,
    onTertiary = PixelBlack,
    onBackground = PixelWhite,
    onSurface = PixelWhite,
)

private val LightColorScheme = lightColorScheme(
    primary = PixelDarkBlue,
    secondary = PixelDarkGreen,
    tertiary = ExpYellow,
    background = PixelLightGray,
    surface = PixelWhite,
    onPrimary = PixelWhite,
    onSecondary = PixelWhite,
    onTertiary = PixelBlack,
    onBackground = PixelBlack,
    onSurface = PixelBlack,
)

@Composable
fun PixelWarriorMonstersTheme(
    darkTheme: Boolean = true, // Default to dark theme for pixel art aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PixelTypography,
        content = content
    )
}