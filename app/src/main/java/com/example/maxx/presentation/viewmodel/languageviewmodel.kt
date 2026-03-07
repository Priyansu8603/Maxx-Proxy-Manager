package com.example.maxx.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.maxx.data.local.preferences.SettingsDataStore
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Locale

class LanguageViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val dataStore = SettingsDataStore(application)

    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language

    private val _shouldRecreate = MutableStateFlow(false)
    val shouldRecreate: StateFlow<Boolean> = _shouldRecreate

    init {
        dataStore.languageFlow
            .onEach { _language.value = it }
            .launchIn(viewModelScope)
    }

    fun setLanguage(code: String) {
        viewModelScope.launch {
            dataStore.setLanguage(code)
            _shouldRecreate.value = true
        }
    }

    fun resetLanguageToDefault() {
        viewModelScope.launch {
            dataStore.setLanguage("en")
            _shouldRecreate.value = true
        }
    }

    fun acknowledgeRecreate() {
        _shouldRecreate.value = false
    }
}

fun setAppLocale(context: Context, languageCode: String): Context {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)

    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)

    return context.createConfigurationContext(config)
}
