package com.curseforge.mobile.domain

import com.curseforge.mobile.data.local.Settings
import com.curseforge.mobile.data.model.Addon
import com.curseforge.mobile.data.model.FileInfoResponse
import kotlinx.coroutines.flow.Flow

interface RepositoryContract {
    suspend fun search(query: String, page: Int, versionFilter: String?): Result<List<Addon>>
    suspend fun resolveFileInfo(fileId: Long): Result<FileInfoResponse>
    fun settings(): Flow<Settings>
    suspend fun saveBaseUrl(url: String)
    suspend fun setAutoOpen(enabled: Boolean)
    fun favorites(): Flow<Set<Long>>
    suspend fun toggleFavorite(addon: Addon, isFavorite: Boolean)
}
