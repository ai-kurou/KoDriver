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
import kurou.kodriver.domain.model.LmuWindowsFlagReadoutTarget
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.RedFlagVoiceType
import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsRedFlagPreferencesRepository
import kurou.kodriver.domain.repository.TextToSpeechRepository
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagRecordedVoiceSelectedUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsFlagEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsFlagRecordedVoiceSelectedUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsRedFlagVoiceTypeUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

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
            settingsUseCases =
                FlagSettingsUseCases(
                    observeFlagEnabledStates = ObserveLmuWindowsFlagEnabledStatesUseCase(repository),
                    observeRedFlagVoiceType = ObserveLmuWindowsRedFlagVoiceTypeUseCase(redFlagRepository),
                    saveFlagEnabledState = SaveLmuWindowsFlagEnabledStateUseCase(repository),
                    saveRedFlagVoiceType = SaveLmuWindowsRedFlagVoiceTypeUseCase(redFlagRepository),
                    readoutTexts =
                        FlagReadoutTextUseCases(
                            observeSectorYellowFlag =
                                ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase(
                                    textRepository,
                                ),
                            observeBlueFlag = ObserveLmuWindowsBlueFlagReadoutTextUseCase(textRepository),
                            observeFullCourseYellow =
                                ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase(textRepository),
                            observeRedFlag = ObserveLmuWindowsRedFlagReadoutTextUseCase(textRepository),
                            saveSectorYellowFlag = SaveLmuWindowsSectorYellowFlagReadoutTextUseCase(textRepository),
                            saveBlueFlag = SaveLmuWindowsBlueFlagReadoutTextUseCase(textRepository),
                            saveFullCourseYellow =
                                SaveLmuWindowsFullCourseYellowFlagReadoutTextUseCase(textRepository),
                            saveRedFlag = SaveLmuWindowsRedFlagReadoutTextUseCase(textRepository),
                            observeRecordedVoiceSelected =
                                ObserveLmuWindowsFlagRecordedVoiceSelectedUseCase(textRepository),
                            saveRecordedVoiceSelected = SaveLmuWindowsFlagRecordedVoiceSelectedUseCase(textRepository),
                        ),
                ),
            playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
            speakText = SpeakTextUseCase(ttsRepository),
            playStartSoundForKey = PlayStartSoundForKeyUseCase(ttsEngine),
            checkTextToSpeechAvailable = CheckTextToSpeechAvailableUseCase(ttsRepository),
        )

    private fun stubRecordedVoiceSelected(selected: Boolean = false) {
        LmuWindowsFlagReadoutTarget.entries.forEach { target ->
            every { textRepository.observeRecordedVoiceSelected(target) } returns MutableStateFlow(selected)
        }
    }

    private fun stubReadoutTexts(
        sectorYellow: String = "",
        blue: String = "",
        fullCourseYellow: String = "",
        red: String = "",
    ) {
        every { textRepository.observeSectorYellowFlagText() } returns MutableStateFlow(sectorYellow)
        every { textRepository.observeBlueFlagText() } returns MutableStateFlow(blue)
        every { textRepository.observeFullCourseYellowFlagText() } returns MutableStateFlow(fullCourseYellow)
        every { textRepository.observeRedFlagText() } returns MutableStateFlow(red)
        stubRecordedVoiceSelected()
    }

    private fun verifyReadoutTextsObserved() {
        verify(exactly = 1) { textRepository.observeSectorYellowFlagText() }
        verify(exactly = 1) { textRepository.observeBlueFlagText() }
        verify(exactly = 1) { textRepository.observeFullCourseYellowFlagText() }
        verify(exactly = 1) { textRepository.observeRedFlagText() }
        LmuWindowsFlagReadoutTarget.entries.forEach { target ->
            verify(exactly = 1) { textRepository.observeRecordedVoiceSelected(target) }
        }
    }

    @Test
    fun `初期状態はすべてのフラグが enabled=true の UiState を返す`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
            every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
            stubReadoutTexts()
            coEvery { ttsRepository.isAvailable() } returns true
            val viewModel = createViewModel()

            val state = viewModel.uiState.first()

            assertEquals(true, state.enabledStates[ReadoutItemKey.LmuWindows.Flag.BlueFlag])
            assertEquals(true, state.enabledStates[ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag])
            assertEquals(true, state.enabledStates[ReadoutItemKey.LmuWindows.Flag.FullCourseYellow])
            assertEquals(true, state.enabledStates[ReadoutItemKey.LmuWindows.Flag.RedFlag])
            assertEquals(RedFlagVoiceType.SESSION_STOP, state.redFlagVoiceType)
            verify(exactly = 1) { repository.observeFlagEnabledStates() }
            verify(exactly = 1) { redFlagRepository.observeVoiceType() }
            verifyReadoutTextsObserved()
            coVerify(exactly = 1) { ttsRepository.isAvailable() }
            confirmVerified(repository, redFlagRepository, textRepository, ttsRepository)
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
            stubReadoutTexts()
            coEvery { ttsRepository.isAvailable() } returns true
            val viewModel = createViewModel()

            viewModel.onFlagEnabledChanged(FlagReadoutItem.BlueFlag, false)

            assertEquals(false, viewModel.uiState.first().enabledStates[ReadoutItemKey.LmuWindows.Flag.BlueFlag])
            coVerify(exactly = 1) { repository.saveFlagEnabledState(ReadoutItemKey.LmuWindows.Flag.BlueFlag, false) }
            verify(exactly = 1) { repository.observeFlagEnabledStates() }
            verify(exactly = 1) { redFlagRepository.observeVoiceType() }
            verifyReadoutTextsObserved()
            coVerify(exactly = 1) { ttsRepository.isAvailable() }
            confirmVerified(repository, redFlagRepository, textRepository, ttsRepository)
        }

    @Test
    fun `onFlagEnabledChanged にレッドフラッグを渡すと UiState が更新される`() =
        runTest {
            val statesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { repository.observeFlagEnabledStates() } returns statesFlow
            coEvery { repository.saveFlagEnabledState(ReadoutItemKey.LmuWindows.Flag.RedFlag, false) } answers {
                statesFlow.update { it + (ReadoutItemKey.LmuWindows.Flag.RedFlag to false) }
            }
            every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
            stubReadoutTexts()
            coEvery { ttsRepository.isAvailable() } returns true
            val viewModel = createViewModel()

            viewModel.onFlagEnabledChanged(FlagReadoutItem.RedFlag, false)

            assertEquals(false, viewModel.uiState.first().enabledStates[ReadoutItemKey.LmuWindows.Flag.RedFlag])
            coVerify(exactly = 1) { repository.saveFlagEnabledState(ReadoutItemKey.LmuWindows.Flag.RedFlag, false) }
            verify(exactly = 1) { repository.observeFlagEnabledStates() }
            verify(exactly = 1) { redFlagRepository.observeVoiceType() }
            verifyReadoutTextsObserved()
            coVerify(exactly = 1) { ttsRepository.isAvailable() }
            confirmVerified(repository, redFlagRepository, textRepository, ttsRepository)
        }

    @Test
    fun `onRedFlagVoiceTypeChanged を呼ぶと UiState が更新される`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
            val voiceTypeFlow = MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
            every { redFlagRepository.observeVoiceType() } returns voiceTypeFlow
            stubReadoutTexts()
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
            verifyReadoutTextsObserved()
            coVerify(exactly = 1) { ttsRepository.isAvailable() }
            confirmVerified(repository, redFlagRepository, textRepository, ttsRepository)
        }

    @Test
    fun `onPreviewClicked に BlueFlag を渡すと BlueFlag イベントが再生される`() {
        every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
        every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
        stubReadoutTexts()
        coEvery { ttsRepository.isAvailable() } returns true
        every { ttsEngine.speak(SpeechEvent.BlueFlag, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPreviewClicked(FlagReadoutItem.BlueFlag)

        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.BlueFlag, false) }
        verify(exactly = 1) { repository.observeFlagEnabledStates() }
        verify(exactly = 1) { redFlagRepository.observeVoiceType() }
        verifyReadoutTextsObserved()
        coVerify(exactly = 1) { ttsRepository.isAvailable() }
        confirmVerified(ttsEngine, repository, redFlagRepository, textRepository, ttsRepository)
    }

    @Test
    fun `onPreviewClicked に SectorYellowFlag を渡すと YellowFlag イベントが再生される`() {
        every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
        every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
        stubReadoutTexts()
        coEvery { ttsRepository.isAvailable() } returns true
        every { ttsEngine.speak(SpeechEvent.YellowFlag, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPreviewClicked(FlagReadoutItem.SectorYellowFlag)

        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.YellowFlag, false) }
        verify(exactly = 1) { repository.observeFlagEnabledStates() }
        verify(exactly = 1) { redFlagRepository.observeVoiceType() }
        verifyReadoutTextsObserved()
        coVerify(exactly = 1) { ttsRepository.isAvailable() }
        confirmVerified(ttsEngine, repository, redFlagRepository, textRepository, ttsRepository)
    }

    @Test
    fun `onPreviewClicked に FullCourseYellow を渡すと FullCourseYellow イベントが再生される`() {
        every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
        every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
        stubReadoutTexts()
        coEvery { ttsRepository.isAvailable() } returns true
        every { ttsEngine.speak(SpeechEvent.FullCourseYellow, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPreviewClicked(FlagReadoutItem.FullCourseYellow)

        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.FullCourseYellow, false) }
        verify(exactly = 1) { repository.observeFlagEnabledStates() }
        verify(exactly = 1) { redFlagRepository.observeVoiceType() }
        verifyReadoutTextsObserved()
        coVerify(exactly = 1) { ttsRepository.isAvailable() }
        confirmVerified(ttsEngine, repository, redFlagRepository, textRepository, ttsRepository)
    }

    @Test
    fun `onRedFlagPreviewClicked に RED_FLAG を渡すと RedFlag イベントが再生される`() {
        every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
        every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
        stubReadoutTexts()
        coEvery { ttsRepository.isAvailable() } returns true
        every { ttsEngine.speak(SpeechEvent.RedFlag, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onRedFlagPreviewClicked(RedFlagVoiceType.RED_FLAG)

        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.RedFlag, false) }
        verify(exactly = 1) { repository.observeFlagEnabledStates() }
        verify(exactly = 1) { redFlagRepository.observeVoiceType() }
        verifyReadoutTextsObserved()
        coVerify(exactly = 1) { ttsRepository.isAvailable() }
        confirmVerified(ttsEngine, repository, redFlagRepository, textRepository, ttsRepository)
    }

    @Test
    fun `onRedFlagPreviewClicked に SESSION_STOP を渡すと SessionStop イベントが再生される`() {
        every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
        every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
        stubReadoutTexts()
        coEvery { ttsRepository.isAvailable() } returns true
        every { ttsEngine.speak(SpeechEvent.SessionStop, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onRedFlagPreviewClicked(RedFlagVoiceType.SESSION_STOP)

        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.SessionStop, false) }
        verify(exactly = 1) { repository.observeFlagEnabledStates() }
        verify(exactly = 1) { redFlagRepository.observeVoiceType() }
        verifyReadoutTextsObserved()
        coVerify(exactly = 1) { ttsRepository.isAvailable() }
        confirmVerified(ttsEngine, repository, redFlagRepository, textRepository, ttsRepository)
    }
}
