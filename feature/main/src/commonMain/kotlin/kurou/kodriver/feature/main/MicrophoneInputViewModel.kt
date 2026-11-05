package kurou.kodriver.feature.main

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
import kurou.kodriver.domain.model.MicrophoneFailureReason
import kurou.kodriver.domain.usecase.GetAudioInputDevicesUseCase
import kurou.kodriver.domain.usecase.ObserveMicrophoneInputStatusUseCase
import kurou.kodriver.domain.model.MicrophoneInputStatus as DomainMicrophoneInputStatus

/** マイク入力の取得を開始・停止し、入力デバイス名・状態・音量を確認用に保持する。 */
internal class MicrophoneInputViewModel(
    private val observeMicrophoneInputStatus: ObserveMicrophoneInputStatusUseCase,
    private val getAudioInputDevices: GetAudioInputDevicesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MicrophoneInputUiState())
    val uiState: StateFlow<MicrophoneInputUiState> = _uiState.asStateFlow()

    private var captureJob: Job? = null

    /** アプリが見えなくなった時点で取得中だったか。前面に戻ったときに再開するかの判断に使う。 */
    private var resumeOnForeground = false

    fun onEnabledChange(enabled: Boolean) {
        if (enabled) start() else stop()
    }

    /** アプリが見えなくなったときに取得を止める。取得中だった場合は [onAppForegrounded] で再開する。 */
    fun onAppBackgrounded() {
        val wasEnabled = _uiState.value.enabled
        stop()
        resumeOnForeground = wasEnabled
    }

    fun onAppForegrounded() {
        if (!resumeOnForeground) return
        resumeOnForeground = false
        start()
    }

    /** 取得を止める。再開はしない。 */
    fun stop() {
        resumeOnForeground = false
        captureJob?.cancel()
        captureJob = null
        _uiState.update {
            if (it.enabled) {
                it.copy(enabled = false, status = MicrophoneInputStatus.Stopped, level = 0f)
            } else {
                it
            }
        }
    }

    private fun start() {
        captureJob?.cancel()
        _uiState.update {
            it.copy(enabled = true, status = MicrophoneInputStatus.Starting, level = 0f)
        }
        captureJob =
            viewModelScope.launch {
                try {
                    val deviceName = getAudioInputDevices().firstOrNull()?.name
                    _uiState.update { it.copy(deviceName = deviceName) }
                    observeMicrophoneInputStatus().collect(::onStatus)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    captureError(e)
                    onFailed(MicrophoneFailureReason.Unavailable)
                }
            }
    }

    private fun onStatus(status: DomainMicrophoneInputStatus) {
        when (status) {
            DomainMicrophoneInputStatus.Starting -> {
                _uiState.update { it.copy(status = MicrophoneInputStatus.Starting, level = 0f) }
            }

            is DomainMicrophoneInputStatus.Capturing -> {
                _uiState.update { it.copy(status = MicrophoneInputStatus.Capturing, level = status.level) }
            }

            is DomainMicrophoneInputStatus.Failed -> {
                onFailed(status.reason)
            }
        }
    }

    private fun onFailed(reason: MicrophoneFailureReason) {
        _uiState.update {
            it.copy(
                enabled = false,
                level = 0f,
                status =
                    when (reason) {
                        MicrophoneFailureReason.Unavailable -> MicrophoneInputStatus.Unavailable
                        MicrophoneFailureReason.NoSignal -> MicrophoneInputStatus.NoSignal
                    },
            )
        }
    }
}
