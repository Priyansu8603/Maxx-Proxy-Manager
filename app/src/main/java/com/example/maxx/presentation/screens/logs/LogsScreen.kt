package com.example.maxx.presentation.screens.logs

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.maxx.presentation.theme.PoppinsFontFamily
import java.text.SimpleDateFormat
import java.util.*
// =============================================================================
//  Screen
// =============================================================================
@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    navController: NavHostController,
    isDarkMode: Boolean,
) {
    var selectedFilter by remember { mutableStateOf(LogLevel.ALL) }
    var showClearDialog by remember { mutableStateOf(false) }
    val logs = remember {
        listOf(
            LogEntry(
                timestamp = logParseTime("14:22:05"),
                level     = LogLevel.SUCCESS,
                proxyType = "SOCKS5",
                message   = "Proxy connection established: US-East-1",
                details   = "Host: 198.51.100.42:1080\nLatency: 87ms\nCountry: United States"
            ),
            LogEntry(
                timestamp = logParseTime("14:20:12"),
                level     = LogLevel.ERROR,
                proxyType = "HTTP",
                message   = "Authentication failed for \"Private-Tokyo\"",
                details   = "Host: 203.0.113.55:8080\nError: 407 Proxy Authentication Required\nCheck credentials and retry."
            ),
            LogEntry(
                timestamp = logParseTime("14:18:45"),
                level     = LogLevel.WARNING,
                proxyType = "HTTPS",
                message   = "Latency spike detected on \"German-Tunn\"",
                details   = "Latency: 924ms\nThreshold: 500ms\nConsider switching to a closer server."
            ),
            LogEntry(
                timestamp = logParseTime("14:15:30"),
                level     = LogLevel.INFO,
                proxyType = "AUTO",
                message   = "Configuration updated via clipboard",
                details   = "Source: Clipboard\nProxy: socks5://user@192.0.2.1:1080\nApplied: true"
            ),
            LogEntry(
                timestamp = logParseTime("14:10:02"),
                level     = LogLevel.ERROR,
                proxyType = "SOCKS4",
                message   = "Socket timeout: Remote server unreachable",
                details   = "Host: 172.16.0.99:443\nTimeout: 10 000 ms\nAll retries exhausted."
            )
        )
    }
    val filteredLogs = remember(selectedFilter, logs) {
        if (selectedFilter == LogLevel.ALL) logs
        else logs.filter { it.level == selectedFilter }
    }
    val errorCount   = remember(logs) { logs.count { it.level == LogLevel.ERROR } }
    val warningCount = remember(logs) { logs.count { it.level == LogLevel.WARNING } }
    val successCount = remember(logs) { logs.count { it.level == LogLevel.SUCCESS } }
    val infoCount    = remember(logs) { logs.count { it.level == LogLevel.INFO } }

    val levelCounts = remember(logs) {
        mapOf(
            LogLevel.SUCCESS to successCount,
            LogLevel.ERROR   to errorCount,
            LogLevel.WARNING to warningCount,
            LogLevel.INFO    to infoCount,
        )
    }
    Scaffold(
        topBar = {
            LogsTopBar(onClearClick = { showClearDialog = true })
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Divider immediately below the TopAppBar — chips sit between this and the one below
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            LogFilterRow(
                selected = selectedFilter,
                counts   = levelCounts,
                onSelect = { selectedFilter = it },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            if (filteredLogs.isEmpty()) {
                LogsEmptyState(filter = selectedFilter)
            } else {
                LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item { LogSectionHeader(count = filteredLogs.size) }
                    items(items = filteredLogs, key = { it.id }) { log ->
                        LogItemCard(log = log)
                    }
                    item { LogsSystemMonitoringFooter() }
                }
            }
        }
    }
    if (showClearDialog) {
        LogsClearDialog(
            onConfirm = { showClearDialog = false },
            onDismiss = { showClearDialog = false },
        )
    }
}
// =============================================================================
//  Top bar
// =============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogsTopBar(onClearClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text       = "Logs",
                fontWeight = FontWeight.Bold,
                fontSize   = 20.sp,
                color      = MaterialTheme.colorScheme.onBackground,
            )
        },
        actions = {
            IconButton(onClick = onClearClick) {
                Icon(
                    imageVector        = Icons.Default.Delete,
                    contentDescription = "Clear All Logs",
                    tint               = Color(0xFFE53935),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor       = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background, // no elevation tint on scroll
        ),
        scrollBehavior = null,  // no automatic shadow/elevation on scroll
    )
}
// =============================================================================
//  Filter chips row  — ALL 5 levels, horizontally scrollable
// =============================================================================
@Composable
private fun LogFilterRow(
    selected: LogLevel,
    counts: Map<LogLevel, Int>,
    onSelect: (LogLevel) -> Unit,
) {
    val totalCount = counts.values.sum()
    val chips = listOf(
        Triple(LogLevel.ALL,     "ALL",      totalCount),
        Triple(LogLevel.SUCCESS, "SUCCESS",  counts[LogLevel.SUCCESS] ?: 0),
        Triple(LogLevel.ERROR,   "ERRORS",   counts[LogLevel.ERROR]   ?: 0),
        Triple(LogLevel.WARNING, "WARNINGS", counts[LogLevel.WARNING] ?: 0),
        Triple(LogLevel.INFO,    "INFO",     counts[LogLevel.INFO]    ?: 0),
    )

    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        chips.forEach { (level, label, count) ->
            LogFilterChip(
                label    = label,
                count    = count,
                selected = selected == level,
                onClick  = { onSelect(level) },
            )
        }
    }
}

@Composable
private fun LogFilterChip(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor        = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val textColor      = if (selected) MaterialTheme.colorScheme.onPrimary
                         else MaterialTheme.colorScheme.onSurfaceVariant
    val strokeColor    = if (selected) Color.Transparent
                         else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    val badgeBg        = if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
                         else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
    val badgeTextColor = if (selected) MaterialTheme.colorScheme.onPrimary
                         else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = strokeColor,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable { onClick() }
            .padding(horizontal = 11.dp, vertical = 7.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text       = label,
            fontSize   = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color      = textColor,
            fontFamily = PoppinsFontFamily,
        )
        // Circular count badge
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(50))
                .background(badgeBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text       = count.toString(),
                fontWeight = FontWeight.Bold,
                color      = badgeTextColor,
                fontFamily = PoppinsFontFamily,
                style      = MaterialTheme.typography.labelSmall.copy(
                    fontSize     = 10.sp,
                    lineHeight   = 10.sp,   // match fontSize to eliminate extra line spacing
                    platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                        includeFontPadding = false  // strip internal font ascent/descent padding
                    ),
                ),
            )
        }
    }
}
// =============================================================================
//  Section header
// =============================================================================
@Composable
private fun LogSectionHeader(count: Int) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(2.dp),
                    )
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text          = "RECENT ACTIVITY",
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color         = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text          = "SHOWING $count EVENTS",
            fontSize      = 11.sp,
            fontWeight    = FontWeight.Medium,
            letterSpacing = 0.5.sp,
            color         = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
// =============================================================================
//  Log item card
// =============================================================================
@Composable
private fun LogItemCard(log: LogEntry) {
    var expanded by remember { mutableStateOf(false) }

    val accentColor   = log.level.displayColor
    val isHighlighted = log.level == LogLevel.ERROR

    // Use Surface instead of Card so we can fully control shape/border with zero shadow
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness    = Spring.StiffnessMedium,
                )
            )
            .border(
                width = if (isHighlighted) 1.5.dp else 0.8.dp,
                color = if (isHighlighted) accentColor.copy(alpha = 0.55f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
            ),
        shape  = RoundedCornerShape(12.dp),
        color  = MaterialTheme.colorScheme.surface,
        // Zero shadow — clean flat card boundaries
        shadowElevation = 0.dp,
        tonalElevation  = 0.dp,
    ) {
        // height(IntrinsicSize.Min) makes fillMaxHeight() on the accent bar work
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            // Left accent bar — fillMaxHeight stretches it to exact card height
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        color = accentColor,
                        shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
            ) {
                // Header row
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier          = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector        = log.level.icon,
                            contentDescription = null,
                            tint               = accentColor,
                            modifier           = Modifier.size(15.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text       = logFormatTimestamp(log.timestamp),
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text       = " \u2022 ${log.proxyType}",
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    LogLevelBadge(level = log.level)
                }

                Spacer(Modifier.height(6.dp))

                // Message
                Text(
                    text       = log.message,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(8.dp))

                // View Diagnostics toggle
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text       = "View Diagnostics",
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color      = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector        = if (expanded) Icons.Default.KeyboardArrowUp
                                             else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(16.dp),
                    )
                }

                // Expandable diagnostics — plain if(), animateContentSize on the Surface handles the animation
                if (expanded) {
                    Column {
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text       = log.details ?: "No additional details available.",
                            fontSize   = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp,
                            modifier   = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                        .copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(6.dp),
                                )
                                .padding(10.dp),
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}
// =============================================================================
//  Level badge
// =============================================================================
@Composable
private fun LogLevelBadge(level: LogLevel) {
    if (level == LogLevel.ALL) return
    val (bg, text) = when (level) {
        LogLevel.SUCCESS -> Color(0xFF4CAF50).copy(alpha = 0.15f) to Color(0xFF4CAF50)
        LogLevel.ERROR   -> Color(0xFFE53935).copy(alpha = 0.15f) to Color(0xFFE53935)
        LogLevel.WARNING -> Color(0xFFF57C00).copy(alpha = 0.15f) to Color(0xFFF57C00)
        LogLevel.INFO    -> Color(0xFF1E88E5).copy(alpha = 0.15f) to Color(0xFF1E88E5)
        else             -> Color.Transparent to Color.Transparent
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text          = level.name,
            fontSize      = 10.sp,
            fontWeight    = FontWeight.Bold,
            color         = text,
            letterSpacing = 0.8.sp,
        )
    }
}
// =============================================================================
//  System Monitoring footer
// =============================================================================
@Composable
private fun LogsSystemMonitoringFooter() {
    Spacer(Modifier.height(8.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
            )
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text          = "SYSTEM MONITORING ACTIVE",
            fontSize      = 11.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color         = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
// =============================================================================
//  Empty state
// =============================================================================
@Composable
private fun LogsEmptyState(filter: LogLevel) {
    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector        = filter.icon,
                contentDescription = null,
                modifier           = Modifier.size(64.dp),
                tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text       = if (filter == LogLevel.ALL) "No logs available" else "No ${filter.name.lowercase()} logs",
                fontSize   = 16.sp,
                fontWeight = FontWeight.Medium,
                color      = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text     = "Logs will appear here as you use the app",
                fontSize = 13.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        }
    }
}
// =============================================================================
//  Clear logs dialog
// =============================================================================
@Composable
private fun LogsClearDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector        = Icons.Default.Delete,
                contentDescription = null,
                tint               = Color(0xFFE53935),
            )
        },
        title            = { Text("Clear All Logs") },
        text             = { Text("Are you sure you want to delete all logs? This action cannot be undone.") },
        confirmButton    = {
            TextButton(onClick = onConfirm) {
                Text("Clear", color = Color(0xFFE53935), fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton    = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
// =============================================================================
//  Helpers
// =============================================================================
private fun logParseTime(hhmmss: String): Long {
    return try {
        val sdf    = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val today  = Calendar.getInstance()
        val parsed = Calendar.getInstance().apply {
            time = sdf.parse(hhmmss) ?: return@apply
            set(Calendar.YEAR,        today.get(Calendar.YEAR))
            set(Calendar.MONTH,       today.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH))
        }
        parsed.timeInMillis
    } catch (_: Exception) {
        System.currentTimeMillis()
    }
}
private fun logFormatTimestamp(timestamp: Long): String =
    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))