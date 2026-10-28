package kurou.kodriver.feature.otheroverlaytextsizedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.OverlayTextSize

/** 適用中の文字サイズと未保存の選択値から画面状態を合成する冷たい Flow。 */
internal class OtherOverlayTextSizeDetailFlow(
    observedOverlayTextSize: Flow<OverlayTextSize>,
    pendingOverlayTextSize: Flow<OverlayTextSize?>,
) {
    val uiState: Flow<OtherOverlayTextSizeDetailUiState> =
        combine(observedOverlayTextSize, pendingOverlayTextSize) { observed, pending ->
            OtherOverlayTextSizeDetailUiState(
                selectedOverlayTextSize = observed,
                pendingOverlayTextSize = pending ?: observed,
            )
        }
}
