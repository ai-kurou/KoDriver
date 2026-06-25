@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherkeepscreenondetail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnUseCase
import kurou.kodriver.domain.usecase.SaveKeepScreenOnUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OtherKeepScreenOnDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeKeepScreenOnPreferencesRepository
    private lateinit var viewModel: OtherKeepScreenOnDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeKeepScreenOnPreferencesRepository()
        viewModel = OtherKeepScreenOnDetailViewModel(
            observeKeepScreenOn = ObserveKeepScreenOnUseCase(repository),
            saveKeepScreenOn = SaveKeepScreenOnUseCase(repository),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態はkeepScreenOnがfalseである`() = runTest {
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.keepScreenOn)
        assertFalse(uiState.pendingKeepScreenOn)
    }

    @Test
    fun `onPendingValueChangedでpendingが更新される`() = runTest {
        viewModel.onPendingValueChanged(true)

        val uiState = viewModel.uiState.first()
        assertTrue(uiState.pendingKeepScreenOn)
    }

    @Test
    fun `onConfirmで設定が保存される`() = runTest {
        viewModel.onPendingValueChanged(true)
        viewModel.onConfirm()

        val uiState = viewModel.uiState.first()
        assertTrue(uiState.keepScreenOn)
    }

    @Test
    fun `onDismissでpendingがリセットされる`() = runTest {
        viewModel.onPendingValueChanged(true)
        viewModel.onDismiss()

        val uiState = viewModel.uiState.first()
        assertEquals(false, uiState.pendingKeepScreenOn)
    }
}
