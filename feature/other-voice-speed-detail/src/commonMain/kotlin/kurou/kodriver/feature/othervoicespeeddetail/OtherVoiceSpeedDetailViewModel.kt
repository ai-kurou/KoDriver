package kurou.kodriver.feature.othervoicespeeddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceSpeedUseCase
import kurou.kodriver.domain.usecase.SaveVoiceSpeedUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.math.roundToInt

internal class OtherVoiceSpeedDetailViewModel(
    observeVoiceSpeed: ObserveVoiceSpeedUseCase,
    private val saveVoiceSpeed: SaveVoiceSpeedUseCase,
    private val speakText: SpeakTextUseCase,
    private val observeSoundVolume: ObserveSoundVolumeUseCase,
) : ViewModel() {
    private val isPreviewing = MutableStateFlow(false)
    private var previewJob: Job? = null
    private var saveJob: Job? = null
    private var previewRequest = 0

    val uiState: StateFlow<OtherVoiceSpeedDetailUiState> =
        combine(observeVoiceSpeed(), isPreviewing) { speed, previewing ->
            OtherVoiceSpeedDetailUiState(speed = speed, isPreviewing = previewing)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            OtherVoiceSpeedDetailUiState(),
        )

    fun onSpeedChanged(speed: Float) {
        saveJob = viewModelScope.launch { saveVoiceSpeed((speed * 10).roundToInt() / 10f) }
    }

    /** ペインを離れたときに、再生中の試聴を止める。 */
    fun onPreviewStopped() {
        previewRequest++
        previewJob?.cancel()
        isPreviewing.update { false }
    }

    /** 試聴中に呼ぶと停止し、停止中に呼ぶと保存済みの速度とボイスで [text] を読み上げる。 */
    fun onPreviewClicked(text: String) {
        val stopPreview = isPreviewing.value
        val request = ++previewRequest
        previewJob?.cancel()
        isPreviewing.update { false }
        if (stopPreview) return
        previewJob =
            viewModelScope.launch {
                try {
                    // 保存済みの速度を読み直すため、直前のスライダー操作の保存が終わってから読み上げる。
                    saveJob?.join()
                    val volume = observeSoundVolume().first()
                    if (volume <= 0) return@launch
                    isPreviewing.update { true }
                    speakText(text, volume = volume)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // 試聴に失敗しても画面の操作を続けられるようにする。
                } finally {
                    if (previewRequest == request) isPreviewing.update { false }
                }
            }
    }
}
