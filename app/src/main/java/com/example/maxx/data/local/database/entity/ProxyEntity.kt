package com.example.maxx.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proxy_profiles")
data class ProxyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val ip: String,
    val port: Int,
    val protocol: String,
    val username: String? = null,
    val password: String? = null,
    val isFavorite: Boolean = false,
    val bandwidthLimitMB: Int? = null,
    val orderIndex: Int = 0,
    val remarks: String? = null,
    val country: String? = null,
    val countryCode: String? = null,
    val city: String? = null,
    val flagUrl: String? = null,
    val isp: String? = null
)
