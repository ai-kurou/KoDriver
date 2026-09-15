package kurou.kodriver.feature.narratoroverlay

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.ObserveLatestTelemetryLogUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class NarratorOverlayViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: TelemetryLogRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        NarratorOverlayViewModel(
            observeLatestTelemetryLog = ObserveLatestTelemetryLogUseCase(repository),
        )

    @Test
    fun `初期状態は latestTelemetryLog が null の UiState を返す`() =
        runTest {
            every { repository.observeLatestTelemetryLog() } returns MutableStateFlow(null)
            val viewModel = createViewModel()

            val state = viewModel.uiState.first()

            assertNull(state.latestTelemetryLog)
            verify(exactly = 1) { repository.observeLatestTelemetryLog() }
            confirmVerified(repository)
        }

    @Test
    fun `TelemetryLogRepository の Flow が更新されると UiState の latestTelemetryLog も更新される`() =
        runTest {
            val telemetryLog =
                TelemetryLog(
                    id = 1L,
                    createdAt = 1_000L,
                    simulator = Simulator.AceWindows,
                    readoutItemKey = ReadoutItemKey.AceWindows.RemainingFuel.Root,
                    narratedText = "コーナー進入注意",
                    telemetryJson = "{}",
                )
            val telemetryLogFlow = MutableStateFlow<TelemetryLog?>(null)
            every { repository.observeLatestTelemetryLog() } returns telemetryLogFlow
            val viewModel = createViewModel()

            telemetryLogFlow.update { telemetryLog }

            assertEquals(telemetryLog, viewModel.uiState.first().latestTelemetryLog)
            verify(exactly = 1) { repository.observeLatestTelemetryLog() }
            confirmVerified(repository)
        }
}
