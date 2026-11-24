package kurou.kodriver.feature.othermicrophonecutquietsounddetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** 保存済みの段階値と「コマンド受付中もカットする」から画面状態を生成する冷たい Flow。 */
internal class OtherMicrophoneCutQuietSoundDetailFlow(
    observedLevel: Flow<Int>,
    observedCutInCommand: Flow<Boolean>,
) {
    val uiState: Flow<OtherMicrophoneCutQuietSoundDetailUiState> =
        combine(observedLevel, observedCutInCommand) { level, cutInCommand ->
            OtherMicrophoneCutQuietSoundDetailUiState(level = level, cutInCommand = cutInCommand)
        }
}
