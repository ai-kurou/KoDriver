package kurou.kodriver.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kurou.kodriver.domain.captureErrorTypeOnly
import kurou.kodriver.domain.launchCapturingErrors
import kurou.kodriver.domain.model.SPEECH_RECOGNITION_COMMAND_TIMEOUT_MS
import kurou.kodriver.domain.model.SPEECH_RECOGNITION_HISTORY_MAX
import kurou.kodriver.domain.model.SPEECH_RECOGNITION_RESUME_DELAY_MS
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.model.SpeechRecognitionFailureReason
import kurou.kodriver.domain.model.SpeechRecognitionTranscript
import kurou.kodriver.domain.model.containsSpeechRecognitionWakeWord
import kurou.kodriver.domain.runCapturingErrors
import kurou.kodriver.domain.usecase.CheckRecordAudioPermissionGrantedUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechPlaybackActiveUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechRecognitionUseCase
import kurou.kodriver.domain.usecase.PlaySpeechRecognitionChimeUseCase
import kurou.kodriver.domain.usecase.ReportSpeechRecognitionTranscriptUseCase
import kurou.kodriver.domain.usecase.RequestManualReadoutItemUseCase
import kurou.kodriver.domain.usecase.SetSpeechRecognitionModeUseCase

/**
 * 音声認識の聞き取りを開始・停止し、認識状況を確認用に保持する。
 *
 * ウェイクワードを待つ→検出したらコマンドを [SPEECH_RECOGNITION_COMMAND_TIMEOUT_MS] だけ待つ→ウェイクワード待ちへ戻る、
 * の順に動く。コマンドは待っている間だけ読み上げ項目の語句と照合する。読み上げ中は自分の音声を拾わないよう聞き取りを止める。
 */
@Suppress("LongParameterList")
internal class SpeechRecognitionViewModel(
    private val observeSpeechRecognition: ObserveSpeechRecognitionUseCase,
    private val checkRecordAudioPermissionGranted: CheckRecordAudioPermissionGrantedUseCase,
    private val requestManualReadoutItem: RequestManualReadoutItemUseCase,
    private val setSpeechRecognitionMode: SetSpeechRecognitionModeUseCase,
    private val observeSpeechPlaybackActive: ObserveSpeechPlaybackActiveUseCase,
    private val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    private val playChime: PlaySpeechRecognitionChimeUseCase,
    private val reportSpeechRecognitionTranscript: ReportSpeechRecognitionTranscriptUseCase,
    /** 入力デバイスの一覧と選択。デバイスを選べないプラットフォーム（Android）では null。 */
    private val audioInputDeviceSelector: AudioInputDeviceSelector? = null,
) : ViewModel() {
    val uiState: StateFlow<SpeechRecognitionUiState>
        field = MutableStateFlow(SpeechRecognitionUiState())

    private var listenJob: Job? = null

    /** 次の聞き取りが開始前に待つ、マイクを閉じ終えていない前の聞き取り。 */
    private var startWaits: List<Job> = emptyList()
    private val commandWait =
        CommandWaitController(
            scope = viewModelScope,
            setMode = setSpeechRecognitionMode::invoke,
            playChime = playChime::invoke,
            onCommandWaitStarted = {
                uiState.update { it.copy(status = SpeechRecognitionStatus.CommandWaiting, partialText = "") }
            },
            onCommandWaitEnded = {
                uiState.update { it.copy(status = SpeechRecognitionStatus.Listening, partialText = "") }
            },
        )

    private val commandHandler =
        SpeechRecognitionCommandHandler(requestManualReadoutItem, observeSelectedSimulator, commandWait)

    /** 入力デバイスを選べないプラットフォーム（Android）では null。 */
    private val deviceController =
        audioInputDeviceSelector?.let {
            SpeechRecognitionDeviceController(
                scope = viewModelScope,
                selector = it,
                uiState = uiState,
                cancelLaunch = ::cancelLaunch,
                restart = ::start,
            )
        }

    /** 直近に確定した発話の先頭候補。次の発話が確定するか、聞き取りの開始・停止・失敗まで残す。 */
    private val latestRecognizedText = MutableStateFlow<String?>(null)

    init {
        // オーバーレイなど他の画面へ、聞き取った文言を共有する。聞き取りを行っていない間は空にする
        viewModelScope.launchCapturingErrors {
            combine(
                uiState.map { it.enabled to it.partialText }.distinctUntilChanged(),
                latestRecognizedText,
            ) { (enabled, partialText), recognizedText ->
                if (enabled) SpeechRecognitionTranscript(partialText, recognizedText) else SpeechRecognitionTranscript()
            }.distinctUntilChanged().collect { reportSpeechRecognitionTranscript(it) }
        }
    }

    /** 読み上げの再生状態を購読し、再生中は聞き取りを止める。 */
    private var playbackJob: Job? = null

    /** アプリが見えなくなった時点で聞き取り中だったか。前面に戻ったときに聞き取りを再開するかの判断に使う。 */
    private var resumeOnForeground = false

    /** 権限の要求結果を待っている間だけ true。停止すると無効になり、後から届いた許可では聞き取りを始めない。 */
    private var permissionRequestPending = false

    /** 起動時の自動開始を判断済みか。画面の再生成で呼び直されても、一度しか開始しない。 */
    private var launchHandled = false

    /** 起動時の自動開始が入力デバイスの取得を待っている間のジョブ。ユーザーの操作や停止で取り消し、後から開始させない。 */
    private var launchJob: Job? = null

    /**
     * アプリの起動時に一度だけ呼び、条件を満たせば聞き取りを自動で始める。前面に戻ったときの再開は [onAppForegrounded] が担う。
     * 権限が未許可なら何もしない（起動時に権限ダイアログは出さない）。
     * 入力デバイスを選べるプラットフォームでは、ユーザーがデバイスを選んで保存している場合だけ始める
     * （保存名が一覧になければ先頭のデバイスで開く）。開けなければ通常の失敗状態になり、やり直さない。
     */
    fun onAppLaunched() {
        if (launchHandled) return
        launchHandled = true
        if (!checkRecordAudioPermissionGranted()) return
        val selector = audioInputDeviceSelector
        if (selector == null) {
            start()
            return
        }
        launchJob =
            viewModelScope.launch {
                val devices =
                    try {
                        selector.refresh()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        captureErrorTypeOnly(e)
                        return@launch
                    }
                // 取得中の操作（スイッチ・デバイス選択・停止・背面化）はこのジョブを取り消すため、ここへ来るのは何も操作されなかった場合だけ
                if (devices.hasSavedSelection && devices.selected != null) start()
            }
    }

    /** スイッチの操作。マイクの権限が未許可なら [requestPermission] で要求し、結果を [onPermissionResult] で受ける。 */
    fun onEnabledChange(
        enabled: Boolean,
        requestPermission: () -> Unit,
    ) {
        cancelLaunch()
        when {
            !enabled -> {
                stop()
            }

            checkRecordAudioPermissionGranted() -> {
                start()
            }

            else -> {
                permissionRequestPending = true
                requestPermission()
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        if (!permissionRequestPending) return
        permissionRequestPending = false
        if (granted) {
            start()
        } else {
            uiState.update { it.copy(enabled = false, status = SpeechRecognitionStatus.PermissionDenied) }
        }
    }

    /** 入力デバイスの一覧を取得し直す。デバイスを選べないプラットフォームでは何もしない。停止中でも選択中のデバイスを表示するため、カードの表示時とメニューを開いたときに呼ぶ。 */
    fun refreshDevices() {
        deviceController?.refresh()
    }

    /** ドロップダウンで入力デバイスを選ぶ。聞き取り中の場合は、保存した選択で聞き取りをやり直す。 */
    fun onDeviceSelected(name: String) {
        deviceController?.select(name)
    }

    /** アプリが見えなくなったときに聞き取りを止める。聞き取り中だった場合は [onAppForegrounded] で再開する。 */
    fun onAppBackgrounded() {
        val wasEnabled = uiState.value.enabled
        stop()
        resumeOnForeground = wasEnabled
    }

    /** アプリが前面に戻ったとき、見えなくなる前に聞き取り中だったなら再開する。権限が外されていれば再開しない。 */
    fun onAppForegrounded() {
        if (!resumeOnForeground) return
        resumeOnForeground = false
        if (checkRecordAudioPermissionGranted()) start()
    }

    /** 聞き取りを止める。再開はしない。 */
    fun stop() {
        cancelLaunch()
        resumeOnForeground = false
        permissionRequestPending = false
        cancelJobs()
        latestRecognizedText.value = null
        uiState.update {
            if (it.enabled) {
                it.copy(enabled = false, status = SpeechRecognitionStatus.Stopped, partialText = "", level = null)
            } else {
                it
            }
        }
    }

    /** 入力デバイスの取得を待っている起動時の自動開始を取り消す。 */
    private fun cancelLaunch() {
        launchJob?.cancel()
        launchJob = null
    }

    private fun cancelJobs() {
        // 止めた聞き取りがマイクを閉じ終えるまで、次の開始が待てるように覚えておく
        startWaits = (listOfNotNull(listenJob) + startWaits).filter { !it.isCompleted }
        listenJob?.cancel()
        listenJob = null
        playbackJob?.cancel()
        playbackJob = null
        commandWait.reset()
    }

    private fun observePlayback() {
        playbackJob?.cancel()
        playbackJob =
            viewModelScope.launchCapturingErrors {
                var wasActive = false
                observeSpeechPlaybackActive().collectLatest { active ->
                    runCapturingErrors {
                        if (active) {
                            wasActive = true
                            commandWait.pause()
                        } else if (wasActive) {
                            wasActive = false
                            // 読み上げの残響が消えるまで待ってから再開する。待つ間に次の読み上げが始まれば、collectLatest がこの待ちを取り消す
                            delay(SPEECH_RECOGNITION_RESUME_DELAY_MS)
                            commandWait.resume()
                        }
                    }
                }
            }
    }

    private fun start() {
        // 待機中にキャンセルされた開始も含め、マイクを閉じ終えていない前の聞き取りをすべて待つ
        cancelJobs()
        val waits = startWaits
        latestRecognizedText.value = null
        uiState.update {
            it.copy(enabled = true, status = SpeechRecognitionStatus.Starting, partialText = "", level = null)
        }
        // 前の聞き取りのモードを引き継がず、ウェイクワード待ちから始める。読み上げ中なら observePlayback が続けて止める
        commandWait.applyMode()
        observePlayback()
        listenJob =
            viewModelScope.launch {
                try {
                    // 前の聞き取りがマイクを閉じ終えてから開く。同じデバイスを同時に開けない環境で失敗しないようにする
                    waits.joinAll()
                    deviceController?.sync()
                    observeSpeechRecognition().collect(::onEvent)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    captureErrorTypeOnly(e)
                    onFailed(SpeechRecognitionFailureReason.RecognitionFailed)
                }
            }
    }

    private suspend fun onEvent(event: SpeechRecognitionEvent) {
        when (event) {
            SpeechRecognitionEvent.Listening -> {
                // マイクの再取得でも届くため、コマンド待ちの間はその表示を保つ
                val status =
                    if (commandWait.commandWaiting) {
                        SpeechRecognitionStatus.CommandWaiting
                    } else {
                        SpeechRecognitionStatus.Listening
                    }
                uiState.update { it.copy(status = status, partialText = "") }
            }

            is SpeechRecognitionEvent.Level -> {
                // 入力音量は発話の証拠にならない（環境音でも届く）ため、無発話の自動停止の数え直しには使わない
                uiState.update { it.copy(level = event.level) }
            }

            is SpeechRecognitionEvent.Partial -> {
                uiState.update { it.copy(partialText = event.text) }
                // 呼びかけを言い終える前の途中経過で検出し、続けて話すコマンドを取りこぼさない
                val idle = !commandWait.paused && !commandWait.commandWaiting
                if (idle && containsSpeechRecognitionWakeWord(listOf(event.text))) {
                    commandWait.startCommandWait()
                }
            }

            is SpeechRecognitionEvent.Recognized -> {
                latestRecognizedText.value = event.candidates.first()
                uiState.update {
                    it.copy(
                        partialText = "",
                        recognized = (listOf(event.candidates) + it.recognized).take(SPEECH_RECOGNITION_HISTORY_MAX),
                    )
                }
                commandHandler.onRecognizedCandidates(event.candidates)
            }

            is SpeechRecognitionEvent.Failed -> {
                onFailed(event.reason)
            }
        }
    }

    private fun onFailed(reason: SpeechRecognitionFailureReason) {
        playbackJob?.cancel()
        playbackJob = null
        commandWait.reset()
        latestRecognizedText.value = null
        uiState.update {
            it.copy(
                enabled = false,
                partialText = "",
                level = null,
                status = reason.toStatus(),
            )
        }
    }
}

private fun SpeechRecognitionFailureReason.toStatus(): SpeechRecognitionStatus =
    when (this) {
        SpeechRecognitionFailureReason.PermissionDenied -> SpeechRecognitionStatus.PermissionDenied
        SpeechRecognitionFailureReason.Unavailable -> SpeechRecognitionStatus.Unavailable
        SpeechRecognitionFailureReason.MicrophoneUnavailable -> SpeechRecognitionStatus.MicrophoneUnavailable
        SpeechRecognitionFailureReason.NoSignal -> SpeechRecognitionStatus.NoSignal
        SpeechRecognitionFailureReason.RecognitionFailed -> SpeechRecognitionStatus.RecognitionFailed
    }
