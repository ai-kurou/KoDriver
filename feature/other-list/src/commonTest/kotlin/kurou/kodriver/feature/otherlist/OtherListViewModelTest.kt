package kurou.kodriver.feature.otherlist

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AppUpdate
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class FakeAppUpdateRepository(
    private val latestRelease: AppUpdate? = null,
) : AppUpdateRepository {
    override suspend fun getLatestRelease(): AppUpdate? = latestRelease
}

class OtherListViewModelTest {

    private val viewModel = OtherListViewModel(
        checkAppUpdateAvailable = CheckAppUpdateAvailableUseCase(FakeAppUpdateRepository()),
        currentVersion = "0.5.0",
        appVersionLabel = "Windows版KoDriverバージョン",
    )

    @Test
    fun `初期状態では全項目が表示され選択項目はない`() = runTest {
        assertEquals(buildOtherListItems(), viewModel.uiState.first().items)
        assertEquals("Windows版KoDriverバージョン", viewModel.uiState.first().appVersionLabel)
        assertEquals("0.5.0", viewModel.uiState.first().appVersion)
        assertNull(viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `音量を選択すると選択状態になる`() = runTest {
        viewModel.onItemSelected(OtherListItemType.Volume)

        assertEquals(OtherListItemType.Volume, viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `GitHubレポジトリを選択しても状態は変わらない`() = runTest {
        val initialState = viewModel.uiState.first()

        viewModel.onItemSelected(OtherListItemType.GitHubRepository)

        assertEquals(initialState, viewModel.uiState.first())
    }

    @Test
    fun `リリースページを選択しても状態は変わらない`() = runTest {
        val initialState = viewModel.uiState.first()

        viewModel.onItemSelected(OtherListItemType.ReleasePage)

        assertEquals(initialState, viewModel.uiState.first())
    }

    @Test
    fun `onItemSelectedで項目を選択し再選択すると解除される`() = runTest {
        viewModel.onItemSelected(OtherListItemType.License)

        assertEquals(OtherListItemType.License, viewModel.uiState.first().selectedItem)

        viewModel.onItemSelected(OtherListItemType.License)

        assertNull(viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `clearSelectedItemで選択状態が解除される`() = runTest {
        viewModel.onItemSelected(OtherListItemType.License)

        viewModel.clearSelectedItem()

        assertNull(viewModel.uiState.first().selectedItem)
    }
}
