package com.example.maxx.presentation.screens.logs

data class LogEntry(
    val id: Long = System.nanoTime(),
    val timestamp: Long,
    val level: LogLevel,
    val proxyType: String,
    val message: String,
    val details: String? = null
)

