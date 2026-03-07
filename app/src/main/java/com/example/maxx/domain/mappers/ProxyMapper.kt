package com.example.maxx.domain.mappers

import com.example.maxx.data.local.database.entity.ProxyEntity
import com.example.maxx.domain.models.ProxyProfile

object ProxyMapper {

    fun ProxyEntity.toDomain(): ProxyProfile {
        return ProxyProfile(
            id = id,
            name = name,
            ip = ip,
            port = port,
            protocol = protocol,
            username = username,
            password = password,
            isFavorite = isFavorite,
            bandwidthLimitMB = bandwidthLimitMB,
            orderIndex = orderIndex,
            remarks = remarks,
            country = country,
            countryCode = countryCode,
            city = city,
            flagUrl = flagUrl,
            isp = isp
        )
    }

    fun ProxyProfile.toEntity(): ProxyEntity {
        return ProxyEntity(
            id = id,
            name = name,
            ip = ip,
            port = port,
            protocol = protocol,
            username = username,
            password = password,
            isFavorite = isFavorite,
            bandwidthLimitMB = bandwidthLimitMB,
            orderIndex = orderIndex,
            remarks = remarks,
            country = country,
            countryCode = countryCode,
            city = city,
            flagUrl = flagUrl,
            isp = isp
        )
    }

    fun List<ProxyEntity>.toDomainList(): List<ProxyProfile> {
        return map { it.toDomain() }
    }

    fun List<ProxyProfile>.toEntityList(): List<ProxyEntity> {
        return map { it.toEntity() }
    }
}
