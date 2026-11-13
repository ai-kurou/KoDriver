package kurou.kodriver.feature.main

import androidx.lifecycle.viewModelScope
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.SPEECH_RECOGNITION_RESUME_DELAY_MS_DEFAULT
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.model.SpeechRecognitionMode
import kurou.kodriver.domain.repository.RecordAudioPermissionRepository
import kurou.kodriver.domain.repository.SpeechRecognitionRepository
import kurou.kodriver.domain.usecase.CheckRecordAudioPermissionGrantedUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechPlaybackActiveUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechRecognitionResumeDelayUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechRecognitionUseCase
import kurou.kodriver.domain.usecase.PlaySpeechRecognitionChimeUseCase
import kurou.kodriver.domain.usecase.RequestManualReadoutItemUseCase
import kurou.kodriver.domain.usecase.SaveSpeechRecognitionResumeDelayUseCase
import kurou.kodriver.domain.usecase.SetSpeechRecognitionModeUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SpeechRecognitionViewModelResumeDelayTest {
    private val speechRepository: SpeechRecognitionRepository = mockk()
    private val permissionRepository: RecordAudioPermissionRepository = mockk()
    private val modeRepository = FakeSpeechRecognitionModeRepository()
    private val playbackRepository = FakeSpeechPlaybackStateRepository()
    private val resumeDelayRepository = FakeSpeechRecognitionResumeDelayPreferencesRepository()
    private val events = MutableSharedFlow<SpeechRecognitionEvent>(extraBufferCapacity = 16)
    private val dispatcher = UnconfinedTestDispatcher()
    private var viewModel: SpeechRecognitionViewModel? = null

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        viewModel?.viewModelScope?.cancel()
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        repository: FakeSpeechRecognitionResumeDelayPreferencesRepository? = resumeDelayRepository,
    ): SpeechRecognitionViewModel {
        every { permissionRepository.isGranted() } returns true
        every { speechRepository.observeRecognition() } returns events
        return SpeechRecognitionViewModel(
            observeSpeechRecognition = ObserveSpeechRecognitionUseCase(speechRepository),
            checkRecordAudioPermissionGranted = CheckRecordAudioPermissionGrantedUseCase(permissionRepository),
            requestManualReadoutItem = RequestManualReadoutItemUseCase(FakeManualReadoutItemRequestRepository()),
            setSpeechRecognitionMode = SetSpeechRecognitionModeUseCase(modeRepository),
            observeSpeechPlaybackActive = ObserveSpeechPlaybackActiveUseCase(playbackRepository),
            observeSelectedSimulator = ObserveSelectedSimulatorUseCase(FakeSelectedSimulatorRepository()),
            playChime = PlaySpeechRecognitionChimeUseCase(FakeSpeechRecognitionChimePlayer()),
            observeResumeDelay = repository?.let { ObserveSpeechRecognitionResumeDelayUseCase(it) },
            saveResumeDelay = repository?.let { SaveSpeechRecognitionResumeDelayUseCase(it) },
        ).also { viewModel = it }
    }

    private fun advanceBy(millis: Long) {
        dispatcher.scheduler.advanceTimeBy(millis)
        dispatcher.scheduler.runCurrent()
    }

    @Test
    fun `保存済みの再開待ち時間を表示に反映する`() {
        resumeDelayRepository.saved.value = 1_500

        assertEquals(1_500, createViewModel().uiState.value.resumeDelayMs)

        resumeDelayRepository.saved.value = 2_000

        assertEquals(2_000, viewModel?.uiState?.value?.resumeDelayMs)
    }

    @Test
    fun `再開待ち時間を選ぶと保存する`() {
        val viewModel = createViewModel()

        viewModel.onResumeDelaySelected(3_000)

        assertEquals(3_000, resumeDelayRepository.saved.value)
        assertEquals(3_000, viewModel.uiState.value.resumeDelayMs)
    }

    @Test
    fun `読み上げが終わってから保存した待ち時間が過ぎるまで再開しない`() {
        resumeDelayRepository.saved.value = 1_500
        val viewModel = createViewModel()
        viewModel.onEnabledChange(true) {}
        events.tryEmit(SpeechRecognitionEvent.Listening)
        playbackRepository.update("lmu_windows", true)
        modeRepository.modes.clear()

        playbackRepository.update("lmu_windows", false)
        advanceBy(1_499)

        assertEquals(emptyList(), modeRepository.modes)

        advanceBy(1)

        assertEquals(listOf(SpeechRecognitionMode.WakeWord), modeRepository.modes)
    }

    @Test
    fun `再開待ち時間の購読に失敗しても既定値のまま続ける`() {
        val repository =
            FakeSpeechRecognitionResumeDelayPreferencesRepository(readError = IllegalStateException("read"))

        assertEquals(
            SPEECH_RECOGNITION_RESUME_DELAY_MS_DEFAULT,
            createViewModel(repository).uiState.value.resumeDelayMs,
        )
    }

    @Test
    fun `再開待ち時間のUseCaseが無いときは選んでも何もしない`() {
        val viewModel = createViewModel(repository = null)

        viewModel.onResumeDelaySelected(3_000)

        assertEquals(SPEECH_RECOGNITION_RESUME_DELAY_MS_DEFAULT, viewModel.uiState.value.resumeDelayMs)
    }

    @Test
    fun `ミリ秒を秒の文字列にする`() {
        assertEquals("0.5", 500.toSecondsText())
        assertEquals("1.0", 1_000.toSecondsText())
        assertEquals("2.5", 2_500.toSecondsText())
    }
}
