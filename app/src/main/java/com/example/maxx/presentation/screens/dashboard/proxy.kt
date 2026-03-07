package com.example.maxx.presentation.screens.dashboard

data class Proxyprofile(
    val name: String,
    val ip: String,
    val port: Int,
    val protocol: String, // "HTTP", "SOCKS5", "HTTPS"
    val remarks: String = "",
    val isFavorite: Boolean = false
)

val proxyMap = mapOf(
    "Albania" to listOf(
        Proxyprofile(
            name = "US Public Proxy 1",
            ip = "51.158.68.133",  // HTTP Proxy
            port = 8811,
            protocol = "HTTP",
            remarks = "Free public proxy"
        ),
        Proxyprofile(
            name = "US Public Proxy 2",
            ip = "104.248.63.15",  // HTTP Proxy
            port = 30588,
            protocol = "HTTP",
            remarks = "DigitalOcean test"
        )
    ),
    "Germany" to listOf(
        Proxyprofile(
            name = "DE Public Proxy",
            ip = "116.203.28.62",
            port = 1080,
            protocol = "SOCKS5",
            remarks = "Socks proxy"
        )
    )
)
