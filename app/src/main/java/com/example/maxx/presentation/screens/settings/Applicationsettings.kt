package com.example.maxx.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController, isDarkMode: Boolean, toggleTheme: () -> Unit) {
    var darkMode by remember { mutableStateOf(false) }
    var proxyAutofill by remember { mutableStateOf(true) }
    var logsEnabled by remember { mutableStateOf(false) }
    var bandwidthLimit by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("English") }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var selectedAutoDeleteOption by remember { mutableStateOf("1 week") }
    var showAutoDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )

            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text("App Settings", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            SettingRowWithArrow("Language", selectedLanguage) {
                showLanguageDialog = true
            }
            SettingRowWithSwitch("Dark Mode", "ON/OFF for dark theme", isDarkMode) {
                toggleTheme()
            }
            SettingRowWithArrow("Get Pro", "Upgrade to access premium features") {
            }

            SettingRowWithArrow("Rate on Play Store", "Leave a review on the Play Store") {
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Proxy Settings", color = Color(0xFF1976D2), style = MaterialTheme.typography.labelLarge)
            SettingRowWithArrow("Bypass List", "Edit apps that bypass proxy") { }
            SettingRowWithSwitch("Proxy Autofill", "Default: ON", proxyAutofill) { proxyAutofill = it }
            SettingRowWithSwitch("Logs", "Default: OFF", logsEnabled) { logsEnabled = it }
            SettingRowWithArrow("Auto Delete Logs", selectedAutoDeleteOption) {
                showAutoDeleteDialog = true
            }
            SettingRowWithSwitch("Bandwidth Limit", "Default: OFF", bandwidthLimit) { bandwidthLimit = it }

            Spacer(modifier = Modifier.height(32.dp))
        }
        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                confirmButton = {},
                title = { Text("Choose Language") },
                text = {
                    Column {
                        val languages = listOf(
                            "English", "Russian", "Español", "Chinese",
                            "Vietnamese", "French", "German"
                        )
                        languages.forEach { language ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedLanguage = language
                                        showLanguageDialog = false
                                    }
                                    .padding(8.dp)
                            ) {
                                Text(text = language)
                            }
                        }
                    }
                }
            )
        }
        if (showAutoDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showAutoDeleteDialog = false },
                title = { Text("Auto Delete Logs") },
                text = {
                    Column {
                        val deleteOptions = listOf("Never", "1 day", "2 days", "3 days", "4 days", "1 week", "2 weeks")
                        deleteOptions.forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedAutoDeleteOption = option
                                        showAutoDeleteDialog = false
                                    }
                                    .padding(8.dp)
                            ) {
                                Text(option)
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }


    }
}
@Composable
fun SettingRowWithArrow(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun SettingRowWithSwitch(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun SettingRowButton(title: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun SettingRowButtonWithIcon(title: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Icon(Icons.Default.Star, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge)
    }
}
