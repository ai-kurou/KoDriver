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
import kurou.kodriver.domain.model.AudioInputDevice
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
    private var refreshJob: Job? = null

    /** ユーザーが選んだデバイス名。保存はせず、アプリを終了すると既定（先頭）に戻る。 */
    private var selectedDeviceName: String? = null

    /** アプリが見えなくなった時点で取得中だったか。前面に戻ったときに再開するかの判断に使う。 */
    private var resumeOnForeground = false

    fun onEnabledChange(enabled: Boolean) {
        if (enabled) start() else stop()
    }

    /** 入力デバイスの一覧を取得し直す。停止中でも選択中のデバイス名を表示できるよう、カードの表示時とメニューを開いたときに呼ぶ。 */
    fun refreshDevices() {
        refreshJob?.cancel()
        refreshJob =
            viewModelScope.launch {
                try {
                    val devices = getAudioInputDevices()
                    val before = _uiState.value.deviceName
                    val device = applyDevices(devices)
                    // 取得中に解決先のデバイスが変わった場合は、表示と取得元がずれないよう取得し直す
                    if (_uiState.value.enabled && before != null && device?.name != before) start()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    captureError(e)
                }
            }
    }

    /** ドロップダウンで入力デバイスを選ぶ。取得中の場合は選んだデバイスで取得し直す。 */
    fun onDeviceSelected(name: String) {
        selectedDeviceName = name
        if (name == _uiState.value.deviceName) return
        _uiState.update { it.copy(deviceName = name) }
        if (_uiState.value.enabled) start()
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
        // 取得の終了（マイクを閉じる処理）を次の開始が待てるよう、キャンセルしたジョブの参照は残す
        captureJob?.cancel()
        _uiState.update {
            if (it.enabled) {
                it.copy(enabled = false, status = MicrophoneInputStatus.Stopped, level = 0f)
            } else {
                it
            }
        }
    }

    private fun start() {
        // 古い一覧取得の結果が、新しく始める取得の表示を上書きしないようにする
        refreshJob?.cancel()
        val previous = captureJob
        previous?.cancel()
        _uiState.update {
            it.copy(enabled = true, status = MicrophoneInputStatus.Starting, level = 0f)
        }
        captureJob =
            viewModelScope.launch {
                try {
                    // 前の取得がマイクを閉じ終えてから開く。同じデバイスを同時に開けない環境で失敗しないようにする
                    previous?.join()
                    // 表示する名前と実際に取得するデバイスを一致させるため、解決したデバイスを明示して開く
                    val device = applyDevices(getAudioInputDevices())
                    observeMicrophoneInputStatus(device).collect(::onStatus)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    captureError(e)
                    onFailed(MicrophoneFailureReason.Unavailable)
                }
            }
    }

    /** 一覧を反映し、選択中（見つからなければ先頭）のデバイスを返す。 */
    private fun applyDevices(devices: List<AudioInputDevice>): AudioInputDevice? {
        val device = devices.firstOrNull { it.name == selectedDeviceName } ?: devices.firstOrNull()
        // 選択中のデバイスが一覧から消えたら選択を破棄し、再接続されても勝手に元へ戻らないようにする
        if (devices.isNotEmpty() && device?.name != selectedDeviceName) selectedDeviceName = null
        _uiState.update { it.copy(devices = devices.map(AudioInputDevice::name), deviceName = device?.name) }
        return device
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
