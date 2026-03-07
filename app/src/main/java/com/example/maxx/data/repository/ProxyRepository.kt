package com.example.maxx.data.repository

import android.content.Context
import android.util.Log
import com.example.maxx.domain.models.ConnectionState
import com.example.maxx.domain.models.ProxyProfile
import com.example.maxx.domain.models.ProxyStats
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class ProxyRepository @Inject constructor( @ApplicationContext private val context: Context) {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _proxyStats = MutableStateFlow(ProxyStats())
    val proxyStats: StateFlow<ProxyStats> = _proxyStats.asStateFlow()

    private val _proxyLogs = MutableStateFlow<List<String>>(emptyList())
    val proxyLogs: StateFlow<List<String>> = _proxyLogs.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedProxy = MutableStateFlow<ProxyProfile?>(null)
    val selectedProxy: StateFlow<ProxyProfile?> = _selectedProxy.asStateFlow()

    // Proxy testing will be handled by TestingEngine (to be created in Phase 2)
    fun selectProxy(proxy: ProxyProfile) {
        _selectedProxy.value = proxy
        Log.d("ProxyRepository", "Proxy selected: ${proxy.name}")
    }

    fun deselectProxy() {
        _selectedProxy.value = null
        _connectionState.value = ConnectionState.Idle
        Log.d("ProxyRepository", "Proxy deselected")
    }

    fun updateConnectionState(state: ConnectionState) {
        _connectionState.value = state
    }

    fun updateStats(stats: ProxyStats) {
        _proxyStats.value = stats
    }

    fun addLog(message: String) {
        _proxyLogs.value = (_proxyLogs.value + message).takeLast(100)
    }

    fun clearLogs() {
        _proxyLogs.value = emptyList()
    }

    fun setError(error: String?) {
        _errorMessage.value = error
        if (error != null) {
            _connectionState.value = ConnectionState.Error(error)
        }
    }

}
