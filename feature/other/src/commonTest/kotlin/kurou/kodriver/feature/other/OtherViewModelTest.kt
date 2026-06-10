package kurou.kodriver.feature.other

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OtherViewModelTest {

    private val viewModel = OtherViewModel()

    @Test
    fun `初期状態では全項目が表示され選択項目はない`() = runTest {
        assertEquals(OtherItemType.entries.map { it.id }, viewModel.uiState.first().items)
        assertEquals(OtherItemType.GitHubRepository.id, viewModel.uiState.first().items.first())
        assertNull(viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `GitHubレポジトリを選択しても状態は変わらない`() = runTest {
        val initialState = viewModel.uiState.first()

        viewModel.onItemSelected(OtherItemType.GitHubRepository.id)

        assertEquals(initialState, viewModel.uiState.first())
    }

    @Test
    fun `onItemSelectedで項目を選択し再選択すると解除される`() = runTest {
        viewModel.onItemSelected(OtherItemType.License.id)

        assertEquals(OtherItemType.License, viewModel.uiState.first().selectedItem)

        viewModel.onItemSelected(OtherItemType.License.id)

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
        viewModel.onItemSelected(OtherItemType.License.id)

        viewModel.clearSelectedItem()

        assertNull(viewModel.uiState.first().selectedItem)
    }
}
