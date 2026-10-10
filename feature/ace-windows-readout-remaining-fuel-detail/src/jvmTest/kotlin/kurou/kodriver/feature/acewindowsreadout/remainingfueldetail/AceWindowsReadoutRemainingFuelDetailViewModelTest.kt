package kurou.kodriver.feature.acewindowsreadout.remainingfueldetail

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
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsReadoutRemainingFuelDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: AceWindowsRemainingFuelPreferencesRepository = mockk()

    private val readoutPreferencesRepository: ReadoutPreferencesRepository = mockk()

    private val speakText: SpeakTextUseCase = mockk()
    private val playStartSound: PlayStartSoundForKeyUseCase = mockk()
    private val checkAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val observeVolume: ObserveSoundVolumeUseCase = mockk()
    private val textFlow = MutableStateFlow(ACE_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT)

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
        AceWindowsReadoutRemainingFuelDetailViewModel(
            remainingFuelUseCases =
                RemainingFuelUseCases(
                    observeThresholdPercentage =
                        ObserveAceWindowsRemainingFuelThresholdPercentageUseCase(repository),
                    saveThresholdPercentage =
                        SaveAceWindowsRemainingFuelThresholdPercentageUseCase(repository),
                    observeReadoutEnabledStates =
                        ObserveReadoutEnabledStatesUseCase(readoutPreferencesRepository),
                    saveReadoutEnabledState =
                        SaveReadoutEnabledStateUseCase(readoutPreferencesRepository),
                ),
            readout =
                RemainingFuelReadoutUseCases(
                    ObserveAceWindowsRemainingFuelReadoutTextUseCase(repository),
                    SaveAceWindowsRemainingFuelReadoutTextUseCase(repository),
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
            coEvery { checkAvailable() } returns true
            every { repository.observeThresholdPercentage() } returns MutableStateFlow(30)
            val viewModel = createViewModel()
            assertEquals(AceWindowsReadoutRemainingFuelDetailUiState(), viewModel.uiState.value)

            assertEquals(
                AceWindowsReadoutRemainingFuelDetailUiState(
                    thresholdPercentage = 30,
                    enabled = true,
                    readoutText = ACE_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT,
                    isTextToSpeechAvailable = true,
                ),
                viewModel.uiState.first(),
            )
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) }
            confirmVerified(repository, readoutPreferencesRepository)
        }

    @Test
    fun `onThresholdChangedを呼ぶとuiStateのthresholdPercentageが更新される`() =
        runTest {
            every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) } returns
                enabledStatesFlow
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns true
            val thresholdFlow = MutableStateFlow(50)
            every { repository.observeThresholdPercentage() } returns thresholdFlow
            coEvery { repository.saveThresholdPercentage(30) } answers { thresholdFlow.update { 30 } }
            val viewModel = createViewModel()

            viewModel.onThresholdChanged(30)

            assertEquals(30, viewModel.uiState.first().thresholdPercentage)
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) }
            coVerify(exactly = 1) { repository.saveThresholdPercentage(30) }
            confirmVerified(repository, readoutPreferencesRepository)
        }

    @Test
    fun `onThresholdResetを呼ぶとthresholdPercentageがデフォルト値30に戻る`() =
        runTest {
            every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) } returns
                enabledStatesFlow
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns true
            val thresholdFlow = MutableStateFlow(50)
            every { repository.observeThresholdPercentage() } returns thresholdFlow
            coEvery { repository.saveThresholdPercentage(30) } answers { thresholdFlow.update { 30 } }
            val viewModel = createViewModel()

            viewModel.onThresholdReset()

            assertEquals(30, viewModel.uiState.first().thresholdPercentage)
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) }
            coVerify(exactly = 1) { repository.saveThresholdPercentage(30) }
            confirmVerified(repository, readoutPreferencesRepository)
        }

    @Test
    fun `onEnabledChangedにfalseを渡すとuiStateのenabledがfalseになる`() =
        runTest {
            every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) } returns
                enabledStatesFlow
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns true
            every { repository.observeThresholdPercentage() } returns MutableStateFlow(30)
            coEvery {
                readoutPreferencesRepository.saveReadoutEnabledState(
                    Simulator.AceWindows.id,
                    AceWindowsReadoutItemKey.RemainingFuel.DetailEnabled,
                    false,
                )
            } answers {
                enabledStatesFlow.update {
                    it + (AceWindowsReadoutItemKey.RemainingFuel.DetailEnabled to false)
                }
            }
            val viewModel = createViewModel()

            viewModel.onEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().enabled)
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) }
            coVerify(exactly = 1) {
                readoutPreferencesRepository.saveReadoutEnabledState(
                    Simulator.AceWindows.id,
                    AceWindowsReadoutItemKey.RemainingFuel.DetailEnabled,
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
            coEvery { checkAvailable() } returns true
            every { repository.observeThresholdPercentage() } returns MutableStateFlow(50)
            coEvery { repository.saveReadoutText("残り{percent}%") } answers { textFlow.update { "残り{percent}%" } }
            val viewModel = createViewModel()
            assertEquals(ACE_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT, viewModel.uiState.first().readoutText)
            viewModel.onReadoutTextChanged(" 残り{percent}% ")
            assertEquals("残り{percent}%", viewModel.uiState.first().readoutText)
            verify(exactly = 1) { repository.observeReadoutText() }
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            coVerify(exactly = 1) { repository.saveReadoutText("残り{percent}%") }
            confirmVerified(repository)
        }

    @Test
    fun `現在の閾値に置換して開始音の後に試聴する`() =
        runTest {
            every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) } returns
                enabledStatesFlow
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns true
            val threshold = MutableStateFlow(50)
            every { repository.observeThresholdPercentage() } returns threshold
            every { observeVolume() } returns MutableStateFlow(60)
            coEvery { playStartSound(AceWindowsReadoutItemKey.RemainingFuel.Root) } returns Unit
            coEvery { speakText("残り50%", volume = 60) } returns Unit
            coEvery { speakText("残り70%", volume = 60) } returns Unit
            val viewModel = createViewModel()
            val collection =
                backgroundScope.launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) { viewModel.uiState.collect {} }
            assertEquals(50, viewModel.uiState.first().thresholdPercentage)
            viewModel.onReadoutTextPreviewClicked("残り{percent}%", 50)
            // 保存値は50のままでも、画面から渡した70を試聴に使う。
            viewModel.onReadoutTextPreviewClicked("残り{percent}%", 70)
            coVerify(exactly = 2) { playStartSound(AceWindowsReadoutItemKey.RemainingFuel.Root) }
            coVerify(exactly = 1) { speakText("残り50%", volume = 60) }
            coVerify(exactly = 1) { speakText("残り70%", volume = 60) }
            coVerifyOrder {
                playStartSound(AceWindowsReadoutItemKey.RemainingFuel.Root)
                speakText("残り50%", volume = 60)
                playStartSound(AceWindowsReadoutItemKey.RemainingFuel.Root)
                speakText("残り70%", volume = 60)
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
            coEvery { checkAvailable() } returns true
            every { repository.observeThresholdPercentage() } returns MutableStateFlow(50)
            createViewModel().onReadoutTextPreviewClicked(" ", 50)
            verify(exactly = 0) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(AceWindowsReadoutItemKey.RemainingFuel.Root) }
            coVerify(exactly = 0) { speakText(" ", volume = 60) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `TTS利用不可を反映し試聴しない`() =
        runTest {
            every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) } returns
                enabledStatesFlow
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns false
            every { repository.observeThresholdPercentage() } returns MutableStateFlow(50)
            val viewModel = createViewModel()
            assertEquals(false, viewModel.uiState.first().isTextToSpeechAvailable)
            viewModel.onReadoutTextPreviewClicked("注意", 50)
            verify(exactly = 0) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(AceWindowsReadoutItemKey.RemainingFuel.Root) }
            coVerify(exactly = 0) { speakText("注意", volume = 60) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `音量ゼロ以下では開始音も本文も試聴しない`() =
        runTest {
            every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) } returns
                enabledStatesFlow
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns true
            every { repository.observeThresholdPercentage() } returns MutableStateFlow(50)
            val volume = MutableStateFlow(0)
            every { observeVolume() } returns volume
            val viewModel = createViewModel()
            viewModel.onReadoutTextPreviewClicked("注意", 50)
            volume.update { -1 }
            viewModel.onReadoutTextPreviewClicked("注意", 50)
            verify(exactly = 2) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(AceWindowsReadoutItemKey.RemainingFuel.Root) }
            coVerify(exactly = 0) { speakText("注意", volume = 0) }
            coVerify(exactly = 0) { speakText("注意", volume = -1) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `ペインを離れると開始音待機中の試聴を停止する`() =
        runTest {
            every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) } returns
                enabledStatesFlow
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns true
            val threshold = MutableStateFlow(50)
            every { repository.observeThresholdPercentage() } returns threshold
            every { observeVolume() } returns MutableStateFlow(60)
            val viewModel = createViewModel()
            val collection =
                backgroundScope.launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) { viewModel.uiState.collect {} }
            assertEquals(50, viewModel.uiState.first().thresholdPercentage)
            val pendingStartSound = CompletableDeferred<Unit>()
            coEvery { playStartSound(AceWindowsReadoutItemKey.RemainingFuel.Root) } coAnswers
                { pendingStartSound.await() }
            viewModel.onReadoutTextPreviewClicked("残り{percent}%", 50)
            viewModel.onPreviewStopped()
            pendingStartSound.complete(Unit)
            coVerify(exactly = 1) { playStartSound(AceWindowsReadoutItemKey.RemainingFuel.Root) }
            coVerify(exactly = 0) { speakText("残り50%", volume = 60) }
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
            coEvery { checkAvailable() } returns true
            val threshold = MutableStateFlow(50)
            every { repository.observeThresholdPercentage() } returns threshold
            every { observeVolume() } returns MutableStateFlow(60)
            val viewModel = createViewModel()
            val collection =
                backgroundScope.launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) { viewModel.uiState.collect {} }
            assertEquals(50, viewModel.uiState.first().thresholdPercentage)
            val pendingStartSound = CompletableDeferred<Unit>()
            coEvery { playStartSound(AceWindowsReadoutItemKey.RemainingFuel.Root) } coAnswers
                { pendingStartSound.await() }
            viewModel.onReadoutTextPreviewClicked("残り{percent}%", 50)
            viewModel.onReadoutTextPreviewClicked("残り{percent}%", 50)
            pendingStartSound.complete(Unit)
            coVerify(exactly = 1) { playStartSound(AceWindowsReadoutItemKey.RemainingFuel.Root) }
            coVerify(exactly = 0) { speakText("残り50%", volume = 60) }
            verify(exactly = 1) { observeVolume() }
            confirmVerified(playStartSound, speakText, observeVolume)
            collection.cancel()
        }
}
