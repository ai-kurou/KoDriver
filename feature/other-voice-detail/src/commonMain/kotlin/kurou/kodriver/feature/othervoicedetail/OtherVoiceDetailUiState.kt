package kurou.kodriver.feature.othervoicedetail

import kurou.kodriver.domain.model.TextToSpeechVoice
import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED

/** OtherVoiceDetail 画面の表示状態。 */
data class OtherVoiceDetailUiState(
    val voices: List<TextToSpeechVoice> = emptyList(),
    val selectedVoiceId: String = VOICE_ID_UNSPECIFIED,
    val isLoading: Boolean = true,
    val savedVoiceMissing: Boolean = false,
)
