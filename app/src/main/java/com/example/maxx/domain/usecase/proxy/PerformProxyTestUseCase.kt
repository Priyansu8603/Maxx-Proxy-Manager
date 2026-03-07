package com.example.maxx.domain.usecase.proxy

import android.util.Log
import com.example.maxx.domain.models.GeoIPResponse
import com.example.maxx.domain.models.ProxyTestResult
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PerformProxyTestUseCase @Inject constructor() {

    suspend operator fun invoke(
        proxyId: Int,
        proxyHost: String,
        proxyPort: Int,
        proxyType: String = "SOCKS5"
    ): ProxyTestResult = withContext(Dispatchers.IO) {
        try {
            // Get real IP first (without proxy)
            val realIp = getRealIp()

            // Test proxy connection and measure latency
            val startTime = System.currentTimeMillis()
            val proxy = createProxy(proxyHost, proxyPort, proxyType)
            val proxyIpInfo = getIpInfoThroughProxy(proxy)
            val latency = System.currentTimeMillis() - startTime

            // Check for IP leak
            val ipLeakDetected = realIp != null && proxyIpInfo?.query == realIp

            ProxyTestResult(
                proxyId = proxyId,
                success = true,
                latencyMs = latency,
                proxyIp = proxyIpInfo?.query,
                realIp = realIp,
                country = proxyIpInfo?.country,
                countryCode = proxyIpInfo?.countryCode,
                city = proxyIpInfo?.city,
                isp = proxyIpInfo?.isp,
                ipLeakDetected = ipLeakDetected,
                downloadSpeedMbps = 0.0, // TODO: Implement speed test
                uploadSpeedMbps = 0.0
            )
        } catch (e: Exception) {
            Log.e("ProxyTest", "Test failed for proxy $proxyId", e)
            ProxyTestResult(
                proxyId = proxyId,
                success = false,
                errorMessage = when (e) {
                    is SocketTimeoutException -> "Connection timed out"
                    is ConnectException -> "Connection refused"
                    is UnknownHostException -> "Unknown host"
                    is IOException -> "Network error: ${e.message}"
                    else -> "Test failed: ${e.message}"
                }
            )
        }
    }

    private fun createProxy(host: String, port: Int, type: String): Proxy {
        val proxyType = when (type.uppercase()) {
            "HTTP", "HTTPS" -> Proxy.Type.HTTP
            "SOCKS5", "SOCKS" -> Proxy.Type.SOCKS
            else -> Proxy.Type.SOCKS
        }
        return Proxy(proxyType, InetSocketAddress(host, port))
    }

    private suspend fun getRealIp(): String? = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url("https://api.ipify.org?format=json")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    Gson().fromJson(json, IpifyResponse::class.java).ip
                } else null
            }
        } catch (e: Exception) {
            Log.w("ProxyTest", "Failed to get real IP", e)
            null
        }
    }

    private suspend fun getIpInfoThroughProxy(proxy: Proxy): GeoIPResponse? = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .proxy(proxy)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url("http://ip-api.com/json/")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    Gson().fromJson(json, GeoIPResponse::class.java)
                } else null
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private data class IpifyResponse(val ip: String)
}

