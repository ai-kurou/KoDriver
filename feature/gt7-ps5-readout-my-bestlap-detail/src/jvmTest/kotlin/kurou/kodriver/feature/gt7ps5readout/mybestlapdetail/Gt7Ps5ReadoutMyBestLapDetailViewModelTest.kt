@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.gt7ps5readout.mybestlapdetail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5ReadoutMyBestLapDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeReadoutPreferencesRepository
    private lateinit var viewModel: Gt7Ps5ReadoutMyBestLapDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeReadoutPreferencesRepository()
        viewModel = Gt7Ps5ReadoutMyBestLapDetailViewModel(
            observeReadoutEnabledStates = ObserveReadoutEnabledStatesUseCase(repository),
            saveReadoutEnabledState = SaveReadoutEnabledStateUseCase(repository),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態は enabled=true の UiState を返す`() = runTest {
        val state = viewModel.uiState.first()
        assertEquals(true, state.enabled)
    }

    @Test
    fun `onEnabledChanged false を渡すと enabled=false になる`() = runTest {
        viewModel.onEnabledChanged(false)
        val state = viewModel.uiState.first()
        assertEquals(false, state.enabled)
    }

    @Test
    fun `onEnabledChanged true を渡すと enabled=true になる`() = runTest {
        repository.saveReadoutEnabledState("gt7_ps5", ReadoutItemKey.BEST_LAP, false)
        viewModel.onEnabledChanged(true)
        val state = viewModel.uiState.first()
        assertEquals(true, state.enabled)
    }
}
