package kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
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
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelLapsThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRemainingFuelLapsThresholdUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsReadoutRemainingFuelLapsDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: AceWindowsRemainingFuelLapsPreferencesRepository = mockk()

    private val readoutPreferencesRepository: ReadoutPreferencesRepository = mockk()

    private val speakText: SpeakTextUseCase = mockk()
    private val playStartSound: PlayStartSoundForKeyUseCase = mockk()
    private val checkAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val observeVolume: ObserveSoundVolumeUseCase = mockk()
    private val textFlow = MutableStateFlow(ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT)

    private val emptyTextFlow = MutableStateFlow(ACE_WINDOWS_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT)

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
        AceWindowsReadoutRemainingFuelLapsDetailViewModel(
            remainingFuelLapsUseCases =
                RemainingFuelLapsUseCases(
                    observeThreshold =
                        ObserveAceWindowsRemainingFuelLapsThresholdUseCase(repository),
                    saveThreshold =
                        SaveAceWindowsRemainingFuelLapsThresholdUseCase(repository),
                    observeReadoutEnabledStates =
                        ObserveReadoutEnabledStatesUseCase(readoutPreferencesRepository),
                    saveReadoutEnabledState =
                        SaveReadoutEnabledStateUseCase(readoutPreferencesRepository),
                ),
            readout =
                RemainingFuelLapsReadoutUseCases(
                    ObserveAceWindowsRemainingFuelLapsReadoutTextUseCase(repository),
                    SaveAceWindowsRemainingFuelLapsReadoutTextUseCase(repository),
                    ObserveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase(repository),
                    SaveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase(repository),
                    speakText,
                    playStartSound,
                    checkAvailable,
                    observeVolume,
                ),
        )

    @Test
    fun `初期状態はリポジトリのデフォルト値を反映したUiStateを返す`() =
        runTest {
            every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) } returns
                enabledStatesFlow
            every { repository.observeReadoutText() } returns textFlow
            every { repository.observeEmptyReadoutText() } returns emptyTextFlow
            coEvery { checkAvailable() } returns true
            every { repository.observeThresholdLaps() } returns MutableStateFlow(3)
            val viewModel = createViewModel()
            assertEquals(AceWindowsReadoutRemainingFuelLapsDetailUiState(), viewModel.uiState.value)

            assertEquals(
                AceWindowsReadoutRemainingFuelLapsDetailUiState(
                    remainingFuelLaps = 3,
                    enabled = true,
                    readoutText = ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT,
                    isTextToSpeechAvailable = true,
                ),
                viewModel.uiState.first(),
            )
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeEmptyReadoutText() }
            verify(exactly = 1) { repository.observeThresholdLaps() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) }
            confirmVerified(repository, readoutPreferencesRepository)
        }

    @Test
    fun `onRemainingFuelLapsChangedを呼ぶとuiStateのremainingFuelLapsが更新される`() =
        runTest {
            every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) } returns
                enabledStatesFlow
            every { repository.observeReadoutText() } returns textFlow
            every { repository.observeEmptyReadoutText() } returns emptyTextFlow
            coEvery { checkAvailable() } returns true
            val thresholdFlow = MutableStateFlow(4)
            every { repository.observeThresholdLaps() } returns thresholdFlow
            coEvery { repository.saveThresholdLaps(3) } answers { thresholdFlow.update { 3 } }
            val viewModel = createViewModel()

            viewModel.onRemainingFuelLapsChanged(3)

            assertEquals(3, viewModel.uiState.first().remainingFuelLaps)
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeEmptyReadoutText() }
            verify(exactly = 1) { repository.observeThresholdLaps() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) }
            coVerify(exactly = 1) { repository.saveThresholdLaps(3) }
            confirmVerified(repository, readoutPreferencesRepository)
        }

    @Test
    fun `onResetRemainingFuelLapsを呼ぶとremainingFuelLapsがデフォルト値3に戻る`() =
        runTest {
            every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) } returns
                enabledStatesFlow
            every { repository.observeReadoutText() } returns textFlow
            every { repository.observeEmptyReadoutText() } returns emptyTextFlow
            coEvery { checkAvailable() } returns true
            val thresholdFlow = MutableStateFlow(4)
            every { repository.observeThresholdLaps() } returns thresholdFlow
            coEvery { repository.saveThresholdLaps(3) } answers { thresholdFlow.update { 3 } }
            val viewModel = createViewModel()

            viewModel.onResetRemainingFuelLaps()

            assertEquals(3, viewModel.uiState.first().remainingFuelLaps)
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeEmptyReadoutText() }
            verify(exactly = 1) { repository.observeThresholdLaps() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) }
            coVerify(exactly = 1) { repository.saveThresholdLaps(3) }
            confirmVerified(repository, readoutPreferencesRepository)
        }

    @Test
    fun `onEnabledChangedにfalseを渡すとuiStateのenabledがfalseになる`() =
        runTest {
            every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) } returns
                enabledStatesFlow
            every { repository.observeReadoutText() } returns textFlow
            every { repository.observeEmptyReadoutText() } returns emptyTextFlow
            coEvery { checkAvailable() } returns true
            every { repository.observeThresholdLaps() } returns MutableStateFlow(3)
            coEvery {
                readoutPreferencesRepository.saveReadoutEnabledState(
                    Simulator.AceWindows.id,
                    ReadoutItemKey.AceWindows.RemainingFuelLaps.DetailEnabled,
                    false,
                )
            } answers {
                enabledStatesFlow.update {
                    it + (ReadoutItemKey.AceWindows.RemainingFuelLaps.DetailEnabled to false)
                }
            }
            val viewModel = createViewModel()

            viewModel.onEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().enabled)
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeEmptyReadoutText() }
            verify(exactly = 1) { repository.observeThresholdLaps() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) }
            coVerify(exactly = 1) {
                readoutPreferencesRepository.saveReadoutEnabledState(
                    Simulator.AceWindows.id,
                    ReadoutItemKey.AceWindows.RemainingFuelLaps.DetailEnabled,
                    false,
                )
            }
            confirmVerified(repository, readoutPreferencesRepository)
        }

    @Test
    fun `文言の監視と保存をUiStateに反映する`() =
        runTest {
            every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) } returns
                enabledStatesFlow
            every { repository.observeReadoutText() } returns textFlow
            every { repository.observeEmptyReadoutText() } returns emptyTextFlow
            coEvery { checkAvailable() } returns true
            every { repository.observeThresholdLaps() } returns MutableStateFlow(4)
            coEvery { repository.saveReadoutText("残り{laps}周") } answers { textFlow.update { "残り{laps}周" } }
            val viewModel = createViewModel()
            assertEquals(ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT, viewModel.uiState.first().readoutText)
            viewModel.onReadoutTextChanged(" 残り{laps}周 ")
            assertEquals("残り{laps}周", viewModel.uiState.first().readoutText)
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeEmptyReadoutText() }
            verify(exactly = 1) { repository.observeThresholdLaps() }
            coVerify(exactly = 1) { repository.saveReadoutText("残り{laps}周") }
            confirmVerified(repository)
        }

    @Test
    fun `保存値と異なっても画面に表示中の閾値に置換して開始音の後に試聴する`() =
        runTest {
            every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) } returns
                enabledStatesFlow
            every { repository.observeReadoutText() } returns textFlow
            every { repository.observeEmptyReadoutText() } returns emptyTextFlow
            coEvery { checkAvailable() } returns true
            val threshold = MutableStateFlow(4)
            every { repository.observeThresholdLaps() } returns threshold
            every { observeVolume() } returns MutableStateFlow(60)
            coEvery { playStartSound(ReadoutItemKey.AceWindows.RemainingFuelLaps.Root) } returns Unit
            coEvery { speakText("残り4周", volume = 60) } returns Unit
            coEvery { speakText("残り5周", volume = 60) } returns Unit
            val viewModel = createViewModel()
            val collection =
                backgroundScope.launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) { viewModel.uiState.collect {} }
            assertEquals(4, viewModel.uiState.first().remainingFuelLaps)
            viewModel.onReadoutTextPreviewClicked("残り{laps}周", 4)
            // 保存済みの閾値が4周のままでも、画面に表示中の5周で試聴する。
            viewModel.onReadoutTextPreviewClicked("残り{laps}周", 5)
            assertEquals(4, viewModel.uiState.first().remainingFuelLaps)
            coVerify(exactly = 2) { playStartSound(ReadoutItemKey.AceWindows.RemainingFuelLaps.Root) }
            coVerify(exactly = 1) { speakText("残り4周", volume = 60) }
            coVerify(exactly = 1) { speakText("残り5周", volume = 60) }
            coVerifyOrder {
                playStartSound(ReadoutItemKey.AceWindows.RemainingFuelLaps.Root)
                speakText("残り4周", volume = 60)
                playStartSound(ReadoutItemKey.AceWindows.RemainingFuelLaps.Root)
                speakText("残り5周", volume = 60)
            }
            verify(exactly = 2) { observeVolume() }
            confirmVerified(playStartSound, speakText, observeVolume)
            collection.cancel()
        }

    @Test
    fun `空白文言では音量を取得せず試聴しない`() =
        runTest {
            every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) } returns
                enabledStatesFlow
            every { repository.observeReadoutText() } returns textFlow
            every { repository.observeEmptyReadoutText() } returns emptyTextFlow
            coEvery { checkAvailable() } returns true
            every { repository.observeThresholdLaps() } returns MutableStateFlow(4)
            val viewModel = createViewModel()
            viewModel.onReadoutTextPreviewClicked(" ", 4)
            viewModel.onEmptyReadoutTextPreviewClicked(" ")
            verify(exactly = 0) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(ReadoutItemKey.AceWindows.RemainingFuelLaps.Root) }
            coVerify(exactly = 0) { speakText(" ", volume = 60) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `TTS利用不可を反映し試聴しない`() =
        runTest {
            every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) } returns
                enabledStatesFlow
            every { repository.observeReadoutText() } returns textFlow
            every { repository.observeEmptyReadoutText() } returns emptyTextFlow
            coEvery { checkAvailable() } returns false
            every { repository.observeThresholdLaps() } returns MutableStateFlow(4)
            val viewModel = createViewModel()
            assertEquals(false, viewModel.uiState.first().isTextToSpeechAvailable)
            viewModel.onReadoutTextPreviewClicked("注意", 4)
            viewModel.onEmptyReadoutTextPreviewClicked("注意")
            verify(exactly = 0) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(ReadoutItemKey.AceWindows.RemainingFuelLaps.Root) }
            coVerify(exactly = 0) { speakText("注意", volume = 60) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `音量ゼロ以下では開始音も本文も試聴しない`() =
        runTest {
            every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) } returns
                enabledStatesFlow
            every { repository.observeReadoutText() } returns textFlow
            every { repository.observeEmptyReadoutText() } returns emptyTextFlow
            coEvery { checkAvailable() } returns true
            every { repository.observeThresholdLaps() } returns MutableStateFlow(4)
            val volume = MutableStateFlow(0)
            every { observeVolume() } returns volume
            val viewModel = createViewModel()
            viewModel.onReadoutTextPreviewClicked("注意", 4)
            viewModel.onEmptyReadoutTextPreviewClicked("注意")
            volume.update { -1 }
            viewModel.onReadoutTextPreviewClicked("注意", 4)
            viewModel.onEmptyReadoutTextPreviewClicked("注意")
            verify(exactly = 4) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(ReadoutItemKey.AceWindows.RemainingFuelLaps.Root) }
            coVerify(exactly = 0) { speakText("注意", volume = 0) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `燃料なし用の文言を保存して反映する`() =
        runTest {
            every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) } returns
                enabledStatesFlow
            every { repository.observeReadoutText() } returns textFlow
            every { repository.observeEmptyReadoutText() } returns emptyTextFlow
            coEvery { checkAvailable() } returns true
            every { repository.observeThresholdLaps() } returns MutableStateFlow(3)
            coEvery { repository.saveEmptyReadoutText("燃料切れ") } answers { emptyTextFlow.update { "燃料切れ" } }
            val viewModel = createViewModel()
            assertEquals("燃料残り1周未満", viewModel.uiState.first().emptyReadoutText)
            viewModel.onEmptyReadoutTextChanged(" 燃料切れ ")
            assertEquals("燃料切れ", viewModel.uiState.first().emptyReadoutText)
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeEmptyReadoutText() }
            verify(exactly = 1) { repository.observeThresholdLaps() }
            coVerify(exactly = 1) { repository.saveEmptyReadoutText("燃料切れ") }
            confirmVerified(repository)
        }

    @Test
    fun `燃料なし用の文言は置換せず開始音の後に試聴する`() =
        runTest {
            every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) } returns
                enabledStatesFlow
            every { repository.observeReadoutText() } returns textFlow
            every { repository.observeEmptyReadoutText() } returns emptyTextFlow
            coEvery { checkAvailable() } returns true
            every { repository.observeThresholdLaps() } returns MutableStateFlow(3)
            every { observeVolume() } returns MutableStateFlow(60)
            coEvery { playStartSound(ReadoutItemKey.AceWindows.RemainingFuelLaps.Root) } returns Unit
            coEvery { speakText("燃料なし{laps}", volume = 60) } returns Unit
            createViewModel().onEmptyReadoutTextPreviewClicked("燃料なし{laps}")
            coVerify(exactly = 1) { playStartSound(ReadoutItemKey.AceWindows.RemainingFuelLaps.Root) }
            coVerify(exactly = 1) { speakText("燃料なし{laps}", volume = 60) }
            coVerifyOrder {
                playStartSound(ReadoutItemKey.AceWindows.RemainingFuelLaps.Root)
                speakText("燃料なし{laps}", volume = 60)
            }
            verify(exactly = 1) { observeVolume() }
            confirmVerified(playStartSound, speakText, observeVolume)
        }

    @Test
    fun `ペインを離れると開始音待機中の試聴を停止する`() =
        runTest {
            every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) } returns
                enabledStatesFlow
            every { repository.observeReadoutText() } returns textFlow
            every { repository.observeEmptyReadoutText() } returns emptyTextFlow
            coEvery { checkAvailable() } returns true
            val threshold = MutableStateFlow(4)
            every { repository.observeThresholdLaps() } returns threshold
            every { observeVolume() } returns MutableStateFlow(60)
            val viewModel = createViewModel()
            val collection =
                backgroundScope.launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) { viewModel.uiState.collect {} }
            assertEquals(4, viewModel.uiState.first().remainingFuelLaps)
            val pendingStartSound = CompletableDeferred<Unit>()
            coEvery { playStartSound(ReadoutItemKey.AceWindows.RemainingFuelLaps.Root) } coAnswers
                { pendingStartSound.await() }
            viewModel.onReadoutTextPreviewClicked("残り{laps}周", 4)
            viewModel.onPreviewStopped()
            pendingStartSound.complete(Unit)
            coVerify(exactly = 1) { playStartSound(ReadoutItemKey.AceWindows.RemainingFuelLaps.Root) }
            coVerify(exactly = 0) { speakText("残り4周", volume = 60) }
            verify(exactly = 1) { observeVolume() }
            confirmVerified(playStartSound, speakText, observeVolume)
            collection.cancel()
        }

    @Test
    fun `試聴中に再押しすると開始音待機中の試聴を停止する`() =
        runTest {
            every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) } returns
                enabledStatesFlow
            every { repository.observeReadoutText() } returns textFlow
            every { repository.observeEmptyReadoutText() } returns emptyTextFlow
            coEvery { checkAvailable() } returns true
            val threshold = MutableStateFlow(4)
            every { repository.observeThresholdLaps() } returns threshold
            every { observeVolume() } returns MutableStateFlow(60)
            val viewModel = createViewModel()
            val collection =
                backgroundScope.launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) { viewModel.uiState.collect {} }
            assertEquals(4, viewModel.uiState.first().remainingFuelLaps)
            val pendingStartSound = CompletableDeferred<Unit>()
            coEvery { playStartSound(ReadoutItemKey.AceWindows.RemainingFuelLaps.Root) } coAnswers
                { pendingStartSound.await() }
            viewModel.onReadoutTextPreviewClicked("残り{laps}周", 4)
            viewModel.onReadoutTextPreviewClicked("残り{laps}周", 4)
            pendingStartSound.complete(Unit)
            coVerify(exactly = 1) { playStartSound(ReadoutItemKey.AceWindows.RemainingFuelLaps.Root) }
            coVerify(exactly = 0) { speakText("残り4周", volume = 60) }
            verify(exactly = 1) { observeVolume() }
            confirmVerified(playStartSound, speakText, observeVolume)
            collection.cancel()
        }
}
