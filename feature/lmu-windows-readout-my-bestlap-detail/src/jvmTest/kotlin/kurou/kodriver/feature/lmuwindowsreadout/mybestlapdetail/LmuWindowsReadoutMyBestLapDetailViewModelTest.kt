package kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.usecase.ObserveLmuWindowsMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsMyBestLapVoiceTypeUseCase
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
class LmuWindowsReadoutMyBestLapDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeLmuWindowsMyBestLapPreferencesRepository
    private val playedEvents = mutableListOf<SpeechEvent>()
    private lateinit var viewModel: LmuWindowsReadoutMyBestLapDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeLmuWindowsMyBestLapPreferencesRepository()
        viewModel = createViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態は voiceType=FORMAL の UiState を返す`() = runTest {
        val state = viewModel.uiState.first()

        assertEquals(MyBestLapVoiceType.FORMAL, state.voiceType)
    }

    @Test
    fun `保存済み音声タイプが CASUAL なら voiceType=CASUAL の UiState を返す`() = runTest {
        repository = FakeLmuWindowsMyBestLapPreferencesRepository(MyBestLapVoiceType.CASUAL)
        viewModel = createViewModel()

        val state = viewModel.uiState.first()

        assertEquals(MyBestLapVoiceType.CASUAL, state.voiceType)
    }

    @Test
    fun `onVoiceTypeChanged に CASUAL を渡すと voiceType=CASUAL になる`() = runTest {
        viewModel.onVoiceTypeChanged(MyBestLapVoiceType.CASUAL)

        assertEquals(MyBestLapVoiceType.CASUAL, viewModel.uiState.first().voiceType)
    }

    @Test
    fun `onVoiceTypeChanged に FORMAL を渡すと voiceType=FORMAL になる`() = runTest {
        repository.saveVoiceType(MyBestLapVoiceType.CASUAL)

        viewModel.onVoiceTypeChanged(MyBestLapVoiceType.FORMAL)

        assertEquals(MyBestLapVoiceType.FORMAL, viewModel.uiState.first().voiceType)
    }

    @Test
    fun `onPreviewClicked に FORMAL を渡すと MyBestLapFormal イベントが再生される`() {
        viewModel.onPreviewClicked(MyBestLapVoiceType.FORMAL)

        assertEquals(listOf<SpeechEvent>(SpeechEvent.MyBestLapFormal), playedEvents)
    }

    @Test
    fun `onPreviewClicked に CASUAL を渡すと MyBestLapCasual イベントが再生される`() {
        viewModel.onPreviewClicked(MyBestLapVoiceType.CASUAL)

        assertEquals(listOf<SpeechEvent>(SpeechEvent.MyBestLapCasual), playedEvents)
    }

    private fun createViewModel() = LmuWindowsReadoutMyBestLapDetailViewModel(
        observeMyBestLapVoiceType = ObserveLmuWindowsMyBestLapVoiceTypeUseCase(repository),
        saveMyBestLapVoiceType = SaveLmuWindowsMyBestLapVoiceTypeUseCase(repository),
        playSpeechEvent = PlaySpeechEventUseCase(FakeTextToSpeechEngine { playedEvents.add(it) }),
    )
}
