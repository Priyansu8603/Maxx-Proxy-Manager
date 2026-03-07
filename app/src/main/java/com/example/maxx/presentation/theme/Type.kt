package com.example.maxx.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.maxx.R

// ─────────────────────────────────────────────────────────────────────────────
//  Poppins font family — loaded from res/font/
//  Single source of truth: every TextStyle below references PoppinsFontFamily.
//  MaxxTheme already passes AppTypography → MaterialTheme, so all Text()
//  composables in every screen automatically use Poppins with zero changes.
// ─────────────────────────────────────────────────────────────────────────────
val PoppinsFontFamily = FontFamily(
    Font(R.font.poppins_light,    weight = FontWeight.Light),
    Font(R.font.poppins_regular,  weight = FontWeight.Normal),
    Font(R.font.poppins_medium,   weight = FontWeight.Medium),
    Font(R.font.poppins_semibold, weight = FontWeight.SemiBold),
    Font(R.font.poppins_bold,     weight = FontWeight.Bold),
    Font(R.font.poppins_italic,   weight = FontWeight.Normal, style = FontStyle.Italic),
)

// ─────────────────────────────────────────────────────────────────────────────
//  Full Material3 typography — every role uses PoppinsFontFamily
// ─────────────────────────────────────────────────────────────────────────────
val AppTypography = Typography(
    // ── Display ────────────────────────────────────────────────────────────
    displayLarge = TextStyle(
        fontFamily   = PoppinsFontFamily,
        fontWeight   = FontWeight.Light,
        fontSize     = 57.sp,
        lineHeight   = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Light,
        fontSize   = 45.sp,
        lineHeight = 52.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 36.sp,
        lineHeight = 44.sp,
    ),

    // ── Headline ───────────────────────────────────────────────────────────
    headlineLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 32.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 24.sp,
        lineHeight = 32.sp,
    ),

    // ── Title ──────────────────────────────────────────────────────────────
    titleLarge = TextStyle(
        fontFamily   = PoppinsFontFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 22.sp,
        lineHeight   = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily   = PoppinsFontFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 16.sp,
        lineHeight   = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily   = PoppinsFontFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 14.sp,
        lineHeight   = 20.sp,
        letterSpacing = 0.1.sp,
    ),

    // ── Body ───────────────────────────────────────────────────────────────
    bodyLarge = TextStyle(
        fontFamily   = PoppinsFontFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 16.sp,
        lineHeight   = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily   = PoppinsFontFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 14.sp,
        lineHeight   = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily   = PoppinsFontFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 12.sp,
        lineHeight   = 16.sp,
        letterSpacing = 0.4.sp,
    ),

    // ── Label ──────────────────────────────────────────────────────────────
    labelLarge = TextStyle(
        fontFamily   = PoppinsFontFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 14.sp,
        lineHeight   = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily   = PoppinsFontFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 12.sp,
        lineHeight   = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily   = PoppinsFontFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 11.sp,
        lineHeight   = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)
