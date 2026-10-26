package kurou.kodriver.feature.otherlist

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.usecase.ObserveOverlayBackgroundOpacityUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayTextColorUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayTextSizeUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayVisibleUseCase
import kurou.kodriver.domain.usecase.SaveOverlayVisibleUseCase

/** オーバーレイ設定の観測・表示状態と表示ON/OFFの保存を管理する。実行のライフサイクルは呼び出し元に従う。 */
class OtherListOverlaySettingsFlow(
    private val observeOverlayVisible: ObserveOverlayVisibleUseCase,
    private val saveOverlayVisible: SaveOverlayVisibleUseCase,
    private val observeOverlayTextSize: ObserveOverlayTextSizeUseCase,
    private val observeOverlayTextColor: ObserveOverlayTextColorUseCase,
    private val observeOverlayBackgroundOpacity: ObserveOverlayBackgroundOpacityUseCase,
) {
    val uiState: Flow<OtherListOverlaySettingsUiState> =
        combine(
            observeOverlayVisible(),
            observeOverlayTextSize(),
            observeOverlayTextColor(),
            observeOverlayBackgroundOpacity(),
        ) { overlayVisible, overlayTextSize, overlayTextColor, overlayBackgroundOpacity ->
            OtherListOverlaySettingsUiState(
                overlayVisible,
                overlayTextSize,
                overlayTextColor,
                overlayBackgroundOpacity,
            )
        }

    suspend fun onOverlayVisibleChange(visible: Boolean) = saveOverlayVisible(visible)
}
