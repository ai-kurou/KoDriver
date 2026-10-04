package kurou.kodriver.feature.acewindowsreadout.flagdetail

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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AceWindowsFlagPreferencesRepository
import kurou.kodriver.domain.repository.AceWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kurou.kodriver.domain.repository.TextToSpeechRepository
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsCheckeredFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsCheckeredFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsFlagEnabledStateUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsReadoutFlagDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: AceWindowsFlagPreferencesRepository = mockk()

    private val ttsEngine: TextToSpeechEngine = mockk()

    private val texts: AceWindowsFlagReadoutTextPreferencesRepository = mockk()
    private val tts: TextToSpeechRepository = mockk()
    private val observeVoice: ObserveVoiceUseCase = mockk()
    private val volumes: SoundVolumePreferencesRepository = mockk()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        AceWindowsReadoutFlagDetailViewModel(
            settingsUseCases =
                FlagSettingsUseCases(
                    ObserveAceWindowsFlagEnabledStatesUseCase(repository),
                    SaveAceWindowsFlagEnabledStateUseCase(repository),
                    FlagReadoutTextUseCases(
                        ObserveAceWindowsCheckeredFlagReadoutTextUseCase(texts),
                        SaveAceWindowsCheckeredFlagReadoutTextUseCase(texts),
                    ),
                ),
            playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
            speakText = SpeakTextUseCase(tts, observeVoice),
            playStartSoundForKey = PlayStartSoundForKeyUseCase(ttsEngine),
            checkTextToSpeechAvailable = CheckTextToSpeechAvailableUseCase(tts),
            observeSoundVolume = ObserveSoundVolumeUseCase(volumes),
        )

    @Test
    fun `初期状態はすべてのフラグが enabled=true の UiState を返す`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
            every { texts.observeCheckeredFlagText() } returns flowOf("完走")
            coEvery { tts.isAvailable() } returns true
            val viewModel = createViewModel()

            val state = viewModel.uiState.first()

            FlagReadoutItem.entries.forEach { item ->
                assertEquals(true, state.enabledStates[item.key])
            }
            verify(exactly = 1) { repository.observeFlagEnabledStates() }
            confirmVerified(repository)
        }

    @Test
    fun `onFlagEnabledChanged を呼ぶと UiState が更新される`() =
        runTest {
            val statesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { repository.observeFlagEnabledStates() } returns statesFlow
            coEvery { repository.saveFlagEnabledState(ReadoutItemKey.AceWindows.Flag.BlueFlag, false) } answers {
                statesFlow.update { it + (ReadoutItemKey.AceWindows.Flag.BlueFlag to false) }
            }
            every { texts.observeCheckeredFlagText() } returns flowOf("完走")
            coEvery { tts.isAvailable() } returns true
            val viewModel = createViewModel()

            viewModel.onFlagEnabledChanged(FlagReadoutItem.BlueFlag, false)

            assertEquals(false, viewModel.uiState.first().enabledStates[ReadoutItemKey.AceWindows.Flag.BlueFlag])
            coVerify(exactly = 1) { repository.saveFlagEnabledState(ReadoutItemKey.AceWindows.Flag.BlueFlag, false) }
            verify(exactly = 1) { repository.observeFlagEnabledStates() }
            confirmVerified(repository)
        }

    @Test
    fun `onPreviewClicked を呼ぶと各フラグに対応する SpeechEvent が再生される`() {
        every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
        val eventByItem =
            mapOf(
                FlagReadoutItem.WhiteFlag to SpeechEvent.AceWindowsWhiteFlag,
                FlagReadoutItem.GreenFlag to SpeechEvent.AceWindowsGreenFlag,
                FlagReadoutItem.RedFlag to SpeechEvent.AceWindowsRedFlag,
                FlagReadoutItem.BlueFlag to SpeechEvent.AceWindowsBlueFlag,
                FlagReadoutItem.YellowFlag to SpeechEvent.AceWindowsYellowFlag,
                FlagReadoutItem.BlackFlag to SpeechEvent.AceWindowsBlackFlag,
                FlagReadoutItem.BlackWhiteFlag to SpeechEvent.AceWindowsBlackWhiteFlag,
                FlagReadoutItem.OrangeCircleFlag to SpeechEvent.AceWindowsOrangeCircleFlag,
                FlagReadoutItem.RedYellowStripesFlag to SpeechEvent.AceWindowsRedYellowStripesFlag,
            )
        assertEquals(FlagReadoutItem.entries.filter { it.defaultText == null }.toSet(), eventByItem.keys)
        eventByItem.forEach { (_, event) -> every { ttsEngine.speak(event, false) } returns Unit }
        every { texts.observeCheckeredFlagText() } returns flowOf("完走")
        coEvery { tts.isAvailable() } returns true
        val viewModel = createViewModel()

        eventByItem.forEach { (item, _) -> viewModel.onPreviewClicked(item) }
        viewModel.onPreviewClicked(FlagReadoutItem.CheckeredFlag)

        eventByItem.forEach { (_, event) -> verify(exactly = 1) { ttsEngine.speak(event, false) } }
        verify(exactly = 1) { repository.observeFlagEnabledStates() }
        confirmVerified(ttsEngine, repository)
    }

    @Test
    fun `Checkeredのみ文言を監視し空白保存とリセットを反映する`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns flowOf(emptyMap())
            val textFlow = MutableStateFlow("完走")
            every { texts.observeCheckeredFlagText() } returns textFlow
            coEvery { tts.isAvailable() } returns true
            coEvery { texts.saveCheckeredFlagText("") } answers { textFlow.update { "" } }
            coEvery { texts.saveCheckeredFlagText("チェッカーフラッグ") } answers {
                textFlow.update { "チェッカーフラッグ" }
            }
            val vm = createViewModel()
            assertEquals(mapOf(FlagReadoutItem.CheckeredFlag to "完走"), vm.uiState.first().flagTexts)
            assertTrue(vm.uiState.first().isTextToSpeechAvailable)
            vm.onFlagTextChanged(FlagReadoutItem.CheckeredFlag, "   ")
            assertFalse(vm.uiState.first().hasReadoutText(FlagReadoutItem.CheckeredFlag))
            vm.onFlagTextReset(FlagReadoutItem.CheckeredFlag)
            assertEquals("チェッカーフラッグ", vm.uiState.first().flagText(FlagReadoutItem.CheckeredFlag))
            coVerify(exactly = 1) { texts.saveCheckeredFlagText("") }
            coVerify(exactly = 1) { texts.saveCheckeredFlagText("チェッカーフラッグ") }
            verify(exactly = 1) { texts.observeCheckeredFlagText() }
            confirmVerified(texts)
        }

    @Test
    fun `カスタム文言はtrimして保存しWAV項目は保存とリセットをしない`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns flowOf(emptyMap())
            every { texts.observeCheckeredFlagText() } returns flowOf("完走")
            coEvery { tts.isAvailable() } returns true
            coEvery { texts.saveCheckeredFlagText("注意") } returns Unit
            val vm = createViewModel()
            vm.onFlagTextChanged(FlagReadoutItem.CheckeredFlag, "  注意  ")
            FlagReadoutItem.entries.filter { it.defaultText == null }.forEach {
                vm.onFlagTextChanged(it, "WAV")
                vm.onFlagTextReset(it)
            }
            coVerify(exactly = 1) { texts.saveCheckeredFlagText("注意") }
            verify(exactly = 1) { texts.observeCheckeredFlagText() }
            confirmVerified(texts)
        }

    @Test
    fun `試聴はFlagRootの開始音の後に設定音声と音量で読み上げる`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns flowOf(emptyMap())
            every { texts.observeCheckeredFlagText() } returns flowOf("完走")
            coEvery { tts.isAvailable() } returns true
            every { volumes.volume() } returns flowOf(42)
            every { observeVoice() } returns flowOf("voice-a")
            val calls = mutableListOf<String>()
            coEvery { ttsEngine.playStartSound(ReadoutItemKey.AceWindows.Flag.Root) } answers { calls += "start" }
            coEvery { tts.speak("完走", false, 42, "voice-a") } answers { calls += "text" }
            val vm = createViewModel()
            vm.onFlagTextPreviewClicked("完走")
            assertEquals(listOf("start", "text"), calls)
            coVerify(exactly = 1) { ttsEngine.playStartSound(ReadoutItemKey.AceWindows.Flag.Root) }
            coVerify(exactly = 1) { tts.speak("完走", false, 42, "voice-a") }
            coVerify(exactly = 1) { tts.isAvailable() }
            verify(exactly = 1) { volumes.volume() }
            verify(exactly = 1) { observeVoice() }
            confirmVerified(ttsEngine, tts, volumes, observeVoice)
        }

    @Test
    fun `空文字と空白は開始音も本文も試聴しない`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns flowOf(emptyMap())
            every { texts.observeCheckeredFlagText() } returns flowOf("完走")
            coEvery { tts.isAvailable() } returns true
            val vm = createViewModel()
            vm.onFlagTextPreviewClicked("")
            vm.onFlagTextPreviewClicked("   ")
            coVerify(exactly = 1) { tts.isAvailable() }
            verify(exactly = 0) { volumes.volume() }
            confirmVerified(tts, ttsEngine, volumes, observeVoice)
        }

    @Test
    fun `TTS利用不可では開始音も本文も試聴しない`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns flowOf(emptyMap())
            every { texts.observeCheckeredFlagText() } returns flowOf("完走")
            coEvery { tts.isAvailable() } returns false
            val vm = createViewModel()
            assertFalse(vm.uiState.first().isTextToSpeechAvailable)
            vm.onFlagTextPreviewClicked("完走")
            coVerify(exactly = 1) { tts.isAvailable() }
            verify(exactly = 0) { volumes.volume() }
            confirmVerified(tts, ttsEngine, volumes, observeVoice)
        }

    @Test
    fun `音量0以下では開始音も本文も試聴しない`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns flowOf(emptyMap())
            every { texts.observeCheckeredFlagText() } returns flowOf("完走")
            coEvery { tts.isAvailable() } returns true
            every { volumes.volume() } returnsMany listOf(flowOf(0), flowOf(-1))
            val vm = createViewModel()
            vm.onFlagTextPreviewClicked("完走")
            vm.onFlagTextPreviewClicked("完走")
            coVerify(exactly = 1) { tts.isAvailable() }
            verify(exactly = 2) { volumes.volume() }
            confirmVerified(tts, ttsEngine, volumes, observeVoice)
        }

    @Test
    fun `未設定の文言は既定値を使いWAV項目は空文字`() {
        val state = AceWindowsReadoutFlagDetailUiState()
        assertEquals("チェッカーフラッグ", state.flagText(FlagReadoutItem.CheckeredFlag))
        assertTrue(state.hasReadoutText(FlagReadoutItem.CheckeredFlag))
        assertEquals("", state.flagText(FlagReadoutItem.WhiteFlag))
        assertFalse(state.hasReadoutText(FlagReadoutItem.WhiteFlag))
    }

    @Test
    fun `WAV項目の文言UseCase集約は何も監視保存しない`() =
        runTest {
            val useCases =
                FlagReadoutTextUseCases(
                    ObserveAceWindowsCheckeredFlagReadoutTextUseCase(texts),
                    SaveAceWindowsCheckeredFlagReadoutTextUseCase(texts),
                )
            assertEquals(null, useCases.observe(FlagReadoutItem.WhiteFlag).firstOrNull())
            useCases.save(FlagReadoutItem.WhiteFlag, "注意")
            verify(exactly = 0) { texts.observeCheckeredFlagText() }
            coVerify(exactly = 0) { texts.saveCheckeredFlagText("注意") }
            confirmVerified(texts)
        }
}
