package kurou.kodriver.feature.othervoicepitchdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** 保存済みの声の高さと試聴状態から画面状態を合成する冷たい Flow。 */
internal class OtherVoicePitchDetailFlow(
    pitch: Flow<Float>,
    isPreviewing: Flow<Boolean>,
) {
    val uiState: Flow<OtherVoicePitchDetailUiState> =
        combine(pitch, isPreviewing) { pitch, previewing ->
            OtherVoicePitchDetailUiState(pitch = pitch, isPreviewing = previewing)
        }
}
