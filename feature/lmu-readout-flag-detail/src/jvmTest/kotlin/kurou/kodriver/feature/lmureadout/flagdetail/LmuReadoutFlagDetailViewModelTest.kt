@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.lmureadout.flagdetail

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
    override val currentReadoutItemKey: String? = null
    override fun speak(event: SpeechEvent) = onSpeak(event)
    override fun stop() = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class LmuReadoutFlagDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeFlagPreferencesRepository
    private val playedEvents = mutableListOf<SpeechEvent>()
    private lateinit var viewModel: LmuReadoutFlagDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeFlagPreferencesRepository()
        viewModel = LmuReadoutFlagDetailViewModel(
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
        assertEquals(true, state.enabledStates[ReadoutItemKey.BLUE_FLAG])
        assertEquals(true, state.enabledStates[ReadoutItemKey.SECTOR_YELLOW_FLAG])
        assertEquals(true, state.enabledStates[ReadoutItemKey.FULL_COURSE_YELLOW])
        assertEquals(true, state.enabledStates[ReadoutItemKey.RED_FLAG])
    }

    @Test
    fun `onFlagEnabledChanged を呼ぶと UiState が更新される`() = runTest {
        viewModel.onFlagEnabledChanged(ReadoutItemKey.BLUE_FLAG, false)
        assertEquals(false, viewModel.uiState.first().enabledStates[ReadoutItemKey.BLUE_FLAG])
    }

    @Test
    fun `onPreviewClicked に BLUE_FLAG を渡すと BlueFlag イベントが再生される`() {
        viewModel.onPreviewClicked(ReadoutItemKey.BLUE_FLAG)
        assertEquals(listOf<SpeechEvent>(SpeechEvent.BlueFlag), playedEvents)
    }

    @Test
    fun `onPreviewClicked に SECTOR_YELLOW_FLAG を渡すと YellowFlag イベントが再生される`() {
        viewModel.onPreviewClicked(ReadoutItemKey.SECTOR_YELLOW_FLAG)
        assertEquals(listOf<SpeechEvent>(SpeechEvent.YellowFlag), playedEvents)
    }

    @Test
    fun `onPreviewClicked に FULL_COURSE_YELLOW を渡すと FullCourseYellow イベントが再生される`() {
        viewModel.onPreviewClicked(ReadoutItemKey.FULL_COURSE_YELLOW)
        assertEquals(listOf<SpeechEvent>(SpeechEvent.FullCourseYellow), playedEvents)
    }

    @Test
    fun `onPreviewClicked に RED_FLAG を渡すと SessionStop イベントが再生される`() {
        viewModel.onPreviewClicked(ReadoutItemKey.RED_FLAG)
        assertEquals(listOf<SpeechEvent>(SpeechEvent.SessionStop), playedEvents)
    }

    @Test
    fun `onPreviewClicked に不明なキーを渡しても何も再生されない`() {
        viewModel.onPreviewClicked("unknown_key")
        assertEquals(emptyList<SpeechEvent>(), playedEvents)
    }
}
