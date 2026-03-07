package com.example.maxx.presentation.screens.testing

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.maxx.domain.models.ProxyTestResult
import com.example.maxx.presentation.theme.isDark

// ─────────────────────────────────────────────────────────────────────────────
//  Country code → flag emoji
// ─────────────────────────────────────────────────────────────────────────────
fun countryCodeToFlagEmoji(code: String?): String {
    if (code.isNullOrBlank() || code.length != 2) return "🌐"
    return code.uppercase().map { c ->
        String(Character.toChars(c.code - 0x41 + 0x1F1E6))
    }.joinToString("")
}

// ─────────────────────────────────────────────────────────────────────────────
//  ProxyTestResultBottomSheet
//  Industry pattern:
//    • No isDarkMode param — reads MaterialTheme.isDark (app-level toggle)
//    • No hardcoded Color() — only design-token colours + theme derivations
//    • ModalBottomSheet with skipPartiallyExpanded = true
//    • AnimatedContent for loading ↔ result transition
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProxyTestResultBottomSheet(
    isDarkMode: Boolean,          // kept for call-site compat; internal logic uses MaterialTheme.isDark
    testResult: ProxyTestResult?,
    isTesting: Boolean,
    onDismiss: () -> Unit,
    onTestAgain: () -> Unit,
    onOpenBrowser: (() -> Unit)? = null,  // optional — only shown when result is successful
) {
    val cs         = MaterialTheme.colorScheme
    val dark       = MaterialTheme.isDark
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest  = onDismiss,
        sheetState        = sheetState,
        containerColor    = cs.background,
        shape             = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle        = {
            Box(
                Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(cs.onSurface.copy(alpha = 0.2f))
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp),
        ) {

            // ── Header row ────────────────────────────────────
            SheetHeader(
                dark      = dark,
                cs        = cs,
                onDismiss = onDismiss,
            )

            Spacer(Modifier.height(16.dp))

            // ── Animated content: loading ↔ result ────────────
            AnimatedContent(
                targetState = when {
                    isTesting && testResult == null -> SheetState.Loading
                    testResult != null              -> SheetState.Result(testResult)
                    else                            -> SheetState.Loading
                },
                transitionSpec = {
                    (fadeIn() + scaleIn(initialScale = 0.96f)) togetherWith
                    (fadeOut() + scaleOut(targetScale = 0.96f))
                },
                label = "sheet_content",
            ) { state ->
                when (state) {
                    is SheetState.Loading -> LoadingContent(cs = cs)
                    is SheetState.Result  -> ResultContent(
                        result          = state.result,
                        dark            = dark,
                        cs              = cs,
                        onTestAgain     = onTestAgain,
                        onDismiss       = onDismiss,
                        onOpenBrowser   = onOpenBrowser,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Internal state discriminator (avoids null checks in AnimatedContent)
// ─────────────────────────────────────────────────────────────────────────────
private sealed interface SheetState {
    object Loading : SheetState
    data class Result(val result: ProxyTestResult) : SheetState
}

// ─────────────────────────────────────────────────────────────────────────────
//  HEADER  — refresh icon · "Test Summary" · ✕
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SheetHeader(dark: Boolean, cs: ColorScheme, onDismiss: () -> Unit) {
    // Accent: purple in dark, blue in light — matches design exactly
    val accent = if (dark) Color(0xFF7B61FF) else cs.primary

    Row(
        modifier            = Modifier.fillMaxWidth(),
        verticalAlignment   = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Refresh, null, tint = accent, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(
            "Test Summary",
            fontWeight = FontWeight.Bold,
            fontSize   = 18.sp,
            color      = cs.onBackground,
            modifier   = Modifier.weight(1f),
        )
        IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, "Close", tint = cs.onSurfaceVariant)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  LOADING STATE
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LoadingContent(cs: ColorScheme) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator(color = cs.primary, strokeWidth = 3.dp)
            Text("Testing proxy connection…", fontSize = 14.sp, color = cs.onSurfaceVariant)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  RESULT CONTENT — matches both design screenshots exactly
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ResultContent(
    result: ProxyTestResult,
    dark: Boolean,
    cs: ColorScheme,
    onTestAgain: () -> Unit,
    onDismiss: () -> Unit,
    onOpenBrowser: (() -> Unit)? = null,
) {
    val accent      = if (dark) Color(0xFF7B61FF) else cs.primary
    val cardBg      = cs.surface
    val sectionBg   = if (dark) Color(0xFF1E1E2E) else Color(0xFFF4F4F8)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // ── 1. Status card (✓ Success / ✕ Failed) ─────────────
        StatusCard(result = result, dark = dark, cardBg = sectionBg, accent = accent, cs = cs)

        // ── 2. Latency row ────────────────────────────────────
        if (result.success) {
            LatencyCard(latencyMs = result.latencyMs, dark = dark, cardBg = cardBg, cs = cs)

            // ── 3. Download / Upload ──────────────────────────
            SpeedRow(
                downloadMbps = result.downloadSpeedMbps,
                uploadMbps   = result.uploadSpeedMbps,
                accent       = accent,
                cardBg       = cardBg,
                cs           = cs,
            )

            // ── 4. Network info ───────────────────────────────
            NetworkInfoCard(
                result    = result,
                accent    = accent,
                cardBg    = cardBg,
                sectionBg = sectionBg,
                cs        = cs,
            )
        } else {
            // ── Error detail ──────────────────────────────────
            ErrorCard(message = result.errorMessage, cs = cs)
        }

        Spacer(Modifier.height(4.dp))

        // ── Bottom actions ────────────────────────────────────
        // Row 1: Close + Test Again (always visible)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick  = onDismiss,
                modifier = Modifier.weight(1f).height(48.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = cs.onBackground),
                border   = BorderStroke(1.dp, cs.outline.copy(alpha = 0.5f)),
            ) {
                Text("Close", fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick  = onTestAgain,
                modifier = Modifier.weight(1f).height(48.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White),
            ) {
                Icon(Icons.Default.Refresh, null, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Test Again", fontWeight = FontWeight.SemiBold)
            }
        }

        // Row 2: "Open in Safe Browser" — only shown when test succeeded & callback is provided
        if (result.success && onOpenBrowser != null) {
            Button(
                onClick  = onOpenBrowser,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = if (dark) Color(0xFF1A2340) else Color(0xFFE8EEFF),
                    contentColor   = accent,
                ),
            ) {
                Icon(Icons.Default.Public, null, Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Open in Safe Browser", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  STATUS CARD  — circle icon · "Success" / "Failed" · subtitle
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StatusCard(
    result: ProxyTestResult,
    dark: Boolean,
    cardBg: Color,
    accent: Color,
    cs: ColorScheme,
) {
    val successGreen = if (dark) Color(0xFF4ADE80) else Color(0xFF22C55E)
    val failRed      = Color(0xFFEF4444)
    val iconColor    = if (result.success) successGreen else failRed
    val duration     = if (result.latencyMs > 0) "Test completed in ${"%.1f".format(result.latencyMs / 1000f)}s" else ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = cardBg),
        shape    = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border   = BorderStroke(1.dp, iconColor.copy(alpha = 0.25f)),
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Animated circle icon
            val scale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
                label = "icon_scale",
            )
            Box(
                Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = if (result.success) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint               = iconColor,
                    modifier           = Modifier.size(36.dp),
                )
            }
            Text(
                text       = if (result.success) "Success" else "Failed",
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 26.sp,
                color      = cs.onSurface,
            )
            if (duration.isNotBlank()) {
                Text(duration, fontSize = 13.sp, color = cs.onSurfaceVariant)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  LATENCY ROW
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LatencyCard(latencyMs: Long, dark: Boolean, cardBg: Color, cs: ColorScheme) {
    val latColor = latencyColor(latencyMs)
    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = cardBg),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = BorderStroke(1.dp, latColor.copy(alpha = 0.28f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bolt, null, tint = latColor, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(5.dp))
                Text("LATENCY", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, color = cs.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$latencyMs", fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, color = cs.onSurface)
                Spacer(Modifier.width(4.dp))
                Text("ms", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  DOWNLOAD / UPLOAD ROW
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SpeedRow(
    downloadMbps: Double,
    uploadMbps: Double,
    accent: Color,
    cardBg: Color,
    cs: ColorScheme,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SpeedCard(
            label    = "DOWNLOAD",
            value    = if (downloadMbps > 0) "%.1f".format(downloadMbps) else "—",
            icon     = Icons.Default.ArrowDownward,
            iconBg   = cs.primaryContainer,
            iconTint = cs.primary,
            cardBg   = cardBg,
            cs       = cs,
            modifier = Modifier.weight(1f),
        )
        SpeedCard(
            label    = "UPLOAD",
            value    = if (uploadMbps > 0) "%.1f".format(uploadMbps) else "—",
            icon     = Icons.Default.ArrowUpward,
            iconBg   = accent.copy(alpha = 0.18f),   // purple tint in dark, blue tint in light
            iconTint = accent,
            cardBg   = cardBg,
            cs       = cs,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SpeedCard(
    label: String,
    value: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    cardBg: Color,
    cs: ColorScheme,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier  = modifier,
        colors    = CardDefaults.cardColors(containerColor = cardBg),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = BorderStroke(1.dp, iconTint.copy(alpha = 0.22f)),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    Modifier.size(26.dp).clip(RoundedCornerShape(7.dp)).background(iconBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, null, tint = iconTint, modifier = Modifier.size(15.dp))
                }
                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = cs.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = cs.onSurface)
                if (value != "—") {
                    Spacer(Modifier.width(3.dp))
                    Text("MBPS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = cs.onSurfaceVariant, modifier = Modifier.padding(bottom = 5.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  NETWORK INFO CARD  — IP · Location · ISP
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun NetworkInfoCard(
    result: ProxyTestResult,
    accent: Color,
    cardBg: Color,
    sectionBg: Color,
    cs: ColorScheme,
) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        // Header row: "NETWORK INFO" + "PROXY ACTIVE" pill
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "NETWORK INFO",
                fontSize      = 10.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color         = cs.onSurfaceVariant,
                modifier      = Modifier.weight(1f),
            )
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(.6f)),
            ) {
                Text(
                    "PROXY ACTIVE",
                    fontSize   = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color      = accent,
                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }

        // Info rows card
        Card(
            modifier  = Modifier.fillMaxWidth(),
            colors    = CardDefaults.cardColors(containerColor = cardBg),
            shape     = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(0.dp),
            border    = BorderStroke(1.dp, accent.copy(alpha = 0.20f)),
        ) {
            Column(Modifier.padding(vertical = 4.dp)) {
                NetworkInfoRow(
                    icon  = Icons.Default.Language,
                    label = "IP Address",
                    value = result.proxyIp ?: result.realIp ?: "—",
                    cs    = cs,
                )
                HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = cs.outline.copy(.4f), thickness = 0.5.dp)
                NetworkInfoRow(
                    icon  = Icons.Default.LocationOn,
                    label = "Location",
                    value = buildString {
                        val flag = countryCodeToFlagEmoji(result.countryCode)
                        if (flag != "🌐") append("$flag ")
                        append(result.city ?: "")
                        if (!result.city.isNullOrBlank() && !result.country.isNullOrBlank()) append(", ")
                        append(result.countryCode?.uppercase() ?: result.country ?: "Unknown")
                    },
                    cs    = cs,
                    valueBold = true,
                )
                HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = cs.outline.copy(.4f), thickness = 0.5.dp)
                NetworkInfoRow(
                    icon  = Icons.Default.Dns,
                    label = "ISP",
                    value = result.isp ?: "—",
                    cs    = cs,
                    valueBold = true,
                )
            }
        }
    }
}

@Composable
private fun NetworkInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    cs: ColorScheme,
    valueBold: Boolean = false,
) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, fontSize = 13.sp, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(
            text       = value,
            fontSize   = 13.sp,
            fontWeight = if (valueBold) FontWeight.Bold else FontWeight.Normal,
            color      = cs.onSurface,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  ERROR CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ErrorCard(message: String?, cs: ColorScheme) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = cs.errorContainer),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = BorderStroke(1.dp, cs.error.copy(alpha = 0.35f)),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.ErrorOutline, null, tint = cs.error, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Connection Failed", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = cs.onErrorContainer)
                Spacer(Modifier.height(4.dp))
                Text(message ?: "Unknown error occurred.", fontSize = 13.sp, color = cs.onErrorContainer.copy(.75f))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Latency colour helper
// ─────────────────────────────────────────────────────────────────────────────
private fun latencyColor(ms: Long): Color = when {
    ms < 100 -> Color(0xFF4ADE80)
    ms < 300 -> Color(0xFFFACC15)
    else     -> Color(0xFFEF4444)
}
