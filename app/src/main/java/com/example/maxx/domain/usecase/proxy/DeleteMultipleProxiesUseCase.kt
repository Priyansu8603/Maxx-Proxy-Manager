package com.example.maxx.domain.usecase.proxy

import com.example.maxx.data.local.database.dao.ProxyDao
import javax.inject.Inject

class DeleteMultipleProxiesUseCase @Inject constructor(
    private val proxyDao: ProxyDao
) {
    suspend operator fun invoke(ids: List<Int>) {
        proxyDao.deleteMultiple(ids)
    }
}

