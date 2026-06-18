package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.usecase.ObserveLateralThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLongitudinalThresholdUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLateralThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLongitudinalThresholdUseCase
import kurou.kodriver.domain.usecase.VehicleApproachPreferencesUseCases
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutVehicleApproachDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var thresholdsRepository: FakeProximityThresholdsRepository
    private lateinit var vehicleApproachPreferencesRepository: FakeVehicleApproachPreferencesRepository
    private lateinit var playedEvents: MutableList<QueuedSpeechEvent>
    private lateinit var viewModel: LmuWindowsReadoutVehicleApproachDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        thresholdsRepository = FakeProximityThresholdsRepository()
        vehicleApproachPreferencesRepository = FakeVehicleApproachPreferencesRepository()
        playedEvents = mutableListOf()
        viewModel = LmuWindowsReadoutVehicleApproachDetailViewModel(
            observeLateralThreshold = ObserveLateralThresholdUseCase(thresholdsRepository),
            observeLongitudinalThreshold = ObserveLongitudinalThresholdUseCase(thresholdsRepository),
            vehicleApproachPreferences = VehicleApproachPreferencesUseCases(vehicleApproachPreferencesRepository),
            saveLateralThreshold = SaveLateralThresholdUseCase(thresholdsRepository),
            saveLongitudinalThreshold = SaveLongitudinalThresholdUseCase(thresholdsRepository),
            playSpeechEvent = PlaySpeechEventUseCase(
                FakeTextToSpeechEngine { event, queue ->
                    playedEvents.add(QueuedSpeechEvent(event, queue))
                },
            ),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態はリポジトリのデフォルト値を反映した UiState を返す`() = runTest {
        assertEquals(
            LmuWindowsReadoutVehicleApproachDetailUiState(
                lateralThresholdMeters = 5.0,
                longitudinalThresholdMeters = 1.0,
                skipFirstLap = true,
                startReadoutEnabled = true,
                startReadoutType = VehicleApproachStartReadoutType.CAR_LEFT_RIGHT,
            ),
            viewModel.uiState.first(),
        )
    }

    @Test
    fun `onLateralThresholdChanged を呼ぶと UiState の lateralThresholdMeters が更新される`() = runTest {
        viewModel.onLateralThresholdChanged(3.5)
        assertEquals(3.5, viewModel.uiState.first().lateralThresholdMeters)
    }

    @Test
    fun `onLongitudinalThresholdChanged を呼ぶと UiState の longitudinalThresholdMeters が更新される`() = runTest {
        viewModel.onLongitudinalThresholdChanged(15.0)
        assertEquals(15.0, viewModel.uiState.first().longitudinalThresholdMeters)
    }

    @Test
    fun `onSkipFirstLapChanged を呼ぶと UiState の skipFirstLap が更新される`() = runTest {
        viewModel.onSkipFirstLapChanged(true)
        assertEquals(true, viewModel.uiState.first().skipFirstLap)
    }

    @Test
    fun `onStartReadoutEnabledChanged を呼ぶと UiState の startReadoutEnabled が更新される`() = runTest {
        viewModel.onStartReadoutEnabledChanged(false)
        assertEquals(false, viewModel.uiState.first().startReadoutEnabled)
    }

    @Test
    fun `onStartReadoutTypeChanged を呼ぶと UiState の startReadoutType が更新される`() = runTest {
        viewModel.onStartReadoutTypeChanged(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH)
        assertEquals(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH, viewModel.uiState.first().startReadoutType)
    }

    @Test
    fun `onStartReadoutPreviewClicked を呼ぶと CarLeft の後に CarRight がキュー再生される`() {
        viewModel.onStartReadoutPreviewClicked()

        assertEquals(
            listOf(
                QueuedSpeechEvent(SpeechEvent.CarLeft, queue = false),
                QueuedSpeechEvent(SpeechEvent.CarRight, queue = true),
            ),
            playedEvents,
        )
    }
}

private data class QueuedSpeechEvent(
    val event: SpeechEvent,
    val queue: Boolean,
)

private class FakeTextToSpeechEngine(
    private val onSpeak: (SpeechEvent, Boolean) -> Unit,
) : TextToSpeechEngine {
    override val currentReadoutItemKey: String? = null
    override fun speak(event: SpeechEvent, queue: Boolean) = onSpeak(event, queue)
    override fun stop() = Unit
}
