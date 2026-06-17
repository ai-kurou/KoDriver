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
        currentVersion = "",
    )

    @Test
    fun `初期状態では全項目が表示され選択項目はない`() = runTest {
        assertEquals(buildOtherListItems().map { it.id }, viewModel.uiState.first().items)
        assertNull(viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `音量を選択すると選択状態になる`() = runTest {
        viewModel.onItemSelected(OtherListItemType.Volume.id)

        assertEquals(OtherListItemType.Volume, viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `GitHubレポジトリを選択しても状態は変わらない`() = runTest {
        val initialState = viewModel.uiState.first()

        viewModel.onItemSelected(OtherListItemType.GitHubRepository.id)

        assertEquals(initialState, viewModel.uiState.first())
    }

    @Test
    fun `リリースページを選択しても状態は変わらない`() = runTest {
        val initialState = viewModel.uiState.first()

        viewModel.onItemSelected(OtherListItemType.ReleasePage.id)

        assertEquals(initialState, viewModel.uiState.first())
    }

    @Test
    fun `onItemSelectedで項目を選択し再選択すると解除される`() = runTest {
        viewModel.onItemSelected(OtherListItemType.License.id)

        assertEquals(OtherListItemType.License, viewModel.uiState.first().selectedItem)

        viewModel.onItemSelected(OtherListItemType.License.id)

        assertNull(viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `存在しない項目を選択しても状態は変わらない`() = runTest {
        val initialState = viewModel.uiState.first()

        viewModel.onItemSelected("unknown")

        assertEquals(initialState, viewModel.uiState.first())
    }

    @Test
    fun `clearSelectedItemで選択状態が解除される`() = runTest {
        viewModel.onItemSelected(OtherListItemType.License.id)

        viewModel.clearSelectedItem()

        assertNull(viewModel.uiState.first().selectedItem)
    }
}
