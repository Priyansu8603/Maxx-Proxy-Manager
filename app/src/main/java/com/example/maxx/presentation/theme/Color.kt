package com.example.maxx.presentation.theme

import androidx.compose.ui.graphics.Color

// ─── Brand / Accent ───────────────────────────────────────────────────────────
val Brand400 = Color(0xFF7B96FF)   // dark-mode accent (lighter blue)
val Brand600 = Color(0xFF4F6EF7)   // light-mode accent (rich blue)

// ─── Neutral palette ─────────────────────────────────────────────────────────
// Light
val Neutral0   = Color(0xFFFFFFFF)
val Neutral50  = Color(0xFFF5F6F7)
val Neutral100 = Color(0xFFEEEEF4)
val Neutral200 = Color(0xFFDDDDEE)
val Neutral600 = Color(0xFF777777)
val Neutral900 = Color(0xFF1A1A2E)

// Dark
val Dark50  = Color(0xFF1A1A2E)   // background
val Dark100 = Color(0xFF1C1C2E)   // surface / card
val Dark200 = Color(0xFF2A2A3E)   // elevated surface (search-bar etc.)
val Dark300 = Color(0xFF2E2E44)   // chip / badge bg
val Dark400 = Color(0xFFAAAAAA)   // sub-text / placeholder
val Dark600 = Color(0xFF8888AA)   // muted label

// ─── Semantic status ─────────────────────────────────────────────────────────
val LatencyGood    = Color(0xFF4CAF50)   // < 100 ms
val LatencyMedium  = Color(0xFFFFC107)   // < 300 ms
val LatencyBad     = Color(0xFFF44336)   // ≥ 300 ms / error
val StatusTesting  = Color(0xFFFFC107)   // in-progress indicator

// ─── Legacy aliases kept for gradual migration ───────────────────────────────
// These match the old CustomColors so old screens don't break immediately.
// Remove once every screen is migrated.
val LightBackground = Neutral50
val LightCard       = Neutral0
val DarkBackground  = Dark50
val DarkCard        = Dark100
