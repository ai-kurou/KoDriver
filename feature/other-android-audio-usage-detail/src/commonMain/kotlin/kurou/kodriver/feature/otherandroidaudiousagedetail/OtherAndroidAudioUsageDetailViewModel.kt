package kurou.kodriver.feature.otherandroidaudiousagedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.AndroidAudioUsage
import kurou.kodriver.domain.usecase.ObserveAndroidAudioUsageUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveAndroidAudioUsageUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

internal class OtherAndroidAudioUsageDetailViewModel(
    observeAndroidAudioUsage: ObserveAndroidAudioUsageUseCase,
    private val saveAndroidAudioUsage: SaveAndroidAudioUsageUseCase,
    private val speakText: SpeakTextUseCase,
    private val observeSoundVolume: ObserveSoundVolumeUseCase,
) : ViewModel() {
    private val previewingUsage = MutableStateFlow<AndroidAudioUsage?>(null)
    private var previewJob: Job? = null
    private var previewRequest = 0

    val uiState: StateFlow<OtherAndroidAudioUsageDetailUiState> =
        OtherAndroidAudioUsageDetailFlow(
            usage = observeAndroidAudioUsage(),
            previewingUsage = previewingUsage,
        ).uiState.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            OtherAndroidAudioUsageDetailUiState(),
        )

    fun onUsageSelected(usage: AndroidAudioUsage) {
        viewModelScope.launch { saveAndroidAudioUsage(usage) }
    }

    fun onPreviewClicked(
        usage: AndroidAudioUsage,
        text: String,
    ) {
        val stopPreview = previewingUsage.value == usage
        val request = ++previewRequest
        previewJob?.cancel()
        previewingUsage.update { null }
        if (stopPreview) return
        previewJob =
            viewModelScope.launch {
                try {
                    val volume = observeSoundVolume().first()
                    if (volume <= 0) return@launch
                    previewingUsage.update { usage }
                    speakText(text, volume = volume, audioUsage = usage)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // 試聴に失敗しても画面の操作を続けられるようにする。
                } finally {
                    if (previewRequest == request) previewingUsage.update { null }
                }
            }
    }

    fun onPreviewStopped() {
        previewRequest++
        previewJob?.cancel()
        previewingUsage.update { null }
    }
}
