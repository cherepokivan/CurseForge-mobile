package com.curseforge.mobile.domain

import com.curseforge.mobile.BuildConfig
import com.curseforge.mobile.data.local.FavoriteAddonEntity
import com.curseforge.mobile.data.local.FavoriteDao
import com.curseforge.mobile.data.local.SettingsStore
import com.curseforge.mobile.data.model.Addon
import com.curseforge.mobile.data.model.FileInfoResponse
import com.curseforge.mobile.data.model.toDomain
import com.curseforge.mobile.data.remote.ApiFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AddonRepository(
    private val settingsStore: SettingsStore,
    private val favoriteDao: FavoriteDao
) : RepositoryContract {
    private val pageCache = mutableMapOf<String, MutableMap<Int, List<Addon>>>()

    override suspend fun search(query: String, page: Int, versionFilter: String?): Result<List<Addon>> = runCatching {
        val settings = settingsStore.settingsFlow.first()
        val baseUrl = settings.baseUrlOverride.takeIf { it.isNotBlank() } ?: BuildConfig.VERCEL_BASE_URL
        val normalizedVersion = versionFilter?.trim().orEmpty().ifBlank { "all" }
        val cacheKey = "$query|$normalizedVersion"

        pageCache[cacheKey]?.get(page)?.let { return@runCatching it }

        val api = ApiFactory.create(baseUrl)
        val result = api.searchAddons(
            query = query,
            page = page,
            version = versionFilter?.trim()?.ifBlank { null }
        ).results.map { it.toDomain() }

        pageCache.getOrPut(cacheKey) { mutableMapOf() }[page] = result
        result
    }

    override suspend fun resolveFileInfo(fileId: Long): Result<FileInfoResponse> = runCatching {
        val settings = settingsStore.settingsFlow.first()
        val baseUrl = settings.baseUrlOverride.takeIf { it.isNotBlank() } ?: BuildConfig.VERCEL_BASE_URL
        ApiFactory.create(baseUrl).fileInfo(fileId)
    }

    override fun settings() = settingsStore.settingsFlow

    override suspend fun saveBaseUrl(url: String) = settingsStore.updateBaseUrl(url)

    override suspend fun setAutoOpen(enabled: Boolean) = settingsStore.updateAutoOpen(enabled)

    override fun favorites(): Flow<Set<Long>> = favoriteDao.observeFavorites().map { list -> list.map { it.addonId }.toSet() }

    override suspend fun toggleFavorite(addon: Addon, isFavorite: Boolean) {
        if (isFavorite) favoriteDao.delete(addon.id)
        else favoriteDao.upsert(FavoriteAddonEntity(addon.id, addon.name, addon.author))
    }
}
