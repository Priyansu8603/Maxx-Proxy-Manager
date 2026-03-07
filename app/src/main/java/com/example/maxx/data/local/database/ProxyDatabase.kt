package com.example.maxx.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.maxx.data.local.database.dao.ProxyDao
import com.example.maxx.data.local.database.entity.ProxyEntity

@Database(entities = [ProxyEntity::class], version = 3)
abstract class ProxyDatabase : RoomDatabase() {
    abstract fun proxyDao(): ProxyDao
}
