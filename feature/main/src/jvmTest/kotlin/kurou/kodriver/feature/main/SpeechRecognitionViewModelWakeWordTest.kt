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
import kurou.kodriver.domain.model.SpeechRecognitionChime
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.model.SpeechRecognitionFailureReason
import kurou.kodriver.domain.model.SpeechRecognitionMode
import kurou.kodriver.domain.model.registerAceWindowsReadoutItemKeys
import kurou.kodriver.domain.model.registerGt7Ps5ReadoutItemKeys
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys
import kurou.kodriver.domain.repository.RecordAudioPermissionRepository
import kurou.kodriver.domain.repository.SpeechRecognitionRepository
import kurou.kodriver.domain.usecase.CheckRecordAudioPermissionGrantedUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechPlaybackActiveUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechRecognitionUseCase
import kurou.kodriver.domain.usecase.PlaySpeechRecognitionChimeUseCase
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
    private val chimePlayer = FakeSpeechRecognitionChimePlayer()
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

    /** [clearModes] が true なら、開始時に伝えたモードを記録から消す（開始後のモード切り替えだけを検証するため）。 */
    private fun createListeningViewModel(clearModes: Boolean = true): SpeechRecognitionViewModel {
        every { permissionRepository.isGranted() } returns true
        every { speechRepository.observeRecognition() } returns events
        return SpeechRecognitionViewModel(
            observeSpeechRecognition = ObserveSpeechRecognitionUseCase(speechRepository),
            checkRecordAudioPermissionGranted = CheckRecordAudioPermissionGrantedUseCase(permissionRepository),
            requestManualReadoutItem = RequestManualReadoutItemUseCase(itemRequestRepository),
            setSpeechRecognitionMode = SetSpeechRecognitionModeUseCase(modeRepository),
            observeSpeechPlaybackActive = ObserveSpeechPlaybackActiveUseCase(playbackRepository),
            observeSelectedSimulator = ObserveSelectedSimulatorUseCase(FakeSelectedSimulatorRepository()),
            playChime = PlaySpeechRecognitionChimeUseCase(chimePlayer),
        ).also {
            viewModel = it
            it.onEnabledChange(true) {}
            events.tryEmit(SpeechRecognitionEvent.Listening)
            if (clearModes) modeRepository.modes.clear()
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

        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))

        assertEquals(SpeechRecognitionStatus.CommandWaiting, status)
        assertEquals(listOf(SpeechRecognitionMode.Command), modeRepository.modes)
    }

    @Test
    fun `ウェイクワードを検出すると確認音を1回鳴らす`() {
        createListeningViewModel()

        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))

        assertEquals(1, chimePlayer.playCount)
    }

    @Test
    fun `ウェイクワードの途中経過で検出しても確認音は1回で、続く確定結果では鳴らさない`() {
        createListeningViewModel()

        events.tryEmit(SpeechRecognitionEvent.Partial("コドライバー"))
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))

        assertEquals(1, chimePlayer.playCount)
    }

    @Test
    fun `ウェイクワードを検出していないときは確認音を鳴らさない`() {
        createListeningViewModel()

        events.tryEmit(SpeechRecognitionEvent.Partial("燃料"))
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("こんにちは")))

        assertEquals(0, chimePlayer.playCount)
    }

    @Test
    fun `確認音の再生に失敗してもコマンド待ちを続け、コマンドを受け付ける`() {
        chimePlayer.failure = IllegalStateException("chime failed")
        createListeningViewModel()

        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))

        assertEquals(1, chimePlayer.playCount)
        assertEquals(SpeechRecognitionStatus.CommandWaiting, status)
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("燃料周回を教えて")))
        assertEquals(1, itemRequestRepository.requests.size)
    }

    @Test
    fun `ウェイクワードの途中経過でもコマンド待ちになる`() {
        createListeningViewModel()

        events.tryEmit(SpeechRecognitionEvent.Partial("このドライバー"))

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
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))

        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("じこべ")))

        assertEquals(1, itemRequestRepository.requests.size)
        assertEquals(SpeechRecognitionStatus.Listening, status)
        assertEquals(
            listOf(SpeechRecognitionMode.Command, SpeechRecognitionMode.WakeWord),
            modeRepository.modes,
        )
    }

    @Test
    fun `コマンド待ちの間に語句に一致しない確定結果が来てもコマンド待ちを続ける`() {
        createListeningViewModel()
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))

        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("こんにちは")))

        assertEquals(emptyList(), itemRequestRepository.requests)
        assertEquals(SpeechRecognitionStatus.CommandWaiting, status)
        assertEquals(listOf(SpeechRecognitionMode.Command), modeRepository.modes)
    }

    @Test
    fun `コマンド待ちの間にもう一度ウェイクワードが来ても待ち時間は延ばさずモードも変えない`() {
        createListeningViewModel()
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))
        advanceBy(SPEECH_RECOGNITION_COMMAND_TIMEOUT_MS - 1)

        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))
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
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))

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
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))
        advanceBy(SPEECH_RECOGNITION_COMMAND_TIMEOUT_MS)

        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("じこべ")))

        assertEquals(emptyList(), itemRequestRepository.requests)
    }

    @Test
    fun `読み上げが始まるとコマンド待ちを打ち切って聞き取りを止める`() {
        createListeningViewModel()
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))

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
    fun `確認音が鳴っている間に読み上げが始まったら確認音を止める`() {
        chimePlayer.holdPlayback = true
        createListeningViewModel()
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))
        assertEquals(false, chimePlayer.cancelled)

        playbackRepository.update("lmu_windows", true)

        assertEquals(true, chimePlayer.cancelled)
    }

    @Test
    fun `確認音が鳴っている間にコマンド待ちが時間切れになったら確認音を止める`() {
        chimePlayer.holdPlayback = true
        createListeningViewModel()
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))

        advanceBy(SPEECH_RECOGNITION_COMMAND_TIMEOUT_MS)

        assertEquals(true, chimePlayer.cancelled)
    }

    @Test
    fun `コマンドが一致しないまま時間切れになると下降音を1回鳴らす`() {
        createListeningViewModel()
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))
        assertEquals(listOf(SpeechRecognitionChime.WakeWordDetected), chimePlayer.played)

        advanceBy(SPEECH_RECOGNITION_COMMAND_TIMEOUT_MS)

        assertEquals(
            listOf(SpeechRecognitionChime.WakeWordDetected, SpeechRecognitionChime.CommandTimedOut),
            chimePlayer.played,
        )
        assertEquals(SpeechRecognitionStatus.Listening, status)
    }

    @Test
    fun `時間切れの前に待ちを終えたときは下降音を鳴らさない`() {
        createListeningViewModel()
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("燃料周回を教えて")))

        advanceBy(SPEECH_RECOGNITION_COMMAND_TIMEOUT_MS)

        assertEquals(listOf(SpeechRecognitionChime.WakeWordDetected), chimePlayer.played)
    }

    @Test
    fun `時間切れの前に読み上げが始まったときは下降音を鳴らさない`() {
        createListeningViewModel()
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))
        playbackRepository.update("lmu_windows", true)

        advanceBy(SPEECH_RECOGNITION_COMMAND_TIMEOUT_MS)

        assertEquals(listOf(SpeechRecognitionChime.WakeWordDetected), chimePlayer.played)
    }

    @Test
    fun `下降音が鳴っている間に読み上げが始まったら下降音を止める`() {
        chimePlayer.holdPlayback = true
        createListeningViewModel()
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))
        advanceBy(SPEECH_RECOGNITION_COMMAND_TIMEOUT_MS)
        assertEquals(listOf(SpeechRecognitionChime.WakeWordDetected), chimePlayer.cancelledChimes)

        playbackRepository.update("lmu_windows", true)

        assertEquals(
            listOf(SpeechRecognitionChime.WakeWordDetected, SpeechRecognitionChime.CommandTimedOut),
            chimePlayer.cancelledChimes,
        )
    }

    @Test
    fun `下降音の再生に失敗しても聞き取りを続け、次のウェイクワードを受け付ける`() {
        chimePlayer.failure = IllegalStateException("chime failed")
        val viewModel = createListeningViewModel()
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))

        advanceBy(SPEECH_RECOGNITION_COMMAND_TIMEOUT_MS)
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))

        assertEquals(true, viewModel.uiState.value.enabled)
        assertEquals(SpeechRecognitionStatus.CommandWaiting, status)
    }

    @Test
    fun `読み上げ中はウェイクワードや語句を受け付けない`() {
        createListeningViewModel()
        playbackRepository.update("lmu_windows", true)

        events.tryEmit(SpeechRecognitionEvent.Partial("コドライバー"))
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))

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

        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))

        assertEquals(SpeechRecognitionStatus.CommandWaiting, status)
    }

    @Test
    fun `停止するとコマンド待ちの時間切れや読み上げの購読をやめる`() {
        val viewModel = createListeningViewModel()
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))

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
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))
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
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))

        viewModel.onEnabledChange(true) {}
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("じこべ")))

        assertEquals(emptyList(), itemRequestRepository.requests)
    }

    @Test
    fun `聞き取りを始めるとウェイクワード待ちのモードを伝える`() {
        createListeningViewModel(clearModes = false)

        assertEquals(listOf(SpeechRecognitionMode.WakeWord), modeRepository.modes)
    }

    @Test
    fun `聞き取りを始めた時点で読み上げ中ならウェイクワード待ちを伝えたあと続けて止める`() {
        playbackRepository.update("lmu_windows", true)

        createListeningViewModel(clearModes = false)

        assertEquals(listOf(SpeechRecognitionMode.WakeWord, SpeechRecognitionMode.Paused), modeRepository.modes)
    }

    @Test
    fun `コマンド待ちのまま聞き取りをやり直すとウェイクワード待ちのモードから始める`() {
        val viewModel = createListeningViewModel()
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))
        modeRepository.modes.clear()

        viewModel.onEnabledChange(true) {}

        assertEquals(listOf(SpeechRecognitionMode.WakeWord), modeRepository.modes)
    }

    @Test
    fun `コマンド待ちの間にマイクの再取得でListeningが届いてもコマンド待ちの表示を保つ`() {
        createListeningViewModel()
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))

        events.tryEmit(SpeechRecognitionEvent.Listening)

        assertEquals(SpeechRecognitionStatus.CommandWaiting, status)
    }

    @Test
    fun `無発話が続いても聞き取りを自動で止めない`() {
        createListeningViewModel()

        advanceBy(10 * 60 * 1_000L)

        assertEquals(SpeechRecognitionStatus.Listening, status)
        assertTrue(viewModel?.uiState?.value?.enabled == true)
    }
}
