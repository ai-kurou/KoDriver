package kurou.kodriver.feature.otheroverlaytextcolordetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** 観測した文字色と未保存の選択から画面状態を合成する冷たい Flow。 */
internal class OtherOverlayTextColorDetailFlow(
    observedOverlayTextColor: Flow<Long>,
    pendingOverlayTextColor: Flow<Long?>,
) {
    val uiState: Flow<OtherOverlayTextColorDetailUiState> =
        combine(observedOverlayTextColor, pendingOverlayTextColor) { observed, pending ->
            OtherOverlayTextColorDetailUiState(
                selectedOverlayTextColor = observed,
                pendingOverlayTextColor = pending ?: observed,
            )
        }
}
