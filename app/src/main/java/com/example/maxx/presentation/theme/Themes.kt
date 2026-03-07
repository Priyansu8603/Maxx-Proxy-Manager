package com.example.maxx.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
//  Extra semantic tokens not covered by Material3 ColorScheme
//  (latency colours, active-connection banner bg, search-bar bg, etc.)
// ─────────────────────────────────────────────────────────────────────────────
data class AppExtraColors(
    // Status / latency
    val latencyGood: Color,
    val latencyMedium: Color,
    val latencyBad: Color,
    val statusTesting: Color,
    // Active-connection banner background
    val bannerBackground: Color,
    // Elevated search-bar / chip background
    val elevatedSurface: Color,
    // Protocol-badge background
    val badgeBackground: Color,
    // Protocol-badge text
    val badgeText: Color,
    // Muted label (e.g. "ACTIVE CONNECTION")
    val mutedLabel: Color,
)

private val LightExtraColors = AppExtraColors(
    latencyGood    = LatencyGood,
    latencyMedium  = LatencyMedium,
    latencyBad     = LatencyBad,
    statusTesting  = StatusTesting,
    bannerBackground  = Color(0xFFF0F4FF),
    elevatedSurface   = Neutral100,
    badgeBackground   = Color(0xFFE8EEFF),
    badgeText         = Brand600,
    mutedLabel        = Color(0xFF8888AA),
)

private val DarkExtraColors = AppExtraColors(
    latencyGood    = LatencyGood,
    latencyMedium  = LatencyMedium,
    latencyBad     = LatencyBad,
    statusTesting  = StatusTesting,
    bannerBackground  = Dark100,
    elevatedSurface   = Dark200,
    badgeBackground   = Dark300,
    badgeText         = Brand400,
    mutedLabel        = Dark600,
)

// Composition Local so any composable can read extra colours without prop-drilling
val LocalAppExtraColors = staticCompositionLocalOf { LightExtraColors }

/**
 * Composition local that carries the **app-level** dark-mode flag.
 * Always use this instead of [isSystemInDarkTheme] when the app has its
 * own theme toggle — the two can differ when the user overrides the system.
 */
val LocalAppIsDark = staticCompositionLocalOf { false }

/** Shortcut: `MaterialTheme.extraColors` */
val MaterialTheme.extraColors: AppExtraColors
    @Composable @ReadOnlyComposable get() = LocalAppExtraColors.current

/** Shortcut: `MaterialTheme.isDark` — reads the **app** dark flag, not the system one */
val MaterialTheme.isDark: Boolean
    @Composable @ReadOnlyComposable get() = LocalAppIsDark.current

// ─────────────────────────────────────────────────────────────────────────────
//  Full Material3 color schemes
// ─────────────────────────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary            = Brand600,
    onPrimary          = Neutral0,
    primaryContainer   = Color(0xFFDDE3FF),
    onPrimaryContainer = Color(0xFF001356),
    secondary          = Color(0xFF575E91),
    onSecondary        = Neutral0,
    background         = Neutral50,
    onBackground       = Neutral900,
    surface            = Neutral0,
    onSurface          = Neutral900,
    surfaceVariant     = Neutral100,
    onSurfaceVariant   = Neutral600,
    outline            = Neutral200,
    error              = LatencyBad,
    onError            = Neutral0,
)

private val DarkColorScheme = darkColorScheme(
    primary            = Brand400,
    onPrimary          = Neutral900,
    primaryContainer   = Color(0xFF172872),
    onPrimaryContainer = Color(0xFFDDE3FF),
    secondary          = Color(0xFFBBC3FA),
    onSecondary        = Color(0xFF252D61),
    background         = Dark50,
    onBackground       = Neutral0,
    surface            = Dark100,
    onSurface          = Neutral0,
    surfaceVariant     = Dark200,
    onSurfaceVariant   = Dark400,
    outline            = Dark300,
    error              = LatencyBad,
    onError            = Neutral0,
)

// ─────────────────────────────────────────────────────────────────────────────
//  MaxxTheme — single entry point for the whole app
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MaxxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extraColors = if (darkTheme) DarkExtraColors else LightExtraColors

    CompositionLocalProvider(
        LocalAppExtraColors provides extraColors,
        LocalAppIsDark      provides darkTheme,   // ← app-level flag, not system
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = AppTypography,
            content     = content
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  CustomColors — LEGACY shim, delegates to MaterialTheme.
//  Kept so existing screens that call CustomColors.backgroundColor(isDarkMode)
//  continue to compile during migration. Will be deleted once all screens
//  are migrated to MaterialTheme.colorScheme directly.
// ─────────────────────────────────────────────────────────────────────────────
@Deprecated("Use MaterialTheme.colorScheme.background instead")
object CustomColors {
    val lightBackground = LightBackground
    val lightCard       = LightCard
    val darkBackground  = DarkBackground
    val darkCard        = DarkCard

    fun backgroundColor(isDarkMode: Boolean) = if (isDarkMode) DarkBackground else LightBackground
    fun cardColor(isDarkMode: Boolean)        = if (isDarkMode) DarkCard       else LightCard
    fun editButtonBackground(isDarkMode: Boolean) = if (isDarkMode) Neutral0   else Neutral900
    fun editButtonIcon(isDarkMode: Boolean)        = if (isDarkMode) Neutral900 else Neutral0
}
