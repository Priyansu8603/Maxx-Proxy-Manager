package com.example.maxx.domain.models

sealed class ConnectionState {
    object Idle : ConnectionState()
    object Connecting : ConnectionState()
    data class Connected(val proxyName: String) : ConnectionState()
    data class Disconnecting(val proxyName: String) : ConnectionState()
    object Disconnected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}
