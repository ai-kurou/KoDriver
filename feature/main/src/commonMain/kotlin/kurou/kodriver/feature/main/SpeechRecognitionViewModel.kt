package kurou.kodriver.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.captureError
import kurou.kodriver.domain.lmuwindows.model.matchLmuWindowsManualReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutItemRequest
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.model.SpeechRecognitionFailureReason
import kurou.kodriver.domain.usecase.CheckRecordAudioPermissionGrantedUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechRecognitionUseCase
import kurou.kodriver.domain.usecase.RequestManualReadoutItemUseCase

/** 認識途中の文字列も確定結果も届かないまま経つと、聞き取りを自動で止めるまでの時間。つけっぱなしを防ぐ。 */
internal const val SPEECH_RECOGNITION_IDLE_TIMEOUT_MS = 5 * 60 * 1_000L

/** 音声認識の常時聞き取りを開始・停止し、認識状況を確認用に保持する。 */
internal class SpeechRecognitionViewModel(
    private val observeSpeechRecognition: ObserveSpeechRecognitionUseCase,
    private val checkRecordAudioPermissionGranted: CheckRecordAudioPermissionGrantedUseCase,
    private val requestManualReadoutItem: RequestManualReadoutItemUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SpeechRecognitionUiState())
    val uiState: StateFlow<SpeechRecognitionUiState> = _uiState.asStateFlow()

    private var listenJob: Job? = null

    /** 発話がないまま [SPEECH_RECOGNITION_IDLE_TIMEOUT_MS] 経つと聞き取りを止める。認識途中の文字列や確定結果で数え直す。 */
    private var idleJob: Job? = null

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
        listenJob?.cancel()
        listenJob = null
        idleJob?.cancel()
        idleJob = null
    }

    private fun restartIdleTimer() {
        idleJob?.cancel()
        idleJob =
            viewModelScope.launch {
                delay(SPEECH_RECOGNITION_IDLE_TIMEOUT_MS)
                cancelJobs()
                _uiState.update {
                    it.copy(
                        enabled = false,
                        status = SpeechRecognitionStatus.IdleStopped,
                        partialText = "",
                        level = null,
                    )
                }
            }
    }

    private fun start() {
        cancelJobs()
        _uiState.update {
            it.copy(enabled = true, status = SpeechRecognitionStatus.Starting, partialText = "", level = null)
        }
        restartIdleTimer()
        listenJob =
            viewModelScope.launch {
                try {
                    observeSpeechRecognition().collect(::onEvent)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    captureError(e)
                    onFailed(SpeechRecognitionFailureReason.Unavailable)
                }
            }
    }

    private suspend fun onEvent(event: SpeechRecognitionEvent) {
        when (event) {
            SpeechRecognitionEvent.Listening -> {
                _uiState.update { it.copy(status = SpeechRecognitionStatus.Listening, partialText = "") }
            }

            is SpeechRecognitionEvent.Level -> {
                // 入力音量は発話の証拠にならない（環境音でも届く）ため、無発話の自動停止の数え直しには使わない
                _uiState.update { it.copy(level = event.level) }
            }

            is SpeechRecognitionEvent.Partial -> {
                restartIdleTimer()
                _uiState.update { it.copy(partialText = event.text) }
            }

            is SpeechRecognitionEvent.Recognized -> {
                restartIdleTimer()
                _uiState.update {
                    it.copy(
                        partialText = "",
                        recognized = (listOf(event.candidates) + it.recognized).take(SPEECH_RECOGNITION_HISTORY_MAX),
                    )
                }
                // 現在はLMUの語句だけを照合する。選択中のシミュレーターがLMUでなければ、LMUのNarrator側が依頼を無視する。
                matchLmuWindowsManualReadoutItemKey(event.candidates)?.let { itemKey ->
                    requestManualReadoutItem(ManualReadoutItemRequest(Simulator.LmuWindows.id, itemKey))
                }
            }

            is SpeechRecognitionEvent.Failed -> {
                onFailed(event.reason)
            }
        }
    }

    private fun onFailed(reason: SpeechRecognitionFailureReason) {
        idleJob?.cancel()
        idleJob = null
        _uiState.update {
            it.copy(
                enabled = false,
                partialText = "",
                level = null,
                status =
                    when (reason) {
                        SpeechRecognitionFailureReason.PermissionDenied -> SpeechRecognitionStatus.PermissionDenied
                        SpeechRecognitionFailureReason.Unavailable -> SpeechRecognitionStatus.Unavailable
                    },
            )
        }
    }
}
