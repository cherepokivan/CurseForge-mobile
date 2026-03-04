package com.curseforge.mobile.ui

import com.curseforge.mobile.data.local.Settings
import com.curseforge.mobile.data.model.Addon
import com.curseforge.mobile.data.model.FileInfoResponse
import com.curseforge.mobile.domain.RepositoryContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `search reset replaces items`() = runTest {
        val repo = FakeRepository(
            pages = mapOf(1 to listOf(Addon(1, "A", "desc", "me", 11, "a.mcpack", null)))
        )
        val vm = MainViewModel(repo)

        vm.onQueryChanged("magic")
        vm.search(reset = true)
        advanceUntilIdle()

        assertEquals(1, vm.state.value.items.size)
        assertEquals("A", vm.state.value.items.first().name)
    }

    @Test
    fun `search sends version filter`() = runTest {
        val repo = FakeRepository(pages = emptyMap())
        val vm = MainViewModel(repo)

        vm.onVersionFilterChanged("26.3.0")
        vm.search(reset = true)
        advanceUntilIdle()

        assertEquals("26.3.0", repo.lastVersion)
    }
}

private class FakeRepository(
    private val pages: Map<Int, List<Addon>>
) : RepositoryContract {
    var lastVersion: String? = null

    override suspend fun search(query: String, page: Int, versionFilter: String?): Result<List<Addon>> {
        lastVersion = versionFilter
        return Result.success(pages[page].orEmpty())
    }

    override suspend fun resolveFileInfo(fileId: Long): Result<FileInfoResponse> = error("not used")
    override fun settings(): Flow<Settings> = flowOf(Settings())
    override suspend fun saveBaseUrl(url: String) = Unit
    override suspend fun setAutoOpen(enabled: Boolean) = Unit
    override fun favorites(): Flow<Set<Long>> = MutableStateFlow(emptySet())
    override suspend fun toggleFavorite(addon: Addon, isFavorite: Boolean) = Unit
}
