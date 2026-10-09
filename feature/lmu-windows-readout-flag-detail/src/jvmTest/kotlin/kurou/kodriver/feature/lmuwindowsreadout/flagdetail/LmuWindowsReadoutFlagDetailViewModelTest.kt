package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kurou.kodriver.domain.repository.TextToSpeechRepository
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceSpeedUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsFlagEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutFlagDetailViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val flags: LmuWindowsFlagPreferencesRepository = mockk()
    private val texts: LmuWindowsFlagReadoutTextPreferencesRepository = mockk()
    private val tts: TextToSpeechRepository = mockk()
    private val engine: TextToSpeechEngine = mockk()
    private val observeVoice: ObserveVoiceUseCase = mockk()
    private val observeVoiceSpeed: ObserveVoiceSpeedUseCase = mockk()
    private val volumes: SoundVolumePreferencesRepository = mockk()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun stubReadouts() {
        every { flags.observeFlagEnabledStates() } returns flowOf(emptyMap())
        every { texts.observeSectorYellowFlagText() } returns flowOf("黄旗")
        every { texts.observeBlueFlagText() } returns flowOf("青旗")
        every { texts.observeFullCourseYellowFlagText() } returns flowOf("減速")
        every { texts.observeRedFlagText() } returns flowOf("停止")
    }

    private fun createViewModel(volume: Int = 42): LmuWindowsReadoutFlagDetailViewModel {
        every { volumes.volume() } returns flowOf(volume)
        return LmuWindowsReadoutFlagDetailViewModel(
            FlagSettingsUseCases(
                ObserveLmuWindowsFlagEnabledStatesUseCase(flags),
                SaveLmuWindowsFlagEnabledStateUseCase(flags),
                FlagReadoutTextUseCases(
                    ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase(texts),
                    ObserveLmuWindowsBlueFlagReadoutTextUseCase(texts),
                    ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase(texts),
                    ObserveLmuWindowsRedFlagReadoutTextUseCase(texts),
                    SaveLmuWindowsSectorYellowFlagReadoutTextUseCase(texts),
                    SaveLmuWindowsBlueFlagReadoutTextUseCase(texts),
                    SaveLmuWindowsFullCourseYellowFlagReadoutTextUseCase(texts),
                    SaveLmuWindowsRedFlagReadoutTextUseCase(texts),
                ),
            ),
            SpeakTextUseCase(tts, observeVoice, observeVoiceSpeed),
            PlayStartSoundForKeyUseCase(engine),
            CheckTextToSpeechAvailableUseCase(tts),
            ObserveSoundVolumeUseCase(volumes),
        )
    }

    @Test
    fun `全フラッグの文言と有効状態を取得する`() =
        runTest {
            every { flags.observeFlagEnabledStates() } returns flowOf(emptyMap())
            every { texts.observeSectorYellowFlagText() } returns flowOf("黄旗")
            every { texts.observeBlueFlagText() } returns flowOf("青旗")
            every { texts.observeFullCourseYellowFlagText() } returns flowOf("減速")
            every { texts.observeRedFlagText() } returns flowOf("停止")
            coEvery { tts.isAvailable() } returns true
            val state = createViewModel().uiState.first()
            assertEquals(listOf("青旗", "黄旗", "減速", "停止"), FlagReadoutItem.entries.map(state::flagText))
            assertTrue(state.isTextToSpeechAvailable)
        }

    @Test
    fun `全フラッグは文言と空文字をそのまま保存する`() =
        runTest {
            coEvery { tts.isAvailable() } returns true
            coEvery { texts.saveSectorYellowFlagText("注意") } returns Unit
            coEvery { texts.saveBlueFlagText("注意") } returns Unit
            coEvery { texts.saveFullCourseYellowFlagText("注意") } returns Unit
            coEvery { texts.saveRedFlagText("注意") } returns Unit
            coEvery { texts.saveSectorYellowFlagText("") } returns Unit
            coEvery { texts.saveBlueFlagText("") } returns Unit
            coEvery { texts.saveFullCourseYellowFlagText("") } returns Unit
            coEvery { texts.saveRedFlagText("") } returns Unit
            stubReadouts()
            val vm = createViewModel()
            FlagReadoutItem.entries.forEach {
                vm.onFlagTextChanged(it, "注意")
                vm.onFlagTextChanged(it, "")
            }
            coVerify(exactly = 1) { texts.saveSectorYellowFlagText("注意") }
            coVerify(exactly = 1) { texts.saveBlueFlagText("注意") }
            coVerify(exactly = 1) { texts.saveFullCourseYellowFlagText("注意") }
            coVerify(exactly = 1) { texts.saveRedFlagText("注意") }
            coVerify(exactly = 1) { texts.saveSectorYellowFlagText("") }
            coVerify(exactly = 1) { texts.saveBlueFlagText("") }
            coVerify(exactly = 1) { texts.saveFullCourseYellowFlagText("") }
            coVerify(exactly = 1) { texts.saveRedFlagText("") }
            verify(exactly = 1) { texts.observeSectorYellowFlagText() }
            verify(exactly = 1) { texts.observeBlueFlagText() }
            verify(exactly = 1) { texts.observeFullCourseYellowFlagText() }
            verify(exactly = 1) { texts.observeRedFlagText() }
            confirmVerified(texts)
        }

    @Test
    fun `リセットすると全4種それぞれの既定文言を保存する`() =
        runTest {
            coEvery { tts.isAvailable() } returns true
            coEvery { texts.saveBlueFlagText("ブルーフラッグ") } returns Unit
            coEvery { texts.saveSectorYellowFlagText("イエローフラッグ") } returns Unit
            coEvery { texts.saveFullCourseYellowFlagText("フルコースイエロー") } returns Unit
            coEvery { texts.saveRedFlagText("レッドフラッグ") } returns Unit
            stubReadouts()
            val vm = createViewModel()

            FlagReadoutItem.entries.forEach { vm.onFlagTextReset(it) }

            coVerify(exactly = 1) { texts.saveBlueFlagText("ブルーフラッグ") }
            coVerify(exactly = 1) { texts.saveSectorYellowFlagText("イエローフラッグ") }
            coVerify(exactly = 1) { texts.saveFullCourseYellowFlagText("フルコースイエロー") }
            coVerify(exactly = 1) { texts.saveRedFlagText("レッドフラッグ") }
            verify(exactly = 1) { texts.observeBlueFlagText() }
            verify(exactly = 1) { texts.observeSectorYellowFlagText() }
            verify(exactly = 1) { texts.observeFullCourseYellowFlagText() }
            verify(exactly = 1) { texts.observeRedFlagText() }
            confirmVerified(texts)
        }

    @Test
    fun `試聴は開始音の後に自由文字列を読み上げる`() =
        runTest {
            coEvery { tts.isAvailable() } returns true
            coEvery { engine.playStartSound(ReadoutItemKey.LmuWindows.Flag.Root) } returns Unit
            every { observeVoiceSpeed() } returns flowOf(1.0f)
            every { observeVoice() } returns flowOf("voice-a")
            coEvery { tts.speak("注意", false, 42, "voice-a", 1.0f) } returns Unit
            stubReadouts()
            val vm = createViewModel()
            vm.onFlagTextPreviewClicked("注意")
            // 開始音の有効設定は個別フラッグではなくトップレベルの Flag.Root に保存される。
            coVerify(exactly = 1) { engine.playStartSound(ReadoutItemKey.LmuWindows.Flag.Root) }
            coVerify(exactly = 1) { tts.speak("注意", false, 42, "voice-a", 1.0f) }
            coVerify(exactly = 1) { tts.isAvailable() }
            verify(exactly = 1) { observeVoice() }
            verify(exactly = 1) { observeVoiceSpeed() }
            confirmVerified(engine, tts, observeVoice, observeVoiceSpeed)
        }

    @Test
    fun `音量ゼロでは開始音も本文も試聴しない`() =
        runTest {
            coEvery { tts.isAvailable() } returns true
            stubReadouts()
            val vm = createViewModel(volume = 0)
            vm.onFlagTextPreviewClicked("注意")
            coVerify(exactly = 1) { tts.isAvailable() }
            verify(exactly = 1) { volumes.volume() }
            confirmVerified(tts, engine, volumes)
        }

    @Test
    fun `空文字と空白文言は試聴しない`() =
        runTest {
            coEvery { tts.isAvailable() } returns true
            stubReadouts()
            val vm = createViewModel()
            vm.onFlagTextPreviewClicked("")
            vm.onFlagTextPreviewClicked("  ")
            coVerify(exactly = 1) { tts.isAvailable() }
            confirmVerified(tts, engine)
        }

    @Test
    fun `TTS利用不可の場合は試聴しない`() =
        runTest {
            coEvery { tts.isAvailable() } returns false
            stubReadouts()
            val vm = createViewModel()
            vm.onFlagTextPreviewClicked("注意")
            coVerify(exactly = 1) { tts.isAvailable() }
            confirmVerified(tts, engine)
        }

    @Test
    fun `全フラッグの有効状態を保存する`() =
        runTest {
            coEvery { tts.isAvailable() } returns true
            FlagReadoutItem.entries.forEach { coEvery { flags.saveFlagEnabledState(it.key, false) } returns Unit }
            stubReadouts()
            val vm = createViewModel()
            FlagReadoutItem.entries.forEach { vm.onFlagEnabledChanged(it, false) }
            FlagReadoutItem.entries.forEach { coVerify(exactly = 1) { flags.saveFlagEnabledState(it.key, false) } }
            verify(exactly = 1) { flags.observeFlagEnabledStates() }
            confirmVerified(flags)
        }

    @Test
    fun `ペインを離れると開始音待機中の試聴を停止する`() =
        runTest {
            coEvery { tts.isAvailable() } returns true
            every { observeVoiceSpeed() } returns flowOf(1.0f)
            every { observeVoice() } returns flowOf("voice-a")
            stubReadouts()
            val vm = createViewModel()
            val pendingStartSound = CompletableDeferred<Unit>()
            coEvery { engine.playStartSound(ReadoutItemKey.LmuWindows.Flag.Root) } coAnswers
                { pendingStartSound.await() }
            vm.onFlagTextPreviewClicked("注意")
            vm.onPreviewStopped()
            pendingStartSound.complete(Unit)
            coVerify(exactly = 1) { engine.playStartSound(ReadoutItemKey.LmuWindows.Flag.Root) }
            coVerify(exactly = 0) { tts.speak("注意", false, 42, "voice-a", 1.0f) }
            coVerify(exactly = 1) { tts.isAvailable() }
            verify(exactly = 0) { observeVoice() }
            verify(exactly = 0) { observeVoiceSpeed() }
            confirmVerified(engine, tts, observeVoice, observeVoiceSpeed)
        }

    @Test
    fun `試聴中に再押しすると開始音待機中の試聴を停止する`() =
        runTest {
            coEvery { tts.isAvailable() } returns true
            every { observeVoiceSpeed() } returns flowOf(1.0f)
            every { observeVoice() } returns flowOf("voice-a")
            stubReadouts()
            val vm = createViewModel()
            val pendingStartSound = CompletableDeferred<Unit>()
            coEvery { engine.playStartSound(ReadoutItemKey.LmuWindows.Flag.Root) } coAnswers
                { pendingStartSound.await() }
            vm.onFlagTextPreviewClicked("注意")
            vm.onFlagTextPreviewClicked("注意")
            pendingStartSound.complete(Unit)
            coVerify(exactly = 1) { engine.playStartSound(ReadoutItemKey.LmuWindows.Flag.Root) }
            coVerify(exactly = 0) { tts.speak("注意", false, 42, "voice-a", 1.0f) }
            coVerify(exactly = 1) { tts.isAvailable() }
            verify(exactly = 0) { observeVoice() }
            verify(exactly = 0) { observeVoiceSpeed() }
            confirmVerified(engine, tts, observeVoice, observeVoiceSpeed)
        }
}
