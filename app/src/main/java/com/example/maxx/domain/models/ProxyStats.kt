package com.example.maxx.domain.models

data class ProxyStats(
    val uploadedBytes: Long = 0,
    val downloadedBytes: Long = 0,
    val latencyMs: Long = 0
)
