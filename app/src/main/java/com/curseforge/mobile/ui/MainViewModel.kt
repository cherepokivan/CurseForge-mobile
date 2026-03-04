package com.curseforge.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.curseforge.mobile.BuildConfig
import com.curseforge.mobile.data.model.Addon
import com.curseforge.mobile.domain.RepositoryContract
import com.curseforge.mobile.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class UiState(
    val query: String = "",
    val versionFilter: String = "",
    val page: Int = 1,
    val items: List<Addon> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val favorites: Set<Long> = emptySet(),
    val autoOpen: Boolean = true,
    val baseUrlOverride: String = "",
    val logs: String = ""
)

class MainViewModel(private val repository: RepositoryContract) : ViewModel() {
    private val mutableState = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = mutableState.asStateFlow()

    val quickVersionPresets = listOf("", "1.20.132", "26.0.0.2", "26.3.0")

    init {
        viewModelScope.launch {
            combine(repository.settings(), repository.favorites()) { settings, favorites ->
                settings to favorites
            }.collect { (settings, favorites) ->
                mutableState.value = mutableState.value.copy(
                    autoOpen = settings.autoOpenAfterDownload,
                    baseUrlOverride = settings.baseUrlOverride,
                    favorites = favorites
                )
            }
        }
        search(reset = true)
    }

    fun onQueryChanged(value: String) {
        mutableState.value = mutableState.value.copy(query = value)
    }

    fun onVersionFilterChanged(value: String) {
        mutableState.value = mutableState.value.copy(versionFilter = value)
    }

    fun applyPresetVersion(version: String) {
        mutableState.value = mutableState.value.copy(versionFilter = version)
    }

    fun search(reset: Boolean) {
        val nextPage = if (reset) 1 else mutableState.value.page + 1
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(loading = true, error = null)
            val query = mutableState.value.query
            val version = mutableState.value.versionFilter.trim().ifBlank { null }
            repository.search(query, nextPage, version)
                .onSuccess { data ->
                    mutableState.value = mutableState.value.copy(
                        loading = false,
                        page = nextPage,
                        items = if (reset) data else mutableState.value.items + data
                    )
                }
                .onFailure {
                    Logger.log("Search", "Error: ${it.message}")
                    mutableState.value = mutableState.value.copy(
                        loading = false,
                        error = it.message ?: "Ошибка сети. Проверьте подключение и URL бэкенда."
                    )
                }
        }
    }

    fun toggleFavorite(addon: Addon) {
        viewModelScope.launch {
            repository.toggleFavorite(addon, mutableState.value.favorites.contains(addon.id))
        }
    }

    fun saveSettings(baseUrl: String, autoOpen: Boolean) {
        viewModelScope.launch {
            repository.saveBaseUrl(baseUrl)
            repository.setAutoOpen(autoOpen)
        }
    }

    fun refreshLogs() {
        mutableState.value = mutableState.value.copy(logs = Logger.recent())
    }

    fun backendBaseUrlHint(): String = BuildConfig.VERCEL_BASE_URL
}
