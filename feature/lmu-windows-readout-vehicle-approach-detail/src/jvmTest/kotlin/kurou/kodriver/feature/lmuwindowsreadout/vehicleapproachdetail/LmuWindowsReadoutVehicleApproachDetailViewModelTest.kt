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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.model.VehicleApproachSustainedReadoutType
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kurou.kodriver.domain.usecase.LmuWindowsVehicleApproachPreferencesUseCases
import kurou.kodriver.domain.usecase.LmuWindowsVehicleApproachThresholdsUseCases
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleApproachEnabledStateUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
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
        LmuWindowsReadoutVehicleApproachDetailViewModel(
            thresholds = LmuWindowsVehicleApproachThresholdsUseCases(thresholdsRepository),
            vehicleApproachPreferences =
                LmuWindowsVehicleApproachPreferencesUseCases(
                    vehicleApproachPreferencesRepository,
                ),
            observeEnabledStates =
                ObserveLmuWindowsVehicleApproachEnabledStatesUseCase(
                    vehicleApproachPreferencesRepository,
                ),
            saveEnabledState = SaveLmuWindowsVehicleApproachEnabledStateUseCase(vehicleApproachPreferencesRepository),
            playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
        )

    @Test
    fun `初期状態はリポジトリのデフォルト値を反映した UiState を返す`() =
        runTest {
            val lateralFlow = MutableStateFlow(5.0)
            val longitudinalFlow = MutableStateFlow(5.0)
            val skipFirstLapFlow = MutableStateFlow(true)
            val startReadoutEnabledFlow = MutableStateFlow(true)
            val startReadoutTypeFlow = MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
            every { thresholdsRepository.observeLateralThresholdMeters() } returns lateralFlow
            every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns longitudinalFlow
            every { thresholdsRepository.observeSustainedApproachDurationSeconds() } returns MutableStateFlow(4)
            every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns skipFirstLapFlow
            every { vehicleApproachPreferencesRepository.observeEnabledStates() } returns
                startReadoutEnabledFlow.map {
                    mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to it)
                }
            every { vehicleApproachPreferencesRepository.observeStartReadoutType() } returns startReadoutTypeFlow
            every { vehicleApproachPreferencesRepository.observeSustainedReadoutType() } returns
                MutableStateFlow(VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT)
            val viewModel = createViewModel()

            assertEquals(
                LmuWindowsReadoutVehicleApproachDetailUiState(
                    lateralThresholdMeters = 5.0,
                    longitudinalThresholdMeters = 5.0,
                    sustainedApproachDurationSeconds = 4,
                    skipFirstLap = true,
                    startReadoutEnabled = true,
                    startReadoutType = VehicleApproachStartReadoutType.CAR_LEFT_RIGHT,
                    sustainedReadoutType = VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT,
                ),
                viewModel.uiState.first(),
            )
            verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeSustainedApproachDurationSeconds() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeEnabledStates() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartReadoutType() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedReadoutType() }
            confirmVerified(thresholdsRepository, vehicleApproachPreferencesRepository)
        }

    @Test
    fun `onLateralThresholdChanged を呼ぶと UiState の lateralThresholdMeters が更新される`() =
        runTest {
            val lateralFlow = MutableStateFlow(5.0)
            every { thresholdsRepository.observeLateralThresholdMeters() } returns lateralFlow
            every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns MutableStateFlow(1.0)
            every { thresholdsRepository.observeSustainedApproachDurationSeconds() } returns MutableStateFlow(4)
            every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(true)
            every { vehicleApproachPreferencesRepository.observeEnabledStates() } returns
                MutableStateFlow(
                    mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to true),
                )
            every { vehicleApproachPreferencesRepository.observeStartReadoutType() } returns
                MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
            every { vehicleApproachPreferencesRepository.observeSustainedReadoutType() } returns
                MutableStateFlow(VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT)
            coEvery { thresholdsRepository.saveLateralThresholdMeters(3.5) } answers {
                lateralFlow.update { 3.5 }
            }
            val viewModel = createViewModel()

            viewModel.onLateralThresholdChanged(3.5)

            assertEquals(3.5, viewModel.uiState.first().lateralThresholdMeters)
            coVerify(exactly = 1) { thresholdsRepository.saveLateralThresholdMeters(3.5) }
            verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeSustainedApproachDurationSeconds() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeEnabledStates() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartReadoutType() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedReadoutType() }
            confirmVerified(thresholdsRepository, vehicleApproachPreferencesRepository)
        }

    @Test
    fun `onLongitudinalThresholdChanged を呼ぶと UiState の longitudinalThresholdMeters が更新される`() =
        runTest {
            val longitudinalFlow = MutableStateFlow(1.0)
            every { thresholdsRepository.observeLateralThresholdMeters() } returns MutableStateFlow(5.0)
            every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns longitudinalFlow
            every { thresholdsRepository.observeSustainedApproachDurationSeconds() } returns MutableStateFlow(4)
            every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(true)
            every { vehicleApproachPreferencesRepository.observeEnabledStates() } returns
                MutableStateFlow(
                    mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to true),
                )
            every { vehicleApproachPreferencesRepository.observeStartReadoutType() } returns
                MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
            every { vehicleApproachPreferencesRepository.observeSustainedReadoutType() } returns
                MutableStateFlow(VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT)
            coEvery { thresholdsRepository.saveLongitudinalThresholdMeters(15.0) } answers {
                longitudinalFlow.update { 15.0 }
            }
            val viewModel = createViewModel()

            viewModel.onLongitudinalThresholdChanged(15.0)

            assertEquals(15.0, viewModel.uiState.first().longitudinalThresholdMeters)
            coVerify(exactly = 1) { thresholdsRepository.saveLongitudinalThresholdMeters(15.0) }
            verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeSustainedApproachDurationSeconds() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeEnabledStates() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartReadoutType() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedReadoutType() }
            confirmVerified(thresholdsRepository, vehicleApproachPreferencesRepository)
        }

    @Test
    fun `onSkipFirstLapChanged を呼ぶと UiState の skipFirstLap が更新される`() =
        runTest {
            val skipFirstLapFlow = MutableStateFlow(false)
            every { thresholdsRepository.observeLateralThresholdMeters() } returns MutableStateFlow(5.0)
            every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns MutableStateFlow(1.0)
            every { thresholdsRepository.observeSustainedApproachDurationSeconds() } returns MutableStateFlow(4)
            every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns skipFirstLapFlow
            every { vehicleApproachPreferencesRepository.observeEnabledStates() } returns
                MutableStateFlow(
                    mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to true),
                )
            every { vehicleApproachPreferencesRepository.observeStartReadoutType() } returns
                MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
            every { vehicleApproachPreferencesRepository.observeSustainedReadoutType() } returns
                MutableStateFlow(VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT)
            coEvery { vehicleApproachPreferencesRepository.saveSkipFirstLap(true) } answers {
                skipFirstLapFlow.update { true }
            }
            val viewModel = createViewModel()

            viewModel.onSkipFirstLapChanged(true)

            assertEquals(true, viewModel.uiState.first().skipFirstLap)
            coVerify(exactly = 1) { vehicleApproachPreferencesRepository.saveSkipFirstLap(true) }
            verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeSustainedApproachDurationSeconds() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeEnabledStates() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartReadoutType() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedReadoutType() }
            confirmVerified(vehicleApproachPreferencesRepository, thresholdsRepository)
        }

    @Test
    fun `onStartReadoutEnabledChanged を呼ぶと UiState の startReadoutEnabled が更新される`() =
        runTest {
            val startReadoutEnabledFlow = MutableStateFlow(true)
            every { thresholdsRepository.observeLateralThresholdMeters() } returns MutableStateFlow(5.0)
            every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns MutableStateFlow(1.0)
            every { thresholdsRepository.observeSustainedApproachDurationSeconds() } returns MutableStateFlow(4)
            every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(true)
            every { vehicleApproachPreferencesRepository.observeEnabledStates() } returns
                startReadoutEnabledFlow.map {
                    mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to it)
                }
            every { vehicleApproachPreferencesRepository.observeStartReadoutType() } returns
                MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
            every { vehicleApproachPreferencesRepository.observeSustainedReadoutType() } returns
                MutableStateFlow(VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT)
            coEvery {
                vehicleApproachPreferencesRepository.saveEnabledState(
                    ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout,
                    false,
                )
            } answers {
                startReadoutEnabledFlow.update { false }
            }
            val viewModel = createViewModel()

            viewModel.onStartReadoutEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().startReadoutEnabled)
            coVerify(exactly = 1) {
                vehicleApproachPreferencesRepository.saveEnabledState(
                    ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout,
                    false,
                )
            }
            verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeSustainedApproachDurationSeconds() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeEnabledStates() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartReadoutType() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedReadoutType() }
            confirmVerified(vehicleApproachPreferencesRepository, thresholdsRepository)
        }

    @Test
    fun `onSustainedReadoutEnabledChanged を呼ぶと UiState の sustainedReadoutEnabled が更新される`() =
        runTest {
            val sustainedReadoutEnabledFlow = MutableStateFlow(false)
            every { thresholdsRepository.observeLateralThresholdMeters() } returns MutableStateFlow(5.0)
            every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns MutableStateFlow(1.0)
            every { thresholdsRepository.observeSustainedApproachDurationSeconds() } returns MutableStateFlow(4)
            every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(true)
            every { vehicleApproachPreferencesRepository.observeEnabledStates() } returns
                sustainedReadoutEnabledFlow.map {
                    mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.Sustained to it)
                }
            every { vehicleApproachPreferencesRepository.observeStartReadoutType() } returns
                MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
            every { vehicleApproachPreferencesRepository.observeSustainedReadoutType() } returns
                MutableStateFlow(VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT)
            coEvery {
                vehicleApproachPreferencesRepository.saveEnabledState(
                    ReadoutItemKey.LmuWindows.VehicleApproach.Sustained,
                    true,
                )
            } answers {
                sustainedReadoutEnabledFlow.update { true }
            }
            val viewModel = createViewModel()

            viewModel.onSustainedReadoutEnabledChanged(true)

            assertEquals(true, viewModel.uiState.first().sustainedReadoutEnabled)
            coVerify(exactly = 1) {
                vehicleApproachPreferencesRepository.saveEnabledState(
                    ReadoutItemKey.LmuWindows.VehicleApproach.Sustained,
                    true,
                )
            }
            verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeSustainedApproachDurationSeconds() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeEnabledStates() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartReadoutType() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedReadoutType() }
            confirmVerified(vehicleApproachPreferencesRepository, thresholdsRepository)
        }

    @Test
    fun `onStartReadoutTypeChanged を呼ぶと UiState の startReadoutType が更新されプレビュー再生される`() =
        runTest {
            val startReadoutTypeFlow = MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
            every { thresholdsRepository.observeLateralThresholdMeters() } returns MutableStateFlow(5.0)
            every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns MutableStateFlow(1.0)
            every { thresholdsRepository.observeSustainedApproachDurationSeconds() } returns MutableStateFlow(4)
            every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(true)
            every { vehicleApproachPreferencesRepository.observeEnabledStates() } returns
                MutableStateFlow(
                    mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to true),
                )
            every { vehicleApproachPreferencesRepository.observeStartReadoutType() } returns startReadoutTypeFlow
            every { vehicleApproachPreferencesRepository.observeSustainedReadoutType() } returns
                MutableStateFlow(VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT)
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
            verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeSustainedApproachDurationSeconds() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeEnabledStates() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartReadoutType() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedReadoutType() }
            confirmVerified(vehicleApproachPreferencesRepository, ttsEngine, thresholdsRepository)
        }

    @Test
    fun `onSustainedReadoutTypeChanged を呼ぶと UiState の sustainedReadoutType が更新されプレビュー再生される`() =
        runTest {
            val sustainedReadoutTypeFlow = MutableStateFlow(VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT)
            every { thresholdsRepository.observeLateralThresholdMeters() } returns MutableStateFlow(5.0)
            every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns MutableStateFlow(1.0)
            every { thresholdsRepository.observeSustainedApproachDurationSeconds() } returns MutableStateFlow(4)
            every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(true)
            every { vehicleApproachPreferencesRepository.observeEnabledStates() } returns
                MutableStateFlow(
                    mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to true),
                )
            every { vehicleApproachPreferencesRepository.observeStartReadoutType() } returns
                MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
            every { vehicleApproachPreferencesRepository.observeSustainedReadoutType() } returns
                sustainedReadoutTypeFlow
            val newType = VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED
            coEvery {
                vehicleApproachPreferencesRepository.saveSustainedReadoutType(newType)
            } answers {
                sustainedReadoutTypeFlow.update { newType }
            }
            every { ttsEngine.speak(SpeechEvent.LeftSustained, false) } returns Unit
            every { ttsEngine.speak(SpeechEvent.RightSustained, true) } returns Unit
            val viewModel = createViewModel()

            viewModel.onSustainedReadoutTypeChanged(newType)

            assertEquals(newType, viewModel.uiState.first().sustainedReadoutType)
            coVerify(exactly = 1) {
                vehicleApproachPreferencesRepository.saveSustainedReadoutType(newType)
            }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.LeftSustained, false) }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.RightSustained, true) }
            verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeSustainedApproachDurationSeconds() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeEnabledStates() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartReadoutType() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedReadoutType() }
            confirmVerified(vehicleApproachPreferencesRepository, ttsEngine, thresholdsRepository)
        }

    @Test
    fun `onSustainedReadoutTypeChanged に KEEP_LEFT_RIGHT を渡すと KeepLeft の後に KeepRight がキュー再生される`() =
        runTest {
            val sustainedReadoutTypeFlow = MutableStateFlow(VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED)
            every { thresholdsRepository.observeLateralThresholdMeters() } returns MutableStateFlow(5.0)
            every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns MutableStateFlow(1.0)
            every { thresholdsRepository.observeSustainedApproachDurationSeconds() } returns MutableStateFlow(4)
            every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(true)
            every { vehicleApproachPreferencesRepository.observeEnabledStates() } returns
                MutableStateFlow(
                    mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to true),
                )
            every { vehicleApproachPreferencesRepository.observeStartReadoutType() } returns
                MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
            every { vehicleApproachPreferencesRepository.observeSustainedReadoutType() } returns
                sustainedReadoutTypeFlow
            val newType = VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT
            coEvery {
                vehicleApproachPreferencesRepository.saveSustainedReadoutType(newType)
            } answers {
                sustainedReadoutTypeFlow.update { newType }
            }
            every { ttsEngine.speak(SpeechEvent.KeepLeft, false) } returns Unit
            every { ttsEngine.speak(SpeechEvent.KeepRight, true) } returns Unit
            val viewModel = createViewModel()

            viewModel.onSustainedReadoutTypeChanged(newType)

            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.KeepLeft, false) }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.KeepRight, true) }
            verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeSustainedApproachDurationSeconds() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeEnabledStates() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartReadoutType() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedReadoutType() }
            coVerify(exactly = 1) { vehicleApproachPreferencesRepository.saveSustainedReadoutType(newType) }
            confirmVerified(ttsEngine, thresholdsRepository, vehicleApproachPreferencesRepository)
        }

    @Test
    fun `onResetLongitudinalThreshold を呼ぶと longitudinalThresholdMeters がデフォルト値に戻る`() =
        runTest {
            val longitudinalFlow = MutableStateFlow(1.0)
            every { thresholdsRepository.observeLateralThresholdMeters() } returns MutableStateFlow(5.0)
            every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns longitudinalFlow
            every { thresholdsRepository.observeSustainedApproachDurationSeconds() } returns MutableStateFlow(4)
            every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(true)
            every { vehicleApproachPreferencesRepository.observeEnabledStates() } returns
                MutableStateFlow(
                    mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to true),
                )
            every { vehicleApproachPreferencesRepository.observeStartReadoutType() } returns
                MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
            every { vehicleApproachPreferencesRepository.observeSustainedReadoutType() } returns
                MutableStateFlow(VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT)
            coEvery {
                thresholdsRepository.saveLongitudinalThresholdMeters(
                    LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT,
                )
            } answers {
                longitudinalFlow.update {
                    LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT
                }
            }
            val viewModel = createViewModel()

            viewModel.onResetLongitudinalThreshold()

            assertEquals(
                LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT,
                viewModel.uiState.first().longitudinalThresholdMeters,
            )
            coVerify(exactly = 1) {
                thresholdsRepository.saveLongitudinalThresholdMeters(
                    LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT,
                )
            }
            verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeSustainedApproachDurationSeconds() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeEnabledStates() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartReadoutType() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedReadoutType() }
            confirmVerified(thresholdsRepository, vehicleApproachPreferencesRepository)
        }

    @Test
    fun `onResetLateralThreshold を呼ぶと lateralThresholdMeters がデフォルト値に戻る`() =
        runTest {
            val lateralFlow = MutableStateFlow(5.0)
            every { thresholdsRepository.observeLateralThresholdMeters() } returns lateralFlow
            every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns MutableStateFlow(1.0)
            every { thresholdsRepository.observeSustainedApproachDurationSeconds() } returns MutableStateFlow(4)
            every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(true)
            every { vehicleApproachPreferencesRepository.observeEnabledStates() } returns
                MutableStateFlow(
                    mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to true),
                )
            every { vehicleApproachPreferencesRepository.observeStartReadoutType() } returns
                MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
            every { vehicleApproachPreferencesRepository.observeSustainedReadoutType() } returns
                MutableStateFlow(VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT)
            coEvery {
                thresholdsRepository.saveLateralThresholdMeters(
                    LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT,
                )
            } answers {
                lateralFlow.update {
                    LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT
                }
            }
            val viewModel = createViewModel()

            viewModel.onResetLateralThreshold()

            assertEquals(
                LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT,
                viewModel.uiState.first().lateralThresholdMeters,
            )
            coVerify(exactly = 1) {
                thresholdsRepository.saveLateralThresholdMeters(
                    LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT,
                )
            }
            verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeSustainedApproachDurationSeconds() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeEnabledStates() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartReadoutType() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedReadoutType() }
            confirmVerified(thresholdsRepository, vehicleApproachPreferencesRepository)
        }

    @Test
    fun `onSustainedApproachDurationSecondsChanged を呼ぶと UiState の sustainedApproachDurationSeconds が更新される`() =
        runTest {
            val sustainedDurationFlow = MutableStateFlow(4)
            every { thresholdsRepository.observeLateralThresholdMeters() } returns MutableStateFlow(5.0)
            every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns MutableStateFlow(1.0)
            every { thresholdsRepository.observeSustainedApproachDurationSeconds() } returns sustainedDurationFlow
            every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(true)
            every { vehicleApproachPreferencesRepository.observeEnabledStates() } returns
                MutableStateFlow(
                    mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to true),
                )
            every { vehicleApproachPreferencesRepository.observeStartReadoutType() } returns
                MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
            every { vehicleApproachPreferencesRepository.observeSustainedReadoutType() } returns
                MutableStateFlow(VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT)
            coEvery { thresholdsRepository.saveSustainedApproachDurationSeconds(8) } answers {
                sustainedDurationFlow.update { 8 }
            }
            val viewModel = createViewModel()

            viewModel.onSustainedApproachDurationSecondsChanged(8)

            assertEquals(8, viewModel.uiState.first().sustainedApproachDurationSeconds)
            coVerify(exactly = 1) { thresholdsRepository.saveSustainedApproachDurationSeconds(8) }
            verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeSustainedApproachDurationSeconds() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeEnabledStates() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartReadoutType() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedReadoutType() }
            confirmVerified(thresholdsRepository, vehicleApproachPreferencesRepository)
        }

    @Test
    fun `onResetSustainedApproachDurationSeconds を呼ぶと sustainedApproachDurationSeconds がデフォルト値に戻る`() =
        runTest {
            val sustainedDurationFlow = MutableStateFlow(8)
            every { thresholdsRepository.observeLateralThresholdMeters() } returns MutableStateFlow(5.0)
            every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns MutableStateFlow(1.0)
            every { thresholdsRepository.observeSustainedApproachDurationSeconds() } returns sustainedDurationFlow
            every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(true)
            every { vehicleApproachPreferencesRepository.observeEnabledStates() } returns
                MutableStateFlow(
                    mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to true),
                )
            every { vehicleApproachPreferencesRepository.observeStartReadoutType() } returns
                MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
            every { vehicleApproachPreferencesRepository.observeSustainedReadoutType() } returns
                MutableStateFlow(VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT)
            coEvery {
                thresholdsRepository.saveSustainedApproachDurationSeconds(
                    LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT,
                )
            } answers {
                sustainedDurationFlow.update { LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT }
            }
            val viewModel = createViewModel()

            viewModel.onResetSustainedApproachDurationSeconds()

            assertEquals(
                LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT,
                viewModel.uiState.first().sustainedApproachDurationSeconds,
            )
            coVerify(exactly = 1) {
                thresholdsRepository.saveSustainedApproachDurationSeconds(
                    LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT,
                )
            }
            verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeSustainedApproachDurationSeconds() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeEnabledStates() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartReadoutType() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedReadoutType() }
            confirmVerified(thresholdsRepository, vehicleApproachPreferencesRepository)
        }

    @Test
    fun `onStartReadoutPreviewClicked を呼ぶと CarLeft の後に CarRight がキュー再生される`() {
        every { thresholdsRepository.observeLateralThresholdMeters() } returns MutableStateFlow(5.0)
        every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns MutableStateFlow(1.0)
        every { thresholdsRepository.observeSustainedApproachDurationSeconds() } returns MutableStateFlow(4)
        every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(true)
        every { vehicleApproachPreferencesRepository.observeEnabledStates() } returns
            MutableStateFlow(
                mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to true),
            )
        every { vehicleApproachPreferencesRepository.observeStartReadoutType() } returns
            MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
        every { vehicleApproachPreferencesRepository.observeSustainedReadoutType() } returns
            MutableStateFlow(VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT)
        every { ttsEngine.speak(SpeechEvent.CarLeft, false) } returns Unit
        every { ttsEngine.speak(SpeechEvent.CarRight, true) } returns Unit
        val viewModel = createViewModel()

        viewModel.onStartReadoutPreviewClicked()

        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.CarLeft, false) }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.CarRight, true) }
        verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
        verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
        verify(exactly = 1) { thresholdsRepository.observeSustainedApproachDurationSeconds() }
        verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
        verify(exactly = 1) { vehicleApproachPreferencesRepository.observeEnabledStates() }
        verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartReadoutType() }
        verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedReadoutType() }
        confirmVerified(ttsEngine, thresholdsRepository, vehicleApproachPreferencesRepository)
    }
}
