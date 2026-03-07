package com.example.maxx.domain.usecase.proxy

import com.example.maxx.data.local.database.dao.ProxyDao
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val proxyDao: ProxyDao
) {
    suspend operator fun invoke(proxyId: Int, isFavorite: Boolean) {
        proxyDao.updateFavoriteStatus(proxyId, isFavorite)
    }
}

