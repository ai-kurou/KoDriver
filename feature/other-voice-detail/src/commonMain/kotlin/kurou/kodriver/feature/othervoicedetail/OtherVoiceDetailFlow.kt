package kurou.kodriver.feature.othervoicedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED

/** 音声一覧のロード状態、保存済み音声ID、試聴中IDから画面状態を合成する冷たい Flow。 */
internal class OtherVoiceDetailFlow(
    loadState: Flow<OtherVoiceDetailUiState>,
    savedVoiceId: Flow<String>,
    previewingVoiceId: Flow<String?>,
) {
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
