package kurou.kodriver.feature.othervoicedetail

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
import kurou.kodriver.domain.model.TextToSpeechVoice
import kurou.kodriver.domain.usecase.GetAvailableVoicesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceUseCase
import kurou.kodriver.domain.usecase.SaveVoiceUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

internal class OtherVoiceDetailViewModel(
    private val getAvailableVoices: GetAvailableVoicesUseCase,
    observeVoice: ObserveVoiceUseCase,
    private val saveVoice: SaveVoiceUseCase,
    private val speakText: SpeakTextUseCase,
    private val observeSoundVolume: ObserveSoundVolumeUseCase,
) : ViewModel() {
    private val previewingVoiceId = MutableStateFlow<String?>(null)
    private var previewJob: Job? = null
    private var previewRequest = 0
    private val refreshTrigger = MutableStateFlow(0)

    val uiState: StateFlow<OtherVoiceDetailUiState> =
        OtherVoiceDetailFlow(
            refreshTrigger = refreshTrigger,
            loadVoices = ::loadVoices,
            savedVoiceId = observeVoice(),
            previewingVoiceId = previewingVoiceId,
        ).uiState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OtherVoiceDetailUiState())

    /** 取得に失敗しても取得完了（空の一覧）として扱い、「再読み込み」から再試行できるようにする。 */
    private suspend fun loadVoices(): List<TextToSpeechVoice> =
        try {
            getAvailableVoices()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            captureOtherVoiceDetailError(e)
            emptyList()
        }

    fun onVoiceSelected(id: String) {
        viewModelScope.launch {
            try {
                saveVoice(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                captureOtherVoiceDetailError(e)
            }
        }
    }

    fun onPreviewClicked(
        voiceId: String,
        text: String,
    ) {
        val stopPreview = previewingVoiceId.value == voiceId
        val request = ++previewRequest
        previewJob?.cancel()
        previewingVoiceId.update { null }
        if (stopPreview) return
        previewJob =
            viewModelScope.launch {
                try {
                    val volume = observeSoundVolume().first()
                    if (volume <= 0) return@launch
                    previewingVoiceId.update { voiceId }
                    speakText(text, volume = volume, voiceId = voiceId)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // 試聴に失敗しても画面の操作を続けられるようにする。
                } finally {
                    if (previewRequest == request) previewingVoiceId.update { null }
                }
            }
    }

    /** 再生中または開始待ちの試聴を止める。 */
    fun onPreviewStopped() {
        previewRequest++
        previewJob?.cancel()
        previewingVoiceId.update { null }
    }

    fun onRetryClicked() {
        refreshTrigger.update { it + 1 }
    }
}
