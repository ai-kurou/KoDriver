@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail

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
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelLapsUseCase
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
class Gt7Ps5ReadoutRemainingFuelLapsDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeGt7Ps5RemainingFuelLapsPreferencesRepository
    private val playedEvents = mutableListOf<SpeechEvent>()
    private lateinit var viewModel: Gt7Ps5ReadoutRemainingFuelLapsDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeGt7Ps5RemainingFuelLapsPreferencesRepository()
        viewModel = Gt7Ps5ReadoutRemainingFuelLapsDetailViewModel(
            observeGt7Ps5RemainingFuelLaps = ObserveGt7Ps5RemainingFuelLapsUseCase(repository),
            saveGt7Ps5RemainingFuelLaps = SaveGt7Ps5RemainingFuelLapsUseCase(repository),
            playSpeechEvent = PlaySpeechEventUseCase(FakeTextToSpeechEngine { playedEvents.add(it) }),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態は燃料残り周回数3のUiStateを返す`() = runTest {
        assertEquals(3, viewModel.uiState.first().remainingFuelLaps)
    }

    @Test
    fun `onRemainingFuelLapsChangedに1を渡すと燃料残り周回数が1になる`() = runTest {
        viewModel.onRemainingFuelLapsChanged(1)

        assertEquals(1, viewModel.uiState.first().remainingFuelLaps)
    }

    @Test
    fun `onResetRemainingFuelLapsを呼ぶと燃料残り周回数が3になる`() = runTest {
        repository.saveRemainingFuelLaps(5)

        viewModel.onResetRemainingFuelLaps()

        assertEquals(3, viewModel.uiState.first().remainingFuelLaps)
    }

    @Test
    fun `onPreviewClickedを呼ぶと設定中の燃料残り周回数イベントが再生される`() = runTest {
        viewModel.onRemainingFuelLapsChanged(4)
        assertEquals(4, viewModel.uiState.first().remainingFuelLaps)

        viewModel.onPreviewClicked()

        assertEquals(listOf<SpeechEvent>(SpeechEvent.RemainingFuelLapsWarning(4)), playedEvents)
    }
}
