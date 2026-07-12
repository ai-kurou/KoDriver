@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
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
import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsFlagEnabledStateUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutFlagDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: LmuWindowsFlagPreferencesRepository

    @MockK
    private lateinit var ttsEngine: TextToSpeechEngine

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = LmuWindowsReadoutFlagDetailViewModel(
        observeFlagEnabledStates = ObserveLmuWindowsFlagEnabledStatesUseCase(repository),
        saveFlagEnabledState = SaveLmuWindowsFlagEnabledStateUseCase(repository),
        playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
    )

    @Test
    fun `初期状態はすべてのフラグが enabled=true の UiState を返す`() = runTest {
        every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
        val viewModel = createViewModel()

        val state = viewModel.uiState.first()

        assertEquals(true, state.enabledStates[ReadoutItemKey.LmuWindows.Flag.BlueFlag])
        assertEquals(true, state.enabledStates[ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag])
        assertEquals(true, state.enabledStates[ReadoutItemKey.LmuWindows.Flag.FullCourseYellow])
        assertEquals(true, state.enabledStates[ReadoutItemKey.LmuWindows.Flag.RedFlag])
        verify(exactly = 1) { repository.observeFlagEnabledStates() }
        confirmVerified(repository)
    }

    @Test
    fun `onFlagEnabledChanged を呼ぶと UiState が更新される`() = runTest {
        val statesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
        every { repository.observeFlagEnabledStates() } returns statesFlow
        coEvery { repository.saveFlagEnabledState(ReadoutItemKey.LmuWindows.Flag.BlueFlag, false) } answers {
            statesFlow.update { it + (ReadoutItemKey.LmuWindows.Flag.BlueFlag to false) }
        }
        val viewModel = createViewModel()

        viewModel.onFlagEnabledChanged(FlagReadoutItem.BlueFlag, false)

        assertEquals(false, viewModel.uiState.first().enabledStates[ReadoutItemKey.LmuWindows.Flag.BlueFlag])
        coVerify(exactly = 1) { repository.saveFlagEnabledState(ReadoutItemKey.LmuWindows.Flag.BlueFlag, false) }
    }

    @Test
    fun `onPreviewClicked に BlueFlag を渡すと BlueFlag イベントが再生される`() {
        every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
        every { ttsEngine.speak(SpeechEvent.BlueFlag, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPreviewClicked(FlagReadoutItem.BlueFlag)

        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.BlueFlag, false) }
    }

    @Test
    fun `onPreviewClicked に SectorYellowFlag を渡すと YellowFlag イベントが再生される`() {
        every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
        every { ttsEngine.speak(SpeechEvent.YellowFlag, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPreviewClicked(FlagReadoutItem.SectorYellowFlag)

        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.YellowFlag, false) }
    }

    @Test
    fun `onPreviewClicked に FullCourseYellow を渡すと FullCourseYellow イベントが再生される`() {
        every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
        every { ttsEngine.speak(SpeechEvent.FullCourseYellow, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPreviewClicked(FlagReadoutItem.FullCourseYellow)

        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.FullCourseYellow, false) }
    }

    @Test
    fun `onPreviewClicked に RedFlag を渡すと SessionStop イベントが再生される`() {
        every { repository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
        every { ttsEngine.speak(SpeechEvent.SessionStop, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPreviewClicked(FlagReadoutItem.RedFlag)

        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.SessionStop, false) }
    }
}
