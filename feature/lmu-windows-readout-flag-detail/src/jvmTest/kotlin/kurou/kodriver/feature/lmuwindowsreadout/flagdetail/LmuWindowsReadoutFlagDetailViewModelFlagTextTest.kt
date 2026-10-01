@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
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
import kurou.kodriver.domain.usecase.SaveLmuWindowsFlagTextAndRecordedVoiceSelectedUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsRedFlagReadoutTextUseCase
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
class LmuWindowsReadoutFlagDetailViewModelFlagTextTest {
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
                            saveTextAndRecordedVoiceSelected =
                                SaveLmuWindowsFlagTextAndRecordedVoiceSelectedUseCase(textRepository),
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
    fun `カスタム文言とTTSの利用可否が UiState に反映される`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
            every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
            stubReadoutTexts(sectorYellow = "イエロー、注意", blue = "ブルー、譲って", fullCourseYellow = "フルコース、減速", red = "赤旗、停止")
            coEvery { ttsRepository.isAvailable() } returns true
            val viewModel = createViewModel()

            val state = viewModel.uiState.first()

            assertEquals("イエロー、注意", state.flagText(FlagReadoutItem.SectorYellowFlag))
            assertEquals("ブルー、譲って", state.flagText(FlagReadoutItem.BlueFlag))
            assertEquals("フルコース、減速", state.flagText(FlagReadoutItem.FullCourseYellow))
            assertEquals("赤旗、停止", state.flagText(FlagReadoutItem.RedFlag))
            assertTrue(state.isTextToSpeechAvailable)
            verify(exactly = 1) { repository.observeFlagEnabledStates() }
            verify(exactly = 1) { redFlagRepository.observeVoiceType() }
            verifyReadoutTextsObserved()
            coVerify(exactly = 1) { ttsRepository.isAvailable() }
            confirmVerified(repository, redFlagRepository, textRepository, ttsRepository)
        }

    @Test
    fun `TTSを利用できない場合は isTextToSpeechAvailable が false になる`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
            every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
            stubReadoutTexts()
            coEvery { ttsRepository.isAvailable() } returns false
            val viewModel = createViewModel()

            assertFalse(viewModel.uiState.first().isTextToSpeechAvailable)
            verify(exactly = 1) { repository.observeFlagEnabledStates() }
            verify(exactly = 1) { redFlagRepository.observeVoiceType() }
            verifyReadoutTextsObserved()
            coVerify(exactly = 1) { ttsRepository.isAvailable() }
            confirmVerified(repository, redFlagRepository, textRepository, ttsRepository)
        }

    @Test
    fun `onFlagTextChanged はフラッグごとの保存先に文言を保存し UiState が更新される`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
            every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
            val yellowFlow = MutableStateFlow("")
            val blueFlow = MutableStateFlow("")
            val fullCourseYellowFlow = MutableStateFlow("")
            val redFlow = MutableStateFlow("")
            every { textRepository.observeSectorYellowFlagText() } returns yellowFlow
            every { textRepository.observeBlueFlagText() } returns blueFlow
            every { textRepository.observeFullCourseYellowFlagText() } returns fullCourseYellowFlow
            every { textRepository.observeRedFlagText() } returns redFlow
            stubRecordedVoiceSelected()
            coEvery {
                textRepository.saveTextAndRecordedVoiceSelected(
                    LmuWindowsFlagReadoutTarget.SECTOR_YELLOW_FLAG,
                    "イエロー、注意",
                    false,
                )
            } answers { yellowFlow.update { "イエロー、注意" } }
            coEvery {
                textRepository.saveTextAndRecordedVoiceSelected(
                    LmuWindowsFlagReadoutTarget.BLUE_FLAG,
                    "ブルー、譲って",
                    false,
                )
            } answers { blueFlow.update { "ブルー、譲って" } }
            coEvery {
                textRepository.saveTextAndRecordedVoiceSelected(
                    LmuWindowsFlagReadoutTarget.FULL_COURSE_YELLOW,
                    "フルコース、減速",
                    false,
                )
            } answers { fullCourseYellowFlow.update { "フルコース、減速" } }
            coEvery {
                textRepository.saveTextAndRecordedVoiceSelected(
                    LmuWindowsFlagReadoutTarget.RED_FLAG,
                    "赤旗、停止",
                    false,
                )
            } answers { redFlow.update { "赤旗、停止" } }
            coEvery { ttsRepository.isAvailable() } returns true
            val viewModel = createViewModel()

            viewModel.onFlagTextChanged(FlagReadoutItem.SectorYellowFlag, "イエロー、注意")
            viewModel.onFlagTextChanged(FlagReadoutItem.BlueFlag, "ブルー、譲って")
            viewModel.onFlagTextChanged(FlagReadoutItem.FullCourseYellow, "フルコース、減速")
            viewModel.onFlagTextChanged(FlagReadoutItem.RedFlag, "赤旗、停止")

            val state = viewModel.uiState.first()
            assertEquals("イエロー、注意", state.flagText(FlagReadoutItem.SectorYellowFlag))
            assertEquals("ブルー、譲って", state.flagText(FlagReadoutItem.BlueFlag))
            assertEquals("フルコース、減速", state.flagText(FlagReadoutItem.FullCourseYellow))
            assertEquals("赤旗、停止", state.flagText(FlagReadoutItem.RedFlag))
            coVerify(exactly = 1) {
                textRepository.saveTextAndRecordedVoiceSelected(
                    LmuWindowsFlagReadoutTarget.SECTOR_YELLOW_FLAG,
                    "イエロー、注意",
                    false,
                )
            }
            coVerify(exactly = 1) {
                textRepository.saveTextAndRecordedVoiceSelected(
                    LmuWindowsFlagReadoutTarget.BLUE_FLAG,
                    "ブルー、譲って",
                    false,
                )
            }
            coVerify(exactly = 1) {
                textRepository.saveTextAndRecordedVoiceSelected(
                    LmuWindowsFlagReadoutTarget.FULL_COURSE_YELLOW,
                    "フルコース、減速",
                    false,
                )
            }
            coVerify(exactly = 1) {
                textRepository.saveTextAndRecordedVoiceSelected(
                    LmuWindowsFlagReadoutTarget.RED_FLAG,
                    "赤旗、停止",
                    false,
                )
            }
            verify(exactly = 1) { repository.observeFlagEnabledStates() }
            verify(exactly = 1) { redFlagRepository.observeVoiceType() }
            verifyReadoutTextsObserved()
            coVerify(exactly = 1) { ttsRepository.isAvailable() }
            confirmVerified(repository, redFlagRepository, textRepository, ttsRepository)
        }

    @Test
    fun `onFlagTextPreviewClicked は開始音を鳴らしてから入力文言をTTSで読み上げる`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
            every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
            stubReadoutTexts()
            coEvery { ttsRepository.isAvailable() } returns true
            FlagReadoutItem.entries.forEach { item -> coEvery { ttsEngine.playStartSound(item.key) } just Runs }
            val viewModel = createViewModel()

            FlagReadoutItem.entries.forEach { item ->
                viewModel.onFlagTextPreviewClicked(item, "${item.name}の文言")
            }

            FlagReadoutItem.entries.forEach { item ->
                coVerifyOrder {
                    ttsEngine.playStartSound(item.key)
                    ttsRepository.speak("${item.name}の文言", false)
                }
                coVerify(exactly = 1) { ttsEngine.playStartSound(item.key) }
                coVerify(exactly = 1) { ttsRepository.speak("${item.name}の文言", false) }
            }
            verify(exactly = 1) { repository.observeFlagEnabledStates() }
            verify(exactly = 1) { redFlagRepository.observeVoiceType() }
            verifyReadoutTextsObserved()
            coVerify(exactly = 1) { ttsRepository.isAvailable() }
            confirmVerified(repository, redFlagRepository, textRepository, ttsRepository, ttsEngine)
        }

    @Test
    fun `onFlagTextPreviewClicked は文言が空ならレッドフラッグ以外は収録音声を再生する`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
            every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
            stubReadoutTexts()
            coEvery { ttsRepository.isAvailable() } returns true
            val items = FlagReadoutItem.entries - FlagReadoutItem.RedFlag
            items.forEach { item -> every { ttsEngine.speak(item.previewEvent, false) } returns Unit }
            val viewModel = createViewModel()

            items.forEach { item -> viewModel.onFlagTextPreviewClicked(item, "") }

            items.forEach { item ->
                verify(exactly = 1) { ttsEngine.speak(item.previewEvent, false) }
            }
            verify(exactly = 1) { repository.observeFlagEnabledStates() }
            verify(exactly = 1) { redFlagRepository.observeVoiceType() }
            verifyReadoutTextsObserved()
            coVerify(exactly = 1) { ttsRepository.isAvailable() }
            confirmVerified(repository, redFlagRepository, textRepository, ttsRepository, ttsEngine)
        }

    @Test
    fun `onFlagTextPreviewClicked はレッドフラッグの文言が空なら選択中の音声種別の収録音声を再生する`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
            val voiceTypeFlow = MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
            every { redFlagRepository.observeVoiceType() } returns voiceTypeFlow
            stubReadoutTexts()
            coEvery { ttsRepository.isAvailable() } returns true
            every { ttsEngine.speak(SpeechEvent.SessionStop, false) } returns Unit
            every { ttsEngine.speak(SpeechEvent.RedFlag, false) } returns Unit
            val viewModel = createViewModel()

            viewModel.onFlagTextPreviewClicked(FlagReadoutItem.RedFlag, "")
            voiceTypeFlow.value = RedFlagVoiceType.RED_FLAG
            viewModel.onFlagTextPreviewClicked(FlagReadoutItem.RedFlag, " ")

            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.SessionStop, false) }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.RedFlag, false) }
            verify(exactly = 3) { redFlagRepository.observeVoiceType() } // 初期化時に1回、試聴ごとに1回
            verify(exactly = 1) { repository.observeFlagEnabledStates() }
            verifyReadoutTextsObserved()
            coVerify(exactly = 1) { ttsRepository.isAvailable() }
            confirmVerified(repository, redFlagRepository, textRepository, ttsRepository, ttsEngine)
        }

    @Test
    fun `空文字を入力しても収録音声の明示選択は解除しない`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
            every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
            stubReadoutTexts()
            coEvery { ttsRepository.isAvailable() } returns true
            val viewModel = createViewModel()

            viewModel.onFlagTextChanged(FlagReadoutItem.BlueFlag, "")
            viewModel.uiState.first()

            coVerify(exactly = 1) { textRepository.saveBlueFlagText("") }
            verify(exactly = 1) { repository.observeFlagEnabledStates() }
            verify(exactly = 1) { redFlagRepository.observeVoiceType() }
            verifyReadoutTextsObserved()
            coVerify(exactly = 1) { ttsRepository.isAvailable() }
            confirmVerified(repository, redFlagRepository, textRepository, ttsRepository)
        }

    @Test
    fun `onRecordedVoiceSelected はフラッグごとの収録音声選択を保存してから試聴を呼び文言は変更しない`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
            every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
            stubReadoutTexts()
            coEvery { ttsRepository.isAvailable() } returns true
            val viewModel = createViewModel()

            var previewCount = 0
            FlagReadoutItem.entries.forEach { viewModel.onRecordedVoiceSelected(it) { previewCount++ } }
            viewModel.uiState.first()

            assertEquals(FlagReadoutItem.entries.size, previewCount)

            FlagReadoutItem.entries.forEach { item ->
                coVerify(exactly = 1) { textRepository.saveRecordedVoiceSelected(item.target, true) }
            }
            verify(exactly = 1) { repository.observeFlagEnabledStates() }
            verify(exactly = 1) { redFlagRepository.observeVoiceType() }
            verifyReadoutTextsObserved()
            coVerify(exactly = 1) { ttsRepository.isAvailable() }
            confirmVerified(repository, redFlagRepository, textRepository, ttsRepository)
        }

    @Test
    fun `レッド以外は収録音声が選ばれていてもカスタム文言を使う`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
            every { redFlagRepository.observeVoiceType() } returns MutableStateFlow(RedFlagVoiceType.SESSION_STOP)
            stubReadoutTexts(sectorYellow = "イエロー、注意", blue = "ブルー、譲って", fullCourseYellow = "減速", red = "赤旗、停止")
            stubRecordedVoiceSelected()
            every { textRepository.observeRecordedVoiceSelected(LmuWindowsFlagReadoutTarget.BLUE_FLAG) } returns
                MutableStateFlow(true)
            every {
                textRepository.observeRecordedVoiceSelected(LmuWindowsFlagReadoutTarget.SECTOR_YELLOW_FLAG)
            } returns MutableStateFlow(true)
            every {
                textRepository.observeRecordedVoiceSelected(
                    LmuWindowsFlagReadoutTarget.FULL_COURSE_YELLOW,
                )
            } returns
                MutableStateFlow(true)
            coEvery { ttsRepository.isAvailable() } returns true
            val viewModel = createViewModel()

            val state = viewModel.uiState.first()

            assertTrue(state.isRecordedVoiceSelected(FlagReadoutItem.BlueFlag))
            assertTrue(state.isCustomTextSelected(FlagReadoutItem.BlueFlag))
            assertTrue(state.isCustomTextSelected(FlagReadoutItem.RedFlag))
            assertTrue(state.isCustomTextSelected(FlagReadoutItem.SectorYellowFlag))
            assertTrue(state.isCustomTextSelected(FlagReadoutItem.FullCourseYellow))
            verify(exactly = 1) { repository.observeFlagEnabledStates() }
            verify(exactly = 1) { redFlagRepository.observeVoiceType() }
            verifyReadoutTextsObserved()
            coVerify(exactly = 1) { ttsRepository.isAvailable() }
            confirmVerified(repository, redFlagRepository, textRepository, ttsRepository)
        }
}
