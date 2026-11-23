package kurou.kodriver.feature.othermicrophonecutquietsounddetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** 保存済みの段階値から画面状態を生成する冷たい Flow。 */
internal class OtherMicrophoneCutQuietSoundDetailFlow(
    observedLevel: Flow<Int>,
) {
    val uiState: Flow<OtherMicrophoneCutQuietSoundDetailUiState> =
        observedLevel.map { level -> OtherMicrophoneCutQuietSoundDetailUiState(level = level) }
}
