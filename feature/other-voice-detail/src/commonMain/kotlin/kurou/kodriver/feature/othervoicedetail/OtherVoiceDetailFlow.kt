package kurou.kodriver.feature.othervoicedetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kurou.kodriver.domain.model.TextToSpeechVoice
import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED

/** 購読・再読み込みごとに音声一覧を取得し、保存済み音声ID・試聴中IDと画面状態を合成する cold Flow。 */
internal class OtherVoiceDetailFlow(
    refreshTrigger: Flow<Int>,
    loadVoices: suspend () -> List<TextToSpeechVoice>,
    savedVoiceId: Flow<String>,
    previewingVoiceId: Flow<String?>,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val loadState: Flow<OtherVoiceDetailUiState> =
        refreshTrigger.flatMapLatest {
            flow {
                emit(OtherVoiceDetailUiState())
                emit(OtherVoiceDetailUiState(voices = loadVoices(), isLoading = false))
            }
        }

    val uiState: Flow<OtherVoiceDetailUiState> =
        combine(loadState, savedVoiceId, previewingVoiceId) { state, voiceId, previewingId ->
            state.copy(
                selectedVoiceId = voiceId,
                previewingVoiceId = previewingId,
                savedVoiceMissing =
                    !state.isLoading && voiceId != VOICE_ID_UNSPECIFIED && state.voices.none { it.id == voiceId },
            )
        }
}
