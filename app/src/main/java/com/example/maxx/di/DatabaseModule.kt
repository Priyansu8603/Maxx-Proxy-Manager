package com.example.maxx.di

import android.content.Context
import androidx.room.Room
import com.example.maxx.data.local.database.ProxyDatabase
import com.example.maxx.data.local.database.dao.ProxyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideProxyDatabase(
        @ApplicationContext context: Context
    ): ProxyDatabase {
        return Room.databaseBuilder(
            context,
            ProxyDatabase::class.java,
            "proxy_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideProxyDao(database: ProxyDatabase): ProxyDao {
        return database.proxyDao()
    }
}

