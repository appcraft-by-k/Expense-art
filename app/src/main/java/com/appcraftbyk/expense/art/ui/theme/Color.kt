package com.appcraftbyk.expense.art.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/*
 * Color tokens from PRD §5.1.
 * Each token is declared once per theme so screens never hardcode hex values.
 */

// Light theme
internal val LightPrimary = Color(0xFF2563EB)
internal val LightOnPrimary = Color(0xFFFFFFFF)
internal val LightPrimaryContainer = Color(0xFFDCE7FF)
internal val LightOnPrimaryContainer = Color(0xFF0A2E6F)
internal val LightInversePrimary = Color(0xFF9CC0FF)

internal val LightSecondary = Color(0xFF4B5A6E)
internal val LightOnSecondary = Color(0xFFFFFFFF)
internal val LightSecondaryContainer = Color(0xFFE2E8F0)
internal val LightOnSecondaryContainer = Color(0xFF1B2733)

internal val LightTertiary = Color(0xFF16875D)
internal val LightOnTertiary = Color(0xFFFFFFFF)
internal val LightTertiaryContainer = Color(0xFFD7F2E6)
internal val LightOnTertiaryContainer = Color(0xFF05382A)

internal val LightBackground = Color(0xFFF7F8FA)
internal val LightOnBackground = Color(0xFF17191D)
internal val LightSurface = Color(0xFFFFFFFF)
internal val LightOnSurface = Color(0xFF17191D)
internal val LightSurfaceVariant = Color(0xFFF0F2F5)
internal val LightOnSurfaceVariant = Color(0xFF68707D)

internal val LightInverseSurface = Color(0xFF23262C)
internal val LightInverseOnSurface = Color(0xFFF4F5F7)

internal val LightError = Color(0xFFD94A4A)
internal val LightOnError = Color(0xFFFFFFFF)
internal val LightErrorContainer = Color(0xFFFBE3E3)
internal val LightOnErrorContainer = Color(0xFF4A1010)

internal val LightOutline = Color(0xFFC7CCD4)
internal val LightOutlineVariant = Color(0xFFE4E7EC)

internal val LightSurfaceBright = Color(0xFFFFFFFF)
internal val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
internal val LightSurfaceContainerLow = Color(0xFFFCFCFD)
internal val LightSurfaceContainer = Color(0xFFF7F8FA)
internal val LightSurfaceContainerHigh = Color(0xFFF0F2F5)
internal val LightSurfaceContainerHighest = Color(0xFFE9ECF1)
internal val LightSurfaceDim = Color(0xFFE9ECF1)

internal val LightPrimaryFixed = Color(0xFFDCE7FF)
internal val LightPrimaryFixedDim = Color(0xFFA9C6FF)
internal val LightOnPrimaryFixed = Color(0xFF0A2E6F)
internal val LightOnPrimaryFixedVariant = Color(0xFF1B4A9C)
internal val LightSecondaryFixed = Color(0xFFE2E8F0)
internal val LightSecondaryFixedDim = Color(0xFFB9C6D9)
internal val LightOnSecondaryFixed = Color(0xFF1B2733)
internal val LightOnSecondaryFixedVariant = Color(0xFF33465C)
internal val LightTertiaryFixed = Color(0xFFD7F2E6)
internal val LightTertiaryFixedDim = Color(0xFFA8DFC8)
internal val LightOnTertiaryFixed = Color(0xFF05382A)
internal val LightOnTertiaryFixedVariant = Color(0xFF0C5A40)

// Dark theme
internal val DarkPrimary = Color(0xFF76A7FF)
internal val DarkOnPrimary = Color(0xFF06285C)
internal val DarkPrimaryContainer = Color(0xFF1B4A9C)
internal val DarkOnPrimaryContainer = Color(0xFFDCE7FF)
internal val DarkInversePrimary = Color(0xFF2563EB)

internal val DarkSecondary = Color(0xFFB6C4D9)
internal val DarkOnSecondary = Color(0xFF1E2A3A)
internal val DarkSecondaryContainer = Color(0xFF33455C)
internal val DarkOnSecondaryContainer = Color(0xFFDCE6F2)

internal val DarkTertiary = Color(0xFF55C89A)
internal val DarkOnTertiary = Color(0xFF00382A)
internal val DarkTertiaryContainer = Color(0xFF0C4A36)
internal val DarkOnTertiaryContainer = Color(0xFFB7F0D8)

internal val DarkBackground = Color(0xFF101114)
internal val DarkOnBackground = Color(0xFFF4F5F7)
internal val DarkSurface = Color(0xFF1B1D22)
internal val DarkOnSurface = Color(0xFFF4F5F7)
internal val DarkSurfaceVariant = Color(0xFF15161A)
internal val DarkOnSurfaceVariant = Color(0xFFA7ADB8)

internal val DarkInverseSurface = Color(0xFFF4F5F7)
internal val DarkInverseOnSurface = Color(0xFF1B1D22)

internal val DarkError = Color(0xFFFF7777)
internal val DarkOnError = Color(0xFF4A0D0D)
internal val DarkErrorContainer = Color(0xFF6B1F1F)
internal val DarkOnErrorContainer = Color(0xFFFFDAD6)

internal val DarkOutline = Color(0xFF4C515B)
internal val DarkOutlineVariant = Color(0xFF2B2E35)

internal val DarkSurfaceBright = Color(0xFF343841)
internal val DarkSurfaceContainerLowest = Color(0xFF0C0D0F)
internal val DarkSurfaceContainerLow = Color(0xFF17181C)
internal val DarkSurfaceContainer = Color(0xFF1B1D22)
internal val DarkSurfaceContainerHigh = Color(0xFF22242A)
internal val DarkSurfaceContainerHighest = Color(0xFF2A2D34)
internal val DarkSurfaceDim = Color(0xFF101114)

internal val DarkPrimaryFixed = Color(0xFFDCE7FF)
internal val DarkPrimaryFixedDim = Color(0xFFA9C6FF)
internal val DarkOnPrimaryFixed = Color(0xFF0A2E6F)
internal val DarkOnPrimaryFixedVariant = Color(0xFF1B4A9C)
internal val DarkSecondaryFixed = Color(0xFFE2E8F0)
internal val DarkSecondaryFixedDim = Color(0xFFB9C6D9)
internal val DarkOnSecondaryFixed = Color(0xFF1B2733)
internal val DarkOnSecondaryFixedVariant = Color(0xFF33465C)
internal val DarkTertiaryFixed = Color(0xFFD7F2E6)
internal val DarkTertiaryFixedDim = Color(0xFFA8DFC8)
internal val DarkOnTertiaryFixed = Color(0xFF05382A)
internal val DarkOnTertiaryFixedVariant = Color(0xFF0C5A40)

/**
 * Semantic finance colors that Material 3's [androidx.compose.material3.ColorScheme] does not
 * define. Borrowed and lent money must stay visually distinct, so these are first-class tokens.
 *
 * `positive`, `negative` and `warning` match PRD §5.1 and are intended for amounts, icons and
 * fill accents. For small text on a [negativeContainer] / [warningContainer] / [positiveContainer],
 * use the matching `on...Container` color to keep WCAG AA contrast.
 */
@Immutable
data class ExpenseArtColors(
    val positive: Color,
    val positiveContainer: Color,
    val onPositiveContainer: Color,
    val negative: Color,
    val negativeContainer: Color,
    val onNegativeContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color
)

internal val LightExpenseArtColors = ExpenseArtColors(
    positive = Color(0xFF16875D),
    positiveContainer = Color(0xFFDCF3E9),
    onPositiveContainer = Color(0xFF07392A),
    negative = Color(0xFFD94A4A),
    negativeContainer = Color(0xFFFBE3E3),
    onNegativeContainer = Color(0xFF4A1010),
    warning = Color(0xFFD68A17),
    warningContainer = Color(0xFFFAEDD7),
    onWarningContainer = Color(0xFF432C00)
)

internal val DarkExpenseArtColors = ExpenseArtColors(
    positive = Color(0xFF55C89A),
    positiveContainer = Color(0xFF10402F),
    onPositiveContainer = Color(0xFFB7F0D8),
    negative = Color(0xFFFF7777),
    negativeContainer = Color(0xFF4C1717),
    onNegativeContainer = Color(0xFFFFD9D9),
    warning = Color(0xFFF2B95C),
    warningContainer = Color(0xFF45320F),
    onWarningContainer = Color(0xFFFFE1B0)
)
