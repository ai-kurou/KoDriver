@file:Suppress("TooManyFunctions")

package kurou.kodriver.feature.acewindowsreadout.flagdetail

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
import kurou.kodriver.domain.model.AceWindowsFlagReadoutTextKey
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AceWindowsFlagPreferencesRepository
import kurou.kodriver.domain.repository.AceWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kurou.kodriver.domain.repository.TextToSpeechRepository
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlackFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlackWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsCheckeredFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsGreenFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsOrangeCircleFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRedYellowStripesFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveVoicePitchUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceSpeedUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsBlackFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsBlackWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsCheckeredFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsFlagEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsGreenFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsOrangeCircleFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRedYellowStripesFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsYellowFlagReadoutTextUseCase
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
    private val observeVoiceSpeed: ObserveVoiceSpeedUseCase = mockk()
    private val observeVoicePitch: ObserveVoicePitchUseCase = mockk()
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
                        ObserveAceWindowsWhiteFlagReadoutTextUseCase(texts),
                        SaveAceWindowsWhiteFlagReadoutTextUseCase(texts),
                        ObserveAceWindowsGreenFlagReadoutTextUseCase(texts),
                        SaveAceWindowsGreenFlagReadoutTextUseCase(texts),
                        ObserveAceWindowsRedFlagReadoutTextUseCase(texts),
                        SaveAceWindowsRedFlagReadoutTextUseCase(texts),
                        ObserveAceWindowsBlueFlagReadoutTextUseCase(texts),
                        SaveAceWindowsBlueFlagReadoutTextUseCase(texts),
                        ObserveAceWindowsYellowFlagReadoutTextUseCase(texts),
                        SaveAceWindowsYellowFlagReadoutTextUseCase(texts),
                        ObserveAceWindowsBlackFlagReadoutTextUseCase(texts),
                        SaveAceWindowsBlackFlagReadoutTextUseCase(texts),
                        ObserveAceWindowsBlackWhiteFlagReadoutTextUseCase(texts),
                        SaveAceWindowsBlackWhiteFlagReadoutTextUseCase(texts),
                        ObserveAceWindowsOrangeCircleFlagReadoutTextUseCase(texts),
                        SaveAceWindowsOrangeCircleFlagReadoutTextUseCase(texts),
                        ObserveAceWindowsRedYellowStripesFlagReadoutTextUseCase(texts),
                        SaveAceWindowsRedYellowStripesFlagReadoutTextUseCase(texts),
                    ),
                ),
            speakText = SpeakTextUseCase(tts, observeVoice, observeVoiceSpeed, observeVoicePitch),
            playStartSoundForKey = PlayStartSoundForKeyUseCase(ttsEngine),
            checkTextToSpeechAvailable = CheckTextToSpeechAvailableUseCase(tts),
            observeSoundVolume = ObserveSoundVolumeUseCase(volumes),
        )

    @Test
    fun `初期状態はすべてのフラグが enabled=true の UiState を返す`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
            every { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) } returns flowOf("完走")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) } returns flowOf("ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) } returns flowOf("グリーンフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED) } returns flowOf("レッドフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) } returns flowOf("ブルーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) } returns flowOf("イエローフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) } returns flowOf("ブラックフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) } returns flowOf("ブラック・ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) } returns
                flowOf("オレンジボールフラッグ、車両に不具合があります")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) } returns
                flowOf("レッド・イエローストライプフラッグ、路面が滑りやすいです")
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
            coEvery { repository.saveFlagEnabledState(AceWindowsReadoutItemKey.Flag.BlueFlag, false) } answers {
                statesFlow.update { it + (AceWindowsReadoutItemKey.Flag.BlueFlag to false) }
            }
            every { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) } returns flowOf("完走")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) } returns flowOf("ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) } returns flowOf("グリーンフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED) } returns flowOf("レッドフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) } returns flowOf("ブルーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) } returns flowOf("イエローフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) } returns flowOf("ブラックフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) } returns flowOf("ブラック・ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) } returns
                flowOf("オレンジボールフラッグ、車両に不具合があります")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) } returns
                flowOf("レッド・イエローストライプフラッグ、路面が滑りやすいです")
            coEvery { tts.isAvailable() } returns true
            val viewModel = createViewModel()

            viewModel.onFlagEnabledChanged(FlagReadoutItem.BlueFlag, false)

            assertEquals(false, viewModel.uiState.first().enabledStates[AceWindowsReadoutItemKey.Flag.BlueFlag])
            coVerify(exactly = 1) { repository.saveFlagEnabledState(AceWindowsReadoutItemKey.Flag.BlueFlag, false) }
            verify(exactly = 1) { repository.observeFlagEnabledStates() }
            confirmVerified(repository)
        }

    @Test
    fun `10種の文言を監視し空白保存とリセットを反映する`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns flowOf(emptyMap())
            val textFlow = MutableStateFlow("完走")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) } returns textFlow
            every { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) } returns flowOf("ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) } returns flowOf("グリーンフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED) } returns flowOf("レッドフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) } returns flowOf("ブルーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) } returns flowOf("イエローフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) } returns flowOf("ブラックフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) } returns flowOf("ブラック・ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) } returns
                flowOf("オレンジボールフラッグ、車両に不具合があります")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) } returns
                flowOf("レッド・イエローストライプフラッグ、路面が滑りやすいです")
            coEvery { tts.isAvailable() } returns true
            coEvery { texts.saveText(AceWindowsFlagReadoutTextKey.CHECKERED, "") } answers { textFlow.update { "" } }
            coEvery { texts.saveText(AceWindowsFlagReadoutTextKey.CHECKERED, "チェッカーフラッグ") } answers {
                textFlow.update { "チェッカーフラッグ" }
            }
            val vm = createViewModel()
            assertEquals(
                FlagReadoutItem.entries.associateWith {
                    if (it == FlagReadoutItem.CheckeredFlag) "完走" else it.defaultText
                },
                vm.uiState.first().flagTexts,
            )
            assertTrue(vm.uiState.first().isTextToSpeechAvailable)
            vm.onFlagTextChanged(FlagReadoutItem.CheckeredFlag, "   ")
            assertFalse(vm.uiState.first().hasReadoutText(FlagReadoutItem.CheckeredFlag))
            vm.onFlagTextReset(FlagReadoutItem.CheckeredFlag)
            assertEquals("チェッカーフラッグ", vm.uiState.first().flagText(FlagReadoutItem.CheckeredFlag))
            coVerify(exactly = 1) { texts.saveText(AceWindowsFlagReadoutTextKey.CHECKERED, "") }
            coVerify(exactly = 1) { texts.saveText(AceWindowsFlagReadoutTextKey.CHECKERED, "チェッカーフラッグ") }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.RED) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) }
            confirmVerified(texts)
        }

    @Test
    fun `カスタム文言はtrimして保存する`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns flowOf(emptyMap())
            every { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) } returns flowOf("完走")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) } returns flowOf("ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) } returns flowOf("グリーンフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED) } returns flowOf("レッドフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) } returns flowOf("ブルーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) } returns flowOf("イエローフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) } returns flowOf("ブラックフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) } returns flowOf("ブラック・ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) } returns
                flowOf("オレンジボールフラッグ、車両に不具合があります")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) } returns
                flowOf("レッド・イエローストライプフラッグ、路面が滑りやすいです")
            coEvery { tts.isAvailable() } returns true
            coEvery { texts.saveText(AceWindowsFlagReadoutTextKey.CHECKERED, "注意") } returns Unit
            val vm = createViewModel()
            vm.onFlagTextChanged(FlagReadoutItem.CheckeredFlag, "  注意  ")
            coVerify(exactly = 1) { texts.saveText(AceWindowsFlagReadoutTextKey.CHECKERED, "注意") }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.RED) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) }
            confirmVerified(texts)
        }

    @Test
    fun `試聴はFlagRootの開始音の後に設定音声と音量で読み上げる`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns flowOf(emptyMap())
            every { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) } returns flowOf("完走")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) } returns flowOf("ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) } returns flowOf("グリーンフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED) } returns flowOf("レッドフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) } returns flowOf("ブルーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) } returns flowOf("イエローフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) } returns flowOf("ブラックフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) } returns flowOf("ブラック・ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) } returns
                flowOf("オレンジボールフラッグ、車両に不具合があります")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) } returns
                flowOf("レッド・イエローストライプフラッグ、路面が滑りやすいです")
            coEvery { tts.isAvailable() } returns true
            every { volumes.volume() } returns flowOf(42)
            every { observeVoiceSpeed() } returns flowOf(1.0f)
            every { observeVoicePitch() } returns flowOf(1.0f)
            every { observeVoice() } returns flowOf("voice-a")
            val calls = mutableListOf<String>()
            val startSoundCompleted = CompletableDeferred<Unit>()
            coEvery { ttsEngine.playStartSound(AceWindowsReadoutItemKey.Flag.Root) } coAnswers {
                calls += "start"
                startSoundCompleted.await()
            }
            coEvery { tts.speak("完走", false, 42, "voice-a", 1.0f, 1.0f) } answers { calls += "text" }
            val vm = createViewModel()
            vm.onFlagTextPreviewClicked("完走")
            assertEquals(listOf("start"), calls)
            coVerify(exactly = 0) { tts.speak("完走", false, 42, "voice-a", 1.0f, 1.0f) }
            startSoundCompleted.complete(Unit)
            assertEquals(listOf("start", "text"), calls)
            coVerify(exactly = 1) { ttsEngine.playStartSound(AceWindowsReadoutItemKey.Flag.Root) }
            coVerify(exactly = 1) { tts.speak("完走", false, 42, "voice-a", 1.0f, 1.0f) }
            coVerify(exactly = 1) { tts.isAvailable() }
            verify(exactly = 1) { volumes.volume() }
            verify(exactly = 1) { observeVoice() }
            verify(exactly = 1) { observeVoiceSpeed() }
            verify(exactly = 1) { observeVoicePitch() }
            confirmVerified(ttsEngine, tts, volumes, observeVoice, observeVoiceSpeed, observeVoicePitch)
        }

    @Test
    fun `空文字と空白は開始音も本文も試聴しない`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns flowOf(emptyMap())
            every { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) } returns flowOf("完走")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) } returns flowOf("ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) } returns flowOf("グリーンフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED) } returns flowOf("レッドフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) } returns flowOf("ブルーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) } returns flowOf("イエローフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) } returns flowOf("ブラックフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) } returns flowOf("ブラック・ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) } returns
                flowOf("オレンジボールフラッグ、車両に不具合があります")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) } returns
                flowOf("レッド・イエローストライプフラッグ、路面が滑りやすいです")
            coEvery { tts.isAvailable() } returns true
            val vm = createViewModel()
            vm.onFlagTextPreviewClicked("")
            vm.onFlagTextPreviewClicked("   ")
            coVerify(exactly = 1) { tts.isAvailable() }
            verify(exactly = 0) { volumes.volume() }
            confirmVerified(tts, ttsEngine, volumes, observeVoice, observeVoiceSpeed, observeVoicePitch)
        }

    @Test
    fun `TTS利用不可では開始音も本文も試聴しない`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns flowOf(emptyMap())
            every { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) } returns flowOf("完走")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) } returns flowOf("ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) } returns flowOf("グリーンフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED) } returns flowOf("レッドフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) } returns flowOf("ブルーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) } returns flowOf("イエローフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) } returns flowOf("ブラックフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) } returns flowOf("ブラック・ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) } returns
                flowOf("オレンジボールフラッグ、車両に不具合があります")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) } returns
                flowOf("レッド・イエローストライプフラッグ、路面が滑りやすいです")
            coEvery { tts.isAvailable() } returns false
            val vm = createViewModel()
            assertFalse(vm.uiState.first().isTextToSpeechAvailable)
            vm.onFlagTextPreviewClicked("完走")
            coVerify(exactly = 1) { tts.isAvailable() }
            verify(exactly = 0) { volumes.volume() }
            confirmVerified(tts, ttsEngine, volumes, observeVoice, observeVoiceSpeed, observeVoicePitch)
        }

    @Test
    fun `音量0以下では開始音も本文も試聴しない`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns flowOf(emptyMap())
            every { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) } returns flowOf("完走")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) } returns flowOf("ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) } returns flowOf("グリーンフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED) } returns flowOf("レッドフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) } returns flowOf("ブルーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) } returns flowOf("イエローフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) } returns flowOf("ブラックフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) } returns flowOf("ブラック・ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) } returns
                flowOf("オレンジボールフラッグ、車両に不具合があります")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) } returns
                flowOf("レッド・イエローストライプフラッグ、路面が滑りやすいです")
            coEvery { tts.isAvailable() } returns true
            every { volumes.volume() } returnsMany listOf(flowOf(0), flowOf(-1))
            val vm = createViewModel()
            vm.onFlagTextPreviewClicked("完走")
            vm.onFlagTextPreviewClicked("完走")
            coVerify(exactly = 1) { tts.isAvailable() }
            verify(exactly = 2) { volumes.volume() }
            confirmVerified(tts, ttsEngine, volumes, observeVoice, observeVoiceSpeed, observeVoicePitch)
        }

    @Test
    fun `未設定の全10種の文言は既定値を使う`() {
        val state = AceWindowsReadoutFlagDetailUiState()
        FlagReadoutItem.entries.forEach { item ->
            assertEquals(item.defaultText, state.flagText(item))
            assertTrue(state.hasReadoutText(item))
        }
    }

    @Test
    fun `ペインを離れると開始音待機中の試聴を停止する`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns flowOf(emptyMap())
            every { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) } returns flowOf("完走")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) } returns flowOf("ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) } returns flowOf("グリーンフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED) } returns flowOf("レッドフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) } returns flowOf("ブルーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) } returns flowOf("イエローフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) } returns flowOf("ブラックフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) } returns flowOf("ブラック・ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) } returns
                flowOf("オレンジボールフラッグ、車両に不具合があります")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) } returns
                flowOf("レッド・イエローストライプフラッグ、路面が滑りやすいです")
            coEvery { tts.isAvailable() } returns true
            every { volumes.volume() } returns flowOf(42)
            every { observeVoiceSpeed() } returns flowOf(1.0f)
            every { observeVoicePitch() } returns flowOf(1.0f)
            every { observeVoice() } returns flowOf("voice-a")
            val vm = createViewModel()
            val pendingStartSound = CompletableDeferred<Unit>()
            coEvery { ttsEngine.playStartSound(AceWindowsReadoutItemKey.Flag.Root) } coAnswers
                { pendingStartSound.await() }
            vm.onFlagTextPreviewClicked("完走")
            vm.onPreviewStopped()
            pendingStartSound.complete(Unit)
            coVerify(exactly = 0) { tts.speak("完走", false, 42, "voice-a", 1.0f, 1.0f) }
            coVerify(exactly = 1) { ttsEngine.playStartSound(AceWindowsReadoutItemKey.Flag.Root) }
            coVerify(exactly = 1) { tts.isAvailable() }
            verify(exactly = 1) { volumes.volume() }
            verify(exactly = 0) { observeVoice() }
            verify(exactly = 0) { observeVoiceSpeed() }
            verify(exactly = 0) { observeVoicePitch() }
            confirmVerified(tts, ttsEngine, volumes, observeVoice, observeVoiceSpeed, observeVoicePitch)
        }

    @Test
    fun `試聴中に再押しすると開始音待機中の試聴を停止する`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns flowOf(emptyMap())
            every { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) } returns flowOf("完走")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) } returns flowOf("ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) } returns flowOf("グリーンフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED) } returns flowOf("レッドフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) } returns flowOf("ブルーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) } returns flowOf("イエローフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) } returns flowOf("ブラックフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) } returns flowOf("ブラック・ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) } returns
                flowOf("オレンジボールフラッグ、車両に不具合があります")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) } returns
                flowOf("レッド・イエローストライプフラッグ、路面が滑りやすいです")
            coEvery { tts.isAvailable() } returns true
            every { volumes.volume() } returns flowOf(42)
            every { observeVoiceSpeed() } returns flowOf(1.0f)
            every { observeVoicePitch() } returns flowOf(1.0f)
            every { observeVoice() } returns flowOf("voice-a")
            val vm = createViewModel()
            val pendingStartSound = CompletableDeferred<Unit>()
            coEvery { ttsEngine.playStartSound(AceWindowsReadoutItemKey.Flag.Root) } coAnswers
                { pendingStartSound.await() }
            vm.onFlagTextPreviewClicked("完走")
            vm.onFlagTextPreviewClicked("完走")
            pendingStartSound.complete(Unit)
            coVerify(exactly = 0) { tts.speak("完走", false, 42, "voice-a", 1.0f, 1.0f) }
            coVerify(exactly = 1) { ttsEngine.playStartSound(AceWindowsReadoutItemKey.Flag.Root) }
            coVerify(exactly = 1) { tts.isAvailable() }
            verify(exactly = 1) { volumes.volume() }
            verify(exactly = 0) { observeVoice() }
            verify(exactly = 0) { observeVoiceSpeed() }
            verify(exactly = 0) { observeVoicePitch() }
            confirmVerified(tts, ttsEngine, volumes, observeVoice, observeVoiceSpeed, observeVoicePitch)
        }
}
