package com.curseforge.mobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteAddonEntity(
    @PrimaryKey val addonId: Long,
    val name: String,
    val author: String
)
