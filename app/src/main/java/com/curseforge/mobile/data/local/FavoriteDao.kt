package com.curseforge.mobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites")
    fun observeFavorites(): Flow<List<FavoriteAddonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FavoriteAddonEntity)

    @Query("DELETE FROM favorites WHERE addonId = :addonId")
    suspend fun delete(addonId: Long)
}
