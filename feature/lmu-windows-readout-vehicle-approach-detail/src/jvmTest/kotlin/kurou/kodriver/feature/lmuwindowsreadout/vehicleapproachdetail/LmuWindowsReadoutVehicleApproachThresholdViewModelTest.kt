package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachReadoutTextPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kurou.kodriver.domain.repository.TextToSpeechRepository
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.LmuWindowsVehicleApproachPreferencesUseCases
import kurou.kodriver.domain.usecase.LmuWindowsVehicleApproachThresholdsUseCases
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceSpeedUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleApproachEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleApproachStartRightReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutVehicleApproachThresholdViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val thresholdsRepository: LmuWindowsVehicleApproachThresholdsPreferencesRepository = mockk()

    private val vehicleApproachPreferencesRepository: LmuWindowsVehicleApproachPreferencesRepository = mockk()
    private val vehicleApproachReadoutTextPreferencesRepository:
        LmuWindowsVehicleApproachReadoutTextPreferencesRepository = mockk(relaxUnitFun = true)

    private val volumes: SoundVolumePreferencesRepository = mockk()
    private val ttsEngine: TextToSpeechEngine = mockk()
    private val textToSpeechRepository: TextToSpeechRepository = mockk()
    private val observeVoice: ObserveVoiceUseCase = mockk()
    private val observeVoiceSpeed: ObserveVoiceSpeedUseCase = mockk()

    @BeforeTest
    fun setUp() {
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
                    vehicleApproachReadoutTextPreferencesRepository,
                ),
            observeEnabledStates =
                ObserveLmuWindowsVehicleApproachEnabledStatesUseCase(
                    vehicleApproachPreferencesRepository,
                ),
            saveEnabledState = SaveLmuWindowsVehicleApproachEnabledStateUseCase(vehicleApproachPreferencesRepository),
            observeSustainedLeftText =
                ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase(
                    vehicleApproachReadoutTextPreferencesRepository,
                ),
            observeSustainedRightText =
                ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase(
                    vehicleApproachReadoutTextPreferencesRepository,
                ),
            startReadout =
                StartReadoutUseCases(
                    speakText = SpeakTextUseCase(textToSpeechRepository, observeVoice, observeVoiceSpeed),
                    playStartSoundForKey = PlayStartSoundForKeyUseCase(ttsEngine),
                    checkTextToSpeechAvailable = CheckTextToSpeechAvailableUseCase(textToSpeechRepository),
                    observeSoundVolume = ObserveSoundVolumeUseCase(volumes),
                    saveLeftText =
                        SaveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase(
                            vehicleApproachReadoutTextPreferencesRepository,
                        ),
                    saveRightText =
                        SaveLmuWindowsVehicleApproachStartRightReadoutTextUseCase(
                            vehicleApproachReadoutTextPreferencesRepository,
                        ),
                ),
        )

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
            every { vehicleApproachReadoutTextPreferencesRepository.observeStartLeftReadoutText() } returns
                MutableStateFlow("カーレフト")
            every { vehicleApproachReadoutTextPreferencesRepository.observeStartRightReadoutText() } returns
                MutableStateFlow("カーライト")
            every { vehicleApproachReadoutTextPreferencesRepository.observeSustainedLeftReadoutText() } returns
                MutableStateFlow("キープライト")
            every { vehicleApproachReadoutTextPreferencesRepository.observeSustainedRightReadoutText() } returns
                MutableStateFlow("キープレフト")
            coEvery { thresholdsRepository.saveLateralThresholdMeters(3.5) } answers {
                lateralFlow.update { 3.5 }
            }
            coEvery { textToSpeechRepository.isAvailable() } returns true
            val viewModel = createViewModel()

            viewModel.onLateralThresholdChanged(3.5)

            assertEquals(3.5, viewModel.uiState.first().lateralThresholdMeters)
            coVerify(exactly = 1) { thresholdsRepository.saveLateralThresholdMeters(3.5) }
            verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeSustainedApproachDurationSeconds() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeEnabledStates() }
            verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeStartLeftReadoutText() }
            verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeStartRightReadoutText() }
            verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeSustainedLeftReadoutText() }
            verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeSustainedRightReadoutText() }
            coVerify(exactly = 1) { textToSpeechRepository.isAvailable() }
            confirmVerified(
                observeVoice,
                observeVoiceSpeed,
                textToSpeechRepository,
                thresholdsRepository,
                vehicleApproachPreferencesRepository,
                vehicleApproachReadoutTextPreferencesRepository,
            )
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
            every { vehicleApproachReadoutTextPreferencesRepository.observeStartLeftReadoutText() } returns
                MutableStateFlow("カーレフト")
            every { vehicleApproachReadoutTextPreferencesRepository.observeStartRightReadoutText() } returns
                MutableStateFlow("カーライト")
            every { vehicleApproachReadoutTextPreferencesRepository.observeSustainedLeftReadoutText() } returns
                MutableStateFlow("キープライト")
            every { vehicleApproachReadoutTextPreferencesRepository.observeSustainedRightReadoutText() } returns
                MutableStateFlow("キープレフト")
            coEvery { thresholdsRepository.saveLongitudinalThresholdMeters(15.0) } answers {
                longitudinalFlow.update { 15.0 }
            }
            coEvery { textToSpeechRepository.isAvailable() } returns true
            val viewModel = createViewModel()

            viewModel.onLongitudinalThresholdChanged(15.0)

            assertEquals(15.0, viewModel.uiState.first().longitudinalThresholdMeters)
            coVerify(exactly = 1) { thresholdsRepository.saveLongitudinalThresholdMeters(15.0) }
            verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeSustainedApproachDurationSeconds() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeEnabledStates() }
            verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeStartLeftReadoutText() }
            verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeStartRightReadoutText() }
            verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeSustainedLeftReadoutText() }
            verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeSustainedRightReadoutText() }
            coVerify(exactly = 1) { textToSpeechRepository.isAvailable() }
            confirmVerified(
                observeVoice,
                observeVoiceSpeed,
                textToSpeechRepository,
                thresholdsRepository,
                vehicleApproachPreferencesRepository,
                vehicleApproachReadoutTextPreferencesRepository,
            )
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
            every { vehicleApproachReadoutTextPreferencesRepository.observeStartLeftReadoutText() } returns
                MutableStateFlow("カーレフト")
            every { vehicleApproachReadoutTextPreferencesRepository.observeStartRightReadoutText() } returns
                MutableStateFlow("カーライト")
            every { vehicleApproachReadoutTextPreferencesRepository.observeSustainedLeftReadoutText() } returns
                MutableStateFlow("キープライト")
            every { vehicleApproachReadoutTextPreferencesRepository.observeSustainedRightReadoutText() } returns
                MutableStateFlow("キープレフト")
            coEvery {
                thresholdsRepository.saveLongitudinalThresholdMeters(
                    LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT,
                )
            } answers {
                longitudinalFlow.update {
                    LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT
                }
            }
            coEvery { textToSpeechRepository.isAvailable() } returns true
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
            verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeStartLeftReadoutText() }
            verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeStartRightReadoutText() }
            verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeSustainedLeftReadoutText() }
            verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeSustainedRightReadoutText() }
            coVerify(exactly = 1) { textToSpeechRepository.isAvailable() }
            confirmVerified(
                observeVoice,
                observeVoiceSpeed,
                textToSpeechRepository,
                thresholdsRepository,
                vehicleApproachPreferencesRepository,
                vehicleApproachReadoutTextPreferencesRepository,
            )
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
            every { vehicleApproachReadoutTextPreferencesRepository.observeStartLeftReadoutText() } returns
                MutableStateFlow("カーレフト")
            every { vehicleApproachReadoutTextPreferencesRepository.observeStartRightReadoutText() } returns
                MutableStateFlow("カーライト")
            every { vehicleApproachReadoutTextPreferencesRepository.observeSustainedLeftReadoutText() } returns
                MutableStateFlow("キープライト")
            every { vehicleApproachReadoutTextPreferencesRepository.observeSustainedRightReadoutText() } returns
                MutableStateFlow("キープレフト")
            coEvery {
                thresholdsRepository.saveLateralThresholdMeters(
                    LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT,
                )
            } answers {
                lateralFlow.update {
                    LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT
                }
            }
            coEvery { textToSpeechRepository.isAvailable() } returns true
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
            verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeStartLeftReadoutText() }
            verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeStartRightReadoutText() }
            verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeSustainedLeftReadoutText() }
            verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeSustainedRightReadoutText() }
            coVerify(exactly = 1) { textToSpeechRepository.isAvailable() }
            confirmVerified(
                observeVoice,
                observeVoiceSpeed,
                textToSpeechRepository,
                thresholdsRepository,
                vehicleApproachPreferencesRepository,
                vehicleApproachReadoutTextPreferencesRepository,
            )
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
            every { vehicleApproachReadoutTextPreferencesRepository.observeStartLeftReadoutText() } returns
                MutableStateFlow("カーレフト")
            every { vehicleApproachReadoutTextPreferencesRepository.observeStartRightReadoutText() } returns
                MutableStateFlow("カーライト")
            every { vehicleApproachReadoutTextPreferencesRepository.observeSustainedLeftReadoutText() } returns
                MutableStateFlow("キープライト")
            every { vehicleApproachReadoutTextPreferencesRepository.observeSustainedRightReadoutText() } returns
                MutableStateFlow("キープレフト")
            coEvery { thresholdsRepository.saveSustainedApproachDurationSeconds(8) } answers {
                sustainedDurationFlow.update { 8 }
            }
            coEvery { textToSpeechRepository.isAvailable() } returns true
            val viewModel = createViewModel()

            viewModel.onSustainedApproachDurationSecondsChanged(8)

            assertEquals(8, viewModel.uiState.first().sustainedApproachDurationSeconds)
            coVerify(exactly = 1) { thresholdsRepository.saveSustainedApproachDurationSeconds(8) }
            verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeSustainedApproachDurationSeconds() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeEnabledStates() }
            verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeStartLeftReadoutText() }
            verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeStartRightReadoutText() }
            verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeSustainedLeftReadoutText() }
            verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeSustainedRightReadoutText() }
            coVerify(exactly = 1) { textToSpeechRepository.isAvailable() }
            confirmVerified(
                observeVoice,
                observeVoiceSpeed,
                textToSpeechRepository,
                thresholdsRepository,
                vehicleApproachPreferencesRepository,
                vehicleApproachReadoutTextPreferencesRepository,
            )
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
            every { vehicleApproachReadoutTextPreferencesRepository.observeStartLeftReadoutText() } returns
                MutableStateFlow("カーレフト")
            every { vehicleApproachReadoutTextPreferencesRepository.observeStartRightReadoutText() } returns
                MutableStateFlow("カーライト")
            every { vehicleApproachReadoutTextPreferencesRepository.observeSustainedLeftReadoutText() } returns
                MutableStateFlow("キープライト")
            every { vehicleApproachReadoutTextPreferencesRepository.observeSustainedRightReadoutText() } returns
                MutableStateFlow("キープレフト")
            coEvery {
                thresholdsRepository.saveSustainedApproachDurationSeconds(
                    LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT,
                )
            } answers {
                sustainedDurationFlow.update { LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT }
            }
            coEvery { textToSpeechRepository.isAvailable() } returns true
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
            verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeStartLeftReadoutText() }
            verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeStartRightReadoutText() }
            verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeSustainedLeftReadoutText() }
            verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeSustainedRightReadoutText() }
            coVerify(exactly = 1) { textToSpeechRepository.isAvailable() }
            confirmVerified(
                observeVoice,
                observeVoiceSpeed,
                textToSpeechRepository,
                thresholdsRepository,
                vehicleApproachPreferencesRepository,
                vehicleApproachReadoutTextPreferencesRepository,
            )
        }

    @Test
    fun `空白文言は試聴しない`() {
        every { thresholdsRepository.observeLateralThresholdMeters() } returns MutableStateFlow(5.0)
        every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns MutableStateFlow(1.0)
        every { thresholdsRepository.observeSustainedApproachDurationSeconds() } returns MutableStateFlow(4)
        every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(true)
        every { vehicleApproachPreferencesRepository.observeEnabledStates() } returns
            MutableStateFlow(
                mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to true),
            )
        every { vehicleApproachReadoutTextPreferencesRepository.observeStartLeftReadoutText() } returns
            MutableStateFlow("カーレフト")
        every { vehicleApproachReadoutTextPreferencesRepository.observeStartRightReadoutText() } returns
            MutableStateFlow("カーライト")
        every { vehicleApproachReadoutTextPreferencesRepository.observeSustainedLeftReadoutText() } returns
            MutableStateFlow("キープライト")
        every { vehicleApproachReadoutTextPreferencesRepository.observeSustainedRightReadoutText() } returns
            MutableStateFlow("キープレフト")
        coEvery { textToSpeechRepository.isAvailable() } returns true
        val viewModel = createViewModel()
        viewModel.onStartLeftTextPreviewClicked(" ")
        viewModel.onStartRightTextPreviewClicked(" ")
        coVerify(exactly = 0) { ttsEngine.playStartSound(ReadoutItemKey.LmuWindows.VehicleApproach.Root) }
        coVerify(exactly = 0) { textToSpeechRepository.speak(" ", false, 60, VOICE_ID_UNSPECIFIED, 1.0f) }
        verify(exactly = 0) { volumes.volume() }
        verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
        verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
        verify(exactly = 1) { thresholdsRepository.observeSustainedApproachDurationSeconds() }
        verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
        verify(exactly = 1) { vehicleApproachPreferencesRepository.observeEnabledStates() }
        verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeStartLeftReadoutText() }
        verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeStartRightReadoutText() }
        verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeSustainedLeftReadoutText() }
        verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeSustainedRightReadoutText() }
        coVerify(exactly = 1) { textToSpeechRepository.isAvailable() }
        confirmVerified(
            observeVoice,
            observeVoiceSpeed,
            textToSpeechRepository,
            ttsEngine,
            volumes,
            thresholdsRepository,
            vehicleApproachPreferencesRepository,
            vehicleApproachReadoutTextPreferencesRepository,
        )
    }

    @Test
    fun `音量ゼロでは試聴しない`() {
        every { thresholdsRepository.observeLateralThresholdMeters() } returns MutableStateFlow(5.0)
        every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns MutableStateFlow(1.0)
        every { thresholdsRepository.observeSustainedApproachDurationSeconds() } returns MutableStateFlow(4)
        every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(true)
        every { vehicleApproachPreferencesRepository.observeEnabledStates() } returns
            MutableStateFlow(
                mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to true),
            )
        every { vehicleApproachReadoutTextPreferencesRepository.observeStartLeftReadoutText() } returns
            MutableStateFlow("カーレフト")
        every { vehicleApproachReadoutTextPreferencesRepository.observeStartRightReadoutText() } returns
            MutableStateFlow("カーライト")
        every { vehicleApproachReadoutTextPreferencesRepository.observeSustainedLeftReadoutText() } returns
            MutableStateFlow("キープライト")
        every { vehicleApproachReadoutTextPreferencesRepository.observeSustainedRightReadoutText() } returns
            MutableStateFlow("キープレフト")
        coEvery { textToSpeechRepository.isAvailable() } returns true
        every { volumes.volume() } returns MutableStateFlow(0)
        val viewModel = createViewModel()
        viewModel.onStartLeftTextPreviewClicked("注意")
        viewModel.onStartRightTextPreviewClicked("注意")
        coVerify(exactly = 0) { ttsEngine.playStartSound(ReadoutItemKey.LmuWindows.VehicleApproach.Root) }
        coVerify(exactly = 0) { textToSpeechRepository.speak("注意", false, 0, VOICE_ID_UNSPECIFIED, 1.0f) }
        verify(exactly = 2) { volumes.volume() }
        verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
        verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
        verify(exactly = 1) { thresholdsRepository.observeSustainedApproachDurationSeconds() }
        verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
        verify(exactly = 1) { vehicleApproachPreferencesRepository.observeEnabledStates() }
        verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeStartLeftReadoutText() }
        verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeStartRightReadoutText() }
        verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeSustainedLeftReadoutText() }
        verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeSustainedRightReadoutText() }
        coVerify(exactly = 1) { textToSpeechRepository.isAvailable() }
        confirmVerified(
            observeVoice,
            observeVoiceSpeed,
            textToSpeechRepository,
            ttsEngine,
            volumes,
            thresholdsRepository,
            vehicleApproachPreferencesRepository,
            vehicleApproachReadoutTextPreferencesRepository,
        )
    }

    @Test
    fun `開始音の後に指定音量で左右を試聴する`() {
        every { thresholdsRepository.observeLateralThresholdMeters() } returns MutableStateFlow(5.0)
        every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns MutableStateFlow(1.0)
        every { thresholdsRepository.observeSustainedApproachDurationSeconds() } returns MutableStateFlow(4)
        every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(true)
        every { vehicleApproachPreferencesRepository.observeEnabledStates() } returns
            MutableStateFlow(
                mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to true),
            )
        every { vehicleApproachReadoutTextPreferencesRepository.observeStartLeftReadoutText() } returns
            MutableStateFlow("カーレフト")
        every { vehicleApproachReadoutTextPreferencesRepository.observeStartRightReadoutText() } returns
            MutableStateFlow("カーライト")
        every { vehicleApproachReadoutTextPreferencesRepository.observeSustainedLeftReadoutText() } returns
            MutableStateFlow("キープライト")
        every { vehicleApproachReadoutTextPreferencesRepository.observeSustainedRightReadoutText() } returns
            MutableStateFlow("キープレフト")
        coEvery { textToSpeechRepository.isAvailable() } returns true
        every { volumes.volume() } returns MutableStateFlow(60)
        coEvery { ttsEngine.playStartSound(ReadoutItemKey.LmuWindows.VehicleApproach.Root) } returns Unit
        every { observeVoiceSpeed() } returns flowOf(1.0f)
        every { observeVoice() } returns flowOf("voice-a")
        coEvery { textToSpeechRepository.speak("注意", false, 60, "voice-a", 1.0f) } returns Unit
        val viewModel = createViewModel()
        viewModel.onStartLeftTextPreviewClicked("注意")
        viewModel.onStartRightTextPreviewClicked("注意")
        coVerify(exactly = 2) { ttsEngine.playStartSound(ReadoutItemKey.LmuWindows.VehicleApproach.Root) }
        verify(exactly = 2) { observeVoice() }
        verify(exactly = 2) { observeVoiceSpeed() }
        coVerify(exactly = 2) { textToSpeechRepository.speak("注意", false, 60, "voice-a", 1.0f) }
        verify(exactly = 2) { volumes.volume() }
        coVerifyOrder {
            ttsEngine.playStartSound(ReadoutItemKey.LmuWindows.VehicleApproach.Root)
            textToSpeechRepository.speak("注意", false, 60, "voice-a", 1.0f)
            ttsEngine.playStartSound(ReadoutItemKey.LmuWindows.VehicleApproach.Root)
            textToSpeechRepository.speak("注意", false, 60, "voice-a", 1.0f)
        }
        verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
        verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
        verify(exactly = 1) { thresholdsRepository.observeSustainedApproachDurationSeconds() }
        verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
        verify(exactly = 1) { vehicleApproachPreferencesRepository.observeEnabledStates() }
        verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeStartLeftReadoutText() }
        verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeStartRightReadoutText() }
        verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeSustainedLeftReadoutText() }
        verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeSustainedRightReadoutText() }
        coVerify(exactly = 1) { textToSpeechRepository.isAvailable() }
        confirmVerified(
            observeVoice,
            observeVoiceSpeed,
            textToSpeechRepository,
            ttsEngine,
            volumes,
            thresholdsRepository,
            vehicleApproachPreferencesRepository,
            vehicleApproachReadoutTextPreferencesRepository,
        )
    }

    @Test
    fun `継続文言も開始音の後に指定音量で左右を試聴する`() {
        every { thresholdsRepository.observeLateralThresholdMeters() } returns MutableStateFlow(5.0)
        every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns MutableStateFlow(1.0)
        every { thresholdsRepository.observeSustainedApproachDurationSeconds() } returns MutableStateFlow(4)
        every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(true)
        every { vehicleApproachPreferencesRepository.observeEnabledStates() } returns
            MutableStateFlow(
                mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to true),
            )
        every { vehicleApproachReadoutTextPreferencesRepository.observeStartLeftReadoutText() } returns
            MutableStateFlow("カーレフト")
        every { vehicleApproachReadoutTextPreferencesRepository.observeStartRightReadoutText() } returns
            MutableStateFlow("カーライト")
        every { vehicleApproachReadoutTextPreferencesRepository.observeSustainedLeftReadoutText() } returns
            MutableStateFlow("キープライト")
        every { vehicleApproachReadoutTextPreferencesRepository.observeSustainedRightReadoutText() } returns
            MutableStateFlow("キープレフト")
        coEvery { textToSpeechRepository.isAvailable() } returns true
        every { volumes.volume() } returns MutableStateFlow(60)
        coEvery { ttsEngine.playStartSound(ReadoutItemKey.LmuWindows.VehicleApproach.Root) } returns Unit
        every { observeVoiceSpeed() } returns flowOf(1.0f)
        every { observeVoice() } returns flowOf(VOICE_ID_UNSPECIFIED)
        coEvery { textToSpeechRepository.speak("注意", false, 60, VOICE_ID_UNSPECIFIED, 1.0f) } returns Unit
        val viewModel = createViewModel()
        viewModel.onSustainedLeftTextPreviewClicked("注意")
        viewModel.onSustainedRightTextPreviewClicked("注意")
        coVerify(exactly = 2) { ttsEngine.playStartSound(ReadoutItemKey.LmuWindows.VehicleApproach.Root) }
        verify(exactly = 2) { observeVoice() }
        verify(exactly = 2) { observeVoiceSpeed() }
        coVerify(exactly = 2) { textToSpeechRepository.speak("注意", false, 60, VOICE_ID_UNSPECIFIED, 1.0f) }
        verify(exactly = 2) { volumes.volume() }
        coVerifyOrder {
            ttsEngine.playStartSound(ReadoutItemKey.LmuWindows.VehicleApproach.Root)
            textToSpeechRepository.speak("注意", false, 60, VOICE_ID_UNSPECIFIED, 1.0f)
            ttsEngine.playStartSound(ReadoutItemKey.LmuWindows.VehicleApproach.Root)
            textToSpeechRepository.speak("注意", false, 60, VOICE_ID_UNSPECIFIED, 1.0f)
        }
        verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
        verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
        verify(exactly = 1) { thresholdsRepository.observeSustainedApproachDurationSeconds() }
        verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
        verify(exactly = 1) { vehicleApproachPreferencesRepository.observeEnabledStates() }
        verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeStartLeftReadoutText() }
        verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeStartRightReadoutText() }
        verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeSustainedLeftReadoutText() }
        verify(exactly = 1) { vehicleApproachReadoutTextPreferencesRepository.observeSustainedRightReadoutText() }
        coVerify(exactly = 1) { textToSpeechRepository.isAvailable() }
        confirmVerified(
            observeVoice,
            observeVoiceSpeed,
            textToSpeechRepository,
            ttsEngine,
            volumes,
            thresholdsRepository,
            vehicleApproachPreferencesRepository,
            vehicleApproachReadoutTextPreferencesRepository,
        )
    }
}
