package com.example.maxx.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.maxx.navigation.routes.Screen
import com.example.maxx.presentation.theme.Brand400
import com.example.maxx.presentation.theme.Brand600
import com.example.maxx.presentation.theme.PoppinsFontFamily
import com.example.maxx.presentation.theme.isDark
import com.example.maxx.presentation.viewmodel.SettingsViewModel

// ─────────────────────────────────────────────────────────────────────────────
//  SettingsHomeScreen — all-in-one settings page matching design spec
//  Architecture: Presentation layer, MVVM, state hoisted to SettingsViewModel
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHomeScreen(
    navController: NavHostController,
    isDarkMode: Boolean,
    toggleTheme: () -> Unit,
) {
    val vm: SettingsViewModel = hiltViewModel()

    // ── Collect state ─────────────────────────────────────────────────────────
    val proxyAutofill     by vm.proxyAutofill.collectAsState()
    val defaultProxyType  by vm.defaultProxyType.collectAsState()
    val autoTestOnAdd     by vm.autoTestOnAdd.collectAsState()
    val language          by vm.language.collectAsState()
    val appearance        by vm.appearance.collectAsState()

    // ── Local dialog state ─────────────────────────────────────────────────────
    var showResetDialog       by remember { mutableStateOf(false) }
    var showProxyTypeDropdown by remember { mutableStateOf(false) }
    var showLanguageDropdown  by remember { mutableStateOf(false) }
    var showAppearanceDropdown by remember { mutableStateOf(false) }

    val isDark = MaterialTheme.isDark

    // ── Reset all confirmation dialog ─────────────────────────────────────────
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Reset All Settings?",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            text = {
                Text(
                    text = "This will restore all settings to their default values. This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.resetAll()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text("Reset", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showResetDialog = false },
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text("Cancel", fontFamily = PoppinsFontFamily)
                }
            },
        )
    }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(16.dp))

            // ── GET PRO BANNER ────────────────────────────────────────────────
            GetProBanner(
                isDark = isDark,
                onGetProClick = { navController.navigate(Screen.GetPro.route) },
            )

            Spacer(Modifier.height(20.dp))

            // ── PROXY CONFIGURATION SECTION ───────────────────────────────────
            SectionLabel(text = "PROXY CONFIGURATION")

            SettingsCard {
                // Proxy Autofill — Switch
                SettingsRowSwitch(
                    icon = Icons.Outlined.AutoFixHigh,
                    title = "Proxy Autofill",
                    subtitle = "Auto-detect from clipboard",
                    checked = proxyAutofill,
                    onCheckedChange = vm::setProxyAutofill,
                    isDark = isDark,
                )

                SettingsDivider()

                // Default Proxy Type — Inline Dropdown
                SettingsRowDropdown(
                    icon = Icons.Outlined.Code,
                    title = "Default Proxy Type",
                    subtitle = "Preferred protocol",
                    selectedValue = defaultProxyType,
                    expanded = showProxyTypeDropdown,
                    onExpandToggle = { showProxyTypeDropdown = !showProxyTypeDropdown },
                    options = listOf("HTTP", "SOCKS5"),
                    onOptionSelected = { type ->
                        vm.setDefaultProxyType(type)
                        showProxyTypeDropdown = false
                    },
                    isDark = isDark,
                )

                SettingsDivider()

                // Auto-test on Add — Switch
                SettingsRowSwitch(
                    icon = Icons.Outlined.Refresh,
                    title = "Auto-test on Add",
                    subtitle = "Verify connection immediately",
                    checked = autoTestOnAdd,
                    onCheckedChange = vm::setAutoTestOnAdd,
                    isDark = isDark,
                )

                SettingsDivider()

                // Import/Export
                SettingsRowArrow(
                    icon = Icons.Outlined.SwapVert,
                    title = "Import/Export Proxies",
                    subtitle = "Backup your configurations",
                    onClick = { /* TODO: implement */ },
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── APPLICATION SECTION ───────────────────────────────────────────
            SectionLabel(text = "APPLICATION")

            SettingsCard {
                // Language — Inline Dropdown
                SettingsRowDropdown(
                    icon = Icons.Outlined.Language,
                    title = "Language",
                    subtitle = "System default",
                    selectedValue = languageDisplayName(language),
                    expanded = showLanguageDropdown,
                    onExpandToggle = { showLanguageDropdown = !showLanguageDropdown },
                    options = listOf(
                        "English", "Hindi", "Russian", "Español",
                        "Chinese", "Vietnamese", "French", "German",
                    ),
                    onOptionSelected = { name ->
                        vm.setLanguage(languageCodeFromName(name))
                        showLanguageDropdown = false
                    },
                    isDark = isDark,
                )

                SettingsDivider()

                // Appearance — Inline Dropdown (System / Light / Dark)
                SettingsRowDropdown(
                    icon = Icons.Outlined.DarkMode,
                    title = "Appearance",
                    subtitle = "Dark/Light/Auto",
                    selectedValue = appearance,
                    expanded = showAppearanceDropdown,
                    onExpandToggle = { showAppearanceDropdown = !showAppearanceDropdown },
                    options = listOf("System", "Light", "Dark"),
                    onOptionSelected = { mode ->
                        vm.setAppearance(mode)
                        showAppearanceDropdown = false
                    },
                    isDark = isDark,
                )

                SettingsDivider()

                // Reset All Settings — danger button row
                ResetAllRow(onClick = { showResetDialog = true })
            }

            Spacer(Modifier.height(20.dp))

            // ── ABOUT PROXYGENZ SECTION ───────────────────────────────────────
            SectionLabel(text = "ABOUT PROXYGENZ")

            SettingsCard {
                // App Version — badge
                SettingsRowVersionBadge(
                    icon = Icons.Outlined.Info,
                    title = "App Version",
                    subtitle = "Build 2.4.0-release",
                    badge = "Latest",
                    isDark = isDark,
                )

                SettingsDivider()

                SettingsRowArrow(
                    icon = Icons.Outlined.Shield,
                    title = "Privacy Policy",
                    subtitle = "How we handle your data",
                    onClick = { /* TODO: Open URL */ },
                )

                SettingsDivider()

                SettingsRowArrow(
                    icon = Icons.Outlined.StarOutline,
                    title = "Rate Us",
                    subtitle = "Support the developers",
                    onClick = { /* TODO: Open Play Store */ },
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Get Pro Access Banner
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun GetProBanner(
    isDark: Boolean,
    onGetProClick: () -> Unit,
) {
    val gradientColors = if (isDark) {
        listOf(Color(0xFF2D1B6B), Color(0xFF1A1A3E))
    } else {
        listOf(Color(0xFFE8EEFF), Color(0xFFD8E0FF))
    }
    val accentColor = if (isDark) Brand400 else Brand600

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onGetProClick),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .background(Brush.horizontalGradient(gradientColors))
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon container
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.20f))
                        .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp),
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Get Pro Access",
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1A1A3E),
                    )
                    Text(
                        text = "Unlimited nodes & priority speed",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = PoppinsFontFamily,
                        color = accentColor.copy(alpha = 0.85f),
                    )
                }

                // PRO badge
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = accentColor,
                ) {
                    Text(
                        text = "PRO",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.8.sp,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Section Label (uppercase, small, muted)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        style = MaterialTheme.typography.labelSmall,
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.8.sp,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  Card wrapper
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
        ),
    ) {
        Column(content = content)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Thin divider between rows
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 56.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
        thickness = 0.5.dp,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  Row: icon + title + subtitle + Switch
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SettingsRowSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isDark: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIconBox(icon = icon, isDark = isDark)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = PoppinsFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = if (isDark) Brand400 else Brand600,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            ),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Row: icon + title + subtitle + Inline Dropdown
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SettingsRowDropdown(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selectedValue: String,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    isDark: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIconBox(icon = icon, isDark = isDark)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = PoppinsFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Compact dropdown chip
        Box {
            OutlinedButton(
                onClick = onExpandToggle,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Text(
                    text = selectedValue,
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onExpandToggle,
                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = if (option == selectedValue) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (option == selectedValue) {
                                    if (isDark) Brand400 else Brand600
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                        },
                        onClick = { onOptionSelected(option) },
                        leadingIcon = if (option == selectedValue) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (isDark) Brand400 else Brand600,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        } else null,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Row: icon + title + subtitle + Arrow (navigate or action)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SettingsRowArrow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val isDark = MaterialTheme.isDark
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIconBox(icon = icon, isDark = isDark)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = PoppinsFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Row: App Version with a "Latest" pill badge on the right
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SettingsRowVersionBadge(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String,
    isDark: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIconBox(icon = icon, isDark = isDark)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = PoppinsFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = androidx.compose.foundation.BorderStroke(
                0.5.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            ),
        ) {
            Text(
                text = badge,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Reset All Settings full-width danger button row
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ResetAllRow(onClick: () -> Unit) {
    val errorColor = MaterialTheme.colorScheme.error
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = errorColor.copy(alpha = 0.15f),
            contentColor = errorColor,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Reset All Settings",
            style = MaterialTheme.typography.labelLarge,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Icon box — rounded square with tinted background (like design)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SettingsIconBox(icon: ImageVector, isDark: Boolean) {
    val accentColor = if (isDark) Brand400 else Brand600
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(accentColor.copy(alpha = if (isDark) 0.15f else 0.10f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(18.dp),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Helpers
// ─────────────────────────────────────────────────────────────────────────────
private fun languageDisplayName(code: String): String = when (code) {
    "en"    -> "English"
    "hi"    -> "Hindi"
    "ru"    -> "Russian"
    "es"    -> "Español"
    "zh-CN" -> "Chinese"
    "vi"    -> "Vietnamese"
    "fr"    -> "French"
    "de"    -> "German"
    else    -> "English"
}

private fun languageCodeFromName(name: String): String = when (name) {
    "Hindi"      -> "hi"
    "Russian"    -> "ru"
    "Español"    -> "es"
    "Chinese"    -> "zh-CN"
    "Vietnamese" -> "vi"
    "French"     -> "fr"
    "German"     -> "de"
    else         -> "en"
}
