package com.example.maxx.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val DARK_MODE          = booleanPreferencesKey("dark_mode")
        val LANGUAGE           = stringPreferencesKey("language")
        val PROXY_AUTOFILL     = booleanPreferencesKey("proxy_autofill")
        val DEFAULT_PROXY_TYPE = stringPreferencesKey("default_proxy_type")
        val AUTO_TEST_ON_ADD   = booleanPreferencesKey("auto_test_on_add")
        // "System" | "Light" | "Dark"
        val APPEARANCE         = stringPreferencesKey("appearance")
    }

    // ── Dark Mode ─────────────────────────────────────────────────────────────
    val darkModeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.DARK_MODE] ?: false
    }
    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DARK_MODE] = enabled }
    }

    // ── Language ──────────────────────────────────────────────────────────────
    val languageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.LANGUAGE] ?: "en"
    }
    suspend fun setLanguage(code: String) {
        context.dataStore.edit { it[PreferencesKeys.LANGUAGE] = code }
    }

    // ── Proxy Autofill ────────────────────────────────────────────────────────
    val proxyAutofillFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.PROXY_AUTOFILL] ?: true
    }
    suspend fun setProxyAutofill(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.PROXY_AUTOFILL] = enabled }
    }

    // ── Default Proxy Type ────────────────────────────────────────────────────
    val defaultProxyTypeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.DEFAULT_PROXY_TYPE] ?: "SOCKS5"
    }
    suspend fun setDefaultProxyType(type: String) {
        context.dataStore.edit { it[PreferencesKeys.DEFAULT_PROXY_TYPE] = type }
    }

    // ── Auto-Test on Add ──────────────────────────────────────────────────────
    val autoTestOnAddFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.AUTO_TEST_ON_ADD] ?: true
    }
    suspend fun setAutoTestOnAdd(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AUTO_TEST_ON_ADD] = enabled }
    }

    // ── Appearance ────────────────────────────────────────────────────────────
    val appearanceFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.APPEARANCE] ?: "System"
    }
    suspend fun setAppearance(mode: String) {
        context.dataStore.edit { it[PreferencesKeys.APPEARANCE] = mode }
    }

    // ── Reset All ─────────────────────────────────────────────────────────────
    suspend fun resetAll() {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.DARK_MODE]          = false
            prefs[PreferencesKeys.LANGUAGE]           = "en"
            prefs[PreferencesKeys.PROXY_AUTOFILL]     = true
            prefs[PreferencesKeys.DEFAULT_PROXY_TYPE] = "SOCKS5"
            prefs[PreferencesKeys.AUTO_TEST_ON_ADD]   = true
            prefs[PreferencesKeys.APPEARANCE]         = "System"
        }
    }
}
