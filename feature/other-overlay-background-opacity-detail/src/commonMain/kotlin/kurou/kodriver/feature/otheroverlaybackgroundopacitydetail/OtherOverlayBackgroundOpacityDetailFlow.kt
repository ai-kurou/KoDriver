package kurou.kodriver.feature.otheroverlaybackgroundopacitydetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** 適用中の背景透明度から画面状態を生成する冷たい Flow。 */
internal class OtherOverlayBackgroundOpacityDetailFlow(
    observedOverlayBackgroundOpacity: Flow<Int>,
) {
    val uiState: Flow<OtherOverlayBackgroundOpacityDetailUiState> =
        observedOverlayBackgroundOpacity.map { opacity ->
            OtherOverlayBackgroundOpacityDetailUiState(opacity = opacity)
        }
}
