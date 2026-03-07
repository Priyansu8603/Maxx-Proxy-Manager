package com.example.maxx.domain.models

data class GeoIPResponse(
    val query: String?,      // IP address returned by ip-api.com
    val country: String?,
    val countryCode: String?,
    val city: String?,
    val isp: String?
)
