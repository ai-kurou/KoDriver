package kurou.kodriver.feature.othervoicespeeddetail

import kurou.kodriver.domain.model.VOICE_SPEED_DEFAULT

/**
 * OtherVoiceSpeedDetail 画面の表示状態。
 */
data class OtherVoiceSpeedDetailUiState(
    val speed: Float = VOICE_SPEED_DEFAULT,
    val isPreviewing: Boolean = false,
)
