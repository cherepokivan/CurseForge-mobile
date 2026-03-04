package com.curseforge.mobile.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.curseforge.mobile.data.model.Addon
import com.curseforge.mobile.domain.ServiceLocator
import com.curseforge.mobile.download.DownloadHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(appContext: Context) {
    val vm: MainViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(ServiceLocator.repository(appContext)) as T
        }
    })

    val state by vm.state.collectAsState()
    var tab by remember { mutableStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("CurseForge Bedrock", fontWeight = FontWeight.SemiBold)
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Каталог") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Настройки") })
            }
            when (tab) {
                0 -> CatalogScreen(state, vm)
                1 -> SettingsScreen(state, vm)
            }
        }
    }
}

@Composable
private fun CatalogScreen(state: UiState, vm: MainViewModel) {
    val context = LocalContext.current
    val helper = remember { DownloadHelper(context) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = state.query,
            onValueChange = vm::onQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Поиск аддонов") }
        )

        OutlinedTextField(
            value = state.versionFilter,
            onValueChange = vm::onVersionFilterChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Версия Bedrock (опционально)") },
            placeholder = { Text("например: 1.20.132 / 26.0.0.2 / 26.3.0") }
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(vm.quickVersionPresets) { preset ->
                val title = if (preset.isBlank()) "Все версии" else preset
                Button(onClick = { vm.applyPresetVersion(preset) }) {
                    Text(title)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.search(reset = true) }) { Text("Найти") }
            Button(onClick = { vm.search(reset = false) }) { Text("Ещё") }
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (state.loading) CircularProgressIndicator()

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.items) { addon ->
                AddonCard(addon, state.favorites.contains(addon.id), onFavorite = { vm.toggleFavorite(addon) }) {
                    val resolved = addon.downloadUrl
                        ?: "${if (state.baseUrlOverride.isNotBlank()) state.baseUrlOverride else vm.backendBaseUrlHint()}/api/download?fileId=${addon.latestFileId}"
                    helper.enqueueDownload(addon.latestFileId, addon.latestFileName, resolved)
                    Toast.makeText(context, "Загрузка началась", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

@Composable
private fun AddonCard(addon: Addon, isFavorite: Boolean, onFavorite: () -> Unit, onDownload: () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(addon.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = onFavorite) {
                    Text(if (isFavorite) "★" else "☆", color = MaterialTheme.colorScheme.primary)
                }
            }
            Text(addon.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Автор: ${addon.author}")
            Text("Файл: ${addon.latestFileName}")
            Button(onClick = onDownload) { Text("Download & Install") }
        }
    }
}

@Composable
private fun SettingsScreen(state: UiState, vm: MainViewModel) {
    var baseUrl by remember(state.baseUrlOverride) { mutableStateOf(state.baseUrlOverride) }
    var autoOpen by remember(state.autoOpen) { mutableStateOf(state.autoOpen) }
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Build-time URL: ${vm.backendBaseUrlHint()}")
        OutlinedTextField(
            value = baseUrl,
            onValueChange = { baseUrl = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Override BASE_URL (debug)") }
        )
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Авто-открывать после загрузки")
            Switch(checked = autoOpen, onCheckedChange = { autoOpen = it })
        }
        Button(onClick = {
            vm.saveSettings(baseUrl.trim(), autoOpen)
            Toast.makeText(context, "Настройки сохранены", Toast.LENGTH_SHORT).show()
        }) { Text("Сохранить") }

        Button(onClick = { vm.refreshLogs() }) { Text("Send logs (preview)") }
        Text(state.logs.ifBlank { "Логи пока пустые" }, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
