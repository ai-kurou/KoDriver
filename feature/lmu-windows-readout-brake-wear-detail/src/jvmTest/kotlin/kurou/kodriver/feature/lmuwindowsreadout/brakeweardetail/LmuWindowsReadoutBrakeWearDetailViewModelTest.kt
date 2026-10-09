package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.LmuWindowsBrakeWearInvestigationData
import kurou.kodriver.domain.repository.LmuWindowsBrakeWearInvestigationRepository
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeWearInvestigationUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutBrakeWearDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository: LmuWindowsBrakeWearInvestigationRepository = mockk()
    private val dataFlow = MutableStateFlow(LmuWindowsBrakeWearInvestigationData())

    private val first =
        LmuWindowsBrakeWearInvestigationData(
            wearablesBrakes = listOf(0.036, 0.035, 0.032, 0.031),
            brakeInfo = listOf(0.036, 0.036, 0.032, 0.032),
        )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        LmuWindowsReadoutBrakeWearDetailViewModel(ObserveLmuWindowsBrakeWearInvestigationUseCase(repository))

    @Test
    fun `初期状態は値なしで基準も未設定`() =
        runTest {
            every { repository.investigationStream() } returns dataFlow
            val viewModel = createViewModel()
            backgroundScope.launch { viewModel.uiState.collect {} }

            assertEquals(LmuWindowsReadoutBrakeWearDetailUiState(), viewModel.uiState.value)
            verify(exactly = 1) { repository.investigationStream() }
            confirmVerified(repository)
        }

    @Test
    fun `取得した生の値がuiStateのcurrentに反映される`() =
        runTest {
            every { repository.investigationStream() } returns dataFlow
            val viewModel = createViewModel()
            backgroundScope.launch { viewModel.uiState.collect {} }

            dataFlow.update { first }

            assertEquals(first, viewModel.uiState.first().current)
            assertNull(viewModel.uiState.first().baseline)
            verify(exactly = 1) { repository.investigationStream() }
            confirmVerified(repository)
        }

    @Test
    fun `基準に設定すると以降の値が変わっても基準は保持される`() =
        runTest {
            every { repository.investigationStream() } returns dataFlow
            val viewModel = createViewModel()
            backgroundScope.launch { viewModel.uiState.collect {} }
            dataFlow.update { first }
            viewModel.uiState.first { it.current == first }

            viewModel.onBaselineSet()
            val next = first.copy(wearablesBrakes = listOf(0.034, 0.033, 0.03, 0.029))
            dataFlow.update { next }

            val state = viewModel.uiState.first { it.current == next }
            assertEquals(first, state.baseline)
            verify(exactly = 1) { repository.investigationStream() }
            confirmVerified(repository)
        }

    @Test
    fun `値を1つも取得できていないときは基準に設定しない`() =
        runTest {
            every { repository.investigationStream() } returns dataFlow
            val viewModel = createViewModel()
            backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.onBaselineSet()

            assertNull(viewModel.uiState.first().baseline)
            verify(exactly = 1) { repository.investigationStream() }
            confirmVerified(repository)
        }

    @Test
    fun `基準をクリアすると基準が未設定に戻る`() =
        runTest {
            every { repository.investigationStream() } returns dataFlow
            val viewModel = createViewModel()
            backgroundScope.launch { viewModel.uiState.collect {} }
            dataFlow.update { first }
            viewModel.uiState.first { it.current == first }
            viewModel.onBaselineSet()
            viewModel.uiState.first { it.baseline == first }

            viewModel.onBaselineCleared()

            assertNull(viewModel.uiState.first().baseline)
            verify(exactly = 1) { repository.investigationStream() }
            confirmVerified(repository)
        }
}
