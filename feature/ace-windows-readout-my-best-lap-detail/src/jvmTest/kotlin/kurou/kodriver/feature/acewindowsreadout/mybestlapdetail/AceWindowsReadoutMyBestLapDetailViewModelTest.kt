package kurou.kodriver.feature.acewindowsreadout.mybestlapdetail

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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.ACE_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.AceWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsMyBestLapReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsMyBestLapReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsReadoutMyBestLapDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: AceWindowsMyBestLapPreferencesRepository = mockk()

    private val enabledRepository: ReadoutPreferencesRepository = mockk()

    private val speakText: SpeakTextUseCase = mockk()
    private val playStartSound: PlayStartSoundForKeyUseCase = mockk()
    private val checkAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val observeVolume: ObserveSoundVolumeUseCase = mockk()
    private val textFlow = MutableStateFlow(ACE_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        AceWindowsReadoutMyBestLapDetailViewModel(
            myBestLapUseCases =
                MyBestLapUseCases(
                    observeEnabledStates = ObserveReadoutEnabledStatesUseCase(enabledRepository),
                    saveEnabledState = SaveReadoutEnabledStateUseCase(enabledRepository),
                ),
            readout =
                MyBestLapReadoutUseCases(
                    ObserveAceWindowsMyBestLapReadoutTextUseCase(repository),
                    SaveAceWindowsMyBestLapReadoutTextUseCase(repository),
                    speakText,
                    playStartSound,
                    checkAvailable,
                    observeVolume,
                ),
        )

    @Test
    fun `初期状態はデフォルト値のUiStateを返す`() =
        runTest {
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns false
            every {
                enabledRepository.observeReadoutEnabledStates(Simulator.AceWindows.id)
            } returns MutableStateFlow(emptyMap())
            val viewModel = createViewModel()

            assertEquals(AceWindowsReadoutMyBestLapDetailUiState(), viewModel.uiState.first())
            verify(exactly = 1) { enabledRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) }
            verify(exactly = 1) { repository.observeReadoutText() }
            confirmVerified(repository, enabledRepository)
        }

    @Test
    fun `onEnabledChangedを呼ぶとuiStateのenabledが更新される`() =
        runTest {
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns false
            val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { enabledRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) } returns enabledStatesFlow
            coEvery {
                enabledRepository.saveReadoutEnabledState(
                    Simulator.AceWindows.id,
                    ReadoutItemKey.AceWindows.MyBestLap.DetailEnabled,
                    false,
                )
            } answers {
                enabledStatesFlow.update { it + (ReadoutItemKey.AceWindows.MyBestLap.DetailEnabled to false) }
            }
            val viewModel = createViewModel()

            viewModel.onEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().enabled)
            verify(exactly = 1) { enabledRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) }
            coVerify(exactly = 1) {
                enabledRepository.saveReadoutEnabledState(
                    Simulator.AceWindows.id,
                    ReadoutItemKey.AceWindows.MyBestLap.DetailEnabled,
                    false,
                )
            }
            verify(exactly = 1) { repository.observeReadoutText() }
            confirmVerified(repository, enabledRepository)
        }

    @Test
    fun `文言の監視と保存をUiStateに反映する`() =
        runTest {
            every {
                enabledRepository.observeReadoutEnabledStates(Simulator.AceWindows.id)
            } returns MutableStateFlow(emptyMap())
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns true
            coEvery { repository.saveReadoutText("更新{laptime}") } answers
                { textFlow.update { "更新{laptime}" } }
            val viewModel = createViewModel()
            assertEquals(ACE_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT, viewModel.uiState.first().readoutText)
            assertEquals(true, viewModel.uiState.first().isTextToSpeechAvailable)
            viewModel.onReadoutTextChanged(" 更新{laptime} ")
            assertEquals("更新{laptime}", viewModel.uiState.first().readoutText)
            verify(exactly = 1) { repository.observeReadoutText() }
            coVerify(exactly = 1) { repository.saveReadoutText("更新{laptime}") }
            verify(exactly = 1) { enabledRepository.observeReadoutEnabledStates(Simulator.AceWindows.id) }
            confirmVerified(repository, enabledRepository)
        }

    @Test
    fun `サンプルタイムに置換して開始音の後に試聴する`() =
        runTest {
            every {
                enabledRepository.observeReadoutEnabledStates(Simulator.AceWindows.id)
            } returns MutableStateFlow(emptyMap())
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns true
            every { observeVolume() } returns MutableStateFlow(60)
            coEvery { playStartSound(ReadoutItemKey.AceWindows.MyBestLap.Root) } returns Unit
            coEvery { speakText("更新1分23秒456{unknown}", volume = 60) } returns Unit
            createViewModel().onReadoutTextPreviewClicked("更新{laptime}{unknown}")
            coVerify(exactly = 1) { playStartSound(ReadoutItemKey.AceWindows.MyBestLap.Root) }
            coVerify(exactly = 1) { speakText("更新1分23秒456{unknown}", volume = 60) }
            coVerifyOrder {
                playStartSound(ReadoutItemKey.AceWindows.MyBestLap.Root)
                speakText("更新1分23秒456{unknown}", volume = 60)
            }
            verify(exactly = 1) { observeVolume() }
            confirmVerified(playStartSound, speakText, observeVolume)
        }

    @Test
    fun `空白文言では音量を取得せず試聴しない`() =
        runTest {
            every {
                enabledRepository.observeReadoutEnabledStates(Simulator.AceWindows.id)
            } returns MutableStateFlow(emptyMap())
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns true
            createViewModel().onReadoutTextPreviewClicked(" ")
            verify(exactly = 0) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(ReadoutItemKey.AceWindows.MyBestLap.Root) }
            coVerify(exactly = 0) { speakText(" ", volume = 60) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `TTS利用不可を反映し試聴しない`() =
        runTest {
            every {
                enabledRepository.observeReadoutEnabledStates(Simulator.AceWindows.id)
            } returns MutableStateFlow(emptyMap())
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns false
            val viewModel = createViewModel()
            assertEquals(false, viewModel.uiState.first().isTextToSpeechAvailable)
            viewModel.onReadoutTextPreviewClicked("注意")
            verify(exactly = 0) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(ReadoutItemKey.AceWindows.MyBestLap.Root) }
            coVerify(exactly = 0) { speakText("注意", volume = 60) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `音量ゼロ以下では開始音も本文も試聴しない`() =
        runTest {
            every {
                enabledRepository.observeReadoutEnabledStates(Simulator.AceWindows.id)
            } returns MutableStateFlow(emptyMap())
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns true
            val volume = MutableStateFlow(0)
            every { observeVolume() } returns volume
            val viewModel = createViewModel()
            viewModel.onReadoutTextPreviewClicked("注意")
            volume.update { -1 }
            viewModel.onReadoutTextPreviewClicked("注意")
            verify(exactly = 2) { observeVolume() }
            coVerify(exactly = 0) { playStartSound(ReadoutItemKey.AceWindows.MyBestLap.Root) }
            coVerify(exactly = 0) { speakText("注意", volume = 0) }
            coVerify(exactly = 0) { speakText("注意", volume = -1) }
            confirmVerified(observeVolume, playStartSound, speakText)
        }

    @Test
    fun `ペインを離れると開始音待機中の試聴を停止する`() =
        runTest {
            every {
                enabledRepository.observeReadoutEnabledStates(Simulator.AceWindows.id)
            } returns MutableStateFlow(emptyMap())
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns true
            every { observeVolume() } returns MutableStateFlow(60)
            val viewModel = createViewModel()
            val pendingStartSound = CompletableDeferred<Unit>()
            coEvery { playStartSound(ReadoutItemKey.AceWindows.MyBestLap.Root) } coAnswers
                { pendingStartSound.await() }
            viewModel.onReadoutTextPreviewClicked("更新{laptime}{unknown}")
            viewModel.onPreviewStopped()
            pendingStartSound.complete(Unit)
            coVerify(exactly = 1) { playStartSound(ReadoutItemKey.AceWindows.MyBestLap.Root) }
            coVerify(exactly = 0) { speakText("更新1分23秒456{unknown}", volume = 60) }
            verify(exactly = 1) { observeVolume() }
            confirmVerified(playStartSound, speakText, observeVolume)
        }

    @Test
    fun `試聴中に再押しすると開始音待機中の試聴を停止する`() =
        runTest {
            every {
                enabledRepository.observeReadoutEnabledStates(Simulator.AceWindows.id)
            } returns MutableStateFlow(emptyMap())
            every { repository.observeReadoutText() } returns textFlow
            coEvery { checkAvailable() } returns true
            every { observeVolume() } returns MutableStateFlow(60)
            val viewModel = createViewModel()
            val pendingStartSound = CompletableDeferred<Unit>()
            coEvery { playStartSound(ReadoutItemKey.AceWindows.MyBestLap.Root) } coAnswers
                { pendingStartSound.await() }
            viewModel.onReadoutTextPreviewClicked("更新{laptime}{unknown}")
            viewModel.onReadoutTextPreviewClicked("更新{laptime}{unknown}")
            pendingStartSound.complete(Unit)
            coVerify(exactly = 1) { playStartSound(ReadoutItemKey.AceWindows.MyBestLap.Root) }
            coVerify(exactly = 0) { speakText("更新1分23秒456{unknown}", volume = 60) }
            verify(exactly = 1) { observeVolume() }
            confirmVerified(playStartSound, speakText, observeVolume)
        }
}
