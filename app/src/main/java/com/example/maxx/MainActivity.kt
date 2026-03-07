package com.example.maxx

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.maxx.data.local.preferences.SettingsDataStore
import com.example.maxx.navigation.graph.AppNavGraph
import com.example.maxx.presentation.theme.MaxxTheme
import com.example.maxx.presentation.viewmodel.SettingsViewModel
import com.example.maxx.presentation.viewmodel.setAppLocale
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    @Suppress("unused") // used via attachBaseContext — Hilt field injection
    lateinit var settingsDataStore: SettingsDataStore

    // ── Android 13+ notification permission ──────────────────────────────────
    private val notifPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d("NotifPermission", "Result: $granted")
            showToast(if (granted) "Notifications enabled" else "Notifications disabled")
        }

    // ── Apply saved language BEFORE Activity is attached ─────────────────────
    override fun attachBaseContext(newBase: Context) {
        val lang = runBlocking { SettingsDataStore(newBase).languageFlow.first() }
        super.attachBaseContext(setAppLocale(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()

        setContent {
            // Single ViewModel for all app-wide settings (theme + language)
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val isDarkMode      by settingsViewModel.darkMode.collectAsState()
            val shouldRecreate  by settingsViewModel.shouldRecreate.collectAsState()

            // Recreate Activity when language changes (needed for string resources)
            if (shouldRecreate) {
                LaunchedEffect(Unit) {
                    settingsViewModel.acknowledgeRecreate()
                    recreate()
                }
            }

            // MaxxTheme wraps everything — single source of truth for colours
            MaxxTheme(darkTheme = isDarkMode) {
                AppNavGraph(
                    isDarkMode      = isDarkMode,
                    toggleTheme     = { settingsViewModel.toggleDarkMode() },
                    onResetLanguage = { settingsViewModel.resetLanguageToDefault() },
                )
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun showToast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
