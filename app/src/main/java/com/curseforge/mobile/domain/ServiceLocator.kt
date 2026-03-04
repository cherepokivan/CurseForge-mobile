package com.curseforge.mobile.domain

import android.content.Context
import androidx.room.Room
import com.curseforge.mobile.data.local.AppDatabase
import com.curseforge.mobile.data.local.SettingsStore

object ServiceLocator {
    @Volatile
    private var repository: AddonRepository? = null

    fun repository(context: Context): AddonRepository {
        return repository ?: synchronized(this) {
            repository ?: buildRepository(context.applicationContext).also { repository = it }
        }
    }

    private fun buildRepository(context: Context): AddonRepository {
        val db = Room.databaseBuilder(context, AppDatabase::class.java, "addons.db").build()
        return AddonRepository(SettingsStore(context), db.favoriteDao())
    }
}
