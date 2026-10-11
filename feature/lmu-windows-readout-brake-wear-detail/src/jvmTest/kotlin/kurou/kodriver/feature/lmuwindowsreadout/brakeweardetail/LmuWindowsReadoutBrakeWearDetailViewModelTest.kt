package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

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
import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassBrakeWearSelectionUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsBrakeWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassBrakeWearSelectionUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.StopSpeechUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutBrakeWearDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val vehicleClassRepository: LmuWindowsVehicleClassBrakeWearPreferencesRepository = mockk()

    private val readoutPreferencesRepository: ReadoutPreferencesRepository = mockk()

    private val checkAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val observeVolume: ObserveSoundVolumeUseCase = mockk()
    private val textFlow = MutableStateFlow(LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT)
    private val playSpeechEvent: PlaySpeechEventUseCase = mockk()
    private val stopSpeech: StopSpeechUseCase = mockk()

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
        LmuWindowsReadoutBrakeWearDetailViewModel(
            brakeWearUseCases =
                BrakeWearUseCases(
                    observeVehicleClassLowThreshold =
                        ObserveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase(vehicleClassRepository),
                    observeVehicleClassSelection =
                        ObserveLmuWindowsVehicleClassBrakeWearSelectionUseCase(vehicleClassRepository),
                    saveVehicleClassLowThreshold =
                        SaveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase(vehicleClassRepository),
                    observeText = ObserveLmuWindowsBrakeWearReadoutTextUseCase(vehicleClassRepository),
                    saveText = SaveLmuWindowsBrakeWearReadoutTextUseCase(vehicleClassRepository),
                    saveVehicleClassSelection =
                        SaveLmuWindowsVehicleClassBrakeWearSelectionUseCase(vehicleClassRepository),
                ),
            observeReadoutEnabledStates = ObserveReadoutEnabledStatesUseCase(readoutPreferencesRepository),
            saveReadoutEnabledState = SaveReadoutEnabledStateUseCase(readoutPreferencesRepository),
            readout = BrakeWearReadoutUseCases(playSpeechEvent, stopSpeech, checkAvailable, observeVolume),
        )

    @Test
    fun `初期状態はリポジトリのデフォルト値を反映したUiStateを返す`() =
        runTest {
            stubReadout()
            every { vehicleClassRepository.observeLowThresholdPercent() } returns
                MutableStateFlow(mapOf(LmuWindowsVehicleClassData.Gte to 15))
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            val viewModel = createViewModel()

            assertEquals(
                LmuWindowsReadoutBrakeWearDetailUiState(
                    vehicleClassLowThresholdPercent = mapOf(LmuWindowsVehicleClassData.Gte to 15),
                    selectedVehicleClass = LmuWindowsVehicleClassData.Hypercar,
                    enabled = true,
                    isTextToSpeechAvailable = true,
                ),
                viewModel.uiState.first(),
            )
            verify(exactly = 1) { vehicleClassRepository.observeReadoutText() }
            verify(exactly = 1) { vehicleClassRepository.observeLowThresholdPercent() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.LmuWindows.id) }
            confirmVerified(vehicleClassRepository, readoutPreferencesRepository)
        }

    @Test
    fun `onVehicleClassLowThresholdChangedを呼ぶとuiStateのvehicleClassLowThresholdPercentが更新される`() =
        runTest {
            stubReadout()
            val thresholdFlow =
                MutableStateFlow<Map<LmuWindowsVehicleClassData, Int>>(mapOf(LmuWindowsVehicleClassData.Gte to 15))
            every { vehicleClassRepository.observeLowThresholdPercent() } returns thresholdFlow
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            coEvery {
                vehicleClassRepository.saveLowThresholdPercent(LmuWindowsVehicleClassData.Gte, 20)
            } answers {
                thresholdFlow.update { it + (LmuWindowsVehicleClassData.Gte to 20) }
            }
            val viewModel = createViewModel()

            viewModel.onVehicleClassLowThresholdChanged(LmuWindowsVehicleClassData.Gte, 20)

            assertEquals<Map<LmuWindowsVehicleClassData, Int>>(
                mapOf(LmuWindowsVehicleClassData.Gte to 20),
                viewModel.uiState.first().vehicleClassLowThresholdPercent,
            )
            verify(exactly = 1) { vehicleClassRepository.observeReadoutText() }
            verify(exactly = 1) { vehicleClassRepository.observeLowThresholdPercent() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.LmuWindows.id) }
            coVerify(exactly = 1) {
                vehicleClassRepository.saveLowThresholdPercent(LmuWindowsVehicleClassData.Gte, 20)
            }
            confirmVerified(vehicleClassRepository, readoutPreferencesRepository)
        }

    @Test
    fun `onVehicleClassLowThresholdResetを呼ぶとそのクラスの閾値がデフォルト値に戻る`() =
        runTest {
            stubReadout()
            val thresholdFlow =
                MutableStateFlow<Map<LmuWindowsVehicleClassData, Int>>(mapOf(LmuWindowsVehicleClassData.Gt3 to 10))
            every { vehicleClassRepository.observeLowThresholdPercent() } returns thresholdFlow
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            coEvery {
                vehicleClassRepository.saveLowThresholdPercent(LmuWindowsVehicleClassData.Gt3, 20)
            } answers {
                thresholdFlow.update { it + (LmuWindowsVehicleClassData.Gt3 to 20) }
            }
            val viewModel = createViewModel()

            viewModel.onVehicleClassLowThresholdReset(LmuWindowsVehicleClassData.Gt3)

            assertEquals<Map<LmuWindowsVehicleClassData, Int>>(
                mapOf(LmuWindowsVehicleClassData.Gt3 to 20),
                viewModel.uiState.first().vehicleClassLowThresholdPercent,
            )
            verify(exactly = 1) { vehicleClassRepository.observeReadoutText() }
            verify(exactly = 1) { vehicleClassRepository.observeLowThresholdPercent() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.LmuWindows.id) }
            coVerify(exactly = 1) {
                vehicleClassRepository.saveLowThresholdPercent(LmuWindowsVehicleClassData.Gt3, 20)
            }
            confirmVerified(vehicleClassRepository, readoutPreferencesRepository)
        }

    @Test
    fun `onVehicleClassSelectedを呼ぶとuiStateのselectedVehicleClassが更新される`() =
        runTest {
            stubReadout()
            every { vehicleClassRepository.observeLowThresholdPercent() } returns MutableStateFlow(emptyMap())
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
            verify(exactly = 1) { vehicleClassRepository.observeLowThresholdPercent() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.LmuWindows.id) }
            coVerify(exactly = 1) { vehicleClassRepository.saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gte) }
            confirmVerified(vehicleClassRepository, readoutPreferencesRepository)
        }

    @Test
    fun `onEnabledChangedにfalseを渡すとuiStateのenabledがfalseになる`() =
        runTest {
            stubReadout()
            every { vehicleClassRepository.observeLowThresholdPercent() } returns MutableStateFlow(emptyMap())
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            coEvery {
                readoutPreferencesRepository.saveReadoutEnabledState(
                    Simulator.LmuWindows.id,
                    LmuWindowsReadoutItemKey.BrakeWear.WarningReadout,
                    false,
                )
            } answers {
                enabledStatesFlow.update {
                    it + (LmuWindowsReadoutItemKey.BrakeWear.WarningReadout to false)
                }
            }
            val viewModel = createViewModel()

            viewModel.onEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().enabled)
            verify(exactly = 1) { vehicleClassRepository.observeReadoutText() }
            verify(exactly = 1) { vehicleClassRepository.observeLowThresholdPercent() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.LmuWindows.id) }
            coVerify(exactly = 1) {
                readoutPreferencesRepository.saveReadoutEnabledState(
                    Simulator.LmuWindows.id,
                    LmuWindowsReadoutItemKey.BrakeWear.WarningReadout,
                    false,
                )
            }
            confirmVerified(vehicleClassRepository, readoutPreferencesRepository)
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
            every { vehicleClassRepository.observeLowThresholdPercent() } returns
                MutableStateFlow(mapOf(LmuWindowsVehicleClassData.Hypercar to 20))
            coEvery { vehicleClassRepository.saveReadoutText("残り{percent}%") } answers {
                textFlow.update { "残り{percent}%" }
            }
            val viewModel = createViewModel()
            assertEquals("ブレーキ残量{percent}%以下", viewModel.uiState.first().readoutText)
            viewModel.onReadoutTextChanged(" 残り{percent}% ")
            assertEquals("残り{percent}%", viewModel.uiState.first().readoutText)
            verify(exactly = 1) { vehicleClassRepository.observeReadoutText() }
            verify(exactly = 1) { vehicleClassRepository.observeLowThresholdPercent() }
            coVerify(exactly = 1) { vehicleClassRepository.saveReadoutText("残り{percent}%") }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            confirmVerified(vehicleClassRepository)
        }

    @Test
    fun `現在の閾値と編集中の文言を解決したイベントで試聴する`() =
        runTest {
            stubReadout()
            val selection = MutableStateFlow<LmuWindowsVehicleClassData>(LmuWindowsVehicleClassData.Hypercar)
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns selection
            val threshold =
                MutableStateFlow<Map<LmuWindowsVehicleClassData, Int>>(
                    mapOf(LmuWindowsVehicleClassData.Hypercar to 20),
                )
            every { vehicleClassRepository.observeLowThresholdPercent() } returns threshold
            every { observeVolume() } returns MutableStateFlow(60)
            every { playSpeechEvent(SpeechEvent.LmuWindowsBrakeWearLow(20, "残り20%")) } returns Unit
            every { playSpeechEvent(SpeechEvent.LmuWindowsBrakeWearLow(12, "残り12%")) } returns Unit
            val viewModel = createViewModel()
            val collection =
                backgroundScope.launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) { viewModel.uiState.collect {} }
            assertEquals(
                20,
                viewModel.uiState.first().vehicleClassLowThresholdPercent[LmuWindowsVehicleClassData.Hypercar],
            )
            viewModel.onReadoutTextPreviewClicked("残り{percent}%")
            threshold.update { mapOf(LmuWindowsVehicleClassData.Gte to 12) }
            selection.update { LmuWindowsVehicleClassData.Gte }
            viewModel.onReadoutTextPreviewClicked("残り{percent}%")
            verify(exactly = 1) { playSpeechEvent(SpeechEvent.LmuWindowsBrakeWearLow(20, "残り20%")) }
            verify(exactly = 1) { playSpeechEvent(SpeechEvent.LmuWindowsBrakeWearLow(12, "残り12%")) }
            verify(exactly = 2) { observeVolume() }
            coVerify(exactly = 1) { checkAvailable() }
            confirmVerified(playSpeechEvent, observeVolume, checkAvailable)
            collection.cancel()
        }

    @Test
    fun `空白文言では音量を取得せず試聴しない`() =
        runTest {
            stubReadout()
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            every { vehicleClassRepository.observeLowThresholdPercent() } returns
                MutableStateFlow(mapOf(LmuWindowsVehicleClassData.Hypercar to 20))
            createViewModel().onReadoutTextPreviewClicked(" ")
            verify(exactly = 0) { observeVolume() }
            verify(exactly = 0) { playSpeechEvent(SpeechEvent.LmuWindowsBrakeWearLow(20, " ")) }
            confirmVerified(observeVolume, playSpeechEvent)
        }

    @Test
    fun `TTS利用不可を反映し試聴しない`() =
        runTest {
            stubReadout(available = false)
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            every { vehicleClassRepository.observeLowThresholdPercent() } returns
                MutableStateFlow(mapOf(LmuWindowsVehicleClassData.Hypercar to 20))
            val viewModel = createViewModel()
            assertEquals(false, viewModel.uiState.first().isTextToSpeechAvailable)
            viewModel.onReadoutTextPreviewClicked("注意")
            verify(exactly = 0) { observeVolume() }
            verify(exactly = 0) { playSpeechEvent(SpeechEvent.LmuWindowsBrakeWearLow(20, "注意")) }
            confirmVerified(observeVolume, playSpeechEvent)
        }

    @Test
    fun `音量ゼロ以下では開始音も本文も試聴しない`() =
        runTest {
            stubReadout()
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            every { vehicleClassRepository.observeLowThresholdPercent() } returns
                MutableStateFlow(mapOf(LmuWindowsVehicleClassData.Hypercar to 20))
            val volume = MutableStateFlow(0)
            every { observeVolume() } returns volume
            val viewModel = createViewModel()
            viewModel.onReadoutTextPreviewClicked("注意")
            volume.update { -1 }
            viewModel.onReadoutTextPreviewClicked("注意")
            verify(exactly = 2) { observeVolume() }
            verify(exactly = 0) { playSpeechEvent(SpeechEvent.LmuWindowsBrakeWearLow(20, "注意")) }
            confirmVerified(observeVolume, playSpeechEvent)
        }

    @Test
    fun `選択クラスの閾値が未保存なら既定閾値で試聴する`() =
        runTest {
            stubReadout()
            every { vehicleClassRepository.observeLowThresholdPercent() } returns MutableStateFlow(emptyMap())
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Gte)
            every { observeVolume() } returns MutableStateFlow(60)
            every { playSpeechEvent(SpeechEvent.LmuWindowsBrakeWearLow(20, "残量20%")) } returns Unit
            val viewModel = createViewModel()
            assertEquals(LmuWindowsVehicleClassData.Gte, viewModel.uiState.first().selectedVehicleClass)
            viewModel.onReadoutTextPreviewClicked("残量{percent}%")
            verify(exactly = 1) { playSpeechEvent(SpeechEvent.LmuWindowsBrakeWearLow(20, "残量20%")) }
            verify(exactly = 1) { observeVolume() }
            coVerify(exactly = 1) { checkAvailable() }
            confirmVerified(playSpeechEvent, observeVolume, checkAvailable)
        }

    @Test
    fun `試聴前のペイン離脱では停止せず試聴後の離脱で一度だけ停止する`() =
        runTest {
            stubReadout()
            every { vehicleClassRepository.observeLowThresholdPercent() } returns
                MutableStateFlow(mapOf(LmuWindowsVehicleClassData.Hypercar to 20))
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            every { observeVolume() } returns MutableStateFlow(60)
            every { playSpeechEvent(SpeechEvent.LmuWindowsBrakeWearLow(20, "残量20%")) } returns Unit
            every { stopSpeech(LmuWindowsReadoutItemKey.BrakeWear.Root) } returns Unit
            val viewModel = createViewModel()
            viewModel.onPreviewStopped()
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.BrakeWear.Root) }
            viewModel.onReadoutTextPreviewClicked("残量{percent}%")
            viewModel.onPreviewStopped()
            viewModel.onPreviewStopped()
            verify(exactly = 1) { stopSpeech(LmuWindowsReadoutItemKey.BrakeWear.Root) }
            verify(exactly = 1) { playSpeechEvent(SpeechEvent.LmuWindowsBrakeWearLow(20, "残量20%")) }
            verify(exactly = 1) { observeVolume() }
            coVerify(exactly = 1) { checkAvailable() }
            confirmVerified(playSpeechEvent, observeVolume, checkAvailable, stopSpeech)
        }
}
