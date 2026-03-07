package com.example.maxx.presentation.screens.settings

import android.app.Activity
import android.widget.Toast

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProxySettingsScreen(
    navController: NavHostController,
    isDarkMode: Boolean,
    toggleTheme: () -> Unit,
    onResetLanguage: () -> Unit
) {
    var proxyAutofill by remember { mutableStateOf(true) }
    var logsEnabled by remember { mutableStateOf(false) }
    var bandwidthLimit by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surface
    val context = LocalContext.current
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.proxy_settings),
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
            Text(
                stringResource(R.string.basic),
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp),
                color = Color.Gray,
                fontSize = 13.sp
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    ,
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.bypass_list), fontWeight = FontWeight.Normal, fontSize = 16.sp)
                        Text(stringResource(R.string.bypass_list_description), fontSize = 13.sp, color = Color.Gray)
                    }
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.proxy_autofill), color = MaterialTheme.colorScheme.onBackground,fontWeight = FontWeight.Normal, fontSize = 16.sp)
                        Text(stringResource(R.string.proxy_autofill_description), fontSize = 13.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = proxyAutofill,
                        onCheckedChange = { proxyAutofill = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color.Black,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFE4E4E7),
                            uncheckedBorderColor = Color.Transparent
                        )

                    )

                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.advanced_settings),
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp),
                color = Color.Gray,
                fontSize = 13.sp
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.logs),color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Normal, fontSize = 16.sp)
                        Text(stringResource(R.string.logs_description), fontSize = 13.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = logsEnabled,
                        onCheckedChange = { logsEnabled = it },
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
                        .clickable {  }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.auto_delete_logs),color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Normal, fontSize = 16.sp)
                        Text(stringResource(R.string.hours_24), fontSize = 13.sp, color = Color.Gray)
                    }
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.bandwidth_limit),color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Normal, fontSize = 16.sp)
                        Text(stringResource(R.string.bandwidth_limit_description), fontSize = 13.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = bandwidthLimit,
                        onCheckedChange = { bandwidthLimit = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color.Black,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFE4E4E7),
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 13.dp)
                    .clickable { showResetDialog = true },
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.reset_app),
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        color=Color.Red
                    )
                    Text(
                        text = stringResource(R.string.reset_app_description),
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

        }
    }
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text("Reset Settings")
            },
            text = {
                Text("Are you sure you want to reset all settings to their default values?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        settingsViewModel.resetDarkModeToDefault()
                        onResetLanguage()
                        Toast.makeText(context, "Settings reset to default", Toast.LENGTH_SHORT).show()
                        showResetDialog = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
