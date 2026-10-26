package kurou.kodriver.feature.otherlist

import kurou.kodriver.domain.model.OVERLAY_BACKGROUND_OPACITY_DEFAULT
import kurou.kodriver.domain.model.OVERLAY_TEXT_COLOR_DEFAULT
import kurou.kodriver.domain.model.OVERLAY_TEXT_SIZE_DEFAULT
import kurou.kodriver.domain.model.OVERLAY_VISIBLE_DEFAULT
import kurou.kodriver.domain.model.OverlayTextSize

/** オーバーレイ設定フローの表示状態。 */
data class OtherListOverlaySettingsUiState(
    val overlayVisible: Boolean = OVERLAY_VISIBLE_DEFAULT,
    val overlayTextSize: OverlayTextSize = OVERLAY_TEXT_SIZE_DEFAULT,
    val overlayTextColor: Long = OVERLAY_TEXT_COLOR_DEFAULT,
    val overlayBackgroundOpacity: Int = OVERLAY_BACKGROUND_OPACITY_DEFAULT,
)
