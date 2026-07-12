@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
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
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kurou.kodriver.domain.usecase.LmuWindowsVehicleApproachPreferencesUseCases
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachLateralThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachLongitudinalThresholdUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleApproachLateralThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleApproachLongitudinalThresholdUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutVehicleApproachDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var thresholdsRepository: LmuWindowsVehicleApproachThresholdsPreferencesRepository

    @MockK
    private lateinit var vehicleApproachPreferencesRepository: LmuWindowsVehicleApproachPreferencesRepository

    @MockK
    private lateinit var ttsEngine: TextToSpeechEngine

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = LmuWindowsReadoutVehicleApproachDetailViewModel(
        observeLateralThreshold = ObserveLmuWindowsVehicleApproachLateralThresholdUseCase(thresholdsRepository),
        observeLongitudinalThreshold = ObserveLmuWindowsVehicleApproachLongitudinalThresholdUseCase(
            thresholdsRepository,
        ),
        vehicleApproachPreferences = LmuWindowsVehicleApproachPreferencesUseCases(
            vehicleApproachPreferencesRepository,
        ),
        saveLateralThreshold = SaveLmuWindowsVehicleApproachLateralThresholdUseCase(thresholdsRepository),
        saveLongitudinalThreshold = SaveLmuWindowsVehicleApproachLongitudinalThresholdUseCase(thresholdsRepository),
        playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
    )

    @Test
    fun `初期状態はリポジトリのデフォルト値を反映した UiState を返す`() = runTest {
        val lateralFlow = MutableStateFlow(5.0)
        val longitudinalFlow = MutableStateFlow(1.0)
        val skipFirstLapFlow = MutableStateFlow(true)
        val startReadoutEnabledFlow = MutableStateFlow(true)
        val startReadoutTypeFlow = MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
        every { thresholdsRepository.observeLateralThresholdMeters() } returns lateralFlow
        every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns longitudinalFlow
        every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns skipFirstLapFlow
        every { vehicleApproachPreferencesRepository.observeStartReadoutEnabled() } returns startReadoutEnabledFlow
        every { vehicleApproachPreferencesRepository.observeStartReadoutType() } returns startReadoutTypeFlow
        val viewModel = createViewModel()

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
        verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
        verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
        verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
        verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartReadoutEnabled() }
        verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartReadoutType() }
        confirmVerified(thresholdsRepository, vehicleApproachPreferencesRepository)
    }

    @Test
    fun `onLateralThresholdChanged を呼ぶと UiState の lateralThresholdMeters が更新される`() = runTest {
        val lateralFlow = MutableStateFlow(5.0)
        every { thresholdsRepository.observeLateralThresholdMeters() } returns lateralFlow
        every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns MutableStateFlow(1.0)
        every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(true)
        every { vehicleApproachPreferencesRepository.observeStartReadoutEnabled() } returns MutableStateFlow(true)
        every { vehicleApproachPreferencesRepository.observeStartReadoutType() } returns
            MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
        coEvery { thresholdsRepository.saveLateralThresholdMeters(3.5) } answers {
            lateralFlow.update { 3.5 }
        }
        val viewModel = createViewModel()

        viewModel.onLateralThresholdChanged(3.5)

        assertEquals(3.5, viewModel.uiState.first().lateralThresholdMeters)
        coVerify(exactly = 1) { thresholdsRepository.saveLateralThresholdMeters(3.5) }
    }

    @Test
    fun `onLongitudinalThresholdChanged を呼ぶと UiState の longitudinalThresholdMeters が更新される`() = runTest {
        val longitudinalFlow = MutableStateFlow(1.0)
        every { thresholdsRepository.observeLateralThresholdMeters() } returns MutableStateFlow(5.0)
        every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns longitudinalFlow
        every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(true)
        every { vehicleApproachPreferencesRepository.observeStartReadoutEnabled() } returns MutableStateFlow(true)
        every { vehicleApproachPreferencesRepository.observeStartReadoutType() } returns
            MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
        coEvery { thresholdsRepository.saveLongitudinalThresholdMeters(15.0) } answers {
            longitudinalFlow.update { 15.0 }
        }
        val viewModel = createViewModel()

        viewModel.onLongitudinalThresholdChanged(15.0)

        assertEquals(15.0, viewModel.uiState.first().longitudinalThresholdMeters)
        coVerify(exactly = 1) { thresholdsRepository.saveLongitudinalThresholdMeters(15.0) }
    }

    @Test
    fun `onSkipFirstLapChanged を呼ぶと UiState の skipFirstLap が更新される`() = runTest {
        val skipFirstLapFlow = MutableStateFlow(false)
        every { thresholdsRepository.observeLateralThresholdMeters() } returns MutableStateFlow(5.0)
        every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns MutableStateFlow(1.0)
        every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns skipFirstLapFlow
        every { vehicleApproachPreferencesRepository.observeStartReadoutEnabled() } returns MutableStateFlow(true)
        every { vehicleApproachPreferencesRepository.observeStartReadoutType() } returns
            MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
        coEvery { vehicleApproachPreferencesRepository.saveSkipFirstLap(true) } answers {
            skipFirstLapFlow.update { true }
        }
        val viewModel = createViewModel()

        viewModel.onSkipFirstLapChanged(true)

        assertEquals(true, viewModel.uiState.first().skipFirstLap)
        coVerify(exactly = 1) { vehicleApproachPreferencesRepository.saveSkipFirstLap(true) }
    }

    @Test
    fun `onStartReadoutEnabledChanged を呼ぶと UiState の startReadoutEnabled が更新される`() = runTest {
        val startReadoutEnabledFlow = MutableStateFlow(true)
        every { thresholdsRepository.observeLateralThresholdMeters() } returns MutableStateFlow(5.0)
        every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns MutableStateFlow(1.0)
        every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(true)
        every { vehicleApproachPreferencesRepository.observeStartReadoutEnabled() } returns startReadoutEnabledFlow
        every { vehicleApproachPreferencesRepository.observeStartReadoutType() } returns
            MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
        coEvery { vehicleApproachPreferencesRepository.saveStartReadoutEnabled(false) } answers {
            startReadoutEnabledFlow.update { false }
        }
        val viewModel = createViewModel()

        viewModel.onStartReadoutEnabledChanged(false)

        assertEquals(false, viewModel.uiState.first().startReadoutEnabled)
        coVerify(exactly = 1) { vehicleApproachPreferencesRepository.saveStartReadoutEnabled(false) }
    }

    @Test
    fun `onStartReadoutTypeChanged を呼ぶと UiState の startReadoutType が更新されプレビュー再生される`() = runTest {
        val startReadoutTypeFlow = MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
        every { thresholdsRepository.observeLateralThresholdMeters() } returns MutableStateFlow(5.0)
        every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns MutableStateFlow(1.0)
        every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(true)
        every { vehicleApproachPreferencesRepository.observeStartReadoutEnabled() } returns MutableStateFlow(true)
        every { vehicleApproachPreferencesRepository.observeStartReadoutType() } returns startReadoutTypeFlow
        val newType = VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH
        coEvery {
            vehicleApproachPreferencesRepository.saveStartReadoutType(newType)
        } answers {
            startReadoutTypeFlow.update { newType }
        }
        every { ttsEngine.speak(SpeechEvent.LeftApproach, false) } returns Unit
        every { ttsEngine.speak(SpeechEvent.RightApproach, true) } returns Unit
        val viewModel = createViewModel()

        viewModel.onStartReadoutTypeChanged(newType)

        assertEquals(newType, viewModel.uiState.first().startReadoutType)
        coVerify(exactly = 1) {
            vehicleApproachPreferencesRepository.saveStartReadoutType(newType)
        }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.LeftApproach, false) }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.RightApproach, true) }
    }

    @Test
    fun `onResetLongitudinalThreshold を呼ぶと longitudinalThresholdMeters がデフォルト値に戻る`() = runTest {
        val longitudinalFlow = MutableStateFlow(1.0)
        every { thresholdsRepository.observeLateralThresholdMeters() } returns MutableStateFlow(5.0)
        every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns longitudinalFlow
        every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(true)
        every { vehicleApproachPreferencesRepository.observeStartReadoutEnabled() } returns MutableStateFlow(true)
        every { vehicleApproachPreferencesRepository.observeStartReadoutType() } returns
            MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
        coEvery {
            thresholdsRepository.saveLongitudinalThresholdMeters(
                LmuWindowsReadoutVehicleApproachDetailViewModel.DEFAULT_LONGITUDINAL_THRESHOLD_METERS,
            )
        } answers {
            longitudinalFlow.update {
                LmuWindowsReadoutVehicleApproachDetailViewModel.DEFAULT_LONGITUDINAL_THRESHOLD_METERS
            }
        }
        val viewModel = createViewModel()

        viewModel.onResetLongitudinalThreshold()

        assertEquals(
            LmuWindowsReadoutVehicleApproachDetailViewModel.DEFAULT_LONGITUDINAL_THRESHOLD_METERS,
            viewModel.uiState.first().longitudinalThresholdMeters,
        )
        coVerify(exactly = 1) {
            thresholdsRepository.saveLongitudinalThresholdMeters(
                LmuWindowsReadoutVehicleApproachDetailViewModel.DEFAULT_LONGITUDINAL_THRESHOLD_METERS,
            )
        }
    }

    @Test
    fun `onResetLateralThreshold を呼ぶと lateralThresholdMeters がデフォルト値に戻る`() = runTest {
        val lateralFlow = MutableStateFlow(5.0)
        every { thresholdsRepository.observeLateralThresholdMeters() } returns lateralFlow
        every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns MutableStateFlow(1.0)
        every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(true)
        every { vehicleApproachPreferencesRepository.observeStartReadoutEnabled() } returns MutableStateFlow(true)
        every { vehicleApproachPreferencesRepository.observeStartReadoutType() } returns
            MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
        coEvery {
            thresholdsRepository.saveLateralThresholdMeters(
                LmuWindowsReadoutVehicleApproachDetailViewModel.DEFAULT_LATERAL_THRESHOLD_METERS,
            )
        } answers {
            lateralFlow.update {
                LmuWindowsReadoutVehicleApproachDetailViewModel.DEFAULT_LATERAL_THRESHOLD_METERS
            }
        }
        val viewModel = createViewModel()

        viewModel.onResetLateralThreshold()

        assertEquals(
            LmuWindowsReadoutVehicleApproachDetailViewModel.DEFAULT_LATERAL_THRESHOLD_METERS,
            viewModel.uiState.first().lateralThresholdMeters,
        )
        coVerify(exactly = 1) {
            thresholdsRepository.saveLateralThresholdMeters(
                LmuWindowsReadoutVehicleApproachDetailViewModel.DEFAULT_LATERAL_THRESHOLD_METERS,
            )
        }
    }

    @Test
    fun `onStartReadoutPreviewClicked を呼ぶと CarLeft の後に CarRight がキュー再生される`() {
        every { thresholdsRepository.observeLateralThresholdMeters() } returns MutableStateFlow(5.0)
        every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns MutableStateFlow(1.0)
        every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(true)
        every { vehicleApproachPreferencesRepository.observeStartReadoutEnabled() } returns MutableStateFlow(true)
        every { vehicleApproachPreferencesRepository.observeStartReadoutType() } returns
            MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
        every { ttsEngine.speak(SpeechEvent.CarLeft, false) } returns Unit
        every { ttsEngine.speak(SpeechEvent.CarRight, true) } returns Unit
        val viewModel = createViewModel()

        viewModel.onStartReadoutPreviewClicked()

        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.CarLeft, false) }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.CarRight, true) }
    }
}
