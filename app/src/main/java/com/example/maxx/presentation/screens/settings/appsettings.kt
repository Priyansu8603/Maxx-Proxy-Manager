package com.example.maxx.presentation.screens.settings


import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.maxx.R
import com.example.maxx.presentation.viewmodel.SettingsViewModel
import com.example.maxx.presentation.viewmodel.setAppLocale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    navController: NavHostController,
    isDarkMode: Boolean,
    toggleTheme: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val settingsViewModel: SettingsViewModel = hiltViewModel()

    val darkModeEnabled by settingsViewModel.darkMode.collectAsState()
    val selectedLanguageCode by settingsViewModel.language.collectAsState()
    val darkTheme: Boolean = darkModeEnabled
    Switch(
        checked = darkModeEnabled,
        onCheckedChange = {
            settingsViewModel.toggleDarkMode()
        },
        colors = SwitchDefaults.colors(
            checkedThumbColor = if (darkTheme) Color.White else Color.Black,
            uncheckedThumbColor = Color.Gray,
            checkedTrackColor = if (darkTheme) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.3f),
            uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
        )
    )

    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }

    val languageOptions = mapOf(
        "English" to "en",
        "Hindi" to "hi",
        "Russian" to "ru",
        "Español" to "es",
        "Chinese" to "zh-CN",
        "Vietnamese" to "vi",
        "French" to "fr",
        "German" to "de"
    )

    val selectedLanguageLabel = languageOptions.entries
        .firstOrNull { it.value == selectedLanguageCode }?.key ?: "English"

    val backgroundColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surface

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_settings),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = cardColor
                )
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                ,
                colors = CardDefaults.cardColors(containerColor = cardColor),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLanguageDialog = true }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.more_language),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = selectedLanguageLabel,
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Change language",
                        tint = Color.Gray
                    )
                }

                // Dark Mode Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.more_dark_mode),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = stringResource(R.string.more_enable_dark_mode),
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = darkModeEnabled,
                        onCheckedChange = {
                            settingsViewModel.toggleDarkMode()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color.Black,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFE4E4E7),
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.more_app_version),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = stringResource(R.string.more_version_number),
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            if (showLanguageDialog) {
                AlertDialog(
                    onDismissRequest = { showLanguageDialog = false },
                    confirmButton = {},
                    title = {
                        Text(
                            text = stringResource(R.string.select_language),
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    text = {
                        Column {
                            languageOptions.forEach { (label, code) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            settingsViewModel.setLanguage(code)
                                            setAppLocale(context, code)
                                            activity?.recreate()
                                            showLanguageDialog = false
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 15.sp,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
