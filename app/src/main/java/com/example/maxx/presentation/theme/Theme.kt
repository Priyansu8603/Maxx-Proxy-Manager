package com.example.maxx.presentation.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
// ─────────────────────────────────────────────────────────────────────────────
//  Colour schemes (reference only — MaxxTheme in Themes.kt is the real entry point)
// ─────────────────────────────────────────────────────────────────────────────
private val LightColors = lightColorScheme(
    primary       = Color(0xFF6200EE),
    secondary     = Color(0xFF03DAC6),
    background    = Color(0xFFFFFFFF),
    surface       = Color(0xFFFFFFFF),
    onPrimary     = Color.White,
    onSecondary   = Color.Black,
    onBackground  = Color.Black,
    onSurface     = Color.Black,
)
private val DarkColors = darkColorScheme(
    primary       = Color(0xFFBB86FC),
    secondary     = Color(0xFF03DAC6),
    background    = Color(0xFF121212),
    surface       = Color(0xFF121212),
    onPrimary     = Color.Black,
    onSecondary   = Color.Black,
    onBackground  = Color.White,
    onSurface     = Color.White,
)
// ─────────────────────────────────────────────────────────────────────────────
//  Legacy theme — NOT used by the app. The real entry point is MaxxTheme (Themes.kt).
//
//  WHY THE BUG HAPPENED:
//    `typography = Typography` resolved to kotlin.text.Typography (a built-in Kotlin
//    object) instead of androidx.compose.material3.Typography, causing a type-mismatch
//    compile error.
//
//  FIX:
//    Use AppTypography which is explicitly declared as
//    androidx.compose.material3.Typography in Type.kt.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = AppTypography,  // ← AppTypography is material3.Typography (Type.kt)
        shapes      = Shapes(),
        content     = content
    )
}
