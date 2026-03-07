package com.example.maxx.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.maxx.R

@Composable
fun settingsmainscreen(
    navController: NavHostController,
    isDarkMode: Boolean,
    toggleTheme: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surface

    Scaffold(
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            Text(
                text = stringResource(R.string.settings),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 32.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                SettingsItem(title = stringResource(R.string.app_settings), description = stringResource(R.string.app_settings_description)) {
                    navController.navigate("AppSettingsScreen")
                }
                SettingsItem(title = stringResource(R.string.proxy_settings), description = stringResource(R.string.bypass_list_description)) {
                    navController.navigate("ProxySettingsScreen")
                }
                SettingsItem(title = stringResource(R.string.get_pro), description = stringResource(R.string.unlock_pro)) {
                    navController.navigate("getpro")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                SettingsItem1(title = stringResource(R.string.rate_on_playstore), description = stringResource(R.string.rate_description)) {}
                SettingsItem1(title = stringResource(R.string.share_with_friends), description = stringResource(R.string.share_description)) {}
            }
        }
    }
}


@Composable
fun SettingsItem(title: String, description: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp,color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Normal)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = description, fontSize = 13.sp, color = Color.Gray)
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}
@Composable
fun SettingsItem1(title: String, description: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp,color = MaterialTheme.colorScheme.onBackground,  fontWeight = FontWeight.Normal)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = description, fontSize = 13.sp, color = Color.Gray)
        }
    }
}