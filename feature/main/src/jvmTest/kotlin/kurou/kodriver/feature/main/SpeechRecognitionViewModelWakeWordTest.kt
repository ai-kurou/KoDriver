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
import kurou.kodriver.domain.model.SpeechRecognitionMode
import kurou.kodriver.domain.model.registerAceWindowsReadoutItemKeys
import kurou.kodriver.domain.model.registerGt7Ps5ReadoutItemKeys
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys
import kurou.kodriver.domain.repository.RecordAudioPermissionRepository
import kurou.kodriver.domain.repository.SpeechRecognitionRepository
import kurou.kodriver.domain.usecase.CheckRecordAudioPermissionGrantedUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechPlaybackActiveUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechRecognitionUseCase
import kurou.kodriver.domain.usecase.RequestManualReadoutItemUseCase
import kurou.kodriver.domain.usecase.SetSpeechRecognitionModeUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions")
class SpeechRecognitionViewModelWakeWordTest {
    private val speechRepository: SpeechRecognitionRepository = mockk()
    private val permissionRepository: RecordAudioPermissionRepository = mockk()
    private val itemRequestRepository = FakeManualReadoutItemRequestRepository()
    private val modeRepository = FakeSpeechRecognitionModeRepository()
    private val playbackRepository = FakeSpeechPlaybackStateRepository()
    private val events = MutableSharedFlow<SpeechRecognitionEvent>(extraBufferCapacity = 16)
    private val dispatcher = UnconfinedTestDispatcher()
    private var viewModel: SpeechRecognitionViewModel? = null

    @BeforeTest
    fun setUp() {
        registerLmuWindowsReadoutItemKeys()
        registerGt7Ps5ReadoutItemKeys()
        registerAceWindowsReadoutItemKeys()
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        viewModel?.viewModelScope?.cancel()
        Dispatchers.resetMain()
    }

    private fun createListeningViewModel(): SpeechRecognitionViewModel {
        every { permissionRepository.isGranted() } returns true
        every { speechRepository.observeRecognition() } returns events
        return SpeechRecognitionViewModel(
            observeSpeechRecognition = ObserveSpeechRecognitionUseCase(speechRepository),
            checkRecordAudioPermissionGranted = CheckRecordAudioPermissionGrantedUseCase(permissionRepository),
            requestManualReadoutItem = RequestManualReadoutItemUseCase(itemRequestRepository),
            setSpeechRecognitionMode = SetSpeechRecognitionModeUseCase(modeRepository),
            observeSpeechPlaybackActive = ObserveSpeechPlaybackActiveUseCase(playbackRepository),
        ).also {
            viewModel = it
            it.onEnabledChange(true) {}
            events.tryEmit(SpeechRecognitionEvent.Listening)
        }
    }

    private fun advanceBy(millis: Long) {
        dispatcher.scheduler.advanceTimeBy(millis)
        dispatcher.scheduler.runCurrent()
    }

    private val status get() = viewModel?.uiState?.value?.status

    @Test
    fun `ウェイクワードを言う前は語句に一致しても依頼を発行しない`() {
        createListeningViewModel()

        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("燃料周回を教えて")))

        assertEquals(emptyList(), itemRequestRepository.requests)
        assertEquals(SpeechRecognitionStatus.Listening, status)
    }

    @Test
    fun `ウェイクワードの確定結果でコマンド待ちになりモードを切り替える`() {
        createListeningViewModel()

        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("ドライバー")))

        assertEquals(SpeechRecognitionStatus.CommandWaiting, status)
        assertEquals(listOf(SpeechRecognitionMode.Command), modeRepository.modes)
    }

    @Test
    fun `ウェイクワードの途中経過でもコマンド待ちになる`() {
        createListeningViewModel()

        events.tryEmit(SpeechRecognitionEvent.Partial("このどらいばー"))

        assertEquals(SpeechRecognitionStatus.CommandWaiting, status)
        assertEquals(listOf(SpeechRecognitionMode.Command), modeRepository.modes)
    }

    @Test
    fun `ウェイクワードを含まない途中経過や確定結果ではコマンド待ちにならない`() {
        createListeningViewModel()

        events.tryEmit(SpeechRecognitionEvent.Partial("燃料"))
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("こんにちは")))

        assertEquals(SpeechRecognitionStatus.Listening, status)
        assertEquals(emptyList(), modeRepository.modes)
    }

    @Test
    fun `コマンド待ちの間に語句に一致すると依頼を発行してウェイクワード待ちへ戻る`() {
        createListeningViewModel()
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("ドライバー")))

        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("じこべ")))

        assertEquals(3, itemRequestRepository.requests.size)
        assertEquals(SpeechRecognitionStatus.Listening, status)
        assertEquals(
            listOf(SpeechRecognitionMode.Command, SpeechRecognitionMode.WakeWord),
            modeRepository.modes,
        )
    }

    @Test
    fun `コマンド待ちの間に語句に一致しない確定結果が来てもコマンド待ちを続ける`() {
        createListeningViewModel()
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("ドライバー")))

        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("こんにちは")))

        assertEquals(emptyList(), itemRequestRepository.requests)
        assertEquals(SpeechRecognitionStatus.CommandWaiting, status)
        assertEquals(listOf(SpeechRecognitionMode.Command), modeRepository.modes)
    }

    @Test
    fun `コマンド待ちの間にもう一度ウェイクワードが来ても待ち時間は延ばさずモードも変えない`() {
        createListeningViewModel()
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("ドライバー")))
        advanceBy(SPEECH_RECOGNITION_COMMAND_TIMEOUT_MS - 1)

        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("ドライバー")))
        advanceBy(1)

        assertEquals(SpeechRecognitionStatus.Listening, status)
        assertEquals(
            listOf(SpeechRecognitionMode.Command, SpeechRecognitionMode.WakeWord),
            modeRepository.modes,
        )
    }

    @Test
    fun `コマンドが来ないまま時間切れになるとウェイクワード待ちへ戻る`() {
        createListeningViewModel()
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("ドライバー")))

        advanceBy(SPEECH_RECOGNITION_COMMAND_TIMEOUT_MS - 1)
        assertEquals(SpeechRecognitionStatus.CommandWaiting, status)

        advanceBy(1)
        assertEquals(SpeechRecognitionStatus.Listening, status)
        assertEquals(
            listOf(SpeechRecognitionMode.Command, SpeechRecognitionMode.WakeWord),
            modeRepository.modes,
        )
        assertTrue(viewModel?.uiState?.value?.enabled == true)
    }

    @Test
    fun `時間切れの後は語句に一致しても依頼を発行しない`() {
        createListeningViewModel()
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("ドライバー")))
        advanceBy(SPEECH_RECOGNITION_COMMAND_TIMEOUT_MS)

        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("じこべ")))

        assertEquals(emptyList(), itemRequestRepository.requests)
    }

    @Test
    fun `読み上げが始まるとコマンド待ちを打ち切って聞き取りを止める`() {
        createListeningViewModel()
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("ドライバー")))

        playbackRepository.update("lmu_windows", true)

        assertEquals(SpeechRecognitionStatus.Listening, status)
        assertEquals(
            listOf(SpeechRecognitionMode.Command, SpeechRecognitionMode.Paused),
            modeRepository.modes,
        )
        advanceBy(SPEECH_RECOGNITION_COMMAND_TIMEOUT_MS)
        assertEquals(listOf(SpeechRecognitionMode.Command, SpeechRecognitionMode.Paused), modeRepository.modes)
    }

    @Test
    fun `読み上げ中はウェイクワードや語句を受け付けない`() {
        createListeningViewModel()
        playbackRepository.update("lmu_windows", true)

        events.tryEmit(SpeechRecognitionEvent.Partial("ドライバー"))
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("ドライバー")))

        assertEquals(SpeechRecognitionStatus.Listening, status)
        assertEquals(listOf(SpeechRecognitionMode.Paused), modeRepository.modes)
        assertEquals(emptyList(), itemRequestRepository.requests)
    }

    @Test
    fun `読み上げが終わっても待ち時間が過ぎるまでは再開しない`() {
        createListeningViewModel()
        playbackRepository.update("lmu_windows", true)

        playbackRepository.update("lmu_windows", false)
        advanceBy(SPEECH_RECOGNITION_RESUME_DELAY_MS - 1)

        assertEquals(listOf(SpeechRecognitionMode.Paused), modeRepository.modes)

        advanceBy(1)

        assertEquals(listOf(SpeechRecognitionMode.Paused, SpeechRecognitionMode.WakeWord), modeRepository.modes)
    }

    @Test
    fun `再開を待つ間に次の読み上げが始まったら再開しない`() {
        createListeningViewModel()
        playbackRepository.update("lmu_windows", true)
        playbackRepository.update("lmu_windows", false)
        advanceBy(SPEECH_RECOGNITION_RESUME_DELAY_MS - 1)

        playbackRepository.update("lmu_windows", true)
        advanceBy(SPEECH_RECOGNITION_RESUME_DELAY_MS)

        assertEquals(listOf(SpeechRecognitionMode.Paused, SpeechRecognitionMode.Paused), modeRepository.modes)
    }

    @Test
    fun `再開したあとは再びウェイクワードを受け付ける`() {
        createListeningViewModel()
        playbackRepository.update("lmu_windows", true)
        playbackRepository.update("lmu_windows", false)
        advanceBy(SPEECH_RECOGNITION_RESUME_DELAY_MS)

        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("ドライバー")))

        assertEquals(SpeechRecognitionStatus.CommandWaiting, status)
    }

    @Test
    fun `停止するとコマンド待ちの時間切れや読み上げの購読をやめる`() {
        val viewModel = createListeningViewModel()
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("ドライバー")))

        viewModel.stop()
        modeRepository.modes.clear()
        advanceBy(SPEECH_RECOGNITION_COMMAND_TIMEOUT_MS)
        playbackRepository.update("lmu_windows", true)

        assertEquals(SpeechRecognitionStatus.Stopped, status)
        assertEquals(emptyList(), modeRepository.modes)
    }

    @Test
    fun `失敗で止まったあとは時間切れや読み上げでモードを切り替えない`() {
        createListeningViewModel()
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("ドライバー")))
        events.tryEmit(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.Unavailable))
        modeRepository.modes.clear()

        advanceBy(SPEECH_RECOGNITION_COMMAND_TIMEOUT_MS)
        playbackRepository.update("lmu_windows", true)

        assertEquals(SpeechRecognitionStatus.Unavailable, status)
        assertEquals(emptyList(), modeRepository.modes)
    }

    @Test
    fun `聞き取りをやり直すとウェイクワード待ちから始める`() {
        val viewModel = createListeningViewModel()
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("ドライバー")))

        viewModel.onEnabledChange(true) {}
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("じこべ")))

        assertEquals(emptyList(), itemRequestRepository.requests)
    }

    @Test
    fun `聞き取りを始めた時点で読み上げ中なら最初から止める`() {
        playbackRepository.update("lmu_windows", true)

        createListeningViewModel()

        assertEquals(listOf(SpeechRecognitionMode.Paused), modeRepository.modes)
    }

    @Test
    fun `無発話が続いても聞き取りを自動で止めない`() {
        createListeningViewModel()

        advanceBy(10 * 60 * 1_000L)

        assertEquals(SpeechRecognitionStatus.Listening, status)
        assertTrue(viewModel?.uiState?.value?.enabled == true)
    }
}
