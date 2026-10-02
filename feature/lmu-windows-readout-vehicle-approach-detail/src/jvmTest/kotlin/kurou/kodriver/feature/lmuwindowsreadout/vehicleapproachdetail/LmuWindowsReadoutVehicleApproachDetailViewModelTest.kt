@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
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
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
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
class LmuWindowsReadoutVehicleApproachDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val thresholdsRepository: LmuWindowsVehicleApproachThresholdsPreferencesRepository = mockk()

    private val vehicleApproachPreferencesRepository: LmuWindowsVehicleApproachPreferencesRepository = mockk()

    private val volumes: SoundVolumePreferencesRepository = mockk()
    private val ttsEngine: TextToSpeechEngine = mockk()
    private val textToSpeechRepository: TextToSpeechRepository = mockk()

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
                ),
            observeEnabledStates =
                ObserveLmuWindowsVehicleApproachEnabledStatesUseCase(
                    vehicleApproachPreferencesRepository,
                ),
            saveEnabledState = SaveLmuWindowsVehicleApproachEnabledStateUseCase(vehicleApproachPreferencesRepository),
            observeSustainedLeftText =
                ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase(vehicleApproachPreferencesRepository),
            observeSustainedRightText =
                ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase(vehicleApproachPreferencesRepository),
            startReadout =
                StartReadoutUseCases(
                    speakText = SpeakTextUseCase(textToSpeechRepository),
                    playStartSoundForKey = PlayStartSoundForKeyUseCase(ttsEngine),
                    checkTextToSpeechAvailable = CheckTextToSpeechAvailableUseCase(textToSpeechRepository),
                    observeSoundVolume = ObserveSoundVolumeUseCase(volumes),
                    saveLeftText =
                        SaveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase(
                            vehicleApproachPreferencesRepository,
                        ),
                    saveRightText =
                        SaveLmuWindowsVehicleApproachStartRightReadoutTextUseCase(
                            vehicleApproachPreferencesRepository,
                        ),
                ),
        )

    @Test
    fun `初期状態はリポジトリのデフォルト値を反映した UiState を返す`() =
        runTest {
            val sustainedLeft = MutableStateFlow("左継続")
            val sustainedRight = MutableStateFlow("右継続")
            val lateralFlow = MutableStateFlow(5.0)
            val longitudinalFlow = MutableStateFlow(5.0)
            val skipFirstLapFlow = MutableStateFlow(true)
            val startReadoutEnabledFlow = MutableStateFlow(true)

            every { thresholdsRepository.observeLateralThresholdMeters() } returns lateralFlow
            every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns longitudinalFlow
            every { thresholdsRepository.observeSustainedApproachDurationSeconds() } returns MutableStateFlow(4)
            every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns skipFirstLapFlow
            every { vehicleApproachPreferencesRepository.observeEnabledStates() } returns
                startReadoutEnabledFlow.map {
                    mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to it)
                }
            every { vehicleApproachPreferencesRepository.observeStartLeftReadoutText() } returns
                MutableStateFlow("左注意")
            every { vehicleApproachPreferencesRepository.observeStartRightReadoutText() } returns
                MutableStateFlow("右注意")
            every { vehicleApproachPreferencesRepository.observeSustainedLeftReadoutText() } returns
                sustainedLeft
            every { vehicleApproachPreferencesRepository.observeSustainedRightReadoutText() } returns
                sustainedRight
            coEvery { textToSpeechRepository.isAvailable() } returns true
            val viewModel = createViewModel()

            assertEquals(
                LmuWindowsReadoutVehicleApproachDetailUiState(
                    lateralThresholdMeters = 5.0,
                    longitudinalThresholdMeters = 5.0,
                    sustainedApproachDurationSeconds = 4,
                    skipFirstLap = true,
                    startReadoutEnabled = true,
                    isTextToSpeechAvailable = true,
                    startLeftText = "左注意",
                    startRightText = "右注意",
                    sustainedLeftText = "左継続",
                    sustainedRightText = "右継続",
                ),
                viewModel.uiState.first(),
            )
            sustainedLeft.update { "左変更" }
            sustainedRight.update { "" }
            assertEquals("左変更", viewModel.uiState.first().sustainedLeftText)
            assertEquals("", viewModel.uiState.first().sustainedRightText)

            verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeSustainedApproachDurationSeconds() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeEnabledStates() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartLeftReadoutText() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartRightReadoutText() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedLeftReadoutText() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedRightReadoutText() }
            coVerify(exactly = 1) { textToSpeechRepository.isAvailable() }
            confirmVerified(textToSpeechRepository, thresholdsRepository, vehicleApproachPreferencesRepository)
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
            every { vehicleApproachPreferencesRepository.observeStartLeftReadoutText() } returns
                MutableStateFlow("カーレフト")
            every { vehicleApproachPreferencesRepository.observeStartRightReadoutText() } returns
                MutableStateFlow("カーライト")
            every { vehicleApproachPreferencesRepository.observeSustainedLeftReadoutText() } returns
                MutableStateFlow("キープライト")
            every { vehicleApproachPreferencesRepository.observeSustainedRightReadoutText() } returns
                MutableStateFlow("キープレフト")
            coEvery { vehicleApproachPreferencesRepository.saveSkipFirstLap(true) } answers {
                skipFirstLapFlow.update { true }
            }
            coEvery { textToSpeechRepository.isAvailable() } returns true
            val viewModel = createViewModel()

            viewModel.onSkipFirstLapChanged(true)

            assertEquals(true, viewModel.uiState.first().skipFirstLap)
            coVerify(exactly = 1) { vehicleApproachPreferencesRepository.saveSkipFirstLap(true) }
            verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeSustainedApproachDurationSeconds() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeEnabledStates() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartLeftReadoutText() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartRightReadoutText() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedLeftReadoutText() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedRightReadoutText() }
            coVerify(exactly = 1) { textToSpeechRepository.isAvailable() }
            confirmVerified(textToSpeechRepository, vehicleApproachPreferencesRepository, thresholdsRepository)
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
            every { vehicleApproachPreferencesRepository.observeStartLeftReadoutText() } returns
                MutableStateFlow("カーレフト")
            every { vehicleApproachPreferencesRepository.observeStartRightReadoutText() } returns
                MutableStateFlow("カーライト")
            every { vehicleApproachPreferencesRepository.observeSustainedLeftReadoutText() } returns
                MutableStateFlow("キープライト")
            every { vehicleApproachPreferencesRepository.observeSustainedRightReadoutText() } returns
                MutableStateFlow("キープレフト")
            coEvery {
                vehicleApproachPreferencesRepository.saveEnabledState(
                    ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout,
                    false,
                )
            } answers {
                startReadoutEnabledFlow.update { false }
            }
            coEvery { textToSpeechRepository.isAvailable() } returns true
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
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartLeftReadoutText() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartRightReadoutText() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedLeftReadoutText() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedRightReadoutText() }
            coVerify(exactly = 1) { textToSpeechRepository.isAvailable() }
            confirmVerified(textToSpeechRepository, vehicleApproachPreferencesRepository, thresholdsRepository)
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
            every { vehicleApproachPreferencesRepository.observeStartLeftReadoutText() } returns
                MutableStateFlow("カーレフト")
            every { vehicleApproachPreferencesRepository.observeStartRightReadoutText() } returns
                MutableStateFlow("カーライト")
            every { vehicleApproachPreferencesRepository.observeSustainedLeftReadoutText() } returns
                MutableStateFlow("キープライト")
            every { vehicleApproachPreferencesRepository.observeSustainedRightReadoutText() } returns
                MutableStateFlow("キープレフト")
            coEvery {
                vehicleApproachPreferencesRepository.saveEnabledState(
                    ReadoutItemKey.LmuWindows.VehicleApproach.Sustained,
                    true,
                )
            } answers {
                sustainedReadoutEnabledFlow.update { true }
            }
            coEvery { textToSpeechRepository.isAvailable() } returns true
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
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartLeftReadoutText() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartRightReadoutText() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedLeftReadoutText() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedRightReadoutText() }
            coVerify(exactly = 1) { textToSpeechRepository.isAvailable() }
            confirmVerified(textToSpeechRepository, vehicleApproachPreferencesRepository, thresholdsRepository)
        }

    @Test
    fun `TTS利用不可では試聴せずUiStateへ反映する`() =
        runTest {
            every { thresholdsRepository.observeLateralThresholdMeters() } returns MutableStateFlow(5.0)
            every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns MutableStateFlow(1.0)
            every { thresholdsRepository.observeSustainedApproachDurationSeconds() } returns MutableStateFlow(4)
            every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(true)
            every { vehicleApproachPreferencesRepository.observeEnabledStates() } returns
                MutableStateFlow(
                    mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to true),
                )
            every { vehicleApproachPreferencesRepository.observeStartLeftReadoutText() } returns
                MutableStateFlow("カーレフト")
            every { vehicleApproachPreferencesRepository.observeStartRightReadoutText() } returns
                MutableStateFlow("カーライト")
            every { vehicleApproachPreferencesRepository.observeSustainedLeftReadoutText() } returns
                MutableStateFlow("キープライト")
            every { vehicleApproachPreferencesRepository.observeSustainedRightReadoutText() } returns
                MutableStateFlow("キープレフト")
            coEvery { textToSpeechRepository.isAvailable() } returns false
            val viewModel = createViewModel()
            assertEquals(false, viewModel.uiState.first().isTextToSpeechAvailable)
            viewModel.onStartLeftTextPreviewClicked("注意")
            viewModel.onStartRightTextPreviewClicked("注意")
            coVerify(exactly = 0) { ttsEngine.playStartSound(ReadoutItemKey.LmuWindows.VehicleApproach.Root) }
            coVerify(exactly = 0) { textToSpeechRepository.speak("注意", false, 60) }
            verify(exactly = 0) { volumes.volume() }
            verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeSustainedApproachDurationSeconds() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeEnabledStates() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartLeftReadoutText() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartRightReadoutText() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedLeftReadoutText() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedRightReadoutText() }
            coVerify(exactly = 1) { textToSpeechRepository.isAvailable() }
            confirmVerified(
                textToSpeechRepository,
                ttsEngine,
                volumes,
                thresholdsRepository,
                vehicleApproachPreferencesRepository,
            )
        }

    @Test
    fun `左右文言の変更を保存しUiStateへ反映する`() =
        runTest {
            every { thresholdsRepository.observeLateralThresholdMeters() } returns MutableStateFlow(5.0)
            every { thresholdsRepository.observeLongitudinalThresholdMeters() } returns MutableStateFlow(1.0)
            val left = MutableStateFlow("カーレフト")
            val right = MutableStateFlow("カーライト")
            every { thresholdsRepository.observeSustainedApproachDurationSeconds() } returns MutableStateFlow(4)
            every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(true)
            every { vehicleApproachPreferencesRepository.observeEnabledStates() } returns
                MutableStateFlow(
                    mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to true),
                )
            every { vehicleApproachPreferencesRepository.observeStartLeftReadoutText() } returns left
            every { vehicleApproachPreferencesRepository.observeStartRightReadoutText() } returns right
            every { vehicleApproachPreferencesRepository.observeSustainedLeftReadoutText() } returns
                MutableStateFlow("キープライト")
            every { vehicleApproachPreferencesRepository.observeSustainedRightReadoutText() } returns
                MutableStateFlow("キープレフト")
            coEvery { textToSpeechRepository.isAvailable() } returns true
            coEvery { vehicleApproachPreferencesRepository.saveStartLeftReadoutText("左注意") } answers
                { left.update { "左注意" } }
            coEvery { vehicleApproachPreferencesRepository.saveStartRightReadoutText("") } answers
                { right.update { "" } }
            val viewModel = createViewModel()
            viewModel.onStartLeftTextChanged("左注意")
            viewModel.onStartRightTextChanged("")
            assertEquals("左注意", viewModel.uiState.first().startLeftText)
            assertEquals("", viewModel.uiState.first().startRightText)
            assertEquals(true, viewModel.uiState.first().isTextToSpeechAvailable)
            coVerify(exactly = 1) { vehicleApproachPreferencesRepository.saveStartLeftReadoutText("左注意") }
            coVerify(exactly = 1) { vehicleApproachPreferencesRepository.saveStartRightReadoutText("") }
            verify(exactly = 1) { thresholdsRepository.observeLateralThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { thresholdsRepository.observeSustainedApproachDurationSeconds() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSkipFirstLap() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeEnabledStates() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartLeftReadoutText() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeStartRightReadoutText() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedLeftReadoutText() }
            verify(exactly = 1) { vehicleApproachPreferencesRepository.observeSustainedRightReadoutText() }
            coVerify(exactly = 1) { textToSpeechRepository.isAvailable() }
            confirmVerified(textToSpeechRepository, thresholdsRepository, vehicleApproachPreferencesRepository)
        }
}
