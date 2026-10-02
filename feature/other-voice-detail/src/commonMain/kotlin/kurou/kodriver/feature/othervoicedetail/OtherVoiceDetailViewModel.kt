package kurou.kodriver.feature.othervoicedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.TextToSpeechVoice
import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED
import kurou.kodriver.domain.usecase.GetAvailableVoicesUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceUseCase
import kurou.kodriver.domain.usecase.SaveVoiceUseCase

internal class OtherVoiceDetailViewModel(
    private val getAvailableVoices: GetAvailableVoicesUseCase,
    observeVoice: ObserveVoiceUseCase,
    private val saveVoice: SaveVoiceUseCase,
) : ViewModel() {
    private val refreshTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<OtherVoiceDetailUiState> =
        combine(
            refreshTrigger.flatMapLatest {
                flow {
                    emit(OtherVoiceDetailUiState())
                    emit(OtherVoiceDetailUiState(voices = loadVoices(), isLoading = false))
                }
            },
            observeVoice(),
        ) { state, voiceId ->
            state.copy(
                selectedVoiceId = voiceId,
                savedVoiceMissing =
                    !state.isLoading && voiceId != VOICE_ID_UNSPECIFIED && state.voices.none { it.id == voiceId },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OtherVoiceDetailUiState())

    /** 取得に失敗しても取得完了（空の一覧）として扱い、「再読み込み」から再試行できるようにする。 */
    private suspend fun loadVoices(): List<TextToSpeechVoice> =
        try {
            getAvailableVoices()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            emptyList()
        }

    fun onVoiceSelected(id: String) {
        viewModelScope.launch { saveVoice(id) }
    }

    fun onRetryClicked() {
        refreshTrigger.update { it + 1 }
    }
}
