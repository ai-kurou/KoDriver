package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

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
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutTyreTemperatureDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: LmuWindowsTyreTemperaturePreferencesRepository = mockk()

    private val vehicleClassRepository: LmuWindowsVehicleClassTyreTemperaturePreferencesRepository = mockk()

    private val ttsEngine: TextToSpeechEngine = mockk()

    private val observeText: ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase = mockk()
    private val saveText: SaveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase = mockk()
    private val speakText: SpeakTextUseCase = mockk()
    private val playStartSound: PlayStartSoundForKeyUseCase = mockk()
    private val checkAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val observeVolume: ObserveSoundVolumeUseCase = mockk()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        LmuWindowsReadoutTyreTemperatureDetailViewModel(
            tyreTemperatureUseCases =
                TyreTemperatureUseCases(
                    observeEnabledStates = ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase(repository),
                    observeLowWarningPhases = ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(repository),
                    observeVehicleClassHighThreshold =
                        ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase(vehicleClassRepository),
                    observeVehicleClassSelection =
                        ObserveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase(vehicleClassRepository),
                    saveEnabledState = SaveLmuWindowsTyreTemperatureEnabledStateUseCase(repository),
                    saveLowWarningPhases = SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(repository),
                    saveVehicleClassHighThreshold =
                        SaveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase(vehicleClassRepository),
                    saveVehicleClassSelection =
                        SaveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase(vehicleClassRepository),
                ),
            readout =
                TyreTemperatureOverheatReadoutUseCases(
                    observeText,
                    saveText,
                    speakText,
                    playStartSound,
                    checkAvailable,
                    observeVolume,
                ),
            playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
        )

    @Test
    fun `初期状態はリポジトリのデフォルト値を反映したUiStateを返す`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeLowWarningPhases() } returns MutableStateFlow(emptyMap())
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns MutableStateFlow(emptyMap())
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            stubReadout()
            val viewModel = createViewModel()

            assertEquals(
                LmuWindowsReadoutTyreTemperatureDetailUiState(overheatWarningEnabled = true),
                viewModel.uiState.first(),
            )
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeLowWarningPhases() }
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            confirmVerified(repository, vehicleClassRepository)
        }

    @Test
    fun `onOverheatWarningEnabledChangedを呼ぶとuiStateのoverheatWarningEnabledが更新される`() =
        runTest {
            val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { repository.observeEnabledStates() } returns enabledStatesFlow
            every { repository.observeLowWarningPhases() } returns MutableStateFlow(emptyMap())
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns MutableStateFlow(emptyMap())
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            coEvery {
                repository.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, false)
            } answers {
                enabledStatesFlow.update { it + (ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning to false) }
            }
            stubReadout()
            val viewModel = createViewModel()

            viewModel.onOverheatWarningEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().overheatWarningEnabled)
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeLowWarningPhases() }
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, false)
            }
            confirmVerified(repository, vehicleClassRepository)
        }

    @Test
    fun `onLowWarningPreviewClickedを呼ぶとTyreColdイベントが再生される`() {
        every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
        every { repository.observeLowWarningPhases() } returns MutableStateFlow(emptyMap())
        every { vehicleClassRepository.observeHighThresholdCelsius() } returns MutableStateFlow(emptyMap())
        every { vehicleClassRepository.observeSelectedVehicleClass() } returns
            MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
        every { ttsEngine.speak(SpeechEvent.TyreCold, false) } returns Unit
        stubReadout()
        val viewModel = createViewModel()

        viewModel.onLowWarningPreviewClicked()

        verify(exactly = 1) { repository.observeEnabledStates() }
        verify(exactly = 1) { repository.observeLowWarningPhases() }
        verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
        verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.TyreCold, false) }
        confirmVerified(repository, vehicleClassRepository, ttsEngine)
    }

    @Test
    fun `onLowWarningEnabledChangedを呼ぶとuiStateのlowWarningEnabledが更新される`() =
        runTest {
            val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { repository.observeEnabledStates() } returns enabledStatesFlow
            every { repository.observeLowWarningPhases() } returns MutableStateFlow(emptyMap())
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns MutableStateFlow(emptyMap())
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            coEvery {
                repository.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning, false)
            } answers {
                enabledStatesFlow.update { it + (ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning to false) }
            }
            stubReadout()
            val viewModel = createViewModel()

            viewModel.onLowWarningEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().lowWarningEnabled)
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeLowWarningPhases() }
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning, false)
            }
            confirmVerified(repository, vehicleClassRepository)
        }

    @Test
    fun `onLowWarningPhaseToggledで未選択のフェーズを渡すと選択に追加される`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            val lowWarningPhasesFlow =
                MutableStateFlow(
                    mapOf(
                        SessionPhase.GARAGE to false,
                        SessionPhase.WARM_UP to false,
                        SessionPhase.GRID_WALK to false,
                        SessionPhase.FORMATION to false,
                    ),
                )
            every { repository.observeLowWarningPhases() } returns lowWarningPhasesFlow
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns MutableStateFlow(emptyMap())
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            coEvery { repository.saveLowWarningPhases(setOf(SessionPhase.GARAGE)) } answers {
                lowWarningPhasesFlow.update {
                    mapOf(
                        SessionPhase.GARAGE to true,
                        SessionPhase.WARM_UP to false,
                        SessionPhase.GRID_WALK to false,
                        SessionPhase.FORMATION to false,
                    )
                }
            }
            stubReadout()
            val viewModel = createViewModel()
            viewModel.uiState.first()

            viewModel.onLowWarningPhaseToggled(SessionPhase.GARAGE)

            assertEquals(setOf(SessionPhase.GARAGE), viewModel.uiState.first().lowWarningPhases)
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeLowWarningPhases() }
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            coVerify(exactly = 1) { repository.saveLowWarningPhases(setOf(SessionPhase.GARAGE)) }
            confirmVerified(repository, vehicleClassRepository)
        }

    @Test
    fun `onLowWarningPhaseToggledで選択済みのフェーズを渡すと選択から除外される`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            val defaultPhases =
                mapOf(
                    SessionPhase.GARAGE to false,
                    SessionPhase.WARM_UP to true,
                    SessionPhase.GRID_WALK to true,
                    SessionPhase.FORMATION to true,
                )
            val lowWarningPhasesFlow = MutableStateFlow(defaultPhases)
            every { repository.observeLowWarningPhases() } returns lowWarningPhasesFlow
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns MutableStateFlow(emptyMap())
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            coEvery {
                repository.saveLowWarningPhases(setOf(SessionPhase.WARM_UP, SessionPhase.GRID_WALK))
            } answers {
                lowWarningPhasesFlow.update {
                    mapOf(
                        SessionPhase.GARAGE to false,
                        SessionPhase.WARM_UP to true,
                        SessionPhase.GRID_WALK to true,
                        SessionPhase.FORMATION to false,
                    )
                }
            }
            stubReadout()
            val viewModel = createViewModel()
            viewModel.uiState.first()

            viewModel.onLowWarningPhaseToggled(SessionPhase.FORMATION)

            assertEquals(
                setOf(SessionPhase.WARM_UP, SessionPhase.GRID_WALK),
                viewModel.uiState.first().lowWarningPhases,
            )
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeLowWarningPhases() }
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            coVerify(exactly = 1) {
                repository.saveLowWarningPhases(setOf(SessionPhase.WARM_UP, SessionPhase.GRID_WALK))
            }
            confirmVerified(repository, vehicleClassRepository)
        }

    @Test
    fun `onVehicleClassHighThresholdChangedを呼ぶとuiStateのvehicleClassHighThresholdCelsiusが更新される`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeLowWarningPhases() } returns MutableStateFlow(emptyMap())
            val vehicleClassHighThresholdFlow =
                MutableStateFlow(
                    mapOf<LmuWindowsVehicleClassData, Celsius>(LmuWindowsVehicleClassData.Gte to Celsius(95)),
                )
            every {
                vehicleClassRepository.observeHighThresholdCelsius()
            } returns vehicleClassHighThresholdFlow
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            coEvery {
                vehicleClassRepository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gte, Celsius(100))
            } answers {
                vehicleClassHighThresholdFlow.update { it + (LmuWindowsVehicleClassData.Gte to Celsius(100)) }
            }
            stubReadout()
            val viewModel = createViewModel()

            viewModel.onVehicleClassHighThresholdChanged(LmuWindowsVehicleClassData.Gte, 100)

            assertEquals(
                mapOf<LmuWindowsVehicleClassData, Int>(LmuWindowsVehicleClassData.Gte to 100),
                viewModel.uiState.first().vehicleClassHighThresholdCelsius,
            )
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeLowWarningPhases() }
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            coVerify(exactly = 1) {
                vehicleClassRepository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gte, Celsius(100))
            }
            confirmVerified(repository, vehicleClassRepository)
        }

    @Test
    fun `onVehicleClassHighThresholdResetを呼ぶとそのクラスの閾値がデフォルト値に戻る`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeLowWarningPhases() } returns MutableStateFlow(emptyMap())
            val vehicleClassHighThresholdFlow =
                MutableStateFlow(
                    mapOf<LmuWindowsVehicleClassData, Celsius>(LmuWindowsVehicleClassData.Gt3 to Celsius(100)),
                )
            every {
                vehicleClassRepository.observeHighThresholdCelsius()
            } returns vehicleClassHighThresholdFlow
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            coEvery {
                vehicleClassRepository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gt3, Celsius(90))
            } answers {
                vehicleClassHighThresholdFlow.update { it + (LmuWindowsVehicleClassData.Gt3 to Celsius(90)) }
            }
            stubReadout()
            val viewModel = createViewModel()

            viewModel.onVehicleClassHighThresholdReset(LmuWindowsVehicleClassData.Gt3)

            assertEquals(
                mapOf<LmuWindowsVehicleClassData, Int>(LmuWindowsVehicleClassData.Gt3 to 90),
                viewModel.uiState.first().vehicleClassHighThresholdCelsius,
            )
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeLowWarningPhases() }
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            coVerify(exactly = 1) {
                vehicleClassRepository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gt3, Celsius(90))
            }
            confirmVerified(repository, vehicleClassRepository)
        }

    @Test
    fun `onVehicleClassSelectedを呼ぶとuiStateのselectedVehicleClassが更新される`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeLowWarningPhases() } returns MutableStateFlow(emptyMap())
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns MutableStateFlow(emptyMap())
            val selectedVehicleClassFlow =
                MutableStateFlow<LmuWindowsVehicleClassData>(LmuWindowsVehicleClassData.Hypercar)
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns selectedVehicleClassFlow
            coEvery { vehicleClassRepository.saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gte) } answers {
                selectedVehicleClassFlow.value = LmuWindowsVehicleClassData.Gte
            }
            stubReadout()
            val viewModel = createViewModel()

            viewModel.onVehicleClassSelected(LmuWindowsVehicleClassData.Gte)

            assertEquals(LmuWindowsVehicleClassData.Gte, viewModel.uiState.first().selectedVehicleClass)
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeLowWarningPhases() }
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            coVerify(exactly = 1) { vehicleClassRepository.saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gte) }
            confirmVerified(repository, vehicleClassRepository)
        }

    private val textFlow = MutableStateFlow(LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT)

    private fun stubReadout(available: Boolean = false) {
        every { observeText() } returns textFlow
        coEvery { checkAvailable() } returns available
    }

    private fun stubSettings() {
        every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
        every { repository.observeLowWarningPhases() } returns MutableStateFlow(emptyMap())
        every { vehicleClassRepository.observeHighThresholdCelsius() } returns MutableStateFlow(emptyMap())
        every { vehicleClassRepository.observeSelectedVehicleClass() } returns
            MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
    }

    @Test
    fun `文言の監視と保存をUiStateに反映する`() =
        runTest {
            stubSettings()
            stubReadout(available = true)
            coEvery { saveText(" 注意 ") } answers { textFlow.update { "注意" } }
            val viewModel = createViewModel()
            assertEquals(
                LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT,
                viewModel.uiState.first().overheatReadoutText,
            )
            viewModel.onOverheatReadoutTextChanged(" 注意 ")
            assertEquals("注意", viewModel.uiState.first().overheatReadoutText)
            verify(exactly = 1) { observeText() }
            coVerify(exactly = 1) { saveText(" 注意 ") }
            confirmVerified(observeText, saveText)
        }

    @Test
    fun `入力文言を置換せず開始音の後に試聴する`() =
        runTest {
            stubSettings()
            stubReadout(available = true)
            every { observeVolume() } returns flowOf(60)
            coEvery { playStartSound(ReadoutItemKey.LmuWindows.TyreTemperature.Root) } returns Unit
            coEvery { speakText("注意{percent}", volume = 60) } returns Unit
            val viewModel = createViewModel()
            viewModel.onOverheatReadoutTextPreviewClicked("注意{percent}")
            verify(exactly = 1) { observeVolume() }
            coVerify(exactly = 1) { playStartSound(ReadoutItemKey.LmuWindows.TyreTemperature.Root) }
            coVerify(exactly = 1) { speakText("注意{percent}", volume = 60) }
            coVerifyOrder {
                playStartSound(ReadoutItemKey.LmuWindows.TyreTemperature.Root)
                speakText("注意{percent}", volume = 60)
            }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `空白文言では音量を取得せず試聴しない`() =
        runTest {
            stubSettings()
            stubReadout(available = true)
            createViewModel().onOverheatReadoutTextPreviewClicked(" ")
            verify(exactly = 0) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(ReadoutItemKey.LmuWindows.TyreTemperature.Root) }
            coVerify(exactly = 0) { speakText(" ", volume = 60) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `TTS利用不可を反映し試聴しない`() =
        runTest {
            stubSettings()
            stubReadout(available = false)
            val viewModel = createViewModel()
            assertEquals(false, viewModel.uiState.first().isTextToSpeechAvailable)
            viewModel.onOverheatReadoutTextPreviewClicked("注意")
            verify(exactly = 0) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(ReadoutItemKey.LmuWindows.TyreTemperature.Root) }
            coVerify(exactly = 0) { speakText("注意", volume = 60) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `音量ゼロ以下では開始音も本文も試聴しない`() =
        runTest {
            stubSettings()
            stubReadout(available = true)
            val volume = MutableStateFlow(0)
            every { observeVolume() } returns volume
            val viewModel = createViewModel()
            viewModel.onOverheatReadoutTextPreviewClicked("注意")
            volume.update { -1 }
            viewModel.onOverheatReadoutTextPreviewClicked("注意")
            verify(exactly = 2) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(ReadoutItemKey.LmuWindows.TyreTemperature.Root) }
            coVerify(exactly = 0) { speakText("注意", volume = 0) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }
}
