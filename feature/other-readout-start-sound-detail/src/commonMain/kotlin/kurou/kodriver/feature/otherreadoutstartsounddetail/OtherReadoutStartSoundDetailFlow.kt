package kurou.kodriver.feature.otherreadoutstartsounddetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.ReadoutStartSoundType

/** 保存済みの開始音種別と未確定の選択から画面状態を合成する冷たい Flow。 */
internal class OtherReadoutStartSoundDetailFlow(
    savedType: Flow<ReadoutStartSoundType>,
    pendingType: Flow<ReadoutStartSoundType?>,
) {
    val uiState: Flow<OtherReadoutStartSoundDetailUiState> =
        combine(savedType, pendingType) { saved, pending ->
            OtherReadoutStartSoundDetailUiState(
                selectedType = saved,
                pendingType = pending ?: saved,
            )
        }
}
