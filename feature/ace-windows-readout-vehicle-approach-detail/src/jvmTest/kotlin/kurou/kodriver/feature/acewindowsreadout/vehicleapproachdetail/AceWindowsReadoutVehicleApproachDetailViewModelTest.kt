package kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
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
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.usecase.AceWindowsVehicleApproachThresholdsUseCases
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsVehicleApproachReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsVehicleApproachEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsVehicleApproachReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsReadoutVehicleApproachDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: AceWindowsVehicleApproachPreferencesRepository = mockk()

    private val ttsEngine: TextToSpeechEngine = mockk()

    private val checkAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val observeVolume: ObserveSoundVolumeUseCase = mockk()
    private val speakText: SpeakTextUseCase = mockk()
    private val textFlow = MutableStateFlow(ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT)

    private val thresholdFlow = MutableStateFlow(ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        AceWindowsReadoutVehicleApproachDetailViewModel(
            thresholds = AceWindowsVehicleApproachThresholdsUseCases(repository),
            observeEnabledStates = ObserveAceWindowsVehicleApproachEnabledStatesUseCase(repository),
            saveEnabledState = SaveAceWindowsVehicleApproachEnabledStateUseCase(repository),
            observeReadoutText = ObserveAceWindowsVehicleApproachReadoutTextUseCase(repository),
            saveReadoutText = SaveAceWindowsVehicleApproachReadoutTextUseCase(repository),
            speakText = speakText,
            playStartSoundForKey = PlayStartSoundForKeyUseCase(ttsEngine),
            checkTextToSpeechAvailable = checkAvailable,
            observeSoundVolume = observeVolume,
        )

    @Test
    fun `初期状態はデフォルト値のUiStateを返す`() =
        runTest {
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns true
            every { repository.observeThresholdMeters() } returns thresholdFlow
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            val viewModel = createViewModel()

            assertEquals(
                AceWindowsReadoutVehicleApproachDetailUiState(isTextToSpeechAvailable = true),
                viewModel.uiState.first(),
            )
            verify(exactly = 1) { repository.observeThresholdMeters() }
            verify(exactly = 1) { repository.observeEnabledStates() }
            verifyObservers()
        }

    @Test
    fun `onThresholdChangedを呼ぶとuiStateのthresholdMetersが更新される`() =
        runTest {
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns true
            every { repository.observeThresholdMeters() } returns thresholdFlow
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            coEvery { repository.saveThresholdMeters(7.0) } answers { thresholdFlow.update { 7.0 } }
            val viewModel = createViewModel()

            viewModel.onThresholdChanged(7.0)

            assertEquals(7.0, viewModel.uiState.first().thresholdMeters)
            verify(exactly = 1) { repository.observeThresholdMeters() }
            verify(exactly = 1) { repository.observeEnabledStates() }
            coVerify(exactly = 1) { repository.saveThresholdMeters(7.0) }
            verifyObservers()
        }

    @Test
    fun `onResetThresholdを呼ぶとthresholdMetersがデフォルト値に戻る`() =
        runTest {
            thresholdFlow.update { 7.0 }
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns true
            every { repository.observeThresholdMeters() } returns thresholdFlow
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            coEvery {
                repository.saveThresholdMeters(ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT)
            } answers {
                thresholdFlow.update { ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT }
            }
            val viewModel = createViewModel()

            viewModel.onResetThreshold()

            assertEquals(
                ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT,
                viewModel.uiState.first().thresholdMeters,
            )
            verify(exactly = 1) { repository.observeThresholdMeters() }
            verify(exactly = 1) { repository.observeEnabledStates() }
            coVerify(exactly = 1) {
                repository.saveThresholdMeters(ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT)
            }
            verifyObservers()
        }

    @Test
    fun `onStartReadoutEnabledChangedを呼ぶとuiStateのstartReadoutEnabledが更新される`() =
        runTest {
            val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns true
            every { repository.observeThresholdMeters() } returns thresholdFlow
            every { repository.observeEnabledStates() } returns enabledStatesFlow
            coEvery {
                repository.saveEnabledState(ReadoutItemKey.AceWindows.VehicleApproach.StartReadout, false)
            } answers {
                enabledStatesFlow.update { it + (ReadoutItemKey.AceWindows.VehicleApproach.StartReadout to false) }
            }
            val viewModel = createViewModel()

            viewModel.onStartReadoutEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().startReadoutEnabled)
            verify(exactly = 1) { repository.observeThresholdMeters() }
            verify(exactly = 1) { repository.observeEnabledStates() }
            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.AceWindows.VehicleApproach.StartReadout, false)
            }
            verifyObservers()
        }

    @Test
    fun `入力文言を正規化して保存し状態へ反映する`() =
        runTest {
            stubSettings()
            coEvery { repository.saveReadoutText("周囲に注意") } answers { textFlow.update { "周囲に注意" } }
            val viewModel = createViewModel()
            viewModel.onReadoutTextChanged("  周囲に注意  ")
            assertEquals("周囲に注意", viewModel.uiState.first().readoutText)
            coVerify(exactly = 1) { repository.saveReadoutText("周囲に注意") }
            verifySettings()
        }

    @Test
    fun `文言をデフォルトに戻す`() =
        runTest {
            stubSettings()
            textFlow.update { "周囲に注意" }
            coEvery { repository.saveReadoutText(ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT) } answers {
                textFlow.update { ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT }
            }
            val viewModel = createViewModel()
            viewModel.onReadoutTextReset()
            assertEquals(ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT, viewModel.uiState.first().readoutText)
            coVerify(exactly = 1) { repository.saveReadoutText(ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT) }
            verifySettings()
        }

    @Test
    fun `試聴はRoot開始音の完了後に入力文言を指定音量で読み上げる`() =
        runTest {
            stubSettings()
            val calls = mutableListOf<String>()
            every { observeVolume() } returns flowOf(42)
            coEvery { ttsEngine.playStartSound(ReadoutItemKey.AceWindows.VehicleApproach.Root) } answers {
                calls += "start"
            }
            coEvery { speakText("周囲に注意", volume = 42) } answers { calls += "text" }
            val viewModel = createViewModel()
            viewModel.onPreviewClicked("周囲に注意")
            assertEquals(listOf("start", "text"), calls)
            verify(exactly = 1) { observeVolume() }
            coVerify(exactly = 1) { ttsEngine.playStartSound(ReadoutItemKey.AceWindows.VehicleApproach.Root) }
            coVerify(exactly = 1) { speakText("周囲に注意", volume = 42) }
            confirmVerified(observeVolume, ttsEngine, speakText)
            verifySettings()
        }

    @Test
    fun `空文字と空白は音量も取得せず試聴しない`() =
        runTest {
            stubSettings()
            val viewModel = createViewModel()
            listOf("", " \t\n ").forEach { text ->
                viewModel.onPreviewClicked(text)
                coVerify(exactly = 0) { speakText(text, volume = 100) }
            }
            verify(exactly = 0) { observeVolume() }
            coVerify(exactly = 0) { ttsEngine.playStartSound(ReadoutItemKey.AceWindows.VehicleApproach.Root) }
            coVerify(exactly = 0) { speakText("接近", volume = 100) }
            confirmVerified(observeVolume, ttsEngine, speakText)
            verifySettings()
        }

    @Test
    fun `TTS利用不可なら案内状態になり試聴しない`() =
        runTest {
            stubSettings()
            coEvery { checkAvailable() } returns false
            val viewModel = createViewModel()
            viewModel.onPreviewClicked("接近")
            assertEquals(false, viewModel.uiState.first().isTextToSpeechAvailable)
            verify(exactly = 0) { observeVolume() }
            coVerify(exactly = 0) { ttsEngine.playStartSound(ReadoutItemKey.AceWindows.VehicleApproach.Root) }
            coVerify(exactly = 0) { speakText("接近", volume = 100) }
            confirmVerified(observeVolume, ttsEngine, speakText)
            verifySettings()
        }

    @Test
    fun `音量0以下は開始音も本文も試聴しない`() =
        runTest {
            stubSettings()
            val volume = MutableStateFlow(0)
            every { observeVolume() } returns volume
            val viewModel = createViewModel()
            viewModel.onPreviewClicked("接近")
            volume.update { -1 }
            viewModel.onPreviewClicked("接近")
            verify(exactly = 2) { observeVolume() }
            coVerify(exactly = 0) { ttsEngine.playStartSound(ReadoutItemKey.AceWindows.VehicleApproach.Root) }
            coVerify(exactly = 0) { speakText("接近", volume = 0) }
            coVerify(exactly = 0) { speakText("接近", volume = -1) }
            confirmVerified(observeVolume, ttsEngine, speakText)
            verifySettings()
        }

    private fun stubSettings() {
        every { repository.observeReadoutText() } returns textFlow
        coEvery { checkAvailable() } returns true
        every { repository.observeThresholdMeters() } returns thresholdFlow
        every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
    }

    private fun verifySettings() {
        verify(exactly = 1) { repository.observeThresholdMeters() }
        verify(exactly = 1) { repository.observeEnabledStates() }
        verifyObservers()
    }

    private fun verifyObservers() {
        verify(exactly = 1) { repository.observeReadoutText() }
        coVerify(exactly = 1) { checkAvailable() }
        confirmVerified(repository, checkAvailable)
    }

    @Test
    fun `ペインを離れると開始音待機中の試聴を停止する`() =
        runTest {
            stubSettings()
            every { observeVolume() } returns flowOf(42)
            val viewModel = createViewModel()
            val pendingStartSound = CompletableDeferred<Unit>()
            coEvery { ttsEngine.playStartSound(ReadoutItemKey.AceWindows.VehicleApproach.Root) } coAnswers
                { pendingStartSound.await() }
            viewModel.onPreviewClicked("周囲に注意")
            viewModel.onPreviewStopped()
            pendingStartSound.complete(Unit)
            verify(exactly = 1) { observeVolume() }
            coVerify(exactly = 1) { ttsEngine.playStartSound(ReadoutItemKey.AceWindows.VehicleApproach.Root) }
            coVerify(exactly = 0) { speakText("周囲に注意", volume = 42) }
            confirmVerified(observeVolume, ttsEngine, speakText)
        }

    @Test
    fun `試聴中に再押しすると開始音待機中の試聴を停止する`() =
        runTest {
            stubSettings()
            every { observeVolume() } returns flowOf(42)
            val viewModel = createViewModel()
            val pendingStartSound = CompletableDeferred<Unit>()
            coEvery { ttsEngine.playStartSound(ReadoutItemKey.AceWindows.VehicleApproach.Root) } coAnswers
                { pendingStartSound.await() }
            viewModel.onPreviewClicked("周囲に注意")
            viewModel.onPreviewClicked("周囲に注意")
            pendingStartSound.complete(Unit)
            verify(exactly = 1) { observeVolume() }
            coVerify(exactly = 1) { ttsEngine.playStartSound(ReadoutItemKey.AceWindows.VehicleApproach.Root) }
            coVerify(exactly = 0) { speakText("周囲に注意", volume = 42) }
            confirmVerified(observeVolume, ttsEngine, speakText)
        }
}
