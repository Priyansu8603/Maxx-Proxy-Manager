package com.example.maxx.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maxx.data.local.preferences.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    // ── Dark Mode ─────────────────────────────────────────────────────────────
    private val _darkMode = MutableStateFlow(false)
    val darkMode: StateFlow<Boolean> = _darkMode

    // ── Language ──────────────────────────────────────────────────────────────
    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language

    // ── Proxy Autofill ────────────────────────────────────────────────────────
    private val _proxyAutofill = MutableStateFlow(true)
    val proxyAutofill: StateFlow<Boolean> = _proxyAutofill

    // ── Default Proxy Type ────────────────────────────────────────────────────
    private val _defaultProxyType = MutableStateFlow("SOCKS5")
    val defaultProxyType: StateFlow<String> = _defaultProxyType

    // ── Auto-Test on Add ──────────────────────────────────────────────────────
    private val _autoTestOnAdd = MutableStateFlow(true)
    val autoTestOnAdd: StateFlow<Boolean> = _autoTestOnAdd

    // ── Appearance ("System" | "Light" | "Dark") ───────────────────────────
    private val _appearance = MutableStateFlow("System")
    val appearance: StateFlow<String> = _appearance

    // ── Activity-recreate trigger ─────────────────────────────────────────────
    private val _shouldRecreate = MutableStateFlow(false)
    val shouldRecreate: StateFlow<Boolean> = _shouldRecreate

    init {
        settingsDataStore.darkModeFlow
            .onEach { _darkMode.value = it }
            .launchIn(viewModelScope)

        settingsDataStore.languageFlow
            .onEach { _language.value = it }
            .launchIn(viewModelScope)

        settingsDataStore.proxyAutofillFlow
            .onEach { _proxyAutofill.value = it }
            .launchIn(viewModelScope)

        settingsDataStore.defaultProxyTypeFlow
            .onEach { _defaultProxyType.value = it }
            .launchIn(viewModelScope)

        settingsDataStore.autoTestOnAddFlow
            .onEach { _autoTestOnAdd.value = it }
            .launchIn(viewModelScope)

        settingsDataStore.appearanceFlow
            .onEach { _appearance.value = it }
            .launchIn(viewModelScope)
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    fun toggleDarkMode() {
        viewModelScope.launch { settingsDataStore.setDarkMode(!_darkMode.value) }
    }

    fun resetDarkModeToDefault() {
        viewModelScope.launch { settingsDataStore.setDarkMode(false) }
    }

    fun setLanguage(code: String) {
        viewModelScope.launch {
            settingsDataStore.setLanguage(code)
            _shouldRecreate.value = true
        }
    }

    fun resetLanguageToDefault() {
        viewModelScope.launch {
            settingsDataStore.setLanguage("en")
            _shouldRecreate.value = true
        }
    }

    fun setProxyAutofill(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setProxyAutofill(enabled) }
    }

    fun setDefaultProxyType(type: String) {
        viewModelScope.launch { settingsDataStore.setDefaultProxyType(type) }
    }

    fun setAutoTestOnAdd(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setAutoTestOnAdd(enabled) }
    }

    fun setAppearance(mode: String) {
        viewModelScope.launch {
            settingsDataStore.setAppearance(mode)
            // Map appearance to darkMode and persist
            val dark = when (mode) {
                "Dark"  -> true
                "Light" -> false
                else    -> false   // "System" defaults to light; real logic lives in MainActivity
            }
            settingsDataStore.setDarkMode(dark)
            _shouldRecreate.value = true
        }
    }

    fun resetAll() {
        viewModelScope.launch {
            settingsDataStore.resetAll()
            _shouldRecreate.value = true
        }
    }

    fun acknowledgeRecreate() {
        _shouldRecreate.value = false
    }
}
