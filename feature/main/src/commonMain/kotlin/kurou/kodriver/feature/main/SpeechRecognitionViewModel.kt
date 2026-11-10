package kurou.kodriver.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kurou.kodriver.domain.acewindows.model.matchAceWindowsManualReadoutItemKey
import kurou.kodriver.domain.captureErrorTypeOnly
import kurou.kodriver.domain.gt7ps5.model.matchGt7Ps5ManualReadoutItemKey
import kurou.kodriver.domain.launchCapturingErrors
import kurou.kodriver.domain.lmuwindows.model.matchLmuWindowsManualReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutItemRequest
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.model.SpeechRecognitionFailureReason
import kurou.kodriver.domain.model.containsSpeechRecognitionWakeWord
import kurou.kodriver.domain.runCapturingErrors
import kurou.kodriver.domain.usecase.CheckRecordAudioPermissionGrantedUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechPlaybackActiveUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechRecognitionUseCase
import kurou.kodriver.domain.usecase.PlaySpeechRecognitionChimeUseCase
import kurou.kodriver.domain.usecase.RequestManualReadoutItemUseCase
import kurou.kodriver.domain.usecase.SetSpeechRecognitionModeUseCase

/** ウェイクワードを検出してから、コマンドを待つ時間。過ぎるとウェイクワード待ちへ戻る。 */
internal const val SPEECH_RECOGNITION_COMMAND_TIMEOUT_MS = 6_000L

/** 読み上げが終わってから聞き取りを再開するまでの待ち時間。読み上げ直後の残響をウェイクワードと誤認しないための余裕。 */
internal const val SPEECH_RECOGNITION_RESUME_DELAY_MS = 500L

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
    /** 入力デバイスの一覧と選択。デバイスを選べないプラットフォーム（Android）では null。 */
    private val audioInputDeviceSelector: AudioInputDeviceSelector? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SpeechRecognitionUiState())
    val uiState: StateFlow<SpeechRecognitionUiState> = _uiState.asStateFlow()

    private var listenJob: Job? = null

    /** 次の聞き取りが開始前に待つ、マイクを閉じ終えていない前の聞き取り。 */
    private var startWaits: List<Job> = emptyList()
    private var refreshJob: Job? = null

    private val commandWait =
        CommandWaitController(
            scope = viewModelScope,
            setMode = setSpeechRecognitionMode::invoke,
            playChime = playChime::invoke,
            onCommandWaitStarted = {
                _uiState.update { it.copy(status = SpeechRecognitionStatus.CommandWaiting, partialText = "") }
            },
            onCommandWaitEnded = {
                _uiState.update { it.copy(status = SpeechRecognitionStatus.Listening, partialText = "") }
            },
        )

    /** 読み上げの再生状態を購読し、再生中は聞き取りを止める。 */
    private var playbackJob: Job? = null

    /** アプリが見えなくなった時点で聞き取り中だったか。前面に戻ったときに聞き取りを再開するかの判断に使う。 */
    private var resumeOnForeground = false

    /** 権限の要求結果を待っている間だけ true。停止すると無効になり、後から届いた許可では聞き取りを始めない。 */
    private var permissionRequestPending = false

    /** スイッチの操作。マイクの権限が未許可なら [requestPermission] で要求し、結果を [onPermissionResult] で受ける。 */
    fun onEnabledChange(
        enabled: Boolean,
        requestPermission: () -> Unit,
    ) {
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
            _uiState.update { it.copy(enabled = false, status = SpeechRecognitionStatus.PermissionDenied) }
        }
    }

    /** 入力デバイスの一覧を取得し直す。デバイスを選べないプラットフォームでは何もしない。停止中でも選択中のデバイスを表示するため、カードの表示時とメニューを開いたときに呼ぶ。 */
    fun refreshDevices() {
        val selector = audioInputDeviceSelector ?: return
        refreshJob?.cancel()
        refreshJob =
            viewModelScope.launch {
                try {
                    val devices = selector.refresh()
                    val before = _uiState.value.deviceName
                    _uiState.update { it.copy(devices = devices.names, deviceName = devices.selected) }
                    // 聞き取り中に解決先のデバイスが変わった場合は、表示と取得元がずれないよう聞き取りをやり直す
                    if (_uiState.value.enabled && before != null && devices.selected != before) start()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // 保存値を読めなくても、聞き取りは止めない。次の取得で読み直す
                    captureErrorTypeOnly(e)
                }
            }
    }

    /** ドロップダウンで入力デバイスを選ぶ。聞き取り中の場合は、保存した選択で聞き取りをやり直す。 */
    fun onDeviceSelected(name: String) {
        val selector = audioInputDeviceSelector ?: return
        val previousName = _uiState.value.deviceName
        refreshJob?.cancel()
        _uiState.update { it.copy(deviceName = name) }
        viewModelScope.launch {
            try {
                selector.select(name)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // 保存できなかった選択では聞き取りをやり直さず、表示も元に戻す（再度選べば保存をやり直せる）
                captureErrorTypeOnly(e)
                _uiState.update { it.copy(deviceName = previousName) }
                return@launch
            }
            // 認識は保存済みのデバイス名を読んで開くため、保存が終わってからやり直す
            if (name != previousName && _uiState.value.enabled) start()
        }
    }

    /** アプリが見えなくなったときに聞き取りを止める。聞き取り中だった場合は [onAppForegrounded] で再開する。 */
    fun onAppBackgrounded() {
        val wasEnabled = _uiState.value.enabled
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
        resumeOnForeground = false
        permissionRequestPending = false
        cancelJobs()
        _uiState.update {
            if (it.enabled) {
                it.copy(enabled = false, status = SpeechRecognitionStatus.Stopped, partialText = "", level = null)
            } else {
                it
            }
        }
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
        _uiState.update {
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
                    syncDeviceDisplay()
                    observeSpeechRecognition().collect(::onEvent)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    captureErrorTypeOnly(e)
                    onFailed(SpeechRecognitionFailureReason.RecognitionFailed)
                }
            }
    }

    /** 聞き取りを始める前に一覧と選択を取り直し、画面の表示と実際に開くデバイスを同じ結果に揃える。 */
    private suspend fun syncDeviceDisplay() {
        val selector = audioInputDeviceSelector ?: return
        try {
            val devices = selector.refresh()
            _uiState.update { it.copy(devices = devices.names, deviceName = devices.selected) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            captureErrorTypeOnly(e)
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
                _uiState.update { it.copy(status = status, partialText = "") }
            }

            is SpeechRecognitionEvent.Level -> {
                // 入力音量は発話の証拠にならない（環境音でも届く）ため、無発話の自動停止の数え直しには使わない
                _uiState.update { it.copy(level = event.level) }
            }

            is SpeechRecognitionEvent.Partial -> {
                _uiState.update { it.copy(partialText = event.text) }
                // 呼びかけを言い終える前の途中経過で検出し、続けて話すコマンドを取りこぼさない
                val idle = !commandWait.paused && !commandWait.commandWaiting
                if (idle && containsSpeechRecognitionWakeWord(listOf(event.text))) {
                    commandWait.startCommandWait()
                }
            }

            is SpeechRecognitionEvent.Recognized -> {
                _uiState.update {
                    it.copy(
                        partialText = "",
                        recognized = (listOf(event.candidates) + it.recognized).take(SPEECH_RECOGNITION_HISTORY_MAX),
                    )
                }
                onRecognizedCandidates(event.candidates)
            }

            is SpeechRecognitionEvent.Failed -> {
                onFailed(event.reason)
            }
        }
    }

    private suspend fun onRecognizedCandidates(candidates: List<String>) {
        when {
            commandWait.paused -> {}

            !commandWait.commandWaiting -> {
                if (containsSpeechRecognitionWakeWord(candidates)) commandWait.startCommandWait()
            }

            else -> {
                try {
                    if (requestManualReadoutItem.requestMatchedReadouts(
                            observeSelectedSimulator().first(),
                            candidates,
                        )
                    ) {
                        commandWait.finishCommandWait()
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // 1回の依頼に失敗しても聞き取り全体は止めない。種類だけ記録してウェイクワード待ちへ戻る
                    captureErrorTypeOnly(e)
                    commandWait.finishCommandWait()
                }
            }
        }
    }

    private fun onFailed(reason: SpeechRecognitionFailureReason) {
        playbackJob?.cancel()
        playbackJob = null
        commandWait.reset()
        _uiState.update {
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

/**
 * 選択中のシミュレーターの語句だけを照合し、一致したら依頼を発行する。依頼したら true。
 * 選択中でないシミュレーターの語句は照合しない（そのNarratorは依頼を無視するため、コマンド待ちだけが終わってしまう）。
 * 音声認識の読み上げでは開始音を鳴らさない（playStartSound = false）。
 */
private suspend fun RequestManualReadoutItemUseCase.requestMatchedReadouts(
    selected: Simulator,
    candidates: List<String>,
): Boolean {
    val itemKey =
        when (selected) {
            Simulator.LmuWindows -> matchLmuWindowsManualReadoutItemKey(candidates)
            Simulator.Gt7Ps5 -> matchGt7Ps5ManualReadoutItemKey(candidates)
            Simulator.AceWindows -> matchAceWindowsManualReadoutItemKey(candidates)
        } ?: return false
    this(ManualReadoutItemRequest(selected.id, itemKey, playStartSound = false))
    return true
}
