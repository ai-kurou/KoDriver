package kurou.kodriver.feature.othervoicespeeddetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** 保存済みの読み上げ速度と試聴中の状態から画面状態を合成する冷たい Flow。 */
internal class OtherVoiceSpeedDetailFlow(
    speed: Flow<Float>,
    isPreviewing: Flow<Boolean>,
) {
    val uiState: Flow<OtherVoiceSpeedDetailUiState> =
        combine(speed, isPreviewing) { speed, previewing ->
            OtherVoiceSpeedDetailUiState(speed = speed, isPreviewing = previewing)
        }
}
