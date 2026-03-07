package com.example.maxx.domain.usecase.proxy

import com.example.maxx.data.local.database.dao.ProxyDao
import com.example.maxx.domain.mappers.ProxyMapper.toEntity
import com.example.maxx.domain.models.ProxyProfile
import javax.inject.Inject

class UpdateProxyUseCase @Inject constructor(
    private val proxyDao: ProxyDao
) {
    suspend operator fun invoke(profile: ProxyProfile) {
        proxyDao.update(profile.toEntity())
    }
}

