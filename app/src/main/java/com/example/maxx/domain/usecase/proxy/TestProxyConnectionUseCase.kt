package com.example.maxx.domain.usecase.proxy

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.*
import javax.inject.Inject

class TestProxyConnectionUseCase @Inject constructor() {

    sealed class ProxyTestResult {
        data class Success(val responseTimeMs: Long) : ProxyTestResult()
        data class Error(val message: String) : ProxyTestResult()
    }

    suspend operator fun invoke(
        proxyHost: String,
        proxyPort: Int,
        testUrl: String = "https://www.google.com"
    ): ProxyTestResult = withContext(Dispatchers.IO) {
        try {
            Log.d("ProxyTest", "Testing proxy: $proxyHost:$proxyPort -> $testUrl")
            val responseTime = testSocks5Proxy(proxyHost, proxyPort, testUrl)
            Log.d("ProxyTest", "Test successful. Response time: $responseTime ms")
            ProxyTestResult.Success(responseTime)
        } catch (e: Exception) {
            Log.e("ProxyTest", "Test failed", e)
            val errorMessage = when (e) {
                is MalformedURLException -> "Invalid URL: ${e.message}"
                is SocketTimeoutException -> "Connection timed out"
                is ConnectException -> "Connection refused"
                is UnknownHostException -> "Unknown host"
                is IOException -> "Network error: ${e.message}"
                else -> "Proxy test failed: ${e.message}"
            }
            ProxyTestResult.Error(errorMessage)
        }
    }

    private suspend fun testSocks5Proxy(
        proxyHost: String,
        proxyPort: Int,
        testUrl: String
    ): Long = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        val proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress(proxyHost, proxyPort))
        val url = URL(testUrl)

        val conn = url.openConnection(proxy) as HttpURLConnection
        try {
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.requestMethod = "HEAD"
            conn.connect()

            if (conn.responseCode !in 200..299) {
                throw IOException("HTTP Error: ${conn.responseCode}")
            }

            System.currentTimeMillis() - startTime
        } finally {
            conn.disconnect()
        }
    }
}

