package com.pixelwarrior.monsters.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Classic DQM-inspired color palette
val PixelBlack = Color(0xFF000000)
val PixelWhite = Color(0xFFFFFFFF)
val PixelDarkGray = Color(0xFF404040)
val PixelGray = Color(0xFF808080)
val PixelLightGray = Color(0xFFC0C0C0)

// DQM Royal Blues (for UI frames and important elements)
val DQMRoyalBlue = Color(0xFF1E3A8A)
val DQMDeepBlue = Color(0xFF1E40AF)
val DQMLightBlue = Color(0xFF3B82F6)
val DQMSkyBlue = Color(0xFF60A5FA)

// Classic DQM Greens (for nature/earth elements)
val DQMForestGreen = Color(0xFF15803D)
val DQMEmerald = Color(0xFF10B981)
val DQMLimeGreen = Color(0xFF84CC16)

// DQM Warm Colors (for fire/energy elements) 
val DQMCrimson = Color(0xFFDC2626)
val DQMGoldenYellow = Color(0xFFF59E0B)
val DQMAmber = Color(0xFFD97706)
val DQMOrange = Color(0xFFEA580C)

// DQM Special Colors
val DQMPurple = Color(0xFF7C3AED)
val DQMTeal = Color(0xFF0D9488)
val DQMRose = Color(0xFFE11D48)
val DQMIndigo = Color(0xFF4338CA)

// Legacy colors for backward compatibility
val PixelBlue = DQMRoyalBlue
val PixelDarkBlue = DQMDeepBlue
val PixelLightBlue = DQMLightBlue

val PixelGreen = DQMForestGreen
val PixelDarkGreen = Color(0xFF005500)
val PixelLightGreen = DQMEmerald

val PixelRed = DQMCrimson
val PixelYellow = DQMGoldenYellow
val PixelGold = DQMAmber
val PixelPurple = DQMPurple
val PixelBrown = Color(0xFF8B4513)

// Game specific UI colors
val HpRed = DQMCrimson
val MpBlue = DQMRoyalBlue
val ExpYellow = DQMGoldenYellow
val GoldOrange = DQMOrange
val AffectionPink = DQMRose
val SynthesisGreen = DQMEmerald

// Monster type colors (DQM-inspired)
val TypeNormal = Color(0xFF9CA3AF)
val TypeFire = DQMCrimson
val TypeWater = DQMLightBlue
val TypeGrass = DQMForestGreen
val TypeElectric = DQMGoldenYellow
val TypeIce = Color(0xFF67E8F9)
val TypeFighting = Color(0xFFF97316)
val TypePoison = DQMPurple
val TypeGround = Color(0xFFA16207)
val TypeFlying = Color(0xFF818CF8)
val TypePsychic = DQMTeal
val TypeBug = Color(0xFF84CC16)
val TypeRock = Color(0xFF78716C)
val TypeGhost = DQMIndigo
val TypeDragon = Color(0xFF6366F1)
val TypeDark = Color(0xFF374151)
val TypeSteel = Color(0xFF94A3B8)

private val DarkColorScheme = darkColorScheme(
    primary = DQMRoyalBlue,
    secondary = DQMForestGreen,
    tertiary = DQMGoldenYellow,
    background = PixelBlack,
    surface = PixelDarkGray,
    onPrimary = PixelWhite,
    onSecondary = PixelWhite,
    onTertiary = PixelBlack,
    onBackground = PixelWhite,
    onSurface = PixelWhite,
)

private val LightColorScheme = lightColorScheme(
    primary = DQMDeepBlue,
    secondary = Color(0xFF0F766E),
    tertiary = DQMAmber,
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