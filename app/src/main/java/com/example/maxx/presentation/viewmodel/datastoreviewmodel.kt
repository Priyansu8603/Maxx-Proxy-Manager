package com.example.maxx.presentation.viewmodel



import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.maxx.data.local.preferences.SettingsDataStore

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class DarkModeViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = SettingsDataStore(application)

    private val _darkMode = MutableStateFlow(false)
    val darkMode: StateFlow<Boolean> = _darkMode

    init {
        dataStore.darkModeFlow
            .onEach { _darkMode.value = it }
            .launchIn(viewModelScope)
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            dataStore.setDarkMode(!_darkMode.value)
        }
    }
    fun resetDarkModeToDefault() {
        viewModelScope.launch {
            dataStore.setDarkMode(false) // Default = light mode
        }
    }
}
