package com.example.maxx.domain.models

data class ProxyTestResult(
    val proxyId: Int,
    val success: Boolean,
    val latencyMs: Long = 0,
    val downloadSpeedMbps: Double = 0.0,
    val uploadSpeedMbps: Double = 0.0,
    val proxyIp: String? = null,
    val realIp: String? = null,
    val country: String? = null,
    val countryCode: String? = null,
    val city: String? = null,
    val isp: String? = null,
    val ipLeakDetected: Boolean = false,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

