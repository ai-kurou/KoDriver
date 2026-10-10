package kurou.kodriver.feature.othervoicepitchdetail

import kurou.kodriver.domain.model.VOICE_PITCH_DEFAULT

/**
 * OtherVoicePitchDetail 画面の表示状態。
 */
data class OtherVoicePitchDetailUiState(
    val pitch: Float = VOICE_PITCH_DEFAULT,
)
