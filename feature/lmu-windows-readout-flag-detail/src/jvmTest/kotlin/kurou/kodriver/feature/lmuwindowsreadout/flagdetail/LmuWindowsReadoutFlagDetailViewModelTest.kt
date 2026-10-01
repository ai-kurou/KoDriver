package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.engine.TextToSpeechEngine
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
            SpeakTextUseCase(tts),
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
    fun `全フラッグの試聴は開始音の後に自由文字列を読み上げる`() =
        runTest {
            coEvery { tts.isAvailable() } returns true
            FlagReadoutItem.entries.forEach { coEvery { engine.playStartSound(it.key) } returns Unit }
            coEvery { tts.speak("注意", false, 42) } returns Unit
            stubReadouts()
            val vm = createViewModel()
            FlagReadoutItem.entries.forEach { vm.onFlagTextPreviewClicked(it, "注意") }
            FlagReadoutItem.entries.forEach { coVerify(exactly = 1) { engine.playStartSound(it.key) } }
            coVerify(exactly = 4) { tts.speak("注意", false, 42) }
            coVerify(exactly = 1) { tts.isAvailable() }
            confirmVerified(engine, tts)
        }

    @Test
    fun `音量ゼロでは開始音も本文も試聴しない`() =
        runTest {
            coEvery { tts.isAvailable() } returns true
            stubReadouts()
            val vm = createViewModel(volume = 0)
            FlagReadoutItem.entries.forEach { vm.onFlagTextPreviewClicked(it, "注意") }
            coVerify(exactly = 1) { tts.isAvailable() }
            verify(exactly = 4) { volumes.volume() }
            confirmVerified(tts, engine, volumes)
        }

    @Test
    fun `空文字と空白文言は試聴しない`() =
        runTest {
            coEvery { tts.isAvailable() } returns true
            stubReadouts()
            val vm = createViewModel()
            FlagReadoutItem.entries.forEach {
                vm.onFlagTextPreviewClicked(it, "")
                vm.onFlagTextPreviewClicked(it, "  ")
            }
            coVerify(exactly = 1) { tts.isAvailable() }
            confirmVerified(tts, engine)
        }

    @Test
    fun `TTS利用不可の場合は試聴しない`() =
        runTest {
            coEvery { tts.isAvailable() } returns false
            stubReadouts()
            val vm = createViewModel()
            FlagReadoutItem.entries.forEach { vm.onFlagTextPreviewClicked(it, "注意") }
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
}
