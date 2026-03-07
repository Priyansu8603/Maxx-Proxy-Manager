package com.example.maxx.data.local.database.dao

import androidx.room.*
import com.example.maxx.data.local.database.entity.ProxyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProxyDao {

    @Query("SELECT * FROM proxy_profiles ORDER BY isFavorite DESC, orderIndex ASC")
    fun getAll(): Flow<List<ProxyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: ProxyEntity)

    @Update
    suspend fun update(profile: ProxyEntity)

    @Delete
    suspend fun delete(profile: ProxyEntity)

    @Query("DELETE FROM proxy_profiles WHERE id IN (:ids)")
    suspend fun deleteMultiple(ids: List<Int>)

    @Query("UPDATE proxy_profiles SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean)
}
