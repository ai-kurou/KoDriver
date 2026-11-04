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
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.model.SpeechRecognitionFailureReason
import kurou.kodriver.domain.repository.RecordAudioPermissionRepository
import kurou.kodriver.domain.repository.SpeechRecognitionRepository
import kurou.kodriver.domain.usecase.CheckRecordAudioPermissionGrantedUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechRecognitionUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SpeechRecognitionViewModelIdleTimeoutTest {
    private val speechRepository: SpeechRecognitionRepository = mockk()
    private val permissionRepository: RecordAudioPermissionRepository = mockk()
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

    private fun createViewModel(granted: Boolean): SpeechRecognitionViewModel {
        every { permissionRepository.isGranted() } returns granted
        every { speechRepository.observeRecognition() } returns events
        return SpeechRecognitionViewModel(
            observeSpeechRecognition = ObserveSpeechRecognitionUseCase(speechRepository),
            checkRecordAudioPermissionGranted = CheckRecordAudioPermissionGrantedUseCase(permissionRepository),
        ).also { viewModel = it }
    }

    private fun advanceBy(millis: Long) {
        dispatcher.scheduler.advanceTimeBy(millis)
        dispatcher.scheduler.runCurrent()
    }

    @Test
    fun `発話がないまま一定時間経つと聞き取りを自動で止める`() {
        val viewModel = createViewModel(granted = true)
        viewModel.onEnabledChange(true) {}
        events.tryEmit(SpeechRecognitionEvent.Listening)

        advanceBy(SPEECH_RECOGNITION_IDLE_TIMEOUT_MS - 1)
        assertTrue(viewModel.uiState.value.enabled)

        advanceBy(1)
        assertEquals(
            SpeechRecognitionUiState(enabled = false, status = SpeechRecognitionStatus.IdleStopped),
            viewModel.uiState.value,
        )
        assertEquals(0, events.subscriptionCount.value)
    }

    @Test
    fun `認識途中の文字列が届くたびに無発話の時間を数え直す`() {
        val viewModel = createViewModel(granted = true)
        viewModel.onEnabledChange(true) {}
        advanceBy(SPEECH_RECOGNITION_IDLE_TIMEOUT_MS - 1)

        events.tryEmit(SpeechRecognitionEvent.Partial("燃"))
        advanceBy(SPEECH_RECOGNITION_IDLE_TIMEOUT_MS - 1)
        assertTrue(viewModel.uiState.value.enabled)

        advanceBy(1)
        assertEquals(SpeechRecognitionStatus.IdleStopped, viewModel.uiState.value.status)
    }

    @Test
    fun `確定結果が届くたびに無発話の時間を数え直し履歴は残す`() {
        val viewModel = createViewModel(granted = true)
        viewModel.onEnabledChange(true) {}
        advanceBy(SPEECH_RECOGNITION_IDLE_TIMEOUT_MS - 1)

        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("燃料")))
        advanceBy(SPEECH_RECOGNITION_IDLE_TIMEOUT_MS - 1)
        assertTrue(viewModel.uiState.value.enabled)

        advanceBy(1)
        assertEquals(SpeechRecognitionStatus.IdleStopped, viewModel.uiState.value.status)
        assertEquals(listOf(listOf("燃料")), viewModel.uiState.value.recognized)
    }

    @Test
    fun `聞き取りの再開を知らせるListeningでは無発話の時間を数え直さない`() {
        val viewModel = createViewModel(granted = true)
        viewModel.onEnabledChange(true) {}
        advanceBy(SPEECH_RECOGNITION_IDLE_TIMEOUT_MS - 1)

        events.tryEmit(SpeechRecognitionEvent.Listening)
        advanceBy(1)

        assertEquals(SpeechRecognitionStatus.IdleStopped, viewModel.uiState.value.status)
    }

    @Test
    fun `停止したら無発話の自動停止は行わない`() {
        val viewModel = createViewModel(granted = true)
        viewModel.onEnabledChange(true) {}
        viewModel.stop()

        advanceBy(SPEECH_RECOGNITION_IDLE_TIMEOUT_MS)

        assertEquals(SpeechRecognitionStatus.Stopped, viewModel.uiState.value.status)
    }

    @Test
    fun `失敗で止まったら無発話の自動停止で状態を上書きしない`() {
        val viewModel = createViewModel(granted = true)
        viewModel.onEnabledChange(true) {}
        events.tryEmit(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.Unavailable))

        advanceBy(SPEECH_RECOGNITION_IDLE_TIMEOUT_MS)

        assertEquals(SpeechRecognitionStatus.Unavailable, viewModel.uiState.value.status)
    }

    @Test
    fun `再度ONにすると無発話の時間を最初から数える`() {
        val viewModel = createViewModel(granted = true)
        viewModel.onEnabledChange(true) {}
        advanceBy(SPEECH_RECOGNITION_IDLE_TIMEOUT_MS)
        assertEquals(SpeechRecognitionStatus.IdleStopped, viewModel.uiState.value.status)

        viewModel.onEnabledChange(true) {}
        advanceBy(SPEECH_RECOGNITION_IDLE_TIMEOUT_MS - 1)

        assertTrue(viewModel.uiState.value.enabled)
        assertEquals(SpeechRecognitionStatus.Starting, viewModel.uiState.value.status)
    }
}
