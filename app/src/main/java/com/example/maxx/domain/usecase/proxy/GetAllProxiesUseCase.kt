package com.example.maxx.domain.usecase.proxy

import com.example.maxx.data.local.database.dao.ProxyDao
import com.example.maxx.domain.mappers.ProxyMapper.toDomain
import com.example.maxx.domain.models.ProxyProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAllProxiesUseCase @Inject constructor(
    private val proxyDao: ProxyDao
) {
    operator fun invoke(): Flow<List<ProxyProfile>> {
        return proxyDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}

