@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.RedFlagVoiceType
import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsRedFlagPreferencesRepository
import kurou.kodriver.domain.repository.TextToSpeechRepository
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsFlagEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsRedFlagVoiceTypeUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutFlagDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: LmuWindowsFlagPreferencesRepository = mockk()

    private val redFlagRepository: LmuWindowsRedFlagPreferencesRepository = mockk()

    private val ttsEngine: TextToSpeechEngine = mockk()

    private val textRepository: LmuWindowsFlagReadoutTextPreferencesRepository = mockk(relaxUnitFun = true)

    private val ttsRepository: TextToSpeechRepository = mockk(relaxUnitFun = true)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        LmuWindowsReadoutFlagDetailViewModel(
            observeFlagEnabledStates = ObserveLmuWindowsFlagEnabledStatesUseCase(repository),
            observeRedFlagVoiceType = ObserveLmuWindowsRedFlagVoiceTypeUseCase(redFlagRepository),
            saveFlagEnabledState = SaveLmuWindowsFlagEnabledStateUseCase(repository),
            saveRedFlagVoiceType = SaveLmuWindowsRedFlagVoiceTypeUseCase(redFlagRepository),
            observeSectorYellowFlagReadoutText =
                ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase(textRepository),
            saveSectorYellowFlagReadoutText =
                SaveLmuWindowsSectorYellowFlagReadoutTextUseCase(textRepository),
            playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
            speakText = SpeakTextUseCase(ttsRepository),
            checkTextToSpeechAvailable = CheckTextToSpeechAvailableUseCase(ttsRepository),
        )

    @Test
    fun `初期状態はすべてのフラグが enabled=true の UiState を返す`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
            every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
            every { textRepository.observeSectorYellowFlagText() } returns MutableStateFlow("")
            coEvery { ttsRepository.isAvailable() } returns true
            val viewModel = createViewModel()

            val state = viewModel.uiState.first()

            assertEquals(true, state.enabledStates[ReadoutItemKey.LmuWindows.Flag.BlueFlag])
            assertEquals(true, state.enabledStates[ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag])
            assertEquals(true, state.enabledStates[ReadoutItemKey.LmuWindows.Flag.FullCourseYellow])
            assertEquals(true, state.enabledStates[ReadoutItemKey.LmuWindows.Flag.RedFlag])
            assertEquals(RedFlagVoiceType.SESSION_STOP, state.redFlagVoiceType)
            verify(exactly = 1) { repository.observeFlagEnabledStates() }
            confirmVerified(repository)
        }

    @Test
    fun `onFlagEnabledChanged を呼ぶと UiState が更新される`() =
        runTest {
            val statesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { repository.observeFlagEnabledStates() } returns statesFlow
            coEvery { repository.saveFlagEnabledState(ReadoutItemKey.LmuWindows.Flag.BlueFlag, false) } answers {
                statesFlow.update { it + (ReadoutItemKey.LmuWindows.Flag.BlueFlag to false) }
            }
            every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
            every { textRepository.observeSectorYellowFlagText() } returns MutableStateFlow("")
            coEvery { ttsRepository.isAvailable() } returns true
            val viewModel = createViewModel()

            viewModel.onFlagEnabledChanged(FlagReadoutItem.BlueFlag, false)

            assertEquals(false, viewModel.uiState.first().enabledStates[ReadoutItemKey.LmuWindows.Flag.BlueFlag])
            coVerify(exactly = 1) { repository.saveFlagEnabledState(ReadoutItemKey.LmuWindows.Flag.BlueFlag, false) }
            verify(exactly = 1) { repository.observeFlagEnabledStates() }
            verify(exactly = 1) { redFlagRepository.observeVoiceType() }
            confirmVerified(repository, redFlagRepository)
        }

    @Test
    fun `onRedFlagEnabledChanged を呼ぶと UiState が更新される`() =
        runTest {
            val statesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { repository.observeFlagEnabledStates() } returns statesFlow
            coEvery { repository.saveFlagEnabledState(ReadoutItemKey.LmuWindows.Flag.RedFlag, false) } answers {
                statesFlow.update { it + (ReadoutItemKey.LmuWindows.Flag.RedFlag to false) }
            }
            every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
            every { textRepository.observeSectorYellowFlagText() } returns MutableStateFlow("")
            coEvery { ttsRepository.isAvailable() } returns true
            val viewModel = createViewModel()

            viewModel.onRedFlagEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().enabledStates[ReadoutItemKey.LmuWindows.Flag.RedFlag])
            coVerify(exactly = 1) { repository.saveFlagEnabledState(ReadoutItemKey.LmuWindows.Flag.RedFlag, false) }
            verify(exactly = 1) { repository.observeFlagEnabledStates() }
            verify(exactly = 1) { redFlagRepository.observeVoiceType() }
            confirmVerified(repository, redFlagRepository)
        }

    @Test
    fun `onRedFlagVoiceTypeChanged を呼ぶと UiState が更新される`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
            val voiceTypeFlow = MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
            every { redFlagRepository.observeVoiceType() } returns voiceTypeFlow
            every { textRepository.observeSectorYellowFlagText() } returns MutableStateFlow("")
            coEvery { ttsRepository.isAvailable() } returns true
            coEvery { redFlagRepository.saveVoiceType(RedFlagVoiceType.RED_FLAG) } answers {
                voiceTypeFlow.update { RedFlagVoiceType.RED_FLAG }
            }
            val viewModel = createViewModel()

            viewModel.onRedFlagVoiceTypeChanged(RedFlagVoiceType.RED_FLAG)

            assertEquals(RedFlagVoiceType.RED_FLAG, viewModel.uiState.first().redFlagVoiceType)
            coVerify(exactly = 1) { redFlagRepository.saveVoiceType(RedFlagVoiceType.RED_FLAG) }
            verify(exactly = 1) { repository.observeFlagEnabledStates() }
            verify(exactly = 1) { redFlagRepository.observeVoiceType() }
            confirmVerified(redFlagRepository, repository)
        }

    @Test
    fun `onPreviewClicked に BlueFlag を渡すと BlueFlag イベントが再生される`() {
        every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
        every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
        every { textRepository.observeSectorYellowFlagText() } returns MutableStateFlow("")
        coEvery { ttsRepository.isAvailable() } returns true
        every { ttsEngine.speak(SpeechEvent.BlueFlag, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPreviewClicked(FlagReadoutItem.BlueFlag)

        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.BlueFlag, false) }
        verify(exactly = 1) { repository.observeFlagEnabledStates() }
        verify(exactly = 1) { redFlagRepository.observeVoiceType() }
        confirmVerified(ttsEngine, repository, redFlagRepository)
    }

    @Test
    fun `onPreviewClicked に SectorYellowFlag を渡すと YellowFlag イベントが再生される`() {
        every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
        every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
        every { textRepository.observeSectorYellowFlagText() } returns MutableStateFlow("")
        coEvery { ttsRepository.isAvailable() } returns true
        every { ttsEngine.speak(SpeechEvent.YellowFlag, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPreviewClicked(FlagReadoutItem.SectorYellowFlag)

        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.YellowFlag, false) }
        verify(exactly = 1) { repository.observeFlagEnabledStates() }
        verify(exactly = 1) { redFlagRepository.observeVoiceType() }
        confirmVerified(ttsEngine, repository, redFlagRepository)
    }

    @Test
    fun `onPreviewClicked に FullCourseYellow を渡すと FullCourseYellow イベントが再生される`() {
        every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
        every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
        every { textRepository.observeSectorYellowFlagText() } returns MutableStateFlow("")
        coEvery { ttsRepository.isAvailable() } returns true
        every { ttsEngine.speak(SpeechEvent.FullCourseYellow, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPreviewClicked(FlagReadoutItem.FullCourseYellow)

        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.FullCourseYellow, false) }
        verify(exactly = 1) { repository.observeFlagEnabledStates() }
        verify(exactly = 1) { redFlagRepository.observeVoiceType() }
        confirmVerified(ttsEngine, repository, redFlagRepository)
    }

    @Test
    fun `onRedFlagPreviewClicked に RED_FLAG を渡すと RedFlag イベントが再生される`() {
        every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
        every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
        every { textRepository.observeSectorYellowFlagText() } returns MutableStateFlow("")
        coEvery { ttsRepository.isAvailable() } returns true
        every { ttsEngine.speak(SpeechEvent.RedFlag, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onRedFlagPreviewClicked(RedFlagVoiceType.RED_FLAG)

        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.RedFlag, false) }
        verify(exactly = 1) { repository.observeFlagEnabledStates() }
        verify(exactly = 1) { redFlagRepository.observeVoiceType() }
        confirmVerified(ttsEngine, repository, redFlagRepository)
    }

    @Test
    fun `onRedFlagPreviewClicked に SESSION_STOP を渡すと SessionStop イベントが再生される`() {
        every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
        every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
        every { textRepository.observeSectorYellowFlagText() } returns MutableStateFlow("")
        coEvery { ttsRepository.isAvailable() } returns true
        every { ttsEngine.speak(SpeechEvent.SessionStop, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onRedFlagPreviewClicked(RedFlagVoiceType.SESSION_STOP)

        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.SessionStop, false) }
        verify(exactly = 1) { repository.observeFlagEnabledStates() }
        verify(exactly = 1) { redFlagRepository.observeVoiceType() }
        confirmVerified(ttsEngine, repository, redFlagRepository)
    }

    @Test
    fun `カスタム文言とTTSの利用可否が UiState に反映される`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
            every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
            every { textRepository.observeSectorYellowFlagText() } returns MutableStateFlow("イエロー、注意")
            coEvery { ttsRepository.isAvailable() } returns true
            val viewModel = createViewModel()

            val state = viewModel.uiState.first()

            assertEquals("イエロー、注意", state.sectorYellowFlagText)
            assertTrue(state.isTextToSpeechAvailable)
        }

    @Test
    fun `TTSを利用できない場合は isTextToSpeechAvailable が false になる`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
            every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
            every { textRepository.observeSectorYellowFlagText() } returns MutableStateFlow("")
            coEvery { ttsRepository.isAvailable() } returns false
            val viewModel = createViewModel()

            assertFalse(viewModel.uiState.first().isTextToSpeechAvailable)
        }

    @Test
    fun `onSectorYellowFlagTextChanged を呼ぶと UiState が更新される`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
            every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
            val textFlow = MutableStateFlow("")
            every { textRepository.observeSectorYellowFlagText() } returns textFlow
            coEvery { textRepository.saveSectorYellowFlagText("イエロー、注意") } answers {
                textFlow.update { "イエロー、注意" }
            }
            coEvery { ttsRepository.isAvailable() } returns true
            val viewModel = createViewModel()

            viewModel.onSectorYellowFlagTextChanged("イエロー、注意")

            assertEquals("イエロー、注意", viewModel.uiState.first().sectorYellowFlagText)
            coVerify(exactly = 1) { textRepository.saveSectorYellowFlagText("イエロー、注意") }
        }

    @Test
    fun `onSectorYellowFlagTextPreviewClicked は入力文言をTTSで読み上げる`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
            every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
            every { textRepository.observeSectorYellowFlagText() } returns MutableStateFlow("")
            coEvery { ttsRepository.isAvailable() } returns true
            val viewModel = createViewModel()

            viewModel.onSectorYellowFlagTextPreviewClicked("イエロー、注意")

            coVerify(exactly = 1) { ttsRepository.speak("イエロー、注意", false) }
            confirmVerified(ttsEngine)
        }

    @Test
    fun `onSectorYellowFlagTextPreviewClicked は文言が空なら収録音声を再生する`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
            every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
            every { textRepository.observeSectorYellowFlagText() } returns MutableStateFlow("")
            coEvery { ttsRepository.isAvailable() } returns true
            every { ttsEngine.speak(SpeechEvent.YellowFlag, false) } returns Unit
            val viewModel = createViewModel()

            viewModel.onSectorYellowFlagTextPreviewClicked("")

            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.YellowFlag, false) }
            confirmVerified(ttsEngine)
        }
}
