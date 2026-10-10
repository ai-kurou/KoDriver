package kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeTemperatureReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsBrakeTemperatureReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.StopSpeechUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutBrakeTemperatureDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val vehicleClassRepository: LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository = mockk()

    private val readoutPreferencesRepository: ReadoutPreferencesRepository = mockk()

    private val checkAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val observeVolume: ObserveSoundVolumeUseCase = mockk()
    private val textFlow = MutableStateFlow(LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT)
    private val stopSpeech: StopSpeechUseCase = mockk()
    private val playSpeechEvent: PlaySpeechEventUseCase = mockk()

    private val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        LmuWindowsReadoutBrakeTemperatureDetailViewModel(
            brakeTemperatureUseCases =
                BrakeTemperatureUseCases(
                    observeVehicleClassHighThreshold =
                        ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase(vehicleClassRepository),
                    observeVehicleClassSelection =
                        ObserveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase(vehicleClassRepository),
                    saveVehicleClassHighThreshold =
                        SaveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase(vehicleClassRepository),
                    observeText = ObserveLmuWindowsBrakeTemperatureReadoutTextUseCase(vehicleClassRepository),
                    saveText = SaveLmuWindowsBrakeTemperatureReadoutTextUseCase(vehicleClassRepository),
                    saveVehicleClassSelection =
                        SaveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase(vehicleClassRepository),
                ),
            observeReadoutEnabledStates = ObserveReadoutEnabledStatesUseCase(readoutPreferencesRepository),
            saveReadoutEnabledState = SaveReadoutEnabledStateUseCase(readoutPreferencesRepository),
            readout = BrakeTemperatureReadoutUseCases(playSpeechEvent, stopSpeech, checkAvailable, observeVolume),
        )

    @Test
    fun `初期状態はリポジトリのデフォルト値を反映したUiStateを返す`() =
        runTest {
            stubReadout()
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns
                MutableStateFlow(mapOf(LmuWindowsVehicleClassData.Gte to 700))
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            val viewModel = createViewModel()

            assertEquals(
                LmuWindowsReadoutBrakeTemperatureDetailUiState(
                    vehicleClassHighThresholdCelsius = mapOf(LmuWindowsVehicleClassData.Gte to 700),
                    selectedVehicleClass = LmuWindowsVehicleClassData.Hypercar,
                    enabled = true,
                    isTextToSpeechAvailable = true,
                ),
                viewModel.uiState.first(),
            )
            verify(exactly = 1) { vehicleClassRepository.observeReadoutText() }
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.LmuWindows.id) }
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.BrakeTemperature.Root) }
            confirmVerified(vehicleClassRepository, readoutPreferencesRepository, stopSpeech)
        }

    @Test
    fun `onVehicleClassHighThresholdChangedを呼ぶとuiStateのvehicleClassHighThresholdCelsiusが更新される`() =
        runTest {
            stubReadout()
            val thresholdFlow =
                MutableStateFlow<Map<LmuWindowsVehicleClassData, Int>>(mapOf(LmuWindowsVehicleClassData.Gte to 700))
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns thresholdFlow
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            coEvery {
                vehicleClassRepository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gte, 800)
            } answers {
                thresholdFlow.update { it + (LmuWindowsVehicleClassData.Gte to 800) }
            }
            val viewModel = createViewModel()

            viewModel.onVehicleClassHighThresholdChanged(LmuWindowsVehicleClassData.Gte, 800)

            assertEquals<Map<LmuWindowsVehicleClassData, Int>>(
                mapOf(LmuWindowsVehicleClassData.Gte to 800),
                viewModel.uiState.first().vehicleClassHighThresholdCelsius,
            )
            verify(exactly = 1) { vehicleClassRepository.observeReadoutText() }
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.LmuWindows.id) }
            coVerify(exactly = 1) {
                vehicleClassRepository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gte, 800)
            }
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.BrakeTemperature.Root) }
            confirmVerified(vehicleClassRepository, readoutPreferencesRepository, stopSpeech)
        }

    @Test
    fun `onVehicleClassHighThresholdResetを呼ぶとそのクラスの閾値がデフォルト値に戻る`() =
        runTest {
            stubReadout()
            val thresholdFlow =
                MutableStateFlow<Map<LmuWindowsVehicleClassData, Int>>(mapOf(LmuWindowsVehicleClassData.Gt3 to 600))
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns thresholdFlow
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            coEvery {
                vehicleClassRepository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gt3, 800)
            } answers {
                thresholdFlow.update { it + (LmuWindowsVehicleClassData.Gt3 to 800) }
            }
            val viewModel = createViewModel()

            viewModel.onVehicleClassHighThresholdReset(LmuWindowsVehicleClassData.Gt3)

            assertEquals<Map<LmuWindowsVehicleClassData, Int>>(
                mapOf(LmuWindowsVehicleClassData.Gt3 to 800),
                viewModel.uiState.first().vehicleClassHighThresholdCelsius,
            )
            verify(exactly = 1) { vehicleClassRepository.observeReadoutText() }
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.LmuWindows.id) }
            coVerify(exactly = 1) {
                vehicleClassRepository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gt3, 800)
            }
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.BrakeTemperature.Root) }
            confirmVerified(vehicleClassRepository, readoutPreferencesRepository, stopSpeech)
        }

    @Test
    fun `onVehicleClassSelectedを呼ぶとuiStateのselectedVehicleClassが更新される`() =
        runTest {
            stubReadout()
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns MutableStateFlow(emptyMap())
            val selectedVehicleClassFlow =
                MutableStateFlow<LmuWindowsVehicleClassData>(LmuWindowsVehicleClassData.Hypercar)
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns selectedVehicleClassFlow
            coEvery { vehicleClassRepository.saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gte) } answers {
                selectedVehicleClassFlow.update { LmuWindowsVehicleClassData.Gte }
            }
            val viewModel = createViewModel()

            viewModel.onVehicleClassSelected(LmuWindowsVehicleClassData.Gte)

            assertEquals(LmuWindowsVehicleClassData.Gte, viewModel.uiState.first().selectedVehicleClass)
            verify(exactly = 1) { vehicleClassRepository.observeReadoutText() }
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.LmuWindows.id) }
            coVerify(exactly = 1) { vehicleClassRepository.saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gte) }
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.BrakeTemperature.Root) }
            confirmVerified(vehicleClassRepository, readoutPreferencesRepository, stopSpeech)
        }

    @Test
    fun `onEnabledChangedにfalseを渡すとuiStateのenabledがfalseになる`() =
        runTest {
            stubReadout()
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns MutableStateFlow(emptyMap())
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            coEvery {
                readoutPreferencesRepository.saveReadoutEnabledState(
                    Simulator.LmuWindows.id,
                    LmuWindowsReadoutItemKey.BrakeTemperature.WarningReadout,
                    false,
                )
            } answers {
                enabledStatesFlow.update {
                    it + (LmuWindowsReadoutItemKey.BrakeTemperature.WarningReadout to false)
                }
            }
            val viewModel = createViewModel()

            viewModel.onEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().enabled)
            verify(exactly = 1) { vehicleClassRepository.observeReadoutText() }
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.LmuWindows.id) }
            coVerify(exactly = 1) {
                readoutPreferencesRepository.saveReadoutEnabledState(
                    Simulator.LmuWindows.id,
                    LmuWindowsReadoutItemKey.BrakeTemperature.WarningReadout,
                    false,
                )
            }
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.BrakeTemperature.Root) }
            confirmVerified(vehicleClassRepository, readoutPreferencesRepository, stopSpeech)
        }

    private fun stubReadout(available: Boolean = true) {
        every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.LmuWindows.id) } returns
            enabledStatesFlow
        every { vehicleClassRepository.observeReadoutText() } returns textFlow
        coEvery { checkAvailable() } returns available
    }

    @Test
    fun `文言の監視と保存をUiStateに反映する`() =
        runTest {
            stubReadout()
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns
                MutableStateFlow(mapOf(LmuWindowsVehicleClassData.Hypercar to 800))
            coEvery { vehicleClassRepository.saveReadoutText("残り{celsius}℃") } answers {
                textFlow.update { "残り{celsius}℃" }
            }
            val viewModel = createViewModel()
            assertEquals("ブレーキ温度{celsius}℃以上", viewModel.uiState.first().readoutText)
            viewModel.onReadoutTextChanged(" 残り{celsius}℃ ")
            assertEquals("残り{celsius}℃", viewModel.uiState.first().readoutText)
            verify(exactly = 1) { vehicleClassRepository.observeReadoutText() }
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            coVerify(exactly = 1) { vehicleClassRepository.saveReadoutText("残り{celsius}℃") }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.BrakeTemperature.Root) }
            confirmVerified(vehicleClassRepository, stopSpeech)
        }

    @Test
    fun `現在の閾値と編集中の文言を解決したイベントで試聴する`() =
        runTest {
            stubReadout()
            val selection = MutableStateFlow<LmuWindowsVehicleClassData>(LmuWindowsVehicleClassData.Hypercar)
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns selection
            val threshold =
                MutableStateFlow<Map<LmuWindowsVehicleClassData, Int>>(
                    mapOf(LmuWindowsVehicleClassData.Hypercar to 800),
                )
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns threshold
            every { observeVolume() } returns MutableStateFlow(60)
            every { playSpeechEvent(SpeechEvent.LmuWindowsBrakeOverheat(800, "残り800℃")) } returns Unit
            every { playSpeechEvent(SpeechEvent.LmuWindowsBrakeOverheat(650, "残り650℃")) } returns Unit
            val viewModel = createViewModel()
            val collection =
                backgroundScope.launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) { viewModel.uiState.collect {} }
            assertEquals(
                800,
                viewModel.uiState.first().vehicleClassHighThresholdCelsius[LmuWindowsVehicleClassData.Hypercar],
            )
            viewModel.onReadoutTextPreviewClicked("残り{celsius}℃")
            threshold.update { mapOf(LmuWindowsVehicleClassData.Gte to 650) }
            selection.update { LmuWindowsVehicleClassData.Gte }
            viewModel.onReadoutTextPreviewClicked("残り{celsius}℃")
            verify(exactly = 1) { playSpeechEvent(SpeechEvent.LmuWindowsBrakeOverheat(800, "残り800℃")) }
            verify(exactly = 1) { playSpeechEvent(SpeechEvent.LmuWindowsBrakeOverheat(650, "残り650℃")) }
            verify(exactly = 2) { observeVolume() }
            coVerify(exactly = 1) { checkAvailable() }
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.BrakeTemperature.Root) }
            confirmVerified(playSpeechEvent, observeVolume, checkAvailable, stopSpeech)
            collection.cancel()
        }

    @Test
    fun `空白文言では音量を取得せず試聴しない`() =
        runTest {
            stubReadout()
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns
                MutableStateFlow(mapOf(LmuWindowsVehicleClassData.Hypercar to 800))
            val viewModel = createViewModel()
            viewModel.onReadoutTextPreviewClicked(" ")
            verify(exactly = 0) { observeVolume() }
            verify(exactly = 0) { playSpeechEvent(SpeechEvent.LmuWindowsBrakeOverheat(800, " ")) }
            viewModel.onPreviewStopped()
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.BrakeTemperature.Root) }
            confirmVerified(observeVolume, playSpeechEvent, stopSpeech)
        }

    @Test
    fun `TTS利用不可を反映し試聴しない`() =
        runTest {
            stubReadout(available = false)
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns
                MutableStateFlow(mapOf(LmuWindowsVehicleClassData.Hypercar to 800))
            val viewModel = createViewModel()
            assertEquals(false, viewModel.uiState.first().isTextToSpeechAvailable)
            viewModel.onReadoutTextPreviewClicked("注意")
            verify(exactly = 0) { observeVolume() }
            verify(exactly = 0) { playSpeechEvent(SpeechEvent.LmuWindowsBrakeOverheat(800, "注意")) }
            viewModel.onPreviewStopped()
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.BrakeTemperature.Root) }
            confirmVerified(observeVolume, playSpeechEvent, stopSpeech)
        }

    @Test
    fun `音量ゼロ以下では開始音も本文も試聴しない`() =
        runTest {
            stubReadout()
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns
                MutableStateFlow(mapOf(LmuWindowsVehicleClassData.Hypercar to 800))
            val volume = MutableStateFlow(0)
            every { observeVolume() } returns volume
            val viewModel = createViewModel()
            viewModel.onReadoutTextPreviewClicked("注意")
            volume.update { -1 }
            viewModel.onReadoutTextPreviewClicked("注意")
            verify(exactly = 2) { observeVolume() }
            verify(exactly = 0) { playSpeechEvent(SpeechEvent.LmuWindowsBrakeOverheat(800, "注意")) }
            viewModel.onPreviewStopped()
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.BrakeTemperature.Root) }
            confirmVerified(observeVolume, playSpeechEvent, stopSpeech)
        }

    @Test
    fun `選択クラスの閾値が未保存なら既定閾値で試聴する`() =
        runTest {
            stubReadout()
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns MutableStateFlow(emptyMap())
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Gte)
            every { observeVolume() } returns MutableStateFlow(60)
            every { playSpeechEvent(SpeechEvent.LmuWindowsBrakeOverheat(800, "温度800℃")) } returns Unit
            val viewModel = createViewModel()
            assertEquals(LmuWindowsVehicleClassData.Gte, viewModel.uiState.first().selectedVehicleClass)
            viewModel.onReadoutTextPreviewClicked("温度{celsius}℃")
            verify(exactly = 1) { playSpeechEvent(SpeechEvent.LmuWindowsBrakeOverheat(800, "温度800℃")) }
            verify(exactly = 1) { observeVolume() }
            coVerify(exactly = 1) { checkAvailable() }
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.BrakeTemperature.Root) }
            confirmVerified(playSpeechEvent, observeVolume, checkAvailable, stopSpeech)
        }

    @Test
    fun `ペインを離れると開始した試聴を一度だけ停止する`() =
        runTest {
            stubReadout()
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            val threshold =
                MutableStateFlow<Map<LmuWindowsVehicleClassData, Int>>(
                    mapOf(LmuWindowsVehicleClassData.Hypercar to 800),
                )
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns threshold
            every { observeVolume() } returns MutableStateFlow(60)
            every { playSpeechEvent(SpeechEvent.LmuWindowsBrakeOverheat(800, "残り800℃")) } returns Unit
            every { stopSpeech(LmuWindowsReadoutItemKey.BrakeTemperature.Root) } returns Unit
            val viewModel = createViewModel()
            viewModel.onPreviewStopped()
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.BrakeTemperature.Root) }
            assertEquals(
                800,
                viewModel.uiState.first().vehicleClassHighThresholdCelsius[LmuWindowsVehicleClassData.Hypercar],
            )
            viewModel.onReadoutTextPreviewClicked("残り{celsius}℃")
            verify(exactly = 1) { playSpeechEvent(SpeechEvent.LmuWindowsBrakeOverheat(800, "残り800℃")) }
            verify(exactly = 1) { observeVolume() }
            coVerify(exactly = 1) { checkAvailable() }
            viewModel.onPreviewStopped()
            viewModel.onPreviewStopped()
            verify(exactly = 1) { stopSpeech(LmuWindowsReadoutItemKey.BrakeTemperature.Root) }
            verify(exactly = 1) { vehicleClassRepository.observeReadoutText() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.LmuWindows.id) }
            confirmVerified(
                vehicleClassRepository,
                readoutPreferencesRepository,
                playSpeechEvent,
                observeVolume,
                checkAvailable,
                stopSpeech,
            )
        }
}
