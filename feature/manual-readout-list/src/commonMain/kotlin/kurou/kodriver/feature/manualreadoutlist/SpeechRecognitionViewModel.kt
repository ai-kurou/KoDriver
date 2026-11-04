package kurou.kodriver.feature.manualreadoutlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.captureError
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.model.SpeechRecognitionFailureReason
import kurou.kodriver.domain.usecase.CheckRecordAudioPermissionGrantedUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechRecognitionUseCase

/** 音声認識の常時聞き取りを開始・停止し、認識状況を確認用に保持する。 */
internal class SpeechRecognitionViewModel(
    private val observeSpeechRecognition: ObserveSpeechRecognitionUseCase,
    private val checkRecordAudioPermissionGranted: CheckRecordAudioPermissionGrantedUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SpeechRecognitionUiState())
    val uiState: StateFlow<SpeechRecognitionUiState> = _uiState.asStateFlow()

    private var listenJob: Job? = null

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

    /** 聞き取りを止める。画面から離れたときやアプリが見えなくなったときにも呼ぶ。 */
    fun stop() {
        permissionRequestPending = false
        listenJob?.cancel()
        listenJob = null
        _uiState.update {
            if (it.enabled) {
                it.copy(enabled = false, status = SpeechRecognitionStatus.Stopped, partialText = "")
            } else {
                it
            }
        }
    }

    private fun start() {
        listenJob?.cancel()
        _uiState.update {
            it.copy(enabled = true, status = SpeechRecognitionStatus.Starting, partialText = "")
        }
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

    private fun onEvent(event: SpeechRecognitionEvent) {
        when (event) {
            SpeechRecognitionEvent.Listening -> {
                _uiState.update { it.copy(status = SpeechRecognitionStatus.Listening, partialText = "") }
            }

            is SpeechRecognitionEvent.Partial -> {
                _uiState.update { it.copy(partialText = event.text) }
            }

            is SpeechRecognitionEvent.Recognized -> {
                _uiState.update {
                    it.copy(
                        partialText = "",
                        recognized = (listOf(event.candidates) + it.recognized).take(SPEECH_RECOGNITION_HISTORY_MAX),
                    )
                }
            }

            is SpeechRecognitionEvent.Failed -> {
                onFailed(event.reason)
            }
        }
    }

    private fun onFailed(reason: SpeechRecognitionFailureReason) {
        _uiState.update {
            it.copy(
                enabled = false,
                partialText = "",
                status =
                    when (reason) {
                        SpeechRecognitionFailureReason.PermissionDenied -> SpeechRecognitionStatus.PermissionDenied
                        SpeechRecognitionFailureReason.Unavailable -> SpeechRecognitionStatus.Unavailable
                    },
            )
        }
    }
}
