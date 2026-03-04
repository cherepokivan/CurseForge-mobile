package com.curseforge.mobile.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

data class Settings(
    val baseUrlOverride: String = "",
    val autoOpenAfterDownload: Boolean = true
)

class SettingsStore(private val context: Context) {
    private val baseUrlKey = stringPreferencesKey("base_url_override")
    private val autoOpenKey = booleanPreferencesKey("auto_open")

    val settingsFlow: Flow<Settings> = context.dataStore.data.map { prefs ->
        Settings(
            baseUrlOverride = prefs[baseUrlKey].orEmpty(),
            autoOpenAfterDownload = prefs[autoOpenKey] ?: true
        )
    }

    suspend fun updateBaseUrl(url: String) {
        context.dataStore.edit { it[baseUrlKey] = url }
    }

    suspend fun updateAutoOpen(enabled: Boolean) {
        context.dataStore.edit { it[autoOpenKey] = enabled }
    }
}
