@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.usecase.ObserveFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveFlagEnabledStateUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeTextToSpeechEngine(
    private val onSpeak: (SpeechEvent) -> Unit,
) : TextToSpeechEngine {
    override val currentReadoutItemKey: ReadoutItemKey? = null
    override fun speak(event: SpeechEvent, queue: Boolean) = onSpeak(event)
    override fun stop() = Unit
    override fun previewStartSound(type: ReadoutStartSoundType) = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutFlagDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeFlagPreferencesRepository
    private val playedEvents = mutableListOf<SpeechEvent>()
    private lateinit var viewModel: LmuWindowsReadoutFlagDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeFlagPreferencesRepository()
        viewModel = LmuWindowsReadoutFlagDetailViewModel(
            observeFlagEnabledStates = ObserveFlagEnabledStatesUseCase(repository),
            saveFlagEnabledState = SaveFlagEnabledStateUseCase(repository),
            playSpeechEvent = PlaySpeechEventUseCase(FakeTextToSpeechEngine { playedEvents.add(it) }),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態はすべてのフラグが enabled=true の UiState を返す`() = runTest {
        val state = viewModel.uiState.first()
        assertEquals(true, state.enabledStates[ReadoutItemKey.BlueFlag])
        assertEquals(true, state.enabledStates[ReadoutItemKey.SectorYellowFlag])
        assertEquals(true, state.enabledStates[ReadoutItemKey.FullCourseYellow])
        assertEquals(true, state.enabledStates[ReadoutItemKey.RedFlag])
    }

    @Test
    fun `onFlagEnabledChanged を呼ぶと UiState が更新される`() = runTest {
        viewModel.onFlagEnabledChanged(FlagReadoutItem.BlueFlag, false)
        assertEquals(false, viewModel.uiState.first().enabledStates[ReadoutItemKey.BlueFlag])
    }

    @Test
    fun `onPreviewClicked に BlueFlag を渡すと BlueFlag イベントが再生される`() {
        viewModel.onPreviewClicked(FlagReadoutItem.BlueFlag)
        assertEquals(listOf<SpeechEvent>(SpeechEvent.BlueFlag), playedEvents)
    }

    @Test
    fun `onPreviewClicked に SectorYellowFlag を渡すと YellowFlag イベントが再生される`() {
        viewModel.onPreviewClicked(FlagReadoutItem.SectorYellowFlag)
        assertEquals(listOf<SpeechEvent>(SpeechEvent.YellowFlag), playedEvents)
    }

    @Test
    fun `onPreviewClicked に FullCourseYellow を渡すと FullCourseYellow イベントが再生される`() {
        viewModel.onPreviewClicked(FlagReadoutItem.FullCourseYellow)
        assertEquals(listOf<SpeechEvent>(SpeechEvent.FullCourseYellow), playedEvents)
    }

    @Test
    fun `onPreviewClicked に RedFlag を渡すと SessionStop イベントが再生される`() {
        viewModel.onPreviewClicked(FlagReadoutItem.RedFlag)
        assertEquals(listOf<SpeechEvent>(SpeechEvent.SessionStop), playedEvents)
    }
}
