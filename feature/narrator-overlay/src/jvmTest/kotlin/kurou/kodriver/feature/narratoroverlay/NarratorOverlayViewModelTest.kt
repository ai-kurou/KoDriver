package kurou.kodriver.feature.narratoroverlay

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
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
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.OVERLAY_TEXT_COLOR_DEFAULT
import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.repository.OverlayBackgroundOpacityPreferencesRepository
import kurou.kodriver.domain.repository.OverlayTextColorPreferencesRepository
import kurou.kodriver.domain.repository.OverlayTextSizePreferencesRepository
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.ObserveLatestNarratedTelemetryLogUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayBackgroundOpacityUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayTextColorUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayTextSizeUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class NarratorOverlayViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val telemetryLogRepository: TelemetryLogRepository = mockk()

    private val overlayTextSizeRepository: OverlayTextSizePreferencesRepository = mockk()

    private val overlayBackgroundOpacityRepository: OverlayBackgroundOpacityPreferencesRepository = mockk()

    private val overlayTextColorRepository: OverlayTextColorPreferencesRepository = mockk()
    private val overlayTextColorFlow = MutableStateFlow(OVERLAY_TEXT_COLOR_DEFAULT)

    private val overlayTextSizeFlow = MutableStateFlow(OverlayTextSize.MEDIUM)
    private val overlayBackgroundOpacityFlow = MutableStateFlow(50)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        NarratorOverlayViewModel(
            observeLatestNarratedTelemetryLog = ObserveLatestNarratedTelemetryLogUseCase(telemetryLogRepository),
            observeOverlayTextColor = ObserveOverlayTextColorUseCase(overlayTextColorRepository),
            observeOverlayTextSize = ObserveOverlayTextSizeUseCase(overlayTextSizeRepository),
            observeOverlayBackgroundOpacity =
                ObserveOverlayBackgroundOpacityUseCase(overlayBackgroundOpacityRepository),
        )

    @Test
    fun `初期状態は latestTelemetryLog が null で overlayTextSize が MEDIUM で backgroundOpacity が50の UiState を返す`() =
        runTest {
            every { overlayTextSizeRepository.observeOverlayTextSize() } returns overlayTextSizeFlow
            every {
                overlayBackgroundOpacityRepository.observeOverlayBackgroundOpacity()
            } returns overlayBackgroundOpacityFlow
            every { overlayTextColorRepository.observeOverlayTextColor() } returns overlayTextColorFlow
            every { telemetryLogRepository.observeLatestNarratedTelemetryLog() } returns MutableStateFlow(null)
            val viewModel = createViewModel()

            val state = viewModel.uiState.first()

            assertNull(state.latestTelemetryLog)
            assertEquals(OverlayTextSize.MEDIUM, state.overlayTextSize)
            assertEquals(50, state.backgroundOpacity)
            assertEquals(OVERLAY_TEXT_COLOR_DEFAULT, state.overlayTextColor)
            verify(exactly = 1) { telemetryLogRepository.observeLatestNarratedTelemetryLog() }
            verify(exactly = 1) { overlayTextSizeRepository.observeOverlayTextSize() }
            verify(exactly = 1) { overlayBackgroundOpacityRepository.observeOverlayBackgroundOpacity() }
            verify(exactly = 1) { overlayTextColorRepository.observeOverlayTextColor() }
            confirmVerified(
                telemetryLogRepository,
                overlayTextSizeRepository,
                overlayBackgroundOpacityRepository,
                overlayTextColorRepository,
            )
        }

    @Test
    fun `TelemetryLogRepository の Flow が更新されると UiState の latestTelemetryLog も更新される`() =
        runTest {
            val telemetryLog =
                TelemetryLog(
                    id = 1L,
                    createdAt = 1_000L,
                    simulator = Simulator.AceWindows,
                    readoutItemKey = AceWindowsReadoutItemKey.RemainingFuel.Root,
                    narratedText = "コーナー進入注意",
                    narrationOutcome = NarrationOutcome.INTERRUPTED,
                    telemetryJson = "{}",
                )
            val telemetryLogFlow = MutableStateFlow<TelemetryLog?>(null)
            every { overlayTextSizeRepository.observeOverlayTextSize() } returns overlayTextSizeFlow
            every {
                overlayBackgroundOpacityRepository.observeOverlayBackgroundOpacity()
            } returns overlayBackgroundOpacityFlow
            every { overlayTextColorRepository.observeOverlayTextColor() } returns overlayTextColorFlow
            every { telemetryLogRepository.observeLatestNarratedTelemetryLog() } returns telemetryLogFlow
            val viewModel = createViewModel()

            telemetryLogFlow.update { telemetryLog }

            assertEquals(telemetryLog, viewModel.uiState.first().latestTelemetryLog)
            verify(exactly = 1) { telemetryLogRepository.observeLatestNarratedTelemetryLog() }
            verify(exactly = 1) { overlayTextSizeRepository.observeOverlayTextSize() }
            verify(exactly = 1) { overlayBackgroundOpacityRepository.observeOverlayBackgroundOpacity() }
            verify(exactly = 1) { overlayTextColorRepository.observeOverlayTextColor() }
            confirmVerified(
                telemetryLogRepository,
                overlayTextSizeRepository,
                overlayBackgroundOpacityRepository,
                overlayTextColorRepository,
            )
        }

    @Test
    fun `OverlayTextSizePreferencesRepository の Flow が更新されると UiState の overlayTextSize も更新される`() =
        runTest {
            every { overlayTextSizeRepository.observeOverlayTextSize() } returns overlayTextSizeFlow
            every {
                overlayBackgroundOpacityRepository.observeOverlayBackgroundOpacity()
            } returns overlayBackgroundOpacityFlow
            every { overlayTextColorRepository.observeOverlayTextColor() } returns overlayTextColorFlow
            every { telemetryLogRepository.observeLatestNarratedTelemetryLog() } returns MutableStateFlow(null)
            val viewModel = createViewModel()

            overlayTextSizeFlow.update { OverlayTextSize.LARGE }

            assertEquals(OverlayTextSize.LARGE, viewModel.uiState.first().overlayTextSize)
            verify(exactly = 1) { telemetryLogRepository.observeLatestNarratedTelemetryLog() }
            verify(exactly = 1) { overlayTextSizeRepository.observeOverlayTextSize() }
            verify(exactly = 1) { overlayBackgroundOpacityRepository.observeOverlayBackgroundOpacity() }
            verify(exactly = 1) { overlayTextColorRepository.observeOverlayTextColor() }
            confirmVerified(
                telemetryLogRepository,
                overlayTextSizeRepository,
                overlayBackgroundOpacityRepository,
                overlayTextColorRepository,
            )
        }

    @Test
    fun `OverlayTextColorPreferencesRepository の Flow が更新されると UiState の overlayTextColor も更新される`() =
        runTest {
            every { overlayTextSizeRepository.observeOverlayTextSize() } returns overlayTextSizeFlow
            every {
                overlayBackgroundOpacityRepository.observeOverlayBackgroundOpacity()
            } returns overlayBackgroundOpacityFlow
            every { overlayTextColorRepository.observeOverlayTextColor() } returns overlayTextColorFlow
            every { telemetryLogRepository.observeLatestNarratedTelemetryLog() } returns MutableStateFlow(null)
            val viewModel = createViewModel()

            overlayTextColorFlow.update { 0xFFFFFFFFL }

            assertEquals(0xFFFFFFFFL, viewModel.uiState.first().overlayTextColor)
            verify(exactly = 1) { telemetryLogRepository.observeLatestNarratedTelemetryLog() }
            verify(exactly = 1) { overlayTextSizeRepository.observeOverlayTextSize() }
            verify(exactly = 1) { overlayBackgroundOpacityRepository.observeOverlayBackgroundOpacity() }
            verify(exactly = 1) { overlayTextColorRepository.observeOverlayTextColor() }
            confirmVerified(
                telemetryLogRepository,
                overlayTextSizeRepository,
                overlayBackgroundOpacityRepository,
                overlayTextColorRepository,
            )
        }

    @Test
    fun `OverlayBackgroundOpacityPreferencesRepository の Flow が更新されると UiState の backgroundOpacity も更新される`() =
        runTest {
            every { overlayTextSizeRepository.observeOverlayTextSize() } returns overlayTextSizeFlow
            every {
                overlayBackgroundOpacityRepository.observeOverlayBackgroundOpacity()
            } returns overlayBackgroundOpacityFlow
            every { overlayTextColorRepository.observeOverlayTextColor() } returns overlayTextColorFlow
            every { telemetryLogRepository.observeLatestNarratedTelemetryLog() } returns MutableStateFlow(null)
            val viewModel = createViewModel()

            overlayBackgroundOpacityFlow.update { 80 }

            assertEquals(80, viewModel.uiState.first().backgroundOpacity)
            verify(exactly = 1) { telemetryLogRepository.observeLatestNarratedTelemetryLog() }
            verify(exactly = 1) { overlayTextSizeRepository.observeOverlayTextSize() }
            verify(exactly = 1) { overlayBackgroundOpacityRepository.observeOverlayBackgroundOpacity() }
            verify(exactly = 1) { overlayTextColorRepository.observeOverlayTextColor() }
            confirmVerified(
                telemetryLogRepository,
                overlayTextSizeRepository,
                overlayBackgroundOpacityRepository,
                overlayTextColorRepository,
            )
        }
}
