package kurou.kodriver.feature.main

import androidx.lifecycle.viewModelScope
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.captureError
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.model.SpeechRecognitionFailureReason
import kurou.kodriver.domain.repository.RecordAudioPermissionRepository
import kurou.kodriver.domain.repository.SpeechRecognitionRepository
import kurou.kodriver.domain.usecase.CheckRecordAudioPermissionGrantedUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechRecognitionUseCase
import kurou.kodriver.domain.usecase.RequestManualReadoutItemUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SpeechRecognitionViewModelTest {
    private val speechRepository: SpeechRecognitionRepository = mockk()
    private val permissionRepository: RecordAudioPermissionRepository = mockk()
    private val itemRequestRepository = FakeManualReadoutItemRequestRepository()
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
            requestManualReadoutItem = RequestManualReadoutItemUseCase(itemRequestRepository),
        ).also { viewModel = it }
    }

    @Test
    fun `権限が許可済みでONにすると聞き取りを開始して準備中になる`() {
        val viewModel = createViewModel(granted = true)

        viewModel.onEnabledChange(true) { error("権限は要求しない") }

        assertEquals(
            SpeechRecognitionUiState(enabled = true, status = SpeechRecognitionStatus.Starting),
            viewModel.uiState.value,
        )
        verify(exactly = 1) { permissionRepository.isGranted() }
        verify(exactly = 1) { speechRepository.observeRecognition() }
        confirmVerified(permissionRepository, speechRepository)
    }

    @Test
    fun `認識イベントに応じて状態を更新する`() {
        val viewModel = createViewModel(granted = true)
        viewModel.onEnabledChange(true) {}

        events.tryEmit(SpeechRecognitionEvent.Listening)
        assertEquals(SpeechRecognitionStatus.Listening, viewModel.uiState.value.status)

        events.tryEmit(SpeechRecognitionEvent.Partial("燃"))
        assertEquals("燃", viewModel.uiState.value.partialText)

        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("燃料", "然料")))
        assertEquals("", viewModel.uiState.value.partialText)
        assertEquals(listOf(listOf("燃料", "然料")), viewModel.uiState.value.recognized)
    }

    @Test
    fun `入力音量を更新し、聞き取りを止めると消す`() {
        val viewModel = createViewModel(granted = true)
        viewModel.onEnabledChange(true) {}
        assertEquals(null, viewModel.uiState.value.level)

        events.tryEmit(SpeechRecognitionEvent.Level(0.3f))
        assertEquals(0.3f, viewModel.uiState.value.level)
        events.tryEmit(SpeechRecognitionEvent.Level(0.8f))
        assertEquals(0.8f, viewModel.uiState.value.level)

        viewModel.onEnabledChange(false) {}
        assertEquals(null, viewModel.uiState.value.level)
    }

    @Test
    fun `失敗すると入力音量を消す`() {
        val viewModel = createViewModel(granted = true)
        viewModel.onEnabledChange(true) {}
        events.tryEmit(SpeechRecognitionEvent.Level(0.3f))

        events.tryEmit(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.Unavailable))

        assertEquals(null, viewModel.uiState.value.level)
    }

    @Test
    fun `認識結果の履歴は新しい順で最大件数までにする`() {
        val viewModel = createViewModel(granted = true)
        viewModel.onEnabledChange(true) {}

        repeat(
            SPEECH_RECOGNITION_HISTORY_MAX + 1,
        ) { events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("結果$it"))) }

        val recognized = viewModel.uiState.value.recognized
        assertEquals(5, recognized.size)
        assertEquals(listOf("結果${SPEECH_RECOGNITION_HISTORY_MAX}"), recognized.first())
        assertEquals(listOf("結果1"), recognized.last())
    }

    @Test
    fun `権限が未許可でONにすると権限を要求して聞き取りは開始しない`() {
        val viewModel = createViewModel(granted = false)
        var requestCount = 0

        viewModel.onEnabledChange(true) { requestCount++ }

        assertEquals(1, requestCount)
        assertEquals(SpeechRecognitionUiState(), viewModel.uiState.value)
        verify(exactly = 1) { permissionRepository.isGranted() }
        verify(exactly = 0) { speechRepository.observeRecognition() }
        confirmVerified(permissionRepository, speechRepository)
    }

    @Test
    fun `権限が許可されたら聞き取りを開始する`() {
        val viewModel = createViewModel(granted = false)
        viewModel.onEnabledChange(true) {}

        viewModel.onPermissionResult(true)

        assertTrue(viewModel.uiState.value.enabled)
        assertEquals(SpeechRecognitionStatus.Starting, viewModel.uiState.value.status)
        verify(exactly = 1) { speechRepository.observeRecognition() }
        confirmVerified(speechRepository)
    }

    @Test
    fun `権限が拒否されたら権限未許可を表示して聞き取りは開始しない`() {
        val viewModel = createViewModel(granted = false)
        viewModel.onEnabledChange(true) {}

        viewModel.onPermissionResult(false)

        assertEquals(
            SpeechRecognitionUiState(enabled = false, status = SpeechRecognitionStatus.PermissionDenied),
            viewModel.uiState.value,
        )
        verify(exactly = 0) { speechRepository.observeRecognition() }
        confirmVerified(speechRepository)
    }

    @Test
    fun `OFFにすると聞き取りを止め履歴は残す`() {
        val viewModel = createViewModel(granted = true)
        viewModel.onEnabledChange(true) {}
        events.tryEmit(SpeechRecognitionEvent.Listening)
        events.tryEmit(SpeechRecognitionEvent.Partial("燃"))
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("燃料")))
        events.tryEmit(SpeechRecognitionEvent.Partial("タ"))

        viewModel.onEnabledChange(false) {}

        assertEquals(
            SpeechRecognitionUiState(
                enabled = false,
                status = SpeechRecognitionStatus.Stopped,
                recognized = listOf(listOf("燃料")),
            ),
            viewModel.uiState.value,
        )
        assertEquals(0, events.subscriptionCount.value)
    }

    @Test
    fun `停止中にstopを呼んでも表示中の状態は変えない`() {
        val viewModel = createViewModel(granted = false)
        viewModel.onEnabledChange(true) {}
        viewModel.onPermissionResult(false)

        viewModel.stop()

        assertEquals(SpeechRecognitionStatus.PermissionDenied, viewModel.uiState.value.status)
    }

    @Test
    fun `聞き取りの再開を知らせるListeningで認識途中の文字列を消す`() {
        val viewModel = createViewModel(granted = true)
        viewModel.onEnabledChange(true) {}
        events.tryEmit(SpeechRecognitionEvent.Listening)
        events.tryEmit(SpeechRecognitionEvent.Partial("燃"))

        events.tryEmit(SpeechRecognitionEvent.Listening)

        assertEquals("", viewModel.uiState.value.partialText)
        assertEquals(SpeechRecognitionStatus.Listening, viewModel.uiState.value.status)
    }

    @Test
    fun `権限の要求中に停止した場合は後から届いた許可で聞き取りを始めない`() {
        val viewModel = createViewModel(granted = false)
        viewModel.onEnabledChange(true) {}

        viewModel.stop()
        viewModel.onPermissionResult(true)

        assertEquals(SpeechRecognitionUiState(), viewModel.uiState.value)
        verify(exactly = 0) { speechRepository.observeRecognition() }
        confirmVerified(speechRepository)
    }

    @Test
    fun `要求していない権限の結果は無視する`() {
        val viewModel = createViewModel(granted = false)

        viewModel.onPermissionResult(true)
        viewModel.onPermissionResult(false)

        assertEquals(SpeechRecognitionUiState(), viewModel.uiState.value)
        verify(exactly = 0) { speechRepository.observeRecognition() }
        confirmVerified(speechRepository)
    }

    @Test
    fun `権限未許可の失敗を表示して停止状態にする`() {
        val viewModel = createViewModel(granted = true)
        viewModel.onEnabledChange(true) {}
        events.tryEmit(SpeechRecognitionEvent.Partial("燃"))

        events.tryEmit(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.PermissionDenied))

        assertEquals(
            SpeechRecognitionUiState(enabled = false, status = SpeechRecognitionStatus.PermissionDenied),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `利用不可の失敗を表示して停止状態にする`() {
        val viewModel = createViewModel(granted = true)
        viewModel.onEnabledChange(true) {}

        events.tryEmit(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.Unavailable))

        assertFalse(viewModel.uiState.value.enabled)
        assertEquals(SpeechRecognitionStatus.Unavailable, viewModel.uiState.value.status)
    }

    @Test
    fun `認識の購読が例外で終わった場合は例外を記録して認識失敗として停止状態にする`() {
        val viewModel = createViewModel(granted = true)
        val exception = IllegalStateException("失敗")
        every { speechRepository.observeRecognition() } returns flow { throw exception }
        val errorCapture = Class.forName("kurou.kodriver.domain.ErrorCaptureKt").kotlin
        mockkStatic(errorCapture)
        every { captureError(exception) } returns Unit
        try {
            viewModel.onEnabledChange(true) {}

            assertFalse(viewModel.uiState.value.enabled)
            assertEquals(SpeechRecognitionStatus.RecognitionFailed, viewModel.uiState.value.status)
            verify(exactly = 1) { captureError(exception) }
            confirmVerified(errorCapture)
        } finally {
            unmockkStatic(errorCapture)
        }
    }
}
