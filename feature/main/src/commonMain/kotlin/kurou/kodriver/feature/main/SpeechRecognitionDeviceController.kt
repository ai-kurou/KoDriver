package kurou.kodriver.feature.main

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.captureErrorTypeOnly

/**
 * 音声認識の入力デバイスの一覧取得と選択を扱い、画面の状態へ反映する。
 *
 * 聞き取り中に開くデバイスが変わった場合のやり直しは [restart] で呼び出し側に任せる。
 * デバイスを選ぶ操作で起動時の自動開始を取り消す処理は [cancelLaunch] で呼び出し側に任せる。
 */
internal class SpeechRecognitionDeviceController(
    private val scope: CoroutineScope,
    private val selector: AudioInputDeviceSelector,
    private val uiState: MutableStateFlow<SpeechRecognitionUiState>,
    private val cancelLaunch: () -> Unit,
    private val restart: () -> Unit,
) {
    private var refreshJob: Job? = null

    /** 入力デバイスの一覧を取得し直す。停止中でも選択中のデバイスを表示するため、カードの表示時とメニューを開いたときに呼ぶ。 */
    fun refresh() {
        refreshJob?.cancel()
        refreshJob =
            scope.launch {
                try {
                    val devices = selector.refresh()
                    val before = uiState.value.deviceName
                    uiState.update { it.copy(devices = devices.names, deviceName = devices.selected) }
                    // 聞き取り中に解決先のデバイスが変わった場合は、表示と取得元がずれないよう聞き取りをやり直す
                    if (uiState.value.enabled && before != null && devices.selected != before) restart()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // 保存値を読めなくても、聞き取りは止めない。次の取得で読み直す
                    captureErrorTypeOnly(e)
                }
            }
    }

    /** ドロップダウンで入力デバイスを選ぶ。聞き取り中の場合は、保存した選択で聞き取りをやり直す。 */
    fun select(name: String) {
        val previousName = uiState.value.deviceName
        cancelLaunch()
        refreshJob?.cancel()
        uiState.update { it.copy(deviceName = name) }
        scope.launch {
            try {
                selector.select(name)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // 保存できなかった選択では聞き取りをやり直さず、表示も元に戻す（再度選べば保存をやり直せる）
                captureErrorTypeOnly(e)
                uiState.update { it.copy(deviceName = previousName) }
                return@launch
            }
            // 認識は保存済みのデバイス名を読んで開くため、保存が終わってからやり直す
            if (name != previousName && uiState.value.enabled) restart()
        }
    }

    /** 聞き取りを始める前に一覧と選択を取り直し、画面の表示と実際に開くデバイスを同じ結果に揃える。 */
    suspend fun sync() {
        try {
            val devices = selector.refresh()
            uiState.update { it.copy(devices = devices.names, deviceName = devices.selected) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            captureErrorTypeOnly(e)
        }
    }
}
